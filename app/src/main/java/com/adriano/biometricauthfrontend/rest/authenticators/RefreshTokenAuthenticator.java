package com.adriano.biometricauthfrontend.rest.authenticators;

import com.adriano.biometricauthfrontend.rest.clients.PyAuthBackendRESTClient;
import com.adriano.biometricauthfrontend.rest.interfaces.PyAuthBackendRESTAPI;
import com.adriano.biometricauthfrontend.rest.pojo.RefreshAuthTokenPOJO;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RefreshTokenAuthenticator implements Authenticator {
    private PyAuthBackendRESTClient pyAuthBackendRESTClient;

    /*
    * NOTE(Adriano):If we don't use another Retrofit instance
    *               we can get trapped in a loop due to the server
    *               returning 401 when the refresh token expires thus
    *               making okhttp to invoke another authenticator till
    *               it crashes...
    * */
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        retrofit2.Response<RefreshAuthTokenPOJO> retrofitResponse;

        Retrofit retrofit = pyAuthBackendRESTClient.instanceRetrofitClient(false);
        retrofitResponse = retrofit.create(PyAuthBackendRESTAPI.class).
                refresh(pyAuthBackendRESTClient.getRefreshToken()).execute();

        RefreshAuthTokenPOJO refreshAuthTokenPOJO = retrofitResponse.body();
        if (retrofitResponse.isSuccessful() && refreshAuthTokenPOJO != null) {
            pyAuthBackendRESTClient.updateAccessToken(refreshAuthTokenPOJO.getAccess());
            return response.request().newBuilder()
                .header("Authorization", "Bearer " + refreshAuthTokenPOJO.getAccess())
                .build();
        }
        return null;
    }

    public RefreshTokenAuthenticator(PyAuthBackendRESTClient pyAuthBackendRESTClient) {
        this.pyAuthBackendRESTClient = pyAuthBackendRESTClient;
    }
}
