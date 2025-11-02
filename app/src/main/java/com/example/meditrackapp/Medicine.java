package com.example.meditrackapp;

import java.util.ArrayList;
import java.util.List;

public class Medicine {
    private String id;
    private String name;
    private String dosage;
    private String startDate;
    private String endDate;
    private String medicineType;
    private String frequency;
    private List<String> customDays;
    private List<String> reminderTimes;
    private boolean reminderEnabled;
    private boolean taken;
    private String userId;
    private String userEmail;
    private String userName;
    private String whenToTake; // Before Meal / After Meal
    private String status = "Next"; // Default status is "Next"

    // Default constructor for Firebase
    public Medicine() {
        this.customDays = new ArrayList<>();
        this.reminderTimes = new ArrayList<>();
        this.reminderEnabled = true;
        this.taken = false;
        this.frequency = "Once a day";
        this.medicineType = "Unknown";
        this.whenToTake = "";
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
        this.frequency = frequency;
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

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getMedicineType() { return medicineType; }
    public void setMedicineType(String medicineType) { this.medicineType = medicineType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getCustomDays() { return customDays; }
    public void setCustomDays(List<String> customDays) { this.customDays = customDays; }

    public List<String> getReminderTimes() { return reminderTimes; }
    public void setReminderTimes(List<String> reminderTimes) { this.reminderTimes = reminderTimes; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public boolean isTaken() { return taken; }
    public void setTaken(boolean taken) { this.taken = taken; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getWhenToTake() { return whenToTake; }
    public void setWhenToTake(String whenToTake) { this.whenToTake = whenToTake; }
}