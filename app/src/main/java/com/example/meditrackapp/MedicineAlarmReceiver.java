package com.example.meditrackapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


//BroadcastReceiver that handles medicine reminder alarms.

public class MedicineAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String medicineId = intent.getStringExtra("medicineId");
        String medicineName = intent.getStringExtra("medicineName");
        String dosage = intent.getStringExtra("dosage");
        String time = intent.getStringExtra("time");
        String medicineType = intent.getStringExtra("medicineType");
        String customDaysStr = intent.getStringExtra("customDays");
        String medicineUserId = intent.getStringExtra("userId");

        if (medicineId == null || medicineName == null || time == null) {
            return;
        }

        // CRITICAL: Verify this medicine belongs to the currently logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // No user logged in, don't show reminder
            return;
        }
        
        if (medicineUserId != null && !currentUser.getUid().equals(medicineUserId)) {
            // This medicine belongs to a different user, don't show reminder
            return;
        }

        // Check if today is a scheduled day for this medicine
        if (!isTodayScheduled(customDaysStr)) {
            // Still reschedule for the next occurrence
            rescheduleAlarmForTomorrow(context, createMedicineObject(medicineId, medicineName, dosage, medicineType, customDaysStr, medicineUserId), time);
            return;
        }

        // Check if this dose has already been taken or skipped today
        String dateKey = getTodayDateString();
        String doseKey = "dose:" + medicineId + ":" + dateKey + ":" + time;
        SharedPreferences prefs = context.getSharedPreferences("dose_status", Context.MODE_PRIVATE);
        String status = prefs.getString(doseKey, "Next");

        if ("Taken".equals(status) || "Skipped".equals(status)) {
            return;
        }

        // Check if already fired to prevent duplicate notifications
        if (prefs.getBoolean("fired:" + doseKey, false)) {
            return;
        }

        // Create a Medicine object for the notification
        Medicine medicine = createMedicineObject(medicineId, medicineName, dosage, medicineType, customDaysStr, medicineUserId);

        // Show notification and play ringtone
        MedicineReminderService reminderService = new MedicineReminderService(context);
        reminderService.showMedicineReminder(medicine, time);

        // Mark this dose as fired
        prefs.edit().putBoolean("fired:" + doseKey, true).apply();
        
        // IMPORTANT: Reschedule this alarm for tomorrow to ensure daily reminders
        rescheduleAlarmForTomorrow(context, medicine, time);
    }
    


    //This ensures alarms repeat daily even with exact alarms.
    private void rescheduleAlarmForTomorrow(Context context, Medicine medicine, String time) {
        try {
            MedicineAlarmScheduler scheduler = new MedicineAlarmScheduler(context);
            scheduler.scheduleMedicineAlarms(medicine);
        } catch (Exception e) {
            // Error rescheduling alarm for tomorrow
        }
    }

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Helper method to create a Medicine object with customDays
     */
    private Medicine createMedicineObject(String medicineId, String medicineName, String dosage, String medicineType, String customDaysStr, String userId) {
        Medicine medicine = new Medicine();
        medicine.setId(medicineId);
        medicine.setName(medicineName);
        medicine.setDosage(dosage);
        medicine.setMedicineType(medicineType);
        medicine.setUserId(userId);
        
        // Restore customDays list from comma-separated string
        if (customDaysStr != null && !customDaysStr.isEmpty()) {
            List<String> customDays = Arrays.asList(customDaysStr.split(","));
            medicine.setCustomDays(customDays);
        }
        
        return medicine;
    }

    /**
     * Check if today is in the scheduled days for this medicine.
     * @param customDaysStr Comma-separated list of scheduled days (e.g., "Monday,Wednesday,Friday")
     * @return true if today is scheduled, false otherwise
     */
    private boolean isTodayScheduled(String customDaysStr) {
        // If no custom days specified, treat as daily (scheduled every day)
        if (customDaysStr == null || customDaysStr.isEmpty()) {
            return true;
        }

        // Get current day of week
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String todayFull = getDayStringFromCalendar(dayOfWeek); // e.g., "Monday"
        String todayShort = todayFull.length() >= 3 ? todayFull.substring(0, 3) : todayFull; // e.g., "Mon"

        // Parse scheduled days
        String[] scheduledDays = customDaysStr.split(",");
        for (String day : scheduledDays) {
            if (day == null) continue;
            String d = day.trim();
            
            // Check full name (e.g., "Monday")
            if (d.equalsIgnoreCase(todayFull)) {
                return true;
            }
            // Check short name (e.g., "Mon")
            if (d.equalsIgnoreCase(todayShort)) {
                return true;
            }
            // Check numeric format (1=Sunday, 2=Monday, etc.)
            if (d.matches("[1-7]")) {
                int scheduledDay = Integer.parseInt(d);
                if (scheduledDay == dayOfWeek) {
                    return true;
                }
            }
        }

        return false;
    }

    //Convert Calendar day of week to full day name.

    private String getDayStringFromCalendar(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            default: return "";
        }
    }
}
