package com.adriano.biometricauthfrontend.users;

import android.os.Parcel;
import android.os.Parcelable;

import com.adriano.biometricauthfrontend.utils.JWTUtils;

import timber.log.Timber;

public class UserInfo implements Parcelable {
    private String accessToken;
    private String refreshToken;
//    private int userId;

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
        dest.writeString(refreshToken);
//        dest.writeInt(userId);
    }


    public int getUserID() {
        String JWTAccess = null;
        try {
            JWTAccess = JWTUtils.getJWTBodyAsJSON(accessToken).getString("UserId");
            return Integer.parseInt(JWTAccess);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

//    public int getUserId() {
//        return userId;
//    }
//
//    public void setUserId(int userId) {
//        this.userId = userId;
//    }

    protected UserInfo(Parcel in) {
        accessToken = in.readString();
        refreshToken = in.readString();
//        userId = in.readInt();
    }

    public UserInfo(String accessToken,String refreshToken,String userId) {
        if( accessToken == null || refreshToken == null || userId == null ) {
            Timber.d("Invalid User Info passed.");
            this.accessToken = "";
            this.refreshToken = "";
        } else {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
