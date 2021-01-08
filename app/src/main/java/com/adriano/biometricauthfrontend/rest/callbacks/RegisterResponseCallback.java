package com.adriano.biometricauthfrontend.rest.callbacks;

import com.adriano.biometricauthfrontend.rest.responses.LoginResponse;
import com.adriano.biometricauthfrontend.rest.responses.RegisterResponse;

public interface RegisterResponseCallback {
    void onRegisterResponseResult(RegisterResponse registerResponse);
}
