package com.adriano.biometricauthfrontend.biometrics;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.Nullable;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public abstract class KeyStoreManager {
    protected KeyStore keyStore;
    protected PrivateKey privateKey;
    protected PublicKey publicKey;

    public abstract PublicKey getPublicKey();
    @Nullable
    public abstract Signature getSignature();

    @Nullable
    public abstract Cipher getEncryptCipher();

    @Nullable
    public abstract Cipher getDecryptCipher();
    protected abstract void generateKey();
}
