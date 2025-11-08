package com.example.meditrackapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

 //Uses AlarmManager to trigger notifications at scheduled times.

public class MedicineAlarmScheduler {
    private static final String TAG = "MedicineAlarmScheduler";
    private final Context context;
    private final AlarmManager alarmManager;

    public MedicineAlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }


     //Schedule alarms for all reminder times of a medicine.

    public void scheduleMedicineAlarms(Medicine medicine) {
        if (medicine == null || medicine.getReminderTimes() == null) return;
        
        // Check if reminders are enabled
        SharedPreferences prefs = context.getSharedPreferences("MediTrackPrefs", Context.MODE_PRIVATE);
        boolean reminderEnabled = prefs.getBoolean("reminder_enabled", true);
        
        if (!reminderEnabled) {
            Log.d(TAG, "Reminders are disabled. Skipping alarm scheduling for " + medicine.getName());
            return;
        }

        List<String> reminderTimes = medicine.getReminderTimes();
        for (String time : reminderTimes) {
            scheduleSingleAlarm(medicine, time);
        }
        Log.d(TAG, "Scheduled " + reminderTimes.size() + " alarms for " + medicine.getName());
    }


    //Schedule a single alarm for a specific medicine and time.

    private void scheduleSingleAlarm(Medicine medicine, String time) {
        try {
            // Parse the time
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date timeDate = sdf.parse(time);
            if (timeDate == null) {
                Log.e(TAG, "Failed to parse time: " + time);
                return;
            }

            // Create calendar for the alarm time
            Calendar alarmTime = Calendar.getInstance();
            Calendar parsedTime = Calendar.getInstance();
            parsedTime.setTime(timeDate);

            // Set the alarm time to today
            alarmTime.set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY));
            alarmTime.set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE));
            alarmTime.set(Calendar.SECOND, 0);
            alarmTime.set(Calendar.MILLISECOND, 0);

            // If the time has already passed today, schedule for tomorrow
            if (alarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
                alarmTime.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Create intent for the alarm
            Intent intent = new Intent(context, MedicineAlarmReceiver.class);
            intent.putExtra("medicineId", medicine.getId());
            intent.putExtra("medicineName", medicine.getName());
            intent.putExtra("dosage", medicine.getDosage());
            intent.putExtra("time", time);
            intent.putExtra("medicineType", medicine.getMedicineType());

            // Use unique request code based on medicine ID and time
            int requestCode = getRequestCode(medicine.getId(), time);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Schedule the alarm using the most reliable method for each Android version
            if (alarmManager != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Android 12+: Check if we can schedule exact alarms
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    alarmTime.getTimeInMillis(),
                                    pendingIntent
                            );
                            Log.d(TAG, "Scheduled exact alarm (Android 12+) for " + medicine.getName());
                        } else {
                            // Fallback to inexact repeating alarm if permission not granted
                            alarmManager.setRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    alarmTime.getTimeInMillis(),
                                    AlarmManager.INTERVAL_DAY,
                                    pendingIntent
                            );
                            Log.w(TAG, "Using inexact alarm (permission not granted) for " + medicine.getName());
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // Android 6.0 - 11: Use setExactAndAllowWhileIdle for reliability
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                alarmTime.getTimeInMillis(),
                                pendingIntent
                        );
                        Log.d(TAG, "Scheduled exact alarm (Android 6-11) for " + medicine.getName());
                    } else {
                        // Android 5.x and below: Use setExact
                        alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                alarmTime.getTimeInMillis(),
                                pendingIntent
                        );
                        Log.d(TAG, "Scheduled exact alarm (Android 5) for " + medicine.getName());
                    }

                    Log.d(TAG, "Alarm scheduled for " + medicine.getName() + " at " + 
                            sdf.format(alarmTime.getTime()) + " (Request code: " + requestCode + ")");
                } catch (SecurityException e) {
                    Log.e(TAG, "SecurityException: Cannot schedule exact alarm. Missing permission?", e);
                    // Try fallback to inexact alarm
                    try {
                        alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                alarmTime.getTimeInMillis(),
                                pendingIntent
                        );
                        Log.d(TAG, "Scheduled inexact alarm as fallback for " + medicine.getName());
                    } catch (Exception fallbackError) {
                        Log.e(TAG, "Failed to schedule any alarm", fallbackError);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling alarm for " + medicine.getName() + " at " + time, e);
        }
    }


    public void cancelMedicineAlarms(Medicine medicine) {
        if (medicine == null || medicine.getReminderTimes() == null) return;

        for (String time : medicine.getReminderTimes()) {
            cancelSingleAlarm(medicine.getId(), time);
        }
        Log.d(TAG, "Cancelled alarms for " + medicine.getName());
    }


    //Cancel a specific alarm.

    private void cancelSingleAlarm(String medicineId, String time) {
        try {
            Intent intent = new Intent(context, MedicineAlarmReceiver.class);
            int requestCode = getRequestCode(medicineId, time);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.d(TAG, "Cancelled alarm with request code: " + requestCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling alarm", e);
        }
    }


     //Cancel all alarms (used when clearing all medicines).

    public void cancelAllAlarms() {
        // Note: This is a simplified version. In a production app, you'd want to
        // maintain a list of all scheduled alarm request codes.
        Log.d(TAG, "Cancel all alarms called");
    }


     //Generate a unique request code for each medicine + time combination.
    private int getRequestCode(String medicineId, String time) {
        // Create a unique hash from medicine ID and time
        String combined = medicineId + "_" + time;
        return combined.hashCode();
    }


    //Reschedule all alarms for a list of medicines.

    public void rescheduleAllMedicines(List<Medicine> medicines) {
        if (medicines == null) return;

        for (Medicine medicine : medicines) {
            // Cancel existing alarms first
            cancelMedicineAlarms(medicine);
            // Schedule new alarms
            scheduleMedicineAlarms(medicine);
        }
        Log.d(TAG, "Rescheduled alarms for " + medicines.size() + " medicines");
    }
}
