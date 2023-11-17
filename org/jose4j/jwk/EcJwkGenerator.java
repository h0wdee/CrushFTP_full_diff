/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECParameterSpec;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.keys.EcKeyUtil;
import org.jose4j.lang.JoseException;

public class EcJwkGenerator {
    public static EllipticCurveJsonWebKey generateJwk(ECParameterSpec spec) throws JoseException {
        return EcJwkGenerator.generateJwk(spec, null, null);
    }

    public static EllipticCurveJsonWebKey generateJwk(ECParameterSpec spec, String provider, SecureRandom secureRandom) throws JoseException {
        EcKeyUtil keyUtil = new EcKeyUtil(provider, secureRandom);
        KeyPair keyPair = keyUtil.generateKeyPair(spec);
        PublicKey publicKey = keyPair.getPublic();
        EllipticCurveJsonWebKey ecJwk = (EllipticCurveJsonWebKey)PublicJsonWebKey.Factory.newPublicJwk(publicKey);
        ecJwk.setPrivateKey(keyPair.getPrivate());
        return ecJwk;
    }
}

