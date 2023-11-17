/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwt.consumer;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.SimpleJwtConsumerTestHelp;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class GoogsTooSmallKeyJwtConsumerTest {
    static String ID_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc2ZmQzMmFlYzdlMGY4YzE5MGRkYThiOWRkODVlN2NmNWFkMzNjNDMifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTE2MzA4NDA4MzE0NjYxNDc4MTMyIiwiYXpwIjoiODIyNzM3NTU1NDI5LWV2dmtkMDBvdHFyNWdsMTEwbmZhcGlzamZvZWEzNmpmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJqa3Rlc3QxQG1hcml0aW1lc291cmNlLmNhIiwiYXRfaGFzaCI6Im85bUZjZUx6QV9ZMnhmNEJqVmdOQmciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiODIyNzM3NTU1NDI5LWV2dmtkMDBvdHFyNWdsMTEwbmZhcGlzamZvZWEzNmpmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaGQiOiJtYXJpdGltZXNvdXJjZS5jYSIsIm9wZW5pZF9pZCI6Imh0dHBzOi8vd3d3Lmdvb2dsZS5jb20vYWNjb3VudHMvbzgvaWQ_aWQ9QUl0T2F3bGIxSEhFZFJJZW00d2Z1MXFNY1BUdWZvUDZzTi11ZVVrIiwiaWF0IjoxNDMxNjEyMjM4LCJleHAiOjE0MzE2MTU4Mzh9.RRMVpR9WJrkddegS4uKNT7rTov-LvRQ9sCtGo_SXrqkNbLZgArSJcmmHHxoQDsVWUjl2ZNG-7ZjDRuMu-POJLR4GHpwmQ8gttAEeywkiW4in5pUOb21AdgH29HDwG2mY6iVavsASHRutK747gURRlpt3wUJOJk00T9W2N0fVsTE";
    static String JWKS_JSON = "{ \"keys\": [\n  {\n   \"kty\": \"RSA\",\n   \"alg\": \"RS256\",\n   \"use\": \"sig\",\n   \"kid\": \"76fd32aec7e0f8c190dda8b9dd85e7cf5ad33c43\",\n   \"n\": \"03TVzpSoWDe8iPqvAde1JmmITIHD6JU8Koy10fSUW0u1QO6fle93GxHOHeQmP7FBhLSy5gWK23za38kN0KMucYGOjcWOwnO_pTQrCXxFzD-HBy_IiRyRkhuaQXsKvpJbblMEmcfeR4cWlzKt9RKjjXBl5bmIiLrN167iftlR84E\",\n   \"e\": \"AQAB\"\n  },\n  {\n   \"kty\": \"RSA\",\n   \"alg\": \"RS256\",\n   \"use\": \"sig\",\n   \"kid\": \"317b5931c783031d970c1a2552266215598a9814\",\n   \"n\": \"sxAi31Tz53-HtjmVlGpyNEGO8MtL-uvwdKDG__a-gPYE8WGEQQgpBXjjFqmIsfs-yd8YHYw0uCJwAu-ILT1AbhVTZiEEnrLKNTc_gPqfveZxnySJCguVx1pWpZ0q9cBMdgvetrbUfRO2Sz1YFgfD7k9BacWwOM-eiFtgrWwOTo8\",\n   \"e\": \"AQAB\"\n  }\n ]\n}";
    static final String CLIENT_ID = "822737555429-evvkd00otqr5gl110nfapisjfoea36jf.apps.googleusercontent.com";
    static final String ISSUER = "accounts.google.com";
    static final NumericDate EVALUATION_TIME = NumericDate.fromSeconds(1431612438L);
    static final String SUBJECT_VALUE = "116308408314661478132";

    @Test
    public void strictByDefault() throws JoseException {
        JsonWebKeySet jwks = new JsonWebKeySet(JWKS_JSON);
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(EVALUATION_TIME).setRequireSubject().setExpectedIssuer(ISSUER).setExpectedAudience(CLIENT_ID).setVerificationKeyResolver(verificationKeyResolver).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(ID_TOKEN, jwtConsumer);
    }

    @Test
    public void firstWorkaroundUsingTwoPass() throws Exception {
        JwtConsumer firstPassJwtConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassJwtConsumer.process(ID_TOKEN);
        jwtContext.getJoseObjects().iterator().next().setDoKeyValidation(false);
        JsonWebKeySet jwks = new JsonWebKeySet(JWKS_JSON);
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(EVALUATION_TIME).setRequireSubject().setExpectedIssuer(ISSUER).setExpectedAudience(CLIENT_ID).setVerificationKeyResolver(verificationKeyResolver).build();
        jwtConsumer.processContext(jwtContext);
        JwtClaims jwtClaims = jwtContext.getJwtClaims();
        Assert.assertThat((Object)SUBJECT_VALUE, (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getSubject()));
    }

    @Test
    public void newerWorkaroundOnConsumerBuilder() throws Exception {
        JsonWebKeySet jwks = new JsonWebKeySet(JWKS_JSON);
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRelaxVerificationKeyValidation().setRequireExpirationTime().setEvaluationTime(EVALUATION_TIME).setRequireSubject().setExpectedIssuer(ISSUER).setExpectedAudience(CLIENT_ID).setVerificationKeyResolver(verificationKeyResolver).build();
        JwtClaims claims = jwtConsumer.processToClaims(ID_TOKEN);
        Assert.assertThat((Object)SUBJECT_VALUE, (Matcher)CoreMatchers.equalTo((Object)claims.getSubject()));
    }

    @Test
    public void testAfterTheyMovedTo2048() throws Exception {
        JsonWebKeySet jwks = new JsonWebKeySet("{\n \"keys\": [\n  {\n   \"kty\": \"RSA\",\n   \"alg\": \"RS256\",\n   \"use\": \"sig\",\n   \"kid\": \"e53139984bd36d2c230552441608cc0b5179487a\",\n   \"n\": \"w5F_3au2fyRLapW4K1g0zT6hjF-co8hjHJWniH3aBOKP45xuSRYXnPrpBHkXM6jFkVHs2pCFAOg6o0tl65iRCcf3hOAI6VOIXjMCJqxNap0-j_lJ6Bc6TBKgX3XD96iEI92iaxn_UIVZ_SpPrbPVyRmH0P7B6oDkwFpApviJRtQzv1F6uyh9W_sNnEZrCZDcs5lL5Xa_44-EkhVNz8yGZmAz9d04htNU7xElmXKs8fRdospyv380WeaWFoNJpc-3ojgRus26jvPy8Oc-d4M5yqs9mI72-1G0zbGVFI_PfxZRL8YdFAIZLg44zGzL2M7pFmagJ7Aj46LUb3p_n9V1NQ\",\n   \"e\": \"AQAB\"\n  },\n  {\n   \"kty\": \"RSA\",\n   \"alg\": \"RS256\",\n   \"use\": \"sig\",\n   \"kid\": \"bc8a31927af20860418f6b2231bbfd7ebcc04665\",\n   \"n\": \"ucGr4fFCJYGVUwHYWAtBNclebyhMjALOTUmmAXdMrCIOgT8TxBEn5oXCrszWX7RoC37nFqc1GlMorfII19qMwHdC_iskju3Rh-AuHr29zkDpYIuh4lRW0xJ0Xyo2Iw4PlV9qgqPJLfkmE5V-sr5RxZNe0T1jyYaOGIJ5nF3WbDkgYW4GNHXhv-5tOwWLThJRtH_n6wtYqsBwqAdVX-EVbkyZvYeOzbiNiop7bDM5Td6ER1oCBC4NZjvjdmnOh8-_x6vB449jL5IRAOIIv8NW9dLtQd2DescZOw46HZjWO-zwyhjQeYY87R93yM9yivJdfrjQxydgEs8Ckh03NDATmQ\",\n   \"e\": \"AQAB\"\n  }\n ]\n}\n");
        String jwt = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImJjOGEzMTkyN2FmMjA4NjA0MThmNmIyMjMxYmJmZDdlYmNjMDQ2NjUifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTA5MzM5ODA3NjQ3Nzc3MzkzOTYxIiwiYXpwIjoiMTA3ODQ0OTAyOTY4Ni5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImF1ZCI6IjEwNzg0NDkwMjk2ODYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJpYXQiOjE0MzYzODUzMzYsImV4cCI6MTQzNjM4ODkzNn0.B8jAoYKnsN0Xy62VjBXIrk5B-3ZdbQNt_qzndhlOJXpo4W0C1Q4BvC8YjFc2k6T1qNuehfSrO9xvm-BQGAXRyuQSZPpcQOtP2_LR39oYpnBgDwGKxTdJwAHTIoYTti1R1o-sAkMk-dt4lP45RbUXJEKST0RLKe9RdjNKLtcg62wSvVuLwaqRYyIRWK3Tb8aRA3Eay8uUe8Llk5qJ-1E1pSOscvlYF6EVNkafKBa4jC5utAu5WwvdDoMFz3ZPOzNnhQsjOdxtnAjN3mI9EWNALUsLrdY54-O0JnVJGywKEnwfeDBcUClt_ZBwV-Rl8WMv8TWZRJ8SWywnYi2gaBnaPw";
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1436388930L)).setRequireSubject().setExpectedIssuer(ISSUER).setExpectedAudience("1078449029686.apps.googleusercontent.com").setVerificationKeyResolver(verificationKeyResolver).build();
        JwtClaims claims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)"109339807647777393961", (Matcher)CoreMatchers.equalTo((Object)claims.getSubject()));
    }
}

