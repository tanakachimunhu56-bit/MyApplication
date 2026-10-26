package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        // SharedPreferences stores small settings that survive closing the app
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        Switch switchExpiry = findViewById(R.id.switchExpiry);
        switchExpiry.setChecked(prefs.getBoolean("expiry_alerts", true));
        switchExpiry.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean("expiry_alerts", isChecked).apply());

        NavHelper.setup(this, R.id.nav_settings);
    }
}
