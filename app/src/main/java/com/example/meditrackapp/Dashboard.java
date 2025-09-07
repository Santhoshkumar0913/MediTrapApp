package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Dashboard extends AppCompatActivity {

    private Button btnMedicineList, btnAddMedicine, btnHistory, btnEmergencyInfo, btnProfileSettings;
    private ImageView backArrowDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI
        btnMedicineList = findViewById(R.id.btnMedicineList);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        btnHistory = findViewById(R.id.btnHistory);
        btnEmergencyInfo = findViewById(R.id.btnEmergencyInfo);
        btnProfileSettings = findViewById(R.id.btnProfileSettings);
        backArrowDashboard = findViewById(R.id.backArrowDashboard);

        // Back arrow → navigate to Login
        backArrowDashboard.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, login.class));
            finish();
        });

        // Set up button click listeners

        btnProfileSettings.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, profile_settings.class)));
    }
}
