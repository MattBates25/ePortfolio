package com.test.weighttrackingapplicationmatthewbates;

/**
 * A class to hold application-wide constants to avoid using "magic strings".
 */
public final class AppConstants {

    // Private constructor to prevent instantiation of this utility class
    private AppConstants() {}

    // SharedPreferences file name
    public static final String PREFS_NAME = "UserPrefs";

    // Key for storing the logged-in user's ID
    public static final String KEY_USER_ID = "USER_ID";
}
