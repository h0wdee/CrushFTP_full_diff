/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jwe.CipherStrengthSupport;
import org.jose4j.jwe.CipherUtil;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionHelp;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionParts;
import org.jose4j.jwe.InitializationVectorHelp;
import org.jose4j.jwx.Headers;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.HmacKey;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.IntegrityException;
import org.jose4j.lang.JoseException;
import org.jose4j.mac.MacUtil;

public class AesCbcHmacSha2ContentEncryptionAlgorithm
extends AlgorithmInfo
implements ContentEncryptionAlgorithm {
    public static final int IV_BYTE_LENGTH = 16;
    private final String hmacJavaAlgorithm;
    private final int tagTruncationLength;
    private final ContentEncryptionKeyDescriptor contentEncryptionKeyDescriptor;

    public AesCbcHmacSha2ContentEncryptionAlgorithm(String alg, int cekByteLen, String javaHmacAlg, int tagTruncationLength) {
        this.setAlgorithmIdentifier(alg);
        this.contentEncryptionKeyDescriptor = new ContentEncryptionKeyDescriptor(cekByteLen, "AES");
        this.hmacJavaAlgorithm = javaHmacAlg;
        this.tagTruncationLength = tagTruncationLength;
        this.setJavaAlgorithm("AES/CBC/PKCS5Padding");
        this.setKeyPersuasion(KeyPersuasion.SYMMETRIC);
        this.setKeyType("AES");
    }

    public String getHmacJavaAlgorithm() {
        return this.hmacJavaAlgorithm;
    }

    public int getTagTruncationLength() {
        return this.tagTruncationLength;
    }

    @Override
    public ContentEncryptionKeyDescriptor getContentEncryptionKeyDescriptor() {
        return this.contentEncryptionKeyDescriptor;
    }

    @Override
    public ContentEncryptionParts encrypt(byte[] plaintext, byte[] aad, byte[] contentEncryptionKey, Headers headers, byte[] ivOverride, ProviderContext providerContext) throws JoseException {
        byte[] iv = InitializationVectorHelp.iv(16, ivOverride, providerContext.getSecureRandom());
        return this.encrypt(plaintext, aad, contentEncryptionKey, iv, headers, providerContext);
    }

    ContentEncryptionParts encrypt(byte[] plaintext, byte[] aad, byte[] key, byte[] iv, Headers headers, ProviderContext providerContext) throws JoseException {
        byte[] cipherText;
        HmacKey hmacKey = new HmacKey(ByteUtil.leftHalf(key));
        AesKey encryptionKey = new AesKey(ByteUtil.rightHalf(key));
        String cipherProvider = ContentEncryptionHelp.getCipherProvider(headers, providerContext);
        Cipher cipher = CipherUtil.getCipher(this.getJavaAlgorithm(), cipherProvider);
        try {
            cipher.init(1, (Key)encryptionKey, new IvParameterSpec(iv));
        }
        catch (InvalidKeyException e) {
            throw new JoseException("Invalid key for " + this.getJavaAlgorithm(), e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new JoseException(e.toString(), e);
        }
        try {
            cipherText = cipher.doFinal(plaintext);
        }
        catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new JoseException(e.toString(), e);
        }
        String macProvider = ContentEncryptionHelp.getMacProvider(headers, providerContext);
        Mac mac = MacUtil.getInitializedMac(this.getHmacJavaAlgorithm(), hmacKey, macProvider);
        byte[] al = this.getAdditionalAuthenticatedDataLengthBytes(aad);
        byte[] authenticationTagInput = ByteUtil.concat(aad, iv, cipherText, al);
        byte[] authenticationTag = mac.doFinal(authenticationTagInput);
        authenticationTag = ByteUtil.subArray(authenticationTag, 0, this.getTagTruncationLength());
        return new ContentEncryptionParts(iv, cipherText, authenticationTag);
    }

    @Override
    public byte[] decrypt(ContentEncryptionParts contentEncryptionParts, byte[] aad, byte[] contentEncryptionKey, Headers headers, ProviderContext providerContext) throws JoseException {
        String cipherProvider = ContentEncryptionHelp.getCipherProvider(headers, providerContext);
        String macProvider = ContentEncryptionHelp.getMacProvider(headers, providerContext);
        byte[] iv = contentEncryptionParts.getIv();
        byte[] ciphertext = contentEncryptionParts.getCiphertext();
        byte[] authenticationTag = contentEncryptionParts.getAuthenticationTag();
        byte[] al = this.getAdditionalAuthenticatedDataLengthBytes(aad);
        byte[] authenticationTagInput = ByteUtil.concat(aad, iv, ciphertext, al);
        HmacKey hmacKey = new HmacKey(ByteUtil.leftHalf(contentEncryptionKey));
        Mac mac = MacUtil.getInitializedMac(this.getHmacJavaAlgorithm(), hmacKey, macProvider);
        byte[] calculatedAuthenticationTag = mac.doFinal(authenticationTagInput);
        boolean tagMatch = ByteUtil.secureEquals(authenticationTag, calculatedAuthenticationTag = ByteUtil.subArray(calculatedAuthenticationTag, 0, this.getTagTruncationLength()));
        if (!tagMatch) {
            Base64Url base64Url = new Base64Url();
            String encTag = base64Url.base64UrlEncode(authenticationTag);
            String calcEncTag = base64Url.base64UrlEncode(calculatedAuthenticationTag);
            throw new IntegrityException("Authentication tag check failed. Message=" + encTag + " calculated=" + calcEncTag);
        }
        AesKey encryptionKey = new AesKey(ByteUtil.rightHalf(contentEncryptionKey));
        Cipher cipher = CipherUtil.getCipher(this.getJavaAlgorithm(), cipherProvider);
        try {
            cipher.init(2, (Key)encryptionKey, new IvParameterSpec(iv));
        }
        catch (InvalidKeyException e) {
            throw new JoseException("Invalid key for " + this.getJavaAlgorithm(), e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new JoseException(e.toString(), e);
        }
        try {
            return cipher.doFinal(ciphertext);
        }
        catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new JoseException(e.toString(), e);
        }
    }

    private byte[] getAdditionalAuthenticatedDataLengthBytes(byte[] additionalAuthenticatedData) {
        long aadLength = ByteUtil.bitLength(additionalAuthenticatedData);
        return ByteUtil.getBytes(aadLength);
    }

    @Override
    public boolean isAvailable() {
        int contentEncryptionKeyByteLength = this.getContentEncryptionKeyDescriptor().getContentEncryptionKeyByteLength();
        int aesByteKeyLength = contentEncryptionKeyByteLength / 2;
        return CipherStrengthSupport.isAvailable(this.getJavaAlgorithm(), aesByteKeyLength);
    }

    public static class Aes128CbcHmacSha256
    extends AesCbcHmacSha2ContentEncryptionAlgorithm
    implements ContentEncryptionAlgorithm {
        public Aes128CbcHmacSha256() {
            super("A128CBC-HS256", 32, "HmacSHA256", 16);
        }
    }

    public static class Aes192CbcHmacSha384
    extends AesCbcHmacSha2ContentEncryptionAlgorithm
    implements ContentEncryptionAlgorithm {
        public Aes192CbcHmacSha384() {
            super("A192CBC-HS384", 48, "HmacSHA384", 24);
        }
    }

    public static class Aes256CbcHmacSha512
    extends AesCbcHmacSha2ContentEncryptionAlgorithm
    implements ContentEncryptionAlgorithm {
        public Aes256CbcHmacSha512() {
            super("A256CBC-HS512", 64, "HmacSHA512", 32);
        }
    }
}

