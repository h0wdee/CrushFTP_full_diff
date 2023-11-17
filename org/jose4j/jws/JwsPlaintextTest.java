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

import java.security.Key;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.InvalidAlgorithmException;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwsPlaintextTest {
    private static final Logger log = LoggerFactory.getLogger(JwsPlaintextTest.class);
    String JWS = "eyJhbGciOiJub25lIn0.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.";
    String PAYLOAD = "{\"iss\":\"joe\",\r\n \"exp\":1300819380,\r\n \"http://example.com/is_root\":true}";
    Key KEY = new HmacKey(new byte[]{1, 2, 3, 4, 5, -3, 28, 123, -53});

    @Test
    public void testExampleDecode() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE);
        jws.setCompactSerialization(this.JWS);
        Assert.assertTrue((boolean)jws.verifySignature());
        String payload = jws.getPayload();
        Assert.assertEquals((Object)this.PAYLOAD, (Object)payload);
    }

    @Test
    public void testExampleEncode() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE);
        jws.setAlgorithmHeaderValue("none");
        jws.setPayload(this.PAYLOAD);
        Assert.assertEquals((Object)this.JWS, (Object)jws.getCompactSerialization());
    }

    @Test(expected=InvalidKeyException.class)
    public void testSignWithKeyNoGood() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);
        jws.setAlgorithmHeaderValue("none");
        jws.setPayload(this.PAYLOAD);
        jws.setKey(this.KEY);
        jws.getCompactSerialization();
    }

    @Test(expected=InvalidKeyException.class)
    public void testExampleVerifyWithKeyNoGood() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);
        jws.setCompactSerialization(this.JWS);
        jws.setKey(this.KEY);
        jws.verifySignature();
    }

    @Test(expected=InvalidAlgorithmException.class)
    public void testExampleVerifyButNoneNotAllowed() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "none"));
        jws.setCompactSerialization(this.JWS);
        jws.verifySignature();
    }

    @Test
    public void testExampleVerifyWithOnlyNoneAllowed() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, "none"));
        jws.setCompactSerialization(this.JWS);
        Assert.assertThat((Object)jws.verifySignature(), (Matcher)CoreMatchers.is((Object)true));
    }

    @Test
    public void testADecode() throws JoseException {
        String cs = "eyJhbGciOiJub25lIn0.eyJhdXRoX3RpbWUiOjEzMzk2MTMyNDgsImV4cCI6MTMzOTYxMzU0OCwiaXNzIjoiaHR0cHM6XC9cL2V4YW1wbGUuY29tIiwiYXVkIjoiYSIsImp0aSI6ImpJQThxYTM1QXJvVjZpUDJxNHdSQWwiLCJ1c2VyX2lkIjoiam9obiIsImlhdCI6MTMzOTYxMzI0OCwiYWNyIjozfQ.";
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);
        jws.setCompactSerialization(cs);
        Assert.assertTrue((boolean)jws.verifySignature());
        String payload = jws.getPayload();
        log.debug(payload);
    }

    @Test
    public void testSysPropForDefaultHandlingOfNone() throws JoseException {
        JsonWebSignature jws;
        String propertyName = "org.jose4j.jws.default-allow-none";
        try {
            System.setProperty(propertyName, "true");
            jws = new JsonWebSignature();
            jws.setPayload("meh");
            jws.setAlgorithmHeaderValue("none");
            jws.getCompactSerialization();
        }
        finally {
            System.clearProperty(propertyName);
        }
        try {
            jws = new JsonWebSignature();
            jws.setPayload("meh");
            jws.setAlgorithmHeaderValue("none");
            jws.getCompactSerialization();
            Assert.fail((String)"none shouldn't have been allowed");
        }
        catch (InvalidAlgorithmException invalidAlgorithmException) {
            // empty catch block
        }
    }
}

