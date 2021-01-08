package com.adriano.biometricauthfrontend.activities;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;

import com.adriano.biometricauthfrontend.R;



public class LoginRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
//        loginFragment = new LoginFragment();
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.FragmentContainer, loginFragment);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
    }
}