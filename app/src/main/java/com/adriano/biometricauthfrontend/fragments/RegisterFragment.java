package com.adriano.biometricauthfrontend.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adriano.biometricauthfrontend.R;
import com.adriano.biometricauthfrontend.utils.Utils;
import com.adriano.biometricauthfrontend.rest.callbacks.RegisterResponseCallback;
import com.adriano.biometricauthfrontend.rest.clients.PyAuthBackendRESTClient;
import com.adriano.biometricauthfrontend.rest.responses.RegisterResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;


public class RegisterFragment extends Fragment implements View.OnClickListener,
        TextView.OnEditorActionListener, RegisterResponseCallback {
    private TextInputLayout usernameTextInputLayout;
    private TextInputLayout emailTextInputLayout;
    private TextInputLayout passwordTextInputLayout;
    private TextInputLayout confirmPasswordTextInputLayout;
    private MaterialButton registerButton;
    private ProgressBar progressBar;
    private PyAuthBackendRESTClient pyAuthBackendRESTClient;

    private static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    public void onRegisterResponseResult(RegisterResponse registerResponse) {
        Utils.setProgressBarVisibility(progressBar,getActivity(),false);
        if( registerResponse.getRequestResponseCode() != 200 ) {
            if( registerResponse.getRequestResponseCode() == 400 ) {
                Utils.displayWarningDialog(
                        registerResponse.printErrors(),getContext());
            } else {
                Utils.displayWarningDialog(getString(R.string.server_unreachable_error),getContext());
            }
        } else {
            MaterialAlertDialogBuilder materialAlertDialogBuilder;
            materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
            materialAlertDialogBuilder.setTitle(R.string.register_title);
            materialAlertDialogBuilder.setMessage(getString(R.string.registration_ok));
            materialAlertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getParentFragmentManager().popBackStackImmediate();
                }
            });
            materialAlertDialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
            materialAlertDialogBuilder.show();
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        boolean registerOk = true;
        if( actionId == EditorInfo.IME_ACTION_DONE ) {
            registerOk = registerUser();
            if( registerOk ) {
                confirmPasswordTextInputLayout.getEditText().clearFocus();
            }
        }
        return !registerOk;
//        boolean loginOk = true;
//        if (actionId == EditorInfo.IME_ACTION_DONE) {
//            loginOk = DoLogin();
//            if( loginOk ) {
//                passwordTextInputLayout.getEditText().clearFocus();
//            }
//        }
//        // If Login is ok then return false to close the keyboard
//        // otherwise return true to keep editing the text...
//        return !loginOk;
    }

    @Override
    public void onClick(View view) {
        if( view.getId() == registerButton.getId() ) {
            registerUser();
        }
    }

    private boolean registerUser() {
        if( !validate() ) {
            return false;
        }
        Utils.setProgressBarVisibility(progressBar,getActivity(),true);
        pyAuthBackendRESTClient.register(usernameTextInputLayout.getEditText().getText().toString(),
                emailTextInputLayout.getEditText().getText().toString(),
                passwordTextInputLayout.getEditText().getText().toString(),this);
        return true;
    }

    private boolean validate() {
        String password;
        String confirmPassword;
        boolean registrationValid;

        registrationValid = true;
        usernameTextInputLayout.setError(null);
        emailTextInputLayout.setError(null);
        passwordTextInputLayout.setError(null);
        confirmPasswordTextInputLayout.setError(null);
        password = passwordTextInputLayout.getEditText().getText().toString();
        confirmPassword = confirmPasswordTextInputLayout.getEditText().getText().toString();
        if(Utils.isEditTextEmpty(usernameTextInputLayout.getEditText().getText())) {
            usernameTextInputLayout.setError(getString(R.string.empty_username_error));
            registrationValid = false;
        }
        if( Utils.isEditTextEmpty(emailTextInputLayout.getEditText().getText())) {
            emailTextInputLayout.setError(getString(R.string.empty_email_error));
            registrationValid = false;
        } else {
            if( !Utils.isEmailValid(emailTextInputLayout.getEditText().getText().toString()) ) {
                emailTextInputLayout.setError(getString(R.string.invalid_email_error));
                registrationValid = false;
            }
        }
        if( Utils.isEditTextEmpty(passwordTextInputLayout.getEditText().getText())) {
            passwordTextInputLayout.setError(getString(R.string.empty_password_error));
            registrationValid = false;
        } else {
            if( password.length() < MIN_PASSWORD_LENGTH ) {
                passwordTextInputLayout.setError(getString(R.string.short_password_error));
                registrationValid = false;
            }
        }
        if( Utils.isEditTextEmpty(confirmPasswordTextInputLayout.getEditText().getText())) {
            confirmPasswordTextInputLayout.setError(getString(R.string.empty_password_error));
            registrationValid = false;
        } else {
            if( !password.equals(confirmPassword) ) {
                confirmPasswordTextInputLayout.setError(getString(R.string.password_match_error));
                registrationValid = false;
            }
        }

        return registrationValid;
    }

    private void initComponents(View view) {
        registerButton = view.findViewById(R.id.RegisterActionButton);
        registerButton.setOnClickListener(this);
        usernameTextInputLayout = view.findViewById(R.id.RegisterUsernameTextInputLayout);
        emailTextInputLayout = view.findViewById(R.id.RegisterEmailTextInputLayout);
        passwordTextInputLayout = view.findViewById(R.id.RegisterPasswordTextInputLayout);
        confirmPasswordTextInputLayout = view.findViewById(R.id.RegisterPasswordConfirmTextInputLayout);
        confirmPasswordTextInputLayout.getEditText().setOnEditorActionListener(this);
        progressBar = view.findViewById(R.id.RegisterProgressBar);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_register, container, false);
        initComponents(view);
        pyAuthBackendRESTClient = new PyAuthBackendRESTClient(getContext());
        return view;
    }
}