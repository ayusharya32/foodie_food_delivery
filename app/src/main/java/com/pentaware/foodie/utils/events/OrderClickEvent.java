package com.pentaware.foodie.utils.events;

import com.pentaware.foodie.models.Order;

public interface OrderClickEvent {
    void onItemClick(Order order);
}
