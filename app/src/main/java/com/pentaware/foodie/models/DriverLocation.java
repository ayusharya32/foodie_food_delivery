package com.pentaware.foodie.models;

import com.pentaware.foodie.models.maps.LocationCoordinates;

import java.io.Serializable;

public class DriverLocation implements Serializable {
    public String driverId;

    public LocationCoordinates locationCoordinates;
}
