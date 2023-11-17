/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jwe.CipherUtil;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwx.Headers;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.ExceptionHelp;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public abstract class WrappingKeyManagementAlgorithm
extends AlgorithmInfo
implements KeyManagementAlgorithm {
    private AlgorithmParameterSpec algorithmParameterSpec;
    protected boolean useSuppliedKeyProviderContext = true;

    public WrappingKeyManagementAlgorithm(String javaAlg, String alg) {
        this.setJavaAlgorithm(javaAlg);
        this.setAlgorithmIdentifier(alg);
    }

    public void setAlgorithmParameterSpec(AlgorithmParameterSpec algorithmParameterSpec) {
        this.algorithmParameterSpec = algorithmParameterSpec;
    }

    @Override
    public ContentEncryptionKeys manageForEncrypt(Key managementKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, byte[] cekOverride, ProviderContext providerContext) throws JoseException {
        byte[] contentEncryptionKey = cekOverride == null ? ByteUtil.randomBytes(cekDesc.getContentEncryptionKeyByteLength()) : cekOverride;
        return this.manageForEnc(managementKey, cekDesc, contentEncryptionKey, providerContext);
    }

    protected ContentEncryptionKeys manageForEnc(Key managementKey, ContentEncryptionKeyDescriptor cekDesc, byte[] contentEncryptionKey, ProviderContext providerContext) throws JoseException {
        ProviderContext.Context ctx = this.useSuppliedKeyProviderContext ? providerContext.getSuppliedKeyProviderContext() : providerContext.getGeneralProviderContext();
        String provider = ctx.getCipherProvider();
        Cipher cipher = CipherUtil.getCipher(this.getJavaAlgorithm(), provider);
        try {
            this.initCipher(cipher, 3, managementKey);
            String contentEncryptionKeyAlgorithm = cekDesc.getContentEncryptionKeyAlgorithm();
            byte[] encryptedKey = cipher.wrap(new SecretKeySpec(contentEncryptionKey, contentEncryptionKeyAlgorithm));
            return new ContentEncryptionKeys(contentEncryptionKey, encryptedKey);
        }
        catch (java.security.InvalidKeyException e) {
            throw new InvalidKeyException("Unable to encrypt (" + cipher.getAlgorithm() + ") the Content Encryption Key: " + e, e);
        }
        catch (InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
            throw new JoseException("Unable to encrypt (" + cipher.getAlgorithm() + ") the Content Encryption Key: " + e, e);
        }
    }

    void initCipher(Cipher cipher, int mode, Key key) throws InvalidAlgorithmParameterException, java.security.InvalidKeyException {
        if (this.algorithmParameterSpec == null) {
            cipher.init(mode, key);
        } else {
            cipher.init(mode, key, this.algorithmParameterSpec);
        }
    }

    @Override
    public Key manageForDecrypt(Key managementKey, byte[] encryptedKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, ProviderContext providerContext) throws JoseException {
        String provider = providerContext.getSuppliedKeyProviderContext().getCipherProvider();
        Cipher cipher = CipherUtil.getCipher(this.getJavaAlgorithm(), provider);
        try {
            this.initCipher(cipher, 4, managementKey);
        }
        catch (java.security.InvalidKeyException e) {
            throw new InvalidKeyException("Unable to initialize cipher (" + cipher.getAlgorithm() + ") for key decryption", e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new JoseException("Unable to initialize cipher (" + cipher.getAlgorithm() + ") for key decryption", e);
        }
        String cekAlg = cekDesc.getContentEncryptionKeyAlgorithm();
        try {
            return cipher.unwrap(encryptedKey, cekAlg, 3);
        }
        catch (Exception e) {
            String string = ExceptionHelp.toStringWithCausesAndAbbreviatedStack(e, JsonWebEncryption.class);
            byte[] bytes = ByteUtil.randomBytes(cekDesc.getContentEncryptionKeyByteLength());
            return new SecretKeySpec(bytes, cekAlg);
        }
    }
}

