package com.example.meditrackapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CustomDaysFragment extends Fragment {

    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private Spinner spinnerFrequency;
    private Button btnContinue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom_days, container, false);
        
        // Initialize views
        initializeViews(view);
        
        // Setup frequency spinner
        setupFrequencySpinner();
        
        // Setup continue button
        btnContinue.setOnClickListener(v -> {
            // Handle continue button click
            // Get selected days and frequency
            // Pass data back to parent activity
        });
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Initialize checkboxes
        cbMonday = view.findViewById(R.id.cbMonday);
        cbTuesday = view.findViewById(R.id.cbTuesday);
        cbWednesday = view.findViewById(R.id.cbWednesday);
        cbThursday = view.findViewById(R.id.cbThursday);
        cbFriday = view.findViewById(R.id.cbFriday);
        cbSaturday = view.findViewById(R.id.cbSaturday);
        cbSunday = view.findViewById(R.id.cbSunday);
        
        // Initialize spinner and button
        spinnerFrequency = view.findViewById(R.id.spinnerFrequency);
        btnContinue = view.findViewById(R.id.btnContinue);
    }
    
    private void setupFrequencySpinner() {
        // Create frequency options
        String[] frequencyOptions = {"Once daily", "Twice daily", "3 times daily"};
        
        // Create and set adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                frequencyOptions
        );
        
        // Set dropdown style
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Set adapter to spinner
        spinnerFrequency.setAdapter(adapter);
    }
    
    // Method to get selected days
    public boolean[] getSelectedDays() {
        boolean[] selectedDays = new boolean[7];
        selectedDays[0] = cbMonday.isChecked();
        selectedDays[1] = cbTuesday.isChecked();
        selectedDays[2] = cbWednesday.isChecked();
        selectedDays[3] = cbThursday.isChecked();
        selectedDays[4] = cbFriday.isChecked();
        selectedDays[5] = cbSaturday.isChecked();
        selectedDays[6] = cbSunday.isChecked();
        return selectedDays;
    }
    
    // Method to get selected frequency
    public int getFrequencyValue() {
        int position = spinnerFrequency.getSelectedItemPosition();
        // Convert position to actual frequency value (1, 2, or 3 times daily)
        return position + 1;
    }
}
