package com.pentaware.foodie.adapters;

import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.pentaware.foodie.databinding.ItemRestaurantBinding;
import com.pentaware.foodie.models.Restaurant;
import com.pentaware.foodie.utils.CommonMethods;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.events.RestaurantItemClickEvent;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
    private static final String TAG = "RestaurantAdapteryy";

    public List<Restaurant> restaurantList;
    private RestaurantItemClickEvent clickEvent;

    public RestaurantAdapter(List<Restaurant> restaurantList, RestaurantItemClickEvent clickEvent) {
        this.restaurantList = restaurantList;
        this.clickEvent = clickEvent;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRestaurantBinding binding = ItemRestaurantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new RestaurantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.bind(restaurant);
    }

    @Override
    public int getItemCount() { return restaurantList.size(); }

    public class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private ItemRestaurantBinding binding;

        public RestaurantViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickEvent != null) {
                        Restaurant restaurant = restaurantList.get(getBindingAdapterPosition());
                        clickEvent.onItemClick(restaurant);
                    }
                }
            });
        }

        public void bind(Restaurant restaurant) {
            binding.txtRestaurantName.setText(restaurant.name);

            String mainCategories = String.join(", ", restaurant.mainCategories);
            binding.txtMainCategories.setText(mainCategories);

            binding.txtPlaceName.setText(restaurant.addressLine1);

            double distanceToRestaurant = GeoFireUtils.getDistanceBetween(
                    new GeoLocation(restaurant.latitude, restaurant.longitude),
                    new GeoLocation(CommonVariables.loggedInUserAddress.latitude,
                            CommonVariables.loggedInUserAddress.longitude)
            ) / 1000;

            String distanceString = CommonMethods.getDecimalValueUpToOnePlace(distanceToRestaurant)
                    + " kms";

            binding.txtDistance.setText(distanceString);

            if(distanceToRestaurant <= 2) {
                binding.txtDeliveryCost.setText("Free Delivery");

            } else {
                Log.d(TAG, "bind: Delivery Cost: " + CommonVariables.appInfo.deliveryCostPerKm
                        * distanceToRestaurant);

                float deliveryCost = (float) (CommonVariables.appInfo.deliveryCostPerKm
                        * distanceToRestaurant);

                if(distanceToRestaurant > 2 && distanceToRestaurant < 15) {
                    deliveryCost = 20;
                }

                String deliveryCostString = CommonVariables.rupeeSymbol +
                        CommonMethods.getDecimalValueUpToOnePlace(deliveryCost) + " Delivery";
                binding.txtDeliveryCost.setText(deliveryCostString);
            }

            String imageUrl = restaurant.showcaseFoodImages != null ?
                    restaurant.showcaseFoodImages.get(0) : null;

            Log.d(TAG, "bind: ImageUrl " + imageUrl);

            if(imageUrl != null && !imageUrl.isEmpty()) {
                Log.d(TAG, "bind: Adding Image");

                Glide.with(binding.getRoot())
                        .load(imageUrl)
                        .into(binding.imgRestaurant);
            }
        }
    }
}
