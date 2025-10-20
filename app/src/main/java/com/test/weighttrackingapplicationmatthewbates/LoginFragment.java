package com.test.weighttrackingapplicationmatthewbates;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A Fragment that handles user authentication.
 * It provides UI for users to log in with existing credentials or register a new account.
 * It performs validation on user input and interacts with the DatabaseHelper to manage user data.
 */
public class LoginFragment extends Fragment {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private DatabaseHelper dbHelper;

    /**
     * Inflates the layout for this fragment, initializes UI components, and sets up click listeners.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize views
        usernameEditText = view.findViewById(R.id.username);
        passwordEditText = view.findViewById(R.id.password);
        loginButton = view.findViewById(R.id.login_button);
        registerButton = view.findViewById(R.id.register_button);
        dbHelper = new DatabaseHelper(getContext());

        // Set click listeners
        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());

        return view;
    }

    /**
     * Handles the user login process.
     * It validates user input, checks credentials against the database, and on successful login,
     * saves the user session and navigates to the home screen.
     */
    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty()) {
            usernameEditText.setError(getString(R.string.error_field_required));
            return;
        }
        if (password.isEmpty()) {
            passwordEditText.setError(getString(R.string.error_field_required));
            return;
        }

        // Check credentials and get user ID
        Integer userId = dbHelper.getUserId(username, password);
        if (userId != null) {
            Toast.makeText(getContext(), getString(R.string.login_successful), Toast.LENGTH_SHORT).show();

            // Save user ID in SharedPreferences for global access
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(AppConstants.KEY_USER_ID, userId);
            editor.apply();

            // Navigate using NavController
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.navigation_home);
        } else {
            Toast.makeText(getContext(), getString(R.string.login_invalid_credentials), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the new user registration process.
     * It performs strict validation on the username and password to ensure they meet security requirements.
     * On successful validation, it adds the new user to the database.
     */
    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Username validation
        if (username.isEmpty()) {
            usernameEditText.setError(getString(R.string.error_field_required));
            return;
        }
        if (username.length() < 4) {
            usernameEditText.setError(getString(R.string.error_username_length));
            return;
        }

        // Password validation
        if (password.isEmpty()) {
            passwordEditText.setError(getString(R.string.error_field_required));
            return;
        }
        if (password.length() < 8) {
            passwordEditText.setError(getString(R.string.error_password_length));
            return;
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            passwordEditText.setError(getString(R.string.error_password_letter));
            return;
        }
        if (!password.matches(".*[0-9].*")) {
            passwordEditText.setError(getString(R.string.error_password_number));
            return;
        }
        if (!password.matches(".*[@#$%^&+=!].*")) {
            passwordEditText.setError(getString(R.string.error_password_special));
            return;
        }

        // Attempt to add the user to the database
        boolean isAdded = dbHelper.addUser(username, password);
        if (isAdded) {
            Toast.makeText(getContext(), getString(R.string.registration_successful), Toast.LENGTH_LONG).show();
            usernameEditText.setText("");
            passwordEditText.setText("");
        } else {
            usernameEditText.setError(getString(R.string.registration_failed_username_exists));
        }
    }
}
