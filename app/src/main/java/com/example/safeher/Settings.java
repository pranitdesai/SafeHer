package com.example.safeher;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Settings extends AppCompatActivity {

    private ImageButton menuButton;
    private Button buttonAccount, buttonDeleteAccount, buttonLogout, buttonLogout2, locationSettings, updateGuardianNo;
    private FirebaseFirestore firestore;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private String name, phone, email;
    private FirebaseFirestore fstore;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferencesOfUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        initUI();
        setupListeners();
        initNavigation();
        initUserInfo();
        View drawerContainer = findViewById(R.id.drawerContainer);

        ViewCompat.setOnApplyWindowInsetsListener(drawerContainer, (v, insets) -> {
            int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottom + 20
            );
            return insets;
        });

    }

    private void initNavigation() {
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.navigationView);

        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open_drawer, R.string.close_drawer);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomePageActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, AccountPage.class));
            } else if (id == R.id.nav_recording) {
                startActivity(new Intent(this, RecordingList.class));
            }
            else if (id == R.id.nav_settings) {

                drawerLayout.closeDrawers();
            }
            drawerLayout.closeDrawers();
            return true;
        });
        View drawerContainer = findViewById(R.id.drawerContainer);

        ViewCompat.setOnApplyWindowInsetsListener(drawerContainer, (v, insets) -> {
            int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottom + 20
            );
            return insets;
        });

    }

    private void initUserInfo() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("users").document(uid);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Data exists, extract it
                    name = document.getString("Full_Name");
                    phone = document.getString("Phone_Number");
                    email = document.getString("Email");
                    setNavHeaderData(name, email, phone);

                    Log.d("Firestore", "Data: " + document.getData());
                } else {
                    Log.d("Firestore", "No such document");
                }
            } else {
                Log.d("Firestore", "get failed with ", task.getException());
            }
        });


    }


    private void initUI() {
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.navigationView);
        menuButton = findViewById(R.id.dotMenu);
        buttonLogout2 = findViewById(R.id.btnLogout);
        buttonAccount = findViewById(R.id.ButtonAccount);
        buttonDeleteAccount = findViewById(R.id.ButtonDeleteAccount);
        buttonLogout = findViewById(R.id.buttonLogout);
        updateGuardianNo = findViewById(R.id.buttonupdate);
        locationSettings = findViewById(R.id.LocationSettings);

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        sharedPreferencesOfUser = getSharedPreferences("isUserLoggedIn", MODE_PRIVATE);
    }

    private void setNavHeaderData(String name, String email, String phone) {

        // get header view
        View headerView = navigationView.getHeaderView(0);

        TextView txtUserName = headerView.findViewById(R.id.navUserName);
        TextView txtUserEmail = headerView.findViewById(R.id.navUserEmail);
        TextView txtUserPhone = headerView.findViewById(R.id.navUserPhone);

        txtUserName.setText(name);
        txtUserEmail.setText(email);
        txtUserPhone.setText(phone);

    }

    private void setupListeners() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        buttonDeleteAccount.setOnClickListener(v -> showCustomDeletePopup());
        buttonAccount.setOnClickListener(v -> navigateTo(AccountPage.class));
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.START));
        updateGuardianNo.setOnClickListener(v -> navigateTo(UpdateGuardianNo.class));
        buttonLogout.setOnClickListener(v -> logoutUser());
        buttonLogout2.setOnClickListener(v -> logoutUser());
        locationSettings.setOnClickListener(v -> navigateTo(LocationSettings.class));
    }

    private void navigateTo(Class<?> targetActivity) {
        startActivity(new Intent(Settings.this, targetActivity));
    }

    private void logoutUser() {
        showCustomLogoutPopup();
    }

    private void deleteUserAccount() {
        if (currentUser == null) {
            showToast("User is not authenticated.");
            return;
        }

        progressDialog.show();

        fstore.collection("users").document(currentUser.getUid()).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.delete().addOnCompleteListener(task1 -> {
                    progressDialog.dismiss();
                    if (task1.isSuccessful()) {
                        showToast("User account deleted successfully.");
                        mAuth.signOut();
                        Intent intent = new Intent(Settings.this, LandingPageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        showToast("Account deletion failed: " + task1.getException().getMessage());
                    }
                });
            } else {
                progressDialog.dismiss();
                showToast("Firestore deletion failed: " + task.getException().getMessage());
            }
        });
    }

    private void showCustomLogoutPopup() {
        showConfirmationDialog("Logout", "Click OK to Confirm Logout", this::logoutConfirmed);
    }

    private void logoutConfirmed() {
        progressDialog.show();
        sharedPreferencesOfUser.edit().remove("isUserLoggedIn").apply();
        mAuth.signOut();
        progressDialog.dismiss();
        Intent intent = new Intent(Settings.this, LandingPageActivity.class);
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


    private void showCustomDeletePopup() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_custom_delete_acc_popup);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupTitle = dialog.findViewById(R.id.popupTitle);
        TextView popupMessage = dialog.findViewById(R.id.popupMessage);
        EditText inpPassword = dialog.findViewById(R.id.textInputEditTextConfirmPassword);
        Button confirmButton = dialog.findViewById(R.id.ConfirmButton);
        Button cancelButton = dialog.findViewById(R.id.CancelButton);
        popupTitle.setText("Delete Account");
        popupMessage.setText("Enter your password to confirm account deletion.");

        confirmButton.setOnClickListener(v -> {
            String password = inpPassword.getText().toString();
            if (password.isEmpty()) {
                showToast("Password cannot be empty.");
                return;
            }

            if (currentUser != null && currentUser.getEmail() != null) {
                progressDialog.show();
                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
                currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        deleteUserAccount();
                    } else {
                        progressDialog.dismiss();
                        showToast("Reauthentication failed: " + task.getException().getMessage());
                    }
                });
            } else {
                showToast("User not found or email missing.");
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
