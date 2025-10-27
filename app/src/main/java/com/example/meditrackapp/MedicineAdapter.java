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
        
        holder.tvMedicineMeta.setText(m.getDosage() + " • " + timeDisplay);
        String freq = (m.getFrequency() != null && !m.getFrequency().isEmpty()) ? m.getFrequency() : "Daily";
        holder.tvFrequency.setText(freq);
        holder.imgStatus.setColorFilter(m.isTaken() ? 0xFF10B981 : 0xFF9CA3AF); // green if taken
    }

    @Override
    public int getItemCount() {
        return items.size();
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