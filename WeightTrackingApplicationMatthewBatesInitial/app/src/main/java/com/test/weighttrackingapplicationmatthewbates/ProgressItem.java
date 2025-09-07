package com.test.weighttrackingapplicationmatthewbates;

public class ProgressItem {
    private String date;
    private float weight;

    public ProgressItem(String date, float weight) {
        this.date = date;
        this.weight = weight;
    }

    public String getDate() {
        return date;
    }

    public float getWeight() {
        return weight;
    }
}
