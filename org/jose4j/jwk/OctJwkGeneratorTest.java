/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwk;

import javax.crypto.SecretKey;
import junit.framework.TestCase;
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.lang.ByteUtil;

public class OctJwkGeneratorTest
extends TestCase {
    public void testGen() {
        int[] nArray = new int[]{128, 192, 256, 192, 384, 512};
        int n = nArray.length;
        int n2 = 0;
        while (n2 < n) {
            int size = nArray[n2];
            OctetSequenceJsonWebKey jsonWebKey = OctJwkGenerator.generateJwk(size);
            OctJwkGeneratorTest.assertNotNull((Object)jsonWebKey.getKey());
            OctJwkGeneratorTest.assertTrue((boolean)(jsonWebKey.getKey() instanceof SecretKey));
            OctJwkGeneratorTest.assertEquals((int)ByteUtil.byteLength(size), (int)jsonWebKey.getOctetSequence().length);
            ++n2;
        }
    }
}

