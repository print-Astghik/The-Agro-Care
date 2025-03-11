package app.psy.innergrowth;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class card2 extends AppCompatActivity {

    private EditText landSizeInput;
    private Spinner cropTypeSpinner;
    private Button calculateButton;
    private TextView resultText;

    private final String[] crops = {"Wheat", "Corn", "Rice", "Barley"};
    private final double[] seedRates = {150, 20, 80, 130}; // kg per hectare
    private final double[] yieldRates = {3.2, 7.5, 6.0, 2.8}; // tons per hectare
    private final double[] fertilizerRates = {100, 120, 90, 110}; // kg per hectare

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card2);

        landSizeInput = findViewById(R.id.landSizeInput);
        cropTypeSpinner = findViewById(R.id.cropTypeSpinner);
        calculateButton = findViewById(R.id.calculateButton);
        resultText = findViewById(R.id.resultText);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, crops);
        cropTypeSpinner.setAdapter(adapter);

        calculateButton.setOnClickListener(v -> calculateResources());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_settings);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_settings) {
                return true;
            } else if (itemId == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
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
    }

    private void calculateResources() {
        String landSizeStr = landSizeInput.getText().toString();
        if (landSizeStr.isEmpty()) {
            resultText.setText("Please enter land size.");
            return;
        }

        double landSize = Double.parseDouble(landSizeStr);
        int selectedCropIndex = cropTypeSpinner.getSelectedItemPosition();

        double seedNeeded = landSize * seedRates[selectedCropIndex];
        double estimatedYield = landSize * yieldRates[selectedCropIndex];
        double fertilizerNeeded = landSize * fertilizerRates[selectedCropIndex];

        String result = "Seed required: " + seedNeeded + " kg\n"
                + "Estimated yield: " + estimatedYield + " tons\n"
                + "Fertilizer required: " + fertilizerNeeded + " kg";

        resultText.setText(result);
    }
}