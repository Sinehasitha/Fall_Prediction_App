package com.sineha.safefall;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar sensitivitySeekBar;
    private TextView sensitivityValue;
    private Switch smsToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sensitivitySeekBar = findViewById(R.id.sensitivitySeekBar);
        sensitivityValue = findViewById(R.id.sensitivityValueText);
        smsToggle = findViewById(R.id.smsToggle);

        SharedPreferences prefs = getSharedPreferences("SafeFallPrefs", MODE_PRIVATE);

        // Load saved values
        float savedThreshold = prefs.getFloat("accelThreshold", 20.0f);
        boolean smsEnabled = prefs.getBoolean("smsEnabled", true);

        sensitivitySeekBar.setProgress((int) savedThreshold);
        sensitivityValue.setText("Sensitivity: " + savedThreshold);
        smsToggle.setChecked(smsEnabled);

        sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float threshold = progress;
                sensitivityValue.setText("Sensitivity: " + threshold);
                prefs.edit().putFloat("accelThreshold", threshold).apply();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        smsToggle.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            prefs.edit().putBoolean("smsEnabled", isChecked).apply();
        });
    }
}
