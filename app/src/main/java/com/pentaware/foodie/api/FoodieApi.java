package com.pentaware.foodie.api;

import com.pentaware.foodie.models.api.EmailRequest;
import com.pentaware.foodie.models.api.geocoding.GeocodingResponse;
import com.pentaware.foodie.models.maps.directions.DirectionResponse;
import com.pentaware.foodie.models.maps.distance.DistanceResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FoodieApi {

    @POST("sendMail")
    Call<Void> sendMail(
            @Body EmailRequest emailRequest
    );

    @GET("geocode/json")
    Call<GeocodingResponse> getLatLngFromPlaceId(
            @Query("place_id") String placeId
    );

    @GET("geocode/json")
    Call<GeocodingResponse> getPlaceFromLatLng(
            @Query("latlng") String latlng
    );

    @GET("directions/json")
    Call<DirectionResponse> getDirection(
            @Query("origin") String origin,
            @Query("destination") String destination
    );

    @GET("distancematrix/json")
    Call<DistanceResponse> getDistance(
            @Query("origins") String startingPoint,
            @Query("destinations") String destination
    );
}
