package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class profile_settings extends AppCompatActivity {

    private EditText etName, etAge, etFamilyPhone, etPersonalPhone, etEmail;
    private Spinner spinnerGender;
    private Button btnLogout;
    private ImageView backArrow;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etFamilyPhone = findViewById(R.id.etFamilyPhone);
        etPersonalPhone = findViewById(R.id.etPersonalPhone);
        etEmail = findViewById(R.id.etEmail);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnLogout = findViewById(R.id.btnLogout);
        backArrow = findViewById(R.id.backArrow);

        // Setup spinner
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        loadUserData();

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // Navigate to login activity and clear back stack
            Intent intent = new Intent(profile_settings.this, login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        backArrow.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(uid).get().addOnSuccessListener(this::populateProfile);
        }
    }

    private void populateProfile(DocumentSnapshot doc) {
        if (doc.exists()) {
            etName.setText(doc.getString("name"));
            etAge.setText(doc.getString("age"));
            etFamilyPhone.setText(doc.getString("familyPhone"));
            etPersonalPhone.setText(doc.getString("personalPhone"));
            etEmail.setText(doc.getString("email"));

            String gender = doc.getString("gender");
            if (gender != null) {
                int position = ((ArrayAdapter) spinnerGender.getAdapter()).getPosition(gender);
                spinnerGender.setSelection(position);
            }
        }
    }
}