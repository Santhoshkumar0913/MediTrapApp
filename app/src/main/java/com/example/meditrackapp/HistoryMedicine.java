package com.example.meditrackapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class HistoryMedicine extends BaseActivity {

    private Button btnAll, btnActive, btnCompleted;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_medicine);

        // Initialize views
        initializeViews();
        
        // Set up click listeners
        setupClickListeners();
        
        // Set default tab
        setActiveTab(btnAll);
    }
    
    private void initializeViews() {
        btnAll = findViewById(R.id.btnAll);
        btnActive = findViewById(R.id.btnActive);
        btnCompleted = findViewById(R.id.btnCompleted);
        backArrow = findViewById(R.id.backArrow);
    }
    
    private void setupClickListeners() {
        // Back arrow click listener
        backArrow.setOnClickListener(v -> finish());
        
        // Tab button click listeners
        btnAll.setOnClickListener(v -> setActiveTab(btnAll));
        btnActive.setOnClickListener(v -> setActiveTab(btnActive));
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
        btnActive.setBackgroundTintList(getColorStateList(R.color.inactive_tab_bg));
        btnCompleted.setBackgroundTintList(getColorStateList(R.color.inactive_tab_bg));
        
        btnAll.setTextColor(getResources().getColor(R.color.inactive_tab_text, getTheme()));
        btnActive.setTextColor(getResources().getColor(R.color.inactive_tab_text, getTheme()));
        btnCompleted.setTextColor(getResources().getColor(R.color.inactive_tab_text, getTheme()));
    }
    
    private void filterMedicineList(int tabId) {
        // In a real app, this would filter the RecyclerView or ListView
        // based on the selected tab (All, Active, or Completed)
        if (tabId == R.id.btnAll) {
            // Show all medicines
        } else if (tabId == R.id.btnActive) {
            // Show only active medicines
        } else if (tabId == R.id.btnCompleted) {
            // Show only completed medicines
        }
    }
}