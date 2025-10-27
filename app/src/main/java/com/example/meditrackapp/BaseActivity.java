package com.example.meditrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        // Create a parent layout to hold both the content and bottom navigation
        FrameLayout rootLayout = new FrameLayout(this);
        FrameLayout contentContainer = new FrameLayout(this);
        contentContainer.setId(R.id.content_container);
        
        // Inflate the bottom navigation
        bottomNavigationView = (BottomNavigationView) getLayoutInflater().inflate(
                R.layout.layout_bottom_navigation, rootLayout, false);
        
        // Set layout parameters
        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.bottom_nav_height);
        
        FrameLayout.LayoutParams navigationParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        navigationParams.gravity = android.view.Gravity.BOTTOM;
        
        // Add views to root layout
        rootLayout.addView(contentContainer, contentParams);
        rootLayout.addView(bottomNavigationView, navigationParams);
        
        // Set the root layout as content view
        super.setContentView(rootLayout);
        
        // Inflate the actual content into the content container
        getLayoutInflater().inflate(layoutResID, contentContainer, true);
        
        // Setup navigation
        setupBottomNavigation();
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_home) {
            if (!(this instanceof Dashboard)) {
                startActivity(new Intent(this, Dashboard.class));
                return true;
            }
        } else if (itemId == R.id.navigation_add) {
            if (!(this instanceof MedicineType)) {
                startActivity(new Intent(this, MedicineType.class));
                return true;
            }
        } else if (itemId == R.id.navigation_schedule) {
            if (!(this instanceof MedicineSchedule)) {
                startActivity(new Intent(this, MedicineSchedule.class));
                return true;
            }
        } else if (itemId == R.id.navigation_settings) {
            if (!(this instanceof Settings)) {
                startActivity(new Intent(this, Settings.class));
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavigationSelection();
    }
    
    private void updateBottomNavigationSelection() {
        if (this instanceof Dashboard) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        } else if (this instanceof MedicineType) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_add);
        } else if (this instanceof AddMedicine) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_add);
        } else if (this instanceof MedicineSchedule) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_schedule);
        } else if (this instanceof Settings) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_settings);
        }
    }
}