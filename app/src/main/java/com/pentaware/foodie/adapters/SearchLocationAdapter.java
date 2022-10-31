package com.pentaware.foodie.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pentaware.foodie.databinding.ItemSearchLocationBinding;
import com.pentaware.foodie.models.maps.PlacePrediction;
import com.pentaware.foodie.utils.events.PlacePredictionClickEvent;

import java.util.List;

public class SearchLocationAdapter extends RecyclerView.Adapter<SearchLocationAdapter.SearchLocationViewHolder> {
    private static final String TAG = "SearchLocationAdapteryy";

    public List<PlacePrediction> predictions;
    private PlacePredictionClickEvent clickEvent;

    public SearchLocationAdapter(List<PlacePrediction> predictions, PlacePredictionClickEvent clickEvent) {
        this.predictions = predictions;
        this.clickEvent = clickEvent;
    }

    @NonNull
    @Override
    public SearchLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchLocationBinding binding = ItemSearchLocationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new SearchLocationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchLocationViewHolder holder, int position) {
        PlacePrediction placePrediction = predictions.get(position);
        holder.bind(placePrediction);
    }

    @Override
    public int getItemCount() { return predictions.size(); }

    public class SearchLocationViewHolder extends RecyclerView.ViewHolder {
        private ItemSearchLocationBinding binding;

        public SearchLocationViewHolder(ItemSearchLocationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickEvent != null) {
                        PlacePrediction placePrediction = predictions.get(getBindingAdapterPosition());
                        clickEvent.onItemClick(placePrediction);
                    }
                }
            });
        }

        public void bind(PlacePrediction prediction) {
            binding.txtPlaceName.setText(prediction.placeName);
            binding.txtPlaceAddress.setText(prediction.placeAddress);
        }
    }
}
