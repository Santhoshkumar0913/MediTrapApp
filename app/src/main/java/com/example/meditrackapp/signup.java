package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class signup extends AppCompatActivity {

    private EditText etName, etAge, etFamilyPhone, etPersonalPhone, etEmail, etPassword;
    private Spinner spinnerGender;
    private Button btnCreateAccount;
    private ImageView backArrow;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etFamilyPhone = findViewById(R.id.etFamilyPhone);
        etPersonalPhone = findViewById(R.id.etPersonalPhone);
        etEmail = findViewById(R.id.etEmailSignup);
        etPassword = findViewById(R.id.etPasswordSignup);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        backArrow = findViewById(R.id.backArrow);

        // Gender options
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Back to Login
        backArrow.setOnClickListener(v -> {
            startActivity(new Intent(signup.this, login.class));
            finish();
        });

        // Register new user
        btnCreateAccount.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String familyPhone = etFamilyPhone.getText().toString().trim();
        String personalPhone = etPersonalPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        // Validate all fields
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (age.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }

        if (familyPhone.isEmpty()) {
            etFamilyPhone.setError("Family phone number is required");
            etFamilyPhone.requestFocus();
            return;
        }

        if (familyPhone.length() != 10) {
            etFamilyPhone.setError("Phone number must be 10 digits");
            etFamilyPhone.requestFocus();
            return;
        }

        if (personalPhone.isEmpty()) {
            etPersonalPhone.setError("Personal phone number is required");
            etPersonalPhone.requestFocus();
            return;
        }

        if (personalPhone.length() != 10) {
            etPersonalPhone.setError("Phone number must be 10 digits");
            etPersonalPhone.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            etPassword.setError("Password must have ≥8 chars, 1 upper, 1 lower, 1 number, 1 special char");
            etPassword.requestFocus();
            return;
        }

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();
                User user = new User(uid, name, age, gender, familyPhone, personalPhone, email);

                // 🔹 Save user data to Firestore (async)
                db.collection("Users").document(uid).set(user)
                        .addOnFailureListener(e -> {
                            Toast.makeText(signup.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                // 🔹 Show success message & redirect immediately
                Toast.makeText(signup.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(signup.this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(signup.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Password validation
    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;

        Pattern upper = Pattern.compile("[A-Z]");
        Pattern lower = Pattern.compile("[a-z]");
        Pattern digit = Pattern.compile("[0-9]");
        Pattern special = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

        return upper.matcher(password).find() &&
                lower.matcher(password).find() &&
                digit.matcher(password).find() &&
                special.matcher(password).find();
    }
}
