package com.test.weighttrackingapplicationmatthewbates;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weightTracker.db";
    private static final int DATABASE_VERSION = 1;

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";

    // Weight table
    private static final String TABLE_WEIGHTS = "weights";
    private static final String COLUMN_WEIGHT_ID = "weight_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_WEIGHT_GOAL = "weight_goal";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_WEIGHT_GOAL + " REAL, " +
                COLUMN_PHONE_NUMBER + " TEXT" +
                ")";
        db.execSQL(createUserTable);

        String createWeightTable = "CREATE TABLE " + TABLE_WEIGHTS + "(" +
                COLUMN_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_WEIGHT + " REAL, " +
                COLUMN_USER_ID + " INTEGER, " +  // Add this line to associate weights with users
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                ")";
        db.execSQL(createWeightTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        onCreate(db);
    }

    // Methods for User CRUD operations
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);

        boolean userExists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return userExists;
    }

    //Method to retrieve the user ID
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

    // Method to add a weight entry
    public boolean addWeight(String date, float weight, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_USER_ID, userId);  // Associate weight with user

        long result = db.insert(TABLE_WEIGHTS, null, values);
        db.close();
        return result != -1;
    }

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

    // Method to retrieve all weight entries
    // Method to retrieve all weight entries for a specific user
    public List<ProgressItem> getAllWeightEntries(int userId) {
        List<ProgressItem> weightEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS,
                new String[]{COLUMN_DATE, COLUMN_WEIGHT},
                COLUMN_USER_ID + "=?", // Filter by user ID
                new String[]{String.valueOf(userId)}, // Pass userId here
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
                weightEntries.add(new ProgressItem(date, weight));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return weightEntries;
    }

    // Method to delete weight entries
    public void deleteSelectedWeights(List<String> dates) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_DATE + " IN (" + new String(new char[dates.size() - 1]).replace("\0", "?,") + "?)";
        db.delete(TABLE_WEIGHTS, whereClause, dates.toArray(new String[0]));
        db.close();
    }


    // Method to update a weight entry
    public boolean updateWeight(String date, float newWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT, newWeight);

        int rowsAffected = db.update(TABLE_WEIGHTS, values, COLUMN_DATE + "=?", new String[]{date});
        db.close();
        return rowsAffected > 0;
    }

    // Method to update the weight goal
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

    //Method to get weight goal
    public Float getWeightGoal(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_WEIGHT_GOAL},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);

        Float weightGoal = null;
        if (cursor.moveToFirst()) {
            weightGoal = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_GOAL));
        }
        cursor.close();
        db.close();
        return weightGoal;
    }

}
