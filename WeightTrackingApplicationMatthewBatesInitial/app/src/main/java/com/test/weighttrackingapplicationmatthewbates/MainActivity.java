package com.test.weighttrackingapplicationmatthewbates;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import android.view.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Set the main layout

        // Setup Bottom Navigation and NavController
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Add listener for destination changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Handle bottom navigation visibility
            if (destination.getId() == R.id.loginFragment) {
                bottomNavigationView.setVisibility(View.GONE); // Hide the bottom navigation
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE); // Show the bottom navigation

                // Update selected item based on the current destination
                if (destination.getId() == R.id.homeFragment) {
                    bottomNavigationView.setSelectedItemId(R.id.homeFragment);
                } else if (destination.getId() == R.id.progressFragment) {
                    bottomNavigationView.setSelectedItemId(R.id.progressFragment);
                } else if (destination.getId() == R.id.goalFragment) {
                    bottomNavigationView.setSelectedItemId(R.id.goalFragment);
                }
            }
        });

        // Set up bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homeFragment) {
                navController.navigate(R.id.homeFragment);
                return true;
            } else if (itemId == R.id.progressFragment) {
                navController.navigate(R.id.progressFragment);
                return true;
            } else if (itemId == R.id.goalFragment) {
                navController.navigate(R.id.goalFragment);
                return true;
            } else {
                return false;
            }
        });

        // Check for SMS permission
        checkSmsPermission();
    }

    // Method to check if SMS permission is granted
    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
        // else, permission already granted
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, continue app without SMS feature
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to send SMS notification (placeholder)
    public void sendSMSNotification(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            // Use SmsManager to send SMS
            Toast.makeText(this, "SMS sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        } else {
            // Permission not granted
            Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show();
        }
    }
}
