package com.pentaware.foodie.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pentaware.foodie.databinding.ItemOrderBinding;
import com.pentaware.foodie.models.Order;
import com.pentaware.foodie.utils.CommonMethods;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.events.OrderClickEvent;

import java.util.List;

public class OrdersAdapter extends
        RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    public List<Order> orderList;
    public OrderClickEvent clickEvent;

    public OrdersAdapter(List<Order> orderList, OrderClickEvent clickEvent) {
        this.orderList = orderList;
        this.clickEvent = clickEvent;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() { return orderList.size(); }

    @SuppressLint("SetTextI18n")
    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickEvent != null) {
                        Order order = orderList.get(getBindingAdapterPosition());
                        clickEvent.onItemClick(order);
                    }
                }
            });
        }

        public void bind(Order order) {
            String orderIdString = order.id;
            binding.txtOrderId.setText(orderIdString);

            String priceString = CommonVariables.rupeeSymbol + (order.foodItemsCost + order.deliveryFee
                    + order.tip);
            binding.txtOrderPrice.setText(priceString);

            String paymentMethodString = CommonMethods.getPaymentMethodString(order.paymentMethod);
            binding.txtPaymentMethod.setText(paymentMethodString);

            String receivedString = "Received: " + CommonMethods.getFormattedDateTime(
                    order.orderTime,
                    "dd-MM-yyyy HH:mm"
            );
            binding.txtOrderReceived.setText(receivedString);

            String orderStatusString = "Status: " + CommonMethods.getOrderStatusString(order.orderStatus);
            binding.txtOrderStatus.setText(orderStatusString);

            loadRestaurantDetails(order);
            loadDeliveryAddressDetails(order);
        }

        private void loadRestaurantDetails(Order order) {
            if(order.restaurant == null) {
                binding.txtRestaurantName.setText("Restaurant Name");
                return;
            }

            binding.txtRestaurantName.setText(order.restaurant.name);
        }

        private void loadDeliveryAddressDetails(Order order) {
            if(order.deliveryAddress == null) {
                binding.txtDeliveryAddress.setText("Delivery Location");
                return;
            }

            binding.txtDeliveryAddress.setText(order.deliveryAddress.getAddressString());
        }
    }
}
