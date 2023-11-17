/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwk;

import java.security.interfaces.RSAPrivateCrtKey;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeyTest;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class RsaJsonWebKeyTest {
    private static final String RSA_JWK_WITH_PRIVATE_KEY = "{\"kty\":\"RSA\",\n \"n\":\"ofgWCuLjybRlzo0tZWJjNiuSfb4p4fAkd_wWJcyQoTbji9k0l8W26mPddx\n      HmfHQp-Vaw-4qPCJrcS2mJPMEzP1Pt0Bm4d4QlL-yRT-SFd2lZS-pCgNMs\n      D1W_YpRPEwOWvG6b32690r2jZ47soMZo9wGzjb_7OMg0LOL-bSf63kpaSH\n      SXndS5z5rexMdbBYUsLA9e-KXBdQOS-UTo7WTBEMa2R2CapHg665xsmtdV\n      MTBQY4uDZlxvb3qCo5ZwKh9kG4LT6_I5IhlJH7aGhyxXFvUK-DWNmoudF8\n      NAco9_h9iaGNj8q2ethFkMLs91kzk2PAcDTW9gb54h4FRWyuXpoQ\",\n \"e\":\"AQAB\",\n \"d\":\"Eq5xpGnNCivDflJsRQBXHx1hdR1k6Ulwe2JZD50LpXyWPEAeP88vLNO97I\n      jlA7_GQ5sLKMgvfTeXZx9SE-7YwVol2NXOoAJe46sui395IW_GO-pWJ1O0\n      BkTGoVEn2bKVRUCgu-GjBVaYLU6f3l9kJfFNS3E0QbVdxzubSu3Mkqzjkn\n      439X0M_V51gfpRLI9JYanrC4D4qAdGcopV_0ZHHzQlBjudU2QvXt4ehNYT\n      CBr6XCLQUShb1juUO1ZdiYoFaFQT5Tw8bGUl_x_jTj3ccPDVZFD9pIuhLh\n      BOneufuBiB4cS98l2SR_RQyGWSeWjnczT0QU91p1DhOVRuOopznQ\"\n}";

    @Test
    public void testParseExampleWithPrivate() throws JoseException {
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(RSA_JWK_WITH_PRIVATE_KEY);
        PublicJsonWebKey pubJwk = (PublicJsonWebKey)jwk;
        Assert.assertEquals((Object)ExampleRsaKeyFromJws.PRIVATE_KEY, (Object)pubJwk.getPrivateKey());
        Assert.assertEquals((Object)ExampleRsaKeyFromJws.PUBLIC_KEY, (Object)pubJwk.getPublicKey());
    }

    @Test
    public void testFromKeyWithPrivate() throws JoseException {
        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(ExampleRsaKeyFromJws.PUBLIC_KEY);
        String jsonNoPrivateKey = jwk.toJson();
        jwk.setPrivateKey(ExampleRsaKeyFromJws.PRIVATE_KEY);
        String dKey = "\"d\"";
        Assert.assertFalse((boolean)jwk.toJson().contains(dKey));
        Assert.assertEquals((Object)jsonNoPrivateKey, (Object)jwk.toJson());
        Assert.assertTrue((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE).contains(dKey));
    }

    @Test
    public void testFromKeyWithCrtPrivateAndBackAndAgain() throws JoseException {
        String json = "{\"kty\":\"RSA\",\n          \"n\":\"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4\n     cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMst\n     n64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2Q\n     vzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbIS\n     D08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw\n     0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\",\n          \"e\":\"AQAB\",\n          \"d\":\"X4cTteJY_gn4FYPsXB8rdXix5vwsg1FLN5E3EaG6RJoVH-HLLKD9\n     M7dx5oo7GURknchnrRweUkC7hT5fJLM0WbFAKNLWY2vv7B6NqXSzUvxT0_YSfqij\n     wp3RTzlBaCxWp4doFk5N2o8Gy_nHNKroADIkJ46pRUohsXywbReAdYaMwFs9tv8d\n     _cPVY3i07a3t8MN6TNwm0dSawm9v47UiCl3Sk5ZiG7xojPLu4sbg1U2jx4IBTNBz\n     nbJSzFHK66jT8bgkuqsk0GjskDJk19Z4qwjwbsnn4j2WBii3RL-Us2lGVkY8fkFz\n     me1z0HbIkfz0Y6mqnOYtqc0X4jfcKoAC8Q\",\n          \"p\":\"83i-7IvMGXoMXCskv73TKr8637FiO7Z27zv8oj6pbWUQyLPQBQxtPV\n     nwD20R-60eTDmD2ujnMt5PoqMrm8RfmNhVWDtjjMmCMjOpSXicFHj7XOuVIYQyqV\n     WlWEh6dN36GVZYk93N8Bc9vY41xy8B9RzzOGVQzXvNEvn7O0nVbfs\",\n          \"q\":\"3dfOR9cuYq-0S-mkFLzgItgMEfFzB2q3hWehMuG0oCuqnb3vobLyum\n     qjVZQO1dIrdwgTnCdpYzBcOfW5r370AFXjiWft_NGEiovonizhKpo9VVS78TzFgx\n     kIdrecRezsZ-1kYd_s1qDbxtkDEgfAITAG9LUnADun4vIcb6yelxk\",\n          \"dp\":\"G4sPXkc6Ya9y8oJW9_ILj4xuppu0lzi_H7VTkS8xj5SdX3coE0oim\n     YwxIi2emTAue0UOa5dpgFGyBJ4c8tQ2VF402XRugKDTP8akYhFo5tAA77Qe_Nmtu\n     YZc3C3m3I24G2GvR5sSDxUyAN2zq8Lfn9EUms6rY3Ob8YeiKkTiBj0\",\n          \"dq\":\"s9lAH9fggBsoFR8Oac2R_E2gw282rT2kGOAhvIllETE1efrA6huUU\n     vMfBcMpn8lqeW6vzznYY5SSQF7pMdC_agI3nG8Ibp1BUb0JUiraRNqUfLhcQb_d9\n     GF4Dh7e74WbRsobRonujTYN1xCaP6TO61jvWrX-L18txXw494Q_cgk\",\n          \"qi\":\"GyM_p6JrXySiz1toFgKbWV-JdI3jQ4ypu9rbMWx3rQJBfmt0FoYzg\n     UIZEVFEcOqwemRN81zoDAaa-Bk0KWNGDjJHZDdDmFhW3AN7lI-puxk_mHZGJ11rx\n     yR8O55XLSe3SPmRfKwZI6yU24ZxvQKFYItdldUKGzO6Ia6zTKhAVRU\",\n          \"alg\":\"RS256\",\n          \"kid\":\"2011-04-29\"}";
        this.doKeyWithCrtPrivateAndBackAndAgain(json);
    }

    @Test
    public void testFromCrtAndBackWithJwsAppendixA2() throws JoseException {
        String json = "     {\"kty\":\"RSA\",\n      \"n\":\"ofgWCuLjybRlzo0tZWJjNiuSfb4p4fAkd_wWJcyQoTbji9k0l8W26mPddx\n           HmfHQp-Vaw-4qPCJrcS2mJPMEzP1Pt0Bm4d4QlL-yRT-SFd2lZS-pCgNMs\n           D1W_YpRPEwOWvG6b32690r2jZ47soMZo9wGzjb_7OMg0LOL-bSf63kpaSH\n           SXndS5z5rexMdbBYUsLA9e-KXBdQOS-UTo7WTBEMa2R2CapHg665xsmtdV\n           MTBQY4uDZlxvb3qCo5ZwKh9kG4LT6_I5IhlJH7aGhyxXFvUK-DWNmoudF8\n           NAco9_h9iaGNj8q2ethFkMLs91kzk2PAcDTW9gb54h4FRWyuXpoQ\",\n      \"e\":\"AQAB\",\n      \"d\":\"Eq5xpGnNCivDflJsRQBXHx1hdR1k6Ulwe2JZD50LpXyWPEAeP88vLNO97I\n           jlA7_GQ5sLKMgvfTeXZx9SE-7YwVol2NXOoAJe46sui395IW_GO-pWJ1O0\n           BkTGoVEn2bKVRUCgu-GjBVaYLU6f3l9kJfFNS3E0QbVdxzubSu3Mkqzjkn\n           439X0M_V51gfpRLI9JYanrC4D4qAdGcopV_0ZHHzQlBjudU2QvXt4ehNYT\n           CBr6XCLQUShb1juUO1ZdiYoFaFQT5Tw8bGUl_x_jTj3ccPDVZFD9pIuhLh\n           BOneufuBiB4cS98l2SR_RQyGWSeWjnczT0QU91p1DhOVRuOopznQ\",\n      \"p\":\"4BzEEOtIpmVdVEZNCqS7baC4crd0pqnRH_5IB3jw3bcxGn6QLvnEtfdUdi\n           YrqBdss1l58BQ3KhooKeQTa9AB0Hw_Py5PJdTJNPY8cQn7ouZ2KKDcmnPG\n           BY5t7yLc1QlQ5xHdwW1VhvKn-nXqhJTBgIPgtldC-KDV5z-y2XDwGUc\",\n      \"q\":\"uQPEfgmVtjL0Uyyx88GZFF1fOunH3-7cepKmtH4pxhtCoHqpWmT8YAmZxa\n           ewHgHAjLYsp1ZSe7zFYHj7C6ul7TjeLQeZD_YwD66t62wDmpe_HlB-TnBA\n           -njbglfIsRLtXlnDzQkv5dTltRJ11BKBBypeeF6689rjcJIDEz9RWdc\",\n      \"dp\":\"BwKfV3Akq5_MFZDFZCnW-wzl-CCo83WoZvnLQwCTeDv8uzluRSnm71I3Q\n           CLdhrqE2e9YkxvuxdBfpT_PI7Yz-FOKnu1R6HsJeDCjn12Sk3vmAktV2zb\n           34MCdy7cpdTh_YVr7tss2u6vneTwrA86rZtu5Mbr1C1XsmvkxHQAdYo0\",\n      \"dq\":\"h_96-mK1R_7glhsum81dZxjTnYynPbZpHziZjeeHcXYsXaaMwkOlODsWa\n           7I9xXDoRwbKgB719rrmI2oKr6N3Do9U0ajaHF-NKJnwgjMd2w9cjz3_-ky\n           NlxAr2v4IKhGNpmM5iIgOS1VZnOZ68m6_pbLBSp3nssTdlqvd0tIiTHU\",\n      \"qi\":\"IYd7DHOhrWvxkwPQsRM2tOgrjbcrfvtQJipd-DlcxyVuuM9sQLdgjVk2o\n           y26F0EmpScGLq2MowX7fhd_QJQ3ydy5cY7YIBi87w93IKLEdfnbJtoOPLU\n           W0ITrJReOgo1cq9SbsxYawBgfp_gh6A5603k2-ZQwVK0JKSHuLFkuQ3U\"\n     }";
        this.doKeyWithCrtPrivateAndBackAndAgain(json);
    }

    private void doKeyWithCrtPrivateAndBackAndAgain(String json) throws JoseException {
        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(json);
        Assert.assertTrue((boolean)(jwk.getPrivateKey() instanceof RSAPrivateCrtKey));
        String jsonOut = jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        Assert.assertFalse((boolean)jsonOut.contains("\"d\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"p\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"q\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"dp\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"dq\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"qi\""));
        jsonOut = jwk.toJson();
        Assert.assertFalse((boolean)jsonOut.contains("\"d\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"p\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"q\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"dp\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"dq\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"qi\""));
        jsonOut = jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
        Assert.assertFalse((boolean)jsonOut.contains("\"d\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"p\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"q\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"dp\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"dq\""));
        Assert.assertFalse((boolean)jsonOut.contains("\"qi\""));
        jsonOut = jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        Assert.assertTrue((boolean)jsonOut.contains("\"d\""));
        Assert.assertTrue((boolean)jsonOut.contains("\"p\""));
        Assert.assertTrue((boolean)jsonOut.contains("\"q\""));
        Assert.assertTrue((boolean)jsonOut.contains("\"dp\""));
        Assert.assertTrue((boolean)jsonOut.contains("\"dq\""));
        Assert.assertTrue((boolean)jsonOut.contains("\"qi\""));
        JsonWebKeyTest.checkEncoding(jsonOut, "n", "e", "d", "p", "q", "dp", "dq", "qi");
        PublicJsonWebKey jwkAgain = PublicJsonWebKey.Factory.newPublicJwk(jsonOut);
        Assert.assertTrue((boolean)(jwkAgain.getPrivateKey() instanceof RSAPrivateCrtKey));
        Assert.assertEquals((Object)jwk.getPrivateKey(), (Object)jwkAgain.getPrivateKey());
    }

    @Test
    public void testToJsonWithPublicKeyOnlyJWKAndIncludePrivateSettings() throws JoseException {
        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(ExampleRsaKeyFromJws.PUBLIC_KEY);
        String jsonNoPrivateKey = jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        PublicJsonWebKey publicOnlyJWK = PublicJsonWebKey.Factory.newPublicJwk(jsonNoPrivateKey);
        Assert.assertThat((Object)jsonNoPrivateKey, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)publicOnlyJWK.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE))));
    }
}

