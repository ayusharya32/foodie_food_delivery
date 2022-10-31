package com.pentaware.foodie.models;

import com.google.firebase.firestore.Exclude;

public class OrderItem {
    public String foodItemId;
    public int quantity;
    public String name;
    public float price;

    @Exclude
    public FoodItem foodItem;

    public OrderItem() {}

    public OrderItem(String foodItemId, int quantity) {
        this.foodItemId = foodItemId;
        this.quantity = quantity;
    }
}
