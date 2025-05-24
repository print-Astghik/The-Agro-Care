package app.psy.innergrowth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class test31 extends AppCompatActivity {

    private static final String CHANNEL_ID = "my_channel";
    private static final int NOTIFICATION_REQUEST_CODE = 100;

    private EditText intervalDaysEditText;
    private TextView selectedDateTimeText;
    private TextView notificationStatusText;

    private int selectedHour = 9;
    private int selectedMinute = 0;
    private int selectedYear, selectedMonth, selectedDay;

    private boolean isNotificationOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test31);

        intervalDaysEditText = findViewById(R.id.interval_days_edit_text);
        selectedDateTimeText = findViewById(R.id.selected_datetime_text);
        notificationStatusText = findViewById(R.id.notification_status_text);

        // Set default date to today
        Calendar now = Calendar.getInstance();
        selectedYear = now.get(Calendar.YEAR);
        selectedMonth = now.get(Calendar.MONTH);
        selectedDay = now.get(Calendar.DAY_OF_MONTH);

        updateDateTimeText();
        createNotificationChannel();
    }

    public void pickTime(View view) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker timePicker, int hourOfDay, int minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateDateTimeText();
                }, selectedHour, selectedMinute, true);
        timePickerDialog.show();
    }

    public void pickDate(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view1, int year, int month, int dayOfMonth) -> {
                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = dayOfMonth;
                    updateDateTimeText();
                }, selectedYear, selectedMonth, selectedDay);
        datePickerDialog.show();
    }

    private void updateDateTimeText() {
        String formatted = String.format("Selected date and time: %04d-%02d-%02d %02d:%02d",
                selectedYear, selectedMonth + 1, selectedDay, selectedHour, selectedMinute);
        selectedDateTimeText.setText(formatted);
    }

    public void createNotification(View view) {
        String intervalText = intervalDaysEditText.getText().toString().trim();
        if (intervalText.isEmpty()) {
            Toast.makeText(this, "Please enter interval days", Toast.LENGTH_SHORT).show();
            return;
        }

        int intervalDays = Integer.parseInt(intervalText);
        long repeatIntervalMillis = intervalDays * 24L * 60L * 60L * 1000L;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "Selected time is in the past. Please select a future time.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                repeatIntervalMillis,
                pendingIntent
        );

        isNotificationOn = true;
        notificationStatusText.setText("Notification is ON ✅");
        Toast.makeText(this, "Notification created!", Toast.LENGTH_SHORT).show();
    }

    public void stopNotification(View view) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        isNotificationOn = false;
        notificationStatusText.setText("Notification is OFF ❌");
        Toast.makeText(this, "Notification stopped.", Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Channel for InnerGrowth reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
