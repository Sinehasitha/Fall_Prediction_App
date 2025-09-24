package com.sineha.safefall;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;

    private TextView accelText, gyroText, fallStatus;
    private EditText phoneInput;
    private Button saveBtn;
    private Button openSettingsBtn;

    private float accelThreshold = 20.0f;
    private boolean smsEnabled = true;
    private long lastFallTime = 0;
    private static final int SMS_PERMISSION_CODE = 123;
    private String savedNumber = "";

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accelText = findViewById(R.id.accelValues);
        gyroText = findViewById(R.id.gyroValues);
        fallStatus = findViewById(R.id.fallStatus);
        phoneInput = findViewById(R.id.phoneNumberInput);
        saveBtn = findViewById(R.id.saveContactButton);
        openSettingsBtn = findViewById(R.id.openSettingsButton);  // ✅ Add this

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        requestPermissions();

        // Load saved number
        SharedPreferences prefs = getSharedPreferences("SafeFallPrefs", MODE_PRIVATE);
        savedNumber = prefs.getString("emergencyNumber", "");
        phoneInput.setText(savedNumber);

        saveBtn.setOnClickListener(v -> {
            savedNumber = phoneInput.getText().toString().trim();
            prefs.edit().putString("emergencyNumber", savedNumber).apply();
            Toast.makeText(this, "Contact Saved!", Toast.LENGTH_SHORT).show();
        });

        // Launch Settings Activity
        openSettingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE
        }, SMS_PERMISSION_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Re-load user settings each time activity resumes
        SharedPreferences prefs = getSharedPreferences("SafeFallPrefs", MODE_PRIVATE);
        accelThreshold = prefs.getFloat("accelThreshold", 20.0f);
        smsEnabled = prefs.getBoolean("smsEnabled", true);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];

            accelText.setText("Accelerometer: X=" + ax + ", Y=" + ay + ", Z=" + az);

            float acceleration = (float) Math.sqrt(ax * ax + ay * ay + az * az);


            // Detect fall
            if (acceleration > accelThreshold && System.currentTimeMillis() - lastFallTime > 5000) {
                lastFallTime = System.currentTimeMillis();
                fallStatus.setText("Fall Status: FALL DETECTED!");
                fallStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                sendSMSAlert();

                // Reset after 5 seconds
                handler.postDelayed(() -> {
                    fallStatus.setText("Fall Status: Normal");
                    fallStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                }, 5000);
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float gx = event.values[0];
            float gy = event.values[1];
            float gz = event.values[2];

            gyroText.setText("Gyroscope: X=" + gx + ", Y=" + gy + ", Z=" + gz);
        }
    }

    private void sendSMSAlert() {
        if (!smsEnabled) {
            Toast.makeText(this, "SMS alert is disabled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!savedNumber.isEmpty() && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(savedNumber, null, "🚨 Alert: A fall has been detected from SafeFall App!", null, null);
            Toast.makeText(this, "SMS Alert Sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission Denied or No Number Set", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not needed
    }
}
