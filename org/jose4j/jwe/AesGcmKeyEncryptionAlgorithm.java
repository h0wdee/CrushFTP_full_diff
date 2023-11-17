/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.spec.SecretKeySpec;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwe.SimpleAeadCipher;
import org.jose4j.jwx.Headers;
import org.jose4j.jwx.KeyValidationSupport;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public class AesGcmKeyEncryptionAlgorithm
extends AlgorithmInfo
implements KeyManagementAlgorithm {
    private static final int TAG_BYTE_LENGTH = 16;
    private static final int IV_BYTE_LENGTH = 12;
    private SimpleAeadCipher simpleAeadCipher;
    private int keyByteLength;

    public AesGcmKeyEncryptionAlgorithm(String alg, int keyByteLength) {
        this.setAlgorithmIdentifier(alg);
        this.setJavaAlgorithm("AES/GCM/NoPadding");
        this.setKeyPersuasion(KeyPersuasion.SYMMETRIC);
        this.setKeyType("oct");
        this.simpleAeadCipher = new SimpleAeadCipher(this.getJavaAlgorithm(), 16);
        this.keyByteLength = keyByteLength;
    }

    @Override
    public ContentEncryptionKeys manageForEncrypt(Key managementKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, byte[] cekOverride, ProviderContext providerContext) throws JoseException {
        byte[] iv;
        SecureRandom secureRandom = providerContext.getSecureRandom();
        byte[] cek = cekOverride == null ? ByteUtil.randomBytes(cekDesc.getContentEncryptionKeyByteLength(), secureRandom) : cekOverride;
        Base64Url base64Url = new Base64Url();
        String encodedIv = headers.getStringHeaderValue("iv");
        if (encodedIv == null) {
            iv = ByteUtil.randomBytes(12, secureRandom);
            encodedIv = base64Url.base64UrlEncode(iv);
            headers.setStringHeaderValue("iv", encodedIv);
        } else {
            iv = base64Url.base64UrlDecode(encodedIv);
        }
        String cipherProvider = providerContext.getSuppliedKeyProviderContext().getCipherProvider();
        SimpleAeadCipher.CipherOutput encrypted = this.simpleAeadCipher.encrypt(managementKey, iv, cek, null, cipherProvider);
        byte[] encryptedKey = encrypted.getCiphertext();
        byte[] tag = encrypted.getTag();
        String encodedTag = base64Url.base64UrlEncode(tag);
        headers.setStringHeaderValue("tag", encodedTag);
        return new ContentEncryptionKeys(cek, encryptedKey);
    }

    @Override
    public Key manageForDecrypt(Key managementKey, byte[] encryptedKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, ProviderContext providerContext) throws JoseException {
        Base64Url base64Url = new Base64Url();
        String encodedIv = headers.getStringHeaderValue("iv");
        byte[] iv = base64Url.base64UrlDecode(encodedIv);
        String encodedTag = headers.getStringHeaderValue("tag");
        byte[] tag = base64Url.base64UrlDecode(encodedTag);
        String cipherProvider = providerContext.getSuppliedKeyProviderContext().getCipherProvider();
        byte[] cek = this.simpleAeadCipher.decrypt(managementKey, iv, encryptedKey, tag, null, cipherProvider);
        return new SecretKeySpec(cek, cekDesc.getContentEncryptionKeyAlgorithm());
    }

    @Override
    public void validateEncryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        this.validateKey(managementKey);
    }

    @Override
    public void validateDecryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        this.validateKey(managementKey);
    }

    void validateKey(Key managementKey) throws InvalidKeyException {
        KeyValidationSupport.validateAesWrappingKey(managementKey, this.getAlgorithmIdentifier(), this.keyByteLength);
    }

    @Override
    public boolean isAvailable() {
        return this.simpleAeadCipher.isAvailable(this.keyByteLength, 12, this.getAlgorithmIdentifier());
    }

    public static class Aes128Gcm
    extends AesGcmKeyEncryptionAlgorithm {
        public Aes128Gcm() {
            super("A128GCMKW", ByteUtil.byteLength(128));
        }
    }

    public static class Aes192Gcm
    extends AesGcmKeyEncryptionAlgorithm {
        public Aes192Gcm() {
            super("A192GCMKW", ByteUtil.byteLength(192));
        }
    }

    public static class Aes256Gcm
    extends AesGcmKeyEncryptionAlgorithm {
        public Aes256Gcm() {
            super("A256GCMKW", ByteUtil.byteLength(256));
        }
    }
}

