package com.example.safeher;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.safeher.databinding.ActivityMapsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private String userid;
    private LatLng location;
    private SharedPreferences useridPrefs;

    // Handler for fetching location every 2 seconds
    private Handler handler = new Handler();
    private Runnable locationUpdater;
    private boolean isUpdating = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get user ID from SharedPreferences
        useridPrefs = getSharedPreferences("UserIdPrefs", MODE_PRIVATE);
        userid = useridPrefs.getString("userId", null);

        // Inflate the layout
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (userid == null) {
            Log.e("MapsActivity", "User ID is null, cannot fetch location.");
            return;
        }

        // Define the task that fetches location from Firebase every 2 seconds
        locationUpdater = new Runnable() {
            @Override
            public void run() {
                if (!isUpdating) return; // Stop if activity is closed

                DatabaseReference latlngRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userid)
                        .child("location");

                latlngRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.child("lat").getValue() != null && snapshot.child("lng").getValue() != null) {
                            double latitude = snapshot.child("lat").getValue(Double.class);
                            double longitude = snapshot.child("lng").getValue(Double.class);
                            location = new LatLng(latitude, longitude);

                            mMap.clear(); // Clear previous markers
                            mMap.addMarker(new MarkerOptions().position(location).title("User Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 20));

                            Log.d("MapsActivity", "Updated location: Lat=" + latitude + ", Lng=" + longitude);
                        } else {
                            Log.e("MapsActivity", "Location data not found for user: " + userid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MapsActivity", "Firebase database error: " + error.getMessage());
                    }
                });

                // Run this task again after 2 seconds
                handler.postDelayed(this, 2000);
            }
        };

        // Start fetching location updates
        handler.post(locationUpdater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isUpdating = false; // Stop updates when activity is destroyed
        handler.removeCallbacks(locationUpdater); // Remove pending tasks
    }
}
