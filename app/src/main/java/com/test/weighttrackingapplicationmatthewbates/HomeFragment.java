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

/**
 * The main dashboard fragment displayed after a user logs in.
 * It provides a quick summary of the user's most recent weight and their current weight goal.
 */
public class HomeFragment extends Fragment {

    private TextView recentWeightTextView;
    private TextView goalTextView;
    private DatabaseHelper dbHelper;
    private int userId;

    /**
     * Inflates the layout for this fragment, initializes UI components, and loads user data.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recentWeightTextView = view.findViewById(R.id.recent_weight_text_view);
        goalTextView = view.findViewById(R.id.goal_text_view);
        dbHelper = new DatabaseHelper(requireContext());

        // Retrieve user ID from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt(AppConstants.KEY_USER_ID, -1);

        // Check if userId is valid
        if (userId != -1) {
            loadUserData();
        } else {
            // Handle case where user is not logged in
            Toast.makeText(getContext(), getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /**
     * Fetches the user's most recent weight and weight goal from the database
     * and updates the TextViews to display the information.
     */
    private void loadUserData() {
        Float recentWeight = dbHelper.getMostRecentWeight(userId);
        Float weightGoal = dbHelper.getWeightGoal(userId);

        if (recentWeight != null) {
            recentWeightTextView.setText(getString(R.string.home_recent_weight_label, recentWeight));
        } else {
            recentWeightTextView.setText(getString(R.string.home_no_weight_data));
        }

        if (weightGoal != null) {
            goalTextView.setText(getString(R.string.home_goal_label, weightGoal));
        } else {
            goalTextView.setText(getString(R.string.home_no_goal_set));
        }
    }
}
