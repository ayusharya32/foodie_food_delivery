package com.pentaware.foodie.models.api.geocoding;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressComponent {
    public static final String PLUS_CODE = "plus_code";
    public static final String NEIGHBOURHOOD = "neighborhood";
    public static final String SUB_LOCALITY = "sublocality";
    public static final String LOCALITY = "locality";
    public static final String CITY = "administrative_area_level_2";
    public static final String STATE = "administrative_area_level_1";
    public static final String POSTAL_CODE = "postal_code";

    @SerializedName("long_name")
    public String longName;

    @SerializedName("short_name")
    public String shortName;

    public List<String> types;
}
