/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jwe.AesKeyWrapManagementAlgorithm;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwe.kdf.PasswordBasedKeyDerivationFunction2;
import org.jose4j.jwx.Headers;
import org.jose4j.jwx.KeyValidationSupport;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;

public class Pbes2HmacShaWithAesKeyWrapAlgorithm
extends AlgorithmInfo
implements KeyManagementAlgorithm {
    private static final byte[] ZERO_BYTE = new byte[1];
    private AesKeyWrapManagementAlgorithm keyWrap;
    private ContentEncryptionKeyDescriptor keyWrapKeyDescriptor;
    private PasswordBasedKeyDerivationFunction2 pbkdf2;
    private long defaultIterationCount = 8192L;
    private int defaultSaltByteLength = 12;

    public Pbes2HmacShaWithAesKeyWrapAlgorithm(String alg, String hmacAlg, AesKeyWrapManagementAlgorithm keyWrapAlg) {
        this.setAlgorithmIdentifier(alg);
        this.setJavaAlgorithm("n/a");
        this.pbkdf2 = new PasswordBasedKeyDerivationFunction2(hmacAlg);
        this.setKeyPersuasion(KeyPersuasion.SYMMETRIC);
        this.setKeyType("PBKDF2");
        this.keyWrap = keyWrapAlg;
        this.keyWrapKeyDescriptor = new ContentEncryptionKeyDescriptor(this.keyWrap.getKeyByteLength(), "AES");
    }

    @Override
    public ContentEncryptionKeys manageForEncrypt(Key managementKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, byte[] cekOverride, ProviderContext providerContext) throws JoseException {
        Key derivedKey = this.deriveForEncrypt(managementKey, headers, providerContext);
        return this.keyWrap.manageForEncrypt(derivedKey, cekDesc, headers, cekOverride, providerContext);
    }

    protected Key deriveForEncrypt(Key managementKey, Headers headers, ProviderContext providerContext) throws JoseException {
        byte[] saltInput;
        Long iterationCount = headers.getLongHeaderValue("p2c");
        if (iterationCount == null) {
            iterationCount = this.defaultIterationCount;
            headers.setObjectHeaderValue("p2c", iterationCount);
        }
        String saltInputString = headers.getStringHeaderValue("p2s");
        Base64Url base64Url = new Base64Url();
        if (saltInputString == null) {
            saltInput = ByteUtil.randomBytes(this.defaultSaltByteLength, providerContext.getSecureRandom());
            saltInputString = base64Url.base64UrlEncode(saltInput);
            headers.setStringHeaderValue("p2s", saltInputString);
        } else {
            saltInput = base64Url.base64UrlDecode(saltInputString);
        }
        return this.deriveKey(managementKey, iterationCount, saltInput, providerContext);
    }

    @Override
    public Key manageForDecrypt(Key managementKey, byte[] encryptedKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, ProviderContext providerContext) throws JoseException {
        Long iterationCount = headers.getLongHeaderValue("p2c");
        String saltInputString = headers.getStringHeaderValue("p2s");
        Base64Url base64Url = new Base64Url();
        byte[] saltInput = base64Url.base64UrlDecode(saltInputString);
        Key derivedKey = this.deriveKey(managementKey, iterationCount, saltInput, providerContext);
        return this.keyWrap.manageForDecrypt(derivedKey, encryptedKey, cekDesc, headers, providerContext);
    }

    private Key deriveKey(Key managementKey, Long iterationCount, byte[] saltInput, ProviderContext providerContext) throws JoseException {
        byte[] salt = ByteUtil.concat(StringUtil.getBytesUtf8(this.getAlgorithmIdentifier()), ZERO_BYTE, saltInput);
        int dkLen = this.keyWrapKeyDescriptor.getContentEncryptionKeyByteLength();
        String macProvider = providerContext.getSuppliedKeyProviderContext().getMacProvider();
        byte[] derivedKeyBytes = this.pbkdf2.derive(managementKey.getEncoded(), salt, iterationCount.intValue(), dkLen, macProvider);
        return new SecretKeySpec(derivedKeyBytes, this.keyWrapKeyDescriptor.getContentEncryptionKeyAlgorithm());
    }

    @Override
    public void validateEncryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        this.validateKey(managementKey);
    }

    @Override
    public void validateDecryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        this.validateKey(managementKey);
    }

    public void validateKey(Key managementKey) throws InvalidKeyException {
        KeyValidationSupport.notNull(managementKey);
    }

    @Override
    public boolean isAvailable() {
        return this.keyWrap.isAvailable();
    }

    public long getDefaultIterationCount() {
        return this.defaultIterationCount;
    }

    public void setDefaultIterationCount(long defaultIterationCount) {
        this.defaultIterationCount = defaultIterationCount;
    }

    public int getDefaultSaltByteLength() {
        return this.defaultSaltByteLength;
    }

    public void setDefaultSaltByteLength(int defaultSaltByteLength) {
        this.defaultSaltByteLength = defaultSaltByteLength;
    }

    public static class HmacSha256Aes128
    extends Pbes2HmacShaWithAesKeyWrapAlgorithm {
        public HmacSha256Aes128() {
            super("PBES2-HS256+A128KW", "HmacSHA256", new AesKeyWrapManagementAlgorithm.Aes128().setUseGeneralProviderContext());
        }
    }

    public static class HmacSha384Aes192
    extends Pbes2HmacShaWithAesKeyWrapAlgorithm {
        public HmacSha384Aes192() {
            super("PBES2-HS384+A192KW", "HmacSHA384", new AesKeyWrapManagementAlgorithm.Aes192().setUseGeneralProviderContext());
        }
    }

    public static class HmacSha512Aes256
    extends Pbes2HmacShaWithAesKeyWrapAlgorithm {
        public HmacSha512Aes256() {
            super("PBES2-HS512+A256KW", "HmacSHA512", new AesKeyWrapManagementAlgorithm.Aes256().setUseGeneralProviderContext());
        }
    }
}

