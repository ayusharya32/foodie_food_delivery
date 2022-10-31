package com.pentaware.foodie.ui.location;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pentaware.foodie.R;
import com.pentaware.foodie.api.FoodieApi;
import com.pentaware.foodie.api.RetrofitInstance;
import com.pentaware.foodie.baseactivity.HomeActivity;
import com.pentaware.foodie.baseactivity.SplashActivity;
import com.pentaware.foodie.databinding.FragmentSetLocationOnMapBinding;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.User;
import com.pentaware.foodie.models.api.geocoding.GeocodingResponse;
import com.pentaware.foodie.models.api.geocoding.GeocodingResult;
import com.pentaware.foodie.models.maps.LocationCoordinates;
import com.pentaware.foodie.utils.CommonMethods;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;
import com.pentaware.foodie.utils.enums.AddressType;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetLocationOnMapFragment extends Fragment {
    private static final String TAG = "SetLocationOnMapyyy";
    private static final int MAP_ZOOM = 15;
    
    private FragmentSetLocationOnMapBinding binding;

    private Address searchAddress;
    private boolean firstOnMapIdleCall;

    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;

    private FoodieApi foodieApi;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSetLocationOnMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchAddress = SetLocationOnMapFragmentArgs.fromBundle(getArguments()).getSearchAddress();
        foodieApi = RetrofitInstance.getRetrofitInstance(getContext()).create(FoodieApi.class);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupLocationDetails();
        setupMap();

        setOnClickListeners();
    }

    private void setupLocationDetails() {
        String locationName;
        String locationAddress;

        if(searchAddress.addressLine2.contains(",")) {
            locationName = searchAddress.addressLine2.split(",")[0].trim();

            String remainingAddress = searchAddress.addressLine2.split(",")[1].trim();
            locationAddress = remainingAddress + "\n" + searchAddress.city + ", " + searchAddress.state +
                    ", " + searchAddress.pinCode;

        } else {
            locationName = searchAddress.addressLine2;
            locationAddress = searchAddress.city + ", " + searchAddress.state + "\n"
                    + searchAddress.pinCode;
        }

        binding.txtLocationName.setText(locationName);
        binding.txtPlaceAddress.setText(locationAddress);
    }

    private void setupMap() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().
                findFragmentById(R.id.fragment_map);

        if(mapFragment != null) {
            firstOnMapIdleCall = true;

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap map) {
                    googleMap = map;

                    zoomToLocation(new LatLng(searchAddress.latitude, searchAddress.longitude),
                            false);

                    googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                        @Override
                        public void onCameraIdle() {
                            if(firstOnMapIdleCall) {
                                firstOnMapIdleCall = false;
                                return;
                            }

                            getPlaceFromLatLng(googleMap.getCameraPosition().target);
                        }
                    });
                }
            });
        }
    }

    private void zoomToLocation(LatLng latLng, boolean animate) {
        if(animate) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
        }
    }

    private void setOnClickListeners() {
        binding.txtChangeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).popBackStack();
            }
        });

        binding.btnConfirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddress();
            }
        });
    }

    private void getPlaceFromLatLng(LatLng latLng) {
        String latLngString = latLng.latitude + "," + latLng.longitude;
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

                searchAddress = geocodingResults.get(0).getPlaceAddress();
                searchAddress.latitude = locationCoordinates.getLat();
                searchAddress.longitude = locationCoordinates.getLng();

                setupLocationDetails();
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void saveAddress() {
        showProgressBar();

        searchAddress.id = UUID.randomUUID().toString();
        searchAddress.userId = auth.getCurrentUser().getUid();
        searchAddress.name = CommonVariables.enteredUserDetails.name;
        searchAddress.phone = CommonVariables.enteredUserDetails.phone;
        searchAddress.addressType = AddressType.HOME;
        searchAddress.locationCoordinateHash = CommonMethods.getCoordinateHash(
                searchAddress.latitude, searchAddress.longitude
        );

        Log.d(TAG, "saveAddress: " + searchAddress);

        db.collection(Constants.COLLECTION_ADDRESSES)
                .document(searchAddress.id)
                .set(searchAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressBar();

                        if(task.isSuccessful()) {
                            saveUserDetails();
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }

    private void saveUserDetails() {
        User user = null;
        String toastMessage = "";

        if(CommonVariables.loggedInUserDetails != null) {
            // Change Location
            user = CommonVariables.loggedInUserDetails;
            toastMessage = "Location Updated";

        } else {
            // New User
            user = CommonVariables.enteredUserDetails;
            toastMessage = "Registration Successful";
        }

        user.id = auth.getCurrentUser().getUid();
        user.primaryAddressId = searchAddress.id;

        CommonVariables.loggedInUserDetails = user;
        CommonVariables.loggedInUserAddress = searchAddress;

        String finalToastMessage = toastMessage;
        db.collection(Constants.COLLECTION_USERS)
                .document(user.id)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressBar();

                        if(task.isSuccessful()) {
                            Toast.makeText(getContext(), finalToastMessage,
                                    Toast.LENGTH_SHORT).show();

                            launchHomeActivity();
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }

    private void launchHomeActivity() {
        Intent intent = new Intent(getContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnConfirmLocation.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnConfirmLocation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
