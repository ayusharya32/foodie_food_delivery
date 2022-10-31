package com.pentaware.foodie.ui.cart;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pentaware.foodie.adapters.CartItemAdapter;
import com.pentaware.foodie.api.FoodieApi;
import com.pentaware.foodie.api.RetrofitInstance;
import com.pentaware.foodie.databinding.FragmentCartBinding;
import com.pentaware.foodie.models.CartItem;
import com.pentaware.foodie.models.FoodItem;
import com.pentaware.foodie.models.Order;
import com.pentaware.foodie.models.OrderItem;
import com.pentaware.foodie.models.Restaurant;
import com.pentaware.foodie.models.maps.LocationCoordinates;
import com.pentaware.foodie.models.maps.distance.Distance;
import com.pentaware.foodie.models.maps.distance.DistanceResponse;
import com.pentaware.foodie.models.maps.distance.Element;
import com.pentaware.foodie.utils.CommonMethods;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;
import com.pentaware.foodie.utils.enums.DeliveryPartnerStatus;
import com.pentaware.foodie.utils.enums.OrderStatus;
import com.pentaware.foodie.utils.events.CartItemClickEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;

public class CartFragment extends Fragment {
    private static final String TAG = "CartFragmentyy";

    private FragmentCartBinding binding;

    private FirebaseFirestore db;
    private FoodieApi foodieApi;

    private CartItemAdapter cartItemAdapter;

    private Restaurant restaurant;
    private float deliveryCharges;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        foodieApi = RetrofitInstance.getRetrofitInstance(getContext()).create(FoodieApi.class);

        deliveryCharges = 0;

        setupCartItems();

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).popBackStack();
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonClick();
            }
        });
    }

    private void setupCartItems() {
        if(CommonVariables.userCartItems == null || CommonVariables.userCartItems.isEmpty()) {
            showCartEmpty();
            return;
        }

        showProgressBar();
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
                        hideProgressBar(cartItems.isEmpty());

                        setupCartItemRecyclerView(cartItems);
                        updateCartTotal();

                        if(restaurant == null) {
                            getRestaurantDetails();
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.d(TAG, "onError: " + e);
                    }
                });
    }

    private void getRestaurantDetails() {
        if(CommonVariables.loggedInUserDetails.cartRestaurantId == null
                || CommonVariables.loggedInUserDetails.cartRestaurantId.isEmpty()) {
            return;
        }

        showProgressBar();
        db.collection(Constants.COLLECTION_RESTAURANTS)
                .document(CommonVariables.loggedInUserDetails.cartRestaurantId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        hideProgressBar(false);

                        if(task.isSuccessful() && task.getResult().exists()) {
                            restaurant = task.getResult().toObject(Restaurant.class);

                            getDistance(
                                    CommonMethods.getCoordinateStringFromLocation(
                                            CommonVariables.loggedInUserAddress.latitude,
                                            CommonVariables.loggedInUserAddress.longitude
                                    ),
                                    CommonMethods.getCoordinateStringFromLocation(
                                            restaurant.latitude, restaurant.longitude)
                            );
                        }
                    }
                });
    }

    private void setupCartItemRecyclerView(List<CartItem> cartItems) {
        cartItemAdapter = new CartItemAdapter(cartItems, new CartItemClickEvent() {
            @Override
            public void onPlusButtonClick(CartItem cartItem) {
                addCartItem(cartItem.foodItem);
            }

            @Override
            public void onMinusButtonClick(CartItem cartItem) {
                removeCartItem(cartItem.foodItem.id, false);
            }
        });

        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCartItems.setNestedScrollingEnabled(false);
        binding.rvCartItems.setAdapter(cartItemAdapter);
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
                            cartItemAdapter.notifyDataSetChanged();
                            updateCartTotal();
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

                                if(!CommonVariables.userCartItems.isEmpty()) {
                                    cartItemAdapter.notifyDataSetChanged();
                                    updateCartTotal();
                                } else {
                                    showCartEmpty();
                                }
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
                            cartItemAdapter.notifyDataSetChanged();
                            updateCartTotal();
                        }
                    }
                });
    }

    private Task<DocumentSnapshot> getFoodItemById(String foodItemId) {
        return db.collection(Constants.COLLECTION_FOOD_ITEMS)
                .document(foodItemId)
                .get();
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
                    setupRestaurantDetails(response.body());
                }
            }

            @Override
            public void onFailure(Call<DistanceResponse> call, Throwable t) {
                Log.d(TAG, "Distance Api Call Failure");
                Toast.makeText(getContext() , t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRestaurantDetails(DistanceResponse distanceResponse) {
        deliveryCharges = CommonMethods.getDeliveryCharges(distanceResponse);

        Element element = distanceResponse.rows.get(0).elements.get(0);
        Distance distance = element.distance;

        String deliveryString = deliveryCharges > 0 ?
                CommonVariables.rupeeSymbol + deliveryCharges + " (" + distance.text + ")" :
                "Free Delivery";
        binding.txtDeliveryCharges.setText(deliveryString);

        String estimatedDeliveryTime = "Estimated Delivery Time: " +
                CommonMethods.getDeliveryTimeString(distanceResponse);
        binding.txtEstimatedDeliveryTime.setText(estimatedDeliveryTime);

        updateCartTotal();
    }

    private void updateCartTotal() {
        float totalPrice = 0;
        int totalItems = 0;

        for(CartItem cartItem: CommonVariables.userCartItems) {
            totalItems += cartItem.quantity;
            totalPrice += cartItem.foodItem.price * cartItem.quantity;
        }

        binding.txtItemTotal.setText(CommonVariables.rupeeSymbol + totalPrice);

        float grandTotal = totalPrice + deliveryCharges;
        binding.txtGrandTotal.setText(CommonVariables.rupeeSymbol + grandTotal);
    }

    private void onNextButtonClick() {
        Navigation.findNavController(getView())
                .navigate(CartFragmentDirections.actionNavCartToNavOrderBill(getOrder()));
    }

    private Order getOrder() {
        Order order = new Order();

        order.id = UUID.randomUUID().toString();
        order.restaurantId = restaurant.id;
        order.customerId = CommonVariables.loggedInUserDetails.id;

        order.orderItems = getOrderItems();
        order.foodItemsCost = getCartFoodItemsTotalPrice();
        order.deliveryFee = deliveryCharges;
        order.tip = 0; // TODO: Take user input for tip

        order.deliveryPartnerStatus = DeliveryPartnerStatus.WAITING_FOR_CONFIRMATION;
        order.orderStatus = OrderStatus.WAITING_FOR_CONFIRMATION;

        order.orderTime = Calendar.getInstance().getTime();

        order.deliveryAddressId = CommonVariables.loggedInUserAddress.id;
        order.deliveryLocationCoordinateHash = CommonVariables.loggedInUserAddress.locationCoordinateHash;
        order.deliveryLocationCoordinates = new LocationCoordinates(
                CommonVariables.loggedInUserAddress.latitude,
                CommonVariables.loggedInUserAddress.longitude
        );

        order.restaurant = restaurant;

        return order;
    }

    private List<OrderItem> getOrderItems() {
        List<OrderItem> orderItems = new ArrayList<>();

        for(CartItem cartItem: CommonVariables.userCartItems) {
            OrderItem orderItem = new OrderItem();

            orderItem.foodItemId = cartItem.foodItemId;
            orderItem.quantity = cartItem.quantity;
            orderItem.name = cartItem.foodItem.name;
            orderItem.price = cartItem.foodItem.price;

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private float getCartFoodItemsTotalPrice() {
        float totalPrice = 0;

        for(CartItem cartItem: CommonVariables.userCartItems) {
            totalPrice += cartItem.foodItem.price * cartItem.quantity;
        }

        return totalPrice;
    }

    private void showProgressBar() {
        binding.cartProgressBar.setVisibility(View.VISIBLE);

        binding.scrollViewContent.setVisibility(View.GONE);
        binding.llCartEmpty.setVisibility(View.GONE);
        binding.flButtons.setVisibility(View.GONE);
    }

    private void hideProgressBar(boolean emptyCart) {
        binding.cartProgressBar.setVisibility(View.GONE);

        if(emptyCart) {
            binding.llCartEmpty.setVisibility(View.VISIBLE);
            binding.flButtons.setVisibility(View.GONE);
        } else {
            binding.scrollViewContent.setVisibility(View.VISIBLE);
            binding.flButtons.setVisibility(View.VISIBLE);
        }
    }

    private void showCartEmpty() {
        binding.cartProgressBar.setVisibility(View.GONE);
        binding.scrollViewContent.setVisibility(View.GONE);
        binding.llCartEmpty.setVisibility(View.VISIBLE);
        binding.flButtons.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
