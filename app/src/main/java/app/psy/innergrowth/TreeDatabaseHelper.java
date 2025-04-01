package app.psy.innergrowth;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class TreeDatabaseHelper {
    private static final String TAG = "TreeDatabaseHelper";
    private final FirebaseFirestore db;

    public TreeDatabaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveTreeLocation(String treeId, double latitude, double longitude) {
        Map<String, Object> treeData = new HashMap<>();
        treeData.put("latitude", latitude);
        treeData.put("longitude", longitude);
        treeData.put("timestamp", System.currentTimeMillis());

        db.collection("trees").document(treeId)
                .set(treeData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Tree location saved successfully!"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving tree location: " + e.getMessage()));
    }

    public void getTreeLocation(String treeId, TreeLocationCallback callback) {
        db.collection("trees").document(treeId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");

                        if (latitude != null && longitude != null) {
                            callback.onLocationReceived(latitude, longitude);
                        } else {
                            callback.onFailure("Invalid tree location data");
                        }
                    } else {
                        callback.onFailure("Tree not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface TreeLocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onFailure(String error);
    }
}
