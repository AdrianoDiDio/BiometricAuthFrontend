package com.adriano.biometricauthfrontend.rest.callbacks;

import com.adriano.biometricauthfrontend.rest.responses.LoginResponse;

public interface LoginResponseCallback {
    void onLoginResponseResult(LoginResponse loginResponse);
}
