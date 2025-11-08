package com.example.meditrackapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryMedicine extends BaseActivity {

    private Button btnAll, btnSkipped, btnCompleted;
    private ImageView backArrow;
    private RecyclerView recyclerHistory;
    private HistoryMedicineAdapter adapter;

    private final List<Medicine> allMedicines = new ArrayList<>();
    private final Map<String, StatusCounts> statusByMed = new HashMap<>();

    private static class StatusCounts {
        int taken;
        int skipped;
        int total() { return taken + skipped; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_medicine);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Recycler setup
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryMedicineAdapter();
        recyclerHistory.setAdapter(adapter);

        // Load data
        loadData();
    }

    private void initializeViews() {
        btnAll = findViewById(R.id.btnAll);
        btnSkipped = findViewById(R.id.btnSkipped);
        btnCompleted = findViewById(R.id.btnCompleted);
        backArrow = findViewById(R.id.backArrow);
        recyclerHistory = findViewById(R.id.recyclerHistory);
    }

    private void setupClickListeners() {
        // Back arrow click listener
        backArrow.setOnClickListener(v -> finish());

        // Tab button click listeners
        btnAll.setOnClickListener(v -> setActiveTab(btnAll));
        btnSkipped.setOnClickListener(v -> setActiveTab(btnSkipped));
        btnCompleted.setOnClickListener(v -> setActiveTab(btnCompleted));
    }

    private void setActiveTab(Button activeButton) {
        // Reset all buttons to inactive state
        resetAllTabs();

        // Set the clicked button to active state
        activeButton.setBackgroundTintList(getColorStateList(R.color.purple_500));
        activeButton.setTextColor(getResources().getColor(android.R.color.white, getTheme()));

        // Filter medicine list based on selected tab
        filterMedicineList(activeButton.getId());
    }

    private void resetAllTabs() {
        // Set all buttons to inactive state
        btnAll.setBackgroundTintList(getColorStateList(R.color.inactive_tab_bg));
        btnSkipped.setBackgroundTintList(getColorStateList(R.color.inactive_tab_bg));
        btnCompleted.setBackgroundTintList(getColorStateList(R.color.inactive_tab_bg));

        btnAll.setTextColor(getResources().getColor(R.color.inactive_tab_text, getTheme()));
        btnSkipped.setTextColor(getResources().getColor(R.color.inactive_tab_text, getTheme()));
        btnCompleted.setTextColor(getResources().getColor(R.color.inactive_tab_text, getTheme()));
    }

    private void filterMedicineList(int tabId) {
        List<HistoryMedicineAdapter.Item> items = new ArrayList<>();
        for (Medicine m : allMedicines) {
            StatusCounts sc = statusByMed.getOrDefault(m.getId(), new StatusCounts());
            boolean include = false;
            if (tabId == R.id.btnAll) include = true;
            else if (tabId == R.id.btnSkipped) include = sc.skipped > 0;
            else if (tabId == R.id.btnCompleted) include = sc.taken > 0;

            if (include) {
                HistoryMedicineAdapter.Item it = new HistoryMedicineAdapter.Item();
                it.medicine = m;
                it.takenCount = sc.taken;
                it.skippedCount = sc.skipped;
                it.totalDoses = sc.total();
                items.add(it);
            }
        }
        adapter.setItems(items);
    }

    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load medicines via helper
        FirebaseMedicineHelper helper = new FirebaseMedicineHelper();
        helper.getAllMedicines(new FirebaseMedicineHelper.OnMedicinesLoadedListener() {
            @Override
            public void onMedicinesLoaded(List<Medicine> medicines) {
                allMedicines.clear();
                for (Medicine m : medicines) {
                    if (user.getUid().equals(m.getUserId())) {
                        allMedicines.add(m);
                    }
                }
                // After medicines load, load statuses
                loadStatusesThenBind();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HistoryMedicine.this, "Failed to load medicines: " + error, Toast.LENGTH_SHORT).show();
                allMedicines.clear();
                loadStatusesThenBind();
            }
        });
    }

    private void loadStatusesThenBind() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseDatabase.getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com")
                .getReference("medicineStatus")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        statusByMed.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            String userId = s.child("userId").getValue(String.class);
                            if (user.getUid().equals(userId)) {
                                String medId = s.child("medicineId").getValue(String.class);
                                String status = s.child("status").getValue(String.class);
                                if (medId == null || status == null) continue;
                                StatusCounts sc = statusByMed.get(medId);
                                if (sc == null) { sc = new StatusCounts(); statusByMed.put(medId, sc); }
                                if ("Taken".equalsIgnoreCase(status)) sc.taken++;
                                else if ("Skipped".equalsIgnoreCase(status)) sc.skipped++;
                            }
                        }
                        // Default show All
                        setActiveTab(btnAll);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Show All with empty counts
                        setActiveTab(btnAll);
                    }
                });
    }
}