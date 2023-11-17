/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwk;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import junit.framework.TestCase;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;

public class EcJwkGeneratorTest
extends TestCase {
    public void testGen() throws JoseException {
        ECParameterSpec[] eCParameterSpecArray = new ECParameterSpec[]{EllipticCurves.P256, EllipticCurves.P384, EllipticCurves.P521};
        int n = eCParameterSpecArray.length;
        int n2 = 0;
        while (n2 < n) {
            ECParameterSpec spec = eCParameterSpecArray[n2];
            EllipticCurveJsonWebKey ecJwk = EcJwkGenerator.generateJwk(spec);
            EcJwkGeneratorTest.assertNotNull((Object)ecJwk.getKey());
            EcJwkGeneratorTest.assertTrue((boolean)(ecJwk.getKey() instanceof ECPublicKey));
            EcJwkGeneratorTest.assertNotNull((Object)ecJwk.getPublicKey());
            EcJwkGeneratorTest.assertTrue((boolean)(ecJwk.getPublicKey() instanceof ECPublicKey));
            EcJwkGeneratorTest.assertNotNull((Object)ecJwk.getPrivateKey());
            EcJwkGeneratorTest.assertTrue((boolean)(ecJwk.getPrivateKey() instanceof ECPrivateKey));
            ++n2;
        }
    }
}

