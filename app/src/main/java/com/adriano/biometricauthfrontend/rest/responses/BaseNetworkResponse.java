package com.adriano.biometricauthfrontend.rest.responses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class BaseNetworkResponse {
    protected ArrayList<String> errorStrings;
    private int requestResponseCode;

    public void setErrors(JSONObject jsonObject) {
        try {
            if( !jsonObject.isNull("detail") ) {
                errorStrings.add(jsonObject.getString("detail"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String printErrors() {
        StringBuilder stringBuilder = new StringBuilder();
        for( String string : errorStrings ) {
            stringBuilder.append(string);
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    protected ArrayList<String> dumpJSonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<String> result = new ArrayList<>();
        if( jsonArray == null ) {
            return result;
        }
        for( int i = 0; i < jsonArray.length(); i++ ) {
            result.add(jsonArray.getString(i));
        }
        return result;
    }

    private void addError(String error) {
        if( error == null ) {
            return;
        }
        errorStrings.add(error);
    }

    public int getRequestResponseCode() {
        return requestResponseCode;
    }

    public void setRequestResponseCode(int requestResponseCode) {
        this.requestResponseCode = requestResponseCode;
    }

    public BaseNetworkResponse() {
        errorStrings = new ArrayList<>();
        requestResponseCode = 0;
    }
}
