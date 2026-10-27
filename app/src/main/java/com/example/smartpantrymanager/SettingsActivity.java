package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up the toolbar with a back arrow
        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Switch switchAlerts = findViewById(R.id.switchExpiryAlerts);

        SharedPreferences prefs = getSharedPreferences("SmartPantryPrefs", MODE_PRIVATE);
        boolean alertsEnabled = prefs.getBoolean("expiry_alerts_enabled", true);
        switchAlerts.setChecked(alertsEnabled);

        switchAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("expiry_alerts_enabled", isChecked).apply();
        });
    }

    // This makes the back arrow actually go back
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Closes this screen and goes back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}