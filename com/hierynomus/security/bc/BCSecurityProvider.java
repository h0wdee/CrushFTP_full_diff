/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.security.bc;

import com.hierynomus.security.Cipher;
import com.hierynomus.security.Mac;
import com.hierynomus.security.MessageDigest;
import com.hierynomus.security.SecurityProvider;
import com.hierynomus.security.bc.BCCipherFactory;
import com.hierynomus.security.bc.BCMac;
import com.hierynomus.security.bc.BCMessageDigest;

public class BCSecurityProvider
implements SecurityProvider {
    @Override
    public MessageDigest getDigest(String name) {
        return new BCMessageDigest(name);
    }

    @Override
    public Mac getMac(String name) {
        return new BCMac(name);
    }

    @Override
    public Cipher getCipher(String name) {
        return BCCipherFactory.create(name);
    }
}

