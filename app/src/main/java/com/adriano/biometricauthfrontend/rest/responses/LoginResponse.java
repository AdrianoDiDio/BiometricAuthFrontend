package com.adriano.biometricauthfrontend.rest.responses;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class LoginResponse extends BaseNetworkResponse {
    private String authToken;
    private String refreshToken;
    private String userID;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LoginResponse() {
        super();
        authToken = "";
        refreshToken = "";
        userID = "";
    }
}
