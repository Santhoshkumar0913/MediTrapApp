package com.example.meditrackapp;

import android.Manifest;
import android.content.Context;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import com.google.firebase.database.ValueEventListener;


//Sends SMS alerts to the user's family phone when doses are skipped.

public class SmsNotifier {

    public static void sendSkipAlert(Context context, Medicine medicine, String scheduledTime) {
        if (medicine == null || scheduledTime == null) return;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted; cannot send from here
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference ref = FirebaseDatabase
                .getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com")
                .getReference("users")
                .child(currentUser.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                User user = snapshot.getValue(User.class);
                if (user == null) return;

                String familyPhone = normalizeIndiaNumber(user.getFamilyPhone());
                if (familyPhone == null || familyPhone.isEmpty()) return;

                String userName = user.getName() != null ? user.getName() : (medicine.getUserName() != null ? medicine.getUserName() : "User");
                String medName = medicine.getName() != null ? medicine.getName() : "medicine";
                String dosage = medicine.getDosage() != null ? medicine.getDosage() : "";
                String time = scheduledTime;

                String message = buildMessage(userName, medName, dosage, time);
                String logKey = logSmsPending(currentUser.getUid(), familyPhone, userName, medName, dosage, time, message);
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    // Build sent/delivered PendingIntents to track status
                    Intent sentIntent = new Intent("SMS_SENT");
                    sentIntent.putExtra("logKey", logKey);
                    PendingIntent sentPI = PendingIntent.getBroadcast(
                            context, logKey.hashCode(), sentIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    Intent deliveredIntent = new Intent("SMS_DELIVERED");
                    deliveredIntent.putExtra("logKey", logKey);
                    PendingIntent deliveredPI = PendingIntent.getBroadcast(
                            context, (logKey.hashCode() ^ 0xABCDEF), deliveredIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    smsManager.sendTextMessage(familyPhone, null, message, sentPI, deliveredPI);
                } catch (Exception e) {
                    logSmsError(logKey, String.valueOf(e));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private static String normalizeIndiaNumber(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("91") && digits.length() == 12) {
            return "+" + digits;
        }
        if (digits.length() == 10) {
            return "+91" + digits;
        }
        if (raw.startsWith("+91")) return raw;
        return null;
    }

    private static String buildMessage(String userName, String medName, String dosage, String time) {
        if (dosage == null || dosage.isEmpty()) {
            return "Alert: " + userName + " skipped " + medName + " at " + time + ".";
        }
        return "Alert: " + userName + " skipped " + medName + " (" + dosage + ") at " + time + ".";
    }

    private static String logSmsPending(String userId, String phone, String userName, String medName,
                                        String dosage, String time, String message) {
        try {
            DatabaseReference ref = FirebaseDatabase
                    .getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com")
                    .getReference("smsLogs");
            String key = ref.push().getKey();
            if (key == null) return "";
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("userName", userName);
            map.put("familyPhone", phone);
            map.put("medicineName", medName);
            map.put("dosage", dosage);
            map.put("scheduledTime", time);
            map.put("message", message);
            map.put("timestamp", System.currentTimeMillis());
            map.put("status", "PENDING");
            ref.child(key).setValue(map);
            return key;
        } catch (Exception ignored) {
            return "";
        }
    }

    private static void logSmsError(String key, String error) {
        try {
            DatabaseReference ref = FirebaseDatabase
                    .getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com")
                    .getReference("smsLogs").child(key);
            HashMap<String, Object> map = new HashMap<>();
            map.put("status", "ERROR");
            map.put("error", error);
            ref.updateChildren(map);
        } catch (Exception ignored) {
        }
    }
}


