package app.psy.innergrowth;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherHelper {
    private static final String API_KEY = "2b9c951f57d15d32dc24e6838ec72fec"; // Այստեղ տեղադրիր API բանալիդ
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface WeatherCallback {
        void onWeatherReceived(double temperature, int humidity, String weatherCondition);
    }

    public static void getWeather(double lat, double lon, WeatherCallback callback) {
        executor.execute(() -> {
            try {
                String urlString = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                double temp = json.getJSONObject("main").getDouble("temp");
                int humidity = json.getJSONObject("main").getInt("humidity");
                String weatherCondition = json.getJSONArray("weather").getJSONObject(0).getString("main");

                handler.post(() -> callback.onWeatherReceived(temp, humidity, weatherCondition));
            } catch (Exception e) {
                Log.e("WeatherHelper", "Error fetching weather data", e);
            }
        });
    }
}
