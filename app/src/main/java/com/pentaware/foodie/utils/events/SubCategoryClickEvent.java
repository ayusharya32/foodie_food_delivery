package com.pentaware.foodie.utils.events;

import com.pentaware.foodie.models.FoodSubCategory;

public interface SubCategoryClickEvent {
    void onItemClick(FoodSubCategory subCategory);
}
