package com.pentaware.foodie.ui.location;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.pentaware.foodie.R;
import com.pentaware.foodie.adapters.SearchLocationAdapter;
import com.pentaware.foodie.api.FoodieApi;
import com.pentaware.foodie.api.RetrofitInstance;
import com.pentaware.foodie.databinding.DialogLocationPermissionNeededBinding;
import com.pentaware.foodie.databinding.FragmentSearchLocationBinding;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.api.geocoding.GeocodingResponse;
import com.pentaware.foodie.models.api.geocoding.GeocodingResult;
import com.pentaware.foodie.models.maps.LocationCoordinates;
import com.pentaware.foodie.models.maps.PlacePrediction;
import com.pentaware.foodie.utils.PermissionUtils;
import com.pentaware.foodie.utils.events.PlacePredictionClickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint({"SetTextI18n", "MissingPermission"})
public class SearchLocationFragment extends Fragment {
    private static final String TAG = "SearchLocationFragmentyy";
    private FragmentSearchLocationBinding binding;

    private SearchLocationAdapter searchLocationAdapter;

    private FoodieApi foodieApi;

    private PlacesClient placesClient;
    private AutocompleteSessionToken token;
    private Handler placePredictionsHandler;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private PermissionUtils permissionUtils;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchLocationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        permissionUtils = PermissionUtils.getInstance(getContext());

        foodieApi = RetrofitInstance.getRetrofitInstance(getContext()).create(FoodieApi.class);

        Places.initialize(getContext().getApplicationContext(), getString(R.string.api_key));
        placesClient = Places.createClient(getContext());
        token = AutocompleteSessionToken.newInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        setOnClickListeners();
        setupSearchLocationRecyclerView();
        showLocationPermissionRequiredTextIfNeeded();

        setupSearchAutocomplete();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location currentLocation = null;

                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "Latitude: " +
                            location.getLatitude() + " Longitude: " + location.getLongitude());

                    currentLocation = location;
                }

                if(currentLocation != null) {
                    stopLocationUpdates();
                    getPlaceFromLatLngAndNavigate(currentLocation);
                }
            }
        };
    }

    private void setOnClickListeners() {
        binding.llCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndGetCurrentLocation();
            }
        });
    }

    private void setupSearchLocationRecyclerView() {
        searchLocationAdapter = new SearchLocationAdapter(Collections.emptyList(), new PlacePredictionClickEvent() {
            @Override
            public void onItemClick(PlacePrediction prediction) {
                Log.d(TAG, "onItemClick: " + prediction.placeName);
                getLatLngFromPlaceIdAndNavigate(prediction);
            }
        });

        binding.rvSearchLocation.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSearchLocation.setAdapter(searchLocationAdapter);
    }

    private void setupSearchAutocomplete() {
        placePredictionsHandler = new Handler();
        binding.progressSearchLocation.setVisibility(View.GONE);

        binding.etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty()) {
                    return;
                }

                binding.progressSearchLocation.setVisibility(View.VISIBLE);

                placePredictionsHandler.removeCallbacksAndMessages(null);
                placePredictionsHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getPlacesPredictions(s.toString());
                    }
                }, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void getPlacesPredictions(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setCountry("in")
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if(task.isSuccessful()) {
                            updatePredictionsInRecyclerView(task.getResult().getAutocompletePredictions());
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                            binding.progressSearchLocation.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void getLatLngFromPlaceIdAndNavigate(PlacePrediction prediction) {
        Call<GeocodingResponse> call = foodieApi.getLatLngFromPlaceId(prediction.placeId);

        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if(!response.isSuccessful()) {
                    Log.d(TAG, response.errorBody().toString());
                    return;
                }

                List<GeocodingResult> geocodingResults = response.body().getGeocodingResultList();

                LocationCoordinates locationCoordinates = geocodingResults.get(0)
                        .geometry.location;

                Address searchAddress = geocodingResults.get(0).getPlaceAddress();
                searchAddress.addressLine2 = prediction.placeName;
                searchAddress.latitude = locationCoordinates.getLat();
                searchAddress.longitude = locationCoordinates.getLng();

                navigateToSetLocationOnMapFragment(searchAddress);
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void getPlaceFromLatLngAndNavigate(Location location) {
        String latLngString = location.getLatitude() + "," + location.getLongitude();
        Call<GeocodingResponse> call = foodieApi.getPlaceFromLatLng(latLngString);

        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if(!response.isSuccessful()) {
                    Log.d(TAG, response.errorBody().toString());
                    return;
                }

                List<GeocodingResult> geocodingResults = response.body().getGeocodingResultList();

                LocationCoordinates locationCoordinates = geocodingResults.get(0)
                        .geometry.location;

                Address searchAddress = geocodingResults.get(0).getPlaceAddress();
                searchAddress.latitude = locationCoordinates.getLat();
                searchAddress.longitude = locationCoordinates.getLng();

                navigateToSetLocationOnMapFragment(searchAddress);
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void updatePredictionsInRecyclerView(List<AutocompletePrediction> autocompletePredictions) {
        List<PlacePrediction> placePredictionList = new ArrayList<>();

        for(AutocompletePrediction prediction: autocompletePredictions) {
            Log.i(TAG, prediction.getPlaceId());
            Log.i(TAG, prediction.getFullText(null).toString());

            PlacePrediction placePrediction = new PlacePrediction(
                    prediction.getPlaceId(),
                    prediction.getPrimaryText(null).toString(),
                    prediction.getSecondaryText(null).toString(),
                    prediction.getFullText(null).toString()
            );

            placePredictionList.add(placePrediction);
        }

        searchLocationAdapter.predictions = placePredictionList;
        searchLocationAdapter.notifyDataSetChanged();
        binding.progressSearchLocation.setVisibility(View.GONE);
    }

    private void checkPermissionsAndGetCurrentLocation() {
        if(!permissionUtils.fineLocationPermissionGranted() ||
                !permissionUtils.coarseLocationPermissionGranted()) {
            showLocationPermissionDialog();
            return;
        }

        createLocationRequest();
    }

    private void navigateToSetLocationOnMapFragment(Address address) {
        Log.d(TAG, "navigateToSetLocationOnMapFragment: Called " + address);

        Navigation.findNavController(getView())
                .navigate(SearchLocationFragmentDirections
                        .actionNavSearchLocationToNavSetLocationOnMap(address));
    }

    private void checkRequiredLocationPermissionsAndRequest() {
        List<String> permissionsToRequest = new ArrayList<>();

        if(!permissionUtils.fineLocationPermissionGranted()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(!permissionUtils.coarseLocationPermissionGranted()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        requestLocationPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
    }

    private void showLocationPermissionRequiredTextIfNeeded() {
        if(permissionUtils.fineLocationPermissionGranted() &&
                permissionUtils.coarseLocationPermissionGranted()) {
            binding.txtLocationPermissionRequired.setVisibility(View.GONE);

        } else {
            binding.txtLocationPermissionRequired.setVisibility(View.VISIBLE);

        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());

        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        startLocationUpdates();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Inside Location Setting Failure " + e);

                        if(e instanceof ResolvableApiException) {
                            ResolvableApiException resolvable = (ResolvableApiException) e;

                            IntentSenderRequest request =
                                    new IntentSenderRequest.Builder(resolvable.getResolution()).build();
                            resolutionForResultLauncher.launch(request);
                        }
                    }
                });
    }

    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: Called");
        Toast.makeText(getContext(), "Fetching current location", Toast.LENGTH_SHORT).show();

        if(fusedLocationProviderClient != null && permissionUtils.fineLocationPermissionGranted() &&
                permissionUtils.coarseLocationPermissionGranted()) {

            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        }
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: Called");

        if(fusedLocationProviderClient != null && permissionUtils.fineLocationPermissionGranted() &&
                permissionUtils.coarseLocationPermissionGranted()) {

            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private ActivityResultLauncher<String[]> requestLocationPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    showLocationPermissionRequiredTextIfNeeded();

                    if(permissionUtils.fineLocationPermissionGranted()
                            && permissionUtils.coarseLocationPermissionGranted()) {

                        createLocationRequest();
                        return;
                    }

                    if(!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                            !permissionUtils.fineLocationPermissionGranted()) {
                        permissionUtils.setPermissionPermanentlyDenied(Manifest.permission.ACCESS_FINE_LOCATION);
                    }

                    if(!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                            !permissionUtils.coarseLocationPermissionGranted()) {
                        permissionUtils.setPermissionPermanentlyDenied(Manifest.permission.ACCESS_COARSE_LOCATION);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> appPermissionSettingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    permissionUtils.updateDeniedPermissionsList();
                    showLocationPermissionRequiredTextIfNeeded();

                    Log.d(TAG, "onActivityResult: " + permissionUtils.getDeniedPermissionsList());

                    if(permissionUtils.fineLocationPermissionGranted() ||
                            permissionUtils.coarseLocationPermissionGranted()) {

                        createLocationRequest();
                    }
                }
            }
    );
    
    private ActivityResultLauncher<IntentSenderRequest> resolutionForResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Location Request OK");
                        startLocationUpdates();

                    } else {
                        Log.d(TAG, "Location Request CANCEL");
                    }
                }
            }
    );

    private void showLocationPermissionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        DialogLocationPermissionNeededBinding dialogBinding = 
                DialogLocationPermissionNeededBinding.inflate(getLayoutInflater(),
                binding.getRoot(), false);
        dialogBuilder.setView(dialogBinding.getRoot());

        List<String> deniedPermissions = permissionUtils.getDeniedPermissionsList();
        boolean permissionPermanentlyDenied = deniedPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) ||
                deniedPermissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION);

        Log.d(TAG, "showLocationPermissionDialog: deniedPermissions: " + deniedPermissions);
        Log.d(TAG, "showLocationPermissionDialog: permissionPermanentlyDenied: " + permissionPermanentlyDenied);

        if(permissionPermanentlyDenied) {
            Log.d(TAG, "showLocationPermissionDialog: Permanent Denial");
            dialogBinding.btnContinue.setText("Settings");
            dialogBinding.txtInfo.setText(R.string.location_permission_needed_after_permanent_denial);
        }

        AlertDialog locationPermissionDialog = dialogBuilder.create();
        locationPermissionDialog.setCancelable(false);

        dialogBinding.btnNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationPermissionDialog.dismiss();
            }
        });

        dialogBinding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationPermissionDialog.dismiss();

                if(permissionPermanentlyDenied) {
                    openAppPermissionSettings();
                    return;
                }

                checkRequiredLocationPermissionsAndRequest();
            }
        });

        locationPermissionDialog.show();
    }

    private void openAppPermissionSettings() {
        appPermissionSettingsLauncher.launch(permissionUtils.getAppLocationSettingsIntent());
    }
}
