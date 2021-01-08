package com.adriano.biometricauthfrontend.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import com.adriano.biometricauthfrontend.R;

public class BiometricUtils {

    public static BiometricPrompt.PromptInfo buildBiometricPromptInfo(String title, String subtitle,
                                                                      String cancelButtonText) {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(cancelButtonText)
                .build();
        return promptInfo;
    }

    public static boolean isBiometricAuthenticationSupported(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int biometry;
        biometry = biometricManager.canAuthenticate();
        switch ( biometry ) {
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return false;
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
        }
        return false;
    }

    public static boolean userHasToEnrollBiometrics(Context context) {
        return BiometricManager.from(context).canAuthenticate() ==
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED;
    }
}
