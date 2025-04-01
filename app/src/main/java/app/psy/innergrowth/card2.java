package app.psy.innergrowth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

public class card2 extends AppCompatActivity {

    private EditText etLandSize;
    private Spinner spCropType, spWeather;
    private Button btnCalculate, btnClearData;
    private LinearLayout llHistory;  // For dynamically adding history items
    private List<String> history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card2);

        etLandSize = findViewById(R.id.etLandSize);
        spCropType = findViewById(R.id.spCropType);
        spWeather = findViewById(R.id.spWeather);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnClearData = findViewById(R.id.btnClearData);
        llHistory = findViewById(R.id.llHistory);  // History layout

        history = new ArrayList<>();

        // Adding spinner options
        String[] crops = {"Barley", "Corn", "Tomato", "Wheat"};
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, crops);
        spCropType.setAdapter(cropAdapter);

        String[] weatherOptions = {"Rainy", "Dry", "Cloudy", "Stormy"};
        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, weatherOptions);
        spWeather.setAdapter(weatherAdapter);

        btnCalculate.setOnClickListener(v -> calculateWaterNeed());
        btnClearData.setOnClickListener(v -> clearHistory());
    }

    private void calculateWaterNeed() {
        String landSizeStr = etLandSize.getText().toString();
        if (landSizeStr.isEmpty()) {
            return;
        }

        double landSize = Double.parseDouble(landSizeStr);
        String selectedCrop = spCropType.getSelectedItem().toString();
        String selectedWeather = spWeather.getSelectedItem().toString();

        double waterNeed = 0.0;

        switch (selectedCrop) {
            case "Barley":
                waterNeed = 500;
                break;
            case "Corn":
                waterNeed = 800;
                break;
            case "Tomato":
                waterNeed = 700;
                break;
            case "Wheat":
                waterNeed = 600;
                break;
        }

        double weatherFactor = 1.0;
        switch (selectedWeather) {
            case "Rainy":
                weatherFactor = 0.8;
                break;
            case "Dry":
                weatherFactor = 1.5;
                break;
            case "Cloudy":
                weatherFactor = 1.2;
                break;
            case "Stormy":
                weatherFactor = 1.1;
                break;
        }

        double totalWaterNeed = landSize * waterNeed * weatherFactor;

        // Creating new history item
        String result = "Water requirement for " + selectedCrop + " in " + selectedWeather + " weather: " +
                String.format("%.2f", totalWaterNeed) + " liters per hectare";

        addHistoryItem(result);
    }

    private void addHistoryItem(String result) {
        history.add(result);

        // Creating a new layout to display the result with a delete button
        LinearLayout historyItemLayout = new LinearLayout(this);
        historyItemLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView historyText = new TextView(this);
        historyText.setText(result);
        historyText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        historyItemLayout.addView(historyText);

        // Create a delete button for this item
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> deleteHistoryItem(historyItemLayout, result));
        historyItemLayout.addView(deleteButton);

        llHistory.addView(historyItemLayout);
    }

    private void deleteHistoryItem(LinearLayout historyItemLayout, String result) {
        // Remove the item from the history list and from the layout
        history.remove(result);
        llHistory.removeView(historyItemLayout);
    }

    private void clearHistory() {
        // Clear all items in history
        history.clear();
        llHistory.removeAllViews();
    }
}
