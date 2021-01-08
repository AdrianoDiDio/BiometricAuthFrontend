package com.adriano.biometricauthfrontend.rest.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RefreshAuthTokenPOJO {
    @SerializedName("access")
    @Expose
    private String access;


    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }
}
