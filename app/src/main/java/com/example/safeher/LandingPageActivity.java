package com.example.safeher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LandingPageActivity extends AppCompatActivity {
    MaterialButton login_button, register_button, parent_login_button;

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("LoginState", MODE_PRIVATE);
        SharedPreferences sharedPreferencesOfUser = getSharedPreferences("isUserLoggedIn", MODE_PRIVATE);

        boolean isUserLoggedIn = sharedPreferencesOfUser.getBoolean("isUserLoggedIn", false);
        boolean isParentLoggedIn = sharedPreferences.getBoolean("isParentLoggin", false);

        if (isParentLoggedIn) {
            // Redirect to ParentalHomePage immediately
            startActivity(new Intent(this, ParentalHomePage.class));
            finish();
            return;  // Stop further execution
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && isUserLoggedIn) {
            // Redirect normal user to HomePage
            startActivity(new Intent(this, HomePageActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.landing_page);
        login_button = findViewById(R.id.login_button);
        register_button = findViewById(R.id.register_button);
        parent_login_button = findViewById(R.id.parent_login_button);
        parent_login_button.setPaintFlags(parent_login_button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        parent_login_button.setOnClickListener(v -> {
            triggerHapticFeedback();
            startActivity(new Intent(this, ParentalLogin.class));
        });
        login_button.setOnClickListener(v->
                {   triggerHapticFeedback();
                    startActivity(new Intent(this, LoginPageActivity.class));
                }
        );
        register_button.setOnClickListener(v-> {
            triggerHapticFeedback();
            startActivity(new Intent(this, RegistrationFormActivity.class));
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
}