package app.psy.innergrowth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class test22 extends AppCompatActivity {

    private static final String API_KEY = "pDwT3o1fg62M7V3GtjTwMdUoZ0QATXB52hwOO6hKgCI4mw0MQZ";  // Թարմացրու քո բանալին
    private static final String PLANT_ID_URL = "https://plant.id/api/v3/identification";

    private TextView plantInfoTextView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test22);

        plantInfoTextView = findViewById(R.id.plantinfo);
        databaseReference = FirebaseDatabase.getInstance().getReference("plants");

        plantInfoTextView.setText("Fetching plant info from Plant.id...");

        new Thread(() -> {
            try {
                String plantName = "tomato"; // Այստեղ նշվում է բույսի անունը
                String response = getPlantInfoFromPlantId(plantName);
                Log.d("PLANT_ID_RESPONSE", response);
                runOnUiThread(() -> processPlantIdResponse(response, plantName));
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> plantInfoTextView.setText("Error fetching data from Plant.id."));
            }
        }).start();
    }

    private String getPlantInfoFromPlantId(String plantName) throws IOException {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonBody = new JSONObject();
        try {
            // ➤ Բեռնում ենք tomato.png նկարն ու վերածում Base64-ի
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tomato);
            String base64Image = encodeImageToBase64(bitmap);

            JSONArray imageArray = new JSONArray();
            imageArray.put(base64Image);

            jsonBody.put("images", imageArray);
            jsonBody.put("organs", new JSONArray().put("leaf")); // կամ "auto"
            jsonBody.put("custom_id", plantName);
            jsonBody.put("similar_images", false);
            jsonBody.put("plant_language", "en");
            jsonBody.put("plant_details", new JSONArray()
                    .put("common_names")
                    .put("url")
                    .put("taxonomy")
                    .put("wiki_description")
                    .put("watering")
                    .put("sunlight"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(PLANT_ID_URL)
                .addHeader("Api-Key", API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response);
            }
            return response.body().string();
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    private void processPlantIdResponse(String json, String plantName) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray results = jsonObject.optJSONArray("suggestions");

            if (results == null || results.length() == 0) {
                plantInfoTextView.setText("No plant info found.");
                return;
            }

            JSONObject firstSuggestion = results.getJSONObject(0);
            JSONObject plantDetails = firstSuggestion.getJSONObject("plant_details");

            String commonName = plantDetails.optJSONArray("common_names") != null ?
                    plantDetails.optJSONArray("common_names").optString(0, "Unknown") : "Unknown";

            JSONObject watering = plantDetails.optJSONObject("watering");
            String wateringInfo = watering != null ? watering.optString("min") + " to " + watering.optString("max") : "Not specified";

            JSONArray sunlightArray = plantDetails.optJSONArray("sunlight");
            String sunlightInfo = (sunlightArray != null && sunlightArray.length() > 0) ?
                    sunlightArray.join(", ").replace("\"", "") : "Not specified";

            JSONObject wiki = plantDetails.optJSONObject("wiki_description");
            String description = wiki != null ? wiki.optString("value", "No description.") : "No description.";

            String info = "🌱 Plant: " + commonName +
                    "\n💧 Watering: " + wateringInfo +
                    "\n🌞 Sunlight: " + sunlightInfo +
                    "\n📖 Description: " + description;

            plantInfoTextView.setText(info);

            // Firebase - տվյալների պահպանման քայլ
            databaseReference.child(plantName).child("commonName").setValue(commonName);
            databaseReference.child(plantName).child("watering").setValue(wateringInfo);
            databaseReference.child(plantName).child("sunlight").setValue(sunlightInfo);
            databaseReference.child(plantName).child("description").setValue(description);

        } catch (JSONException e) {
            e.printStackTrace();
            plantInfoTextView.setText("Error parsing Plant.id response.");
        }
    }
}
