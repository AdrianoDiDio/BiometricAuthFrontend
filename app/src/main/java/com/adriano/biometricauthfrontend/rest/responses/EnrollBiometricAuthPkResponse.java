package com.adriano.biometricauthfrontend.rest.responses;

import com.adriano.biometricauthfrontend.rest.callbacks.EnrollBiometricAuthenticationPkCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class EnrollBiometricAuthPkResponse extends BaseNetworkResponse {

    private String biometricChallenge;


    public String getBiometricChallenge() {
        return biometricChallenge;
    }

    public void setBiometricChallenge(String biometricChallenge) {
        this.biometricChallenge = biometricChallenge;
    }

    @Override
    public void setErrors(JSONObject jsonObject) {
        try {
            if( !jsonObject.isNull("publicKey") ) {
                errorStrings.addAll(dumpJSonArray(jsonObject.getJSONArray("publicKey")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public EnrollBiometricAuthPkResponse() {
        biometricChallenge = "";
    }
}
