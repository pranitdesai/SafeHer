package com.example.safeher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ToggleButton;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class LocationSettings extends AppCompatActivity {

    private ToggleButton location;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_settings);

        location = findViewById(R.id.Location);

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/features_enabled");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    location.setChecked(snapshot.child("Location").getValue(Boolean.class));
                } else {
                    Log.d("Firebase", "No feature settings found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data: " + error.getMessage());
            }
        });

        location.setOnClickListener(v -> updateFeatureSettings());
    }

    private void updateFeatureSettings() {
        Map<String, Boolean> buttonStates = new HashMap<>();
        buttonStates.put("Location", location.isChecked());
        databaseReference.setValue(buttonStates);
    }
}
