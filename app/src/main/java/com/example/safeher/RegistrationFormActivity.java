package com.example.safeher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrationFormActivity extends AppCompatActivity {
    private EditText email, name, phone, password, passwordConfirm;
    private Button signup;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferencesOfUser;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferencesOfUser = getSharedPreferences("isUserLoggedIn", MODE_PRIVATE);

        if (mAuth.getCurrentUser() != null && sharedPreferencesOfUser.getBoolean("isUserLoggedIn", false)) {
            startActivity(new Intent(this, HomePageActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_form);

        initUI();
        signup.setOnClickListener(v -> {
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

    private void initUI() {
        name = findViewById(R.id.textInputEditTextName);
        phone = findViewById(R.id.textInputEditTextPhone);
        email = findViewById(R.id.textInputEditTextEmail);
        password = findViewById(R.id.textInputEditTextPassword);
        passwordConfirm = findViewById(R.id.textInputEditTextConfirmPassword);
        signup = findViewById(R.id.buttonSignUp);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing up...");
    }

    private void registerUser() {
        String stringName = name.getText().toString().trim();
        String stringEmail = email.getText().toString().trim();
        String stringPhone = phone.getText().toString().trim();
        String stringPassword = password.getText().toString().trim();
        String stringConfirmPassword = passwordConfirm.getText().toString().trim();

        if (!validateInput(stringName, stringEmail, stringPhone, stringPassword, stringConfirmPassword)) return;

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(stringEmail, stringPassword)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, GuardianInfo.class);
                        intent.putExtra("StringName", stringName);
                        intent.putExtra("StringEmail", stringEmail);
                        intent.putExtra("StringPhone", stringPhone);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInput(String name, String email, String phone, String password, String confirmPassword) {
        if (name.isEmpty()) {
            this.name.setError("Name is required");
            return false;
        }
        if (phone.isEmpty()) {
            this.phone.setError("Phone number is required");
            return false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Valid email is required");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            this.password.setError("Password must be at least 6 characters");
            return false;
        }
        if (!confirmPassword.equals(password)) {
            this.passwordConfirm.setError("Passwords do not match");
            return false;
        }
        return true;
    }
}
