package com.pentaware.foodie.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pentaware.foodie.R;
import com.pentaware.foodie.databinding.ItemCartItemBinding;
import com.pentaware.foodie.models.CartItem;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.events.CartItemClickEvent;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {
    private static final String TAG = "CartItemAdapteryy";

    public List<CartItem> cartItems;
    private CartItemClickEvent clickEvent;

    public CartItemAdapter(List<CartItem> cartItems, CartItemClickEvent clickEvent) {
        this.cartItems = cartItems;
        this.clickEvent = clickEvent;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartItemBinding binding = ItemCartItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new CartItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    public class CartItemViewHolder extends RecyclerView.ViewHolder {
        private ItemCartItemBinding binding;

        public CartItemViewHolder(ItemCartItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.imgPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickEvent != null) {
                        CartItem cartItem = cartItems.get(getBindingAdapterPosition());
                        clickEvent.onPlusButtonClick(cartItem);
                    }
                }
            });

            binding.imgMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickEvent != null) {
                        CartItem cartItem = cartItems.get(getBindingAdapterPosition());
                        clickEvent.onMinusButtonClick(cartItem);
                    }
                }
            });
        }

        public void bind(CartItem item) {
            if(item.foodItem != null) {
                binding.txtItemName.setText(item.foodItem.name);
                binding.txtItemPrice.setText(CommonVariables.rupeeSymbol + item.foodItem.price);

                if(item.foodItem.isNonVeg) {
                    binding.imgVegNonVeg.setImageResource(R.drawable.ic_non_veg);
                } else {
                    binding.imgVegNonVeg.setImageResource(R.drawable.ic_veg);
                }
            }

            setupItemCount(item);
        }

        private void setupItemCount(CartItem currentItem) {
            binding.txtItemCount.setText(Integer.toString(currentItem.quantity));
        }
    }
}
