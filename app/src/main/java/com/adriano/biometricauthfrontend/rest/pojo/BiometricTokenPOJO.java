package com.adriano.biometricauthfrontend.rest.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BiometricTokenPOJO {
    @SerializedName("biometricToken")
    @Expose
    private String biometricToken;
    @SerializedName("userId")
    @Expose
    private String userId;

    public String getBiometricToken() {
        return biometricToken;
    }

    public void setBiometricToken(String biometricToken) {
        this.biometricToken = biometricToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
