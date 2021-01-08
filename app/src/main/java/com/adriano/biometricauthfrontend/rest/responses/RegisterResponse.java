package com.adriano.biometricauthfrontend.rest.responses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RegisterResponse extends BaseNetworkResponse {

    @Override
    public void setErrors(JSONObject jsonObject) {
        try {
            if( !jsonObject.isNull("username") ) {
                errorStrings.addAll(dumpJSonArray(jsonObject.getJSONArray("username")));
            }
            if( !jsonObject.isNull("email") ) {
                errorStrings.addAll(dumpJSonArray(jsonObject.getJSONArray("email")));
            }
            if( !jsonObject.isNull("password") ) {
                errorStrings.addAll(dumpJSonArray(jsonObject.getJSONArray("password")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public RegisterResponse() {
        super();
        errorStrings = new ArrayList<>();
    }
}
