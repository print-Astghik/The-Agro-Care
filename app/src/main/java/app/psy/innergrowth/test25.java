package app.psy.innergrowth;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class test25 extends AppCompatActivity {

    private static final String BASE_URL_OPENFARM = "https://openfarm.cc/api/v1/crops/";
    private TextView plantInfoTextView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test25);

        // UI տարրը և Firebase-ի հղումը
        plantInfoTextView = findViewById(R.id.plantinfo);
        databaseReference = FirebaseDatabase.getInstance().getReference("plants");

        plantInfoTextView.setText("Fetching plant info from OpenFarm...");

        // Նոր թել՝ OpenFarm-ից տվյալներ բերելու համար
        new Thread(() -> {
            try {
                String openFarmResponse = getOpenFarmData("onion");
                Log.d("OPENFARM_RESPONSE", openFarmResponse);
                runOnUiThread(() -> processOpenFarmData(openFarmResponse));
            } catch (IOException e) {
                Log.e("API_ERROR", "Error fetching OpenFarm data", e);
                runOnUiThread(() -> plantInfoTextView.setText("Error fetching OpenFarm data."));
            }
        }).start();
    }

    // Բերում է բույսի տվյալները ըստ անունի
    private String getOpenFarmData(String plantName) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL_OPENFARM + plantName;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + response);
            }

            return response.body().string();
        }
    }

    // Մշակում է ստացված JSON պատասխանը
    private void processOpenFarmData(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject attributes = data.getJSONObject("attributes");

            String name = attributes.optString("name", "Unknown");
            String description = attributes.optString("description", "No description");
            String sunRequirements = attributes.optString("sun_requirements", "Not specified");
            String watering = attributes.optString("watering", "Not specified");

            String displayText = "🌱 Plant: " + name +
                    "\n🌞 Sun: " + sunRequirements +
                    "\n💧 Watering: " + watering +
                    "\n📖 Description: " + description;

            plantInfoTextView.setText(displayText);

            databaseReference.child(name).child("sunRequirements").setValue(sunRequirements);
            databaseReference.child(name).child("watering").setValue(watering);
            databaseReference.child(name).child("description").setValue(description);

            // ✅ Ստանում ենք guide ID-ն
            JSONObject relationships = data.optJSONObject("relationships");
            if (relationships != null) {
                JSONObject cropGuides = relationships.optJSONObject("crop_guides");
                if (cropGuides != null) {
                    JSONArray guidesArray = cropGuides.optJSONArray("data");
                    if (guidesArray != null && guidesArray.length() > 0) {
                        String guideId = guidesArray.getJSONObject(0).optString("id", null);

                        if (guideId != null) {
                            new Thread(() -> {
                                try {
                                    String guideResponse = getCropGuide(guideId);
                                    Log.d("GUIDE_RESPONSE", guideResponse);
                                    runOnUiThread(() -> processCropGuide(guideResponse, name));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    runOnUiThread(() ->
                                            plantInfoTextView.append("\nError fetching crop guide."));
                                }
                            }).start();
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            plantInfoTextView.setText("Error processing OpenFarm data.");
        }
    }


    // Բերում է կոնկրետ guide տվյալները
    private String getCropGuide(String guideId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String url = "https://openfarm.cc/api/v1/guides/" + guideId;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Guide request failed: " + response);
            }
            return response.body().string();
        }
    }

    // Մշակում է guide-ի պատասխանը
    private void processCropGuide(String jsonResponse, String plantName) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject attributes = jsonObject.getJSONObject("data").getJSONObject("attributes");

            String overview = attributes.optString("overview", "No overview provided.");
            JSONArray practices = attributes.optJSONArray("practices");

            StringBuilder guideText = new StringBuilder("\n📝 Guide Overview: " + overview + "\n");

            if (practices != null) {
                for (int i = 0; i < practices.length(); i++) {
                    guideText.append("👉 Step ").append(i + 1).append(": ").append(practices.getString(i)).append("\n");
                }
            }

            // Ցուցադրում և պահպանում Firebase-ում
            plantInfoTextView.append(guideText.toString());
            databaseReference.child(plantName).child("guideOverview").setValue(overview);
            databaseReference.child(plantName).child("guideSteps").setValue(guideText.toString());

        } catch (JSONException e) {
            Log.e("GUIDE_PARSE_ERROR", "Error processing crop guide", e);
            plantInfoTextView.append("\nError processing crop guide.");
        }
    }
}
