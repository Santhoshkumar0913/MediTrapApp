package com.example.meditrackapp;

public class MedicineStatus {
    private String medicineName;
    private String dosage;
    private String time;
    private String status;
    private long timestamp;

    // Required empty constructor for Firebase
    public MedicineStatus() {
    }

    public MedicineStatus(String medicineName, String dosage, String time, String status, long timestamp) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.time = time;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}