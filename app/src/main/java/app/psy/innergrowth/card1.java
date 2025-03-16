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

public class card1 extends AppCompatActivity {

    private EditText etLandSize;
    private Spinner spCropType, spWeather;
    private Button btnCalculate;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card1);

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

        etLandSize = findViewById(R.id.etLandSize);
        spCropType = findViewById(R.id.spCropType);
        spWeather = findViewById(R.id.spWeather); // Weather spinner
        btnCalculate = findViewById(R.id.btnCalculate);
        tvResult = findViewById(R.id.tvResult);

        // Set up crop type options
        String[] crops = {"Barley", "Corn", "Potato", "Tomato"};
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, crops);
        spCropType.setAdapter(cropAdapter);

        // Set up weather options with the updated weather conditions
        String[] weatherOptions = {"Rainy", "Dry", "Cloudy", "Stormy"};
        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, weatherOptions);
        spWeather.setAdapter(weatherAdapter);

        btnCalculate.setOnClickListener(v -> calculateRequirements());
    }

    private void calculateRequirements() {
        String landSizeStr = etLandSize.getText().toString();
        if (landSizeStr.isEmpty()) {
            tvResult.setText("Please enter the land size");
            return;
        }

        double landSize = Double.parseDouble(landSizeStr); // Land size in square meters
        String selectedCrop = spCropType.getSelectedItem().toString();
        String selectedWeather = spWeather.getSelectedItem().toString(); // Weather selection

        double seedRate = 0, nitrogen = 0, phosphorus = 0, potassium = 0;

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

        // Weather impact on fertilizer requirements
        double weatherFactor = 1.0;
        switch (selectedWeather) {
            case "Rainy":
                weatherFactor = 1.2; // Increase fertilizer requirement for rainy weather
                break;
            case "Dry":
                weatherFactor = 1.5; // Increase fertilizer requirement for dry weather
                break;
            case "Cloudy":
                weatherFactor = 1.1;
                break;
            case "Stormy":
                weatherFactor = 0.9; // Decrease fertilizer requirement in stormy weather
                break;
        }

        double requiredSeeds = (landSize * seedRate) / 10000;
        double requiredNitrogen = ((landSize * nitrogen) / 10000) * weatherFactor;
        double requiredPhosphorus = ((landSize * phosphorus) / 10000) * weatherFactor;
        double requiredPotassium = ((landSize * potassium) / 10000) * weatherFactor;

        String result = "\uD83C\uDF31 Selected Crop: " + selectedCrop + "\n" +
                "\uD83C\uDF27 Selected Weather: " + selectedWeather + "\n" +
                "\uD83D\uDFE2 Required Seeds: " + String.format("%.2f", requiredSeeds) + " kg\n" +
                "\uD83D\uDCA3 Nitrogen (N): " + String.format("%.2f", requiredNitrogen) + " kg\n" +
                "\uD83D\uDCA1 Phosphorus (P): " + String.format("%.2f", requiredPhosphorus) + " kg\n" +
                "\uD83D\uDCA7 Potassium (K): " + String.format("%.2f", requiredPotassium) + " kg";

        tvResult.setText(result);
    }

}
