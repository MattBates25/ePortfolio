package com.test.weighttrackingapplicationmatthewbates;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;

/**
 * Manages the application's local database for storing user and weight data.
 * Handles table creation, upgrades, and provides CRUD (Create, Read, Update, Delete) operations.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // --- Database Constants ---
    private static final String DATABASE_NAME = "weightTracker.db";
    private static final int DATABASE_VERSION = 1;

    // --- User Table Columns ---
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_WEIGHT_GOAL = "weight_goal"; // Note: This is in the users table

    // --- Weight Table Columns ---
    private static final String TABLE_WEIGHTS = "weights";
    private static final String COLUMN_WEIGHT_ID = "weight_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_WEIGHT = "weight";
    // COLUMN_USER_ID is also used here as a foreign key

    /**
     * Constructor for the DatabaseHelper.
     * @param context The application context.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * This is where the creation of tables and the initial population of the tables should happen.
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create the users table
        String createUserTable = "CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_WEIGHT_GOAL + " REAL, " +
                COLUMN_PHONE_NUMBER + " TEXT" +
                ")";

        // SQL statement to create the weights table with a foreign key to the users table
        String createWeightTable = "CREATE TABLE " + TABLE_WEIGHTS + "(" +
                COLUMN_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_WEIGHT + " REAL, " +
                COLUMN_USER_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                ")";
        
        db.execSQL(createUserTable);
        db.execSQL(createWeightTable);

        // Create Indexes for faster lookups on frequently queried columns
        String userIndex = "CREATE INDEX idx_user_id ON " + TABLE_WEIGHTS + "(" + COLUMN_USER_ID + ")";
        String dateIndex = "CREATE INDEX idx_date ON " + TABLE_WEIGHTS + "(" + COLUMN_DATE + ")";
        db.execSQL(userIndex);
        db.execSQL(dateIndex);
    }

    /**
     * Called when the database needs to be upgraded.
     * This method will drop the existing tables and recreate them.
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        onCreate(db);
    }

    /**
     * Called when the database has been opened.
     * Enables foreign key constraints on the database connection.
     * @param db The database.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // --- User Management Methods ---

    /**
     * Adds a new user to the database.
     * @param username The user's chosen username.
     * @param password The user's chosen password.
     * @return true if the user was added successfully, false otherwise.
     */
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Retrieves the user ID for a given username and password combination.
     * @param username The username to check.
     * @param password The password to check.
     * @return The integer user ID if credentials are valid, null otherwise.
     */
    public Integer getUserId(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);

        Integer userId = null;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }
        cursor.close();
        db.close();
        return userId;
    }

    // --- Weight Entry Management Methods ---

    /**
     * Adds a new weight entry for a specific user.
     * @param date The date of the weight entry (e.g., "YYYY-MM-DD").
     * @param weight The weight value.
     * @param userId The ID of the user this entry belongs to.
     * @return true if the entry was added successfully, false otherwise.
     */
    public boolean addWeight(String date, float weight, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_USER_ID, userId);

        long result = db.insert(TABLE_WEIGHTS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Gets the most recent weight entry for a specific user.
     * @param userId The ID of the user to query.
     * @return The most recent weight as a Float, or null if no entries exist.
     */
    public Float getMostRecentWeight(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS,
                new String[]{COLUMN_WEIGHT},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_DATE + " DESC", // Order by date descending
                "1"); // Limit to the most recent entry

        Float recentWeight = null;
        if (cursor.moveToFirst()) {
            recentWeight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
        }
        cursor.close();
        db.close();
        return recentWeight;
    }

    /**
     * Retrieves all weight entries for a specific user from the database, with a specified sort order.
     * @param userId The ID of the user whose entries to fetch.
     * @param sortOption A constant from SortUtils defining the sort order.
     * @return A List of ProgressItem objects representing all weight entries.
     */
    public List<ProgressItem> getAllWeightEntries(int userId, int sortOption) {
        List<ProgressItem> weightEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String orderByClause;
        switch (sortOption) {
            case SortUtils.DATE_OLDEST:
                orderByClause = COLUMN_DATE + " ASC";
                break;
            case SortUtils.WEIGHT_HIGHEST:
                orderByClause = COLUMN_WEIGHT + " DESC";
                break;
            case SortUtils.WEIGHT_LOWEST:
                orderByClause = COLUMN_WEIGHT + " ASC";
                break;
            case SortUtils.DATE_NEWEST:
            default:
                orderByClause = COLUMN_DATE + " DESC";
                break;
        }

        Cursor cursor = db.query(TABLE_WEIGHTS,
                new String[]{COLUMN_WEIGHT_ID, COLUMN_DATE, COLUMN_WEIGHT},
                COLUMN_USER_ID + "=?", // Filter by user ID
                new String[]{String.valueOf(userId)}, // Pass userId here
                null, null, orderByClause);

        if (cursor.moveToFirst()) {
            do {
                int weightId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
                weightEntries.add(new ProgressItem(weightId, date, weight));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return weightEntries;
    }

    /**
     * Deletes a selection of weight entries based on their unique IDs.
     * @param ids A List of integer weight IDs to be deleted.
     */
    public void deleteSelectedWeightsById(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();

        // Build the "IN (?,?,?)" part of the clause to handle a variable number of arguments
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) {
                placeholders.append(",");
            }
        }

        // Convert the Integer list to a String array for the query arguments
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            args[i] = String.valueOf(ids.get(i));
        }

        String whereClause = COLUMN_WEIGHT_ID + " IN (" + placeholders + ")";
        db.delete(TABLE_WEIGHTS, whereClause, args);
        db.close();
    }


    // --- User Goal and Profile Methods ---

    /**
     * Updates the weight goal for a specific user.
     * @param userId The ID of the user to update.
     * @param newGoal The new weight goal value.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateWeightGoal(int userId, float newGoal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT_GOAL, newGoal);

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Updates the phone number for a specific user.
     * @param userId The ID of the user to update.
     * @param phoneNumber The new phone number.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updatePhoneNumber(int userId, String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHONE_NUMBER, phoneNumber);

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Retrieves the phone number for a specific user.
     * @param userId The ID of the user whose phone number to fetch.
     * @return The user's phone number as a String, or null if not set.
     */
    public String getUserPhoneNumber(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_PHONE_NUMBER},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);

        String phoneNumber = null;
        if (cursor.moveToFirst()) {
            phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER));
        }
        cursor.close();
        db.close();
        return phoneNumber;
    }

    /**
     * Retrieves the weight goal for a specific user.
     * @param userId The ID of the user whose weight goal to fetch.
     * @return The user's weight goal as a Float, or null if not set.
     */
    public Float getWeightGoal(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_WEIGHT_GOAL},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);

        Float weightGoal = null;
        if (cursor.moveToFirst()) {
            // Check if the goal is null in the database before trying to get it as a float
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_GOAL))) {
                weightGoal = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_GOAL));
            }
        }
        cursor.close();
        db.close();
        return weightGoal;
    }

}
