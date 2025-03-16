package app.psy.innergrowth;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class card2 extends AppCompatActivity {

    private EditText etLandSize;
    private Spinner spCropType, spWeather;
    private Button btnCalculate;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card2);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_search);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_search) {
                return true;
            } else if (itemId == R.id.bottom_settings) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

        etLandSize = findViewById(R.id.landSizeInput);
        spCropType = findViewById(R.id.cropTypeSpinner);
        spWeather = findViewById(R.id.weatherConditionSpinner);
        btnCalculate = findViewById(R.id.calculateButton);
        tvResult = findViewById(R.id.resultText);

        // Set up crop type options
        String[] crops = {"Barley", "Corn", "Potato", "Tomato"};
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, crops);
        spCropType.setAdapter(cropAdapter);

        // Set up weather options
        String[] weatherConditions = {"Sunny", "Cloudy", "Rainy", "Stormy", "Snowy", "Foggy", "Windy"};
        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, weatherConditions);
        spWeather.setAdapter(weatherAdapter);

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateRequirements();
            }
        });
    }

    private void calculateRequirements() {
        String landSizeStr = etLandSize.getText().toString();
        if (landSizeStr.isEmpty()) {
            tvResult.setText("Please enter the land size");
            return;
        }

        double landSize = Double.parseDouble(landSizeStr);
        String selectedCrop = spCropType.getSelectedItem().toString();
        String selectedWeather = spWeather.getSelectedItem().toString();

        double seedRate = 0;
        double nitrogen = 0, phosphorus = 0, potassium = 0;
        double waterRequirement = 1.0;

        switch (selectedCrop) {
            case "Barley":
                seedRate = 150;
                nitrogen = 50;
                phosphorus = 40;
                potassium = 30;
                break;
            case "Corn":
                seedRate = 25;
                nitrogen = 100;
                phosphorus = 60;
                potassium = 40;
                break;
            case "Potato":
                seedRate = 2000;
                nitrogen = 120;
                phosphorus = 90;
                potassium = 80;
                break;
            case "Tomato":
                seedRate = 500;
                nitrogen = 70;
                phosphorus = 50;
                potassium = 45;
                break;
        }

        switch (selectedWeather) {
            case "Sunny":
                waterRequirement = 1.2;
                break;
            case "Cloudy":
                waterRequirement = 1.0;
                break;
            case "Rainy":
                waterRequirement = 0.8;
                break;
            case "Stormy":
                waterRequirement = 1.5;
                break;
            case "Snowy":
                waterRequirement = 0.5;
                break;
            case "Foggy":
                waterRequirement = 0.9;
                break;
            case "Windy":
                waterRequirement = 1.3;
                break;
        }

        double requiredSeeds = (landSize * seedRate) / 10000;
        double requiredNitrogen = (landSize * nitrogen) / 10000;
        double requiredPhosphorus = (landSize * phosphorus) / 10000;
        double requiredPotassium = (landSize * potassium) / 10000;
        double requiredWater = (landSize * 5000 * waterRequirement) / 10000;

        String result = "🌱 Selected Crop: " + selectedCrop + "\n" +
                "☁️ Weather: " + selectedWeather + "\n" +
                "🟢 Required Seeds: " + String.format("%.2f", requiredSeeds) + " kg\n" +
                "💥 Nitrogen (N): " + String.format("%.2f", requiredNitrogen) + " kg\n" +
                "💡 Phosphorus (P): " + String.format("%.2f", requiredPhosphorus) + " kg\n" +
                "💧 Potassium (K): " + String.format("%.2f", requiredPotassium) + " kg\n" +
                "🌧️ Water Required: " + String.format("%.2f", requiredWater) + " liters";

        tvResult.setText(result);
    }
}
