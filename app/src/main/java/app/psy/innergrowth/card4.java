package app.psy.innergrowth;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class card4 extends AppCompatActivity {

    private EditText editTextWidth, editTextLength, editTextSeedRate;
    private TextView textViewResult;
    private ListView listViewResults;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> calculationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card4);

        editTextWidth = findViewById(R.id.editTextWidth);
        editTextLength = findViewById(R.id.editTextLength);
        editTextSeedRate = findViewById(R.id.editTextSeedRate);
        textViewResult = findViewById(R.id.textViewResult);
        listViewResults = findViewById(R.id.listViewResults);
        Button buttonCalculate = findViewById(R.id.buttonCalculate);
        Button buttonSave = findViewById(R.id.buttonSave);

        databaseReference = FirebaseDatabase.getInstance().getReference("calculations");
        calculationsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, calculationsList);
        listViewResults.setAdapter(adapter);

        buttonCalculate.setOnClickListener(v -> calculateSeed());
        buttonSave.setOnClickListener(v -> saveCalculation());
    }

    private void calculateSeed() {
        String widthStr = editTextWidth.getText().toString();
        String lengthStr = editTextLength.getText().toString();
        String seedRateStr = editTextSeedRate.getText().toString();

        if (widthStr.isEmpty() || lengthStr.isEmpty() || seedRateStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double width = Double.parseDouble(widthStr);
        double length = Double.parseDouble(lengthStr);
        double seedRate = Double.parseDouble(seedRateStr);
        double area = width * length;
        double totalSeeds = area * seedRate;

        String result = "Total Seeds Needed: " + totalSeeds + " kg";
        textViewResult.setText(result);
    }

    private void saveCalculation() {
        String result = textViewResult.getText().toString();
        if (result.equals("Result will be shown here") || result.isEmpty()) {
            Toast.makeText(this, "No calculation to save", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = databaseReference.push().getKey();
        databaseReference.child(id).setValue(result).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Calculation saved", Toast.LENGTH_SHORT).show();

                // Ավելացնել նոր արդյունքը ցուցակում առանց Firebase-ից նորից բեռնելու
                calculationsList.add(result);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadCalculations() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                calculationsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String value = dataSnapshot.getValue(String.class);
                    calculationsList.add(value);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(card4.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}