package com.test.weighttrackingapplicationmatthewbates;

/**
 * A simple data class (POJO) that represents a single weight entry.
 * This object holds the unique ID, date, and weight value for one record.
 */
public class ProgressItem {
    private final int weightId;
    private final String date;
    private final float weight;

    /**
     * Constructor for creating a new ProgressItem.
     * @param weightId The unique ID of the weight entry from the database.
     * @param date The date of the weight entry.
     * @param weight The weight value.
     */
    public ProgressItem(int weightId, String date, float weight) {
        this.weightId = weightId;
        this.date = date;
        this.weight = weight;
    }

    /**
     * Gets the unique database ID of the weight entry.
     * @return The integer weight ID.
     */
    public int getWeightId() {
        return weightId;
    }

    /**
     * Gets the date of the weight entry.
     * @return The date as a String.
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the weight value of the entry.
     * @return The weight as a float.
     */
    public float getWeight() {
        return weight;
    }
}
