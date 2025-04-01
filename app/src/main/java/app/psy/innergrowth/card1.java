package app.psy.innergrowth;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class card1 extends AppCompatActivity {

    private EditText etLandSize;
    private Spinner spCropType, spWeather;
    private Button btnCalculate;
    private TextView tvLastResult;
    private ListView lvHistory;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> historyAdapter;
    private ArrayList<String> historyList;
    private ArrayList<String> historyKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card1);

        databaseReference = FirebaseDatabase.getInstance().getReference("crop_data");

        etLandSize = findViewById(R.id.etLandSize);
        spCropType = findViewById(R.id.spCropType);
        spWeather = findViewById(R.id.spWeather);
        btnCalculate = findViewById(R.id.btnCalculate);
        tvLastResult = findViewById(R.id.tvLastResult);
        lvHistory = findViewById(R.id.lvHistory);

        // Մշակաբույսերի ցանկ
        String[] crops = {"Barley", "Corn", "Potato", "Tomato"};
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, crops);
        spCropType.setAdapter(cropAdapter);

        // Եղանակի տարբերակներ
        String[] weatherOptions = {"Rainy", "Dry", "Cloudy", "Stormy"};
        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, weatherOptions);
        spWeather.setAdapter(weatherAdapter);

        // Պատմության ցուցակ
        historyList = new ArrayList<>();
        historyKeys = new ArrayList<>();
        historyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        lvHistory.setAdapter(historyAdapter);

        btnCalculate.setOnClickListener(v -> calculateAndSaveData());

        // Բեռնում ենք Firebase-ի պատմությունը
        loadHistory();

        // Ջնջում ենք առանձին տվյալներ ListView-ից
        lvHistory.setOnItemClickListener((parent, view, position, id) -> {
            String keyToDelete = historyKeys.get(position);
            deleteHistoryItem(keyToDelete, position);
        });
    }

    private void calculateAndSaveData() {
        String landSizeStr = etLandSize.getText().toString();
        if (landSizeStr.isEmpty()) {
            Toast.makeText(this, "Please enter the land size", Toast.LENGTH_SHORT).show();
            return;
        }

        double landSize = Double.parseDouble(landSizeStr);
        String selectedCrop = spCropType.getSelectedItem().toString();
        String selectedWeather = spWeather.getSelectedItem().toString();

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

        double weatherFactor = 1.0;
        switch (selectedWeather) {
            case "Rainy":
                weatherFactor = 1.2;
                break;
            case "Dry":
                weatherFactor = 1.5;
                break;
            case "Cloudy":
                weatherFactor = 1.1;
                break;
            case "Stormy":
                weatherFactor = 0.9;
                break;
        }

        double requiredSeeds = (landSize * seedRate) / 10000;
        double requiredNitrogen = ((landSize * nitrogen) / 10000) * weatherFactor;
        double requiredPhosphorus = ((landSize * phosphorus) / 10000) * weatherFactor;
        double requiredPotassium = ((landSize * potassium) / 10000) * weatherFactor;

        String result = "🌱 Selected Crop: " + selectedCrop + "\n" +
                "🌧 Selected Weather: " + selectedWeather + "\n" +
                "🌾 Required Seeds: " + String.format("%.2f", requiredSeeds) + " kg\n" +
                "💥 Nitrogen (N): " + String.format("%.2f", requiredNitrogen) + " kg\n" +
                "💡 Phosphorus (P): " + String.format("%.2f", requiredPhosphorus) + " kg\n" +
                "💦 Potassium (K): " + String.format("%.2f", requiredPotassium) + " kg";

        tvLastResult.setText(result);
        tvLastResult.setVisibility(View.VISIBLE);

        String key = databaseReference.push().getKey();
        if (key != null) {
            databaseReference.child(key).setValue(result);
        }
    }

    private void loadHistory() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                historyList.clear();
                historyKeys.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    historyKeys.add(data.getKey());
                    historyList.add(data.getValue(String.class));
                }

                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void deleteHistoryItem(String key, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete?")
                .setMessage("Are you sure you want to delete this calculation?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    databaseReference.child(key).removeValue();
                    historyList.remove(position);
                    historyKeys.remove(position);
                    historyAdapter.notifyDataSetChanged();
                    Toast.makeText(card1.this, "Calculation successfully deleted!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

}