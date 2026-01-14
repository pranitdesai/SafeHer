package com.example.safeher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.Collections;

public class AccountPage extends AppCompatActivity {

    private FirebaseFirestore fstore;
    private FirebaseAuth mAuth;
    private TextView randomCodeTextView, name, email, phoneNumber, guardian1, guardian2, guardian3;
    private String guardian11, guardian22, guardian33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_page);
        initUI();
    }

    private void initUI() {
        randomCodeTextView = findViewById(R.id.randomCodeTextView);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phoneNumber);
        guardian1 = findViewById(R.id.guardian1);
        guardian2 = findViewById(R.id.guardian2);
        guardian3 = findViewById(R.id.guardian3);

        // Initialize Firestore and Auth
        fstore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            showToast("User not logged in!");
            return;
        }

        randomCodeGenerater();
        fetchUserData();
    }

    private void fetchUserData() {
        String userId = mAuth.getCurrentUser().getUid();

        fstore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        name.setText(documentSnapshot.getString("Full_Name"));
                        email.setText(documentSnapshot.getString("Email"));
                        phoneNumber.setText(documentSnapshot.getString("Phone_Number"));
                        guardian11 = documentSnapshot.getString("Guardian_1");
                        guardian22 = documentSnapshot.getString("Guardian_2");
                        guardian33 = documentSnapshot.getString("Guardian_3");
                        if(guardian11 != null)
                        {
                            guardian1.setText(guardian11);
                        }
                        if(guardian22 != null)
                        {
                            guardian2.setText(guardian22);
                        }
                        if(guardian33 != null)
                        {
                            guardian3.setText(guardian33);
                        }
                    } else {
                        showToast("User data not found!");
                    }
                })
                .addOnFailureListener(e -> showToast("Error fetching data: " + e.getMessage()));
    }

    private void randomCodeGenerater() {
        String userId = mAuth.getCurrentUser().getUid();
        String randomCode = SecureRandomCodeGenerator.generateRandomCode();

        DocumentReference docRef = fstore.collection("users").document(userId);
        docRef.set(Collections.singletonMap("code", randomCode), SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        docRef.addSnapshotListener((value, error) -> {
                            if (error != null) {
                                Log.d("Firestore", "Error fetching code: " + error.getMessage());
                                return;
                            }
                            if (value != null && value.contains("code")) {
                                randomCodeTextView.setText(value.getString("code"));
                            } else {
                                Log.d("Firestore", "No data found");
                            }
                        })
                )
                .addOnFailureListener(e -> showToast("Failed to generate code: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
