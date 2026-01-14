package com.example.safeher;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomePageActivity extends AppCompatActivity {

    private static final String TAG = "HomePageActivity";
    private static final int REQUEST_CODE = 100;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECORD_AUDIO
    };

    // UI
    private ImageView btnSetting, btnVoiceRecording, btnActivateSiren, btnCurrentLocation, btnSOS;
    private TextView txtSos;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private DatabaseReference locationRef;
    private DatabaseReference sosTriggerRef;

    // Other
    private MediaPlayer sirenPlayer;
    private MediaRecorder recorder;
    private Uri audioUri;
    MaterialButton btnLogout;

    private GPStracker gps;
    private ProgressDialog progressDialog;
    private SharedPreferences prefs;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private String name, phone, email;

    private double longitude = 0.0, latitude = 0.0;
    private boolean isSirenOn = false;
    private boolean isRecording = false;
    private boolean isSOSActive = false;

    private String G1, G2, G3;

    // -------------------- LIFECYCLE -------------------- //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        initCore();
        initNavigation();
        initUI();
        initUserInfo();

        if (!checkAndRequestPermissions()) return;

        setupFirebase();
        setupListeners();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseRecorder();
        releaseSirenPlayer();
    }

    // -------------------- INIT METHODS -------------------- //

    private void initCore() {
        progressDialog = new ProgressDialog(this);
        prefs = getSharedPreferences("isUserLoggedIn", MODE_PRIVATE);
        gps = new GPStracker(this);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
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
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, AccountPage.class));
            } else if (id == R.id.nav_recording) {
                startActivity(new Intent(this, RecordingList.class));
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

    private void initUI() {
        btnSetting = findViewById(R.id.buttonSetting);
        btnSOS = findViewById(R.id.sosButton);
        btnActivateSiren = findViewById(R.id.ActivateSiren);
        btnCurrentLocation = findViewById(R.id.CurrentLocShare);
        btnVoiceRecording = findViewById(R.id.VoiceRecording);
        txtSos = findViewById(R.id.sosText);
        btnLogout = findViewById(R.id.btnLogout);

        sirenPlayer = MediaPlayer.create(this, R.raw.siren);
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


    // -------------------- FIREBASE -------------------- //

    private void setupFirebase() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LandingPageActivity.class));
            finish();
            return;
        }

        String uid = currentUser.getUid();
        locationRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("location");
        sosTriggerRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("sos_trigger");

        DocumentReference docRef = firestore.collection("users").document(uid);
        docRef.addSnapshotListener(this, (value, error) -> {
            if (error != null) {
                Log.e(TAG, "Firestore error: " + error.getMessage());
                return;
            }
            if (value != null && value.exists()) {
                G1 = value.getString("Guardian_1");
                G2 = value.getString("Guardian_2");
                G3 = value.getString("Guardian_3");
            }
        });
    }

    // -------------------- LISTENERS -------------------- //

    private void setupListeners() {
        btnSetting.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnActivateSiren.setOnClickListener(v -> {
            toggleSiren();
            triggerHapticFeedback();
        });
        btnSOS.setOnClickListener(v -> handleSOSButton());

        btnLogout.setOnClickListener(v -> {
            triggerHapticFeedback();
            logoutWithCustomPopup();
        });
        btnCurrentLocation.setOnClickListener(v -> {
            triggerHapticFeedback();
            fetchAndSendLocation();
            Toast.makeText(this, "Location Sent Successfully", Toast.LENGTH_SHORT).show();
        });

        btnVoiceRecording.setOnClickListener(v -> {
            triggerHapticFeedback();

            if (isRecording) {
                stopRecording();
                btnVoiceRecording.setImageResource(R.drawable.voice_recording_off);
                Toast.makeText(this, "Recording Saved", Toast.LENGTH_SHORT).show();
            } else {
                startRecording();
                btnVoiceRecording.setImageResource(R.drawable.voice_recording_on);
                Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------- SOS LOGIC -------------------- //

    private void handleSOSButton() {
        triggerHapticFeedback();

        isSOSActive = !isSOSActive;

        if (isSOSActive) {
            enableSOS();
        } else {
            disableSOS();
        }
    }

    private void enableSOS() {
        fetchAndSendLocation();
        shareLiveLocation();
        startRecording();

        sendEmergencyMessageToGuardians(
                "I'm in an emergency situation and need immediate help! Please check my location and contact me ASAP."
        );

        updateSOSUI(true);
        sosTriggerRef.setValue(true);
        Toast.makeText(this, "SOS Activated", Toast.LENGTH_SHORT).show();
    }

    private void disableSOS() {
        stopRecording();
        updateSOSUI(false);
        sosTriggerRef.setValue(false);
        Toast.makeText(this, "SOS Cancelled", Toast.LENGTH_SHORT).show();
    }

    private void updateSOSUI(boolean active) {
        if (active) {
            txtSos.setText("Disable\nSOS\nSignal");
            txtSos.setGravity(Gravity.CENTER);
            txtSos.setTextSize(24);
            txtSos.setTextColor(Color.WHITE);
            btnSOS.setImageResource(R.drawable.sos_background);
        } else {
            txtSos.setText("SOS");
            txtSos.setTextSize(40);
            txtSos.setTextColor(Color.parseColor("#ffffff"));
            btnSOS.setImageResource(R.drawable.sos_background);
        }
    }

    // -------------------- LOCATION + SMS -------------------- //

    private void fetchAndSendLocation() {
        gps.getCurrentLocation((lat, lon) -> {
            latitude = lat;
            longitude = lon;

            sendLocationToFirebase(lat, lon);

            String mapLink = "https://www.google.com/maps?q=" + lat + "," + lon;
            sendEmergencyMessageToGuardians(mapLink);
        });
    }

    private void shareLiveLocation() {
        gps.startLocationUpdatesToFirebase();
    }

    private void sendLocationToFirebase(double lat, double lng) {
        if (locationRef == null) return;

        Map<String, Object> latLng = new HashMap<>();
        latLng.put("lat", lat);
        latLng.put("lng", lng);
        locationRef.updateChildren(latLng);
    }

    private void sendEmergencyMessageToGuardians(String message) {
        sendSMSIfValid(G1, message);
        sendSMSIfValid(G2, message);
        sendSMSIfValid(G3, message);
    }

    private void sendSMSIfValid(String phone, String message) {
        if (phone == null || phone.trim().isEmpty() || phone.equalsIgnoreCase("No Number Found")) return;

        if (!hasPermission(Manifest.permission.SEND_SMS)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE);
            return;
        }

        try {
            android.telephony.SmsManager.getDefault().sendTextMessage(phone, null, message, null, null);
        } catch (Exception e) {
            Log.e(TAG, "SMS error: " + e.getMessage());
        }
    }

    // -------------------- SIREN -------------------- //

    private void toggleSiren() {
        if (sirenPlayer == null) sirenPlayer = MediaPlayer.create(this, R.raw.siren);

        if (isSirenOn) {
            stopSiren();
        } else {
            startSiren();
        }
        isSirenOn = !isSirenOn;
    }

    private void startSiren() {
        try {
            sirenPlayer.setLooping(true);
            sirenPlayer.start();
            btnActivateSiren.setImageResource(R.drawable.sirenon);
        } catch (Exception e) {
            Log.e(TAG, "Siren start error: " + e.getMessage());
        }
    }

    private void stopSiren() {
        try {
            if (sirenPlayer.isPlaying()) sirenPlayer.stop();
            sirenPlayer.release();
            sirenPlayer = MediaPlayer.create(this, R.raw.siren);
            btnActivateSiren.setImageResource(R.drawable.sirenoff);
        } catch (Exception e) {
            Log.e(TAG, "Siren stop error: " + e.getMessage());
        }
    }

    private void releaseSirenPlayer() {
        try {
            if (sirenPlayer != null) {
                if (sirenPlayer.isPlaying()) sirenPlayer.stop();
                sirenPlayer.release();
                sirenPlayer = null;
            }
        } catch (Exception ignored) {}
    }

    // -------------------- AUDIO RECORDING -------------------- //

    private void startRecording() {
        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
            return;
        }

        try {
            ContentValues values = new ContentValues();
            String fileName = "SafeHerRecording_" + System.currentTimeMillis() + ".m4a";

            values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4");
            values.put(MediaStore.Audio.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MUSIC + "/SafeHerRecordings");

            audioUri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            if (audioUri == null) return;

            FileDescriptor fd = getContentResolver()
                    .openFileDescriptor(audioUri, "w")
                    .getFileDescriptor();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(fd);

            recorder.prepare();
            recorder.start();
            isRecording = true;

        } catch (Exception e) {
            Log.e(TAG, "Recording error: " + e.getMessage());
            releaseRecorder();
        }
    }

    private void stopRecording() {
        if (recorder == null || !isRecording) return;

        try {
            recorder.stop();
        } catch (Exception e) {
            Log.e(TAG, "Stop recording error: " + e.getMessage());
        } finally {
            releaseRecorder();
        }
    }

    private void releaseRecorder() {
        try {
            if (recorder != null) {
                recorder.release();
                recorder = null;
            }
        } catch (Exception ignored) {}

        isRecording = false;
    }

    // -------------------- PERMISSIONS -------------------- //

    private boolean checkAndRequestPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (!hasPermission(permission)) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    // -------------------- UI HELPERS -------------------- //

    private void triggerHapticFeedback() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }

    // -------------------- LOGOUT -------------------- //

    private void logoutWithCustomPopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_custom_logout_popup);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupTitle = dialog.findViewById(R.id.popupTitle);
        TextView popupMessage = dialog.findViewById(R.id.popupMessage);
        Button confirmButton = dialog.findViewById(R.id.ConfirmButton);
        Button cancelButton = dialog.findViewById(R.id.CancelButton);

        popupTitle.setText("Logout");
        popupMessage.setText("Click OK to Confirm Logout");

        confirmButton.setOnClickListener(v -> {
            progressDialog.show();
            auth.signOut();
            prefs.edit().remove("isUserLoggedIn").apply();
            progressDialog.dismiss();

            Intent intent = new Intent(this, LandingPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
