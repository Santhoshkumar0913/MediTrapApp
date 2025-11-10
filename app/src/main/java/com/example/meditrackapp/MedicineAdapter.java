package com.example.meditrackapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private final List<Medicine> items;

    public MedicineAdapter(List<Medicine> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine m = items.get(position);
        holder.tvMedicineName.setText(m.getName());
        
        // Handle reminder times - show first time or "Multiple times" if more than one
        String timeDisplay;
        if (m.getReminderTimes() != null && !m.getReminderTimes().isEmpty()) {
            if (m.getReminderTimes().size() == 1) {
                timeDisplay = m.getReminderTimes().get(0);
            } else {
                timeDisplay = m.getReminderTimes().size() + " times";
            }
        } else {
            timeDisplay = "No reminders";
        }
        
        holder.tvMedicineMeta.setText(formatDosageForType(m) + " • " + timeDisplay);
        String freq = (m.getFrequency() != null && !m.getFrequency().isEmpty()) ? m.getFrequency() : "Daily";
        holder.tvFrequency.setText(freq);
        holder.imgStatus.setColorFilter(m.isTaken() ? 0xFF10B981 : 0xFF9CA3AF); // green if taken
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    private String formatDosageForType(Medicine med) {
        String dosage = med.getDosage() != null ? med.getDosage() : "";
        String type = med.getMedicineType() != null ? med.getMedicineType().toLowerCase() : "";
        
        // If dosage already includes a unit that's not pill(s), keep it
        if (dosage.matches(".*(ml|g|mg|puff\\(s\\)|unit\\(s\\)|tablet\\(s\\)).*")) {
            return dosage;
        }
        
        // Extract quantity from "X pill(s)" format or plain number
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+)\\s*pill\\(s\\)$").matcher(dosage.trim());
        String qty = null;
        if (m.find()) {
            qty = m.group(1);
        }
        if (qty == null && dosage.matches("^\\d+$")) {
            qty = dosage;
        }
        if (qty == null) {
            return dosage;
        }
        
        // Determine unit based on medicine type
        String unit = "pill(s)";
        if (type.contains("liquid")) {
            unit = "ml";
        } else if (type.contains("inhaler")) {
            unit = "puff(s)";
        } else if (type.contains("cream")) {
            unit = "g";
        } else if (type.contains("injection")) {
            unit = "unit(s)";
        } else if (type.contains("tablet")) {
            unit = "tablet(s)";
        }
        
        return qty + " " + unit;
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        ImageView imgStatus;
        TextView tvMedicineName;
        TextView tvMedicineMeta;
        TextView tvFrequency;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvMedicineMeta = itemView.findViewById(R.id.tvMedicineMeta);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
        }
    }
}