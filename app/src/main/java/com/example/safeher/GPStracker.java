package com.example.safeher;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GPStracker {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    public GPStracker(Context c) {
        this.context = c;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

    }

    public void getCurrentLocation(LocationResultCallback callback) {
        if (!hasLocationPermission()) {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null)
                callback.onLocationRetrieved(location.getLatitude(), location.getLongitude());
            else
                requestNewLocationData(callback);
        });
    }

    private void requestNewLocationData(LocationResultCallback callback) {
        if (!hasLocationPermission()) return;

        LocationRequest locationRequest = new LocationRequest.Builder(2000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    callback.onLocationRetrieved(location.getLatitude(), location.getLongitude());
                    stopLocationUpdates();
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public void stopLocationUpdates() {
        if (locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void startLocationUpdatesToFirebase() {
        if (!hasLocationPermission()) {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_LONG).show();
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(2000) // Update interval 2 sec
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Upload location data to Firebase
                    sendLocation(latitude,longitude);
                    Log.d("Location Update", "Latitude: " + latitude + ", Longitude: " + longitude);
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void sendLocation(double latitude, double longitude) {
        DatabaseReference databaseReferenceLocation;
        String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        databaseReferenceLocation = FirebaseDatabase.getInstance().getReference("users/" + userID + "/location");
        Map<String, Double> LatLng = new HashMap<>();
        LatLng.put("lat", latitude);
        LatLng.put("lng", longitude);
        databaseReferenceLocation.setValue(LatLng);
    }

    public interface LocationResultCallback {
        void onLocationRetrieved(double latitude, double longitude);
    }
}
