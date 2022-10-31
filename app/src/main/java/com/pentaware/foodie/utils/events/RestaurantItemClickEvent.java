package com.pentaware.foodie.utils.events;

import com.pentaware.foodie.models.Restaurant;

public interface RestaurantItemClickEvent {
    void onItemClick(Restaurant restaurant);
}
