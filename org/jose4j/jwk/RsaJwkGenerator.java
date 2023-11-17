/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.security.KeyPair;
import java.security.SecureRandom;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.lang.JoseException;

public class RsaJwkGenerator {
    public static RsaJsonWebKey generateJwk(int bits) throws JoseException {
        return RsaJwkGenerator.generateJwk(bits, null, null);
    }

    public static RsaJsonWebKey generateJwk(int bits, String provider, SecureRandom secureRandom) throws JoseException {
        RsaKeyUtil keyUtil = new RsaKeyUtil(provider, secureRandom);
        KeyPair keyPair = keyUtil.generateKeyPair(bits);
        RsaJsonWebKey rsaJwk = (RsaJsonWebKey)PublicJsonWebKey.Factory.newPublicJwk(keyPair.getPublic());
        rsaJwk.setPrivateKey(keyPair.getPrivate());
        return rsaJwk;
    }
}

