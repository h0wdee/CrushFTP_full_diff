/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.keys;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import org.jose4j.keys.KeyPairUtil;
import org.jose4j.lang.JoseException;

public class EcKeyUtil
extends KeyPairUtil {
    public static final String EC = "EC";

    public EcKeyUtil() {
        this(null, null);
    }

    public EcKeyUtil(String provider, SecureRandom secureRandom) {
        super(provider, secureRandom);
    }

    @Override
    String getAlgorithm() {
        return EC;
    }

    public ECPublicKey publicKey(BigInteger x, BigInteger y, ECParameterSpec spec) throws JoseException {
        ECPoint w = new ECPoint(x, y);
        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(w, spec);
        try {
            PublicKey publicKey = this.getKeyFactory().generatePublic(ecPublicKeySpec);
            return (ECPublicKey)publicKey;
        }
        catch (InvalidKeySpecException e) {
            throw new JoseException("Invalid key spec: " + e, e);
        }
    }

    public ECPrivateKey privateKey(BigInteger d, ECParameterSpec spec) throws JoseException {
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(d, spec);
        try {
            PrivateKey privateKey = this.getKeyFactory().generatePrivate(ecPrivateKeySpec);
            return (ECPrivateKey)privateKey;
        }
        catch (InvalidKeySpecException e) {
            throw new JoseException("Invalid key spec: " + e, e);
        }
    }

    public KeyPair generateKeyPair(ECParameterSpec spec) throws JoseException {
        KeyPairGenerator keyGenerator = this.getKeyPairGenerator();
        try {
            if (this.secureRandom == null) {
                keyGenerator.initialize(spec);
            } else {
                keyGenerator.initialize(spec, this.secureRandom);
            }
            return keyGenerator.generateKeyPair();
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new JoseException("Unable to create EC key pair with spec " + spec, e);
        }
    }
}

