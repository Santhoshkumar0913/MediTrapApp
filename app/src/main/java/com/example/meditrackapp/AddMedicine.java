package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class AddMedicine extends AppCompatActivity {

    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        backArrow = findViewById(R.id.backArrow);

        backArrow.setOnClickListener(v -> {
            startActivity(new Intent(AddMedicine.this, MedicineType.class));
        });


    }
}