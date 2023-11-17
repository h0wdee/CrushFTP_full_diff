/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwe;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import org.jose4j.jwa.AlgorithmFactory;
import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcdhKeyAgreementWithAesKeyWrapAlgorithmTest
extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(EcdhKeyAgreementWithAesKeyWrapAlgorithmTest.class);

    public void testRoundTrip() throws JoseException {
        AlgorithmFactoryFactory aff = AlgorithmFactoryFactory.getInstance();
        AlgorithmFactory<ContentEncryptionAlgorithm> encAlgFactory = aff.getJweContentEncryptionAlgorithmFactory();
        AlgorithmFactory<KeyManagementAlgorithm> algAlgFactory = aff.getJweKeyManagementAlgorithmFactory();
        Set<String> supportedAlgAlgorithms = algAlgFactory.getSupportedAlgorithms();
        Set<String> supportedEncAlgorithms = encAlgFactory.getSupportedAlgorithms();
        Object[] algArray = new String[]{"ECDH-ES+A128KW", "ECDH-ES+A192KW", "ECDH-ES+A256KW"};
        HashSet<String> algs = new HashSet<String>(Arrays.asList(algArray));
        boolean algsReduced = algs.retainAll(supportedAlgAlgorithms);
        Object[] encArray = new String[]{"A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512"};
        HashSet<String> encs = new HashSet<String>(Arrays.asList(encArray));
        boolean encsReduced = encs.retainAll(supportedEncAlgorithms);
        if (algsReduced || encsReduced) {
            log.warn("*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*");
            log.warn("It looks like the JCE's Unlimited Strength Jurisdiction Policy Files are not installed for the JRE.");
            log.warn("So some algorithms are not available and will not be tested.");
            log.warn("{} vs {}", (Object)algs, (Object)Arrays.toString(algArray));
            log.warn("{} vs {}", (Object)encs, (Object)Arrays.toString(encArray));
            log.warn("*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*");
        }
        for (String alg : algs) {
            for (String enc : encs) {
                this.jweRoundTrip(alg, enc);
            }
        }
    }

    private void jweRoundTrip(String alg, String enc) throws JoseException {
        JsonWebEncryption jwe = new JsonWebEncryption();
        String receiverJwkJson = "\n{\"kty\":\"EC\",\n \"crv\":\"P-256\",\n \"x\":\"weNJy2HscCSM6AEDTDg04biOvhFhyyWvOHQfeF_PxMQ\",\n \"y\":\"e8lnCO-AlStT-NJVX-crhB7QRYhiix03illJOVAOyck\",\n \"d\":\"VEmDZpDXXK8p8N0Cndsxs924q6nS1RXFASRl6BfUqdw\"\n}";
        PublicJsonWebKey receiverJwk = PublicJsonWebKey.Factory.newPublicJwk(receiverJwkJson);
        jwe.setAlgorithmHeaderValue(alg);
        jwe.setEncryptionMethodHeaderParameter(enc);
        String plaintext = "Gambling is illegal at Bushwood sir, and I never slice.";
        jwe.setPlaintext(plaintext);
        jwe.setKey(receiverJwk.getPublicKey());
        String compactSerialization = jwe.getCompactSerialization();
        JsonWebEncryption receiverJwe = new JsonWebEncryption();
        receiverJwe.setCompactSerialization(compactSerialization);
        receiverJwe.setKey(receiverJwk.getPrivateKey());
        EcdhKeyAgreementWithAesKeyWrapAlgorithmTest.assertEquals((String)plaintext, (String)receiverJwe.getPlaintextString());
    }
}

