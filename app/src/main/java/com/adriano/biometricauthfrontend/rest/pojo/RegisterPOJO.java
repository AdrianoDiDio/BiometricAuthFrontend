package com.adriano.biometricauthfrontend.rest.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterPOJO {
    @SerializedName("user")
    @Expose
    private UserPOJO user;

    public UserPOJO getUser() {
        return user;
    }

    public void setUser(UserPOJO user) {
        this.user = user;
    }
}
