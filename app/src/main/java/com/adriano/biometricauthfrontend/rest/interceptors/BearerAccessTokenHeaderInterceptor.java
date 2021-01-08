package com.adriano.biometricauthfrontend.rest.interceptors;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class BearerAccessTokenHeaderInterceptor implements Interceptor {
    private String accessToken;
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request newRequest;
        newRequest = chain.request()
                .newBuilder()
                .addHeader("Authorization","Bearer " + accessToken)
                .build();
        return chain.proceed(newRequest);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public BearerAccessTokenHeaderInterceptor(String accessToken) {
        this.accessToken = accessToken;
    }
}
