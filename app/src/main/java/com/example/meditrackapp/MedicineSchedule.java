package com.example.meditrackapp;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Handler;
import android.os.Looper;

public class MedicineSchedule extends BaseActivity {

    private ImageView backArrowDashboard;
    private TextView tvNextTime;
    private TextView tvNextMedicineName;
    private TextView tvNextMedicineDosage;
    private ImageView imgNextMedicine;
    private Button btnMarkAsTaken;
    private Button btnSkip;
    private TextView tvSeeAll;
    private LinearLayout medicineList;
    private CardView cardEmptyState;

    private List<Medicine> todaysMedicines = new ArrayList<>();
    private static class Dose {
        Medicine medicine;
        String time; // formatted like "h:mm a"
        String status; // Next/Taken/Skipped
        String key; // medicineId + date + time
    }
    private final List<Dose> todaysDoses = new ArrayList<>();
    private Dose nextDose = null;
    private FirebaseMedicineHelper firebaseHelper;
    private MedicineAlarmScheduler alarmScheduler;

    // UI refresh handler to update dose statuses from SharedPreferences
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {

            // reflect instantly without requiring navigation or a broadcast to be received
            boolean anyStatusChanged = false;
            for (Dose d : todaysDoses) {
                String latest = getStoredDoseStatus(d.key);
                if (latest != null && !latest.equals(d.status)) {
                    d.status = latest;
                    anyStatusChanged = true;
                }
            }
            // Recompute next dose and refresh UI if anything changed
            findNextDose();
            if (anyStatusChanged) {
                updateMedicineList(); // Rebuild the list to show updated statuses
                updateUI();
            }
            if (!isFinishing()) {
                refreshHandler.postDelayed(this, 5000); // every 5 seconds
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_schedule);

        // Check if user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseHelper = new FirebaseMedicineHelper();
        alarmScheduler = new MedicineAlarmScheduler(this);
        initializeViews();
        setupClickListeners();
        loadTodaysMedicines();
        ensureNotificationPermission();
        ensureSmsPermission();
        ensureAlarmPermission();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list so newly added or edited medicines appear
        loadTodaysMedicines();
        
        // Immediately refresh statuses for existing doses (in case user marked taken/skipped elsewhere)
        for (Dose d : todaysDoses) {
            String latest = getStoredDoseStatus(d.key);
            if (latest != null) {
                d.status = latest;
            }
        }
        updateMedicineList(); // Rebuild list to show correct statuses
        
        // Start UI refresh to show updated statuses
        refreshHandler.removeCallbacks(refreshTask);
        refreshHandler.postDelayed(refreshTask, 1000);
        // Listen for dose status updates from background actions
        if (!isDoseReceiverRegistered) {
            try {
                registerReceiver(doseStatusReceiver, new android.content.IntentFilter("DOSE_STATUS_UPDATED"));
                isDoseReceiverRegistered = true;
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop UI refresh when not visible
        refreshHandler.removeCallbacks(refreshTask);
        // Note: We no longer stop ringtone/notifications when leaving screen
        // They continue to work globally via AlarmManager
        if (isDoseReceiverRegistered) {
            try { unregisterReceiver(doseStatusReceiver); } catch (Exception ignored) {}
            isDoseReceiverRegistered = false;
        }
    }

    private void initializeViews() {
        backArrowDashboard = findViewById(R.id.backArrowDashboard);
        tvNextTime = findViewById(R.id.tvNextTime);
        tvNextMedicineName = findViewById(R.id.tvNextMedicineName);
        tvNextMedicineDosage = findViewById(R.id.tvNextMedicineDosage);
        imgNextMedicine = findViewById(R.id.imgNextMedicine);
        btnMarkAsTaken = findViewById(R.id.btnMarkAsTaken);
        btnSkip = findViewById(R.id.btnSkip);
        tvSeeAll = findViewById(R.id.tvSeeAll);
        medicineList = findViewById(R.id.medicineList);
        cardEmptyState = findViewById(R.id.cardEmptyState);
    }

    private final BroadcastReceiver doseStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "DOSE_STATUS_UPDATED".equals(intent.getAction())) {
                // Rebuild doses to refresh statuses from SharedPreferences
                buildTodaysDoses();
                updateUI();
            }
        }
    };
    private boolean isDoseReceiverRegistered = false;

    private void setupClickListeners() {
        backArrowDashboard.setOnClickListener(v -> {
            startActivity(new Intent(MedicineSchedule.this, Dashboard.class));
            finish();
        });

        tvSeeAll.setOnClickListener(v -> {
            startActivity(new Intent(MedicineSchedule.this, MedicineListActivity.class));
        });

        btnMarkAsTaken.setOnClickListener(v -> {
            if (nextDose != null) {
                markDoseAsTaken(nextDose);
            }
        });

        btnSkip.setOnClickListener(v -> {
            if (nextDose != null) {
                skipDose(nextDose);
            }
        });
    }

    private void loadTodaysMedicines() {
        firebaseHelper.getAllMedicines(new FirebaseMedicineHelper.OnMedicinesLoadedListener() {
            @Override
            public void onMedicinesLoaded(List<Medicine> medicines) {
                // Only show medicines for the current user
                List<Medicine> userMedicines = new ArrayList<>();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    for (Medicine medicine : medicines) {
                        if (currentUser.getUid().equals(medicine.getUserId())) {
                            userMedicines.add(medicine);
                        }
                    }
                }

                // Filter medicines for today
                todaysMedicines.clear();
                String today = getTodayDateString();

                for (Medicine medicine : userMedicines) {
                    // Check if medicine is scheduled for today
                    if (isMedicineScheduledForToday(medicine, today)) {
                        todaysMedicines.add(medicine);
                    }
                }

                // Build doses and find next
                buildTodaysDoses();
                findNextDose();
                
                // Schedule alarms for all today's medicines
                scheduleAlarmsForTodaysMedicines();
                
                updateUI();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MedicineSchedule.this, "Error loading medicines: " + error, Toast.LENGTH_SHORT).show();
                // Fallback to local storage if Firebase fails
                List<Medicine> allMedicines = MedicineRepository.getAll(MedicineSchedule.this);

                // Filter medicines for today
                todaysMedicines.clear();
                String today = getTodayDateString();

                for (Medicine medicine : allMedicines) {
                    // Check if medicine is scheduled for today
                    if (isMedicineScheduledForToday(medicine, today)) {
                        todaysMedicines.add(medicine);
                    }
                }

                // Build doses and find next
                buildTodaysDoses();
                findNextDose();
                
                // Schedule alarms for all today's medicines
                scheduleAlarmsForTodaysMedicines();
                
                updateUI();
            }
        });
    }
    
    /**
     * Schedule alarms for all of today's medicines.
     * This ensures notifications work globally, not just on this screen.
     */
    private void scheduleAlarmsForTodaysMedicines() {
        if (todaysMedicines == null || todaysMedicines.isEmpty()) {
            return;
        }
        
        int scheduledCount = 0;
        for (Medicine medicine : todaysMedicines) {
            if (medicine.getReminderTimes() != null && !medicine.getReminderTimes().isEmpty()) {
                // Cancel existing alarms first to avoid duplicates
                alarmScheduler.cancelMedicineAlarms(medicine);
                // Schedule new alarms
                alarmScheduler.scheduleMedicineAlarms(medicine);
                scheduledCount++;
            }
        }
    }

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean isMedicineScheduledForToday(Medicine medicine, String today) {
        // Must have at least one reminder time
        if (medicine.getReminderTimes() == null || medicine.getReminderTimes().isEmpty()) return false;

        // Check if today falls within the medicine's date range (startDate to endDate)
        if (!isWithinDateRange(medicine, today)) {
            return false;
        }

        // Determine today's day in multiple forms
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String full = getDayStringFromCalendar(dayOfWeek); // e.g., Monday
        String short3 = full.length() >= 3 ? full.substring(0, 3) : full; // e.g., Mon

        List<String> customDays = medicine.getCustomDays();
        if (customDays != null && !customDays.isEmpty()) {
            for (String d : customDays) {
                if (d == null) continue;
                String dd = d.trim();
                if (dd.equalsIgnoreCase(full) || dd.equalsIgnoreCase(short3)) {
                    return true;
                }
                // Accept numeric 1-7 (1=Sunday)
                if (dd.matches("[1-7]")) {
                    int n = Integer.parseInt(dd);
                    if (n == dayOfWeek) return true;
                }
            }
            // Provided days but none match today
            return false;
        }

        // No custom days → treat as daily (but still within date range)
        return true;
    }
    

    //Check if today falls within the medicine's start and end date range.
    //Returns true if no dates are set (backward compatibility).

    private boolean isWithinDateRange(Medicine medicine, String todayStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            Date today = sdf.parse(todayStr);
            if (today == null) return true; // Can't parse today, allow it
            
            // Check start date
            String startDateStr = medicine.getStartDate();
            if (startDateStr != null && !startDateStr.isEmpty()) {
                Date startDate = sdf.parse(startDateStr);
                if (startDate != null && today.before(startDate)) {
                    // Today is before start date - medicine not started yet
                    return false;
                }
            }
            
            // Check end date
            String endDateStr = medicine.getEndDate();
            if (endDateStr != null && !endDateStr.isEmpty()) {
                Date endDate = sdf.parse(endDateStr);
                if (endDate != null && today.after(endDate)) {
                    // Today is after end date - medicine already ended
                    return false;
                }
            }
            
            // Within range (or no range specified)
            return true;
        } catch (Exception e) {
            return true; // On error, allow the medicine to show
        }
    }

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

    private void buildTodaysDoses() {
        todaysDoses.clear();
        if (todaysMedicines.isEmpty()) return;

        String todayKey = getTodayDateString();
        
        for (Medicine med : todaysMedicines) {
            List<String> times = med.getReminderTimes();
            if (times == null || times.isEmpty()) continue;
            for (String time : times) {
                Dose d = new Dose();
                d.medicine = med;
                d.time = time;
                d.key = buildDoseKey(med.getId(), todayKey, time);
                d.status = getStoredDoseStatus(d.key);
                
                todaysDoses.add(d);
            }
        }
    }

    private void findNextDose() {
        nextDose = null;
        if (todaysDoses.isEmpty()) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Calendar now = Calendar.getInstance();
            long nowMillis = now.getTimeInMillis();

            long bestDelta = Long.MAX_VALUE;
            Dose best = null;

            for (Dose d : todaysDoses) {
                if ("Taken".equals(d.status) || "Skipped".equals(d.status)) continue;
                Date medTime = sdf.parse(d.time);
                if (medTime == null) continue;

                Calendar medCal = Calendar.getInstance();
                medCal.setTime(medTime);
                medCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                medCal.set(Calendar.MONTH, now.get(Calendar.MONTH));
                medCal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

                long delta = medCal.getTimeInMillis() - nowMillis;
                if (delta >= 0 && delta < bestDelta) {
                    bestDelta = delta;
                    best = d;
                }
            }

            nextDose = best;
        } catch (Exception e) {
            nextDose = null;
        }
    }

    private void updateUI() {
        // Always recompute next based on current dose statuses and time
        findNextDose();

        if (nextDose != null) {
            // Show next medicine
            tvNextTime.setText(nextDose.time);
            tvNextMedicineName.setText(nextDose.medicine.getName());
            String when = nextDose.medicine.getWhenToTake();
            if (when != null && !when.isEmpty()) {
                tvNextMedicineDosage.setText(when + " • " + formatDosageForType(nextDose.medicine) + " • " + nextDose.time);
            } else {
                tvNextMedicineDosage.setText(formatDosageForType(nextDose.medicine) + " • " + nextDose.time);
            }

            imgNextMedicine.setImageResource(getMedicineTypeIcon(nextDose.medicine.getName()));
            imgNextMedicine.setColorFilter(null); // Remove any color filter

            btnMarkAsTaken.setVisibility(View.VISIBLE);
            btnSkip.setVisibility(View.VISIBLE);
        } else {
            // Show empty state
            tvNextTime.setText("No medication");
            tvNextMedicineName.setText("No medication scheduled");
            tvNextMedicineDosage.setText("Add medicines to see schedule");
            imgNextMedicine.setImageResource(R.drawable.ic_tablet);
            imgNextMedicine.setColorFilter(null);

            btnMarkAsTaken.setVisibility(View.GONE);
            btnSkip.setVisibility(View.GONE);
        }

        updateMedicineList();
    }

    private int getMedicineTypeIcon(String medicineName) {
        // Get medicine type from the medicine object
        Medicine medicine = getMedicineByName(medicineName);
            
        if (medicine != null && medicine.getMedicineType() != null) {
            String type = medicine.getMedicineType().toLowerCase();
            
            if (type.contains("tablet")) {
                return R.drawable.ic_tablet;
            } else if (type.contains("liquid")) {
                return R.drawable.ic_liquid;
            } else if (type.contains("cream")) {
                return R.drawable.ic_cream;
            } else if (type.contains("inhaler")) {
                return R.drawable.ic_inhaler;
            } else if (type.contains("injection")) {
                return R.drawable.ic_injection;
            }
        }
        
        // Default icon for unknown or unavailable types
        return R.drawable.ic_tablet; // Use tablet as default since it's a common type
    }
    
    private Medicine getMedicineByName(String name) {
        for (Medicine med : todaysMedicines) {
            if (med.getName().equals(name)) {
                return med;
            }
        }
        return null;
    }

    private void updateMedicineList() {
        medicineList.removeAllViews();

        if (todaysDoses.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
        } else {
            cardEmptyState.setVisibility(View.GONE);

            for (Dose d : todaysDoses) {
                View medicineItem = createDoseItem(d);
                medicineList.addView(medicineItem);
                // Note: Notifications are now handled globally by AlarmManager
                // No need to check dose time here
            }
        }
    }

    private View createDoseItem(Dose d) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_medicine_schedule, medicineList, false);

        ImageView imgMedicineType = itemView.findViewById(R.id.imgMedicineType);
        TextView tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
        TextView tvMedicineDetails = itemView.findViewById(R.id.tvMedicineDetails);
        TextView tvMedicineStatus = itemView.findViewById(R.id.tvMedicineStatus);

        // Set medicine data
        tvMedicineName.setText(d.medicine.getName());
        String when = d.medicine.getWhenToTake();
        if (when != null && !when.isEmpty()) {
            tvMedicineDetails.setText(when + " • " + formatDosageForType(d.medicine) + " • " + d.time);
        } else {
            tvMedicineDetails.setText(formatDosageForType(d.medicine) + " • " + d.time);
        }

        // Set medicine type icon
        imgMedicineType.setImageResource(getMedicineTypeIcon(d.medicine.getName()));
        imgMedicineType.setColorFilter(null); // Remove any color filter

        // Set status text and color
        String status = d.status;
        tvMedicineStatus.setText(status);

        if ("Taken".equals(status)) {
            tvMedicineStatus.setTextColor(getResources().getColor(R.color.green));
        } else if ("Skipped".equals(status)) {
            tvMedicineStatus.setTextColor(getResources().getColor(R.color.gray));
        } else {
            // Default "Next" status
            tvMedicineStatus.setTextColor(getResources().getColor(R.color.blue));
        }

        return itemView;
    }

    private void markDoseAsTaken(Dose d) {
        storeDoseStatus(d.key, "Taken");
        d.status = "Taken";
        Toast.makeText(MedicineSchedule.this, d.medicine.getName() + " marked as taken", Toast.LENGTH_SHORT).show();
        // Ensure ringtone and notification are stopped and status persisted globally
        MedicineReminderService.updateMedicineStatus(this, d.medicine.getId(), d.time, "Taken");
        updateMedicineList(); // Immediately rebuild list to show updated status
        updateUI();
    }

    private void skipDose(Dose d) {
        storeDoseStatus(d.key, "Skipped");
        d.status = "Skipped";
        Toast.makeText(this, d.medicine.getName() + " skipped", Toast.LENGTH_SHORT).show();
        // Ensure ringtone and notification are stopped and status persisted globally
        MedicineReminderService.updateMedicineStatus(this, d.medicine.getId(), d.time, "Skipped");
        // Send SMS alert to family if permitted
        SmsNotifier.sendSkipAlert(this, d.medicine, d.time);
        updateMedicineList(); // Immediately rebuild list to show updated status
        updateUI();
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }

    private void ensureSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 2001);
            }
        }
    }

    private void ensureAlarmPermission() {
        // For Android 12+ (API 31+), need SCHEDULE_EXACT_ALARM permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Show dialog to user explaining why we need this permission
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("⏰ Alarm & Reminder Permission Required")
                    .setMessage("To receive medicine reminders at exact times (even when the app is closed), this app needs permission to schedule exact alarms.\n\n" +
                               "Without this permission, you may miss important medicine reminders.\n\n" +
                               "Click 'Grant Permission' to enable this critical feature.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        // Open the exact alarm settings page
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Unable to open alarm settings. Please enable manually in Settings → Apps → MediTrack → Alarms & Reminders", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Later", (dialog, which) -> {
                        Toast.makeText(this, "Warning: Medicine reminders will not work reliably without this permission", Toast.LENGTH_LONG).show();
                    })
                    .setCancelable(false)
                    .show();
            } else {
            }
        } else {
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == 1001) { // Notification permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications disabled - You won't see medicine reminders", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 2001) { // SMS permission
        }
    }

    // ---- Dose status persistence (per day, per time) ----
    private String buildDoseKey(String medicineId, String dateKey, String time) {
        return "dose:" + medicineId + ":" + dateKey + ":" + time;
    }

    private String getStoredDoseStatus(String key) {
        return getSharedPreferences("dose_status", MODE_PRIVATE).getString(key, "Next");
    }

    private void storeDoseStatus(String key, String status) {
        getSharedPreferences("dose_status", MODE_PRIVATE)
                .edit()
                .putString(key, status)
                .apply();
    }

    private boolean hasDoseFired(String key) {
        return getSharedPreferences("dose_status", MODE_PRIVATE)
                .getBoolean("fired:" + key, false);
    }

    private void markDoseFired(String key) {
        getSharedPreferences("dose_status", MODE_PRIVATE)
                .edit()
                .putBoolean("fired:" + key, true)
                .apply();
    }

    private String formatDosageForType(Medicine med) {
        String dosage = med.getDosage() != null ? med.getDosage() : "";
        String type = med.getMedicineType() != null ? med.getMedicineType().toLowerCase() : "";
        // If dosage already includes a unit that's not pill(s), keep it
        if (dosage.matches(".*(ml|g|mg|puff\\(s\\)|unit\\(s\\)|tablet\\(s\\)).*")) return dosage;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+)\\s*pill\\(s\\)$").matcher(dosage.trim());
        String qty = null;
        if (m.find()) qty = m.group(1);
        if (qty == null && dosage.matches("^\\d+$")) qty = dosage;
        if (qty == null) return dosage;
        String unit = "pill(s)";
        if (type.contains("liquid")) unit = "ml";
        else if (type.contains("inhaler")) unit = "puff(s)";
        else if (type.contains("cream")) unit = "g";
        else if (type.contains("injection")) unit = "unit(s)";
        else if (type.contains("tablet")) unit = "tablet(s)";
        return qty + " " + unit;
    }
}