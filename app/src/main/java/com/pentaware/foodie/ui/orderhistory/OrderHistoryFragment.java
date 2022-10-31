package com.pentaware.foodie.ui.orderhistory;

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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pentaware.foodie.databinding.FragmentOrderHistoryBinding;
import com.pentaware.foodie.adapters.OrdersAdapter;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.Order;
import com.pentaware.foodie.models.Restaurant;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;
import com.pentaware.foodie.utils.enums.OrderStatus;
import com.pentaware.foodie.utils.events.OrderClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class OrderHistoryFragment extends Fragment {
    private static final String TAG = "OrderHistoryFragy";

    private FragmentOrderHistoryBinding binding;

    private FirebaseFirestore db;

    private OrdersAdapter ordersAdapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        getOrderHistory();

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).popBackStack();
            }
        });
    }

    private void getOrderHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);

        Single.fromCallable(new Callable<List<Order>>() {
            @Override
            public List<Order> call() throws Exception {
                List<Order> orderList = new ArrayList<>();
                QuerySnapshot orderListSnapshot = Tasks.await(getUserOrders());

                Log.d(TAG, "call: " + orderListSnapshot);
                Log.d(TAG, "call: " + orderListSnapshot.toObjects(Order.class));

                if(!orderListSnapshot.isEmpty()) {
                    Log.d(TAG, "call: " + orderListSnapshot.toObjects(Order.class));

                    for(Order order: orderListSnapshot.toObjects(Order.class)) {
                        DocumentSnapshot restaurantDoc = Tasks.await(getRestaurantById(order.restaurantId));
                        DocumentSnapshot deliveryAddressDoc = Tasks.await(getAddressById(order.deliveryAddressId));

                        if(restaurantDoc.exists() && deliveryAddressDoc.exists()) {
                            order.restaurant = restaurantDoc.toObject(Restaurant.class);
                            order.deliveryAddress = deliveryAddressDoc.toObject(Address.class);

                            orderList.add(order);
                        }
                    }
                }

                return orderList;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Order>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        compositeDisposable.add(d);
                        binding.progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Order> orderList) {
                        Log.d(TAG, "onSuccess: " + orderList);

                        binding.progressBar.setVisibility(View.GONE);
                        setupOrdersRecyclerView(orderList);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        binding.progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "onError: " + e);
                        Toast.makeText(getContext(),
                                "Some error occurred in loading orders",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Task<QuerySnapshot> getUserOrders() {
        return db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo(Constants.FIELD_CUSTOMER_ID, CommonVariables.loggedInUserDetails.id)
                .get();
    }

    private void setupOrdersRecyclerView(List<Order> orderList) {
        ordersAdapter = new OrdersAdapter(orderList, new OrderClickEvent() {
            @Override
            public void onItemClick(Order order) {

            }
        });

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrders.setAdapter(ordersAdapter);
    }

    private Task<DocumentSnapshot> getRestaurantById(String restaurantId) {
        Log.d(TAG, "getRestaurantById: Called: " + restaurantId);

        return db.collection(Constants.COLLECTION_RESTAURANTS)
                .document(restaurantId)
                .get();
    }

    private Task<DocumentSnapshot> getAddressById(String addressId) {
        Log.d(TAG, "getAddressById: Called: " + addressId);

        return db.collection(Constants.COLLECTION_ADDRESSES)
                .document(addressId)
                .get();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.clear();
        binding = null;
    }
}
