package app.psy.innergrowth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {
    private final FusedLocationProviderClient fusedLocationClient;
    private final WeatherHelper weatherHelper;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int WEATHER_UPDATE_INTERVAL = 10000; // 10 վայրկյան
    private double lastLatitude;
    private double lastLongitude;
    private final Context context;

    public interface LocationCallbackInterface {
        void onLocationReceived(double latitude, double longitude);
    }

    public interface WeatherUpdateListener {
        void onWeatherUpdated(double temperature, int humidity, String condition);
    }

    private WeatherUpdateListener weatherListener;

    public LocationHelper(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        weatherHelper = new WeatherHelper();
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(LocationCallbackInterface callback) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                callback.onLocationReceived(location.getLatitude(), location.getLongitude());
            } else {
                requestNewLocation(callback);
            }
        }).addOnFailureListener(e -> Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocation(LocationCallbackInterface callback) {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    Toast.makeText(context, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                    return;
                }
                Location location = locationResult.getLastLocation();
                callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                fusedLocationClient.removeLocationUpdates(this);
            }
        }, Looper.getMainLooper());
    }

    @SuppressLint("MissingPermission")
    public void startWeatherUpdates(double latitude, double longitude, WeatherUpdateListener listener) {
        this.lastLatitude = latitude;
        this.lastLongitude = longitude;
        this.weatherListener = listener;
        updateWeather();
    }

    private void updateWeather() {
        weatherHelper.getWeather(lastLatitude, lastLongitude, (temperature, humidity, condition) -> {
            if (weatherListener != null) {
                weatherListener.onWeatherUpdated(temperature, humidity, condition);
            }
            handler.postDelayed(this::updateWeather, WEATHER_UPDATE_INTERVAL);
        });
    }

    public void stopWeatherUpdates() {
        handler.removeCallbacksAndMessages(null);
    }
}
