package com.pentaware.foodie.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pentaware.foodie.R;
import com.pentaware.foodie.databinding.ItemFoodItemBinding;
import com.pentaware.foodie.models.CartItem;
import com.pentaware.foodie.models.FoodItem;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.events.FoodItemClickEvent;

import java.util.ArrayList;
import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder> {
    private static final String TAG = "FoodItemAdaptery";

    public List<FoodItem> foodItems;
    private FoodItemClickEvent clickEvent;

    public FoodItemAdapter(List<FoodItem> foodItems, FoodItemClickEvent clickEvent) {
        this.foodItems = foodItems;
        this.clickEvent = clickEvent;
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodItemBinding binding = ItemFoodItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new FoodItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {
        FoodItem foodItem = foodItems.get(position);
        holder.bind(foodItem);
    }

    @Override
    public int getItemCount() { return foodItems.size(); }

    public class FoodItemViewHolder extends RecyclerView.ViewHolder {
        private ItemFoodItemBinding binding;

        public FoodItemViewHolder(ItemFoodItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.txtAddItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickEvent != null) {
                        FoodItem foodItem = foodItems.get(getBindingAdapterPosition());
                        clickEvent.onAddButtonClick(foodItem);
                    }
                }
            });

            binding.imgPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickEvent != null) {
                        FoodItem foodItem = foodItems.get(getBindingAdapterPosition());
                        clickEvent.onPlusButtonClick(foodItem);
                    }
                }
            });

            binding.imgMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickEvent != null) {
                        FoodItem foodItem = foodItems.get(getBindingAdapterPosition());
                        clickEvent.onMinusButtonClick(foodItem);
                    }
                }
            });
        }

        public void bind(FoodItem item) {
            binding.txtItemName.setText(item.name);
            binding.txtItemPrice.setText(CommonVariables.rupeeSymbol + item.price);

            Glide.with(binding.getRoot())
                    .load(item.imageUrl)
                    .into(binding.imgRestaurant);

            if(item.isNonVeg) {
                binding.imgVegNonVeg.setImageResource(R.drawable.ic_non_veg);
            } else {
                binding.imgVegNonVeg.setImageResource(R.drawable.ic_veg);
            }

            setupItemCount(item);
        }

        private void setupItemCount(FoodItem foodItem) {
            CartItem cartItem = null;

            if(CommonVariables.userCartItems == null) {
                CommonVariables.userCartItems = new ArrayList<>();
            }

            if(!CommonVariables.userCartItems.isEmpty()) {
                Log.d(TAG, "setupItemCount: " + CommonVariables.userCartItems);
                for(CartItem item: CommonVariables.userCartItems) {
                    if(item.foodItemId.equals(foodItem.id)) {
                        cartItem = item;
                    }
                }
            }

            if(cartItem == null) {
                binding.txtAddItem.setVisibility(View.VISIBLE);
                binding.llItemCount.setVisibility(View.GONE);

            } else {
                binding.txtAddItem.setVisibility(View.GONE);
                binding.llItemCount.setVisibility(View.VISIBLE);

                binding.txtItemCount.setText(Integer.toString(cartItem.quantity));
            }
        }
    }
}
