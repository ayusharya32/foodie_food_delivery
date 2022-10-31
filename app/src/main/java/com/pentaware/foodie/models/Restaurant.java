package com.pentaware.foodie.models;

import com.pentaware.foodie.utils.enums.RestaurantType;

import java.io.Serializable;
import java.util.List;

public class Restaurant implements Serializable {
    public String id;
    public String ownerId;

    public String name;
    public String phone;

    public String addressLine1;
    public String addressLine2;
    public String city;
    public String state;
    public String pinCode;
    public double latitude;
    public double longitude;

    public List<String> showcaseFoodImages;
    public List<String> restaurantImages;
    public List<String> menuImages;

    public List<String> mainCategories;
    public float rating;
    public int totalReviews;

    public boolean dineInAvailable;
    public RestaurantType type;

    public String locationCoordinateHash;
}
