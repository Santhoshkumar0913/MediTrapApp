package com.example.meditrackapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


//BroadcastReceiver that runs after device boot/reboot.
//Reschedules all medicine alarms to ensure reminders continue working.

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            rescheduleAllMedicineAlarms(context);
        }
    }


 // Reschedule all medicine alarms after boot.

    private void rescheduleAllMedicineAlarms(Context context) {
        try {
            // Check if user is authenticated
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                return;
            }

            // Load medicines from Firebase and reschedule
            FirebaseMedicineHelper firebaseHelper = new FirebaseMedicineHelper();
            firebaseHelper.getAllMedicines(new FirebaseMedicineHelper.OnMedicinesLoadedListener() {
                @Override
                public void onMedicinesLoaded(List<Medicine> medicines) {
                    if (medicines == null || medicines.isEmpty()) {
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
                }

                @Override
                public void onError(String error) {
                    // Try loading from local storage as fallback
                    try {
                        List<Medicine> localMedicines = MedicineRepository.getAll(context);
                        if (localMedicines != null && !localMedicines.isEmpty()) {
                            MedicineAlarmScheduler alarmScheduler = new MedicineAlarmScheduler(context);
                            alarmScheduler.rescheduleAllMedicines(localMedicines);
                        }
                    } catch (Exception e) {
                        // Error rescheduling from local storage
                    }
                }
            });
        } catch (Exception e) {

        }
    }
}
