package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MedicineType extends BaseActivity {

    private static final String TAG = "MedicineType";
    private ImageView backArrow;
    private Button btnSubmit;
    private RadioButton rbTablets, rbLiquids, rbCreams, rbInhalers, rbInjections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_type);

        backArrow = findViewById(R.id.backArrow);
        btnSubmit = findViewById(R.id.btnSubmit);
        rbTablets = findViewById(R.id.rbTablets);
        rbLiquids = findViewById(R.id.rbLiquids);
        rbCreams = findViewById(R.id.rbCreams);
        rbInhalers = findViewById(R.id.rbInhalers);
        rbInjections = findViewById(R.id.rbInjections);

        // Ensure button disabled until a selection is made
        btnSubmit.setEnabled(false);

        View.OnClickListener singleSelectListener = v -> {
            // Uncheck all, then check the clicked one
            rbTablets.setChecked(false);
            rbLiquids.setChecked(false);
            rbCreams.setChecked(false);
            rbInhalers.setChecked(false);
            rbInjections.setChecked(false);
            ((RadioButton) v).setChecked(true);
            btnSubmit.setEnabled(true);
        };

        rbTablets.setOnClickListener(singleSelectListener);
        rbLiquids.setOnClickListener(singleSelectListener);
        rbCreams.setOnClickListener(singleSelectListener);
        rbInhalers.setOnClickListener(singleSelectListener);
        rbInjections.setOnClickListener(singleSelectListener);

        // Back arrow: go back to previous activity (Dashboard)
        backArrow.setOnClickListener(v -> {
            startActivity(new Intent(MedicineType.this, Dashboard.class));
            finish();
        });

        // Continue button -> open AddMedicine, passing selected type(s)
        btnSubmit.setOnClickListener(v -> {
            String selectedType = "Unknown";
            if (rbTablets.isChecked()) selectedType = "Tablets";
            else if (rbLiquids.isChecked()) selectedType = "Liquids";
            else if (rbCreams.isChecked()) selectedType = "Creams";
            else if (rbInhalers.isChecked()) selectedType = "Inhalers";
            else if (rbInjections.isChecked()) selectedType = "Injections";

            if ("Unknown".equals(selectedType)) {
                Toast.makeText(MedicineType.this, "Please select a medicine type", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(MedicineType.this, AddMedicine.class);
            intent.putExtra("medicineType", selectedType);
            startActivity(intent);
        });
    }
}
