package com.adriano.biometricauthfrontend.rest.clients;

import android.content.Context;

import com.adriano.biometricauthfrontend.BuildConfig;
import com.adriano.biometricauthfrontend.rest.authenticators.RefreshTokenAuthenticator;
import com.adriano.biometricauthfrontend.rest.callbacks.BiometricTokenResponseCallback;
import com.adriano.biometricauthfrontend.rest.callbacks.EnrollBiometricAuthenticationPkCallback;
import com.adriano.biometricauthfrontend.rest.callbacks.LoginResponseCallback;
import com.adriano.biometricauthfrontend.rest.callbacks.LogoutResponseCallback;
import com.adriano.biometricauthfrontend.rest.callbacks.RegisterResponseCallback;
import com.adriano.biometricauthfrontend.rest.callbacks.UserInfoResponseCallback;
import com.adriano.biometricauthfrontend.rest.interceptors.AcceptLanguageHeaderInterceptor;
import com.adriano.biometricauthfrontend.rest.interceptors.BearerAccessTokenHeaderInterceptor;
import com.adriano.biometricauthfrontend.rest.interfaces.PyAuthBackendRESTAPI;
import com.adriano.biometricauthfrontend.rest.pojo.BiometricAuthenticationChallengePOJO;
import com.adriano.biometricauthfrontend.rest.pojo.BiometricTokenPOJO;
import com.adriano.biometricauthfrontend.rest.pojo.LoginPOJO;
import com.adriano.biometricauthfrontend.rest.pojo.RefreshAuthTokenPOJO;
import com.adriano.biometricauthfrontend.rest.pojo.RegisterPOJO;
import com.adriano.biometricauthfrontend.rest.pojo.UserPOJO;
import com.adriano.biometricauthfrontend.rest.responses.BiometricTokenResponse;
import com.adriano.biometricauthfrontend.rest.responses.EnrollBiometricAuthPkResponse;
import com.adriano.biometricauthfrontend.rest.responses.LoginResponse;
import com.adriano.biometricauthfrontend.rest.responses.LogoutResponse;
import com.adriano.biometricauthfrontend.rest.responses.RegisterResponse;
import com.adriano.biometricauthfrontend.rest.responses.UserInfoResponse;
import com.adriano.biometricauthfrontend.users.UserInfo;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class PyAuthBackendRESTClient {
//    private Retrofit retrofit;
    private final PyAuthBackendRESTAPI pyAuthBackendRESTAPI;
    private String accessToken;
    private String refreshToken;
    private BearerAccessTokenHeaderInterceptor bearerAccessTokenHeaderInterceptor;
    private Context context;
    private static final String BASE_URL = "https://adrianodd.pythonanywhere.com/api/";

    public PyAuthBackendRESTAPI getPyAuthBackendRESTAPI() {
        return pyAuthBackendRESTAPI;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
        bearerAccessTokenHeaderInterceptor.setAccessToken(accessToken);
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void getUserInfo(final UserInfoResponseCallback userInfoResponseCallback) {
        Call<UserPOJO> getUserInfoPOJOCall;
        if( userInfoResponseCallback == null ) {
            Timber.d("PyAuthBackendRESTClient:getUserInfo null response callback");
            return;
        }
        getUserInfoPOJOCall = pyAuthBackendRESTAPI.getUserDetails();
        getUserInfoPOJOCall.enqueue(new Callback<UserPOJO>() {
            @Override
            public void onResponse(Call<UserPOJO> call, Response<UserPOJO> response) {
                UserInfoResponse userInfoResponse = new UserInfoResponse();
                userInfoResponse.setRequestResponseCode(response.code());
                if( !response.isSuccessful() ) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        userInfoResponse.setErrors(jsonObject);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    userInfoResponse.setFromUserPOJO(response.body());
                }
                userInfoResponseCallback.onUserInfoResponseCallback(userInfoResponse);
            }

            @Override
            public void onFailure(Call<UserPOJO> call, Throwable t) {
                if( t instanceof IOException ) {
                    Timber.d("getUserInfo:onFailure casued by a network error maybe.");
                } else {
                    Timber.d("getUserInfo:Couldn't parse response");
                }
                UserInfoResponse userInfoResponse = new UserInfoResponse();
                userInfoResponse.setRequestResponseCode(503);
                userInfoResponseCallback.onUserInfoResponseCallback(userInfoResponse);
            }
        });
    }

    public void enrollBiometricAuthenticationPk(String publicKey,
                                                final EnrollBiometricAuthenticationPkCallback
                                                               enrollBiometricAuthenticationPkCallback) {
        Call<BiometricAuthenticationChallengePOJO> enrollBiometricAuthenticationPkCall;
        if( enrollBiometricAuthenticationPkCallback == null ) {
            Timber.d("PyAuthBackendRESTClient:enrollBiometricAuthenticationPublicKey null response callback");
            return;
        }
        enrollBiometricAuthenticationPkCall = pyAuthBackendRESTAPI.initBiometricAuthentication(publicKey);
        enrollBiometricAuthenticationPkCall.enqueue(new Callback<BiometricAuthenticationChallengePOJO>() {
            @Override
            public void onResponse(Call<BiometricAuthenticationChallengePOJO> call,
                                   Response<BiometricAuthenticationChallengePOJO> response) {
                EnrollBiometricAuthPkResponse enrollBiometricAuthPkResponse =
                        new EnrollBiometricAuthPkResponse();
                enrollBiometricAuthPkResponse.setRequestResponseCode(response.code());
                if( response.isSuccessful() ) {
                    enrollBiometricAuthPkResponse.setBiometricChallenge(response.body().getBiometricChallenge());
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        enrollBiometricAuthPkResponse.setErrors(jsonObject);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                enrollBiometricAuthenticationPkCallback.onEnrollBiometricAuthPkResponse(enrollBiometricAuthPkResponse);
            }

            @Override
            public void onFailure(Call<BiometricAuthenticationChallengePOJO> call, Throwable t) {
                if( t instanceof IOException ) {
                    Timber.d("enrollBiometricAuthenticationPublicKey:onFailure caused by a network error maybe.");
                } else {
                    Timber.d("enrollBiometricAuthenticationPublicKey:Couldn't parse response");
                }
                EnrollBiometricAuthPkResponse enrollBiometricAuthPkResponse =
                        new EnrollBiometricAuthPkResponse();
                enrollBiometricAuthPkResponse.setRequestResponseCode(503);
                enrollBiometricAuthenticationPkCallback.onEnrollBiometricAuthPkResponse(enrollBiometricAuthPkResponse);
            }
        });
    }

    public void getBiometricToken(String serverBiometricChallenge,String signedBiometricChallenge,
                                  String nonce,
                                  final BiometricTokenResponseCallback biometricTokenResponseCallback) {
        Call<BiometricTokenPOJO> getBiometricTokenCall;
        if( biometricTokenResponseCallback == null ) {
            Timber.d("PyAuthBackendRESTClient:getBiometricToken null response callback");
            return;
        }
        getBiometricTokenCall = pyAuthBackendRESTAPI.getBiometricToken(serverBiometricChallenge,
                signedBiometricChallenge,nonce);
        getBiometricTokenCall.enqueue(new Callback<BiometricTokenPOJO>() {
            @Override
            public void onResponse(Call<BiometricTokenPOJO> call, Response<BiometricTokenPOJO> response) {
                BiometricTokenResponse biometricTokenResponse = new BiometricTokenResponse();
                biometricTokenResponse.setRequestResponseCode(response.code());
                if( !response.isSuccessful() ) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        biometricTokenResponse.setErrors(jsonObject);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    biometricTokenResponse.setBiometricToken(response.body().getBiometricToken());
                    biometricTokenResponse.setUserId(response.body().getUserId());
                }
                biometricTokenResponseCallback.onBiometricTokenResponseResult(biometricTokenResponse);
            }

            @Override
            public void onFailure(Call<BiometricTokenPOJO> call, Throwable t) {
                if( t instanceof IOException ) {
                    Timber.d("getBiometricToken:onFailure casued by a network error maybe.");
                } else {
                    Timber.d("getBiometricToken:Couldn't parse response");
                }
                BiometricTokenResponse biometricTokenResponse = new BiometricTokenResponse();
                biometricTokenResponse.setRequestResponseCode(503);
                biometricTokenResponseCallback.onBiometricTokenResponseResult(biometricTokenResponse);
            }
        });
    }

    public Response<RefreshAuthTokenPOJO> syncRefreshAuthToken() throws IOException {
        Call<RefreshAuthTokenPOJO> refreshAuthTokenPOJOCall;
        if( refreshToken == null ) {
            Timber.d("PyAuthBackendRESTClient:Tried refresh without RefreshToken.");
            return null;
        }
        refreshAuthTokenPOJOCall = pyAuthBackendRESTAPI.refresh(refreshToken);
        return refreshAuthTokenPOJOCall.execute();
//        Response<RefreshAuthTokenPOJO> refreshAuthTokenPOJOResponse = null;
//        try {
//            refreshAuthTokenPOJOResponse = refreshAuthTokenPOJOCall.execute();
//            if( refreshAuthTokenPOJOResponse.isSuccessful() ) {
//                return refreshAuthTokenPOJOResponse.body().getAccess();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
    }
    public void register(String username, String email, String password,
                         final RegisterResponseCallback registerResponseCallback) {
        Call<RegisterPOJO> registerPOJOCall;
        if( registerResponseCallback == null ) {
            Timber.d("PyAuthBackendRESTClient:Register null response");
            return;
        }
        registerPOJOCall = pyAuthBackendRESTAPI.register(username,email,password);
        registerPOJOCall.enqueue(new Callback<RegisterPOJO>() {
            @Override
            public void onResponse(Call<RegisterPOJO> call, Response<RegisterPOJO> response) {
                RegisterResponse registerResponse = new RegisterResponse();
                registerResponse.setRequestResponseCode(response.code());
                if( !response.isSuccessful() ) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        registerResponse.setErrors(jsonObject);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                registerResponseCallback.onRegisterResponseResult(registerResponse);
            }

            @Override
            public void onFailure(Call<RegisterPOJO> call, Throwable t) {
                if( t instanceof IOException ) {
                    Timber.d("Register:onFailure casued by a network error maybe.");
                } else {
                    Timber.d("Register:Couldn't parse response");
                }
                RegisterResponse registerResponse = new RegisterResponse();
                registerResponse.setRequestResponseCode(503);
                registerResponseCallback.onRegisterResponseResult(registerResponse);
            }
        });
    }

    public void logout(final LogoutResponseCallback logoutResponseCallback) {
        Call<Void> logoutUserCall;
        logoutUserCall = pyAuthBackendRESTAPI.logout(refreshToken);
        logoutUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if( logoutResponseCallback != null ) {
                    LogoutResponse logoutResponse = new LogoutResponse();
                    logoutResponse.setRequestResponseCode(response.code());
                    logoutResponseCallback.onLogoutResponseCallback(logoutResponse);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if( t instanceof IOException ) {
                    Timber.d("logout:onFailure caused by a network error maybe.");
                } else {
                    Timber.d("logout:Couldn't parse response");
                }
                if( logoutResponseCallback != null ) {
                    LogoutResponse logoutResponse = new LogoutResponse();
                    logoutResponse.setRequestResponseCode(503);
                    logoutResponseCallback.onLogoutResponseCallback(logoutResponse);
                }
            }
        });
    }

    public void login(String username, String password, final LoginResponseCallback loginResponseCallback) {
        Call<LoginPOJO> loginPOJOCall;
        if( loginResponseCallback == null ) {
            Timber.d("PyAuthBackendRESTClient:Login null response");
            return;
        }
        loginPOJOCall = pyAuthBackendRESTAPI.login(username,password);
        loginPOJOCall.enqueue(new Callback<LoginPOJO>() {
            @Override
            public void onResponse(Call<LoginPOJO> call, Response<LoginPOJO> response) {
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setRequestResponseCode(response.code());
                if( response.isSuccessful() ) {
                    loginResponse.setAuthToken(response.body().getAccess());
                    loginResponse.setRefreshToken(response.body().getRefresh());
                } else {
                    try {
                        loginResponse.setErrors(new JSONObject(response.errorBody().string()));
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                loginResponseCallback.onLoginResponseResult(loginResponse);
            }

            @Override
            public void onFailure(Call<LoginPOJO> call, Throwable t) {
                if( t instanceof IOException ) {
                    Timber.d("Login:onFailure casued by a network error maybe.");
                } else {
                    Timber.d("Login:Couldn't parse response");
                }
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setRequestResponseCode(503);
                loginResponseCallback.onLoginResponseResult(loginResponse);
            }
        });
    }

    public void biometricLogin(String userId,String biometricToken, final LoginResponseCallback loginResponseCallback) {
        Call<LoginPOJO> loginPOJOCall;
        if( loginResponseCallback == null ) {
            Timber.d("PyAuthBackendRESTClient:Login null response");
            return;
        }
        loginPOJOCall = pyAuthBackendRESTAPI.biometricLogin(userId,biometricToken);
        loginPOJOCall.enqueue(new Callback<LoginPOJO>() {
            @Override
            public void onResponse(Call<LoginPOJO> call, Response<LoginPOJO> response) {
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setRequestResponseCode(response.code());
                if( response.isSuccessful() ) {
                    loginResponse.setAuthToken(response.body().getAccess());
                    loginResponse.setRefreshToken(response.body().getRefresh());
                } else {
                    try {
                        loginResponse.setErrors(new JSONObject(response.errorBody().string()));
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                loginResponseCallback.onLoginResponseResult(loginResponse);
            }

            @Override
            public void onFailure(Call<LoginPOJO> call, Throwable t) {
                if( t instanceof IOException ) {
                    Timber.d("Login:onFailure casued by a network error maybe.");
                } else {
                    Timber.d("Login:Couldn't parse response");
                }
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setRequestResponseCode(503);
                loginResponseCallback.onLoginResponseResult(loginResponse);
            }
        });
    }

    public Retrofit instanceRetrofitClient(boolean authRetry) {
        Retrofit retrofit;
        OkHttpClient okHttpClient;
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        if(BuildConfig.DEBUG ) {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        if( authRetry ) {
            bearerAccessTokenHeaderInterceptor = new BearerAccessTokenHeaderInterceptor(accessToken);
//            CookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(),new SharedPrefsCookiePersistor(context));
            okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .addInterceptor(new AcceptLanguageHeaderInterceptor())
                    .addInterceptor(bearerAccessTokenHeaderInterceptor)
                    .addInterceptor(httpLoggingInterceptor)
                    .authenticator(new RefreshTokenAuthenticator(this))
//                    .cookieJar(cookieJar)
                    .build();
        } else {
            okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .addInterceptor(new AcceptLanguageHeaderInterceptor())
                    .addInterceptor(httpLoggingInterceptor)
//                    .sslSocketFactory(getSSLContext(context).getSocketFactory(), systemDefaultTrustManager())
                    .build();
        }
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        return retrofit;
    }
    public PyAuthBackendRESTClient(UserInfo userInfo,Context context) {
        this.context = context;
        this.accessToken = userInfo.getAccessToken();
        this.refreshToken = userInfo.getRefreshToken();
        Retrofit retrofit = instanceRetrofitClient(true);
        pyAuthBackendRESTAPI = retrofit.create(PyAuthBackendRESTAPI.class);
    }
    public PyAuthBackendRESTClient(Context context) {
        this.context = context;
        Retrofit retrofit = instanceRetrofitClient(false);
        pyAuthBackendRESTAPI = retrofit.create(PyAuthBackendRESTAPI.class);
        accessToken = null;
        refreshToken = null;
    }
}
