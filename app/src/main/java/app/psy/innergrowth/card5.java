package app.psy.innergrowth;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class card5 extends AppCompatActivity {
    private TextView locationText, weatherText, lastWateredText, nextWateringText;
    private Button logWateringButton;
    private LineChart tempChart, humidityChart;
    private ListView wateringHistoryList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> wateringHistory;

    private DatabaseReference databaseReference;
    private FusedLocationProviderClient fusedLocationClient;
    private static final String CHANNEL_ID = "WateringReminder";
    private double temperature = 25, humidity = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card5);

        locationText = findViewById(R.id.locationText);
        weatherText = findViewById(R.id.weatherText);
        lastWateredText = findViewById(R.id.lastWateredText);
        nextWateringText = findViewById(R.id.nextWateringText);
        logWateringButton = findViewById(R.id.logWateringButton);
        tempChart = findViewById(R.id.tempChart);
        humidityChart = findViewById(R.id.humidityChart);
        wateringHistoryList = findViewById(R.id.wateringHistoryList);

        databaseReference = FirebaseDatabase.getInstance().getReference("WateringHistory");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        wateringHistory = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wateringHistory);
        wateringHistoryList.setAdapter(adapter);

        logWateringButton.setOnClickListener(v -> logWatering());

        getLocation();
        createNotificationChannel();
        loadWateringHistory();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                locationText.setText("Lat: " + latitude + ", Lon: " + longitude);
                getWeather(latitude, longitude);
            } else {
                locationText.setText("Could not get location");
            }
        });
    }

    private void updateCharts() {

        ArrayList<Entry> tempEntries = new ArrayList<>();
        tempEntries.add(new Entry(0, (float) temperature));
        LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperature");
        tempDataSet.setColor(android.graphics.Color.RED);
        tempDataSet.setValueTextSize(12f);
        tempChart.setData(new LineData(tempDataSet));
        tempChart.invalidate();


        ArrayList<Entry> humidityEntries = new ArrayList<>();
        humidityEntries.add(new Entry(0, (float) humidity));
        LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Humidity");
        humidityDataSet.setColor(android.graphics.Color.BLUE);
        humidityDataSet.setValueTextSize(12f);
        humidityChart.setData(new LineData(humidityDataSet));
        humidityChart.invalidate();
    }


    private void getWeather(double latitude, double longitude) {
        String apiKey = "2b9c951f57d15d32dc24e6838ec72fec";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        temperature = response.getJSONObject("main").getDouble("temp");
                        humidity = response.getJSONObject("main").getDouble("humidity");
                        weatherText.setText("Temp: " + temperature + "°C, Hum: " + humidity + "%");
                        updateCharts();
                    } catch (JSONException e) {
                        weatherText.setText("Error parsing weather data");
                    }
                },
                error -> weatherText.setText("Weather data unavailable"));

        queue.add(jsonObjectRequest);
    }

    private void logWatering() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, day) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + day;

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hour, minute) -> {
                String selectedTime = hour + ":" + (minute < 10 ? "0" + minute : minute);
                String fullWateringInfo = selectedDate + " " + selectedTime;

                lastWateredText.setText("Last watered: " + fullWateringInfo);
                databaseReference.push().setValue(fullWateringInfo);
                calculateNextWatering();

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void calculateNextWatering() {
        int wateringInterval = (temperature > 30) ? 1 :
                (temperature > 20) ? 2 :
                        (humidity < 40) ? 3 : 5;

        Calendar nextWatering = Calendar.getInstance();
        nextWatering.add(Calendar.DAY_OF_MONTH, wateringInterval);

        runOnUiThread(() -> nextWateringText.setText("Next watering: " + nextWatering.get(Calendar.YEAR) + "-" +
                (nextWatering.get(Calendar.MONTH) + 1) + "-" + nextWatering.get(Calendar.DAY_OF_MONTH)));
    }

    private void loadWateringHistory() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                wateringHistory.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Object data = snapshot.getValue();
                    if (data instanceof String) {
                        // Եթե տվյալը String է, ապա ուղղակի ավելացնում ենք
                        wateringHistory.add((String) data);
                    } else if (data instanceof Map) {
                        // Եթե տվյալը HashMap է, ստանում ենք անհրաժեշտ արժեքները
                        Map<String, Object> wateringData = (Map<String, Object>) data;
                        String date = wateringData.get("date") != null ? wateringData.get("date").toString() : "Unknown date";
                        String time = wateringData.get("time") != null ? wateringData.get("time").toString() : "Unknown time";
                        String waterAmount = wateringData.get("waterAmount") != null ? wateringData.get("waterAmount").toString() : "Unknown amount";

                        // Ավելացնում ենք list-ի մեջ, որպեսզի ցուցադրվի
                        wateringHistory.add(date + " " + time + ", " + waterAmount + "L");
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Watering Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
