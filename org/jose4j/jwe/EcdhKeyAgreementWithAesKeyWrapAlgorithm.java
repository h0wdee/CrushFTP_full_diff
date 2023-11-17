/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jwe.AesKeyWrapManagementAlgorithm;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.EcdhKeyAgreementAlgorithm;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwx.Headers;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public class EcdhKeyAgreementWithAesKeyWrapAlgorithm
extends AlgorithmInfo
implements KeyManagementAlgorithm {
    private AesKeyWrapManagementAlgorithm keyWrap;
    private ContentEncryptionKeyDescriptor keyWrapKeyDescriptor;
    private EcdhKeyAgreementAlgorithm ecdh;

    public EcdhKeyAgreementWithAesKeyWrapAlgorithm(String alg, AesKeyWrapManagementAlgorithm keyWrapAlgorithm) {
        this.setAlgorithmIdentifier(alg);
        this.setJavaAlgorithm("N/A");
        this.setKeyType("EC");
        this.setKeyPersuasion(KeyPersuasion.ASYMMETRIC);
        this.keyWrap = keyWrapAlgorithm;
        this.ecdh = new EcdhKeyAgreementAlgorithm("alg");
        this.keyWrapKeyDescriptor = new ContentEncryptionKeyDescriptor(keyWrapAlgorithm.getKeyByteLength(), "AES");
    }

    @Override
    public ContentEncryptionKeys manageForEncrypt(Key managementKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, byte[] cekOverride, ProviderContext providerContext) throws JoseException {
        ContentEncryptionKeys agreedKeys = this.ecdh.manageForEncrypt(managementKey, this.keyWrapKeyDescriptor, headers, (byte[])null, providerContext);
        String contentEncryptionKeyAlgorithm = this.keyWrapKeyDescriptor.getContentEncryptionKeyAlgorithm();
        SecretKeySpec agreedKey = new SecretKeySpec(agreedKeys.getContentEncryptionKey(), contentEncryptionKeyAlgorithm);
        return this.keyWrap.manageForEncrypt(agreedKey, cekDesc, headers, cekOverride, providerContext);
    }

    @Override
    public Key manageForDecrypt(Key managementKey, byte[] encryptedKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, ProviderContext providerContext) throws JoseException {
        Key agreedKey = this.ecdh.manageForDecrypt(managementKey, ByteUtil.EMPTY_BYTES, this.keyWrapKeyDescriptor, headers, providerContext);
        return this.keyWrap.manageForDecrypt(agreedKey, encryptedKey, cekDesc, headers, providerContext);
    }

    @Override
    public void validateEncryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        this.ecdh.validateEncryptionKey(managementKey, contentEncryptionAlg);
    }

    @Override
    public void validateDecryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        this.ecdh.validateDecryptionKey(managementKey, contentEncryptionAlg);
    }

    @Override
    public boolean isAvailable() {
        return this.ecdh.isAvailable() && this.keyWrap.isAvailable();
    }

    public static class EcdhKeyAgreementWithAes128KeyWrapAlgorithm
    extends EcdhKeyAgreementWithAesKeyWrapAlgorithm
    implements KeyManagementAlgorithm {
        public EcdhKeyAgreementWithAes128KeyWrapAlgorithm() {
            super("ECDH-ES+A128KW", new AesKeyWrapManagementAlgorithm.Aes128().setUseGeneralProviderContext());
        }
    }

    public static class EcdhKeyAgreementWithAes192KeyWrapAlgorithm
    extends EcdhKeyAgreementWithAesKeyWrapAlgorithm
    implements KeyManagementAlgorithm {
        public EcdhKeyAgreementWithAes192KeyWrapAlgorithm() {
            super("ECDH-ES+A192KW", new AesKeyWrapManagementAlgorithm.Aes192().setUseGeneralProviderContext());
        }
    }

    public static class EcdhKeyAgreementWithAes256KeyWrapAlgorithm
    extends EcdhKeyAgreementWithAesKeyWrapAlgorithm
    implements KeyManagementAlgorithm {
        public EcdhKeyAgreementWithAes256KeyWrapAlgorithm() {
            super("ECDH-ES+A256KW", new AesKeyWrapManagementAlgorithm.Aes256().setUseGeneralProviderContext());
        }
    }
}

