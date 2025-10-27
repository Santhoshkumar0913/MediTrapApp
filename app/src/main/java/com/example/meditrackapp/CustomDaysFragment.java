package com.example.meditrackapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class CustomDaysFragment extends Fragment {

    private ImageView backArrowCustom;
    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private Spinner spinnerFrequency;
    private Button btnContinue;
    
    private OnCustomDaysSelectedListener listener;

    public interface OnCustomDaysSelectedListener {
        void onCustomDaysSelected(List<String> selectedDays, String frequency);
    }

    public void setOnCustomDaysSelectedListener(OnCustomDaysSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom_days, container, false);
        
        initializeViews(view);
        setupClickListeners();
        setupFrequencySpinner();
        
        return view;
    }

    private void initializeViews(View view) {
        backArrowCustom = view.findViewById(R.id.backArrowCustom);
        cbMonday = view.findViewById(R.id.cbMonday);
        cbTuesday = view.findViewById(R.id.cbTuesday);
        cbWednesday = view.findViewById(R.id.cbWednesday);
        cbThursday = view.findViewById(R.id.cbThursday);
        cbFriday = view.findViewById(R.id.cbFriday);
        cbSaturday = view.findViewById(R.id.cbSaturday);
        cbSunday = view.findViewById(R.id.cbSunday);
        spinnerFrequency = view.findViewById(R.id.spinnerFrequency);
        btnContinue = view.findViewById(R.id.btnContinue);
    }

    private void setupClickListeners() {
        backArrowCustom.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnContinue.setOnClickListener(v -> {
            List<String> selectedDays = getSelectedDays();
            String frequency = spinnerFrequency.getSelectedItem().toString();
            
            if (selectedDays.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one day", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (listener != null) {
                listener.onCustomDaysSelected(selectedDays, frequency);
            }
            
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupFrequencySpinner() {
        String[] frequencyOptions = {"Once a day", "Twice a day", "Three times a day", "Every 4 hours", "Every 6 hours", "Every 8 hours", "Every 12 hours"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, frequencyOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);
    }

    private List<String> getSelectedDays() {
        List<String> selectedDays = new ArrayList<>();
        
        if (cbMonday.isChecked()) selectedDays.add("Monday");
        if (cbTuesday.isChecked()) selectedDays.add("Tuesday");
        if (cbWednesday.isChecked()) selectedDays.add("Wednesday");
        if (cbThursday.isChecked()) selectedDays.add("Thursday");
        if (cbFriday.isChecked()) selectedDays.add("Friday");
        if (cbSaturday.isChecked()) selectedDays.add("Saturday");
        if (cbSunday.isChecked()) selectedDays.add("Sunday");
        
        return selectedDays;
    }
}