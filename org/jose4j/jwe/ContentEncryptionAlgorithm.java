/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.Algorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionParts;
import org.jose4j.jwx.Headers;
import org.jose4j.lang.JoseException;

public interface ContentEncryptionAlgorithm
extends Algorithm {
    public ContentEncryptionKeyDescriptor getContentEncryptionKeyDescriptor();

    public ContentEncryptionParts encrypt(byte[] var1, byte[] var2, byte[] var3, Headers var4, byte[] var5, ProviderContext var6) throws JoseException;

    public byte[] decrypt(ContentEncryptionParts var1, byte[] var2, byte[] var3, Headers var4, ProviderContext var5) throws JoseException;
}

