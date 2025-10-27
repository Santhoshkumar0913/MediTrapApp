package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

public class MedicineType extends BaseActivity {

    private static final String TAG = "MedicineType";
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

        // Continue button -> open AddMedicine, passing selected type(s)
        btnSubmit.setOnClickListener(v -> {
            StringBuilder typeBuilder = new StringBuilder();
            if (cbTablets.isChecked()) typeBuilder.append("Tablets");
            if (cbLiquids.isChecked()) {
                if (typeBuilder.length() > 0) typeBuilder.append(", ");
                typeBuilder.append("Liquids");
            }
            if (cbCreams.isChecked()) {
                if (typeBuilder.length() > 0) typeBuilder.append(", ");
                typeBuilder.append("Creams");
            }
            if (cbInhalers.isChecked()) {
                if (typeBuilder.length() > 0) typeBuilder.append(", ");
                typeBuilder.append("Inhalers");
            }
            if (cbInjections.isChecked()) {
                if (typeBuilder.length() > 0) typeBuilder.append(", ");
                typeBuilder.append("Injections");
            }

            String selectedType = typeBuilder.length() > 0 ? typeBuilder.toString() : "Unknown";

            Intent intent = new Intent(MedicineType.this, AddMedicine.class);
            intent.putExtra("medicineType", selectedType);
            startActivity(intent);
        });
    }
}
