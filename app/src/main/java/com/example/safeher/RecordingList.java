package com.example.safeher;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class RecordingList extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private String name, phone, email;
    private MaterialButton buttonLogout;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferencesOfUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recording_list);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);
        buttonLogout = findViewById(R.id.btnLogout);
        buttonLogout.setOnClickListener(v -> {
            logoutUser();
        });

        sharedPreferencesOfUser = getSharedPreferences("isUserLoggedIn", MODE_PRIVATE);


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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithAnimation();
            }
        });


        RecyclerView recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Pair<ArrayList<String>, ArrayList<String>> recordings = fetchRecordings(this);
        recyclerView.setAdapter(new MyAdapter(getApplicationContext(), recordings.first, recordings.second));
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
//                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_recording) {
                drawerLayout.closeDrawers();
            }
            else if (id == R.id.nav_settings) {

                startActivity(new Intent(this, Settings.class));
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
    private void showCustomLogoutPopup() {
        showConfirmationDialog("Logout", "Click OK to Confirm Logout", this::logoutConfirmed);
    }
    private void logoutUser() {
        showCustomLogoutPopup();
    }


    private void finishWithAnimation() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    private void logoutConfirmed() {
        progressDialog.show();
        sharedPreferencesOfUser.edit().remove("isUserLoggedIn").apply();
        mAuth.signOut();
        progressDialog.dismiss();
        Intent intent = new Intent(this, LandingPageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private Pair<ArrayList<String>, ArrayList<String>> fetchRecordings(Context context) {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> paths = new ArrayList<>();

        String[] projection = {MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA};
        String selection = MediaStore.Audio.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{"%SafeHerRecordings%"};
        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = context.getContentResolver().query(collection, projection, selection, selectionArgs, MediaStore.Audio.Media.DATE_ADDED + " DESC")) {
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                while (cursor.moveToNext()) {
                    names.add(cursor.getString(nameIndex));
                    paths.add(cursor.getString(pathIndex));
                }
            }
        } catch (Exception e) {
            Log.e("MediaStoreFetch", "Error fetching recordings", e);
        }

        return new Pair<>(names, paths);
    }
}
