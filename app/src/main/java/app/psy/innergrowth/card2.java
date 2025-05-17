package app.psy.innergrowth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class card2 extends AppCompatActivity {

    private EditText etSowingDate, etGrowthDays;
    private Button btnCalculate;
    private TextView tvMaturityDate, tvDaysPassed, tvDaysLeft, tvGrowthPercent;
    private LinearLayout llHistory;
    private DatabaseReference historyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card2);

        etSowingDate = findViewById(R.id.etSowingDate);
        etGrowthDays = findViewById(R.id.etGrowthDays);
        btnCalculate = findViewById(R.id.btnCalculate);
        tvMaturityDate = findViewById(R.id.tvMaturityDate);
        tvDaysPassed = findViewById(R.id.tvDaysPassed);
        tvDaysLeft = findViewById(R.id.tvDaysLeft);
        tvGrowthPercent = findViewById(R.id.tvGrowthPercent);
        llHistory = findViewById(R.id.llHistory);

        historyRef = FirebaseDatabase.getInstance().getReference("growthHistory");

        btnCalculate.setOnClickListener(v -> calculateGrowthInfo());

        etSowingDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(card2.this, (view, selectedYear, selectedMonth, selectedDay) -> {
                selectedMonth++;
                String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay);
                etSowingDate.setText(dateStr);
            }, year, month, day).show();
        });

        loadHistory();
    }

    private void calculateGrowthInfo() {
        String sowingDateStr = etSowingDate.getText().toString().trim();
        String growthDaysStr = etGrowthDays.getText().toString().trim();

        if (TextUtils.isEmpty(sowingDateStr) || TextUtils.isEmpty(growthDaysStr)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date sowingDate = sdf.parse(sowingDateStr);
            int growthDays = Integer.parseInt(growthDaysStr);

            Calendar cal = Calendar.getInstance();
            cal.setTime(sowingDate);
            cal.add(Calendar.DAY_OF_YEAR, growthDays);
            Date maturityDate = cal.getTime();

            long today = System.currentTimeMillis();
            long daysPassed = (today - sowingDate.getTime()) / (1000 * 60 * 60 * 24);
            long daysLeft = (maturityDate.getTime() - today) / (1000 * 60 * 60 * 24);
            double growthPercent = (double) daysPassed / growthDays * 100;
            if (growthPercent > 100) growthPercent = 100;

            tvMaturityDate.setText("📅 Estimated Maturity Date: " + sdf.format(maturityDate));
            tvDaysPassed.setText("⏳ Days Passed: " + daysPassed);
            tvDaysLeft.setText("📉 Days Left: " + Math.max(daysLeft, 0));
            tvGrowthPercent.setText("📊 Growth Percentage: " + String.format(Locale.getDefault(), "%.1f", growthPercent) + "%");

            // Save result to Firebase
            String result = "Sowing Date: " + sowingDateStr + ", Growth Days: " + growthDays + ", Maturity Date: " + sdf.format(maturityDate);
            saveToFirebase(result);

            // Show growth chart
            LineChart lineChart = findViewById(R.id.lineChart);
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i <= daysPassed && i <= growthDays; i++) {
                float percent = (float) i / growthDays * 100;
                if (percent > 100) percent = 100;
                entries.add(new Entry(i, percent));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Growth Progress");
            dataSet.setColor(Color.GREEN);
            dataSet.setValueTextColor(Color.BLACK);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.invalidate();

        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Use yyyy-MM-dd", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToFirebase(String result) {
        String id = historyRef.push().getKey();
        if (id != null) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", id);
            item.put("result", result);
            historyRef.child(id).setValue(item);
        }
    }

    private void loadHistory() {
        llHistory.removeAllViews();
        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                llHistory.removeAllViews();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    String id = itemSnap.child("id").getValue(String.class);
                    String result = itemSnap.child("result").getValue(String.class);
                    if (id != null && result != null) {
                        addHistoryItem(id, result);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(card2.this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addHistoryItem(String id, String result) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv = new TextView(this);
        tv.setText(result);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        layout.addView(tv);

        Button btnDelete = new Button(this);
        btnDelete.setText("Delete");
        btnDelete.setOnClickListener(v -> deleteItem(id));
        layout.addView(btnDelete);

        llHistory.addView(layout);
    }

    private void deleteItem(String id) {
        historyRef.child(id).removeValue();
    }
}
