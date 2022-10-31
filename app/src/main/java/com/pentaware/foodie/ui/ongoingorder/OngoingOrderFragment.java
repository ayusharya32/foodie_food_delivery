package com.pentaware.foodie.ui.ongoingorder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.maps.android.PolyUtil;
import com.pentaware.foodie.R;
import com.pentaware.foodie.adapters.OrderItemsAdapter;
import com.pentaware.foodie.api.FoodieApi;
import com.pentaware.foodie.api.RetrofitInstance;
import com.pentaware.foodie.baseactivity.HomeActivity;
import com.pentaware.foodie.databinding.FragmentOngoingOrderBinding;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.DriverLocation;
import com.pentaware.foodie.models.Order;
import com.pentaware.foodie.models.Restaurant;
import com.pentaware.foodie.models.maps.LocationCoordinates;
import com.pentaware.foodie.models.maps.directions.DirectionResponse;
import com.pentaware.foodie.utils.CommonMethods;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;
import com.pentaware.foodie.utils.enums.DeliveryPartnerStatus;
import com.pentaware.foodie.utils.enums.LocationType;
import com.pentaware.foodie.utils.enums.OrderStatus;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;

public class OngoingOrderFragment extends Fragment {
    private static final String TAG = "OngoingOrderyy";
    private static final float POLYLINE_WIDTH = 10f;

    private FragmentOngoingOrderBinding binding;

    private FirebaseFirestore db;
    private FoodieApi foodieApi;

    private OrderItemsAdapter orderItemsAdapter;

    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private Polyline polyline;
    private Marker startMarker, endMarker;

    private Order order;
    private Restaurant restaurant;
    private Address deliveryAddress;
    private DriverLocation driverLocation;
    private boolean restaurantLocationSetupDone, driverLocationSetupDone;

    private ListenerRegistration orderSnapshotListener, driverLocationListener;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOngoingOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        foodieApi = RetrofitInstance.getRetrofitInstance(getContext()).create(FoodieApi.class);

        addOrderSnapshotListener();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        binding.btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrderSnapshotListener();
            }
        });
    }

    private void addOrderSnapshotListener() {
        if(CommonVariables.loggedInUserDetails.ongoingOrderId == null ||
                CommonVariables.loggedInUserDetails.ongoingOrderId.isEmpty()) {
            Navigation.findNavController(getView()).popBackStack(R.id.nav_ongoing_order, true);
            Navigation.findNavController(getView()).navigate(R.id.nav_home);

            return;
        }

        if(orderSnapshotListener != null) {
            orderSnapshotListener.remove();
        }

        restaurantLocationSetupDone = false;
        driverLocationSetupDone = false;

        showProgressBar();
        orderSnapshotListener = db.collection(Constants.COLLECTION_ORDERS)
                .document(CommonVariables.loggedInUserDetails.ongoingOrderId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException error) {
                        hideProgressBar();

                        if(error != null) {
                            Log.d(TAG, error.getMessage());
                            return;
                        }

                        if(documentSnapshot != null && documentSnapshot.exists()) {
                            order = documentSnapshot.toObject(Order.class);
                            Log.d(TAG, "onEvent: Got Updated Order: " + order);
                            setupOrderDetails();

                            if(order.orderStatus == OrderStatus.DELIVERED) {
                                finishOrder();
                            }

                            if(restaurant == null) {
                                getRestaurant();
                            }

                            if(deliveryAddress == null) {
                                getDeliveryAddress();
                            }
                        }
                    }
                });
    }

    private void finishOrder() {
        CommonVariables.loggedInUserDetails.ongoingOrderId = null;

        db.collection(Constants.COLLECTION_USERS)
                .document(CommonVariables.loggedInUserDetails.id)
                .set(CommonVariables.loggedInUserDetails)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            launchHomeActivity();
                        }
                    }
                });
    }

    private void launchHomeActivity() {
        Intent intent = new Intent(getContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setupOrderDetails() {
        setupOrderItemsRecyclerView();

        String grandTotalString = CommonVariables.rupeeSymbol + (order.foodItemsCost
                + order.deliveryFee + order.tip);
        binding.txtGrandTotal.setText(grandTotalString);

        binding.txtPaymentMethod.setText(CommonMethods.getPaymentMethodString(
                order.paymentMethod
        ));

        binding.txtOrderStatus.setText(CommonMethods.getOrderStatusString(order.orderStatus,
                order.deliveryPartnerStatus));

        setupMap();
    }

    private void setupMap() {
        if(googleMap != null) {
            setRequiredDetailsOnMap();
            return;
        }

        mapFragment = (SupportMapFragment) getChildFragmentManager().
                findFragmentById(R.id.fragment_map);

        if(mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap map) {
                    googleMap = map;

                    setRequiredDetailsOnMap();
                }
            });
        }
    }

    private void setRequiredDetailsOnMap() {
        LocationType locationType = CommonMethods.getLocationType(order.orderStatus,
                order.deliveryPartnerStatus);

        switch(locationType) {
            case RESTAURANT:
                Log.d(TAG, "setRequiredDetailsOnMap: Restaurant");
                if(!restaurantLocationSetupDone && restaurant != null) {
                    addMarkerToLocation(new LatLng(restaurant.latitude, restaurant.longitude));
                }
                break;

            case DELIVERY_PARTNER:
                Log.d(TAG, "setRequiredDetailsOnMap: Driver");
                if(!driverLocationSetupDone) {
                    addDriverLocationSnapshotListener();
                }
                break;
        }
    }

    private void addDriverLocationSnapshotListener() {
        if(order.deliveryPartnerId == null || deliveryAddress == null) {
            return;
        }

        driverLocationListener = db.collection(Constants.COLLECTION_DRIVER_LOCATIONS)
                .document(order.deliveryPartnerId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException error) {
                        if(error != null) {
                            Log.d(TAG, error.getMessage());
                            return;
                        }

                        if(documentSnapshot != null && documentSnapshot.exists()) {
                            driverLocation = documentSnapshot.toObject(DriverLocation.class);
                            Log.d(TAG, "onEvent: Got Driver Location: " + driverLocation);

                            if(driverLocation != null) {
                                if(order.deliveryPartnerStatus ==
                                        DeliveryPartnerStatus.WAY_TO_RESTAURANT && restaurant != null) {
                                    getDirection(
                                            CommonMethods.getCoordinateStringFromLocation(
                                                    driverLocation.locationCoordinates),
                                            CommonMethods.getCoordinateStringFromLocation(
                                                    restaurant.latitude, restaurant.longitude)
                                    );

                                } else {
                                    getDirection(
                                            CommonMethods.getCoordinateStringFromLocation(
                                                    driverLocation.locationCoordinates),

                                            CommonMethods.getCoordinateStringFromLocation(
                                                    deliveryAddress.latitude, deliveryAddress.longitude)
                                    );
                                }
                            }
                        }
                    }
                });
    }

    private void getDirection(String origin, String destination) {
        Log.d(TAG, "getDirection: Called");
        Log.d(TAG, "getDirection: origin: " + origin);
        Log.d(TAG, "getDirection: destination: " + destination);

        Call<DirectionResponse> call = foodieApi.getDirection(origin, destination);

        call.enqueue(new Callback<DirectionResponse>() {
            @Override
            public void onResponse(Call<DirectionResponse> call, retrofit2.Response<DirectionResponse> response) {
                Log.d(TAG, "Direction Response");

                if(!response.isSuccessful()) {
                    Log.d(TAG, "Direction Response Failed");
                    return;
                }

                if(response.body() != null) {
                    showDirectionInfo(response.body());
                }
            }

            @Override
            public void onFailure(Call<DirectionResponse> call, Throwable t) {
                Log.d(TAG, "Direction Response Failed " + t.getMessage());
            }
        });
    }

    private void showDirectionInfo(DirectionResponse directionResponse) {
        if(directionResponse.routes.isEmpty()) {
            return;
        }

        driverLocationSetupDone = true;
        String routePolyline = directionResponse.routes.get(0).routePolyline.points;
        createPolyline(PolyUtil.decode(routePolyline));
    }

    private void createPolyline(List<LatLng> points) {
        Log.d(TAG, points.toString());

        if(googleMap == null) {
            return;
        }

        if(polyline != null) {
            polyline.remove();
        }

        polyline = googleMap.addPolyline(new PolylineOptions().addAll(points));

        polyline.setWidth(POLYLINE_WIDTH);
        polyline.setColor(ContextCompat.getColor(getContext(), R.color.grey_600));

        zoomToViewWholeRoute(points);
    }

    private void zoomToViewWholeRoute(List<LatLng> points) {
        if(googleMap == null) {
            return;
        }

        LatLng startPositionLatLng = points.get(0);
        LatLng endPositionLatLng = points.get(points.size() - 1);

        LatLngBounds.Builder latLngBoundBuilder = new LatLngBounds.Builder();
        latLngBoundBuilder.include(startPositionLatLng);
        latLngBoundBuilder.include(endPositionLatLng);

        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBoundBuilder.build(), 175));

        addMarkersToStartAndEndPoint(startPositionLatLng, endPositionLatLng);
    }

    private void addMarkersToStartAndEndPoint(LatLng start, LatLng end) {
        if(googleMap == null) {
            return;
        }

        if(startMarker != null) {
            startMarker.remove();
        }

        if(endMarker != null) {
            endMarker.remove();
        }

        startMarker = googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(
                        CommonMethods.getResizedBitmap(getContext(), R.drawable.ic_delivery_marker,
                                150, 150)
                ))
                .position(start)
        );

        if(order.deliveryPartnerStatus == DeliveryPartnerStatus.WAY_TO_RESTAURANT) {
            endMarker = googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            CommonMethods.getResizedBitmap(getContext(), R.drawable.ic_map_marker_with_food,
                                    150, 150)
                    ))
                    .position(end));
        } else {
            endMarker = googleMap.addMarker(new MarkerOptions()
                    .position(end));
        }
    }

    private void addMarkerToLocation(LatLng position) {
        if(googleMap == null) {
            return;
        }

        if(startMarker != null) {
            startMarker.remove();
        }

        if(endMarker != null) {
            endMarker.remove();
        }

        if(polyline != null) {
            polyline.remove();
        }

        int height = 150;
        int width = 150;
        BitmapDrawable bitmapDrawable = (BitmapDrawable) AppCompatResources.getDrawable(
                getContext(), R.drawable.ic_map_marker_with_food);

        Bitmap bmp = bitmapDrawable.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(bmp, width, height, false);

        startMarker = googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .position(position)
        );

        zoomToLocation(position, false);

        Log.d(TAG, "addMarkerToLocation: restaurantLocationSetupDone");
        restaurantLocationSetupDone = true;
    }

    private void zoomToLocation(LatLng latLng, boolean animate) {
        if(googleMap == null) {
            return;
        }

        float zoom = 120;

        if(animate) {
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        } else {
            googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    private void setupOrderItemsRecyclerView() {
        orderItemsAdapter = new OrderItemsAdapter(order.orderItems);

        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setNestedScrollingEnabled(false);
        binding.rvOrderItems.setAdapter(orderItemsAdapter);
    }

    private void getRestaurant() {
        if(order == null) {
            return;
        }

        showProgressBar();
        db.collection(Constants.COLLECTION_RESTAURANTS)
                .document(order.restaurantId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        hideProgressBar();
                        if(task.isSuccessful() && task.getResult().exists()) {
                            restaurant = task.getResult().toObject(Restaurant.class);
                            Log.d(TAG, "onEvent: Got Restaurant: " + restaurant);

                            setupRestaurantDetails();
                            setRequiredDetailsOnMap();
                        }
                    }
                });
    }

    private void setupRestaurantDetails() {
        if(restaurant == null){
            return;
        }

        binding.txtRestaurantName.setText(restaurant.name);

        String restaurantAddress = (restaurant.addressLine1 != null ?
                (restaurant.addressLine1 + "\n") : "") +
                restaurant.addressLine2 + "\n" + restaurant.city + ", " +
                restaurant.state + ", " + restaurant.pinCode;

        binding.txtRestaurantAddress.setText(restaurantAddress);
    }

    private void getDeliveryAddress() {
        if(order == null) {
            return;
        }

        showProgressBar();
        db.collection(Constants.COLLECTION_ADDRESSES)
                .document(order.deliveryAddressId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        hideProgressBar();
                        if(task.isSuccessful() && task.getResult().exists()) {
                            deliveryAddress = task.getResult().toObject(Address.class);
                            Log.d(TAG, "onEvent: Got Delivery Address: " + deliveryAddress);

                            setupDeliveryAddressDetails();
                            setRequiredDetailsOnMap();
                        }
                    }
                });
    }

    private void setupDeliveryAddressDetails() {
        if(deliveryAddress == null) {
            return;
        }

        binding.txtDeliveryAddress.setText(deliveryAddress.getAddressString());
    }

    private void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.scrollViewContent.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
        binding.scrollViewContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(orderSnapshotListener != null) {
            orderSnapshotListener.remove();
        }

        if(driverLocationListener != null) {
            driverLocationListener.remove();
        }

        compositeDisposable.clear();
        binding = null;
    }
}
