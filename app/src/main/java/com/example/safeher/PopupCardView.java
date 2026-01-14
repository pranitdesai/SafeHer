package com.example.safeher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PopupCardView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_card);

        EditText emailInput = findViewById(R.id.textInputEditTextEmail);
        Button sendEmail = findViewById(R.id.sendEmailForgotPassword);

        sendEmail.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        Toast.makeText(this, task.isSuccessful() ? "Email sent" : "Email not sent, try again later", Toast.LENGTH_SHORT).show();
                        if (task.isSuccessful()) finish();
                    });
        });
    }
}
