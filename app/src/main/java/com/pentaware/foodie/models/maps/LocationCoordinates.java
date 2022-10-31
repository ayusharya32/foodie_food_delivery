package com.pentaware.foodie.models.maps;

import java.io.Serializable;

public class LocationCoordinates implements Serializable {
    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public LocationCoordinates() {}

    public LocationCoordinates(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "LocationCoordinates{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
