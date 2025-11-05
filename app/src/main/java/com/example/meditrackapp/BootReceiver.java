package com.example.meditrackapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


//BroadcastReceiver that runs after device boot/reboot.
//Reschedules all medicine alarms to ensure reminders continue working.

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String action = intent.getAction();
        Log.d(TAG, "BootReceiver triggered with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Log.d(TAG, "Device booted - rescheduling medicine alarms");
            rescheduleAllMedicineAlarms(context);
        }
    }


 // Reschedule all medicine alarms after boot.

    private void rescheduleAllMedicineAlarms(Context context) {
        try {
            // Check if user is authenticated
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.d(TAG, "No authenticated user, skipping alarm rescheduling");
                return;
            }

            // Load medicines from Firebase and reschedule
            FirebaseMedicineHelper firebaseHelper = new FirebaseMedicineHelper();
            firebaseHelper.getAllMedicines(new FirebaseMedicineHelper.OnMedicinesLoadedListener() {
                @Override
                public void onMedicinesLoaded(List<Medicine> medicines) {
                    if (medicines == null || medicines.isEmpty()) {
                        Log.d(TAG, "No medicines found to reschedule");
                        return;
                    }

                    // Filter medicines for current user
                    MedicineAlarmScheduler alarmScheduler = new MedicineAlarmScheduler(context);
                    int count = 0;
                    
                    for (Medicine medicine : medicines) {
                        if (currentUser.getUid().equals(medicine.getUserId())) {
                            alarmScheduler.scheduleMedicineAlarms(medicine);
                            count++;
                        }
                    }
                    
                    Log.d(TAG, "Rescheduled alarms for " + count + " medicines");
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading medicines: " + error);
                    // Try loading from local storage as fallback
                    try {
                        List<Medicine> localMedicines = MedicineRepository.getAll(context);
                        if (localMedicines != null && !localMedicines.isEmpty()) {
                            MedicineAlarmScheduler alarmScheduler = new MedicineAlarmScheduler(context);
                            alarmScheduler.rescheduleAllMedicines(localMedicines);
                            Log.d(TAG, "Rescheduled alarms from local storage");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error rescheduling from local storage", e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in rescheduleAllMedicineAlarms", e);
        }
    }
}
