/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jws;

import java.security.PublicKey;
import javax.crypto.spec.SecretKeySpec;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwa.JceProviderTestSupport;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.ExceptionHelp;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class PublicKeyAsHmacKeyTest {
    @Test
    public void tryPubKeyAsHmacTrick() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("RS256");
        jws.setPayload("tardier toothache");
        jws.setKey(ExampleRsaKeyFromJws.PRIVATE_KEY);
        this.verify(ExampleRsaKeyFromJws.PUBLIC_KEY, jws.getCompactSerialization(), true);
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("HS256");
        jws.setPayload("http://watchout4snakes.com/wo4snakes/Random/RandomPhrase");
        jws.setKey(new HmacKey(ExampleRsaKeyFromJws.PUBLIC_KEY.getEncoded()));
        this.verify(ExampleRsaKeyFromJws.PUBLIC_KEY, jws.getCompactSerialization(), false);
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("HS256");
        jws.setPayload("salty slop");
        jws.setKey(new SecretKeySpec(ExampleRsaKeyFromJws.PUBLIC_KEY.getEncoded(), "algorithm"));
        this.verify(ExampleRsaKeyFromJws.PUBLIC_KEY, jws.getCompactSerialization(), false);
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("ES256");
        jws.setPayload("flammable overture");
        jws.setKey(ExampleEcKeysFromJws.PRIVATE_256);
        this.verify(ExampleEcKeysFromJws.PUBLIC_256, jws.getCompactSerialization(), true);
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("HS256");
        jws.setPayload("scrupulous undercut");
        jws.setKey(new HmacKey(ExampleEcKeysFromJws.PUBLIC_256.getEncoded()));
        this.verify(ExampleEcKeysFromJws.PUBLIC_256, jws.getCompactSerialization(), false);
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("HS256");
        jws.setPayload("menial predestination");
        jws.setKey(new SecretKeySpec(ExampleEcKeysFromJws.PUBLIC_256.getEncoded(), ""));
        this.verify(ExampleEcKeysFromJws.PUBLIC_256, jws.getCompactSerialization(), false);
    }

    @Test
    public void tryPubKeyAsHmacTrickWithRsaBC1() throws Exception {
        JceProviderTestSupport support = new JceProviderTestSupport();
        support.setUseBouncyCastleRegardlessOfAlgs(true);
        support.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                JsonWebSignature jws = new JsonWebSignature();
                jws.setAlgorithmHeaderValue("HS256");
                jws.setPayload("salty slop");
                jws.setKey(new SecretKeySpec(ExampleRsaKeyFromJws.PUBLIC_KEY.getEncoded(), "algorithm"));
                PublicKeyAsHmacKeyTest.this.verify(ExampleRsaKeyFromJws.PUBLIC_KEY, jws.getCompactSerialization(), false);
            }
        });
    }

    @Test
    public void tryPubKeyAsHmacTrickWithRsaBC2() throws Exception {
        JceProviderTestSupport support = new JceProviderTestSupport();
        support.setUseBouncyCastleRegardlessOfAlgs(true);
        support.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                JsonWebSignature jws = new JsonWebSignature();
                jws.setAlgorithmHeaderValue("HS256");
                jws.setPayload("http://watchout4snakes.com/wo4snakes/Random/RandomPhrase");
                jws.setKey(new HmacKey(ExampleRsaKeyFromJws.PUBLIC_KEY.getEncoded()));
                PublicKeyAsHmacKeyTest.this.verify(ExampleRsaKeyFromJws.PUBLIC_KEY, jws.getCompactSerialization(), false);
            }
        });
    }

    @Test
    public void tryPubKeyAsHmacTrickWithEcBC1() throws Exception {
        JceProviderTestSupport support = new JceProviderTestSupport();
        support.setUseBouncyCastleRegardlessOfAlgs(true);
        support.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                JsonWebSignature jws = new JsonWebSignature();
                jws.setAlgorithmHeaderValue("HS256");
                jws.setPayload("scrupulous undercut");
                jws.setKey(new HmacKey(ExampleEcKeysFromJws.PUBLIC_256.getEncoded()));
                PublicKeyAsHmacKeyTest.this.verify(ExampleEcKeysFromJws.PUBLIC_256, jws.getCompactSerialization(), false);
            }
        });
    }

    @Test
    public void tryPubKeyAsHmacTrickWithEcBC2() throws Exception {
        JceProviderTestSupport support = new JceProviderTestSupport();
        support.setUseBouncyCastleRegardlessOfAlgs(true);
        support.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                JsonWebSignature jws = new JsonWebSignature();
                jws.setAlgorithmHeaderValue("HS256");
                jws.setPayload("menial predestination");
                jws.setKey(new SecretKeySpec(ExampleEcKeysFromJws.PUBLIC_256.getEncoded(), ""));
                PublicKeyAsHmacKeyTest.this.verify(ExampleEcKeysFromJws.PUBLIC_256, jws.getCompactSerialization(), false);
            }
        });
    }

    private void verify(PublicKey verificationKey, String cs, boolean expectedSignatureStatus) throws JoseException {
        JsonWebSignature consumerJws = new JsonWebSignature();
        consumerJws.setDoKeyValidation(false);
        consumerJws.setCompactSerialization(cs);
        consumerJws.setKey(verificationKey);
        try {
            Assert.assertThat((Object)consumerJws.verifySignature(), (Matcher)CoreMatchers.equalTo((Object)expectedSignatureStatus));
        }
        catch (JoseException e) {
            LoggerFactory.getLogger(this.getClass()).debug(ExceptionHelp.toStringWithCauses(e));
            Assert.assertFalse((String)("expected valid signature but got " + e), (boolean)expectedSignatureStatus);
        }
    }

    static /* synthetic */ void access$0(PublicKeyAsHmacKeyTest publicKeyAsHmacKeyTest, PublicKey publicKey, String string, boolean bl) throws JoseException {
        publicKeyAsHmacKeyTest.verify(publicKey, string, bl);
    }
}

