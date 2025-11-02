package com.example.meditrackapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryMedicineAdapter extends RecyclerView.Adapter<HistoryMedicineAdapter.VH> {

    public static class Item {
        public Medicine medicine;
        public int takenCount;
        public int skippedCount;
        public int totalDoses;
    }

    private final List<Item> items = new ArrayList<>();

    public void setItems(List<Item> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_medicine, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item it = items.get(position);
        Medicine m = it.medicine;
        h.tvMedName.setText(m.getName() != null ? m.getName() : "--");

        StringBuilder meta = new StringBuilder();
        if (m.getDosage() != null && !m.getDosage().isEmpty()) meta.append(m.getDosage());
        if (m.getMedicineType() != null && !m.getMedicineType().isEmpty()) {
            if (meta.length() > 0) meta.append(" • ");
            meta.append(m.getMedicineType());
        }
        h.tvMedMeta.setText(meta.toString());

        String progress = "Progress " + it.takenCount + "/" + (it.totalDoses > 0 ? it.totalDoses : 0);
        h.tvProgress.setText(progress);

        String freq = (m.getFrequency() != null && !m.getFrequency().isEmpty()) ? m.getFrequency() : "Once Daily";
        h.tvFrequency.setText(freq);

        String times;
        if (m.getReminderTimes() != null && !m.getReminderTimes().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < m.getReminderTimes().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(m.getReminderTimes().get(i));
            }
            times = sb.toString();
        } else {
            times = "--";
        }
        h.tvTimes.setText(times);

        String duration = (m.getStartDate() != null ? m.getStartDate() : "-") + " - " + (m.getEndDate() != null ? m.getEndDate() : "-");
        h.tvDuration.setText(duration);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMedName, tvMedMeta, tvProgress, tvFrequency, tvTimes, tvDuration;
        VH(@NonNull View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvMedMeta = itemView.findViewById(R.id.tvMedMeta);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvTimes = itemView.findViewById(R.id.tvTimes);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}
