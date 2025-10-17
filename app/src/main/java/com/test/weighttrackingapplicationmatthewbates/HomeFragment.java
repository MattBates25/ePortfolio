package com.test.weighttrackingapplicationmatthewbates;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private TextView recentWeightTextView;
    private TextView goalTextView;
    private DatabaseHelper dbHelper;
    private int userId;  // User ID will be retrieved from SharedPreferences

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recentWeightTextView = view.findViewById(R.id.recent_weight_text_view);
        goalTextView = view.findViewById(R.id.goal_text_view);
        dbHelper = new DatabaseHelper(requireContext());

        // Retrieve user ID from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("USER_ID", -1);  // Use -1 as default if not found

        // Check if userId is valid
        if (userId != -1) {
            loadUserData();
        } else {
            // Handle case where user is not logged in (e.g., navigate to LoginFragment)
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            // Optionally navigate back to login
        }

        return view;
    }

    private void loadUserData() {
        Float recentWeight = dbHelper.getMostRecentWeight(userId);
        Float weightGoal = dbHelper.getWeightGoal(userId);

        recentWeightTextView.setText(recentWeight != null ? "Most Recent Weight: " + recentWeight : "No data");
        goalTextView.setText(weightGoal != null ? "Goal: " + weightGoal : "No goal set");
    }
}
