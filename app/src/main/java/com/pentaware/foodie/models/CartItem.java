package com.pentaware.foodie.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class CartItem {
    public String id;

    public String userId;
    public String foodItemId;
    public int quantity;

    public Date itemUpdatedTime;

    @Exclude
    public FoodItem foodItem;

    public CartItem() {}

    public CartItem(String id, String userId, String foodItemId, int quantity, Date itemUpdatedTime) {
        this.id = id;
        this.userId = userId;
        this.foodItemId = foodItemId;
        this.quantity = quantity;
        this.itemUpdatedTime = itemUpdatedTime;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", foodItemId='" + foodItemId + '\'' +
                ", quantity=" + quantity +
                ", itemUpdatedTime=" + itemUpdatedTime +
                '}';
    }
}
