package com.example.meditrackapp;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MedicineListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerMedicines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load user-added medicines
        java.util.List<Medicine> data = MedicineRepository.getAll(this);
        MedicineAdapter adapter = new MedicineAdapter(data);
        recyclerView.setAdapter(adapter);
    }
}