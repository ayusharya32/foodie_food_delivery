package com.pentaware.foodie.utils.events;

import com.pentaware.foodie.models.FoodItem;

public interface FoodItemClickEvent {
    void onAddButtonClick(FoodItem foodItem);
    void onPlusButtonClick(FoodItem foodItem);
    void onMinusButtonClick(FoodItem foodItem);
}
