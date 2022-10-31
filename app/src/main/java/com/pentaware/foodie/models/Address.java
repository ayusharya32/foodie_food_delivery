package com.pentaware.foodie.models;

import com.pentaware.foodie.utils.enums.AddressType;

import java.io.Serializable;

public class Address implements Serializable {

    public String id;
    public String userId;
    public String name;
    public String phone;
    public String addressLine1;
    public String addressLine2;
    public String city;
    public String state;
    public String pinCode;
    public double latitude;
    public double longitude;
    public AddressType addressType;

    public String locationCoordinateHash;

    public Address() {}

    public Address(String name, String phone, String addressLine1, String addressLine2,
                   String pinCode, String city, String state) {
        this.name = name;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.pinCode = pinCode;
        this.city = city;
        this.state = state;
    }

    public String getAddressString() {
        return (addressLine1 != null ? (addressLine1 + "\n") : "") +
                addressLine2 + "\n" + city + ", " + state + ", " + pinCode;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", pinCode='" + pinCode + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", addressType=" + addressType +
                '}';
    }
}
