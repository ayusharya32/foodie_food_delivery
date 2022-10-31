package com.pentaware.foodie.utils.events;

import com.pentaware.foodie.models.maps.PlacePrediction;

public interface PlacePredictionClickEvent {
    void onItemClick(PlacePrediction prediction);
}
