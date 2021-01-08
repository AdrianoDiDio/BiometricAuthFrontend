package com.adriano.biometricauthfrontend.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.text.Editable;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.preference.SwitchPreferenceCompat;

import com.adriano.biometricauthfrontend.R;
import com.adriano.biometricauthfrontend.fragments.AndroidThemePreference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Utils {

    public static String encodeBase64(byte[] base64ByteArray) {
        return Base64.encodeToString(base64ByteArray,
                Base64.DEFAULT | Base64.URL_SAFE  | Base64.NO_WRAP);
    }
    public static byte[] decodeBase64(String base64String) {
        return Base64.decode(base64String.getBytes(),
                Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP);
    }

    public static void setProgressBarVisibility(ProgressBar progressBar, Activity activity, boolean visible) {
        if( visible ) {
            progressBar.setVisibility(View.VISIBLE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            return;
        }
        progressBar.setVisibility(View.INVISIBLE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    public static void setThemeFromSharedPreferences(SharedPreferences sharedPreferences, Resources resources) {
        String[] darkModeValues = resources.getStringArray(R.array.theme_values);
        String darkModeString;
        String darkModeValue;
        AndroidThemePreference androidThemePreference;
        darkModeString = resources.getString(R.string.key_dark_mode_preference);
        darkModeValue = sharedPreferences.getString(darkModeString,darkModeValues[0]);
        androidThemePreference = AndroidThemePreference.valueOf(darkModeValue);
        switch (androidThemePreference) {
            case MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_NIGHT_FOLLOW_SYSTEM:
            case MODE_NIGHT_FOLLOW_BATTERY_SAVER:
            default:
                if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
                break;
        }
    }

    public static boolean isEmailValid(CharSequence charSequence) {
        if( charSequence == null ) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
    }
    public static boolean isEditTextEmpty(Editable editable) {
        return editable.toString().trim().length() <= 0;
    }
    public static MaterialAlertDialogBuilder displayInfoDialog(String message, Context context) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder;
        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setTitle(R.string.title_info_dialog);
        materialAlertDialogBuilder.setMessage(message);
        materialAlertDialogBuilder.setPositiveButton(android.R.string.ok, null);
        materialAlertDialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        materialAlertDialogBuilder.show();
        return materialAlertDialogBuilder;
    }
    public static MaterialAlertDialogBuilder displayWarningDialog(String message, Context context) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder;
        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setTitle(R.string.title_warning_dialog);
        materialAlertDialogBuilder.setMessage(message);
        materialAlertDialogBuilder.setPositiveButton(android.R.string.ok, null);
        materialAlertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        materialAlertDialogBuilder.show();
        return materialAlertDialogBuilder;
    }
}
