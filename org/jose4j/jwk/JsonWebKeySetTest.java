/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwk;

import java.security.Key;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class JsonWebKeySetTest {
    @Test
    public void testParseExamplePublicKeys() throws JoseException {
        String jwkJson = "{\"keys\":\n     [\n       {\"kty\":\"EC\",\n        \"crv\":\"P-256\",\n        \"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\",\n        \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\",\n        \"use\":\"enc\",\n        \"kid\":\"1\"},\n\n       {\"kty\":\"RSA\",\n        \"n\": \"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx   4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMs   tn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2   QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbI   SD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqb   w0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\",\n        \"e\":\"AQAB\",\n        \"alg\":\"RS256\",\n        \"kid\":\"2011-04-29\"}\n     ]\n   }";
        JsonWebKeySet jwkSet = new JsonWebKeySet(jwkJson);
        List<JsonWebKey> jwks = jwkSet.getJsonWebKeys();
        Assert.assertEquals((long)2L, (long)jwks.size());
        Iterator iterator = jwks.iterator();
        Assert.assertTrue((boolean)(iterator.next() instanceof EllipticCurveJsonWebKey));
        Assert.assertTrue((boolean)(iterator.next() instanceof RsaJsonWebKey));
        JsonWebKey webKey1 = jwkSet.findJsonWebKey("1", null, null, null);
        Assert.assertTrue((boolean)(webKey1 instanceof EllipticCurveJsonWebKey));
        Assert.assertEquals((Object)"enc", (Object)webKey1.getUse());
        Assert.assertNotNull((Object)webKey1.getKey());
        Assert.assertNull((Object)((PublicJsonWebKey)webKey1).getPrivateKey());
        JsonWebKey webKey2011 = jwkSet.findJsonWebKey("2011-04-29", null, null, null);
        Assert.assertTrue((boolean)(webKey2011 instanceof RsaJsonWebKey));
        Assert.assertNotNull((Object)webKey2011.getKey());
        Assert.assertNull((Object)((PublicJsonWebKey)webKey2011).getPrivateKey());
        Assert.assertEquals((Object)"RS256", (Object)webKey2011.getAlgorithm());
        Assert.assertEquals((Object)"enc", (Object)jwkSet.findJsonWebKey("1", null, null, null).getUse());
        Assert.assertNull((Object)jwkSet.findJsonWebKey("nope", null, null, null));
        String json = jwkSet.toJson();
        Assert.assertNotNull((Object)json);
        Assert.assertTrue((boolean)json.contains("0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx"));
    }

    @Test
    public void testParseExamplePrivateKeys() throws JoseException {
        String jwkJson = "{\"keys\":\n       [\n         {\"kty\":\"EC\",\n          \"crv\":\"P-256\",\n          \"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\",\n          \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\",\n          \"d\":\"870MB6gfuTJ4HtUnUvYMyJpr5eUZNP4Bk43bVdj3eAE\",\n          \"use\":\"enc\",\n          \"kid\":\"1\"},\n\n         {\"kty\":\"RSA\",\n          \"n\":\"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4\n     cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMst\n     n64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2Q\n     vzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbIS\n     D08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw\n     0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\",\n          \"e\":\"AQAB\",\n          \"d\":\"X4cTteJY_gn4FYPsXB8rdXix5vwsg1FLN5E3EaG6RJoVH-HLLKD9\n     M7dx5oo7GURknchnrRweUkC7hT5fJLM0WbFAKNLWY2vv7B6NqXSzUvxT0_YSfqij\n     wp3RTzlBaCxWp4doFk5N2o8Gy_nHNKroADIkJ46pRUohsXywbReAdYaMwFs9tv8d\n     _cPVY3i07a3t8MN6TNwm0dSawm9v47UiCl3Sk5ZiG7xojPLu4sbg1U2jx4IBTNBz\n     nbJSzFHK66jT8bgkuqsk0GjskDJk19Z4qwjwbsnn4j2WBii3RL-Us2lGVkY8fkFz\n     me1z0HbIkfz0Y6mqnOYtqc0X4jfcKoAC8Q\",\n          \"p\":\"83i-7IvMGXoMXCskv73TKr8637FiO7Z27zv8oj6pbWUQyLPQBQxtPV\n     nwD20R-60eTDmD2ujnMt5PoqMrm8RfmNhVWDtjjMmCMjOpSXicFHj7XOuVIYQyqV\n     WlWEh6dN36GVZYk93N8Bc9vY41xy8B9RzzOGVQzXvNEvn7O0nVbfs\",\n          \"q\":\"3dfOR9cuYq-0S-mkFLzgItgMEfFzB2q3hWehMuG0oCuqnb3vobLyum\n     qjVZQO1dIrdwgTnCdpYzBcOfW5r370AFXjiWft_NGEiovonizhKpo9VVS78TzFgx\n     kIdrecRezsZ-1kYd_s1qDbxtkDEgfAITAG9LUnADun4vIcb6yelxk\",\n          \"dp\":\"G4sPXkc6Ya9y8oJW9_ILj4xuppu0lzi_H7VTkS8xj5SdX3coE0oim\n     YwxIi2emTAue0UOa5dpgFGyBJ4c8tQ2VF402XRugKDTP8akYhFo5tAA77Qe_Nmtu\n     YZc3C3m3I24G2GvR5sSDxUyAN2zq8Lfn9EUms6rY3Ob8YeiKkTiBj0\",\n          \"dq\":\"s9lAH9fggBsoFR8Oac2R_E2gw282rT2kGOAhvIllETE1efrA6huUU\n     vMfBcMpn8lqeW6vzznYY5SSQF7pMdC_agI3nG8Ibp1BUb0JUiraRNqUfLhcQb_d9\n     GF4Dh7e74WbRsobRonujTYN1xCaP6TO61jvWrX-L18txXw494Q_cgk\",\n          \"qi\":\"GyM_p6JrXySiz1toFgKbWV-JdI3jQ4ypu9rbMWx3rQJBfmt0FoYzg\n     UIZEVFEcOqwemRN81zoDAaa-Bk0KWNGDjJHZDdDmFhW3AN7lI-puxk_mHZGJ11rx\n     yR8O55XLSe3SPmRfKwZI6yU24ZxvQKFYItdldUKGzO6Ia6zTKhAVRU\",\n          \"alg\":\"RS256\",\n          \"kid\":\"2011-04-29\"}\n       ]\n     }\n";
        JsonWebKeySet jwkSet = new JsonWebKeySet(jwkJson);
        List<JsonWebKey> jwks = jwkSet.getJsonWebKeys();
        Assert.assertEquals((long)2L, (long)jwks.size());
        Iterator iterator = jwks.iterator();
        Assert.assertTrue((boolean)(iterator.next() instanceof EllipticCurveJsonWebKey));
        Assert.assertTrue((boolean)(iterator.next() instanceof RsaJsonWebKey));
        JsonWebKey webKey1 = jwkSet.findJsonWebKey("1", null, null, null);
        Assert.assertTrue((boolean)(webKey1 instanceof EllipticCurveJsonWebKey));
        Assert.assertEquals((Object)"enc", (Object)webKey1.getUse());
        Assert.assertNotNull((Object)webKey1.getKey());
        Assert.assertNotNull((Object)((PublicJsonWebKey)webKey1).getPrivateKey());
        JsonWebKey webKey2011 = jwkSet.findJsonWebKey("2011-04-29", null, null, null);
        Assert.assertTrue((boolean)(webKey2011 instanceof RsaJsonWebKey));
        Assert.assertNotNull((Object)webKey2011.getKey());
        Assert.assertEquals((Object)"RS256", (Object)webKey2011.getAlgorithm());
        Assert.assertNotNull((Object)((PublicJsonWebKey)webKey2011).getPrivateKey());
        Assert.assertEquals((Object)"enc", (Object)jwkSet.findJsonWebKey("1", null, null, null).getUse());
        Assert.assertNull((Object)jwkSet.findJsonWebKey("nope", null, null, null));
        String json = jwkSet.toJson();
        Assert.assertNotNull((Object)json);
        Assert.assertTrue((boolean)json.contains("0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx"));
    }

    @Test
    public void testParseExampleSymmetricKeys() throws JoseException {
        String jwkJson = "{\"keys\":\n       [\n         {\"kty\":\"oct\",\n          \"alg\":\"A128KW\",\n          \"k\":\"GawgguFyGrWKav7AX4VKUg\"},\n\n         {\"kty\":\"oct\",\n          \"k\":\"AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75\n     aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow\",\n          \"kid\":\"HMAC key used in JWS A.1 example\"}\n       ]\n     }\n";
        JsonWebKeySet jwkSet = new JsonWebKeySet(jwkJson);
        List<JsonWebKey> jwks = jwkSet.getJsonWebKeys();
        Assert.assertEquals((long)2L, (long)jwks.size());
        Iterator iterator = jwks.iterator();
        Assert.assertTrue((boolean)(iterator.next() instanceof OctetSequenceJsonWebKey));
        Assert.assertTrue((boolean)(iterator.next() instanceof OctetSequenceJsonWebKey));
        Assert.assertFalse((boolean)iterator.hasNext());
        JsonWebKey jwk2 = jwkSet.findJsonWebKey("HMAC key used in JWS A.1 example", null, null, null);
        Key key2 = jwk2.getKey();
        Assert.assertNotNull((Object)key2);
        Assert.assertEquals((long)64L, (long)key2.getEncoded().length);
        JsonWebKey jwk1 = jwkSet.findJsonWebKey(null, null, null, "A128KW");
        Key key1 = jwk1.getKey();
        Assert.assertNotNull((Object)key1);
        Assert.assertEquals((long)16L, (long)key1.getEncoded().length);
    }

    @Test
    public void testFromRsaPublicKeyAndBack() throws JoseException {
        RsaJsonWebKey webKey = new RsaJsonWebKey(ExampleRsaKeyFromJws.PUBLIC_KEY);
        String kid = "my-key-id";
        webKey.setKeyId(kid);
        webKey.setUse("sig");
        JsonWebKeySet jwkSet = new JsonWebKeySet(Collections.singletonList(webKey));
        String json = jwkSet.toJson();
        Assert.assertTrue((boolean)json.contains("sig"));
        Assert.assertTrue((boolean)json.contains(kid));
        JsonWebKeySet parsedJwkSet = new JsonWebKeySet(json);
        List<JsonWebKey> webKeyKeyObjects = parsedJwkSet.getJsonWebKeys();
        Assert.assertEquals((long)1L, (long)webKeyKeyObjects.size());
        JsonWebKey jwk = parsedJwkSet.findJsonWebKey(kid, null, null, null);
        Assert.assertEquals((Object)"RSA", (Object)jwk.getKeyType());
        Assert.assertEquals((Object)kid, (Object)jwk.getKeyId());
        Assert.assertEquals((Object)"sig", (Object)jwk.getUse());
        RsaJsonWebKey rsaJsonWebKey = (RsaJsonWebKey)jwk;
        Assert.assertEquals((Object)ExampleRsaKeyFromJws.PUBLIC_KEY.getModulus(), (Object)rsaJsonWebKey.getRsaPublicKey().getModulus());
        Assert.assertEquals((Object)ExampleRsaKeyFromJws.PUBLIC_KEY.getPublicExponent(), (Object)rsaJsonWebKey.getRsaPublicKey().getPublicExponent());
    }

    @Test
    public void testFromEcPublicKeyAndBack() throws JoseException {
        ECPublicKey[] eCPublicKeyArray = new ECPublicKey[]{ExampleEcKeysFromJws.PUBLIC_256, ExampleEcKeysFromJws.PUBLIC_521};
        int n = eCPublicKeyArray.length;
        int n2 = 0;
        while (n2 < n) {
            ECPublicKey publicKey = eCPublicKeyArray[n2];
            EllipticCurveJsonWebKey webKey = new EllipticCurveJsonWebKey(publicKey);
            String kid = "kkiidd";
            webKey.setKeyId(kid);
            webKey.setUse("enc");
            JsonWebKeySet jwkSet = new JsonWebKeySet(Collections.singletonList(webKey));
            String json = jwkSet.toJson();
            Assert.assertTrue((boolean)json.contains("enc"));
            Assert.assertTrue((boolean)json.contains(kid));
            JsonWebKeySet parsedJwkSet = new JsonWebKeySet(json);
            List<JsonWebKey> webKeyKeyObjects = parsedJwkSet.getJsonWebKeys();
            Assert.assertEquals((long)1L, (long)webKeyKeyObjects.size());
            JsonWebKey jwk = parsedJwkSet.findJsonWebKey(kid, null, null, null);
            Assert.assertEquals((Object)"EC", (Object)jwk.getKeyType());
            Assert.assertEquals((Object)kid, (Object)jwk.getKeyId());
            Assert.assertEquals((Object)"enc", (Object)jwk.getUse());
            EllipticCurveJsonWebKey ecJsonWebKey = (EllipticCurveJsonWebKey)jwk;
            Assert.assertEquals((Object)publicKey.getW().getAffineX(), (Object)ecJsonWebKey.getECPublicKey().getW().getAffineX());
            Assert.assertEquals((Object)publicKey.getW().getAffineY(), (Object)ecJsonWebKey.getECPublicKey().getW().getAffineY());
            Assert.assertEquals((long)publicKey.getParams().getCofactor(), (long)ecJsonWebKey.getECPublicKey().getParams().getCofactor());
            Assert.assertEquals((Object)publicKey.getParams().getCurve(), (Object)ecJsonWebKey.getECPublicKey().getParams().getCurve());
            Assert.assertEquals((Object)publicKey.getParams().getGenerator(), (Object)ecJsonWebKey.getECPublicKey().getParams().getGenerator());
            Assert.assertEquals((Object)publicKey.getParams().getOrder(), (Object)ecJsonWebKey.getECPublicKey().getParams().getOrder());
            ++n2;
        }
    }

    @Test
    public void testCreateFromListOfPubJwks() throws JoseException {
        ArrayList<EllipticCurveJsonWebKey> ecjwks = new ArrayList<EllipticCurveJsonWebKey>();
        ecjwks.add(EcJwkGenerator.generateJwk(EllipticCurves.P256));
        ecjwks.add(EcJwkGenerator.generateJwk(EllipticCurves.P256));
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(ecjwks);
        Assert.assertEquals((long)2L, (long)jsonWebKeySet.getJsonWebKeys().size());
    }

    @Test
    public void testOctAndDefaultToJson() throws JoseException {
        JsonWebKeySet jwks = new JsonWebKeySet(OctJwkGenerator.generateJwk(128), OctJwkGenerator.generateJwk(128));
        String json = jwks.toJson();
        Assert.assertTrue((boolean)json.contains("\"k\""));
        JsonWebKeySet newJwks = new JsonWebKeySet(json);
        Assert.assertEquals((long)jwks.getJsonWebKeys().size(), (long)newJwks.getJsonWebKeys().size());
    }

    @Test
    public void testNewWithVarArgsAndAddLater() throws Exception {
        JsonWebKey jwk1 = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"bbj4v-CvqwOm1q3WkVJEpw\"}");
        JsonWebKey jwk2 = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"h008v_ab_Z-N7q13D-JabC\"}");
        JsonWebKey jwk3 = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"-_-8888888888888888-_-\"}");
        JsonWebKey jwk4 = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"__--_12_--33--_21_--__\"}");
        JsonWebKeySet jwks = new JsonWebKeySet(jwk1);
        jwks.addJsonWebKey(jwk2);
        List<JsonWebKey> jwkList = jwks.getJsonWebKeys();
        jwkList.add(jwk3);
        Assert.assertEquals((long)3L, (long)jwkList.size());
        Assert.assertEquals((long)3L, (long)jwks.getJsonWebKeys().size());
        jwks = new JsonWebKeySet(jwkList);
        jwks.addJsonWebKey(jwk4);
        Assert.assertEquals((long)4L, (long)jwks.getJsonWebKeys().size());
    }

    @Test
    public void testParseSetContainingInvalid() throws Exception {
        String json = "{\"keys\":[{\"kty\":\"EC\",\"x\":\"riwTtQeRjmlDsR4PUQELhejpPkZkQstb0_Lf08qeBzM\",\"y\":\"izN8y6z-8j8bB_Lj10gX9mnaE_E0ZK5fl0hJVyLWMKA\",\"crv\":\"P-256\"},{\"kty\":false,\"x\":\"GS2tEeCRf0CFHzI_y68XiLzqa9-RpG4Xn-dq2lPtShY\",\"y\":\"Rq6ybA7IbjhDTfvP2GSzxEql8II7RvRPb3mJ6tzZUgI\",\"crv\":\"P-256\"},{\"kty\":\"EC\",\"x\":\"IiIIM4W-HDen_11XiGlFXh1kOxKcX1YB5gqMrCM-hMM\",\"y\":\"57-3xqdddSBBarwwXcWu4hIG4dAlIiEYdy4aaFGb57s\",\"crv\":\"P-256\"},{\"kty\":\"EC\",\"x\":[\"IiIIM4W-HDen_11XiGlFXh1kOxKcX1YB5gqMrCM-hMM\",\"huh\"],\"y\":\"57-3xqdddSBBarwwXcWu4hIG4dAlIiEYdy4aaFGb57s\",\"crv\":\"P-256\"},{\"kty\":\"EC\",\"x\":\"rO8MozDmEAVZ0B5zQUDD8PGosFlwmoMmi7I-1rspWz4\",\"y\":\"I6ku1iUzFJgTnjNzjAC1sSGkYfiDqs-eEReFMLI-6n8\",\"crv\":\"P-256\"}{\"kty\":1,\"x\":\"IiIIM4W-HDen_11XiGlFXh1kOxKcX1YB5gqMrCM-hMM\",\"y\":\"57-3xqdddSBBarwwXcWu4hIG4dAlIiEYdy4aaFGb57s\",\"crv\":\"P-256\"},{\"kty\":885584955514411149933357445595595145885566661,\"x\":\"IiIIM4W-HDen_11XiGlFXh1kOxKcX1YB5gqMrCM-hMM\",\"y\":\"57-3xqdddSBBarwwXcWu4hIG4dAlIiEYdy4aaFGb57s\",\"crv\":\"P-256\"},{\"kty\":{\"EC\":\"EC\"},\"x\":\"riwTtQeRjmlDsR4PUQELhejpPkZkQstb0_Lf08qeBzM\",\"y\":\"izN8y6z-8j8bB_Lj10gX9mnaE_E0ZK5fl0hJVyLWMKA\",\"crv\":\"P-256\"},{\"kty\":null,\"x\":\"riwTtQeRjmlDsR4PUQELhejpPkZkQstb0_Lf08qeBzM\",\"y\":\"izN8y6z-8j8bB_Lj10gX9mnaE_E0ZK5fl0hJVyLWMKA\",\"crv\":\"P-256\"},]}";
        JsonWebKeySet jwks = new JsonWebKeySet(json);
        Assert.assertEquals((long)3L, (long)jwks.getJsonWebKeys().size());
    }
}

