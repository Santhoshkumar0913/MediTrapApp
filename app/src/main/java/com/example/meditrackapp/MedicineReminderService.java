package com.example.meditrackapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MedicineReminderService {
    private static final String CHANNEL_ID = "medicine_reminder_channel";
    private static final int BASE_NOTIFICATION_ID = 1001;
    
    private Context context;
    private static MediaPlayer mediaPlayer;
    private static boolean isPlaying = false;
    private static String lastMedicineIdForRingtone;
    private static String lastDoseTimeForRingtone;
    
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
        // Save for ringtone management only
        lastMedicineIdForRingtone = medicine.getId();
        lastDoseTimeForRingtone = time;
        
        // Generate unique notification ID based on medicine ID and time
        int notificationId = generateNotificationId(medicine.getId(), time);
        
        android.util.Log.d("MedicineReminder", "Showing notification for " + medicine.getName() + " at " + time + " (ID: " + notificationId + ")");
        
        // Create intent for "Mark as Taken" action with unique request codes
        Intent takenIntent = new Intent(context, NotificationActionReceiver.class);
        takenIntent.setAction("MARK_AS_TAKEN");
        takenIntent.putExtra("medicineId", medicine.getId());
        takenIntent.putExtra("doseTime", time);
        takenIntent.putExtra("notificationId", notificationId);
        int takenRequestCode = notificationId * 10 + 1;
        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                context, takenRequestCode, takenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Create intent for "Skip" action with unique request codes
        Intent skipIntent = new Intent(context, NotificationActionReceiver.class);
        skipIntent.setAction("SKIP_MEDICINE");
        skipIntent.putExtra("medicineId", medicine.getId());
        skipIntent.putExtra("doseTime", time);
        skipIntent.putExtra("notificationId", notificationId);
        int skipRequestCode = notificationId * 10 + 2;
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(
                context, skipRequestCode, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Create intent to open the app when notification is clicked
        Intent openAppIntent = new Intent(context, Dashboard.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int openRequestCode = notificationId * 10 + 3;
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                context, openRequestCode, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
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
        
        // Show the notification (always, regardless of ringtone setting)
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.notify(notificationId, builder.build());
        
        android.util.Log.d("MedicineReminder", "Notification shown with ID: " + notificationId);
        
        // Check if ringtone is enabled in settings
        SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        boolean ringtoneEnabled = prefs.getBoolean("reminder_ringtone_enabled", true);
        
        // Play the ringtone only if enabled
        if (ringtoneEnabled) {
            playRingtone();
        }
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
        updateMedicineStatus(context, medicineId, lastDoseTimeForRingtone, status);
    }

    public static void updateMedicineStatus(Context context, String medicineId, String time, String status) {
        android.util.Log.d("MedicineReminder", "updateMedicineStatus called: medicineId=" + medicineId + ", time=" + time + ", status=" + status);
        // Stop the ringtone
        stopRingtone();
        
        // Cancel the specific notification
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (time != null && !time.isEmpty()) {
            int notificationId = generateNotificationId(medicineId, time);
            notificationManager.cancel(notificationId);
            android.util.Log.d("MedicineReminder", "Cancelled notification ID: " + notificationId);
        }
        
        // Update the medicine status in Firebase under userId/medicineId
        String userIdForPath = null;
        try {
            com.google.firebase.auth.FirebaseUser u = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (u != null) userIdForPath = u.getUid();
        } catch (Exception ignored) { }
        DatabaseReference medicineRef = userIdForPath == null
                ? FirebaseDatabase.getInstance().getReference().child("medicines")
                : FirebaseDatabase.getInstance().getReference().child("medicines").child(userIdForPath).child(medicineId);
        
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
        DatabaseReference medFetchRef = userIdForPath == null
                ? FirebaseDatabase.getInstance().getReference().child("medicines")
                : FirebaseDatabase.getInstance().getReference().child("medicines").child(userIdForPath).child(medicineId);
        medFetchRef
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Medicine medicine = task.getResult().getValue(Medicine.class);
                        if (medicine != null) {
                            // Get the first reminder time if available, otherwise use empty string
                            String reminderTime = time != null ? time : "";

                            java.util.HashMap<String, Object> map = new java.util.HashMap<>();
                            map.put("status", status);
                            map.put("time", reminderTime);
                            map.put("timestamp", System.currentTimeMillis());
                            map.put("medicineId", medicineId);
                            map.put("medicineName", medicine.getName());
                            map.put("whenToTake", medicine.getWhenToTake());
                            map.put("userId", medicine.getUserId());
                            map.put("userName", medicine.getUserName());
                            statusRef.setValue(map);

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
            android.util.Log.d("MedicineReminder", "Storing dose status: key=" + key + ", status=" + status);
            context.getSharedPreferences("dose_status", Context.MODE_PRIVATE)
                    .edit()
                    .putString(key, status)
                    .apply();
            try {
                // Notify UI screens to refresh dose status
                Intent i = new Intent("DOSE_STATUS_UPDATED");
                i.putExtra("medicineId", medicineId);
                i.putExtra("time", time);
                i.putExtra("status", status);
                context.sendBroadcast(i);
                android.util.Log.d("MedicineReminder", "Broadcast sent for dose status update");
            } catch (Exception e) {
                android.util.Log.e("MedicineReminder", "Error sending broadcast", e);
            }
        }
    }
    
    public static boolean isRingtonePlaying() {
        return isPlaying;
    }
    
    public static String getCurrentMedicineId() {
        return lastMedicineIdForRingtone;
    }
    
    public static String getCurrentDoseTime() {
        return lastDoseTimeForRingtone;
    }
    
    public static void cancelNotification(Context context, String medicineId, String time) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            int notificationId = generateNotificationId(medicineId, time);
            notificationManager.cancel(notificationId);
        }
    }
    
    // Generate unique notification ID based on medicine ID and time
    private static int generateNotificationId(String medicineId, String time) {
        if (medicineId == null || time == null) return BASE_NOTIFICATION_ID;
        String combined = medicineId + time;
        return BASE_NOTIFICATION_ID + Math.abs(combined.hashCode() % 10000);
    }
    
    private static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date());
    }
}