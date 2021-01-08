package com.adriano.biometricauthfrontend.rest.responses;

import com.adriano.biometricauthfrontend.rest.pojo.UserPOJO;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;


public class UserInfoResponse extends BaseNetworkResponse {
    private String username;
    private String email;
    private int userId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setFromUserPOJO(UserPOJO userPOJO) {
        this.username = userPOJO.getUsername();
        this.email = userPOJO.getEmail();
        this.userId = userPOJO.getId();
        Timber.d("Got " + username + " " + email + " " + userId);
    }
    @Override
    public void setErrors(JSONObject jsonObject) {
        Timber.d("UserInfo:Settings errors from " + jsonObject.toString());
        try {
            if( !jsonObject.isNull("detail") ) {
                errorStrings.add(jsonObject.getString("detail"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public UserInfoResponse() {
        userId = -1;
        username = "";
        email = "";
    }
}
