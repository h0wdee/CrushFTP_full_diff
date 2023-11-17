/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwa;

import junit.framework.TestCase;
import org.jose4j.jwa.AlgorithmFactory;
import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.lang.JoseException;

public class AlgorithmFactoryTest
extends TestCase {
    public void testAllJwsKeyPersuasionsNotNull() throws JoseException {
        AlgorithmFactoryFactory algoFactoryFactory = AlgorithmFactoryFactory.getInstance();
        AlgorithmFactory<JsonWebSignatureAlgorithm> jwsAlgorithmFactory = algoFactoryFactory.getJwsAlgorithmFactory();
        AlgorithmFactoryTest.assertFalse((boolean)jwsAlgorithmFactory.isAvailable("blahblahblah"));
        for (String algo : jwsAlgorithmFactory.getSupportedAlgorithms()) {
            JsonWebSignatureAlgorithm jsonWebSignatureAlgorithm = jwsAlgorithmFactory.getAlgorithm(algo);
            AlgorithmFactoryTest.assertNotNull((Object)((Object)jsonWebSignatureAlgorithm.getKeyPersuasion()));
            AlgorithmFactoryTest.assertTrue((boolean)jwsAlgorithmFactory.isAvailable(algo));
        }
    }
}

