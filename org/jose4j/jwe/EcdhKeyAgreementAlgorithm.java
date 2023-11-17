/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmAvailability;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwe.kdf.KdfUtil;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwx.Headers;
import org.jose4j.jwx.KeyValidationSupport;
import org.jose4j.keys.EcKeyUtil;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;

public class EcdhKeyAgreementAlgorithm
extends AlgorithmInfo
implements KeyManagementAlgorithm {
    String algorithmIdHeaderName = "enc";

    public EcdhKeyAgreementAlgorithm() {
        this.setAlgorithmIdentifier("ECDH-ES");
        this.setJavaAlgorithm("ECDH");
        this.setKeyType("EC");
        this.setKeyPersuasion(KeyPersuasion.ASYMMETRIC);
    }

    public EcdhKeyAgreementAlgorithm(String algorithmIdHeaderName) {
        this();
        this.algorithmIdHeaderName = algorithmIdHeaderName;
    }

    @Override
    public ContentEncryptionKeys manageForEncrypt(Key managementKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, byte[] cekOverride, ProviderContext providerContext) throws JoseException {
        KeyValidationSupport.cekNotAllowed(cekOverride, this.getAlgorithmIdentifier());
        ECPublicKey receiversKey = (ECPublicKey)managementKey;
        String keyPairGeneratorProvider = providerContext.getGeneralProviderContext().getKeyPairGeneratorProvider();
        SecureRandom secureRandom = providerContext.getSecureRandom();
        EllipticCurveJsonWebKey ephemeralJwk = EcJwkGenerator.generateJwk(receiversKey.getParams(), keyPairGeneratorProvider, secureRandom);
        return this.manageForEncrypt(managementKey, cekDesc, headers, ephemeralJwk, providerContext);
    }

    ContentEncryptionKeys manageForEncrypt(Key managementKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, PublicJsonWebKey ephemeralJwk, ProviderContext providerContext) throws JoseException {
        headers.setJwkHeaderValue("epk", ephemeralJwk);
        byte[] z = this.generateEcdhSecret(ephemeralJwk.getPrivateKey(), (PublicKey)managementKey, providerContext);
        byte[] derivedKey = this.kdf(cekDesc, headers, z, providerContext);
        return new ContentEncryptionKeys(derivedKey, null);
    }

    @Override
    public Key manageForDecrypt(Key managementKey, byte[] encryptedKey, ContentEncryptionKeyDescriptor cekDesc, Headers headers, ProviderContext providerContext) throws JoseException {
        String keyFactoryProvider = providerContext.getGeneralProviderContext().getKeyFactoryProvider();
        PublicJsonWebKey ephemeralJwk = headers.getPublicJwkHeaderValue("epk", keyFactoryProvider);
        ephemeralJwk.getKey();
        byte[] z = this.generateEcdhSecret((PrivateKey)managementKey, (PublicKey)ephemeralJwk.getKey(), providerContext);
        byte[] derivedKey = this.kdf(cekDesc, headers, z, providerContext);
        String cekAlg = cekDesc.getContentEncryptionKeyAlgorithm();
        return new SecretKeySpec(derivedKey, cekAlg);
    }

    private byte[] kdf(ContentEncryptionKeyDescriptor cekDesc, Headers headers, byte[] z, ProviderContext providerContext) {
        String messageDigestProvider = providerContext.getGeneralProviderContext().getMessageDigestProvider();
        KdfUtil kdf = new KdfUtil(messageDigestProvider);
        int keydatalen = ByteUtil.bitLength(cekDesc.getContentEncryptionKeyByteLength());
        String algorithmID = headers.getStringHeaderValue(this.algorithmIdHeaderName);
        String partyUInfo = headers.getStringHeaderValue("apu");
        String partyVInfo = headers.getStringHeaderValue("apv");
        return kdf.kdf(z, keydatalen, algorithmID, partyUInfo, partyVInfo);
    }

    private KeyAgreement getKeyAgreement(String provider) throws JoseException {
        String javaAlgorithm = this.getJavaAlgorithm();
        try {
            return provider == null ? KeyAgreement.getInstance(javaAlgorithm) : KeyAgreement.getInstance(javaAlgorithm, provider);
        }
        catch (NoSuchAlgorithmException e) {
            throw new UncheckedJoseException("No " + javaAlgorithm + " KeyAgreement available.", e);
        }
        catch (NoSuchProviderException e) {
            throw new JoseException("Cannot get " + javaAlgorithm + " KeyAgreement with provider " + provider, e);
        }
    }

    private byte[] generateEcdhSecret(PrivateKey privateKey, PublicKey publicKey, ProviderContext providerContext) throws JoseException {
        String keyAgreementProvider = providerContext.getSuppliedKeyProviderContext().getKeyAgreementProvider();
        KeyAgreement keyAgreement = this.getKeyAgreement(keyAgreementProvider);
        try {
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);
        }
        catch (java.security.InvalidKeyException e) {
            throw new InvalidKeyException("Invalid Key for " + this.getJavaAlgorithm() + " key agreement.", e);
        }
        return keyAgreement.generateSecret();
    }

    @Override
    public void validateEncryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        KeyValidationSupport.castKey(managementKey, ECPublicKey.class);
    }

    @Override
    public void validateDecryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        KeyValidationSupport.castKey(managementKey, ECPrivateKey.class);
    }

    @Override
    public boolean isAvailable() {
        EcKeyUtil ecKeyUtil = new EcKeyUtil();
        return ecKeyUtil.isAvailable() && AlgorithmAvailability.isAvailable("KeyAgreement", this.getJavaAlgorithm());
    }
}

