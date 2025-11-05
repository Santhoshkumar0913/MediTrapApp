package com.example.meditrackapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * BroadcastReceiver that handles medicine reminder alarms.
 * This receiver triggers notifications regardless of which screen the user is on.
 */
public class MedicineAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "MedicineAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String medicineId = intent.getStringExtra("medicineId");
        String medicineName = intent.getStringExtra("medicineName");
        String dosage = intent.getStringExtra("dosage");
        String time = intent.getStringExtra("time");
        String medicineType = intent.getStringExtra("medicineType");

        Log.d(TAG, "Alarm received for medicine: " + medicineName + " at " + time);

        if (medicineId == null || medicineName == null || time == null) {
            Log.e(TAG, "Missing required data in alarm intent");
            return;
        }

        // Check if this dose has already been taken or skipped today
        String dateKey = getTodayDateString();
        String doseKey = "dose:" + medicineId + ":" + dateKey + ":" + time;
        SharedPreferences prefs = context.getSharedPreferences("dose_status", Context.MODE_PRIVATE);
        String status = prefs.getString(doseKey, "Next");

        if ("Taken".equals(status) || "Skipped".equals(status)) {
            Log.d(TAG, "Dose already " + status + ", skipping notification");
            return;
        }

        // Check if already fired to prevent duplicate notifications
        if (prefs.getBoolean("fired:" + doseKey, false)) {
            Log.d(TAG, "Alarm already fired for this dose, skipping");
            return;
        }

        // Create a Medicine object for the notification
        Medicine medicine = new Medicine();
        medicine.setId(medicineId);
        medicine.setName(medicineName);
        medicine.setDosage(dosage);
        medicine.setMedicineType(medicineType);

        // Show notification and play ringtone
        MedicineReminderService reminderService = new MedicineReminderService(context);
        reminderService.showMedicineReminder(medicine, time);

        // Mark this dose as fired
        prefs.edit().putBoolean("fired:" + doseKey, true).apply();

        Log.d(TAG, "Notification shown for medicine: " + medicineName);
        
        // IMPORTANT: Reschedule this alarm for tomorrow to ensure daily reminders
        rescheduleAlarmForTomorrow(context, medicine, time);
    }
    
    /**
     * Reschedule the alarm for the next day.
     * This ensures alarms repeat daily even with exact alarms.
     */
    private void rescheduleAlarmForTomorrow(Context context, Medicine medicine, String time) {
        try {
            MedicineAlarmScheduler scheduler = new MedicineAlarmScheduler(context);
            scheduler.scheduleMedicineAlarms(medicine);
            Log.d(TAG, "Rescheduled alarm for tomorrow: " + medicine.getName() + " at " + time);
        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling alarm for tomorrow", e);
        }
    }

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date());
    }
}
