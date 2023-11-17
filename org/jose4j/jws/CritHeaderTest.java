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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jws.JwsTestSupport;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CritHeaderTest {
    private static final Logger log = LoggerFactory.getLogger(JwsTestSupport.class);

    @Test
    public void testOnNewKey() throws Exception {
        String[] compactSerializations;
        String headerName = "urn:example.com:nope";
        String[] stringArray = compactSerializations = new String[]{"eyJhbGciOiJFUzI1NiIsImNyaXQiOlsidXJuOmV4YW1wbGUuY29tOm5vcGUiXX0.aG93IGNyaXRpY2FsIHJlYWxseT8.F-xgvRuuaqawpLAiq6ArALlPB0Ay5_EU0YSPtw4U9teq82Gv1GyNzpO51V-u35p_oCe9dT-h0HxeznIg-uMxpQ", "eyJhbGciOiJFUzI1NiIsImNyaXQiOlsidXJuOmV4YW1wbGUuY29tOm5vcGUiXSwidXJuOmV4YW1wbGUuY29tOm5vcGUiOiJodWgifQ.aG93IGNyaXRpY2FsIHJlYWxseT8.xZvf_WCSZY2-oMvpTbHALCGgOchR8ryrV_84Q5toM8KECtm9PCEuORoMKHmCFx-UTOI1QNt28H51GV9MB4c6BQ"};
        int n = compactSerializations.length;
        int n2 = 0;
        while (n2 < n) {
            String cs = stringArray[n2];
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(cs);
            jws.setKey(ExampleEcKeysFromJws.PUBLIC_256);
            CritHeaderTest.expectFail(jws);
            jws = new JsonWebSignature();
            jws.setCompactSerialization(cs);
            jws.setKey(ExampleEcKeysFromJws.PUBLIC_256);
            jws.setKnownCriticalHeaders("urn:example.com:nope");
            Assert.assertThat((Object)"how critical really?", (Matcher)CoreMatchers.equalTo((Object)jws.getPayload()));
            ++n2;
        }
    }

    public static void expectFail(JsonWebStructure jwx) {
        try {
            jwx.getPayload();
            Assert.fail((String)"should have failed due to crit header");
        }
        catch (JoseException e) {
            log.debug("Expected something like this: {}", (Object)e.toString());
        }
    }

    @Test
    public void testJwsAppendixE() throws JoseException {
        String jwscs = "eyJhbGciOiJub25lIiwNCiAiY3JpdCI6WyJodHRwOi8vZXhhbXBsZS5jb20vVU5ERUZJTkVEIl0sDQogImh0dHA6Ly9leGFtcGxlLmNvbS9VTkRFRklORUQiOnRydWUNCn0.RkFJTA.";
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(jwscs);
        jws.setAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE);
        CritHeaderTest.expectFail(jws);
        jws = new JsonWebSignature();
        jws.setCompactSerialization(jwscs);
        jws.setAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE);
        jws.setKnownCriticalHeaders("http://example.com/UNDEFINED");
        Assert.assertThat((Object)jws.getPayload(), (Matcher)CoreMatchers.equalTo((Object)"FAIL"));
    }

    @Test
    public void testJwsBadCrit() throws JoseException {
        String[] compactSerializations;
        String[] stringArray = compactSerializations = new String[]{"eyJhbGciOiJub25lIiwKICJjcml0Ijoic2hvdWxkbm90d29yayIKfQ.RkFJTA.", "eyJhbGciOiJub25lIiwKICJjcml0Ijp0cnVlCn0.bWVo."};
        int n = compactSerializations.length;
        int n2 = 0;
        while (n2 < n) {
            String cs = stringArray[n2];
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(cs);
            jws.setAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE);
            CritHeaderTest.expectFail(jws);
            ++n2;
        }
    }

    @Test
    public void simpleRoundTrip() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setKey(ExampleEcKeysFromJws.PRIVATE_256);
        String payload = "This family is in a rut. We gotta shake things up. We're driving to Walley World.";
        jws.setPayload("This family is in a rut. We gotta shake things up. We're driving to Walley World.");
        jws.setAlgorithmHeaderValue("ES256");
        jws.setCriticalHeaderNames("nope");
        String jwsCompactSerialization = jws.getCompactSerialization();
        jws = new JsonWebSignature();
        jws.setCompactSerialization(jwsCompactSerialization);
        jws.setKey(ExampleEcKeysFromJws.PUBLIC_256);
        CritHeaderTest.expectFail(jws);
        jws = new JsonWebSignature();
        jws.setCompactSerialization(jwsCompactSerialization);
        jws.setKey(ExampleEcKeysFromJws.PUBLIC_256);
        jws.setKnownCriticalHeaders("nope");
        Assert.assertThat((Object)jws.getPayload(), (Matcher)CoreMatchers.equalTo((Object)"This family is in a rut. We gotta shake things up. We're driving to Walley World."));
    }
}

