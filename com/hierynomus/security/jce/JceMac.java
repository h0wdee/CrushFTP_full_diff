/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.security.jce;

import com.hierynomus.security.Mac;
import com.hierynomus.security.SecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import javax.crypto.spec.SecretKeySpec;

public class JceMac
implements Mac {
    private final String algorithm;
    private javax.crypto.Mac mac;

    public JceMac(String algorithm, Provider jceProvider, String providerName) throws SecurityException {
        this.algorithm = algorithm;
        try {
            this.mac = jceProvider != null ? javax.crypto.Mac.getInstance(algorithm, jceProvider) : (providerName != null ? javax.crypto.Mac.getInstance(algorithm, providerName) : javax.crypto.Mac.getInstance(algorithm));
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new SecurityException(e);
        }
    }

    @Override
    public void init(byte[] key) throws SecurityException {
        try {
            this.mac.init(new SecretKeySpec(key, this.algorithm));
        }
        catch (InvalidKeyException e) {
            throw new SecurityException(e);
        }
    }

    @Override
    public void update(byte b) {
        this.mac.update(b);
    }

    @Override
    public void update(byte[] array) {
        this.mac.update(array);
    }

    @Override
    public void update(byte[] array, int offset, int length) {
        this.mac.update(array, offset, length);
    }

    @Override
    public byte[] doFinal() {
        return this.mac.doFinal();
    }

    @Override
    public void reset() {
        this.mac.reset();
    }
}

