package com.example.meditrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.FirebaseDatabase;

public class Settings extends BaseActivity {

    private EditText etName, etAge, etFamilyPhone, etPersonalPhone, etEmail;
    private Spinner spinnerGender;
    private Button btnLogout, btnUpdateProfile;
    private ImageView backArrow;
    private SwitchCompat switchReminder;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etFamilyPhone = findViewById(R.id.etFamilyPhone);
        etPersonalPhone = findViewById(R.id.etPersonalPhone);
        etEmail = findViewById(R.id.etEmail);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnLogout = findViewById(R.id.btnLogout);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        backArrow = findViewById(R.id.backArrow);
        switchReminder = findViewById(R.id.switchReminder);

        // Setup spinner
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        loadUserData();

        // Load reminder ringtone preference (default is true)
        boolean reminderEnabled = prefs.getBoolean("reminder_ringtone_enabled", true);
        switchReminder.setChecked(reminderEnabled);

        // Save preference when switch is toggled (no toast, will show on update)
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("reminder_ringtone_enabled", isChecked).apply();
        });

        btnUpdateProfile.setOnClickListener(v -> {
            Toast.makeText(Settings.this, "Updated", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // Navigate to login activity and clear back stack
            Intent intent = new Intent(Settings.this, login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        backArrow.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            populateProfile(doc);
                        } else {
                            loadFromRealtime(uid);
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadFromRealtime(uid);
                    });
        }
    }

    private void loadFromRealtime(String uid) {
        FirebaseDatabase.getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com")
                .getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            populateFromUser(user);
                        }
                        setAuthEmailFallback();
                    } else {
                        setAuthEmailFallback();
                        Toast.makeText(Settings.this, "No profile found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setAuthEmailFallback();
                });
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
            setAuthEmailFallback();
        }
    }

    private void populateFromUser(User user) {
        etName.setText(user.getName());
        etAge.setText(user.getAge());
        etFamilyPhone.setText(user.getFamilyPhone());
        etPersonalPhone.setText(user.getPersonalPhone());
        etEmail.setText(user.getEmail());

        if (user.getGender() != null) {
            int position = ((ArrayAdapter) spinnerGender.getAdapter()).getPosition(user.getGender());
            spinnerGender.setSelection(position);
        }
    }

    private void setAuthEmailFallback() {
        if (etEmail.getText().toString().trim().isEmpty() && mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
            etEmail.setText(mAuth.getCurrentUser().getEmail());
        }
    }
}