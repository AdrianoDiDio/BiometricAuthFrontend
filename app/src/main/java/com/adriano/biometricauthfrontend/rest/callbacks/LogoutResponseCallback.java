package com.adriano.biometricauthfrontend.rest.callbacks;

import com.adriano.biometricauthfrontend.rest.responses.LogoutResponse;

public interface LogoutResponseCallback {
    void onLogoutResponseCallback(LogoutResponse logoutResponse);
}
