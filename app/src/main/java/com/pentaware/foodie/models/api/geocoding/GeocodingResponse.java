package com.pentaware.foodie.models.api.geocoding;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeocodingResponse {
    @SerializedName("results")
    List<GeocodingResult> geocodingResultList;

    public List<GeocodingResult> getGeocodingResultList() {
        return geocodingResultList;
    }

    @Override
    public String toString() {
        return "GeocodingResponse{" +
                "geocodingResultList=" + geocodingResultList +
                '}';
    }
}
