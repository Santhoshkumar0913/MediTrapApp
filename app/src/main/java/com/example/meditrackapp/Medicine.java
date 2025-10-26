package com.example.meditrackapp;

import java.util.ArrayList;
import java.util.List;

public class Medicine {
    private String id;
    private String name;
    private String dosage;
    private String startDate;
    private String endDate;
    private List<String> customDays;
    private List<String> reminderTimes;
    private boolean reminderEnabled;
    private boolean taken;

    // Default constructor for Firebase
    public Medicine() {
        this.customDays = new ArrayList<>();
        this.reminderTimes = new ArrayList<>();
        this.reminderEnabled = true;
        this.taken = false;
    }

    // Constructor for backward compatibility
    public Medicine(String name, String dosage, String time, String frequency, boolean taken) {
        this();
        this.name = name;
        this.dosage = dosage;
        this.startDate = "";
        this.endDate = "";
        this.reminderTimes.add(time);
        this.taken = taken;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public List<String> getCustomDays() { return customDays; }
    public void setCustomDays(List<String> customDays) { this.customDays = customDays; }

    public List<String> getReminderTimes() { return reminderTimes; }
    public void setReminderTimes(List<String> reminderTimes) { this.reminderTimes = reminderTimes; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public boolean isTaken() { return taken; }
    public void setTaken(boolean taken) { this.taken = taken; }
}