package com.pentaware.foodie.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pentaware.foodie.databinding.ItemSubCategoryBinding;
import com.pentaware.foodie.models.FoodSubCategory;
import com.pentaware.foodie.utils.events.SubCategoryClickEvent;

import java.util.List;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder> {

    public List<FoodSubCategory> subCategoryList;
    private SubCategoryClickEvent clickEvent;

    public SubCategoryAdapter(List<FoodSubCategory> subCategoryList, SubCategoryClickEvent clickEvent) {
        this.subCategoryList = subCategoryList;
        this.clickEvent = clickEvent;
    }

    @NonNull
    @Override
    public SubCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubCategoryBinding binding = ItemSubCategoryBinding.inflate(LayoutInflater.from(
                parent.getContext()), parent, false);
        return new SubCategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubCategoryViewHolder holder, int position) {
        FoodSubCategory subCategory = subCategoryList.get(position);
        holder.bind(subCategory);
    }

    @Override
    public int getItemCount() { return subCategoryList.size(); }

    public class SubCategoryViewHolder extends RecyclerView.ViewHolder {
        private ItemSubCategoryBinding binding;

        public SubCategoryViewHolder(ItemSubCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickEvent != null) {
                        FoodSubCategory subCategory = subCategoryList.get(getBindingAdapterPosition());
                        clickEvent.onItemClick(subCategory);
                    }
                }
            });
        }

        public void bind(FoodSubCategory subCategory) {
            binding.txtName.setText(subCategory.name);

            Glide.with(binding.getRoot())
                    .load(subCategory.imageUrl)
                    .into(binding.imgSubCategory);
        }
    }
}
