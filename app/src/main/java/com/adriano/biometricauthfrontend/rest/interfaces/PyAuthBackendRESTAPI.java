package com.adriano.biometricauthfrontend.rest.interfaces;

import com.adriano.biometricauthfrontend.rest.pojo.BiometricAuthenticationChallengePOJO;
import com.adriano.biometricauthfrontend.rest.pojo.BiometricTokenPOJO;
import com.adriano.biometricauthfrontend.rest.pojo.LoginPOJO;
import com.adriano.biometricauthfrontend.rest.pojo.RefreshAuthTokenPOJO;
import com.adriano.biometricauthfrontend.rest.pojo.RegisterPOJO;
import com.adriano.biometricauthfrontend.rest.pojo.UserPOJO;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PyAuthBackendRESTAPI {
    @POST("login")
    @FormUrlEncoded
    Call<LoginPOJO> login(@Field("username") String username, @Field("password") String password);
    @POST("biometricLogin")
    @FormUrlEncoded
    Call<LoginPOJO> biometricLogin(@Field("userId") String userId,
                                   @Field("biometricToken") String biometricToken);
    @POST("login/refresh")
    @FormUrlEncoded
    Call<RefreshAuthTokenPOJO> refresh(@Field("refresh") String refreshToken);
    @POST("register")
    @FormUrlEncoded
    Call<RegisterPOJO> register(@Field("username") String username, @Field("email") String email,
                                @Field("password") String password);
    @GET("userDetails")
    Call<UserPOJO> getUserDetails();

    @POST("getBiometricToken")
    @FormUrlEncoded
    Call<BiometricTokenPOJO> getBiometricToken(@Field("serverBiometricChallenge") String serverBiometricChallenge,
                                               @Field("signedBiometricChallenge") String signedBiometricChallenge,
                                               @Field("nonce") String nonce,
                                               @Field("publicKey") String publicKey);
    @GET("getBiometricChallenge")
    Call<BiometricAuthenticationChallengePOJO> getBiometricChallenge();
    @POST("logout")
    @FormUrlEncoded
    Call<Void> logout(@Field("refresh") String refreshToken);
}
