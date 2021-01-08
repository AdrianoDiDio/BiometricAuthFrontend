package com.adriano.biometricauthfrontend.biometrics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.adriano.biometricauthfrontend.biometrics.callbacks.BiometricAuthenticationFailed;
import com.adriano.biometricauthfrontend.biometrics.callbacks.BiometricAuthenticationSucceded;


public class BiometricAuthenticationCallbackAdapter extends BiometricPrompt.AuthenticationCallback {
    private BiometricAuthenticationFailed biometricAuthenticationFailed;
    private BiometricAuthenticationSucceded biometricAuthenticationSucceded;
    private Bundle biometricAuthenticationSuccededBundle;

    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if( biometricAuthenticationFailed != null ) {
            biometricAuthenticationFailed.onBiometricAuthenticationFailed(errorCode,errString.toString());
        }
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if( biometricAuthenticationSucceded != null ) {
            biometricAuthenticationSucceded.onBiometricAuthenticationSucceeded(result, biometricAuthenticationSuccededBundle);
        }
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        if( biometricAuthenticationFailed != null ) {
            biometricAuthenticationFailed.onBiometricAuthenticationFailed(-1,"");
        }
    }

    public void registerBiometricAuthenticationSucceededCallback(BiometricAuthenticationSucceded
                        biometricAuthenticationSucceded,Bundle bundle) {
        this.biometricAuthenticationSucceded = biometricAuthenticationSucceded;
        this.biometricAuthenticationSuccededBundle = bundle;
    }

    public void registerBiometricAuthenticationFailedCallback(BiometricAuthenticationFailed biometricAuthenticationFailed) {
        this.biometricAuthenticationFailed = biometricAuthenticationFailed;
    }

    public BiometricAuthenticationCallbackAdapter() {
        biometricAuthenticationSucceded = null;
        biometricAuthenticationFailed = null;
    }
}
