package com.pentaware.foodie.models;

import com.pentaware.foodie.utils.enums.MealOfDay;

import java.util.List;

public class FoodItem {
    public String id;
    public String restaurantId;
    public String subCategoryId;
    public String categoryId;
    public String restaurantCustomCategoryId;

    public String name;
    public String description;
    public String imageUrl;
    public int price;
    public int discountPercentage;
    public boolean isNonVeg;
    public FoodItemCustomizationOptions customizationOptions;
    public List<MealOfDay> mealOfDay;

    @Override
    public String toString() {
        return "FoodItem{" +
                "id='" + id + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                ", subCategoryId='" + subCategoryId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", restaurantCustomCategoryId='" + restaurantCustomCategoryId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", price=" + price +
                ", discountPercentage=" + discountPercentage +
                ", isNonVeg=" + isNonVeg +
                ", customizationOptions=" + customizationOptions +
                ", mealOfDay=" + mealOfDay +
                '}';
    }
}
