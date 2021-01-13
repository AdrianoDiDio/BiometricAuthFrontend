package com.adriano.biometricauthfrontend.rest.responses;

public class BiometricTokenResponse extends BaseNetworkResponse {
    private String biometricToken;
    private int userId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBiometricToken() {
        return biometricToken;
    }

    public void setBiometricToken(String biometricToken) {
        this.biometricToken = biometricToken;
    }

    public BiometricTokenResponse() {
        biometricToken = "";
        userId = 0;
    }
}
