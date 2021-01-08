package com.adriano.biometricauthfrontend.rest.callbacks;


import com.adriano.biometricauthfrontend.rest.responses.EnrollBiometricAuthPkResponse;

public interface EnrollBiometricAuthenticationPkCallback {
    void onEnrollBiometricAuthPkResponse(EnrollBiometricAuthPkResponse
                                                           enrollBiometricAuthPkResponse);
}
