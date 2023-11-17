/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionHelp;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionParts;
import org.jose4j.jwe.InitializationVectorHelp;
import org.jose4j.jwe.SimpleAeadCipher;
import org.jose4j.jwx.Headers;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;

public class AesGcmContentEncryptionAlgorithm
extends AlgorithmInfo
implements ContentEncryptionAlgorithm {
    private static final int IV_BYTE_LENGTH = 12;
    private static final int TAG_BYTE_LENGTH = 16;
    private ContentEncryptionKeyDescriptor contentEncryptionKeyDescriptor;
    private SimpleAeadCipher simpleAeadCipher;

    public AesGcmContentEncryptionAlgorithm(String alg, int keyBitLength) {
        this.setAlgorithmIdentifier(alg);
        this.setJavaAlgorithm("AES/GCM/NoPadding");
        this.setKeyPersuasion(KeyPersuasion.SYMMETRIC);
        this.setKeyType("AES");
        this.contentEncryptionKeyDescriptor = new ContentEncryptionKeyDescriptor(ByteUtil.byteLength(keyBitLength), "AES");
        this.simpleAeadCipher = new SimpleAeadCipher(this.getJavaAlgorithm(), 16);
    }

    @Override
    public ContentEncryptionKeyDescriptor getContentEncryptionKeyDescriptor() {
        return this.contentEncryptionKeyDescriptor;
    }

    @Override
    public ContentEncryptionParts encrypt(byte[] plaintext, byte[] aad, byte[] contentEncryptionKey, Headers headers, byte[] ivOverride, ProviderContext providerContext) throws JoseException {
        byte[] iv = InitializationVectorHelp.iv(12, ivOverride, providerContext.getSecureRandom());
        String cipherProvider = ContentEncryptionHelp.getCipherProvider(headers, providerContext);
        return this.encrypt(plaintext, aad, contentEncryptionKey, iv, cipherProvider);
    }

    public ContentEncryptionParts encrypt(byte[] plaintext, byte[] aad, byte[] contentEncryptionKey, byte[] iv, String provider) throws JoseException {
        AesKey cek = new AesKey(contentEncryptionKey);
        SimpleAeadCipher.CipherOutput encrypted = this.simpleAeadCipher.encrypt(cek, iv, plaintext, aad, provider);
        return new ContentEncryptionParts(iv, encrypted.getCiphertext(), encrypted.getTag());
    }

    @Override
    public byte[] decrypt(ContentEncryptionParts contentEncParts, byte[] aad, byte[] contentEncryptionKey, Headers headers, ProviderContext providerContext) throws JoseException {
        byte[] iv = contentEncParts.getIv();
        AesKey cek = new AesKey(contentEncryptionKey);
        byte[] ciphertext = contentEncParts.getCiphertext();
        byte[] tag = contentEncParts.getAuthenticationTag();
        String cipherProvider = ContentEncryptionHelp.getCipherProvider(headers, providerContext);
        return this.simpleAeadCipher.decrypt(cek, iv, ciphertext, tag, aad, cipherProvider);
    }

    @Override
    public boolean isAvailable() {
        int keyByteLength = this.getContentEncryptionKeyDescriptor().getContentEncryptionKeyByteLength();
        return this.simpleAeadCipher.isAvailable(keyByteLength, 12, this.getAlgorithmIdentifier());
    }

    public static class Aes128Gcm
    extends AesGcmContentEncryptionAlgorithm {
        public Aes128Gcm() {
            super("A128GCM", 128);
        }
    }

    public static class Aes192Gcm
    extends AesGcmContentEncryptionAlgorithm {
        public Aes192Gcm() {
            super("A192GCM", 192);
        }
    }

    public static class Aes256Gcm
    extends AesGcmContentEncryptionAlgorithm {
        public Aes256Gcm() {
            super("A256GCM", 256);
        }
    }
}

