package com.example.meditrackapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpandableMedicineAdapter extends RecyclerView.Adapter<ExpandableMedicineAdapter.MedicineViewHolder> {

    private final List<Medicine> items;
    private int expandedPosition = -1;

    public ExpandableMedicineAdapter(List<Medicine> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_expandable, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine m = items.get(position);
        boolean isExpanded = position == expandedPosition;
        
        holder.tvMedicineName.setText(m.getName());
        
        // Handle reminder times
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
        
        if (m.getWhenToTake() != null && !m.getWhenToTake().isEmpty()) {
            holder.tvMedicineMeta.setText(m.getWhenToTake() + " • " + m.getDosage() + " • " + timeDisplay);
        } else {
            holder.tvMedicineMeta.setText(m.getDosage() + " • " + timeDisplay);
        }
        
        // Set medicine type icon
        holder.imgMedicineType.setImageResource(getMedicineTypeIcon(m));
        holder.imgMedicineType.setColorFilter(null);
        
        // Set expand arrow
        holder.imgExpandArrow.setImageResource(isExpanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
        
        // Set details
        holder.tvMedicineType.setText(m.getMedicineType() != null ? m.getMedicineType() : "Unknown");
        holder.tvFrequency.setText(m.getFrequency() != null && !m.getFrequency().isEmpty() ? m.getFrequency() : "Once a day");
        holder.tvDuration.setText(getSelectedDaysText(m));
        holder.tvStartDate.setText(m.getStartDate() != null && !m.getStartDate().isEmpty() ? m.getStartDate() : "Not set");
        holder.tvEndDate.setText(m.getEndDate() != null && !m.getEndDate().isEmpty() ? m.getEndDate() : "Not set");

        // Bind times list in expanded section
        if (m.getReminderTimes() != null && !m.getReminderTimes().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < m.getReminderTimes().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(m.getReminderTimes().get(i));
            }
            holder.tvTimes.setText(sb.toString());
        } else {
            holder.tvTimes.setText("--");
        }
        
        // Toggle expanded state
        holder.detailsSection.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        
        // Add click listener to header
        holder.headerSection.setOnClickListener(v -> {
            int previousPosition = expandedPosition;
            expandedPosition = isExpanded ? -1 : position;
            if (previousPosition != -1 && previousPosition != position) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int getMedicineTypeIcon(Medicine m) {
        if (m.getMedicineType() != null) {
            String type = m.getMedicineType().toLowerCase();
            
            if (type.contains("tablet")) {
                return R.drawable.ic_tablet;
            } else if (type.contains("liquid")) {
                return R.drawable.ic_liquid;
            } else if (type.contains("cream")) {
                return R.drawable.ic_cream;
            } else if (type.contains("inhaler")) {
                return R.drawable.ic_inhaler;
            } else if (type.contains("injection")) {
                return R.drawable.ic_injection;
            }
        }
        
        return R.drawable.ic_tablet;
    }
    
    private String getSelectedDaysText(Medicine m) {
        if (m.getCustomDays() != null && !m.getCustomDays().isEmpty()) {
            return String.join(", ", m.getCustomDays());
        }
        return "Not specified";
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMedicineType;
        ImageView imgExpandArrow;
        TextView tvMedicineName;
        TextView tvMedicineMeta;
        TextView tvMedicineType;
        TextView tvFrequency;
        TextView tvDuration;
        TextView tvStartDate;
        TextView tvEndDate;
        TextView tvTimes;
        View detailsSection;
        View headerSection;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMedicineType = itemView.findViewById(R.id.imgMedicineType);
            imgExpandArrow = itemView.findViewById(R.id.imgExpandArrow);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvMedicineMeta = itemView.findViewById(R.id.tvMedicineMeta);
            tvMedicineType = itemView.findViewById(R.id.tvMedicineType);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvTimes = itemView.findViewById(R.id.tvTimes);
            detailsSection = itemView.findViewById(R.id.detailsSection);
            headerSection = itemView.findViewById(R.id.headerSection);
        }
    }
}
