package com.example.meditrackapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SmsSendReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        String key = intent.getStringExtra("logKey");
        if (key == null || key.isEmpty()) return;
        
        android.util.Log.d("SmsSendReceiver", "Received action: " + action + ", logKey: " + key);

        DatabaseReference ref = FirebaseDatabase
                .getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com")
                .getReference("smsLogs").child(key);

        HashMap<String, Object> map = new HashMap<>();
        if ("SMS_SENT".equals(action)) {
            switch (getResultCode()) {
                case android.app.Activity.RESULT_OK:
                    map.put("status", "SENT");
                    break;
                case android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    map.put("status", "FAILED");
                    map.put("error", "GENERIC_FAILURE");
                    break;
                case android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE:
                    map.put("status", "FAILED");
                    map.put("error", "NO_SERVICE");
                    break;
                case android.telephony.SmsManager.RESULT_ERROR_NULL_PDU:
                    map.put("status", "FAILED");
                    map.put("error", "NULL_PDU");
                    break;
                case android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF:
                    map.put("status", "FAILED");
                    map.put("error", "RADIO_OFF");
                    break;
                default:
                    map.put("status", "FAILED");
                    map.put("error", "UNKNOWN_ERROR");
                    break;
            }
        } else if ("SMS_DELIVERED".equals(action)) {
            switch (getResultCode()) {
                case android.app.Activity.RESULT_OK:
                    map.put("delivered", true);
                    break;
                case android.app.Activity.RESULT_CANCELED:
                    map.put("delivered", false);
                    break;
            }
        }

        try {
            ref.updateChildren(map);
            android.util.Log.d("SmsSendReceiver", "Updated SMS log with status: " + map.toString());
        } catch (Exception e) {
            android.util.Log.e("SmsSendReceiver", "Error updating SMS log", e);
        }
    }
}


