package com.adriano.biometricauthfrontend.rest.callbacks;


import com.adriano.biometricauthfrontend.rest.responses.BiometricTokenResponse;

public interface BiometricTokenResponseCallback {
    void onBiometricTokenResponseResult(BiometricTokenResponse biometricResponse);
}
