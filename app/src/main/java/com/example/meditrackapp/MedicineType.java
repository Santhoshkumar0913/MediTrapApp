package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MedicineType extends AppCompatActivity {

    private ImageView backArrow;
    private Button btnSubmit;
    private CheckBox cbTablets, cbLiquids, cbCreams, cbInhalers, cbInjections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_type);

        backArrow = findViewById(R.id.backArrow);
        btnSubmit = findViewById(R.id.btnSubmit);
        cbTablets = findViewById(R.id.cbTablets);
        cbLiquids = findViewById(R.id.cbLiquids);
        cbCreams = findViewById(R.id.cbCreams);
        cbInhalers = findViewById(R.id.cbInhalers);
        cbInjections = findViewById(R.id.cbInjections);

        // Back arrow: go back to previous activity (Dashboard)
        backArrow.setOnClickListener(v -> {
            startActivity(new Intent(MedicineType.this, Dashboard.class));
            finish();
        });

        // Continue button -> open AddMedicine
        btnSubmit.setOnClickListener(v -> {
            startActivity(new Intent(MedicineType.this, AddMedicine.class));

        });
    }
}
