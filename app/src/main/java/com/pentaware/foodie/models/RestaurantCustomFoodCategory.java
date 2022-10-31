package com.pentaware.foodie.models;

public class RestaurantCustomFoodCategory {
    public String id;
    public String name;
    public String restaurantId;

    public RestaurantCustomFoodCategory() {}

    public RestaurantCustomFoodCategory(String id, String name, String restaurantId) {
        this.id = id;
        this.name = name;
        this.restaurantId = restaurantId;
    }
}
