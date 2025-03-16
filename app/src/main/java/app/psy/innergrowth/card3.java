package app.psy.innergrowth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class card3 extends AppCompatActivity {

    private EditText fieldAreaInput;
    private Spinner plantTypeSpinner;
    private Spinner weatherConditionSpinner;
    private Button calculateButton;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card3);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_search);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_home:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_search:
                    return true;
                case R.id.bottom_settings:
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_profile:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
            }
            return false;
        });

        fieldAreaInput = findViewById(R.id.field_area);
        plantTypeSpinner = findViewById(R.id.plant_type);
        weatherConditionSpinner = findViewById(R.id.weather_condition);
        calculateButton = findViewById(R.id.calculate_button);
        resultText = findViewById(R.id.result_text);

        // Fill Plant Type Spinner
        String[] crops = {"Barley", "Corn", "Potato", "Tomato"};
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, crops);
        plantTypeSpinner.setAdapter(cropAdapter);

        // Set up weather options
        String[] weatherOptions = {"Sunny", "Cloudy", "Rainy", "Stormy"};
        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, weatherOptions);
        weatherConditionSpinner.setAdapter(weatherAdapter);

        calculateButton.setOnClickListener(v -> calculateWaterRequirement());
    }

    private void calculateWaterRequirement() {
        String fieldAreaStr = fieldAreaInput.getText().toString();
        if (fieldAreaStr.isEmpty()) {
            resultText.setText("Please enter the field area!");
            return;
        }

        double fieldArea = Double.parseDouble(fieldAreaStr);
        String plantType = plantTypeSpinner.getSelectedItem().toString();
        String weatherCondition = weatherConditionSpinner.getSelectedItem().toString();

        double waterNeed = getWaterRequirement(plantType, weatherCondition);
        double totalWater = fieldArea * waterNeed;

        resultText.setText("Required water amount: " + totalWater + " liters");
    }

    private double getWaterRequirement(String plantType, String weatherCondition) {
        double baseWater;
        switch (plantType) {
            case "Barley": baseWater = 5; break;
            case "Corn": baseWater = 6; break;
            case "Potato": baseWater = 4.5; break;
            case "Tomato": baseWater = 7; break;
            default: baseWater = 5;
        }

        switch (weatherCondition) {
            case "Sunny": baseWater *= 1.2; break;
            case "Cloudy": baseWater *= 1.0; break;
            case "Rainy": baseWater *= 0.8; break;
            case "Stormy": baseWater *= 0.7; break;
        }
        return baseWater;
    }
}