package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class Dashboard extends BaseActivity {

    private Button btnMedicineList, btnAddMedicine, btnHistory, btnNearByLocation, btnProfileSettings, btnSmsStatus;
    private ImageView backArrowDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI
        btnMedicineList = findViewById(R.id.btnMedicineList);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        btnHistory = findViewById(R.id.btnHistory);
        btnNearByLocation = findViewById(R.id.btnNearByLocation);
        btnProfileSettings = findViewById(R.id.btnProfileSettings);
        btnSmsStatus = findViewById(R.id.btnSmsStatus);
        backArrowDashboard = findViewById(R.id.backArrowDashboard);

        // Back arrow → navigate to Login
        backArrowDashboard.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, login.class));
            finish();
        });

        // Set up button click listeners

        btnAddMedicine.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, MedicineType.class)));

        btnProfileSettings.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, Settings.class)));
        btnMedicineList.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, MedicineSchedule.class)));
        btnNearByLocation.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, NearByLocationActivity.class)));

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, HistoryMedicine.class)));

        btnSmsStatus.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, SmsStatusActivity.class)));

    }
}
