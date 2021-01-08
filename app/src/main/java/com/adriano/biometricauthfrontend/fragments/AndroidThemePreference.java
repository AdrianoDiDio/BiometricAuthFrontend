package com.adriano.biometricauthfrontend.fragments;

import androidx.appcompat.app.AppCompatDelegate;

public enum AndroidThemePreference {
    MODE_NIGHT_FOLLOW_SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    MODE_NIGHT_FOLLOW_BATTERY_SAVER(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY),
    MODE_NIGHT_NO(AppCompatDelegate.MODE_NIGHT_NO),
    MODE_NIGHT_YES(AppCompatDelegate.MODE_NIGHT_YES);

    private final int nightModeValue;

    AndroidThemePreference(int nightModeValue) {
        this.nightModeValue = nightModeValue;
    }
}
