package com.adriano.biometricauthfrontend.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import com.adriano.biometricauthfrontend.R;
import com.adriano.biometricauthfrontend.fragments.SettingsFragment;
import com.adriano.biometricauthfrontend.fragments.UserInfoFragment;
import com.adriano.biometricauthfrontend.rest.clients.PyAuthBackendRESTClient;
import com.adriano.biometricauthfrontend.users.UserInfo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigation;
    private UserInfoFragment userInfoFragment;
    private SettingsFragment settingsFragment;
    private Fragment activeFragment;
    private UserInfo userInfo;
    private PyAuthBackendRESTClient pyAuthBackendRESTClient;

    public static final String USER_INFO_INTENT_KEY = "UserInfo";
    private static final String ACTIVE_FRAGMENT_BUNDLE_KEY = "OldActiveFragment";


    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder;
        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder.setTitle(R.string.exit_confirm_title);
        materialAlertDialogBuilder.setMessage(R.string.exit_confirm_message);
        materialAlertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pyAuthBackendRESTClient.logout(null);
                finishAffinity();
            }
        });
        materialAlertDialogBuilder.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel());
        materialAlertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        materialAlertDialogBuilder.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //Avoid changing if it's the same.
        if (bottomNavigation.getSelectedItemId() == menuItem.getItemId()) {
            return false;
        }
        switch (menuItem.getItemId()) {
            case R.id.action_show_user_info:
                switchFragment(userInfoFragment);
                break;
            case R.id.action_settings:
                switchFragment(settingsFragment);
                break;
        }
        return true;
    }

    private void switchFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(newFragment).commit();
        activeFragment = newFragment;
    }

    private String getFragmentTagFromID(int Id) {
        String Tag;
        Tag = Integer.toString(Id);
        return Tag;
    }

    private boolean validateIntentExtras() {
        if( !getIntent().hasExtra(USER_INFO_INTENT_KEY) ) {
            Timber.d("UserInfo was not passed to this activity.");
            return false;
        }
        return true;
    }

    private void initComponents() {
        bottomNavigation = findViewById(R.id.BottomNavigationBar);
        bottomNavigation.setSelectedItemId(R.id.action_show_user_info);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
    }
    private void initFragmentManager() {

        getSupportFragmentManager().beginTransaction().add(R.id.MainFragmentContainer, settingsFragment,
                getFragmentTagFromID(R.id.SettingsFragment)).
                hide(settingsFragment).commit();
        getSupportFragmentManager().beginTransaction().
                add(R.id.MainFragmentContainer, userInfoFragment,
                        getFragmentTagFromID(R.id.UserInfoFragment)).commit();
    }
    private void initFragments(Bundle savedInstanceState) {
        if( savedInstanceState != null ) {
            userInfoFragment = (UserInfoFragment) getSupportFragmentManager().findFragmentByTag(
                    getFragmentTagFromID(R.id.UserInfoFragment));
            settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(
                    getFragmentTagFromID(R.id.SettingsFragment));
            activeFragment = getSupportFragmentManager().
                    getFragment(savedInstanceState,ACTIVE_FRAGMENT_BUNDLE_KEY);
        } else {
            userInfoFragment = UserInfoFragment.newInstance(userInfo);
            settingsFragment = SettingsFragment.newInstance(userInfo);
            activeFragment = userInfoFragment;
            initFragmentManager();
        }
    }
    private void init(Bundle savedInstanceState) {
        initFragments(savedInstanceState);
        initComponents();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState,ACTIVE_FRAGMENT_BUNDLE_KEY,activeFragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( !validateIntentExtras() ) {
            finish();
        }
        userInfo = getIntent().getExtras().getParcelable(USER_INFO_INTENT_KEY);
        setContentView(R.layout.activity_main);
        init(savedInstanceState);
        pyAuthBackendRESTClient = new PyAuthBackendRESTClient(userInfo,this);
    }
}