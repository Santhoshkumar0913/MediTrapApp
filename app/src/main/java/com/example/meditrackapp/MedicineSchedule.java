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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_schedule);
        
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
        // Load all medicines from repository
        List<Medicine> allMedicines = MedicineRepository.getAll(this);
        
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
    }

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean isMedicineScheduledForToday(Medicine medicine, String today) {
        // For now, assume all medicines are scheduled daily
        // In a real app, you'd check the medicine's schedule (start date, end date, custom days)
        return true; // Simplified for demo
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
            
            btnMarkAsTaken.setVisibility(View.VISIBLE);
            btnSkip.setVisibility(View.VISIBLE);
        } else {
            // Show empty state
            tvNextTime.setText("No medication");
            tvNextMedicineName.setText("No medication scheduled");
            tvNextMedicineDosage.setText("Add medicines to see schedule");
            imgNextMedicine.setImageResource(R.drawable.ic_check_circle);
            
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
        // Map medicine names to appropriate icons
        String name = medicineName.toLowerCase();
        
        if (name.contains("vitamin") || name.contains("supplement")) {
            return R.drawable.ic_check_circle;
        } else if (name.contains("pain") || name.contains("headache")) {
            return R.drawable.ic_pill_blue;
        } else if (name.contains("antibiotic") || name.contains("infection")) {
            return R.drawable.ic_check_circle;
        } else {
            return R.drawable.ic_check_circle; // Default icon
        }
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
        ImageView imgStatus = itemView.findViewById(R.id.imgStatus);
        
        // Set medicine data
        tvMedicineName.setText(medicine.getName());
        
        String time = medicine.getReminderTimes().isEmpty() ? "12:00 PM" : medicine.getReminderTimes().get(0);
        tvMedicineDetails.setText(medicine.getDosage() + " • " + time);
        
        // Set medicine type icon
        imgMedicineType.setImageResource(getMedicineTypeIcon(medicine.getName()));
        
        // Set status icon
        if (medicine.isTaken()) {
            imgStatus.setImageResource(R.drawable.ic_check_circle);
            imgStatus.setColorFilter(getResources().getColor(R.color.green));
        } else {
            imgStatus.setImageResource(R.drawable.ic_check_circle);
            imgStatus.setColorFilter(getResources().getColor(R.color.gray));
        }
        
        return itemView;
    }

    private void markMedicineAsTaken(Medicine medicine) {
        medicine.setTaken(true);
        // Update in repository
        // For now, just show a toast
        Toast.makeText(this, medicine.getName() + " marked as taken", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    private void skipMedicine(Medicine medicine) {
        Toast.makeText(this, medicine.getName() + " skipped", Toast.LENGTH_SHORT).show();
        // Remove from today's list or mark as skipped
        updateUI();
    }
}