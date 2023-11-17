/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.security.jce;

import com.hierynomus.security.Cipher;
import com.hierynomus.security.Mac;
import com.hierynomus.security.MessageDigest;
import com.hierynomus.security.SecurityException;
import com.hierynomus.security.SecurityProvider;
import com.hierynomus.security.jce.JceCipher;
import com.hierynomus.security.jce.JceMac;
import com.hierynomus.security.jce.JceMessageDigest;
import java.security.Provider;

public class JceSecurityProvider
implements SecurityProvider {
    private final Provider jceProvider;
    private final String providerName;

    public JceSecurityProvider() {
        this.jceProvider = null;
        this.providerName = null;
    }

    public JceSecurityProvider(String providerName) {
        this.providerName = providerName;
        this.jceProvider = null;
    }

    public JceSecurityProvider(Provider provider) {
        this.providerName = null;
        this.jceProvider = provider;
    }

    @Override
    public MessageDigest getDigest(String name) throws SecurityException {
        return new JceMessageDigest(name, this.jceProvider, this.providerName);
    }

    @Override
    public Mac getMac(String name) throws SecurityException {
        return new JceMac(name, this.jceProvider, this.providerName);
    }

    @Override
    public Cipher getCipher(String name) throws SecurityException {
        return new JceCipher(name, this.jceProvider, this.providerName);
    }
}

