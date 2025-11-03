package com.example.meditrackapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MedicineReminderService {
    private static final String CHANNEL_ID = "medicine_reminder_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private Context context;
    private static MediaPlayer mediaPlayer;
    private static boolean isPlaying = false;
    private static String currentMedicineId;
    private static String currentDoseTime;
    
    public MedicineReminderService(Context context) {
        this.context = context;
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Medicine Reminders";
            String description = "Notifications for medicine reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public void showMedicineReminder(Medicine medicine, String time) {
        // Save the current medicine ID and dose time
        currentMedicineId = medicine.getId();
        currentDoseTime = time;
        
        // Create intent for "Mark as Taken" action
        Intent takenIntent = new Intent(context, NotificationActionReceiver.class);
        takenIntent.setAction("MARK_AS_TAKEN");
        takenIntent.putExtra("medicineId", medicine.getId());
        takenIntent.putExtra("doseTime", time);
        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                context, 0, takenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Create intent for "Skip" action
        Intent skipIntent = new Intent(context, NotificationActionReceiver.class);
        skipIntent.setAction("SKIP_MEDICINE");
        skipIntent.putExtra("medicineId", medicine.getId());
        skipIntent.putExtra("doseTime", time);
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(
                context, 1, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Create intent to open the app when notification is clicked
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                context, 2, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Medicine Reminder")
                .setContentText("Time to take " + medicine.getName() + " - " + medicine.getDosage() + " at " + time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(openAppPendingIntent)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_check, "Mark as Taken", takenPendingIntent)
                .addAction(R.drawable.ic_skip, "Skip", skipPendingIntent);
        
        // Show the notification
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        
        // Play the ringtone
        playRingtone();
    }
    
    private void playRingtone() {
        if (mediaPlayer != null) {
            stopRingtone();
        }
        
        mediaPlayer = MediaPlayer.create(context, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        isPlaying = true;
    }
    
    public static void stopRingtone() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }
    
    public static void updateMedicineStatus(Context context, String medicineId, String status) {
        updateMedicineStatus(context, medicineId, currentDoseTime, status);
    }

    public static void updateMedicineStatus(Context context, String medicineId, String time, String status) {
        // Stop the ringtone
        stopRingtone();
        
        // Cancel the notification
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.cancel(NOTIFICATION_ID);
        
        // Update the medicine status in Firebase
        DatabaseReference medicineRef = FirebaseDatabase.getInstance().getReference()
                .child("medicines").child(medicineId);
        
        medicineRef.child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    // Status updated successfully
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
        
        // Store the medicine status record in Firebase
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference()
                .child("medicineStatus").push();
        
        // Get the medicine details to store in the status record
        FirebaseDatabase.getInstance().getReference().child("medicines").child(medicineId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Medicine medicine = task.getResult().getValue(Medicine.class);
                        if (medicine != null) {
                            // Get the first reminder time if available, otherwise use empty string
                            String reminderTime = time != null ? time : "";
                            
                            MedicineStatus medicineStatus = new MedicineStatus(
                                    medicine.getName(),
                                    medicine.getDosage(),
                                    reminderTime,
                                    status,
                                    System.currentTimeMillis()
                            );
                            statusRef.setValue(medicineStatus);

                            // If skipped, notify family via SMS (if permission granted)
                            if ("Skipped".equalsIgnoreCase(status)) {
                                SmsNotifier.sendSkipAlert(context, medicine, reminderTime);
                            }
                        }
                    }
                });

        // Persist dose-level status locally so UI can reflect it
        if (time != null && !time.isEmpty()) {
            String dateKey = getTodayDateString();
            String key = "dose:" + medicineId + ":" + dateKey + ":" + time;
            context.getSharedPreferences("dose_status", Context.MODE_PRIVATE)
                    .edit()
                    .putString(key, status)
                    .apply();
        }
    }
    
    public static boolean isRingtonePlaying() {
        return isPlaying;
    }
    
    public static String getCurrentMedicineId() {
        return currentMedicineId;
    }

    public static String getCurrentDoseTime() {
        return currentDoseTime;
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date());
    }
}