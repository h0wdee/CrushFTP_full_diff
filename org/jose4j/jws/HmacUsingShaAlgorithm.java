/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jws;

import java.security.Key;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmAvailability;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;
import org.jose4j.mac.MacUtil;

public class HmacUsingShaAlgorithm
extends AlgorithmInfo
implements JsonWebSignatureAlgorithm {
    private int minimumKeyLength;

    public HmacUsingShaAlgorithm(String id, String javaAlgo, int minimumKeyLength) {
        this.setAlgorithmIdentifier(id);
        this.setJavaAlgorithm(javaAlgo);
        this.setKeyPersuasion(KeyPersuasion.SYMMETRIC);
        this.setKeyType("oct");
        this.minimumKeyLength = minimumKeyLength;
    }

    @Override
    public boolean verifySignature(byte[] signatureBytes, Key key, byte[] securedInputBytes, ProviderContext providerContext) throws JoseException {
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException(key.getClass() + " cannot be used for HMAC verification.");
        }
        Mac mac = this.getMacInstance(key, providerContext);
        byte[] calculatedSigature = mac.doFinal(securedInputBytes);
        return ByteUtil.secureEquals(signatureBytes, calculatedSigature);
    }

    @Override
    public byte[] sign(Key key, byte[] securedInputBytes, ProviderContext providerContext) throws JoseException {
        Mac mac = this.getMacInstance(key, providerContext);
        return mac.doFinal(securedInputBytes);
    }

    private Mac getMacInstance(Key key, ProviderContext providerContext) throws JoseException {
        String macProvider = providerContext.getSuppliedKeyProviderContext().getMacProvider();
        return MacUtil.getInitializedMac(this.getJavaAlgorithm(), key, macProvider);
    }

    void validateKey(Key key) throws InvalidKeyException {
        int length;
        if (key == null) {
            throw new InvalidKeyException("key is null");
        }
        if (key.getEncoded() != null && (length = ByteUtil.bitLength(key.getEncoded())) < this.minimumKeyLength) {
            throw new InvalidKeyException("A key of the same size as the hash output (i.e. " + this.minimumKeyLength + " bits for " + this.getAlgorithmIdentifier() + ") or larger MUST be used with the HMAC SHA algorithms but this key is only " + length + " bits");
        }
    }

    @Override
    public void validateSigningKey(Key key) throws InvalidKeyException {
        this.validateKey(key);
    }

    @Override
    public void validateVerificationKey(Key key) throws InvalidKeyException {
        this.validateKey(key);
    }

    @Override
    public boolean isAvailable() {
        return AlgorithmAvailability.isAvailable("Mac", this.getJavaAlgorithm());
    }

    public static class HmacSha256
    extends HmacUsingShaAlgorithm {
        public HmacSha256() {
            super("HS256", "HmacSHA256", 256);
        }
    }

    public static class HmacSha384
    extends HmacUsingShaAlgorithm {
        public HmacSha384() {
            super("HS384", "HmacSHA384", 384);
        }
    }

    public static class HmacSha512
    extends HmacUsingShaAlgorithm {
        public HmacSha512() {
            super("HS512", "HmacSHA512", 512);
        }
    }
}

