package com.example.meditrackapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FirebaseMedicineHelper {
    private static final String TAG = "FirebaseMedicineHelper";
    private static final String MEDICINES_PATH = "medicines";
    
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;
    
    public FirebaseMedicineHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com");
        databaseRef = database.getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }
    
    public interface OnMedicineAddedListener {
        void onMedicineAdded(boolean success, String message);
    }
    
    public interface OnMedicinesLoadedListener {
        void onMedicinesLoaded(List<Medicine> medicines);
        void onError(String error);
    }
    
    public void addMedicine(Medicine medicine, OnMedicineAddedListener listener) {
        if (currentUser == null) {
            listener.onMedicineAdded(false, "User not authenticated");
            return;
        }
        
        String userId = currentUser.getUid();
        String medicineId = databaseRef.child(MEDICINES_PATH).child(userId).push().getKey();
        medicine.setId(medicineId);
        
        Map<String, Object> medicineValues = new HashMap<>();
        medicineValues.put("id", medicine.getId());
        medicineValues.put("name", medicine.getName());
        medicineValues.put("dosage", medicine.getDosage());
        medicineValues.put("startDate", medicine.getStartDate());
        medicineValues.put("endDate", medicine.getEndDate());
        medicineValues.put("medicineType", medicine.getMedicineType());
        medicineValues.put("frequency", medicine.getFrequency());
        medicineValues.put("customDays", medicine.getCustomDays());
        medicineValues.put("reminderTimes", medicine.getReminderTimes());
        medicineValues.put("reminderEnabled", medicine.isReminderEnabled());
        medicineValues.put("taken", medicine.isTaken());
        medicineValues.put("userId", medicine.getUserId());
        medicineValues.put("userEmail", medicine.getUserEmail());
        medicineValues.put("userName", medicine.getUserName());
        medicineValues.put("whenToTake", medicine.getWhenToTake());
        
        Log.d(TAG, "Saving medicine to Firebase: " + medicine.getName());
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "Medicine ID: " + medicineId);
        
        databaseRef.child(MEDICINES_PATH)
                .child(userId)
                .child(medicineId)
                .setValue(medicineValues)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Medicine added successfully to Firebase");
                        listener.onMedicineAdded(true, "Medicine added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to add medicine to Firebase: " + e.getMessage());
                        listener.onMedicineAdded(false, "Failed to add medicine: " + e.getMessage());
                    }
                });
    }
    
    public void getAllMedicines(OnMedicinesLoadedListener listener) {
        if (currentUser == null) {
            listener.onError("User not authenticated");
            return;
        }
        
        String userId = currentUser.getUid();
        databaseRef.child(MEDICINES_PATH)
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Medicine> medicineList = new ArrayList<>();
                        
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                String id = snapshot.child("id").getValue(String.class);
                                String name = snapshot.child("name").getValue(String.class);
                                String dosage = snapshot.child("dosage").getValue(String.class);
                                String startDate = snapshot.child("startDate").getValue(String.class);
                                String endDate = snapshot.child("endDate").getValue(String.class);
                                String frequency = snapshot.child("frequency").getValue(String.class);
                                String medicineType = snapshot.child("medicineType").getValue(String.class);
                                String whenToTake = snapshot.child("whenToTake").getValue(String.class);
                                
                                // Handle customDays as a simple list
                                ArrayList<String> customDays = new ArrayList<>();
                                if (snapshot.hasChild("customDays")) {
                                    Object customDaysObj = snapshot.child("customDays").getValue();
                                    if (customDaysObj instanceof Map) {
                                        // If it's stored as a map
                                        Map<String, Object> customDaysMap = (Map<String, Object>) customDaysObj;
                                        customDays.addAll(customDaysMap.values().stream()
                                                .map(Object::toString)
                                                .collect(Collectors.toList()));
                                    } else if (customDaysObj instanceof List) {
                                        // If it's stored as a list
                                        customDays.addAll((List<String>) customDaysObj);
                                    }
                                }
                                
                                // Handle reminderTimes as a simple list
                                ArrayList<String> reminderTimes = new ArrayList<>();
                                if (snapshot.hasChild("reminderTimes")) {
                                    Object reminderTimesObj = snapshot.child("reminderTimes").getValue();
                                    if (reminderTimesObj instanceof Map) {
                                        Map<String, Object> reminderTimesMap = (Map<String, Object>) reminderTimesObj;
                                        reminderTimes.addAll(reminderTimesMap.values().stream()
                                                .map(Object::toString)
                                                .collect(Collectors.toList()));
                                    } else if (reminderTimesObj instanceof List) {
                                        reminderTimes.addAll((List<String>) reminderTimesObj);
                                    }
                                }
                                boolean reminderEnabled = snapshot.child("reminderEnabled").getValue(Boolean.class);
                                boolean taken = snapshot.child("taken").getValue(Boolean.class);
                                String userId = snapshot.child("userId").getValue(String.class);
                                String userEmail = snapshot.child("userEmail").getValue(String.class);
                                String userName = snapshot.child("userName").getValue(String.class);
                                
                                Medicine medicine = new Medicine();
                                medicine.setId(id);
                                medicine.setName(name);
                                medicine.setDosage(dosage);
                                medicine.setStartDate(startDate);
                                medicine.setEndDate(endDate);
                                medicine.setFrequency(frequency != null ? frequency : "Once a day");
                                medicine.setMedicineType(medicineType != null ? medicineType : "Unknown");
                                medicine.setUserId(userId);
                                medicine.setUserEmail(userEmail);
                                medicine.setUserName(userName);
                                medicine.setCustomDays(customDays);
                                medicine.setReminderTimes(reminderTimes);
                                medicine.setReminderEnabled(reminderEnabled);
                                medicine.setTaken(taken);
                                medicine.setWhenToTake(whenToTake != null ? whenToTake : "");
                                
                                medicineList.add(medicine);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing medicine data", e);
                            }
                        }
                        
                        listener.onMedicinesLoaded(medicineList);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onError("Failed to load medicines: " + databaseError.getMessage());
                    }
                });
    }
    
    public void updateMedicine(Medicine medicine, OnMedicineAddedListener listener) {
        if (currentUser == null) {
            listener.onMedicineAdded(false, "User not authenticated");
            return;
        }
        
        String userId = currentUser.getUid();
        String medicineId = medicine.getId();
        
        Map<String, Object> medicineValues = new HashMap<>();
        medicineValues.put("id", medicine.getId());
        medicineValues.put("name", medicine.getName());
        medicineValues.put("dosage", medicine.getDosage());
        medicineValues.put("startDate", medicine.getStartDate());
        medicineValues.put("endDate", medicine.getEndDate());
        medicineValues.put("medicineType", medicine.getMedicineType());
        medicineValues.put("frequency", medicine.getFrequency());
        medicineValues.put("customDays", medicine.getCustomDays());
        medicineValues.put("reminderTimes", medicine.getReminderTimes());
        medicineValues.put("reminderEnabled", medicine.isReminderEnabled());
        medicineValues.put("taken", medicine.isTaken());
        medicineValues.put("userId", medicine.getUserId());
        medicineValues.put("userEmail", medicine.getUserEmail());
        medicineValues.put("userName", medicine.getUserName());
        
        databaseRef.child(MEDICINES_PATH)
                .child(userId)
                .child(medicineId)
                .updateChildren(medicineValues)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onMedicineAdded(true, "Medicine updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onMedicineAdded(false, "Failed to update medicine: " + e.getMessage());
                    }
                });
    }
    
    public void deleteMedicine(String medicineId, OnMedicineAddedListener listener) {
        if (currentUser == null) {
            listener.onMedicineAdded(false, "User not authenticated");
            return;
        }
        
        String userId = currentUser.getUid();
        
        databaseRef.child(MEDICINES_PATH)
                .child(userId)
                .child(medicineId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onMedicineAdded(true, "Medicine deleted successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onMedicineAdded(false, "Failed to delete medicine: " + e.getMessage());
                    }
                });
    }
}