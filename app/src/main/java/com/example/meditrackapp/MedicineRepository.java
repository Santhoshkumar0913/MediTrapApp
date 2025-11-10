package com.example.meditrackapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MedicineRepository {
    private static final String PREFS = "meditrack_prefs";
    private static final String KEY_MEDICINES = "medicines";

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void addMedicine(Context ctx, Medicine m) {
        try {
            JSONArray arr = getArray(ctx);
            JSONObject obj = new JSONObject();
            obj.put("name", m.getName());
            obj.put("dosage", m.getDosage());
            
            // Handle reminder times - store first time for backward compatibility
            String timeDisplay = "12:00 PM"; // Default
            if (m.getReminderTimes() != null && !m.getReminderTimes().isEmpty()) {
                timeDisplay = m.getReminderTimes().get(0);
            }
            obj.put("time", timeDisplay);
            obj.put("frequency", "Daily"); // Default frequency
            obj.put("taken", m.isTaken());
            arr.put(obj);
            prefs(ctx).edit().putString(KEY_MEDICINES, arr.toString()).apply();
        } catch (JSONException e) {

        }
    }

    public static List<Medicine> getAll(Context ctx) {
        List<Medicine> list = new ArrayList<>();
        try {
            JSONArray arr = getArray(ctx);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Medicine(
                        o.optString("name"),
                        o.optString("dosage"),
                        o.optString("time"),
                        o.optString("frequency"),
                        o.optBoolean("taken", false)
                ));
            }
        } catch (JSONException e) {
            // ignore
        }
        return list;
    }

    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY_MEDICINES).apply();
    }

    public static void updateMedicine(Context ctx, Medicine m) {
        try {
            JSONArray arr = getArray(ctx);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.optString("name").equals(m.getName())) {
                    // Update the existing medicine
                    obj.put("taken", m.isTaken());
                    arr.put(i, obj);
                    prefs(ctx).edit().putString(KEY_MEDICINES, arr.toString()).apply();
                    return;
                }
            }
        } catch (JSONException e) {
            // ignore
        }
    }

    private static JSONArray getArray(Context ctx) throws JSONException {
        String json = prefs(ctx).getString(KEY_MEDICINES, "[]");
        return new JSONArray(json);
    }
}