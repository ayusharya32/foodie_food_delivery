package com.pentaware.foodie.ui.orderbill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pentaware.foodie.R;
import com.pentaware.foodie.adapters.OrderItemsAdapter;
import com.pentaware.foodie.baseactivity.HomeActivity;
import com.pentaware.foodie.baseactivity.SplashActivity;
import com.pentaware.foodie.databinding.DialogAddAddressBinding;
import com.pentaware.foodie.databinding.DialogConfirmOrderBinding;
import com.pentaware.foodie.databinding.FragmentOrderBillBinding;
import com.pentaware.foodie.models.CartItem;
import com.pentaware.foodie.models.Order;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;
import com.pentaware.foodie.utils.enums.PaymentMethod;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class OrderBillFragment extends Fragment {
    private static final String TAG = "OrderBillagmentyy";

    private FragmentOrderBillBinding binding;

    private FirebaseFirestore db;

    private OrderItemsAdapter orderItemsAdapter;

    private Order order;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderBillBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        order = OrderBillFragmentArgs.fromBundle(getArguments()).getOrder();

        setupOrderDetails();

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).popBackStack();
            }
        });

        binding.btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProceedButtonClick();
            }
        });
    }

    private void onProceedButtonClick() {
        if(CommonVariables.loggedInUserAddress.addressLine1 == null
                || CommonVariables.loggedInUserAddress.addressLine1.isEmpty()) {
            showAddAddressDialog();
            return;
        }

        proceedToOrderConfirmation();
    }

    private void proceedToOrderConfirmation() {
        if(getSelectedPaymentMethod() == null) {
            Toast.makeText(getContext(), "Choose payment method to proceed",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        order.paymentMethod = getSelectedPaymentMethod();
        showConfirmOrderDialog();
    }

    private PaymentMethod getSelectedPaymentMethod() {
        switch(binding.rgPaymentMethod.getCheckedRadioButtonId()) {
            case R.id.rb_payment_cod:
                return PaymentMethod.CASH_ON_DELIVERY;

            case R.id.rb_payment_online:
                return PaymentMethod.ONLINE_PAYMENT;

            default:
                return null;
        }
    }

    private void setupOrderDetails() {
        setupOrderItemsRecyclerView();

        binding.txtDeliveryAddress.setText(CommonVariables.loggedInUserAddress.getAddressString());

        binding.txtRestaurantName.setText(order.restaurant.name);

        String restaurantAddress = (order.restaurant.addressLine1 != null ?
                (order.restaurant.addressLine1 + "\n") : "") +
                order.restaurant.addressLine2 + "\n" + order.restaurant.city + ", " +
                order.restaurant.state + ", " + order.restaurant.pinCode;

        binding.txtRestaurantAddress.setText(restaurantAddress);

        String grandTotalString = CommonVariables.rupeeSymbol + (order.foodItemsCost
                + order.deliveryFee + order.tip);
        binding.txtGrandTotal.setText(grandTotalString);

        if(CommonVariables.loggedInUserAddress.addressLine1 == null
                || CommonVariables.loggedInUserAddress.addressLine1.isEmpty()) {
            binding.btnProceed.setText("Complete Address");
            binding.txtAddressIncomplete.setVisibility(View.VISIBLE);

        } else {
            binding.btnProceed.setText("Proceed");
            binding.txtAddressIncomplete.setVisibility(View.GONE);
        }
    }

    private void setupOrderItemsRecyclerView() {
        orderItemsAdapter = new OrderItemsAdapter(order.orderItems);

        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setNestedScrollingEnabled(false);
        binding.rvOrderItems.setAdapter(orderItemsAdapter);
    }

    private void confirmOrder() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Throwable {
                // Upload Order
                Tasks.await(uploadOrder());

                // Clear Cart
                for(CartItem cartItem: CommonVariables.userCartItems) {
                    Tasks.await(deleteCartItem(cartItem));
                }
                CommonVariables.userCartItems = new ArrayList<>();

                // Update User
                CommonVariables.loggedInUserDetails.ongoingOrderId = order.id;
                CommonVariables.loggedInUserDetails.cartRestaurantId = null;

                Tasks.await(db.collection(Constants.COLLECTION_USERS)
                        .document(CommonVariables.loggedInUserDetails.id)
                        .set(CommonVariables.loggedInUserDetails));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: Order Successfully Placed");
                        launchHomeActivity();
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.d(TAG, "onError: " + e);
                    }
                });
    }

    private void proceedForPayment() {
        Toast.makeText(getContext(), "Online payment unavailable at the moment",
                Toast.LENGTH_SHORT).show();
    }

    private Task<Void> uploadOrder() {
        return db.collection(Constants.COLLECTION_ORDERS)
                .document(order.id)
                .set(order);
    }

    private Task<Void> deleteCartItem(CartItem cartItem) {
        return db.collection(Constants.COLLECTION_CART_ITEMS)
                .document(cartItem.id)
                .delete();
    }

    private void launchHomeActivity() {
        Intent intent = new Intent(getContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.clear();
        binding = null;
    }

    /************************************ DIALOGS *********************************************/
    private void showConfirmOrderDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        DialogConfirmOrderBinding dialogBinding =
                DialogConfirmOrderBinding.inflate(getLayoutInflater(),
                        binding.getRoot(), false);
        dialogBuilder.setView(dialogBinding.getRoot());

        AlertDialog confirmOrderDialog = dialogBuilder.create();
        confirmOrderDialog.setCancelable(false);

        if(order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            dialogBinding.txtTitle.setText("Cash on Delivery");
            dialogBinding.txtInfo.setText(R.string.confirm_cod_order);
            dialogBinding.btnContinue.setText("Confirm");
        }

        dialogBinding.btnNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmOrderDialog.dismiss();
            }
        });

        dialogBinding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmOrderDialog.dismiss();

                if(order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
                    confirmOrder();
                    return;
                }

                proceedForPayment();
            }
        });

        confirmOrderDialog.show();
    }

    private void showAddAddressDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        DialogAddAddressBinding dialogBinding =
                DialogAddAddressBinding.inflate(getLayoutInflater(),
                        binding.getRoot(), false);
        dialogBuilder.setView(dialogBinding.getRoot());

        AlertDialog addAddressDialog = dialogBuilder.create();

        dialogBinding.btnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBinding.progressBar.setVisibility(View.VISIBLE);
                dialogBinding.btnAddAddress.setVisibility(View.GONE);

                CommonVariables.loggedInUserAddress.addressLine1 =
                        dialogBinding.etAddressLine1.getText().toString().trim();

                updateAddress(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialogBinding.progressBar.setVisibility(View.GONE);
                        dialogBinding.btnAddAddress.setVisibility(View.VISIBLE);

                        if(task.isSuccessful()) {
                            addAddressDialog.dismiss();
                            binding.btnProceed.setText("Proceed");
                            binding.txtAddressIncomplete.setVisibility(View.GONE);

                        } else {
                            Toast.makeText(getContext(), "Some error occurred, try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        addAddressDialog.show();
    }

    private void updateAddress(OnCompleteListener<Void> completeListener) {
        db.collection(Constants.COLLECTION_ADDRESSES)
                .document(CommonVariables.loggedInUserAddress.id)
                .set(CommonVariables.loggedInUserAddress)
                .addOnCompleteListener(completeListener);
    }
}
