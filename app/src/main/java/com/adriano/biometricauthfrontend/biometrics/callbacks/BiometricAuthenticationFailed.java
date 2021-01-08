package com.adriano.biometricauthfrontend.biometrics.callbacks;

public interface BiometricAuthenticationFailed {
    void onBiometricAuthenticationFailed(int errorCode,String message);
}
