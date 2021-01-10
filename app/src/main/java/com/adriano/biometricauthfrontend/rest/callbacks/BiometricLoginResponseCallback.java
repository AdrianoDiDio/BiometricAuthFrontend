package com.adriano.biometricauthfrontend.rest.callbacks;

import com.adriano.biometricauthfrontend.rest.responses.LoginResponse;

public interface BiometricLoginResponseCallback {
    void onBiometricLoginResponseResult(LoginResponse loginResponse);
}
