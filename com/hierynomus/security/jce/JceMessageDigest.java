/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.security.jce;

import com.hierynomus.security.MessageDigest;
import com.hierynomus.security.SecurityException;
import com.hierynomus.security.jce.messagedigest.MD4;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

public class JceMessageDigest
implements MessageDigest {
    private java.security.MessageDigest md;

    JceMessageDigest(String algorithm, Provider jceProvider, String providerName) throws SecurityException {
        try {
            this.md = jceProvider != null ? java.security.MessageDigest.getInstance(algorithm, jceProvider) : (providerName != null ? java.security.MessageDigest.getInstance(algorithm, providerName) : java.security.MessageDigest.getInstance(algorithm));
        }
        catch (NoSuchAlgorithmException e) {
            if ("MD4".equals(algorithm)) {
                this.md = new MD4();
            }
            throw new SecurityException(e);
        }
        catch (NoSuchProviderException e) {
            throw new SecurityException(e);
        }
    }

    @Override
    public void update(byte[] bytes) {
        this.md.update(bytes);
    }

    @Override
    public byte[] digest() {
        return this.md.digest();
    }

    @Override
    public void reset() {
        this.md.reset();
    }
}

