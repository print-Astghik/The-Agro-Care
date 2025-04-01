package app.psy.innergrowth;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import java.util.*;
import java.io.*;
import java.util.Properties;
import okhttp3.*;
import org.json.*;

public class  card3 extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Marker selectedMarker;
    private LatLng selectedLocation;
    private TextView weatherText, lastWateredText, nextWateringText, locationText;
    private EditText waterAmountInput;
    private Spinner plantTypeSpinner;
    private Button logWateringButton;
    private ListView wateringHistoryList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> wateringHistory, historyKeys;
    private DatabaseReference databaseReference;
    private String selectedPlantType = "Tomato";
    private String API_KEY;

    private Button selectDateButton, selectTimeButton;
    private TextView selectedDateText, selectedTimeText;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card3);
        loadApiKey();

        selectDateButton = findViewById(R.id.selectDateButton);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        selectedDateText = findViewById(R.id.selectedDateText);
        selectedTimeText = findViewById(R.id.selectedTimeText);

        selectDateButton.setOnClickListener(v -> openDatePicker());
        selectTimeButton.setOnClickListener(v -> openTimePicker());




        plantTypeSpinner = findViewById(R.id.plantTypeSpinner);
        waterAmountInput = findViewById(R.id.waterAmountInput);
        weatherText = findViewById(R.id.weatherText);
        lastWateredText = findViewById(R.id.lastWateredText);
        nextWateringText = findViewById(R.id.nextWateringText);
        locationText = findViewById(R.id.locationText);
        logWateringButton = findViewById(R.id.logWateringButton);
        wateringHistoryList = findViewById(R.id.wateringHistoryList);

        databaseReference = FirebaseDatabase.getInstance().getReference("WateringHistory");
        wateringHistory = new ArrayList<>();
        historyKeys = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wateringHistory);
        wateringHistoryList.setAdapter(adapter);



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        setupPlantTypeSpinner();
        logWateringButton.setOnClickListener(v -> logWatering());
        loadWateringHistory();
        createNotificationChannel();
    }

    private void loadApiKey() {
        try {
            Properties properties = new Properties();
            InputStream inputStream = getAssets().open("local.properties");
            properties.load(inputStream);
            API_KEY = properties.getProperty("OPENWEATHERMAP_API_KEY");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWateringHistory() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wateringHistory.clear();
                historyKeys.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Object value = data.getValue();

                    if (value instanceof String) {
                        String record = (String) value;
                        wateringHistory.add(record);
                        historyKeys.add(data.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(card3.this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(latLng -> {
            if (selectedMarker != null) selectedMarker.remove();
            selectedLocation = latLng;
            selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            locationText.setText("Location: " + latLng.latitude + ", " + latLng.longitude);
        });
    }


    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = dayOfMonth;
                    selectedDateText.setText("Last Watered Date: " + dayOfMonth + "/" + (month + 1) + "/" + year);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void openTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    selectedTimeText.setText("Last Watered Time: " + String.format("%02d:%02d", hourOfDay, minute));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }


    private void setupPlantTypeSpinner() {
        String[] plantTypes = {"Tomato", "Cucumber", "Lettuce", "Pepper", "Strawberry"};
        ArrayAdapter<String> plantAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, plantTypes);
        plantTypeSpinner.setAdapter(plantAdapter);
        plantTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPlantType = plantTypes[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }



    private void logWatering() {
        String waterAmount = waterAmountInput.getText().toString().trim();
        if (waterAmount.isEmpty() || selectedLocation == null || selectedYear == 0) {
            Toast.makeText(this, "Please enter water amount, select location, and set last watering time", Toast.LENGTH_SHORT).show();
            return;
        }

        String lastWatered = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear + " " + String.format("%02d:%02d", selectedHour, selectedMinute);
        String record = "Plant: " + selectedPlantType + ", Water: " + waterAmount + "L, Last: " + lastWatered;

        String key = databaseReference.push().getKey();
        databaseReference.child(key).setValue(record);

        lastWateredText.setText("Last Watered: " + lastWatered);
        fetchWeatherAndCalculateNextWatering();
    }


    private void fetchWeatherAndCalculateNextWatering() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + selectedLocation.latitude + "&lon=" + selectedLocation.longitude + "&appid=" + API_KEY + "&units=metric";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(card3.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    double temp = json.getJSONObject("main").getDouble("temp");
                    int humidity = json.getJSONObject("main").getInt("humidity");
                    runOnUiThread(() -> calculateNextWatering(temp, humidity));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void calculateNextWatering(double temp, int humidity) {
        int days = 0;
        String nextTime = "08:00 AM";

        // Բույսի տեսակի համապատասխան ջրման ժամանակը հաշվելու տրամաբանությունը
        if (selectedPlantType.equals("Tomato")) {
            // Թոմատի համար ջրելու օրերը
            days = (temp > 30) ? 1 : (temp < 15) ? 3 : 2;
        } else if (selectedPlantType.equals("Cucumber")) {
            // Կերոսինի համար ջրելու օրերը
            days = (temp > 30) ? 1 : 2;
        } else if (selectedPlantType.equals("Lettuce")) {
            days = (temp > 25) ? 1 : 2;
        } else if (selectedPlantType.equals("Pepper")) {
            days = (temp > 25) ? 1 : (temp < 15) ? 3 : 2;
        } else if (selectedPlantType.equals("Strawberry")) {
            days = (temp > 28) ? 1 : 2;
        }

        // Թեքվող ջրման ժամանակի ցուցադրում
        nextWateringText.setText("Next watering in " + days + " days at " + nextTime);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("WateringReminder", "Watering Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}