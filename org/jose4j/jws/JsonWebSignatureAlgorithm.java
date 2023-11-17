/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jws;

import java.security.Key;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.Algorithm;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public interface JsonWebSignatureAlgorithm
extends Algorithm {
    public boolean verifySignature(byte[] var1, Key var2, byte[] var3, ProviderContext var4) throws JoseException;

    public byte[] sign(Key var1, byte[] var2, ProviderContext var3) throws JoseException;

    public void validateSigningKey(Key var1) throws InvalidKeyException;

    public void validateVerificationKey(Key var1) throws InvalidKeyException;
}

