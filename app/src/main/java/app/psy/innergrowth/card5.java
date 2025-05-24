package app.psy.innergrowth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

public class card5 extends AppCompatActivity {
    private TextView locationText, weatherText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card5);

        locationText = findViewById(R.id.locationText);
        weatherText = findViewById(R.id.weatherText);

        // Always show Dilijan coordinates
        double dilijanLat = 40.7402;
        double dilijanLon = 44.8639;
        locationText.setText("Dilijan, Armenia\nLat: " + dilijanLat + ", Lon: " + dilijanLon);

        // Always get Dilijan weather
        getWeather(dilijanLat, dilijanLon);
    }

    private void getWeather(double latitude, double longitude) {
        String apiKey = "2b9c951f57d15d32dc24e6838ec72fec";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String cityName = response.getString("name");
                        String country = response.getJSONObject("sys").getString("country");
                        String weatherMain = response.getJSONArray("weather").getJSONObject(0).getString("main");
                        String weatherDescription = response.getJSONArray("weather").getJSONObject(0).getString("description");

                        double tempKelvin = response.getJSONObject("main").getDouble("temp");
                        double tempCelsius = tempKelvin - 273.15;
                        int humidity = response.getJSONObject("main").getInt("humidity");

                        String weatherInfo = "City: " + cityName + ", " + country + "\n" +
                                "Weather: " + weatherMain + " (" + weatherDescription + ")\n" +
                                String.format("Temperature: %.1f°C\n", tempCelsius) +
                                "Humidity: " + humidity + "%";

                        weatherText.setText(weatherInfo);

                    } catch (JSONException e) {
                        weatherText.setText("Error parsing weather data");
                        e.printStackTrace();
                    }
                },
                error -> weatherText.setText("Weather data unavailable"));

        queue.add(jsonObjectRequest);
    }
}
