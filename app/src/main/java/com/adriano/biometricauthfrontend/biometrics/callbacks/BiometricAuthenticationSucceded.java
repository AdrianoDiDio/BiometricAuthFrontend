package com.adriano.biometricauthfrontend.biometrics.callbacks;

import android.os.Bundle;

import androidx.biometric.BiometricPrompt;

public interface BiometricAuthenticationSucceded {
    void onBiometricAuthenticationSucceeded(BiometricPrompt.AuthenticationResult authenticationResult, Bundle bundle);
}
