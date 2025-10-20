package com.test.weighttrackingapplicationmatthewbates;

import android.app.DatePickerDialog;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A fragment that displays a grid of the user's weight history.
 * It allows users to add new weight entries and delete existing ones.
 * It also triggers SMS notifications when a user's weight meets their goal.
 */
public class ProgressFragment extends Fragment {

    private RecyclerView recyclerView;
    private WeightAdapter weightAdapter;
    private List<ProgressItem> weightItemList;
    private DatabaseHelper databaseHelper;

    /**
     * Inflates the layout for this fragment, initializes the RecyclerView, adapter, database helper,
     * and sets up click listeners for the buttons.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        databaseHelper = new DatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.weight_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        weightItemList = new ArrayList<>();
        weightAdapter = new WeightAdapter(weightItemList);
        recyclerView.setAdapter(weightAdapter);

        loadWeightData();

        Button addWeightButton = view.findViewById(R.id.add_weight_button);
        addWeightButton.setOnClickListener(v -> addWeight());

        Button deleteWeightButton = view.findViewById(R.id.delete_weight_button);
        deleteWeightButton.setOnClickListener(v -> deleteSelectedWeights());

        return view;
    }

    /**
     * Loads all weight entries for the current user from the database and updates the RecyclerView.
     */
    private void loadWeightData() {
        weightItemList.clear();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(AppConstants.KEY_USER_ID, -1);

        if (userId != -1) {
            weightItemList.addAll(databaseHelper.getAllWeightEntries(userId));
            weightAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a dialog for the user to add a new weight entry.
     * The dialog uses a DatePickerDialog for reliable date input and validates the weight input.
     * On success, it saves the entry to the database and checks if the user's goal has been met.
     */
    private void addWeight() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_weight, null);
        builder.setView(dialogView);

        final EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        final EditText editTextWeight = dialogView.findViewById(R.id.editTextWeight);
        Button buttonAddWeight = dialogView.findViewById(R.id.buttonAddWeight);

        editTextDate.setFocusable(false);
        editTextDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editTextDate.setText(formattedDate);
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        buttonAddWeight.setOnClickListener(v -> {
            String date = editTextDate.getText().toString();
            String weightStr = editTextWeight.getText().toString().trim();

            if (date.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.error_date_required), Toast.LENGTH_SHORT).show();
                return;
            }
            if (weightStr.isEmpty()) {
                editTextWeight.setError(getString(R.string.error_weight_required));
                return;
            }

            float weight;
            try {
                weight = Float.parseFloat(weightStr);
                if (weight <= 0) {
                    editTextWeight.setError(getString(R.string.error_goal_positive));
                    return;
                }
            } catch (NumberFormatException e) {
                editTextWeight.setError(getString(R.string.error_valid_number));
                return;
            }

            SharedPreferences sharedPreferences = getContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
            int userId = sharedPreferences.getInt(AppConstants.KEY_USER_ID, -1);

            if (userId != -1) {
                databaseHelper.addWeight(date, weight, userId);

                Float weightGoal = databaseHelper.getWeightGoal(userId);
                if (weightGoal != null && weight < weightGoal) {
                    String phoneNumber = databaseHelper.getUserPhoneNumber(userId);
                    String message = getString(R.string.goal_reached_sms_message);

                    MainActivity mainActivity = (MainActivity) getActivity();
                    if (mainActivity != null && phoneNumber != null && !phoneNumber.isEmpty()) {
                        mainActivity.sendSMSNotification(phoneNumber, message);
                    }
                }

                loadWeightData();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * Deletes all weight entries that have been selected by the user in the adapter.
     * It retrieves a list of unique IDs from the adapter and passes them to the database for deletion.
     */
    private void deleteSelectedWeights() {
        List<Integer> selectedIds = weightAdapter.getSelectedItemIds();
        if (!selectedIds.isEmpty()) {
            databaseHelper.deleteSelectedWeightsById(selectedIds);
            loadWeightData();
        }
    }
}
