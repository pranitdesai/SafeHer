package com.example.safeher;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ParentalHomePage extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parental_home_page);

        userid = getIntent().getStringExtra("userId");
        sharedPreferences = getSharedPreferences("LoginState", MODE_PRIVATE);
        Button livLoc = findViewById(R.id.getLiveLocation);
        Button viewHistory = findViewById(R.id.viewHistory);
        livLoc.setOnClickListener(v ->
                startActivity(new Intent(this, MapsActivity.class))
        );

        viewHistory.setOnClickListener(v ->
                startActivity(new Intent(this, LocationHistoryActivity.class))
        );

        findViewById(R.id.dotMenu).setOnClickListener(v -> {
            showCustomLogoutPopup();
        });
    }
    private void showCustomLogoutPopup() {
        showConfirmationDialog("Logout", "Click OK to Confirm Logout", this::logoutConfirmed);
    }

    private void logoutConfirmed() {

        sharedPreferences.edit().remove("isParentLoggin").apply();
        Intent intent = new Intent(ParentalHomePage.this, LandingPageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_custom_logout_popup);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupTitle = dialog.findViewById(R.id.popupTitle);
        TextView popupMessage = dialog.findViewById(R.id.popupMessage);
        Button confirmButton = dialog.findViewById(R.id.ConfirmButton);
        Button cancelButton = dialog.findViewById(R.id.CancelButton);

        popupTitle.setText(title);
        popupMessage.setText(message);

        confirmButton.setOnClickListener(v -> {
            onConfirm.run();
            dialog.dismiss();
        });
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
