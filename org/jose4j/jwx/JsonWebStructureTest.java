/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Before
 *  org.junit.Test
 */
package org.jose4j.jwx;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.IntegrityException;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonWebStructureTest {
    private static final String YOU_LL_GET_NOTHING_AND_LIKE_IT = "You'll get nothing, and like it!";
    private JsonWebKey oct256bitJwk;

    @Before
    public void symmetricJwk() throws JoseException {
        String json = "{\"kty\":\"oct\",\"kid\":\"9er\",\"k\":\"Ul3CckPpDfGjBzSsXCoQSvX3L0qVcAku2hW9WU-ccSs\"}";
        this.oct256bitJwk = JsonWebKey.Factory.newJwk(json);
    }

    @Test
    public void jws1() throws JoseException {
        String cs = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjllciJ9.WW91J2xsIGdldCBub3RoaW5nLCBhbmQgbGlrZSBpdCE.45s_xV_ol7JBwVcTPbWbaYT5i4mb7j27lEhi_bxpExw";
        JsonWebStructure jwx = JsonWebStructure.fromCompactSerialization(cs);
        Assert.assertTrue((String)(String.valueOf(cs) + " should give a JWS " + jwx), (boolean)(jwx instanceof JsonWebSignature));
        Assert.assertEquals((Object)"HS256", (Object)jwx.getAlgorithmHeaderValue());
        jwx.setKey(this.oct256bitJwk.getKey());
        String payload = jwx.getPayload();
        Assert.assertEquals((Object)YOU_LL_GET_NOTHING_AND_LIKE_IT, (Object)payload);
        Assert.assertEquals((Object)this.oct256bitJwk.getKeyId(), (Object)jwx.getKeyIdHeaderValue());
    }

    @Test(expected=IntegrityException.class)
    public void integrityCheckFailsJws() throws JoseException {
        String cs = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjllciJ9.RGFubnksIEknbSBoYXZpbmcgYSBwYXJ0eSB0aGlzIHdlZWtlbmQuLi4gSG93IHdvdWxkIHlvdSBsaWtlIHRvIGNvbWUgb3ZlciBhbmQgbW93IG15IGxhd24_.45s_xV_ol7JBwVcTPbWbaYT5i4mb7j27lEhi_bxpExw";
        JsonWebStructure jwx = JsonWebStructure.fromCompactSerialization(cs);
        Assert.assertTrue((String)(String.valueOf(cs) + " should give a JWS " + jwx), (boolean)(jwx instanceof JsonWebSignature));
        Assert.assertEquals((Object)"HS256", (Object)jwx.getAlgorithmHeaderValue());
        jwx.setKey(this.oct256bitJwk.getKey());
        Assert.assertEquals((Object)this.oct256bitJwk.getKeyId(), (Object)jwx.getKeyIdHeaderValue());
        jwx.getPayload();
    }

    @Test
    public void jwe1() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2Iiwia2lkIjoiOWVyIn0..XAog2l7TP5-0mIPYjT2ZYg.Zf6vQZhxeAfzk2AyuXsKJSo1R8aluPDvK7a6N7wvSmuIUczDhUtJFmNdXC3d4rPa.XBTguLfGeGKu6YsQVnes2w";
        JsonWebStructure jwx = JsonWebStructure.fromCompactSerialization(cs);
        jwx.setKey(this.oct256bitJwk.getKey());
        Assert.assertTrue((String)(String.valueOf(cs) + " should give a JWE " + jwx), (boolean)(jwx instanceof JsonWebEncryption));
        Assert.assertEquals((Object)"dir", (Object)jwx.getAlgorithmHeaderValue());
        Assert.assertEquals((Object)this.oct256bitJwk.getKeyId(), (Object)jwx.getKeyIdHeaderValue());
        String payload = jwx.getPayload();
        Assert.assertEquals((Object)YOU_LL_GET_NOTHING_AND_LIKE_IT, (Object)payload);
    }

    @Test
    public void jwe2() throws JoseException {
        String cs = "eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2Iiwia2lkIjoiOWVyIn0.RAqGCBMFk7O-B-glFckcFmxUr8BTTXuZk-bXAdRZxpk5Vgs_1yoUQw.hyl68_ADlK4VRDYiQMQS6w.xk--JKIVF4Xjxc0gRGPL30s4PSNtj685WYqXbjyItG0uSffD4ajGXdz4BO8i0sbM.WXaAVpBgftXyO1HkkRvgQQ";
        JsonWebStructure jwx = JsonWebStructure.fromCompactSerialization(cs);
        jwx.setKey(this.oct256bitJwk.getKey());
        Assert.assertTrue((String)(String.valueOf(cs) + " should give a JWE " + jwx), (boolean)(jwx instanceof JsonWebEncryption));
        Assert.assertEquals((Object)"A256KW", (Object)jwx.getAlgorithmHeaderValue());
        Assert.assertEquals((Object)this.oct256bitJwk.getKeyId(), (Object)jwx.getKeyIdHeaderValue());
        String payload = jwx.getPayload();
        Assert.assertEquals((Object)YOU_LL_GET_NOTHING_AND_LIKE_IT, (Object)payload);
    }

    @Test(expected=IntegrityException.class)
    public void integrityCheckFailsJwe() throws JoseException {
        String cs = "eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2Iiwia2lkIjoiOWVyIn0.RAqGCBMFk7O-B-glFckcFmxUr8BTTXuZk-bXAdRZxpk5Vgs_1yoUQw.hyl68_ADlK4VRDYiQMQS6w.xk--JKIVF4Xjxc0gRGPL30s4PSNtj685WYqXbjyItG0uSffD4ajGXdz4BO8i0sbM.aXaAVpBgftxqO1HkkRvgab";
        JsonWebStructure jwx = JsonWebStructure.fromCompactSerialization(cs);
        jwx.setKey(this.oct256bitJwk.getKey());
        Assert.assertTrue((String)(String.valueOf(cs) + " should give a JWE " + jwx), (boolean)(jwx instanceof JsonWebEncryption));
        Assert.assertEquals((Object)"A256KW", (Object)jwx.getAlgorithmHeaderValue());
        Assert.assertEquals((Object)this.oct256bitJwk.getKeyId(), (Object)jwx.getKeyIdHeaderValue());
        jwx.getPayload();
    }

    @Test(expected=JoseException.class)
    public void testFromInvalidCompactSerialization1() throws Exception {
        JsonWebStructure.fromCompactSerialization("blah.blah.blah.blah");
    }

    @Test(expected=JoseException.class)
    public void testFromInvalidCompactSerialization2() throws Exception {
        JsonWebStructure.fromCompactSerialization("nope");
    }

    @Test(expected=JoseException.class)
    public void testFromInvalidCompactSerialization3() throws Exception {
        JsonWebStructure.fromCompactSerialization("blah.blah.blah.blah.too.darn.many");
    }

    @Test(expected=JoseException.class)
    public void testFromInvalidCompactSerialization4() throws Exception {
        JsonWebStructure.fromCompactSerialization("eyJhbGciOiJIUzI1NiJ9..c29tZSBjb250ZW50IHRoYXQgaXMgdGhlIHBheWxvYWQ.qGO7O7W2ECVl6uO7lfsXDgEF-EUEti0i-a_AimulIRA");
    }
}

