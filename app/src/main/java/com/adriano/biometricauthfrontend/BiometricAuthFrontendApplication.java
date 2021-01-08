package com.adriano.biometricauthfrontend;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.adriano.biometricauthfrontend.utils.Utils;

import timber.log.Timber;

public class BiometricAuthFrontendApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if( BuildConfig.DEBUG ) {
            Timber.plant(new Timber.DebugTree());
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Utils.setThemeFromSharedPreferences(sharedPreferences,getResources());
    }
}
