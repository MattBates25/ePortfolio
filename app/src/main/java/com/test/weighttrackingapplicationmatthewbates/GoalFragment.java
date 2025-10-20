package com.test.weighttrackingapplicationmatthewbates;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A fragment that allows the user to view and update their weight goal and phone number for SMS alerts.
 * It interacts with the DatabaseHelper to persist this information.
 */
public class GoalFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private TextView currentGoalValue;
    private EditText editGoal;
    private EditText editPhoneNumber;

    /**
     * Inflates the layout, initializes UI components and the database helper,
     * and sets up click listeners for the buttons.
     * @param inflater The LayoutInflater object that can be used to inflate views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal, container, false);

        // Initialize views
        currentGoalValue = view.findViewById(R.id.current_goal_value);
        editGoal = view.findViewById(R.id.edit_goal);
        editPhoneNumber = view.findViewById(R.id.edit_phone_number);
        Button changeGoalButton = view.findViewById(R.id.change_goal_button);
        Button savePhoneButton = view.findViewById(R.id.save_phone_button);

        databaseHelper = new DatabaseHelper(getContext());

        loadUserData();

        changeGoalButton.setOnClickListener(v -> updateGoal());
        savePhoneButton.setOnClickListener(v -> updatePhoneNumber());

        return view;
    }

    /**
     * Loads the current user's data (weight goal and phone number) from the database
     * and populates the corresponding UI fields.
     */
    private void loadUserData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(AppConstants.KEY_USER_ID, -1);

        if (userId != -1) {
            // Fetch and display the current goal
            Float currentGoal = databaseHelper.getWeightGoal(userId);
            if (currentGoal != null) {
                currentGoalValue.setText(String.valueOf(currentGoal));
            }

            // Fetch and display the current phone number
            String phoneNumber = databaseHelper.getUserPhoneNumber(userId);
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                editPhoneNumber.setText(phoneNumber);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates and updates the user's weight goal in the database.
     * Provides user feedback via Toasts and clears the input field on success.
     */
    private void updateGoal() {
        String newGoalStr = editGoal.getText().toString().trim();
        if (newGoalStr.isEmpty()) {
            editGoal.setError(getString(R.string.error_field_required));
            return;
        }

        float newGoal;
        try {
            newGoal = Float.parseFloat(newGoalStr);
            if (newGoal <= 0) {
                editGoal.setError(getString(R.string.error_goal_positive));
                return;
            }
        } catch (NumberFormatException e) {
            editGoal.setError(getString(R.string.error_valid_number));
            return;
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(AppConstants.KEY_USER_ID, -1);

        if (userId != -1) {
            boolean isSuccess = databaseHelper.updateWeightGoal(userId, newGoal);
            if (isSuccess) {
                currentGoalValue.setText(newGoalStr);
                editGoal.setText(""); // Clear the input field
                Toast.makeText(getContext(), getString(R.string.goal_updated_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.goal_updated_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates and updates the user's phone number in the database.
     * It uses Android's built-in phone number pattern for validation.
     */
    private void updatePhoneNumber() {
        String phoneNumber = editPhoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            editPhoneNumber.setError(getString(R.string.error_field_required));
            return;
        }

        if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            editPhoneNumber.setError(getString(R.string.error_invalid_phone_number));
            return;
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(AppConstants.KEY_USER_ID, -1);

        if (userId != -1) {
            boolean isSuccess = databaseHelper.updatePhoneNumber(userId, phoneNumber);
            if (isSuccess) {
                Toast.makeText(getContext(), getString(R.string.phone_updated_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.phone_updated_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
        }
    }
}
