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

public class FirebaseMedicineHelper {
    private static final String TAG = "FirebaseMedicineHelper";
    private static final String MEDICINES_PATH = "medicines";
    
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;
    
    public FirebaseMedicineHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
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
        medicineValues.put("customDays", medicine.getCustomDays());
        medicineValues.put("reminderTimes", medicine.getReminderTimes());
        medicineValues.put("reminderEnabled", medicine.isReminderEnabled());
        medicineValues.put("taken", medicine.isTaken());
        
        databaseRef.child(MEDICINES_PATH)
                .child(userId)
                .child(medicineId)
                .setValue(medicineValues)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onMedicineAdded(true, "Medicine added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
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
                                ArrayList<String> customDays = new ArrayList<>();
                                if (snapshot.child("customDays").exists()) {
                                    for (DataSnapshot daySnapshot : snapshot.child("customDays").getChildren()) {
                                        customDays.add(daySnapshot.getValue(String.class));
                                    }
                                }
                                ArrayList<String> reminderTimes = new ArrayList<>();
                                if (snapshot.child("reminderTimes").exists()) {
                                    for (DataSnapshot timeSnapshot : snapshot.child("reminderTimes").getChildren()) {
                                        reminderTimes.add(timeSnapshot.getValue(String.class));
                                    }
                                }
                                boolean reminderEnabled = snapshot.child("reminderEnabled").getValue(Boolean.class);
                                boolean taken = snapshot.child("taken").getValue(Boolean.class);
                                
                                Medicine medicine = new Medicine();
                                medicine.setId(id);
                                medicine.setName(name);
                                medicine.setDosage(dosage);
                                medicine.setStartDate(startDate);
                                medicine.setEndDate(endDate);
                                medicine.setCustomDays(customDays);
                                medicine.setReminderTimes(reminderTimes);
                                medicine.setReminderEnabled(reminderEnabled);
                                medicine.setTaken(taken);
                                
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
        medicineValues.put("customDays", medicine.getCustomDays());
        medicineValues.put("reminderTimes", medicine.getReminderTimes());
        medicineValues.put("reminderEnabled", medicine.isReminderEnabled());
        medicineValues.put("taken", medicine.isTaken());
        
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