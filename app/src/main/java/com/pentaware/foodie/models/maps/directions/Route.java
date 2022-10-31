package com.pentaware.foodie.models.maps.directions;

import com.google.gson.annotations.SerializedName;

public class Route {
    @SerializedName("overview_polyline")
    public RoutePolyline routePolyline;
}
