package com.example.safeher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPageActivity extends AppCompatActivity {

    private EditText email, password;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferencesOfUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);


        sharedPreferencesOfUser = getSharedPreferences("isUserLoggedIn", MODE_PRIVATE);
        email = findViewById(R.id.textInputEditTextEmail);
        password = findViewById(R.id.textInputEditTextPassword);
        Button login = findViewById(R.id.buttonLogin);
        Button forgotPass = findViewById(R.id.buttonForgotPassword);
        forgotPass.setPaintFlags(forgotPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        forgotPass.setOnClickListener(v -> {
            triggerHapticFeedback();
            startActivity(new Intent(this, PopupCardView.class));
        });

        login.setOnClickListener(v -> {
            triggerHapticFeedback();
            loginUser();
        });
    }

    private void loginUser() {
        if (!validateInput()) return;

        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        sharedPreferencesOfUser.edit().putBoolean("isUserLoggedIn", true).apply();
                        startActivity(new Intent(this, HomePageActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInput() {
        if (email.getText().toString().isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        }
        if (password.getText().toString().isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return false;
        }
        return true;
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
