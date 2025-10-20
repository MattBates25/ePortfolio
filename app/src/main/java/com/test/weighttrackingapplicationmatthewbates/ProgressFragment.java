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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * A fragment that displays a grid of the user's weight history.
 * It allows users to add new weight entries, delete existing ones, and sort the displayed entries.
 */
public class ProgressFragment extends Fragment {

    private RecyclerView recyclerView;
    private WeightAdapter weightAdapter;
    private List<ProgressItem> weightItemList;
    private DatabaseHelper databaseHelper;
    private Spinner sortSpinner;

    /**
     * Inflates the layout, initializes all UI components including the RecyclerView and Spinner,
     * and sets up listeners for user actions.
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

        // Setup Sort Spinner
        sortSpinner = view.findViewById(R.id.sort_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadWeightData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        loadWeightData(SortUtils.DATE_NEWEST); // Initial load

        Button addWeightButton = view.findViewById(R.id.add_weight_button);
        addWeightButton.setOnClickListener(v -> addWeight());

        Button deleteWeightButton = view.findViewById(R.id.delete_weight_button);
        deleteWeightButton.setOnClickListener(v -> deleteSelectedWeights());

        return view;
    }

    /**
     * Loads weight entries from the database based on the selected sort option.
     * For standard sorting, it relies on the database's ORDER BY clause.
     * For custom sorting, it fetches unsorted data and sorts it in Java.
     * @param sortOption The selected sort option constant from SortUtils.
     */
    private void loadWeightData(int sortOption) {
        weightItemList.clear();
        weightAdapter.clearSelection();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(AppConstants.KEY_USER_ID, -1);

        if (userId != -1) {
            if (sortOption == SortUtils.DISTANCE_FROM_GOAL) {
                // Handle custom sort in Java
                sortEntriesByDistanceFromGoal(userId);
            } else {
                // Handle standard sorts using the database
                weightItemList.addAll(databaseHelper.getAllWeightEntries(userId, sortOption));
            }
            weightAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A custom sorting implementation as described in the enhancement plan.
     * It fetches the user's goal, calculates the absolute distance for each entry, and sorts the list.
     * @param userId The ID of the current user.
     */
    private void sortEntriesByDistanceFromGoal(int userId) {
        Float goalWeight = databaseHelper.getWeightGoal(userId);
        if (goalWeight == null) {
            // If no goal is set, sorting by distance is not possible. Default to standard sort.
            weightItemList.addAll(databaseHelper.getAllWeightEntries(userId, SortUtils.DATE_NEWEST));
            Toast.makeText(getContext(), getString(R.string.error_no_goal_for_sort), Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch unsorted data to sort in Java
        List<ProgressItem> unsortedList = databaseHelper.getAllWeightEntries(userId, SortUtils.DATE_NEWEST); // or any default

        unsortedList.sort(new Comparator<ProgressItem>() {
            @Override
            public int compare(ProgressItem p1, ProgressItem p2) {
                float dist1 = Math.abs(p1.getWeight() - goalWeight);
                float dist2 = Math.abs(p2.getWeight() - goalWeight);
                return Float.compare(dist1, dist2); // Ascending order
            }
        });

        weightItemList.addAll(unsortedList);
    }


    /**
     * Displays a dialog for the user to add a new weight entry.
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

                // Reload data with the current sort option
                loadWeightData(sortSpinner.getSelectedItemPosition());
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * Deletes all weight entries that have been selected by the user in the adapter.
     */
    private void deleteSelectedWeights() {
        List<Integer> selectedIds = weightAdapter.getSelectedItemIds();
        if (!selectedIds.isEmpty()) {
            databaseHelper.deleteSelectedWeightsById(selectedIds);
            // Reload data with the current sort option
            loadWeightData(sortSpinner.getSelectedItemPosition());
        }
    }
}
