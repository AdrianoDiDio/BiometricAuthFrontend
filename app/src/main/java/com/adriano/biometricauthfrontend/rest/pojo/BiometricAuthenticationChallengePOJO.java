package com.adriano.biometricauthfrontend.rest.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BiometricAuthenticationChallengePOJO {
    @SerializedName("biometricChallenge")
    @Expose
    private String biometricChallenge;

    public String getBiometricChallenge() {
        return biometricChallenge;
    }

    public void setBiometricChallenge(String biometricChallenge) {
        this.biometricChallenge = biometricChallenge;
    }
}
