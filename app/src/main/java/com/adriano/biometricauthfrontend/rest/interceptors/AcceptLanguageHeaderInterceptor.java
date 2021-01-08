package com.adriano.biometricauthfrontend.rest.interceptors;

import android.os.Build;
import android.os.LocaleList;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class AcceptLanguageHeaderInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest;
        Request newRequest;
        originalRequest = chain.request();
        newRequest = originalRequest
                .newBuilder()
                .header("Accept-Language",getLanguage())
                .build();
        return chain.proceed(newRequest);
    }

    private String getLanguage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return LocaleList.getDefault().toLanguageTags();
        } else {
            return Locale.getDefault().getLanguage();
        }
    }
}
