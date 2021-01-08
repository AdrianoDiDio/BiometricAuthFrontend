package com.adriano.biometricauthfrontend.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.adriano.biometricauthfrontend.R;
import com.adriano.biometricauthfrontend.biometrics.BiometricAuthenticationCallbackAdapter;
import com.adriano.biometricauthfrontend.biometrics.RSAKeyStoreManager;
import com.adriano.biometricauthfrontend.biometrics.callbacks.BiometricAuthenticationFailed;
import com.adriano.biometricauthfrontend.biometrics.callbacks.BiometricAuthenticationSucceded;
import com.adriano.biometricauthfrontend.utils.BiometricUtils;
import com.adriano.biometricauthfrontend.utils.Utils;
import com.adriano.biometricauthfrontend.activities.MainActivity;
import com.adriano.biometricauthfrontend.rest.callbacks.LoginResponseCallback;
import com.adriano.biometricauthfrontend.rest.clients.PyAuthBackendRESTClient;
import com.adriano.biometricauthfrontend.rest.responses.LoginResponse;
import com.adriano.biometricauthfrontend.users.UserInfo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import timber.log.Timber;

public class LoginFragment extends Fragment implements View.OnClickListener,
        TextView.OnEditorActionListener, LoginResponseCallback, BiometricAuthenticationSucceded,
        BiometricAuthenticationFailed {
    private TextInputLayout usernameTextInputLayout;
    private TextInputLayout passwordTextInputLayout;
    private MaterialButton loginButton;
    private MaterialButton biometricLoginButton;
    private MaterialButton registerButton;
    private PyAuthBackendRESTClient pyAuthBackendRESTClient;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private AutofillManager autofillManager;
    private RSAKeyStoreManager rsaKeyStoreManager;

    private void invalidateBiometricPreferences() {
        String authPreference = getString(R.string.key_biometric_auth_preference);
        String biometricTokenPreferenceKey = getString(R.string.key_biometric_token);
        sharedPreferences.edit().putBoolean(authPreference,false).apply();
        sharedPreferences.edit().putString(biometricTokenPreferenceKey,"").apply();
        biometricLoginButton.setVisibility(View.GONE);
        Utils.setProgressBarVisibility(progressBar,getActivity(),false);
    }
    @Override
    public void onLoginResponseResult(LoginResponse loginResponse) {
        Utils.setProgressBarVisibility(progressBar,getActivity(),false);

        if( loginResponse.getRequestResponseCode() != 200 ) {
            if( loginResponse.getRequestResponseCode() == 401 ) {
                Utils.displayWarningDialog(loginResponse.printErrors(),getContext());
            } else {
                Utils.displayWarningDialog(getString(R.string.server_unreachable_error),getContext());
            }
        } else {
            autofillManager.commit();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            UserInfo userInfo = new UserInfo(loginResponse.getAuthToken(), loginResponse.getRefreshToken(),
                    loginResponse.getUserID());
            String userId = sharedPreferences.getString(
                    getString(R.string.key_biometric_user_info),"");
            if( !String.valueOf(userInfo.getUserID()).equals(userId) ) {
                Timber.d("Invalidating since enroll was done for another user...");
                invalidateBiometricPreferences();
            }
            intent.putExtra(MainActivity.USER_INFO_INTENT_KEY,userInfo);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }
    }

    @Override
    public void onBiometricAuthenticationFailed(int errorCode, String message) {
        if( errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_CANCELED) {
            //User cancelled it...
            Utils.setProgressBarVisibility(progressBar,getActivity(),false);
        }
    }

    @Override
    public void onBiometricAuthenticationSucceeded(BiometricPrompt.AuthenticationResult authenticationResult,Bundle bundle) {
        try {
            byte[] decryptedBiometricToken;
            byte[] decodedBiometricToken;

            String biometricToken = sharedPreferences.getString(
                    getString(R.string.key_biometric_token),"");
            String userId = sharedPreferences.getString(
                    getString(R.string.key_biometric_user_info),"");

            decodedBiometricToken = Utils.decodeBase64(biometricToken);
            decryptedBiometricToken = authenticationResult.getCryptoObject().getCipher().
                    doFinal(decodedBiometricToken);
            String base64DecryptedToken = Utils.encodeBase64(decryptedBiometricToken);

            pyAuthBackendRESTClient.biometricLogin(userId, base64DecryptedToken,
                    LoginFragment.this);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            invalidateBiometricPreferences();
        }
    }

    private boolean doLogin() {
        if( !validateLoginFormAndSetErrors() ) {
            return false;
        }
        Utils.setProgressBarVisibility(progressBar,getActivity(),true);
        pyAuthBackendRESTClient.login(usernameTextInputLayout.getEditText().getText().toString(),
                passwordTextInputLayout.getEditText().getText().toString(),this);
        return true;
    }

    private void biometricLogin() {
        Executor executor;
        BiometricAuthenticationCallbackAdapter biometricAuthenticationCallbackAdapter;

        Utils.setProgressBarVisibility(progressBar,getActivity(),true);

        if( rsaKeyStoreManager.hasKeyPairExpired() ) {
            Utils.displayWarningDialog(getString(R.string.expired_biometric_token),getContext());
            invalidateBiometricPreferences();
            return;
        }
        executor = ContextCompat.getMainExecutor(getContext());
        biometricAuthenticationCallbackAdapter = new BiometricAuthenticationCallbackAdapter();

        biometricAuthenticationCallbackAdapter.registerBiometricAuthenticationSucceededCallback(
                this,null);
        biometricAuthenticationCallbackAdapter.registerBiometricAuthenticationFailedCallback(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginFragment.this, executor,
                biometricAuthenticationCallbackAdapter);
        BiometricPrompt.PromptInfo promptInfo = BiometricUtils.buildBiometricPromptInfo(
                getString(R.string.biometric_prompt_login_title),
                getString(R.string.biometric_prompt_login_subtitle),
                getString(R.string.biometric_prompt_cancel)
        );
        Cipher cipher = rsaKeyStoreManager.getDecryptCipher();
        if( cipher == null ) {
            invalidateBiometricPreferences();
            Utils.displayWarningDialog(getString(R.string.corrupted_biometric_token),getContext());
            return;
        }
        biometricPrompt.authenticate(promptInfo,
                new BiometricPrompt.CryptoObject(cipher));
    }

    private boolean validateLoginFormAndSetErrors() {
        boolean loginValid;
        loginValid = true;
        usernameTextInputLayout.setError(null);
        passwordTextInputLayout.setError(null);
        if(Utils.isEditTextEmpty(usernameTextInputLayout.getEditText().getText())) {
            usernameTextInputLayout.setError(getString(R.string.empty_username_error));
            loginValid = false;
        }
        if( Utils.isEditTextEmpty(passwordTextInputLayout.getEditText().getText())) {
            passwordTextInputLayout.setError(getString(R.string.empty_password_error));
            loginValid = false;
        }
        return loginValid;
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        boolean loginOk = true;
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            loginOk = doLogin();
            if( loginOk ) {
                passwordTextInputLayout.getEditText().clearFocus();
            }
        }
        // If Login is ok then return false to close the keyboard
        // otherwise return true to keep editing the text...
        return !loginOk;
    }

    @Override
    public void onClick(View view) {
        if( view.getId() == loginButton.getId() ) {
            doLogin();
        }
        if( view.getId() == biometricLoginButton.getId() ) {
            biometricLogin();
        }
        if( view.getId() == registerButton.getId() ) {
            FragmentTransaction fragmentTransaction;
            fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.FragmentContainer,new RegisterFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    private void initComponents(View view) {
        loginButton = view.findViewById(R.id.LoginButton);
        loginButton.setOnClickListener(this);
        biometricLoginButton = view.findViewById(R.id.BiometricLoginButton);
        biometricLoginButton.setOnClickListener(this);
        registerButton = view.findViewById(R.id.RegisterButton);
        registerButton.setOnClickListener(this);
        usernameTextInputLayout = view.findViewById(R.id.LoginUsernameTextInputLayout);
        passwordTextInputLayout = view.findViewById(R.id.LoginPasswordTextInputLayout);
        passwordTextInputLayout.getEditText().setOnEditorActionListener(this);
        progressBar = view.findViewById(R.id.LoginProgressBar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences (getContext());
        if( sharedPreferences.getBoolean(getString(R.string.key_biometric_auth_preference),false) ) {
            biometricLoginButton.setVisibility(View.VISIBLE);
        } else {
            biometricLoginButton.setVisibility(View.GONE);
        }

        autofillManager = getContext().getSystemService(AutofillManager.class);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_login, container, false);
        initComponents(view);
        pyAuthBackendRESTClient = new PyAuthBackendRESTClient(getContext());
        rsaKeyStoreManager = new RSAKeyStoreManager(getContext());
        return view;
    }
}
