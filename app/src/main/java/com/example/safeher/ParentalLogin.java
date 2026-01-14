package com.example.safeher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ParentalLogin extends AppCompatActivity {

    private EditText phone, code;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences, useridPrefs;
    private String userId;

    @Override
    protected void onStart() {
        super.onStart();
        sharedPreferences = getSharedPreferences("LoginState", MODE_PRIVATE);
        useridPrefs = getSharedPreferences("UserIdPrefs", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isParentLoggin", false)) {
            triggerHapticFeedback();
            startActivity(new Intent(this, ParentalHomePage.class));
            finish();
        }
    }
    private void triggerHapticFeedback() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parental_login);

        phone = findViewById(R.id.textInputEditTextEmail);
        code = findViewById(R.id.textInputEditTextPassword);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        findViewById(R.id.sendOTP).setOnClickListener(v -> findUserIdByPhone(phone.getText().toString()));
    }

    private void findUserIdByPhone(String phoneNo) {
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        if (phoneNo.equals(doc.getString("Guardian_1")) ||
                                phoneNo.equals(doc.getString("Guardian_2")) ||
                                phoneNo.equals(doc.getString("Guardian_3"))) {
                            userId = doc.getId();
                            useridPrefs.edit().putString("userId", userId).apply();
                            Log.d("ParentalLoginx", "User ID found: " + userId);
                            authenticateCode();
                            return;
                        }
                    }
                    Toast.makeText(this, "Number not found", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void authenticateCode() {
        String enteredCode = code.getText().toString();
        if (enteredCode.isEmpty()) {
            Toast.makeText(this, "Enter Code", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && enteredCode.equals(documentSnapshot.getString("code"))) {
                        progressDialog.show();
                        sharedPreferences.edit().putBoolean("isParentLoggin", true).apply();
                        progressDialog.dismiss();
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, ParentalHomePage.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid Code", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
