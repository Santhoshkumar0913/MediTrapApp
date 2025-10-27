package com.example.meditrackapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MedicineListActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private MedicineAdapter adapter;
    private FirebaseMedicineHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_list);

        // Check if user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerMedicines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseHelper = new FirebaseMedicineHelper();
        loadMedicinesFromFirebase();
    }

    private void loadMedicinesFromFirebase() {
        firebaseHelper.getAllMedicines(new FirebaseMedicineHelper.OnMedicinesLoadedListener() {
            @Override
            public void onMedicinesLoaded(List<Medicine> medicines) {
                // Only show medicines for the current user
                List<Medicine> userMedicines = new ArrayList<>();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                
                if (currentUser != null) {
                    for (Medicine medicine : medicines) {
                        if (currentUser.getUid().equals(medicine.getUserId())) {
                            userMedicines.add(medicine);
                        }
                    }
                }
                
                adapter = new MedicineAdapter(userMedicines);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MedicineListActivity.this, "Error loading medicines: " + error, Toast.LENGTH_SHORT).show();
                // Fallback to local storage if Firebase fails
                List<Medicine> localMedicines = MedicineRepository.getAll(MedicineListActivity.this);
                adapter = new MedicineAdapter(localMedicines);
                recyclerView.setAdapter(adapter);
            }
        });
    }
}