package com.adriano.biometricauthfrontend.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.adriano.biometricauthfrontend.R;
import com.adriano.biometricauthfrontend.biometrics.BiometricAuthenticationCallbackAdapter;
import com.adriano.biometricauthfrontend.biometrics.RSAKeyStoreManager;
import com.adriano.biometricauthfrontend.biometrics.callbacks.BiometricAuthenticationFailed;
import com.adriano.biometricauthfrontend.biometrics.callbacks.BiometricAuthenticationSucceded;
import com.adriano.biometricauthfrontend.rest.callbacks.BiometricTokenResponseCallback;
import com.adriano.biometricauthfrontend.rest.callbacks.EnrollBiometricAuthenticationPkCallback;
import com.adriano.biometricauthfrontend.rest.clients.PyAuthBackendRESTClient;
import com.adriano.biometricauthfrontend.rest.responses.BiometricTokenResponse;
import com.adriano.biometricauthfrontend.rest.responses.EnrollBiometricAuthPkResponse;
import com.adriano.biometricauthfrontend.users.UserInfo;
import com.adriano.biometricauthfrontend.utils.BiometricUtils;
import com.adriano.biometricauthfrontend.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Random;
import java.util.concurrent.Executor;

import timber.log.Timber;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener, EnrollBiometricAuthenticationPkCallback,
        BiometricTokenResponseCallback, BiometricAuthenticationSucceded, BiometricAuthenticationFailed {
    private UserInfo userInfo;
    private SharedPreferences sharedPreferences;
    private PyAuthBackendRESTClient pyAuthBackendRESTClient;
    private RSAKeyStoreManager rsaKeyStoreManager;
    public static final String FRAGMENT_USER_INFO_BUNDLE_KEY = "UserInfoBundle";
    public static final String BIOMETRIC_CHALLENGE_BUNDLE_KEY = "BiometricChallengeBundle";
    public static final String BIOMETRIC_PUBLIC_KEY_BUNDLE_KEY = "BiometricPublicKeyBundle";

    private void invalidateBiometricTokenSettings() {
        sharedPreferences.edit().putInt(getString(R.string.key_biometric_user_id),0).apply();
    }
    private void invalidateBiometricAuthenticationSettings() {
        SwitchPreferenceCompat switchPreferenceCompat = findPreference(getString(R.string.key_biometric_auth_preference));
        switchPreferenceCompat.setChecked(false);
        invalidateBiometricTokenSettings();
    }

    @Override
    public void onBiometricAuthenticationFailed(int errorCode, String message) {
        if( errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                errorCode == BiometricPrompt.ERROR_CANCELED) {
            invalidateBiometricAuthenticationSettings();
        }
    }

    @Override
    public void onBiometricAuthenticationSucceeded(BiometricPrompt.AuthenticationResult authenticationResult,
                                                   Bundle bundle) {
        if( bundle == null ) {
            Timber.d("onBiometricAuthenticationSucceeded:Bundle is null");
            invalidateBiometricAuthenticationSettings();
            return;
        }
        try {
            int nonce = new Random().nextInt();
            String serverBiometricChallenge = bundle.getString(BIOMETRIC_CHALLENGE_BUNDLE_KEY);
            String publicKey = bundle.getString(BIOMETRIC_PUBLIC_KEY_BUNDLE_KEY);
            byte[] decodedChallenge = Base64.decode(serverBiometricChallenge.getBytes(),
                    Base64.DEFAULT | Base64.URL_SAFE);
            String signedChallenge = new String(decodedChallenge, StandardCharsets.UTF_8);
            signedChallenge += nonce;
            BiometricPrompt.CryptoObject cryptoObject = authenticationResult.getCryptoObject();
            Signature signature = cryptoObject.getSignature();
            signature.update(signedChallenge.getBytes());
            byte[] outChallenge = signature.sign();
            String encodedSignedChallenge = Base64.encodeToString(outChallenge,
                    Base64.DEFAULT | Base64.URL_SAFE);
            pyAuthBackendRESTClient.getBiometricToken(encodedSignedChallenge,nonce,publicKey,
                    SettingsFragment.this);
        } catch ( SignatureException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBiometricTokenResponseResult(BiometricTokenResponse biometricResponse) {

        if( biometricResponse.getRequestResponseCode() == 201 ) {
            sharedPreferences.edit().putString(getString(R.string.key_biometric_token),
                        biometricResponse.getBiometricToken()).apply();
            sharedPreferences.edit().putInt(getString(R.string.key_biometric_user_id),
                    biometricResponse.getUserId()).apply();
            Utils.displayInfoDialog(getString(R.string.biometric_enroll_complete),getContext());
        } else {
            if( biometricResponse.getRequestResponseCode() == 400 ) {
                Utils.displayWarningDialog(biometricResponse.printErrors(),getContext());
            } else {
                Utils.displayWarningDialog(getString(R.string.server_unreachable_error), getContext());
            }
            invalidateBiometricAuthenticationSettings();
        }
    }


    private void signBiometricChallengeAndSend(String biometricChallenge) {
        Executor executor = ContextCompat.getMainExecutor(getContext());
        BiometricAuthenticationCallbackAdapter biometricAuthenticationCallbackAdapter;
        biometricAuthenticationCallbackAdapter = new BiometricAuthenticationCallbackAdapter();
        Bundle bundle = new Bundle();
        bundle.putString(BIOMETRIC_CHALLENGE_BUNDLE_KEY,biometricChallenge);
        String encodedPublicKey = Base64.encodeToString(rsaKeyStoreManager.getPublicKey().
                        getEncoded(),
                Base64.DEFAULT | Base64.URL_SAFE  | Base64.NO_WRAP);
        bundle.putString(BIOMETRIC_PUBLIC_KEY_BUNDLE_KEY,encodedPublicKey);
        biometricAuthenticationCallbackAdapter.registerBiometricAuthenticationSucceededCallback(
                this,bundle);
        biometricAuthenticationCallbackAdapter.registerBiometricAuthenticationFailedCallback(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(SettingsFragment.this, executor,
                biometricAuthenticationCallbackAdapter);
        BiometricPrompt.PromptInfo biometricPromptInfo = BiometricUtils.buildBiometricPromptInfo(
                getString(R.string.biometric_prompt_challenge_title),
                getString(R.string.biometric_prompt_challenge_subtitle),
                getString(R.string.biometric_prompt_cancel)
        );
        Signature signature = rsaKeyStoreManager.getSignature();
        if( signature == null ) {
            Timber.d("Signature is null failed...");
            return;
        }
        biometricPrompt.authenticate(biometricPromptInfo, new BiometricPrompt.CryptoObject(signature));
    }
    @Override
    public void onEnrollBiometricAuthPkResponse(EnrollBiometricAuthPkResponse
                                                                  enrollBiometricAuthPkResponse) {
        if( enrollBiometricAuthPkResponse.getRequestResponseCode() != 200 ) {
            if( enrollBiometricAuthPkResponse.getRequestResponseCode() == 400 ) {
                Utils.displayWarningDialog(enrollBiometricAuthPkResponse.printErrors(),getContext());
            } else {
                Utils.displayWarningDialog(getString(R.string.server_unreachable_error), getContext());
            }
            invalidateBiometricAuthenticationSettings();
            return;
        }
        signBiometricChallengeAndSend(enrollBiometricAuthPkResponse.getBiometricChallenge());
    }

    private void enrollUserBiometrics(SwitchPreferenceCompat switchPreferenceCompat) {
        if( BiometricUtils.userHasToEnrollBiometrics(getContext()) ) {
            invalidateBiometricAuthenticationSettings();
            startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
            return;
        }
        //Don't let user enroll if no biometric is supported...
        if( !BiometricUtils.isBiometricAuthenticationSupported(getContext()) ) {
            invalidateBiometricAuthenticationSettings();
            return;
        }
        Timber.d("Checking key validity...");
        if( rsaKeyStoreManager.hasKeyPairExpired() ) {
            //Generate it again
            rsaKeyStoreManager.forceKeyPairRefresh();
            Timber.d("Key has expired...regenerated it");
        }
        PublicKey publicKey = rsaKeyStoreManager.getPublicKey();
        String encodedPublicKey = Base64.encodeToString(publicKey.getEncoded(),
                    Base64.DEFAULT | Base64.URL_SAFE  | Base64.NO_WRAP);
        pyAuthBackendRESTClient.enrollBiometricAuthenticationPk(this);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String darkModeString;
        darkModeString = getString(R.string.key_dark_mode_preference);
        if( key.equals(darkModeString) ) {
            Utils.setThemeFromSharedPreferences(sharedPreferences,getResources());
        }
        if( key.equals(getString(R.string.key_biometric_auth_preference))) {
            SwitchPreferenceCompat authPreference = findPreference(key);
            if( authPreference.isChecked() ) {
                Timber.d("Enroll called");
                enrollUserBiometrics(authPreference);
            } else {
                invalidateBiometricTokenSettings();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_preference);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userInfo = getArguments().getParcelable(FRAGMENT_USER_INFO_BUNDLE_KEY);
        pyAuthBackendRESTClient = new PyAuthBackendRESTClient(userInfo,getContext());
        rsaKeyStoreManager = new RSAKeyStoreManager(getContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences (getContext());
    }

    public static SettingsFragment newInstance(UserInfo userInfo) {
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(FRAGMENT_USER_INFO_BUNDLE_KEY, userInfo);
        settingsFragment.setArguments(bundle);
        return settingsFragment;
    }
}