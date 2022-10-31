package com.pentaware.foodie.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.pentaware.foodie.R;
import com.pentaware.foodie.adapters.RestaurantAdapter;
import com.pentaware.foodie.adapters.SubCategoryAdapter;
import com.pentaware.foodie.auth.DeliveryLocationActivity;
import com.pentaware.foodie.databinding.FragmentHomeBinding;
import com.pentaware.foodie.models.CartItem;
import com.pentaware.foodie.models.FoodItem;
import com.pentaware.foodie.models.FoodSubCategory;
import com.pentaware.foodie.models.Order;
import com.pentaware.foodie.models.OrderItem;
import com.pentaware.foodie.models.Restaurant;
import com.pentaware.foodie.models.maps.LocationCoordinates;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;
import com.pentaware.foodie.utils.enums.DeliveryPartnerStatus;
import com.pentaware.foodie.utils.enums.OrderStatus;
import com.pentaware.foodie.utils.enums.PaymentMethod;
import com.pentaware.foodie.utils.events.RestaurantItemClickEvent;
import com.pentaware.foodie.utils.events.SubCategoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragmentyy";
    private static final String LOADING_POPULAR_CATEGORIES_SECTION = "LOADING_POPULAR_CATEGORIES_SECTION";
    private static final String LOADING_RECENTLY_VIEWED_SECTION = "LOADING_RECENTLY_VIEWED_SECTION";
    private static final String LOADING_EXPLORE_SECTION = "LOADING_EXPLORE_SECTION";

    private FragmentHomeBinding binding;

    private FirebaseFirestore db;

    private SubCategoryAdapter subCategoryAdapter;
    private RestaurantAdapter restaurantAdapter;

    private List<String> loadingProcesses;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        loadingProcesses = new ArrayList<>();

        setupSubCategoryRecyclerView();
        setupRestaurantsRecyclerView();
        setupLocationDetails();

        getSubCategories();
        getNearbyRestaurants();

        setOnClickListeners();
        setupCheckoutDetails();

        binding.imgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView())
                        .navigate(HomeFragmentDirections.actionNavHomeToNavSettings());
            }
        });

//        uploadTestOrder();
    }

    private void setOnClickListeners() {
        binding.llLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), DeliveryLocationActivity.class));
            }
        });

        binding.imgLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), DeliveryLocationActivity.class));
            }
        });

        binding.llCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView())
                        .navigate(HomeFragmentDirections.actionNavHomeToNavCart());
            }
        });

        binding.llOngoingOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView())
                        .navigate(HomeFragmentDirections.actionNavHomeToNavOngoingOrder());
            }
        });
    }

    private void uploadTestOrder() {
        Order order = new Order();

        order.id = UUID.randomUUID().toString();
        order.restaurantId = "13ae02fe-fff5-45cd-b6b7-7b05c21a4768";
        order.deliveryPartnerId = "1iV6WP02xNfGdjdmEXTwZAqwvWe2";
        order.customerId = CommonVariables.loggedInUserDetails.id;
        order.deliveryAddressId = CommonVariables.loggedInUserAddress.id;

        order.orderItems = Arrays.asList(
            new OrderItem("68f0ee1c-a193-43bd-bb67-78ea65b96f8a", 2),
            new OrderItem("78df09ee-311f-4047-9112-ddfdcea2a49c", 1)
        );

        order.foodItemsCost = 617;
        order.deliveryFee = 20;
        order.tip = 10;

        order.deliveryPartnerStatus = DeliveryPartnerStatus.WAITING_FOR_CONFIRMATION;
        order.orderStatus = OrderStatus.WAITING_FOR_CONFIRMATION;

        order.orderTime = Calendar.getInstance().getTime();
        order.deliveryLocationCoordinateHash = CommonVariables.loggedInUserAddress.locationCoordinateHash;
        order.deliveryLocationCoordinates = new LocationCoordinates(
                CommonVariables.loggedInUserAddress.latitude,
                CommonVariables.loggedInUserAddress.longitude
        );

        order.paymentMethod = PaymentMethod.CASH_ON_DELIVERY;

        db.collection(Constants.COLLECTION_ORDERS)
                .document(order.id)
                .set(order);
    }

    private void setupSubCategoryRecyclerView() {
        subCategoryAdapter = new SubCategoryAdapter(Collections.emptyList(), new SubCategoryClickEvent() {
            @Override
            public void onItemClick(FoodSubCategory subCategory) {

            }
        });

        binding.rvPopularCategories.setLayoutManager(new GridLayoutManager(getContext(),
                2, LinearLayoutManager.HORIZONTAL, false));
        binding.rvPopularCategories.setAdapter(subCategoryAdapter);
    }

    private void setupRestaurantsRecyclerView() {
        restaurantAdapter = new RestaurantAdapter(Collections.emptyList(), new RestaurantItemClickEvent() {
            @Override
            public void onItemClick(Restaurant restaurant) {
                Navigation.findNavController(getView())
                        .navigate(HomeFragmentDirections
                                .actionNavHomeToNavRestaurantDetails(restaurant));
            }
        });

        binding.rvExploreRetaurants.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvExploreRetaurants.setAdapter(restaurantAdapter);
        binding.rvExploreRetaurants.setNestedScrollingEnabled(false);
    }

    private void setupLocationDetails() {
        String locationName;
        String locationAddress;

        if(CommonVariables.loggedInUserAddress.addressLine2.contains(",")) {
            locationName = CommonVariables.loggedInUserAddress.addressLine2.split(",")[0].trim();

            String leftAddress = CommonVariables.loggedInUserAddress.addressLine2.split(",")[1].trim();
            locationAddress = leftAddress + "\n" + CommonVariables.loggedInUserAddress.city + ", " + CommonVariables.loggedInUserAddress.state +
                    ", " + CommonVariables.loggedInUserAddress.pinCode;

        } else {
            locationName = CommonVariables.loggedInUserAddress.addressLine2;
            locationAddress = CommonVariables.loggedInUserAddress.city + ", " + CommonVariables.loggedInUserAddress.state + "\n"
                    + CommonVariables.loggedInUserAddress.pinCode;
        }

        binding.txtPlaceName.setText(locationName);
        binding.txtPlaceAddress.setText(locationAddress);
    }

    private void getSubCategories() {
        showProgressBar();
        loadingProcesses.add(LOADING_POPULAR_CATEGORIES_SECTION);

        db.collection(Constants.COLLECTION_SUB_CATEGORIES)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            subCategoryAdapter.subCategoryList =
                                    task.getResult().toObjects(FoodSubCategory.class);

                            subCategoryAdapter.notifyDataSetChanged();
                            loadingProcesses.remove(LOADING_POPULAR_CATEGORIES_SECTION);
                            checkLoadingAndShowDashboard();
                        }
                    }
                });
    }

    private void getNearbyRestaurants() {
        showProgressBar();
        loadingProcesses.add(LOADING_EXPLORE_SECTION);

        GeoLocation center = new GeoLocation(
                CommonVariables.loggedInUserAddress.latitude,
                CommonVariables.loggedInUserAddress.longitude);

        double radiusInMetre = CommonVariables.appInfo.searchRadiusInKm * 1000;

        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInMetre);
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for(GeoQueryBounds bound: bounds) {
            Query q = db.collection(Constants.COLLECTION_RESTAURANTS)
                    .orderBy(Constants.FIELD_LOCATION_COORDINATE_HASH)
                    .startAt(bound.startHash)
                    .endAt(bound.endHash);

            tasks.add(q.get());
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        Log.d(TAG, "Inside onComplete tasks all complete");

                        List<Restaurant> nearbyRestaurants = new ArrayList<>();

                        for(Task<QuerySnapshot> task: tasks) {
                            QuerySnapshot snap = task.getResult();

                            Log.d(TAG, "Query Result " + snap.getDocuments().size());

                            for(DocumentSnapshot doc: snap.getDocuments()) {
                                Restaurant restaurant = doc.toObject(Restaurant.class);

                                Log.d(TAG, "Query Restaurant: " + restaurant);
                                Log.d(TAG, "Query Restaurant Lat: " + restaurant.latitude);
                                Log.d(TAG, "Query Restaurant Lng: " + restaurant.longitude);

                                if(restaurant != null) {
                                    GeoLocation docLocation = new GeoLocation(
                                            restaurant.latitude,
                                            restaurant.longitude
                                    );

                                    double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);

                                    Log.d(TAG, "Distance: " + distanceInM);

                                    if (distanceInM <= radiusInMetre) {
                                        nearbyRestaurants.add(restaurant);
                                    }
                                }
                            }
                        }

                        restaurantAdapter.restaurantList = nearbyRestaurants;
                        restaurantAdapter.notifyDataSetChanged();

                        loadingProcesses.remove(LOADING_EXPLORE_SECTION);
                        checkLoadingAndShowDashboard();
                    }
                });
    }

    private void setupCheckoutDetails() {
        if(CommonVariables.userCartItems == null || CommonVariables.userCartItems.isEmpty()) {
            binding.llCheckout.setVisibility(View.GONE);
            return;
        }

        Single.fromCallable(new Callable<List<CartItem>>() {
                    @Override
                    public List<CartItem> call() throws Exception {
                        for(CartItem cartItem: CommonVariables.userCartItems) {
                            DocumentSnapshot docSnap = Tasks.await(getFoodItemById(cartItem.foodItemId));

                            if(docSnap.exists()) {
                                cartItem.foodItem = docSnap.toObject(FoodItem.class);
                            }
                        }

                        return CommonVariables.userCartItems;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<CartItem>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<CartItem> cartItems) {
                        float totalPrice = 0;
                        int totalItems = 0;

                        for(CartItem cartItem: CommonVariables.userCartItems) {
                            totalItems += cartItem.quantity;
                            totalPrice += cartItem.foodItem.price * cartItem.quantity;
                        }

                        binding.llCheckout.setVisibility(View.VISIBLE);
                        binding.txtTotalItems.setText(totalItems + " Items");
                        binding.txtTotalPrice.setText(CommonVariables.rupeeSymbol + totalPrice);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.d(TAG, "onError: " + e);
                    }
                });
    }

    private Task<DocumentSnapshot> getFoodItemById(String foodItemId) {
        return db.collection(Constants.COLLECTION_FOOD_ITEMS)
                .document(foodItemId)
                .get();
    }

    private void checkLoadingAndShowDashboard() {
        Log.d(TAG, "checkLoadingAndShowDashboard: " + loadingProcesses);
        if(loadingProcesses.isEmpty()) {
            hideProgressBar();
        }
    }

    private void showProgressBar() {
        if(binding == null) {
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.llAppBar.setVisibility(View.GONE);
        binding.llContent.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        if(binding == null) {
            return;
        }

        binding.progressBar.setVisibility(View.GONE);
        binding.llAppBar.setVisibility(View.VISIBLE);
        binding.llContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.clear();
        binding = null;
    }
}
