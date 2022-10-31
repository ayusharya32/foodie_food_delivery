package com.pentaware.foodie.models;

public class FoodCategory {
    public String id;
    public String name;
    public String imageUrl;

    public FoodCategory() {}

    public FoodCategory(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
