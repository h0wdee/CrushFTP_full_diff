/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwk;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import junit.framework.TestCase;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;

public class RsaJwkGeneratorTest
extends TestCase {
    public void testGenerateJwk() throws Exception {
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        RsaJwkGeneratorTest.assertNotNull((Object)rsaJsonWebKey.getPrivateKey());
        RsaJwkGeneratorTest.assertTrue((boolean)(rsaJsonWebKey.getKey() instanceof RSAPublicKey));
        RsaJwkGeneratorTest.assertNotNull((Object)rsaJsonWebKey.getPublicKey());
        RsaJwkGeneratorTest.assertTrue((boolean)(rsaJsonWebKey.getPublicKey() instanceof RSAPublicKey));
        RsaJwkGeneratorTest.assertNotNull((Object)rsaJsonWebKey.getPrivateKey());
        RsaJwkGeneratorTest.assertTrue((boolean)(rsaJsonWebKey.getPrivateKey() instanceof RSAPrivateKey));
    }
}

