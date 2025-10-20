package com.test.weighttrackingapplicationmatthewbates;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.view.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * The main and only Activity for the application.
 * It hosts the NavHostFragment to manage all fragment navigation and handles
 * the BottomNavigationView. It is also responsible for requesting and handling
 * runtime permissions like SEND_SMS.
 */
public class MainActivity extends AppCompatActivity {

    // A constant code used to identify the SMS permission request.
    private static final int SMS_PERMISSION_CODE = 100;
    private BottomNavigationView bottomNavigationView;

    /**
     * Called when the activity is first created. This is where the layout is inflated
     * and all initial setup for navigation and permissions is performed.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up the NavController and link it to the BottomNavigationView and ActionBar.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_progress, R.id.navigation_goal)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Add a listener to show or hide the bottom navigation bar based on the current fragment.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_login) {
                hideBottomNavigation();
            } else {
                showBottomNavigation();
            }
        });

        // Check for SMS permission on startup.
        checkSmsPermission();
    }

    /**
     * Makes the bottom navigation bar visible.
     */
    public void showBottomNavigation() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the bottom navigation bar.
     */
    public void hideBottomNavigation() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    /**
     * Checks if the app has been granted SEND_SMS permission.
     * If not, it will request the permission from the user.
     */
    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    /**
     * Callback for the result from requesting permissions.
     * This method is invoked for every call on requestPermissions().
     * @param requestCode The request code passed in requestPermissions().
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sends an SMS message to the given phone number.
     * It first checks for permission and sanitizes the phone number before sending.
     * @param phoneNumber The destination phone number.
     * @param message The body of the SMS message.
     */
    public void sendSMSNotification(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                // Sanitize the phone number to be only digits to improve reliability.
                String sanitizedPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(sanitizedPhoneNumber, null, message, null, null);
                Toast.makeText(this, "Goal reached SMS sent to " + sanitizedPhoneNumber, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // Catches any error during sending, e.g., invalid number format after sanitizing
                Toast.makeText(this, "SMS failed to send. Please check the phone number format.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

}
