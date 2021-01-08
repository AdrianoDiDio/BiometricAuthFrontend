package com.adriano.biometricauthfrontend.biometrics;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;

import androidx.annotation.Nullable;

import com.adriano.biometricauthfrontend.R;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import timber.log.Timber;

public class RSAKeyStoreManager extends KeyStoreManager{
    private String keyStoreAlias;
    private boolean initialized;

    public boolean hasKeyPairExpired() {
        if( !initialized ) {
            return false;
        }
        try {
            Calendar now = Calendar.getInstance();
            KeyFactory factory = KeyFactory.getInstance(privateKey.getAlgorithm(), "AndroidKeyStore");
            KeyInfo keyInfo = factory.getKeySpec(privateKey, KeyInfo.class);
            return now.getTime().after(keyInfo.getKeyValidityForConsumptionEnd());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return false;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Nullable
    public Signature getSignature() {
        Signature signature = null;
        try {
            signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(privateKey);
            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public Cipher getEncryptCipher() {
        try {
            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return inCipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public Cipher getDecryptCipher() {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec("SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA1,
                    PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(), oaepParameterSpec);
            return cipher;
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException |
                InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void generateKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,
                    "AndroidKeyStore");
            Calendar expirationDate = Calendar.getInstance();
            expirationDate.add(Calendar.MONTH,6);
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(
                    keyStoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT |
                            KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setDigests(KeyProperties.DIGEST_SHA256,KeyProperties.DIGEST_SHA384,
                            KeyProperties.DIGEST_SHA512)
                    .setUserAuthenticationRequired(true)
                    .setKeyValidityEnd(expirationDate.getTime())
                    .build());
            keyPairGenerator.generateKeyPair();
        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public void forceKeyPairRefresh() {
        try {
            generateKey();
            privateKey = (PrivateKey) keyStore.getKey(keyStoreAlias, null);
            publicKey = keyStore.getCertificate(keyStoreAlias).getPublicKey();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            //DEBUG!
//            if( keyStore.containsAlias(keyStoreAlias) ) {
//                keyStore.deleteEntry(keyStoreAlias);
//            }
            if (!keyStore.containsAlias(keyStoreAlias)) {
                generateKey();
            }
            privateKey = (PrivateKey) keyStore.getKey(keyStoreAlias, null);
            publicKey = keyStore.getCertificate(keyStoreAlias).getPublicKey();
            initialized = true;
            Timber.d("Initialized:%s",initialized ? "true" : "false");
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException |
                UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }
    public RSAKeyStoreManager(Context context) {
        keyStoreAlias = context.getString(R.string.key_store_entry_key);
        initialized = false;
        init();
    }
}
