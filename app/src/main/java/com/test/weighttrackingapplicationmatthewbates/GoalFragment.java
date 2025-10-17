package com.test.weighttrackingapplicationmatthewbates;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GoalFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private TextView currentGoalValue;
    private EditText editGoal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal, container, false);

        // Initialize views
        currentGoalValue = view.findViewById(R.id.current_goal_value);
        editGoal = view.findViewById(R.id.edit_goal);
        Button changeGoalButton = view.findViewById(R.id.change_goal_button);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(getContext());

        // Load the current goal
        loadCurrentGoal();

        // Set the button click listener
        changeGoalButton.setOnClickListener(v -> updateGoal());

        return view;
    }

    private void loadCurrentGoal() {
        // Get user ID from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("USER_ID", -1); // Default to -1 for error checking

        if (userId != -1) {
            // Fetch the current goal from the database
            float currentGoal = databaseHelper.getWeightGoal(userId);
            currentGoalValue.setText(String.valueOf(currentGoal));
        } else {
            Toast.makeText(getContext(), "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGoal() {
        String newGoalStr = editGoal.getText().toString();
        if (!newGoalStr.isEmpty()) {
            float newGoal = Float.parseFloat(newGoalStr);

            // Get user ID from SharedPreferences
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = sharedPreferences.getInt("USER_ID", -1); // Default to -1 for error checking

            if (userId != -1) {
                // Update the goal in the database
                databaseHelper.updateWeightGoal(userId, newGoal);
                currentGoalValue.setText(newGoalStr); // Update the displayed goal
                Toast.makeText(getContext(), "Goal updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Please enter a valid weight goal", Toast.LENGTH_SHORT).show();
        }
    }
}
