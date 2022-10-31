package com.pentaware.foodie.models.maps;

public class PlacePrediction {
    public String placeId;
    public String placeName;
    public String placeAddress;
    public String fullAddress;

    public PlacePrediction() {}

    public PlacePrediction(String placeId, String placeName, String placeAddress, String fullAddress) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.fullAddress = fullAddress;
    }
}
