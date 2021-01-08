package com.adriano.biometricauthfrontend.utils;

import android.util.Base64;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import timber.log.Timber;

public class JWTUtils {
    public static JSONObject getJWTBodyAsJSON(String JWTEncoded) throws Exception {
        try {
            String[] split = JWTEncoded.split("\\.");
            return new JSONObject(getJson(split[1]));
        } catch (UnsupportedEncodingException e) {
            Timber.d("getJWTBodyAsJSON:Unsupported encoding...");
        }
        return null;
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
