/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.Key;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.Algorithm;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwx.Headers;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public interface KeyManagementAlgorithm
extends Algorithm {
    public ContentEncryptionKeys manageForEncrypt(Key var1, ContentEncryptionKeyDescriptor var2, Headers var3, byte[] var4, ProviderContext var5) throws JoseException;

    public Key manageForDecrypt(Key var1, byte[] var2, ContentEncryptionKeyDescriptor var3, Headers var4, ProviderContext var5) throws JoseException;

    public void validateEncryptionKey(Key var1, ContentEncryptionAlgorithm var2) throws InvalidKeyException;

    public void validateDecryptionKey(Key var1, ContentEncryptionAlgorithm var2) throws InvalidKeyException;
}

