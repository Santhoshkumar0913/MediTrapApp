package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

public class login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignup;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        // 🔹 Login button
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(login.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Ensure user profile exists in Realtime Database under `users/{uid}`
                            String uid = mAuth.getCurrentUser().getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com")
                                    .getReference("users")
                                    .child(uid);

                            userRef.get().addOnSuccessListener(snapshot -> {
                                if (!snapshot.exists()) {
                                    // Try to read from Firestore `Users` for full profile
                                    FirebaseFirestore.getInstance()
                                            .collection("Users")
                                            .document(uid)
                                            .get()
                                            .addOnSuccessListener(doc -> {
                                                if (doc.exists()) {
                                                    User user = new User(
                                                            uid,
                                                            doc.getString("name"),
                                                            doc.getString("age"),
                                                            doc.getString("gender"),
                                                            doc.getString("familyPhone"),
                                                            doc.getString("personalPhone"),
                                                            doc.getString("email")
                                                    );
                                                    userRef.setValue(user);
                                                } else {
                                                    // Minimal backfill if Firestore doc is missing
                                                    java.util.Map<String, Object> minimal = new java.util.HashMap<>();
                                                    minimal.put("name", mAuth.getCurrentUser().getDisplayName());
                                                    minimal.put("email", mAuth.getCurrentUser().getEmail());
                                                    userRef.setValue(minimal);
                                                }
                                            });
                                }
                            });

                            Toast.makeText(login.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(login.this, Dashboard.class); //  Go to Dashboard
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Close login activity

                        } else {
                            Toast.makeText(login.this, "Login Failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 🔹 Signup button → Go to signup page
        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(login.this, signup.class));
        });
    }
}
