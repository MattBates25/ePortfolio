package com.test.weighttrackingapplicationmatthewbates;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ProgressFragment extends Fragment {

    private RecyclerView recyclerView;
    private WeightAdapter weightAdapter;
    private List<ProgressItem> weightItemList;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(getContext());

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.weight_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize the weight item list
        weightItemList = new ArrayList<>();

        // Initialize and set the adapter
        weightAdapter = new WeightAdapter(weightItemList);
        recyclerView.setAdapter(weightAdapter);

        // Load weights from the database after initializing the adapter
        loadWeightData();

        // Set up the Add button
        Button addWeightButton = view.findViewById(R.id.add_weight_button);
        addWeightButton.setOnClickListener(v -> addWeight());

        // Set up the Delete button
        Button deleteWeightButton = view.findViewById(R.id.delete_weight_button);
        deleteWeightButton.setOnClickListener(v -> deleteSelectedWeights());

        return view;
    }

    private void loadWeightData() {
        weightItemList.clear(); // Clear existing data

        // Retrieve the user ID from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("USER_ID", -1); // Default to -1 for error checking

        if (userId != -1) {
            weightItemList.addAll(databaseHelper.getAllWeightEntries(userId)); // Pass userId to fetch data
            weightAdapter.notifyDataSetChanged(); // Notify the adapter of data change
        } else {
            Toast.makeText(getContext(), "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }



    private void addWeight() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_weight, null);
        builder.setView(dialogView);

        EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        EditText editTextWeight = dialogView.findViewById(R.id.editTextWeight);
        Button buttonAddWeight = dialogView.findViewById(R.id.buttonAddWeight);

        AlertDialog dialog = builder.create();

        buttonAddWeight.setOnClickListener(v -> {
            String date = editTextDate.getText().toString();
            String weightStr = editTextWeight.getText().toString();

            if (!date.isEmpty() && !weightStr.isEmpty()) {
                float weight = Float.parseFloat(weightStr);

                // Retrieve only the user ID from SharedPreferences
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                int userId = sharedPreferences.getInt("USER_ID", -1); // Default to -1 for error checking

                if (userId != -1) {
                    // Call the DatabaseHelper to add weight
                    databaseHelper.addWeight(date, weight, userId);

                    // Check if the new weight is below the goal weight
                    Float weightGoal = databaseHelper.getWeightGoal(userId); // Retrieve weight goal
                    if (weight < weightGoal) {
                        // Get the user's phone number
                        String phoneNumber = databaseHelper.getUserPhoneNumber(userId);
                        String message = "Congratulations! You've reached your weight goal!";

                        // Get a reference to MainActivity and call sendSMSNotification
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if (mainActivity != null) {
                            mainActivity.sendSMSNotification(phoneNumber, message);
                        }
                    }

                    // Reload the weights to display the new entry
                    loadWeightData();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please enter both date and weight", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void deleteSelectedWeights() {
        List<String> selectedDates = weightAdapter.getSelectedDates();
        if (!selectedDates.isEmpty()) {
            databaseHelper.deleteSelectedWeights(selectedDates);
            loadWeightData(); // Refresh data after deletion
        }
    }
}
