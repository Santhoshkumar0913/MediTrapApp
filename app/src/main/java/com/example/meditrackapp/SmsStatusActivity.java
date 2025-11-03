package com.example.meditrackapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SmsStatusActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private android.widget.Button btnClearSms;
    private final List<SmsStatusItem> items = new ArrayList<>();
    private SmsStatusAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_status);

        android.widget.ImageView backArrow = findViewById(R.id.backArrowDashboard);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recyclerSms);
        progressBar = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SmsStatusAdapter(items);
        recyclerView.setAdapter(adapter);

        btnClearSms = findViewById(R.id.btnClearSms);
        if (btnClearSms != null) {
            btnClearSms.setOnClickListener(v -> clearLogs());
        }

        loadLogs();
    }

    private void loadLogs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showEmpty();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase
                .getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com")
                .getReference("smsLogs");
        // Filter client-side by userId
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String userId = String.valueOf(child.child("userId").getValue());
                    if (!user.getUid().equals(userId)) continue;
                    String familyPhone = String.valueOf(child.child("familyPhone").getValue());
                    String medicineName = String.valueOf(child.child("medicineName").getValue());
                    String dosage = String.valueOf(child.child("dosage").getValue());
                    String scheduledTime = String.valueOf(child.child("scheduledTime").getValue());
                    String message = String.valueOf(child.child("message").getValue());
                    long ts = 0L;
                    try { ts = child.child("timestamp").getValue(Long.class); } catch (Exception ignored) {}
                    items.add(new SmsStatusItem(familyPhone, medicineName, dosage, scheduledTime, message, ts));
                }
                // Sort newest first
                items.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                showEmpty();
            }
        });
    }

    private void clearLogs() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase
                .getInstance("https://meditrack-b0746-default-rtdb.firebaseio.com")
                .getReference("smsLogs");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> keys = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String userId = String.valueOf(child.child("userId").getValue());
                    if (user.getUid().equals(userId)) {
                        keys.add(child.getKey());
                    }
                }
                for (String k : keys) {
                    ref.child(k).removeValue();
                }
                items.clear();
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    static class SmsStatusItem {
        final String familyPhone;
        final String medicineName;
        final String dosage;
        final String scheduledTime;
        final String message;
        final long timestamp;

        SmsStatusItem(String familyPhone, String medicineName, String dosage,
                       String scheduledTime, String message, long timestamp) {
            this.familyPhone = familyPhone;
            this.medicineName = medicineName;
            this.dosage = dosage;
            this.scheduledTime = scheduledTime;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    static class SmsStatusAdapter extends RecyclerView.Adapter<SmsStatusViewHolder> {
        private final List<SmsStatusItem> data;

        SmsStatusAdapter(List<SmsStatusItem> data) { this.data = data; }

        @Override
        public SmsStatusViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sms_status, parent, false);
            return new SmsStatusViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SmsStatusViewHolder holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() { return data.size(); }
    }

    static class SmsStatusViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvTimestamp;

        SmsStatusViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(SmsStatusItem item) {
            String medPart = item.dosage == null || item.dosage.isEmpty()
                    ? item.medicineName
                    : item.medicineName + " (" + item.dosage + ")";
            tvTitle.setText(medPart + " • " + item.scheduledTime);
            tvSubtitle.setText("Sent to: " + item.familyPhone + "\n" + item.message);
            String date = new SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
                    .format(new Date(item.timestamp));
            tvTimestamp.setText(date);
        }
    }

    private void showEmpty() {
        recyclerView.setAdapter(adapter);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}


