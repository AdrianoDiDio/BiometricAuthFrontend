package com.adriano.biometricauthfrontend.rest.callbacks;

import com.adriano.biometricauthfrontend.rest.responses.UserInfoResponse;

public interface UserInfoResponseCallback {
    void onUserInfoResponseCallback(UserInfoResponse userInfoResponse);
}
