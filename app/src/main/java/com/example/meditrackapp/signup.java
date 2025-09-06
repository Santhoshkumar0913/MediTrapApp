package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class signup extends AppCompatActivity {

    ImageView backArrow;
    Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Back Arrow → go back to Login
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to Login screen
                Intent intent = new Intent(signup.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        // Create Account Button → later you can add Firebase/Auth logic
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For now just go back to login after signup
                Intent intent = new Intent(signup.this, login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
