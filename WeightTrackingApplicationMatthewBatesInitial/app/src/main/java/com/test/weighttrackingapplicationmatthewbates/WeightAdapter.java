package com.test.weighttrackingapplicationmatthewbates;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {
    private final List<ProgressItem> weightList;

    public WeightAdapter(List<ProgressItem> weightList) {
        this.weightList = weightList;
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
    }

    @Override
    public int getItemCount() {
        return weightList.size();
    }

    public static class WeightViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView weightTextView;

        public WeightViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_data);
            weightTextView = itemView.findViewById(R.id.weight_data);
        }
    }
}
