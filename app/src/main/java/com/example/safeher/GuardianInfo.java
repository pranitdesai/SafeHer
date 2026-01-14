package com.example.safeher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class GuardianInfo extends AppCompatActivity {
    private EditText g1, g2, g3;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fstore;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferencesOfUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_info);

        g1 = findViewById(R.id.textInputEditTextG1);
        g2 = findViewById(R.id.textInputEditTextG2);
        g3 = findViewById(R.id.textInputEditTextG3);
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        sharedPreferencesOfUser = getSharedPreferences("isUserLoggedIn", MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);

        Button signUp = findViewById(R.id.buttonSignUp);
        signUp.setOnClickListener(view -> {
            triggerHapticFeedback();
            registerUser();
        });
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

    private void registerUser() {
        if (g1.getText().toString().isEmpty()) {
            g1.setError("Guardian 1 is required");
            g1.requestFocus();
            return;
        }
        progressDialog.show();

        Map<String, Object> user = new HashMap<>();
        user.put("Full_Name", getIntent().getStringExtra("StringName"));
        user.put("Email", getIntent().getStringExtra("StringEmail"));
        user.put("Phone_Number", getIntent().getStringExtra("StringPhone"));
        user.put("Guardian_1", g1.getText().toString());
        user.put("Guardian_2", g2.getText().toString().isEmpty() ? "No Number Found" : g2.getText().toString());
        user.put("Guardian_3", g3.getText().toString().isEmpty() ? "No Number Found" : g3.getText().toString());

        fstore.collection("users").document(mAuth.getCurrentUser().getUid()).set(user)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    sharedPreferencesOfUser.edit().putBoolean("isUserLoggedIn", true).apply();
                    startActivity(new Intent(this, HomePageActivity.class));
                    finish();
                });
    }
}