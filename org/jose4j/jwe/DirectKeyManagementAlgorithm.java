/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.Key;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwx.Headers;
import org.jose4j.jwx.KeyValidationSupport;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public class DirectKeyManagementAlgorithm
extends AlgorithmInfo
implements KeyManagementAlgorithm {
    public DirectKeyManagementAlgorithm() {
        this.setAlgorithmIdentifier("dir");
        this.setKeyPersuasion(KeyPersuasion.SYMMETRIC);
        this.setKeyType("oct");
    }

    @Override
    public ContentEncryptionKeys manageForEncrypt(Key managementKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, byte[] cekOverride, ProviderContext providerContext) throws JoseException {
        KeyValidationSupport.cekNotAllowed(cekOverride, this.getAlgorithmIdentifier());
        byte[] cekBytes = managementKey.getEncoded();
        return new ContentEncryptionKeys(cekBytes, ByteUtil.EMPTY_BYTES);
    }

    @Override
    public Key manageForDecrypt(Key managementKey, byte[] encryptedKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, ProviderContext providerContext) throws JoseException {
        if (encryptedKey.length != 0) {
            throw new InvalidKeyException("An empty octet sequence is to be used as the JWE Encrypted Key value when utilizing direct encryption but this JWE has " + encryptedKey.length + " octets in the encrypted key part.");
        }
        return managementKey;
    }

    @Override
    public void validateEncryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        this.validateKey(managementKey, contentEncryptionAlg);
    }

    private void validateKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        KeyValidationSupport.notNull(managementKey);
        if (managementKey.getEncoded() != null) {
            int managementKeyByteLength = managementKey.getEncoded().length;
            int expectedByteLength = contentEncryptionAlg.getContentEncryptionKeyDescriptor().getContentEncryptionKeyByteLength();
            if (expectedByteLength != managementKeyByteLength) {
                throw new InvalidKeyException("Invalid key for " + this.getAlgorithmIdentifier() + " with " + contentEncryptionAlg.getAlgorithmIdentifier() + ", expected a " + ByteUtil.bitLength(expectedByteLength) + " bit key but a " + ByteUtil.bitLength(managementKeyByteLength) + " bit key was provided.");
            }
        }
    }

    @Override
    public void validateDecryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        this.validateKey(managementKey, contentEncryptionAlg);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

