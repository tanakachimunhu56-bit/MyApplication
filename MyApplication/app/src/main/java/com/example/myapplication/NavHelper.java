package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Wires up the bottom navigation bar on each main screen
public class NavHelper {

    public static void setup(Activity activity, int selectedItemId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        if (nav == null) {
            return;
        }
        nav.setSelectedItemId(selectedItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true; // already on this screen
            }
            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_suggested) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }
            // Intent: tells Android which screen to open next
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
            activity.finish();
            return true;
        });
    }
}
