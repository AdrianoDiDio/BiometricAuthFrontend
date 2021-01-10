package com.adriano.biometricauthfrontend.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.adriano.biometricauthfrontend.R;
import com.adriano.biometricauthfrontend.activities.LoginRegisterActivity;
import com.adriano.biometricauthfrontend.rest.callbacks.LogoutResponseCallback;
import com.adriano.biometricauthfrontend.rest.responses.LogoutResponse;
import com.adriano.biometricauthfrontend.utils.Utils;
import com.adriano.biometricauthfrontend.rest.callbacks.UserInfoResponseCallback;
import com.adriano.biometricauthfrontend.rest.clients.PyAuthBackendRESTClient;
import com.adriano.biometricauthfrontend.rest.responses.UserInfoResponse;
import com.adriano.biometricauthfrontend.users.UserInfo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

public class UserInfoFragment extends Fragment implements UserInfoResponseCallback, LogoutResponseCallback
        , View.OnClickListener {
    private MaterialTextView userInfoIdTextView;
    private MaterialTextView userInfoUsernameTextView;
    private MaterialTextView userInfoEmailTextView;
    private MaterialButton userInfoRefreshButton;
    private MaterialButton userInfoLogoutButton;
    private ProgressBar userInfoProgressBar;
    private PyAuthBackendRESTClient pyAuthBackendRESTClient;
    public static final String FRAGMENT_USER_INFO_BUNDLE_KEY = "UserInfoBundle";


    private void showLogoutDialog(String message) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder;
        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
        materialAlertDialogBuilder.setTitle(R.string.session_end);
        materialAlertDialogBuilder.setMessage(message);
        materialAlertDialogBuilder.setCancelable(false);
        materialAlertDialogBuilder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
            Intent intent = new Intent(getActivity(), LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        materialAlertDialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        materialAlertDialogBuilder.show();
    }
    @Override
    public void onLogoutResponseCallback(LogoutResponse logoutResponse) {
        Utils.setProgressBarVisibility(userInfoProgressBar,getActivity(),false);
        switch ( logoutResponse.getRequestResponseCode() ) {
            case 205:
                showLogoutDialog(getString(R.string.session_logout_message));
                break;
            case 401:
                showLogoutDialog(getString(R.string.session_expired_message));
                break;
            default:
                Utils.displayWarningDialog(getString(R.string.server_unreachable_error),getContext());
                break;
        }
    }

    @Override
    public void onUserInfoResponseCallback(UserInfoResponse userInfoResponse) {
        Utils.setProgressBarVisibility(userInfoProgressBar,getActivity(),false);
        if( userInfoResponse.getRequestResponseCode() != 200 ) {
            if( userInfoResponse.getRequestResponseCode() == 401 ) {
                showLogoutDialog(getString(R.string.session_expired_message));
            } else {
                Utils.displayWarningDialog(getString(R.string.server_unreachable_error),getContext());
            }
            clearUserInfoViews();
            return;
        }
        userInfoUsernameTextView.setText(userInfoResponse.getUsername());
        userInfoEmailTextView.setText(userInfoResponse.getEmail());
        userInfoIdTextView.setText(Integer.toString(userInfoResponse.getUserId()));

    }
    @Override
    public void onClick(View view) {
        if( view.getId() == userInfoRefreshButton.getId() ) {
            getUserInfo();
        }
        if( view.getId() == userInfoLogoutButton.getId() ) {
            //
            Utils.setProgressBarVisibility(userInfoProgressBar,getActivity(),true);
            pyAuthBackendRESTClient.logout(this);
        }
    }

    private void clearUserInfoViews() {
        userInfoUsernameTextView.setText("");
        userInfoEmailTextView.setText("");
        userInfoIdTextView.setText("");
    }
    private void getUserInfo() {
        Utils.setProgressBarVisibility(userInfoProgressBar,getActivity(),true);
        pyAuthBackendRESTClient.getUserInfo(this);
    }
    private void initComponents(View view) {
        userInfoIdTextView = view.findViewById(R.id.UserInfoId);
        userInfoUsernameTextView = view.findViewById(R.id.UserInfoUsername);
        userInfoEmailTextView = view.findViewById(R.id.UserInfoEmail);
        userInfoRefreshButton = view.findViewById(R.id.UserInfoRefreshButton);
        userInfoRefreshButton.setOnClickListener(this);
        userInfoLogoutButton = view.findViewById(R.id.UserInfoLogoutButton);
        userInfoLogoutButton.setOnClickListener(this);
        userInfoProgressBar = view.findViewById(R.id.UserInfoProgressBar);
    }

    private void init(View view) {
        initComponents(view);
        pyAuthBackendRESTClient = new PyAuthBackendRESTClient(getArguments().
                getParcelable(FRAGMENT_USER_INFO_BUNDLE_KEY),getContext());
        getUserInfo();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_user_info, container, false);
        init(view);
        return view;
    }

    public static UserInfoFragment newInstance(UserInfo userInfo) {
        UserInfoFragment userInfoFragment = new UserInfoFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(FRAGMENT_USER_INFO_BUNDLE_KEY, userInfo);
        userInfoFragment.setArguments(bundle);
        return userInfoFragment;
    }
}
