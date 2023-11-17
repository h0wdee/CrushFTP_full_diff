/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jws;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public abstract class BaseSignatureAlgorithm
extends AlgorithmInfo
implements JsonWebSignatureAlgorithm {
    private AlgorithmParameterSpec algorithmParameterSpec;

    public BaseSignatureAlgorithm(String id, String javaAlgo, String keyAlgo) {
        this.setAlgorithmIdentifier(id);
        this.setJavaAlgorithm(javaAlgo);
        this.setKeyPersuasion(KeyPersuasion.ASYMMETRIC);
        this.setKeyType(keyAlgo);
    }

    protected void setAlgorithmParameterSpec(AlgorithmParameterSpec algorithmParameterSpec) {
        this.algorithmParameterSpec = algorithmParameterSpec;
    }

    @Override
    public boolean verifySignature(byte[] signatureBytes, Key key, byte[] securedInputBytes, ProviderContext providerContext) throws JoseException {
        Signature signature = this.getSignature(providerContext);
        this.initForVerify(signature, key);
        try {
            signature.update(securedInputBytes);
            return signature.verify(signatureBytes);
        }
        catch (SignatureException e) {
            throw new JoseException("Problem verifying signature.", e);
        }
    }

    @Override
    public byte[] sign(Key key, byte[] securedInputBytes, ProviderContext providerContext) throws JoseException {
        Signature signature = this.getSignature(providerContext);
        this.initForSign(signature, key, providerContext);
        try {
            signature.update(securedInputBytes);
            return signature.sign();
        }
        catch (SignatureException e) {
            throw new JoseException("Problem creating signature.", e);
        }
    }

    private void initForSign(Signature signature, Key key, ProviderContext providerContext) throws InvalidKeyException {
        try {
            PrivateKey privateKey = (PrivateKey)key;
            SecureRandom secureRandom = providerContext.getSecureRandom();
            if (secureRandom == null) {
                signature.initSign(privateKey);
            } else {
                signature.initSign(privateKey, secureRandom);
            }
        }
        catch (java.security.InvalidKeyException e) {
            throw new InvalidKeyException(String.valueOf(this.getBadKeyMessage(key)) + "for " + this.getJavaAlgorithm(), e);
        }
    }

    private void initForVerify(Signature signature, Key key) throws InvalidKeyException {
        try {
            PublicKey publicKey = (PublicKey)key;
            signature.initVerify(publicKey);
        }
        catch (java.security.InvalidKeyException e) {
            throw new InvalidKeyException(String.valueOf(this.getBadKeyMessage(key)) + "for " + this.getJavaAlgorithm(), e);
        }
    }

    private String getBadKeyMessage(Key key) {
        String msg = key == null ? "key is null" : "algorithm=" + key.getAlgorithm();
        return "The given key (" + msg + ") is not valid ";
    }

    private Signature getSignature(ProviderContext providerContext) throws JoseException {
        String sigProvider = providerContext.getSuppliedKeyProviderContext().getSignatureProvider();
        String javaAlg = this.getJavaAlgorithm();
        try {
            Signature signature;
            Signature signature2 = signature = sigProvider == null ? Signature.getInstance(javaAlg) : Signature.getInstance(javaAlg, sigProvider);
            if (this.algorithmParameterSpec != null) {
                signature.setParameter(this.algorithmParameterSpec);
            }
            return signature;
        }
        catch (NoSuchAlgorithmException e) {
            throw new JoseException("Unable to get an implementation of algorithm name: " + javaAlg, e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new JoseException("Invalid algorithm parameter (" + this.algorithmParameterSpec + ") for: " + javaAlg, e);
        }
        catch (NoSuchProviderException e) {
            throw new JoseException("Unable to get an implementation of " + javaAlg + " for provider " + sigProvider, e);
        }
    }

    public abstract void validatePrivateKey(PrivateKey var1) throws InvalidKeyException;

    @Override
    public void validateSigningKey(Key key) throws InvalidKeyException {
        this.checkForNullKey(key);
        try {
            this.validatePrivateKey((PrivateKey)key);
        }
        catch (ClassCastException e) {
            throw new InvalidKeyException(String.valueOf(this.getBadKeyMessage(key)) + "(not a private key or is the wrong type of key) for " + this.getJavaAlgorithm() + " / " + this.getAlgorithmIdentifier() + " " + e);
        }
    }

    public abstract void validatePublicKey(PublicKey var1) throws InvalidKeyException;

    @Override
    public void validateVerificationKey(Key key) throws InvalidKeyException {
        this.checkForNullKey(key);
        try {
            this.validatePublicKey((PublicKey)key);
        }
        catch (ClassCastException e) {
            throw new InvalidKeyException(String.valueOf(this.getBadKeyMessage(key)) + "(not a public key or is the wrong type of key) for " + this.getJavaAlgorithm() + "/" + this.getAlgorithmIdentifier() + " " + e);
        }
    }

    private void checkForNullKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key cannot be null");
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            Signature signature = this.getSignature(new ProviderContext());
            return signature != null;
        }
        catch (Exception e) {
            return false;
        }
    }
}

