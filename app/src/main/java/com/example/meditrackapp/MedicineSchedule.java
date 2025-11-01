package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private Medicine nextMedicine = null;
    private FirebaseMedicineHelper firebaseHelper;

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
        initializeViews();
        setupClickListeners();
        loadTodaysMedicines();
        updateUI();
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

    private void setupClickListeners() {
        backArrowDashboard.setOnClickListener(v -> {
            startActivity(new Intent(MedicineSchedule.this, Dashboard.class));
            finish();
        });

        tvSeeAll.setOnClickListener(v -> {
            startActivity(new Intent(MedicineSchedule.this, MedicineListActivity.class));
        });

        btnMarkAsTaken.setOnClickListener(v -> {
            if (nextMedicine != null) {
                markMedicineAsTaken(nextMedicine);
            }
        });

        btnSkip.setOnClickListener(v -> {
            if (nextMedicine != null) {
                skipMedicine(nextMedicine);
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
                
                // Find next medicine
                findNextMedicine();
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
                
                // Find next medicine
                findNextMedicine();
                updateUI();
            }
        });
    }

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean isMedicineScheduledForToday(Medicine medicine, String today) {
        // Check if medicine has reminder times
        if (medicine.getReminderTimes() == null || medicine.getReminderTimes().isEmpty()) {
            return false;
        }
        
        // Get current day of week (1 = Sunday, 2 = Monday, ..., 7 = Saturday)
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String currentDay = getDayStringFromCalendar(dayOfWeek);
        
        // Check if medicine is scheduled for today based on custom days
        List<String> customDays = medicine.getCustomDays();
        if (customDays != null && !customDays.isEmpty()) {
            return customDays.contains(currentDay);
        }
        
        // If no custom days are set, assume it's a daily medicine
        return true;
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

    private void findNextMedicine() {
        nextMedicine = null;
        
        if (!todaysMedicines.isEmpty()) {
            // Find the next medicine based on time
            // For simplicity, just take the first one
            nextMedicine = todaysMedicines.get(0);
        }
    }

    private void updateUI() {
        if (nextMedicine != null) {
            // Show next medicine
            tvNextTime.setText(getNextMedicineTime());
            tvNextMedicineName.setText(nextMedicine.getName());
            tvNextMedicineDosage.setText(nextMedicine.getDosage());
            imgNextMedicine.setImageResource(getMedicineTypeIcon(nextMedicine.getName()));
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

    private String getNextMedicineTime() {
        if (nextMedicine != null && !nextMedicine.getReminderTimes().isEmpty()) {
            return nextMedicine.getReminderTimes().get(0);
        }
        return "12:00 PM";
    }

    private int getMedicineTypeIcon(String medicineName) {
        // Get medicine type from the medicine object
        Medicine medicine = (medicineName != null && medicineName.equals(nextMedicine != null ? nextMedicine.getName() : "")) 
            ? nextMedicine 
            : getMedicineByName(medicineName);
            
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
        
        if (todaysMedicines.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
            
            for (Medicine medicine : todaysMedicines) {
                View medicineItem = createMedicineItem(medicine);
                medicineList.addView(medicineItem);
            }
        }
    }

    private View createMedicineItem(Medicine medicine) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_medicine_schedule, medicineList, false);
        
        ImageView imgMedicineType = itemView.findViewById(R.id.imgMedicineType);
        TextView tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
        TextView tvMedicineDetails = itemView.findViewById(R.id.tvMedicineDetails);
        TextView tvMedicineStatus = itemView.findViewById(R.id.tvMedicineStatus);
        
        // Set medicine data
        tvMedicineName.setText(medicine.getName());
        
        String time = medicine.getReminderTimes().isEmpty() ? "12:00 PM" : medicine.getReminderTimes().get(0);
        tvMedicineDetails.setText(medicine.getDosage() + " • " + time);
        
        // Set medicine type icon
        imgMedicineType.setImageResource(getMedicineTypeIcon(medicine.getName()));
        imgMedicineType.setColorFilter(null); // Remove any color filter
        
        // Set status text and color
        String status = medicine.getStatus();
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

    private void markMedicineAsTaken(Medicine medicine) {
        medicine.setTaken(true);
        medicine.setStatus("Taken");
        
        Toast.makeText(MedicineSchedule.this, medicine.getName() + " marked as taken", Toast.LENGTH_SHORT).show();
        updateMedicineInFirebase(medicine);
    }

    private void skipMedicine(Medicine medicine) {
        medicine.setStatus("Skipped");
        Toast.makeText(this, medicine.getName() + " skipped", Toast.LENGTH_SHORT).show();
        
        // Update in Firebase
        updateMedicineInFirebase(medicine);
    }
    
    private void updateMedicineInFirebase(Medicine medicine) {
        firebaseHelper.updateMedicine(medicine, new FirebaseMedicineHelper.OnMedicineAddedListener() {
            @Override
            public void onMedicineAdded(boolean success, String message) {
                runOnUiThread(() -> {
                    if (success) {
                        // Update UI after successful update
                        updateUI();
                    } else {
                        Toast.makeText(MedicineSchedule.this, "Error updating medicine: " + message, Toast.LENGTH_SHORT).show();
                        // Fallback to local update
                        MedicineRepository.updateMedicine(MedicineSchedule.this, medicine);
                        updateUI();
                    }
                });
            }
        });
    }
}