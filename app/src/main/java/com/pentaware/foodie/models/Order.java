package com.pentaware.foodie.models;

import com.google.firebase.firestore.Exclude;
import com.pentaware.foodie.models.maps.LocationCoordinates;
import com.pentaware.foodie.utils.enums.DeliveryPartnerStatus;
import com.pentaware.foodie.utils.enums.OrderStatus;
import com.pentaware.foodie.utils.enums.PaymentMethod;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    public String id;

    public String restaurantId;
    public String deliveryPartnerId;
    public String customerId;

    public List<OrderItem> orderItems;
    public float foodItemsCost;
    public float deliveryFee;
    public float tip;

    public DeliveryPartnerStatus deliveryPartnerStatus;
    public OrderStatus orderStatus;

    public Date orderTime;
    public Date deliveryTime;
    public String cancellationReason;

    public String deliveryAddressId;
    public String deliveryLocationCoordinateHash;
    public LocationCoordinates deliveryLocationCoordinates;

    public PaymentMethod paymentMethod;

    @Exclude
    public Restaurant restaurant;

    @Exclude
    public User customer;

    @Exclude
    public Address deliveryAddress;

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                ", deliveryPartnerId='" + deliveryPartnerId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", orderItems=" + orderItems +
                ", foodItemsCost=" + foodItemsCost +
                ", deliveryFee=" + deliveryFee +
                ", tip=" + tip +
                ", deliveryPartnerStatus=" + deliveryPartnerStatus +
                ", orderStatus=" + orderStatus +
                ", orderTime=" + orderTime +
                ", deliveryTime=" + deliveryTime +
                ", cancellationReason='" + cancellationReason + '\'' +
                ", deliveryAddressId='" + deliveryAddressId + '\'' +
                ", deliveryLocationCoordinateHash='" + deliveryLocationCoordinateHash + '\'' +
                ", deliveryLocationCoordinates=" + deliveryLocationCoordinates +
                ", paymentMethod=" + paymentMethod +
                ", restaurant=" + restaurant +
                ", customer=" + customer +
                ", deliveryAddress=" + deliveryAddress +
                '}';
    }
}
