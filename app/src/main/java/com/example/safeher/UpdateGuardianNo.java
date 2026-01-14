package com.example.safeher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateGuardianNo extends AppCompatActivity {

    private Button updateButton;
    private EditText g1, g2, g3;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fstore;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_guardian_no);
        initUI();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        updateButton.setOnClickListener(view -> validateAndUpdateGuardians());
    }

    private void initUI() {
        updateButton = findViewById(R.id.buttonUpdate);
        g1 = findViewById(R.id.textInputEditTextG1);
        g2 = findViewById(R.id.textInputEditTextG2);
        g3 = findViewById(R.id.textInputEditTextG3);
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Updating...");
    }

    private void validateAndUpdateGuardians() {
        String stringG1 = g1.getText().toString().trim();
        String stringG2 = g2.getText().toString().trim();
        String stringG3 = g3.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        if (!stringG1.isEmpty()) updates.put("Guardian_1", stringG1);
        if (!stringG2.isEmpty()) updates.put("Guardian_2", stringG2);
        if (!stringG3.isEmpty()) updates.put("Guardian_3", stringG3);

        if (updates.isEmpty()) {
            Toast.makeText(this, "Please enter at least one guardian number", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        DocumentReference docRef = fstore.collection("users").document(mAuth.getCurrentUser().getUid());
        docRef.update(updates)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateGuardianNo.this, "Update Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UpdateGuardianNo.this, HomePageActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateGuardianNo.this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
