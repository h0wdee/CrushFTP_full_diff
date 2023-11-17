/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.Key;
import org.jose4j.jwa.AlgorithmAvailability;
import org.jose4j.jwe.CipherStrengthSupport;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.WrappingKeyManagementAlgorithm;
import org.jose4j.jwx.KeyValidationSupport;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.InvalidKeyException;

public class AesKeyWrapManagementAlgorithm
extends WrappingKeyManagementAlgorithm {
    int keyByteLength;

    public AesKeyWrapManagementAlgorithm(String alg, int keyByteLength) {
        super("AESWrap", alg);
        this.setKeyType("oct");
        this.setKeyPersuasion(KeyPersuasion.SYMMETRIC);
        this.keyByteLength = keyByteLength;
    }

    int getKeyByteLength() {
        return this.keyByteLength;
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
        KeyValidationSupport.validateAesWrappingKey(managementKey, this.getAlgorithmIdentifier(), this.getKeyByteLength());
    }

    @Override
    public boolean isAvailable() {
        int aesByteKeyLength = this.getKeyByteLength();
        String agl = this.getJavaAlgorithm();
        return AlgorithmAvailability.isAvailable("Cipher", agl) && CipherStrengthSupport.isAvailable(agl, aesByteKeyLength);
    }

    AesKeyWrapManagementAlgorithm setUseGeneralProviderContext() {
        this.useSuppliedKeyProviderContext = false;
        return this;
    }

    public static class Aes128
    extends AesKeyWrapManagementAlgorithm {
        public Aes128() {
            super("A128KW", 16);
        }
    }

    public static class Aes192
    extends AesKeyWrapManagementAlgorithm {
        public Aes192() {
            super("A192KW", 24);
        }
    }

    public static class Aes256
    extends AesKeyWrapManagementAlgorithm {
        public Aes256() {
            super("A256KW", 32);
        }
    }
}

