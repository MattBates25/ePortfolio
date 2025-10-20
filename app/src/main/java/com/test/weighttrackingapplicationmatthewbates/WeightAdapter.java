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

/**
 * An adapter to bridge the List of ProgressItem data with the RecyclerView in the ProgressFragment.
 * It manages the creation and binding of views for each weight entry and handles user selections.
 */
public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {
    private final List<ProgressItem> weightList;
    private final HashSet<Integer> selectedItemIds;

    /**
     * Constructor for the WeightAdapter.
     * @param weightList The list of ProgressItem objects to be displayed.
     */
    public WeightAdapter(List<ProgressItem> weightList) {
        this.weightList = weightList;
        this.selectedItemIds = new HashSet<>();
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new WeightViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
        return new WeightViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the ViewHolder to reflect the item at the given position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        ProgressItem currentItem = weightList.get(position);
        holder.dateTextView.setText(currentItem.getDate());
        holder.weightTextView.setText(String.valueOf(currentItem.getWeight()));

        // Set checkbox checked state based on the item's unique ID
        holder.checkBox.setChecked(selectedItemIds.contains(currentItem.getWeightId()));

        // Handle checkbox click to add or remove the item's ID from the selection set
        holder.checkBox.setOnClickListener(v -> {
            if (holder.checkBox.isChecked()) {
                selectedItemIds.add(currentItem.getWeightId());
            } else {
                selectedItemIds.remove(currentItem.getWeightId());
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return weightList.size();
    }

    /**
     * Retrieves a list of the unique database IDs for all items currently selected by the user.
     * @return A List of Integer IDs.
     */
    public List<Integer> getSelectedItemIds() {
        return new ArrayList<>(selectedItemIds);
    }

    /**
     * Clears the current selection of items.
     */
    public void clearSelection() {
        selectedItemIds.clear();
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * It holds references to the individual views within the item layout to avoid expensive findViewById calls.
     */
    public static class WeightViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView weightTextView;
        public CheckBox checkBox;

        public WeightViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_data);
            weightTextView = itemView.findViewById(R.id.weight_data);
            checkBox = itemView.findViewById(R.id.select_checkbox);
        }
    }
}
