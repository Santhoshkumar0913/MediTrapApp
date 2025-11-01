package com.example.meditrackapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String medicineId = intent.getStringExtra("medicineId");
        String doseTime = intent.getStringExtra("doseTime");
        
        if (medicineId == null) {
            return;
        }
        
        if ("MARK_AS_TAKEN".equals(action)) {
            MedicineReminderService.updateMedicineStatus(context, medicineId, doseTime, "Taken");
        } else if ("SKIP_MEDICINE".equals(action)) {
            MedicineReminderService.updateMedicineStatus(context, medicineId, doseTime, "Skipped");
        }
    }
}