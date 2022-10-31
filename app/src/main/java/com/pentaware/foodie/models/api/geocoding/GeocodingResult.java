package com.pentaware.foodie.models.api.geocoding;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.pentaware.foodie.models.Address;

import java.util.ArrayList;
import java.util.List;

public class GeocodingResult {

    public Geometry geometry;

    @SerializedName("place_id")
    public String placeId;

    @SerializedName("formatted_address")
    public String formattedAddress;

    @SerializedName("address_components")
    public List<AddressComponent> addressComponents;

    public Address getPlaceAddress() {
        Address address = new Address();

        List<String> addressLine2Values = new ArrayList<>();

        String neighbourhood = getAddressComponentLongName(AddressComponent.NEIGHBOURHOOD);
        if(!neighbourhood.isEmpty()) {
            addressLine2Values.add(neighbourhood);
        }

        String subLocality = getAddressComponentLongName(AddressComponent.SUB_LOCALITY);
        if(!subLocality.isEmpty()) {
            addressLine2Values.add(subLocality);
        }

        if(addressLine2Values.isEmpty()) {
            String locality = getAddressComponentLongName(AddressComponent.LOCALITY);
            addressLine2Values.add(locality);
        }

        Log.d("AddressComponent", "getPlaceAddress: " + addressLine2Values);
        Log.d("AddressComponent", "getPlaceAddress: " + String.join(", ", addressLine2Values));

        address.addressLine2 = String.join(", ", addressLine2Values);
        address.city = getAddressComponentLongName(AddressComponent.CITY);
        address.state = getAddressComponentLongName(AddressComponent.STATE);
        address.pinCode = getAddressComponentLongName(AddressComponent.POSTAL_CODE);

        return address;
    }

    private String getAddressComponentLongName(String componentType) {
        List<String> componentValues = new ArrayList<>();

        for(AddressComponent addressComponent: addressComponents) {
            if(componentValues.size() >= 2) {
                break;
            }

            if(addressComponent.types.contains(componentType)) {
                componentValues.add(addressComponent.longName);
            }
        }

        return componentValues.size() > 0 ? String.join(", ", componentValues) : "";
    }

    @Override
    public String toString() {
        return "GeocodingResult{" +
                "geometry=" + geometry +
                ", placeId='" + placeId + '\'' +
                ", formattedAddress='" + formattedAddress + '\'' +
                '}';
    }
}
