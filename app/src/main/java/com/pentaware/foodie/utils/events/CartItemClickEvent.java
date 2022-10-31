package com.pentaware.foodie.utils.events;

import com.pentaware.foodie.models.CartItem;

public interface CartItemClickEvent {
    void onPlusButtonClick(CartItem cartItem);
    void onMinusButtonClick(CartItem cartItem);
}
