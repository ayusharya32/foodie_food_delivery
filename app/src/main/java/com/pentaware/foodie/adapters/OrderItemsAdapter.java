package com.pentaware.foodie.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pentaware.foodie.databinding.ItemOrderItemBinding;
import com.pentaware.foodie.models.OrderItem;
import com.pentaware.foodie.utils.CommonVariables;

import java.util.List;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {

    public List<OrderItem> orderItems;

    public OrderItemsAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderItemBinding binding = ItemOrderItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new OrderItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem orderItem = orderItems.get(position);
        holder.bind(orderItem);
    }

    @Override
    public int getItemCount() { return orderItems.size(); }

    public class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private ItemOrderItemBinding binding;

        public OrderItemViewHolder(ItemOrderItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(OrderItem item) {
            binding.txtItemName.setText(item.name);
            binding.txtItemPrice.setText(CommonVariables.rupeeSymbol + item.price);

            binding.txtItemQty.setText("" + item.quantity);
        }
    }
}
