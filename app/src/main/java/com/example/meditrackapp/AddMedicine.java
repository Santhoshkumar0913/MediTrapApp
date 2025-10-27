package com.example.meditrackapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;

import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddMedicine extends BaseActivity {

    private ImageView backArrow;
    private EditText etMedicineName;
    private EditText etDosage;
    private ImageButton btnDecrease, btnIncrease;
    private SwitchCompat switchReminder;
    private LinearLayout containerReminderTimes;
    private Button btnAddAnotherTime;
    private Button btnSaveMedicine;
    private Button btnCustom;
    private Button btnFromDate;
    private Button btnToDate;
    
    private List<String> reminderTimes = new ArrayList<>();
    private int reminderCounter = 1;
    private Calendar fromDate = Calendar.getInstance();
    private Calendar toDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private List<String> customDays = new ArrayList<>();
    private String customFrequency = "Once a day";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        initializeViews();
        setupClickListeners();
        setupDosageControls();
    }

    private void initializeViews() {
        backArrow = findViewById(R.id.backArrow);
        etMedicineName = findViewById(R.id.etMedicineName);
        etDosage = findViewById(R.id.etDosage);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        switchReminder = findViewById(R.id.switchReminder);
        containerReminderTimes = findViewById(R.id.containerReminderTimes);
        btnAddAnotherTime = findViewById(R.id.btnAddAnotherTime);
        btnSaveMedicine = findViewById(R.id.btnSaveMedicine);
        btnCustom = findViewById(R.id.btnCustom);
        btnFromDate = findViewById(R.id.btnFromDate);
        btnToDate = findViewById(R.id.btnToDate);
        
        // Setup first reminder time click listener
        TextView tvFirstTime = findViewById(R.id.tvReminderTime1);
        ImageView btnFirstDelete = findViewById(R.id.btnDeleteTime1);
        
        tvFirstTime.setOnClickListener(v -> showTimePicker(tvFirstTime, 1));
        btnFirstDelete.setOnClickListener(v -> removeFirstReminderTime());
        
        // Initialize date buttons
        btnFromDate.setText(dateFormat.format(fromDate.getTime()));
        btnToDate.setText(dateFormat.format(toDate.getTime()));
    }

    private void setupClickListeners() {
        backArrow.setOnClickListener(v -> {
            startActivity(new Intent(AddMedicine.this, MedicineType.class));
            finish();
        });

        btnSaveMedicine.setOnClickListener(v -> saveMedicine());

        btnAddAnotherTime.setOnClickListener(v -> addNewReminderTime());

        btnCustom.setOnClickListener(v -> showCustomDaysFragment());

        btnFromDate.setOnClickListener(v -> showDatePicker(true));
        btnToDate.setOnClickListener(v -> showDatePicker(false));

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateReminderVisibility(isChecked);
        });
    }

    private void setupDosageControls() {
        btnDecrease.setOnClickListener(v -> {
            int currentDosage = Integer.parseInt(etDosage.getText().toString());
            if (currentDosage > 1) {
                etDosage.setText(String.valueOf(currentDosage - 1));
            }
        });

        btnIncrease.setOnClickListener(v -> {
            int currentDosage = Integer.parseInt(etDosage.getText().toString());
            etDosage.setText(String.valueOf(currentDosage + 1));
        });
    }

    private void updateReminderVisibility(boolean isEnabled) {
        if (isEnabled) {
            containerReminderTimes.setVisibility(View.VISIBLE);
            btnAddAnotherTime.setVisibility(View.VISIBLE);
        } else {
            containerReminderTimes.setVisibility(View.GONE);
            btnAddAnotherTime.setVisibility(View.GONE);
        }
    }

    private void addNewReminderTime() {
        reminderCounter++;
        
        // Create new reminder time card
        CardView newCard = createReminderTimeCard(reminderCounter);
        containerReminderTimes.addView(newCard);
        
        // Update add button visibility
        if (reminderCounter >= 5) { // Limit to 5 reminders
            btnAddAnotherTime.setVisibility(View.GONE);
        }
    }

    private CardView createReminderTimeCard(int cardId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        CardView card = (CardView) inflater.inflate(R.layout.item_reminder_time, containerReminderTimes, false);
        
        TextView tvTime = card.findViewById(R.id.tvReminderTime);
        ImageView btnDelete = card.findViewById(R.id.btnDeleteTime);
        
        // Set unique IDs
        tvTime.setId(View.generateViewId());
        btnDelete.setId(View.generateViewId());
        
        // Set click listeners
        tvTime.setOnClickListener(v -> showTimePicker(tvTime, cardId));
        btnDelete.setOnClickListener(v -> removeReminderTime(card, cardId));
        
        return card;
    }

    private void showTimePicker(TextView textView, int cardId) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minuteOfDay) -> {
                String time = formatTime(hourOfDay, minuteOfDay);
                textView.setText(time);
                textView.setTextColor(getResources().getColor(R.color.black));
                
                // Show delete button
                if (cardId == 1) {
                    ImageView deleteBtn = findViewById(R.id.btnDeleteTime1);
                    if (deleteBtn != null) {
                        deleteBtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    ImageView deleteBtn = findViewById(getDeleteButtonId(cardId));
                    if (deleteBtn != null) {
                        deleteBtn.setVisibility(View.VISIBLE);
                    }
                }
                
                // Add to reminder times list
                updateReminderTimesList(cardId, time);
            },
            hour,
            minute,
            false // 12-hour format
        );
        
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private String formatTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private int getDeleteButtonId(int cardId) {
        // This is a simplified approach - in a real app you'd want a more robust ID management
        return R.id.btnDeleteTime1 + cardId - 1;
    }

    private void updateReminderTimesList(int cardId, String time) {
        // Ensure the list is large enough
        while (reminderTimes.size() < cardId) {
            reminderTimes.add("");
        }
        
        if (cardId <= reminderTimes.size()) {
            reminderTimes.set(cardId - 1, time);
        }
    }

    private void removeFirstReminderTime() {
        TextView tvFirstTime = findViewById(R.id.tvReminderTime1);
        ImageView btnFirstDelete = findViewById(R.id.btnDeleteTime1);
        
        tvFirstTime.setText("Select Time");
        tvFirstTime.setTextColor(getResources().getColor(R.color.inactive_tab_text));
        btnFirstDelete.setVisibility(View.GONE);
        
        // Remove from reminder times list
        if (!reminderTimes.isEmpty()) {
            reminderTimes.remove(0);
        }
    }

    private void removeReminderTime(CardView card, int cardId) {
        containerReminderTimes.removeView(card);
        
        // Remove from reminder times list
        if (cardId <= reminderTimes.size()) {
            reminderTimes.remove(cardId - 1);
        }
        
        // Show add button if we're under the limit
        if (reminderCounter < 5) {
            btnAddAnotherTime.setVisibility(View.VISIBLE);
        }
        
        reminderCounter--;
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar calendar = isFromDate ? fromDate : toDate;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                if (isFromDate) {
                    btnFromDate.setText(dateFormat.format(calendar.getTime()));
                } else {
                    btnToDate.setText(dateFormat.format(calendar.getTime()));
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showCustomDaysFragment() {
        CustomDaysFragment fragment = new CustomDaysFragment();
        fragment.setOnCustomDaysSelectedListener(new CustomDaysFragment.OnCustomDaysSelectedListener() {
            @Override
            public void onCustomDaysSelected(List<String> selectedDays, String frequency) {
                customDays = selectedDays;
                customFrequency = frequency;
                
                // Update the custom button text to show selected days
                if (!selectedDays.isEmpty()) {
                    btnCustom.setText("Custom: " + String.join(", ", selectedDays));
                }
                
                Toast.makeText(AddMedicine.this, "Custom schedule set: " + String.join(", ", selectedDays), Toast.LENGTH_SHORT).show();
            }
        });
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void saveMedicine() {
            String name = etMedicineName.getText().toString().trim();
            String dosageCount = etDosage.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a medicine name", Toast.LENGTH_SHORT).show();
                return;
            }

        if (dosageCount.isEmpty()) {
            Toast.makeText(this, "Please enter dosage", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user information
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create medicine object
        Medicine medicine = new Medicine();
        medicine.setName(name);
        medicine.setDosage(dosageCount + " pill(s)");
        medicine.setReminderEnabled(switchReminder.isChecked());
        medicine.setStartDate(dateFormat.format(fromDate.getTime()));
        medicine.setEndDate(dateFormat.format(toDate.getTime()));
        medicine.setCustomDays(customDays);
        medicine.setUserId(currentUser.getUid());
        medicine.setUserEmail(currentUser.getEmail());
        medicine.setUserName(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
        
        // Add reminder times if enabled
        if (switchReminder.isChecked()) {
            List<String> validTimes = new ArrayList<>();
            for (String time : reminderTimes) {
                if (!time.isEmpty()) {
                    validTimes.add(time);
                }
            }
            medicine.setReminderTimes(validTimes);
        }

        // Save to Firebase
        FirebaseMedicineHelper firebaseHelper = new FirebaseMedicineHelper();
        firebaseHelper.addMedicine(medicine, new FirebaseMedicineHelper.OnMedicineAddedListener() {
            @Override
            public void onMedicineAdded(boolean success, String message) {
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(AddMedicine.this, "Medicine saved successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddMedicine.this, MedicineListActivity.class));
                        finish();
                    } else {
                        // Fallback to local storage if Firebase fails
                        Toast.makeText(AddMedicine.this, "Firebase error, saving locally: " + message, Toast.LENGTH_SHORT).show();
                        saveToLocalStorage(medicine);
                    }
                });
            }
        });
    }

    private void saveToLocalStorage(Medicine medicine) {
        try {
            // Create a temporary Medicine object for backward compatibility with MedicineRepository
            Medicine tempMedicine = new Medicine(
                medicine.getName(),
                medicine.getDosage(),
                medicine.getReminderTimes().isEmpty() ? "12:00 PM" : medicine.getReminderTimes().get(0),
                "Daily",
                medicine.isTaken()
            );
            
            MedicineRepository.addMedicine(this, tempMedicine);
            Toast.makeText(this, "Medicine saved locally", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddMedicine.this, MedicineListActivity.class));
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save medicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}