package com.test.weighttrackingapplicationmatthewbates;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {
    private final List<ProgressItem> weightList;
    private final HashSet<String> selectedDates; // To track selected dates

    public WeightAdapter(List<ProgressItem> weightList) {
        this.weightList = weightList;
        this.selectedDates = new HashSet<>(); // Initialize the set for selected dates
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        ProgressItem currentItem = weightList.get(position);
        holder.dateTextView.setText(currentItem.getDate());
        holder.weightTextView.setText(String.valueOf(currentItem.getWeight()));

        // Set checkbox checked state
        holder.checkBox.setChecked(selectedDates.contains(currentItem.getDate()));

        // Handle checkbox click
        holder.checkBox.setOnClickListener(v -> {
            if (holder.checkBox.isChecked()) {
                selectedDates.add(currentItem.getDate()); // Add to selected
            } else {
                selectedDates.remove(currentItem.getDate()); // Remove from selected
            }
        });
    }

    @Override
    public int getItemCount() {
        return weightList.size();
    }

    public List<String> getSelectedDates() {
        return new ArrayList<>(selectedDates); // Return selected dates as a list
    }

    public static class WeightViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView weightTextView;
        public CheckBox checkBox; // Add a CheckBox for selection

        public WeightViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_data);
            weightTextView = itemView.findViewById(R.id.weight_data);
            checkBox = itemView.findViewById(R.id.select_checkbox); // Ensure this matches your item layout
        }
    }
}
