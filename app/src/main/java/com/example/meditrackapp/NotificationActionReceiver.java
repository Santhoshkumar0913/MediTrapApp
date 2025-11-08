package com.example.meditrackapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String medicineId = intent.getStringExtra("medicineId");
        String doseTime = intent.getStringExtra("doseTime");
        int notificationId = intent.getIntExtra("notificationId", -1);
        
        Log.d("NotificationAction", "Action received: " + action + ", medicineId: " + medicineId + ", time: " + doseTime + ", notifId: " + notificationId);
        
        if (medicineId == null || doseTime == null) {
            Log.e("NotificationAction", "Missing medicineId or doseTime");
            return;
        }
        
        if ("MARK_AS_TAKEN".equals(action)) {
            Log.d("NotificationAction", "Marking as TAKEN: " + medicineId + " at " + doseTime);
            MedicineReminderService.updateMedicineStatus(context, medicineId, doseTime, "Taken");
            Toast.makeText(context, "Marked as taken", Toast.LENGTH_SHORT).show();
        } else if ("SKIP_MEDICINE".equals(action)) {
            Log.d("NotificationAction", "Marking as SKIPPED: " + medicineId + " at " + doseTime);
            MedicineReminderService.updateMedicineStatus(context, medicineId, doseTime, "Skipped");
            Toast.makeText(context, "Medicine skipped", Toast.LENGTH_SHORT).show();
        }
    }
}