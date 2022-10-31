package com.pentaware.foodie.ui.restaurantdetails;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pentaware.foodie.adapters.FoodItemAdapter;
import com.pentaware.foodie.api.FoodieApi;
import com.pentaware.foodie.api.RetrofitInstance;
import com.pentaware.foodie.databinding.FragmentRestaurantDetailsBinding;
import com.pentaware.foodie.models.CartItem;
import com.pentaware.foodie.models.FoodItem;
import com.pentaware.foodie.models.Restaurant;
import com.pentaware.foodie.models.maps.distance.Distance;
import com.pentaware.foodie.models.maps.distance.DistanceResponse;
import com.pentaware.foodie.models.maps.distance.Element;
import com.pentaware.foodie.ui.home.HomeFragmentDirections;
import com.pentaware.foodie.utils.CommonMethods;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;
import com.pentaware.foodie.utils.events.FoodItemClickEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;

public class RestaurantDetailsFragment extends Fragment {
    private static final String TAG = "RestaurantDetailstyy";

    private FragmentRestaurantDetailsBinding binding;

    private FirebaseFirestore db;
    private FoodieApi foodieApi;

    private FoodItemAdapter foodItemAdapter;
    private Restaurant restaurant;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRestaurantDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        foodieApi = RetrofitInstance.getRetrofitInstance(getContext()).create(FoodieApi.class);

        restaurant = RestaurantDetailsFragmentArgs.fromBundle(getArguments()).getRestaurant();

        setupRestaurantDetails();
        getRestaurantFoodItems();
        setupCheckoutDetails();

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        binding.llCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView())
                        .navigate(RestaurantDetailsFragmentDirections.actionNavRestaurantDetailsToNavCart());
            }
        });
    }

    private void setupRestaurantDetails() {
        binding.txtRestaurantName.setText(restaurant.name);

        String mainCategories = String.join(", ", restaurant.mainCategories);
        binding.txtMainCategories.setText(mainCategories);

        binding.txtPlaceName.setText(restaurant.addressLine1);

        getDistance(
                CommonMethods.getCoordinateStringFromLocation(
                        CommonVariables.loggedInUserAddress.latitude,
                        CommonVariables.loggedInUserAddress.longitude
                ),
                CommonMethods.getCoordinateStringFromLocation(
                        restaurant.latitude, restaurant.longitude)
        );
    }

    private void getDistance(String origin, String destination) {
        Log.d(TAG, "getDistance: Called");

        Call<DistanceResponse> call = foodieApi.getDistance(origin, destination);

        call.enqueue(new Callback<DistanceResponse>() {

            @Override
            public void onResponse(Call<DistanceResponse> call, retrofit2.Response<DistanceResponse> response) {
                Log.d(TAG, "Distance Call Response");

                if (!response.isSuccessful()) {
                    Log.d(TAG, "Distance Call Response FAILED: " + response.errorBody());
                    return;
                }

                if(response.body() != null) {
                    setupDeliveryDetails(response.body());
                }
            }

            @Override
            public void onFailure(Call<DistanceResponse> call, Throwable t) {
                Log.d(TAG, "Distance Api Call Failure");
                Toast.makeText(getContext() , t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupDeliveryDetails(DistanceResponse distanceResponse) {
        float deliveryCharges = CommonMethods.getDeliveryCharges(distanceResponse);

        String deliveryChargesString = deliveryCharges > 0 ?
                CommonVariables.rupeeSymbol + deliveryCharges :
                "Free Delivery";
        binding.txtDeliveryCost.setText(deliveryChargesString);

        binding.txtDeliveryTime.setText(CommonMethods.getDeliveryTimeString(distanceResponse));

        Element element = distanceResponse.rows.get(0).elements.get(0);
        Distance distance = element.distance;
        binding.txtDistance.setText(distance.text);
    }

    private void getRestaurantFoodItems() {
        db.collection(Constants.COLLECTION_FOOD_ITEMS)
                .whereEqualTo(Constants.FIELD_RESTAURANT_ID, restaurant.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty()) {
                            setupFoodItemRecyclerView(task.getResult().toObjects(FoodItem.class));
                        }
                    }
                });
    }

    private void setupFoodItemRecyclerView(List<FoodItem> foodItems) {
        foodItemAdapter = new FoodItemAdapter(foodItems, new FoodItemClickEvent() {
            @Override
            public void onAddButtonClick(FoodItem foodItem) {
                if(!otherRestaurantItemsArePresentInCart(foodItem)) {
                    addCartItem(foodItem);
                } else {
                    Toast.makeText(getContext(), "Other Restaurant Items present in cart",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPlusButtonClick(FoodItem foodItem) {
                if(!otherRestaurantItemsArePresentInCart(foodItem)) {
                    addCartItem(foodItem);
                } else {
                    Toast.makeText(getContext(), "Other Restaurant Items present in cart",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onMinusButtonClick(FoodItem foodItem) {
                removeCartItem(foodItem.id, false);
            }
        });

        binding.rvFoodItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFoodItems.setNestedScrollingEnabled(false);
        binding.rvFoodItems.setAdapter(foodItemAdapter);
    }

    private boolean otherRestaurantItemsArePresentInCart(FoodItem foodItem) {
        if(CommonVariables.userCartItems == null || CommonVariables.userCartItems.isEmpty()) {
            return false;
        }

        if(CommonVariables.loggedInUserDetails.cartRestaurantId == null ||
                CommonVariables.loggedInUserDetails.cartRestaurantId.isEmpty()) {
            return false;
        }

        return !foodItem.restaurantId.equals(CommonVariables.loggedInUserDetails.cartRestaurantId);
    }

    private void addCartItem(FoodItem foodItem) {
        CartItem cartItem = null;

        if(CommonVariables.userCartItems == null) {
            CommonVariables.userCartItems = new ArrayList<>();
        }

        if(!CommonVariables.userCartItems.isEmpty()) {
            for(CartItem item: CommonVariables.userCartItems) {
                if(item.foodItemId.equals(foodItem.id)) {
                    cartItem = item;
                }
            }
        }

        if(cartItem == null) {
            cartItem = new CartItem(UUID.randomUUID().toString(),
                    CommonVariables.loggedInUserDetails.id, foodItem.id, 1,
                    Calendar.getInstance().getTime());

            CommonVariables.loggedInUserDetails.cartRestaurantId = foodItem.restaurantId;
            CommonVariables.userCartItems.add(cartItem);

            db.collection(Constants.COLLECTION_USERS)
                    .document(CommonVariables.loggedInUserDetails.id)
                    .set(CommonVariables.loggedInUserDetails)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Cart Restaurant ID Updated");
                            }
                        }
                    });

        } else {
            cartItem.quantity += 1;
            cartItem.itemUpdatedTime = Calendar.getInstance().getTime();
        }

        db.collection(Constants.COLLECTION_CART_ITEMS)
                .document(cartItem.id)
                .set(cartItem)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Added Cart Item");
                            foodItemAdapter.notifyDataSetChanged();
                            setupCheckoutDetails();
                        }
                    }
                });
    }

    private void removeCartItem(String foodItemId, boolean removeCompleteItem) {
        CartItem cartItem = null;

        if(CommonVariables.userCartItems == null) {
            return;
        }

        if(!CommonVariables.userCartItems.isEmpty()) {
            for(CartItem item: CommonVariables.userCartItems) {
                if(item.foodItemId.equals(foodItemId)) {
                    cartItem = item;
                }
            }
        }

        if(cartItem == null) {
            return;
        }

        if(cartItem.quantity == 1) {
            removeCompleteItem = true;
        }

        if(removeCompleteItem) {
            CartItem finalCartItem = cartItem;
            db.collection(Constants.COLLECTION_CART_ITEMS)
                    .document(cartItem.id)
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Cart Item Completely Removed");
                                CommonVariables.userCartItems.remove(finalCartItem);
                                foodItemAdapter.notifyDataSetChanged();
                                setupCheckoutDetails();
                            }
                        }
                    });

            return;
        }

        cartItem.quantity -= 1;
        cartItem.itemUpdatedTime = Calendar.getInstance().getTime();

        db.collection(Constants.COLLECTION_CART_ITEMS)
                .document(cartItem.id)
                .set(cartItem)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Cart Item Removed");
                            foodItemAdapter.notifyDataSetChanged();
                            setupCheckoutDetails();
                        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.clear();
        binding = null;
    }
}
