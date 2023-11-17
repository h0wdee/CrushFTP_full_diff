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

import java.security.Key;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.JceProviderTestSupport;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.SimpleJwkFilter;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.InvalidJwtSignatureException;
import org.jose4j.jwt.consumer.JweCustomizer;
import org.jose4j.jwt.consumer.JwsCustomizer;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.SimpleJwtConsumerTestHelp;
import org.jose4j.jwt.consumer.Validator;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.keys.ExampleRsaJwksFromJwe;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.jose4j.keys.FakeHsmNonExtractableSecretKeySpec;
import org.jose4j.keys.PbkdfKey;
import org.jose4j.keys.resolvers.DecryptionKeyResolver;
import org.jose4j.keys.resolvers.JwksDecryptionKeyResolver;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;
import org.junit.Assert;
import org.junit.Test;

public class JwtConsumerTest {
    @Test
    public void jwt61ExampleUnsecuredJwt() throws InvalidJwtException, MalformedClaimException {
        String jwt = "eyJhbGciOiJub25lIn0.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.";
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        Assert.assertThat((Object)"joe", (Matcher)CoreMatchers.equalTo((Object)jwtContext.getJwtClaims().getIssuer()));
        Assert.assertThat((Object)NumericDate.fromSeconds(1300819380L), (Matcher)CoreMatchers.equalTo((Object)jwtContext.getJwtClaims().getExpirationTime()));
        Assert.assertTrue((boolean)jwtContext.getJwtClaims().getClaimValue("http://example.com/is_root", Boolean.class));
        JwtConsumer consumer = new JwtConsumerBuilder().setVerificationKey(null).setExpectedIssuer("joe").setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819343L)).setJwsAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS).setDisableRequireSignature().build();
        JwtClaims jcs = consumer.processToClaims(jwt);
        Assert.assertThat((Object)"joe", (Matcher)CoreMatchers.equalTo((Object)jcs.getIssuer()));
        Assert.assertThat((Object)NumericDate.fromSeconds(1300819380L), (Matcher)CoreMatchers.equalTo((Object)jcs.getExpirationTime()));
        Assert.assertTrue((boolean)jcs.getClaimValue("http://example.com/is_root", Boolean.class));
        consumer.processContext(jwtContext);
        Assert.assertNull((Object)jcs.getStringClaimValue("no-such-claim"));
        Assert.assertNull((Object)jcs.getClaimValue("no way jose", Boolean.class));
        Assert.assertFalse((boolean)jcs.hasClaim("nope"));
        Assert.assertTrue((boolean)jcs.getStringListClaimValue("nope").isEmpty());
        Assert.assertTrue((boolean)jcs.hasClaim("http://example.com/is_root"));
        Object objectClaimValue = jcs.getClaimValue("http://example.com/is_root");
        Assert.assertNotNull((Object)objectClaimValue);
        Assert.assertFalse((boolean)jcs.hasClaim("nope"));
        objectClaimValue = jcs.getClaimValue("nope");
        Assert.assertNull((Object)objectClaimValue);
        consumer = new JwtConsumerBuilder().setVerificationKey(null).setExpectedIssuer("joe").setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819343L)).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
        consumer = new JwtConsumerBuilder().setVerificationKey(null).setExpectedIssuer("joe").setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819343L)).setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "none", "PS256")).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
        consumer = new JwtConsumerBuilder().setVerificationKey(ExampleRsaJwksFromJwe.APPENDIX_A_1.getKey()).setExpectedIssuer("joe").setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819343L)).setJwsAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
        consumer = new JwtConsumerBuilder().setExpectedIssuer("joe").setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819343L)).setJwsAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
    }

    @Test
    public void jwtA1ExampleEncryptedJWT() throws InvalidJwtException, MalformedClaimException {
        String jwt = "eyJhbGciOiJSU0ExXzUiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.QR1Owv2ug2WyPBnbQrRARTeEk9kDO2w8qDcjiHnSJflSdv1iNqhWXaKH4MqAkQtMoNfABIPJaZm0HaA415sv3aeuBWnD8J-Ui7Ah6cWafs3ZwwFKDFUUsWHSK-IPKxLGTkND09XyjORj_CHAgOPJ-Sd8ONQRnJvWn_hXV1BNMHzUjPyYwEsRhDhzjAD26imasOTsgruobpYGoQcXUwFDn7moXPRfDE8-NoQX7N7ZYMmpUDkR-Cx9obNGwJQ3nM52YCitxoQVPzjbl7WBuB7AohdBoZOdZ24WlN1lVIeh8v1K4krB8xgKvRU8kgFrEn_a1rZgN5TiysnmzTROF869lQ.AxY8DCtDaGlsbGljb3RoZQ.MKOle7UQrG6nSxTLX6Mqwt0orbHvAKeWnDYvpIAeZ72deHxz3roJDXQyhxx0wKaMHDjUEOKIwrtkHthpqEanSBNYHZgmNOV7sln1Eu9g3J8.fiK51VwhsxJ-siBMR-YFiA";
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().setDecryptionKey(ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey()).build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        JwtConsumer c = new JwtConsumerBuilder().setExpectedIssuer("joe").setEvaluationTime(NumericDate.fromSeconds(1300819300L)).setDecryptionKey(ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey()).setDisableRequireSignature().build();
        c.processContext(jwtContext);
        JwtContext context = c.process(jwt);
        JwtClaims jcs = context.getJwtClaims();
        Assert.assertTrue((boolean)jcs.getClaimValue("http://example.com/is_root", Boolean.class));
        String expectedPayload = "{\"iss\":\"joe\",\r\n \"exp\":1300819380,\r\n \"http://example.com/is_root\":true}";
        Assert.assertThat((Object)jcs.getRawJson(), (Matcher)CoreMatchers.equalTo((Object)expectedPayload));
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)context.getJoseObjects().size()));
        Assert.assertThat((Object)context.getJwt(), (Matcher)CoreMatchers.equalTo((Object)jwt));
    }

    @Test
    public void jwtA2ExampleNestedJWT() throws InvalidJwtException, MalformedClaimException {
        String jwt = "eyJhbGciOiJSU0ExXzUiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiY3R5IjoiSldUIn0.g_hEwksO1Ax8Qn7HoN-BVeBoa8FXe0kpyk_XdcSmxvcM5_P296JXXtoHISr_DD_MqewaQSH4dZOQHoUgKLeFly-9RI11TG-_Ge1bZFazBPwKC5lJ6OLANLMd0QSL4fYEb9ERe-epKYE3xb2jfY1AltHqBO-PM6j23Guj2yDKnFv6WO72tteVzm_2n17SBFvhDuR9a2nHTE67pe0XGBUS_TK7ecA-iVq5COeVdJR4U4VZGGlxRGPLRHvolVLEHx6DYyLpw30Ay9R6d68YCLi9FYTq3hIXPK_-dmPlOUlKvPr1GgJzRoeC9G5qCvdcHWsqJGTO_z3Wfo5zsqwkxruxwA.UmVkbW9uZCBXQSA5ODA1Mg.VwHERHPvCNcHHpTjkoigx3_ExK0Qc71RMEParpatm0X_qpg-w8kozSjfNIPPXiTBBLXR65CIPkFqz4l1Ae9w_uowKiwyi9acgVztAi-pSL8GQSXnaamh9kX1mdh3M_TT-FZGQFQsFhu0Z72gJKGdfGE-OE7hS1zuBD5oEUfk0Dmb0VzWEzpxxiSSBbBAzP10l56pPfAtrjEYw-7ygeMkwBl6Z_mLS6w6xUgKlvW6ULmkV-uLC4FUiyKECK4e3WZYKw1bpgIqGYsw2v_grHjszJZ-_I5uM-9RA8ycX9KqPRp9gc6pXmoU_-27ATs9XCvrZXUtK2902AUzqpeEUJYjWWxSNsS-r1TJ1I-FMJ4XyAiGrfmo9hQPcNBYxPz3GQb28Y5CLSQfNgKSGt0A4isp1hBUXBHAndgtcslt7ZoQJaKe_nNJgNliWtWpJ_ebuOpEl8jdhehdccnRMIwAmU1n7SPkmhIl1HlSOpvcvDfhUN5wuqU955vOBvfkBOh5A11UzBuo2WlgZ6hYi9-e3w29bR0C2-pp3jbqxEDw3iWaf2dc5b-LnR0FEYXvI_tYk5rd_J9N0mg0tQ6RbpxNEMNoA9QWk5lgdPvbh9BaO195abQ.AVO9iT5AV4CzvDJCdhSFlQ";
        PrivateKey decryptionKey = ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey();
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().setDecryptionKey(decryptionKey).build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        RSAPublicKey verificationKey = ExampleRsaKeyFromJws.PUBLIC_KEY;
        JwtConsumerBuilder builder = new JwtConsumerBuilder().setDecryptionKey(decryptionKey).setEnableRequireEncryption().setVerificationKey(verificationKey).setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819380L)).setAllowedClockSkewInSeconds(30).setExpectedIssuer("joe");
        JwtConsumer jwtConsumer = builder.build();
        jwtConsumer.processContext(jwtContext);
        JwtContext jwtInfo = jwtConsumer.process(jwt);
        JwtContext[] jwtContextArray = new JwtContext[]{jwtContext, jwtInfo};
        int n = jwtContextArray.length;
        int n2 = 0;
        while (n2 < n) {
            JwtContext ctx = jwtContextArray[n2];
            Assert.assertThat((Object)2, (Matcher)CoreMatchers.equalTo((Object)ctx.getJoseObjects().size()));
            Assert.assertTrue((boolean)(ctx.getJoseObjects().get(0) instanceof JsonWebSignature));
            Assert.assertTrue((boolean)(ctx.getJoseObjects().get(1) instanceof JsonWebEncryption));
            Assert.assertThat((Object)ctx.getJwt(), (Matcher)CoreMatchers.equalTo((Object)jwt));
            JwtClaims jcs = ctx.getJwtClaims();
            Assert.assertThat((Object)"joe", (Matcher)CoreMatchers.equalTo((Object)jcs.getIssuer()));
            Assert.assertThat((Object)NumericDate.fromSeconds(1300819380L), (Matcher)CoreMatchers.equalTo((Object)jcs.getExpirationTime()));
            Assert.assertTrue((boolean)jcs.getClaimValue("http://example.com/is_root", Boolean.class));
            ++n2;
        }
        builder = new JwtConsumerBuilder().setDecryptionKey(null).setEnableRequireEncryption().setVerificationKey(verificationKey).setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819380L)).setAllowedClockSkewInSeconds(30).setExpectedIssuer("joe");
        jwtConsumer = builder.build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, null, jwtConsumer);
        jwtConsumer.processContext(jwtContext);
        builder = new JwtConsumerBuilder().setDecryptionKey(decryptionKey).setEnableRequireEncryption().setVerificationKey(null).setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819380L)).setAllowedClockSkewInSeconds(30).setExpectedIssuer("joe");
        jwtConsumer = builder.build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, jwtConsumer);
        builder = new JwtConsumerBuilder().setDecryptionKey(decryptionKey).setEnableRequireEncryption().setVerificationKey(ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey()).setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819380L)).setAllowedClockSkewInSeconds(30).setExpectedIssuer("joe");
        jwtConsumer = builder.build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, jwtConsumer);
        builder = new JwtConsumerBuilder().setDecryptionKey(ExampleRsaKeyFromJws.PRIVATE_KEY).setEnableRequireEncryption().setVerificationKey(verificationKey).setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1300819380L)).setAllowedClockSkewInSeconds(30).setExpectedIssuer("joe");
        jwtConsumer = builder.build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, jwtConsumer);
    }

    @Test
    public void jwtSec31ExampleJWT() throws Exception {
        String jwt = "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        Assert.assertTrue((boolean)jwtContext.getJwtClaims().getClaimValue("http://example.com/is_root", Boolean.class));
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)jwtContext.getJoseObjects().size()));
        String jwk = "{\"kty\":\"oct\",\"k\":\"AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow\"}";
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(jwk);
        JwksVerificationKeyResolver resolver = new JwksVerificationKeyResolver(Collections.singletonList(jsonWebKey));
        JwtConsumer consumer = new JwtConsumerBuilder().setVerificationKeyResolver(resolver).setEvaluationTime(NumericDate.fromSeconds(1300819372L)).setExpectedIssuer("joe").setRequireExpirationTime().build();
        JwtContext context = consumer.process(jwt);
        Assert.assertTrue((boolean)context.getJwtClaims().getClaimValue("http://example.com/is_root", Boolean.class));
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)context.getJoseObjects().size()));
        consumer.processContext(jwtContext);
        consumer = new JwtConsumerBuilder().setEnableRequireEncryption().setVerificationKey(jsonWebKey.getKey()).setEvaluationTime(NumericDate.fromSeconds(1300819372L)).setExpectedIssuer("joe").setRequireExpirationTime().build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
    }

    @Test
    public void skipSignatureVerification() throws Exception {
        String jwt = "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        JwtConsumer consumer = new JwtConsumerBuilder().setSkipSignatureVerification().setEvaluationTime(NumericDate.fromSeconds(1300819372L)).setExpectedIssuer("joe").setRequireExpirationTime().build();
        JwtContext context = consumer.process(jwt);
        Assert.assertTrue((boolean)context.getJwtClaims().getClaimValue("http://example.com/is_root", Boolean.class));
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)context.getJoseObjects().size()));
    }

    @Test(expected=InvalidJwtSignatureException.class)
    public void jwtBadSig() throws Exception {
        String jwt = "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLAogImV4cCI6MTkwMDgxOTM4MCwKICJodHRwOi8vZXhhbXBsZS5jb20vaXNfcm9vdCI6dHJ1ZX0.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String jwk = "{\"kty\":\"oct\",\"k\":\"AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow\"}";
        JwtConsumer consumer = new JwtConsumerBuilder().setVerificationKey(JsonWebKey.Factory.newJwk(jwk).getKey()).setEvaluationTime(NumericDate.fromSeconds(1900000380L)).setExpectedIssuer("joe").setRequireExpirationTime().build();
        consumer.process(jwt);
    }

    @Test
    public void algConstraints() throws Exception {
        String jwt = "eyJ6aXAiOiJERUYiLCJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiY3R5IjoiSldUIn0.DDyrirrztC88OaDtTkkNgNIyZqQd4gjWrab9KkiBnyOULjWZWt-IAg.Obun_t7l3FYqNUqyW46syg.ChlzoLTN1ovJP9PLHlirc-_yvP4ya_5gdhDSKiZnifS9MjCbeMYebkOCxSHexs09PBbPv30JwtIyM7caqkSNggA8HT_ub1moMpx0uOFhTE9dpdY4Wb4Ym6mqtIQhdwLymDVCI6vRn-NH88vdLluGSYYLhelgcL05qeWJQKzV3mxopgM-Q7N7LycXrodqTdvM.ay9pwehz96tJgRKvSwASDg";
        JsonWebKey wrapKey = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"sUMs42PKNsKn9jeGJ2szKA\"}");
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setDecryptionKey(wrapKey.getKey()).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtContext.getJwtClaims().getStringClaimValue("message")));
        JsonWebKey macKey = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"j-QRollN4PYjebWYcTl32YOGWfdpXi_YYHu03Ifp8K4\"}");
        JwtConsumer consumer = new JwtConsumerBuilder().setDecryptionKey(wrapKey.getKey()).setVerificationKey(macKey.getKey()).setEvaluationTime(NumericDate.fromSeconds(1419982016L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        JwtClaims jwtClaims = consumer.processToClaims(jwt);
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
        consumer.processContext(jwtContext);
        consumer = new JwtConsumerBuilder().setDecryptionKey(wrapKey.getKey()).setVerificationKey(macKey.getKey()).setEvaluationTime(NumericDate.fromSeconds(1419982016L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, "HS256")).setJweAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, "A128KW")).setJweContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, "A128CBC-HS256")).build();
        jwtClaims = consumer.processToClaims(jwt);
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
        consumer.processContext(jwtContext);
        consumer = new JwtConsumerBuilder().setDecryptionKey(wrapKey.getKey()).setVerificationKey(macKey.getKey()).setEvaluationTime(NumericDate.fromSeconds(1419982016L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "HS256")).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
        consumer = new JwtConsumerBuilder().setDecryptionKey(wrapKey.getKey()).setVerificationKey(macKey.getKey()).setEvaluationTime(NumericDate.fromSeconds(1419982016L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().setJweAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "A128KW")).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
        consumer = new JwtConsumerBuilder().setDecryptionKey(wrapKey.getKey()).setVerificationKey(macKey.getKey()).setEvaluationTime(NumericDate.fromSeconds(1419982016L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().setJweContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "A128CBC-HS256")).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
        consumer = new JwtConsumerBuilder().setDecryptionKey(wrapKey.getKey()).setVerificationKey(JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"___RollN4PYjebWYcTl32YOGWfdpXi_YYHu03Ifp8K4\"}").getKey()).setSkipAllValidators().build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
    }

    @Test
    public void customValidatorTest() throws Exception {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzYW1lIiwiYXVkIjoic2FtZSIsImV4cCI6MTQyMDA0NjA2MH0.O1w_nkfQMZvEEvJ0Pach0gPmJUMW8o4aFlA1f2c8m-I";
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"IWlxz1h43wKzyigIXNn-dTRBu89M9L8wmJK4zZmUXrQ\"}");
        JwtConsumer consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1420046040L)).setExpectedAudience("same", "different").setExpectedIssuer("same").setRequireExpirationTime().setVerificationKey(jsonWebKey.getKey()).build();
        JwtContext process = consumer.process(jwt);
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)process.getJoseObjects().size()));
        consumer.processContext(jwtContext);
        consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1420046040L)).setExpectedAudience("same", "different").setExpectedIssuer("same").setRequireExpirationTime().setVerificationKey(jsonWebKey.getKey()).registerValidator(new Validator(){

            @Override
            public String validate(JwtContext jwtContext) throws MalformedClaimException {
                JwtClaims jcs = jwtContext.getJwtClaims();
                String audience = jcs.getAudience().iterator().next();
                String issuer = jcs.getIssuer();
                if (issuer.equals(audience)) {
                    return "You can go blind issuing tokens to yourself...";
                }
                return null;
            }
        }).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
    }

    @Test
    public void wrappedNpeFromCustomValidatorTest() throws Exception {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzYW1lIiwiZXhwIjoxNDIwMDQ2ODE0fQ.LUViXhiMJRZa5veg6ayZCDQaIc0GfVDJDx-878WbFzg";
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"Ek1bHgP9uYyEtB5-V6oAzT_wB4mUnvCpirPqO4MyFwE\"}");
        JwtConsumer consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1420046767L)).setExpectedAudience(false, "other", "different").setExpectedIssuer("same").setRequireExpirationTime().setVerificationKey(jsonWebKey.getKey()).build();
        JwtContext process = consumer.process(jwt);
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)process.getJoseObjects().size()));
        consumer.processContext(jwtContext);
        consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1420046768L)).setExpectedAudience(false, "other", "different").setExpectedIssuer("same").setRequireExpirationTime().setVerificationKey(jsonWebKey.getKey()).registerValidator(new Validator(){

            @Override
            public String validate(JwtContext jwtContext) throws MalformedClaimException {
                try {
                    JwtClaims jcs = jwtContext.getJwtClaims();
                    List<String> audience = jcs.getAudience();
                    Iterator<String> iterator = audience.iterator();
                    iterator.next();
                    return null;
                }
                catch (Exception e) {
                    throw new RuntimeException("Something bad happened.", e);
                }
            }
        }).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
    }

    @Test
    public void someExpectedAndUnexpectedEx() throws Exception {
        String jwt = "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        JwtConsumer consumer = new JwtConsumerBuilder().setVerificationKeyResolver(new VerificationKeyResolver(){

            @Override
            public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
                throw new UnresolvableKeyException("Can't do it!");
            }
        }).setEvaluationTime(NumericDate.fromSeconds(1300819372L)).setExpectedIssuer("joe").setRequireExpirationTime().build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
        consumer = new JwtConsumerBuilder().setVerificationKeyResolver(new VerificationKeyResolver(){

            @Override
            public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
                throw new IllegalArgumentException("Stuff happens...");
            }
        }).setEvaluationTime(NumericDate.fromSeconds(1300819372L)).setExpectedIssuer("joe").setRequireExpirationTime().build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
    }

    @Test
    public void missingCtyInNested() throws Exception {
        String jwt = "eyJ6aXAiOiJERUYiLCJhbGciOiJFQ0RILUVTIiwiZW5jIjoiQTEyOENCQy1IUzI1NiIsImVwayI6eyJrdHkiOiJFQyIsIngiOiIwRGk0VTBZQ0R2NHAtS2hETUZwUThvY0FsZzA2SEwzSHR6UldRbzlDLWV3IiwieSI6IjBfVFJjR1Y3Qy05d0xseFJZSExJOFlKTXlET2hWNW5YeHVPMGdRVmVxd0EiLCJjcnYiOiJQLTI1NiJ9fQ..xw5H8Kztd_sqzbXjt4GKUg.YNa163HLj7MwlvjzGihbOHnJ2PC3NOTnnvVOanuk1O9XFJ97pbbHHQzEeEwG6jfvDgdmlrLjcIJkSu1U8qRby7Xr4gzP6CkaDPbKwvLveETZSNdmZh37XKfnQ4LvKgiko6OQzyLYG1gc97kUOeikXTYVaYaeV1838Bi4q3DsIG-j4ZESg0-ePQesw56A80AEE3j6wXwZ4vqugPP9_ogZzkPFcHf1lt3-A4amNMjDbV8.u-JJCoakXI55BG2rz_kBlg";
        PublicJsonWebKey sigKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"loF6m9WAW_GKrhoh48ctg_d78fbIsmUb02XDOwJj59c\",\"y\":\"kDCHDkCbWjeX8DjD9feQKcndJyerdsLJ4VZ5YSTWCoU\",\"crv\":\"P-256\",\"d\":\"6D1C9gJsT9KXNtTNyqgpdyQuIrK-qzo0_QJOVe9DqJg\"}");
        PublicJsonWebKey encKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"PNbMydlpYRBFTYn_XDFvvRAFqE4e0EJmK6-zULTVERs\",\"y\":\"dyO9wGVgKS3gtP5bx0PE8__MOV_HLSpiwK-mP1RGZgk\",\"crv\":\"P-256\",\"d\":\"FIs8wVojHBdl7vkiZVnLBPw5S9lbn4JF2WWY1OTupic\"}");
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().setEnableLiberalContentTypeHandling().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        JwtConsumer consumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420219088L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, consumer);
        consumer = new JwtConsumerBuilder().setEnableLiberalContentTypeHandling().setDecryptionKey(encKey.getPrivateKey()).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420219088L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        JwtContext ctx = consumer.process(jwt);
        consumer.processContext(jwtContext);
        JwtContext[] jwtContextArray = new JwtContext[]{ctx, jwtContext};
        int n = jwtContextArray.length;
        int n2 = 0;
        while (n2 < n) {
            JwtContext context = jwtContextArray[n2];
            JwtClaims jwtClaims = context.getJwtClaims();
            Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
            List<JsonWebStructure> joseObjects = context.getJoseObjects();
            Assert.assertThat((Object)2, (Matcher)CoreMatchers.equalTo((Object)joseObjects.size()));
            Assert.assertTrue((boolean)(joseObjects.get(0) instanceof JsonWebSignature));
            Assert.assertTrue((boolean)(joseObjects.get(1) instanceof JsonWebEncryption));
            ++n2;
        }
    }

    @Test
    public void missingCtyInNestedViaNimbusExample() throws Exception {
        String jwt = "eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.IAseIHBLnv7hFKz_V3-o-Of3Mf2DIGzFnSh_8sLZgujPaNIG8NlZmA.fwbuvibqYUlDzTXTtsB6yw.5T70ZVMqOTl4q_tYegL0bgJpT2wTUlSvnJ2QAB8KfpNO_J3StiK8oHvSmVOPOrCQJai_XffZGUpmAO2fnGnUajKmQpxm_iaJUZtzexwqeNlVzAr-swLUZDmW0lh3NgDBEAgY4khN7v1L_etToKuuEI6P-UGsg34BqaNuZEkj7ylsY1McZg73t5x9C4Q9dsBbsPLFPPUxxvA2abJhAq1Hew.D1hDq8pD6nQ42yvez-yjlQ\n";
        AesKey decryptionKey = new AesKey(new byte[16]);
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setDecryptionKey(decryptionKey).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().setEnableLiberalContentTypeHandling().build();
        JwtContext jwtContext = firstPassConsumer.process("eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.IAseIHBLnv7hFKz_V3-o-Of3Mf2DIGzFnSh_8sLZgujPaNIG8NlZmA.fwbuvibqYUlDzTXTtsB6yw.5T70ZVMqOTl4q_tYegL0bgJpT2wTUlSvnJ2QAB8KfpNO_J3StiK8oHvSmVOPOrCQJai_XffZGUpmAO2fnGnUajKmQpxm_iaJUZtzexwqeNlVzAr-swLUZDmW0lh3NgDBEAgY4khN7v1L_etToKuuEI6P-UGsg34BqaNuZEkj7ylsY1McZg73t5x9C4Q9dsBbsPLFPPUxxvA2abJhAq1Hew.D1hDq8pD6nQ42yvez-yjlQ\n");
        JwtConsumer consumer = new JwtConsumerBuilder().setEnableLiberalContentTypeHandling().setDecryptionKey(decryptionKey).setVerificationKey(new AesKey(new byte[32])).setEvaluationTime(NumericDate.fromSeconds(1420467806L)).setExpectedIssuer("https://c2id.com").setRequireIssuedAt().build();
        JwtContext ctx = consumer.process("eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.IAseIHBLnv7hFKz_V3-o-Of3Mf2DIGzFnSh_8sLZgujPaNIG8NlZmA.fwbuvibqYUlDzTXTtsB6yw.5T70ZVMqOTl4q_tYegL0bgJpT2wTUlSvnJ2QAB8KfpNO_J3StiK8oHvSmVOPOrCQJai_XffZGUpmAO2fnGnUajKmQpxm_iaJUZtzexwqeNlVzAr-swLUZDmW0lh3NgDBEAgY4khN7v1L_etToKuuEI6P-UGsg34BqaNuZEkj7ylsY1McZg73t5x9C4Q9dsBbsPLFPPUxxvA2abJhAq1Hew.D1hDq8pD6nQ42yvez-yjlQ\n");
        JwtContext[] jwtContextArray = new JwtContext[]{ctx, jwtContext};
        int n = jwtContextArray.length;
        int n2 = 0;
        while (n2 < n) {
            JwtContext context = jwtContextArray[n2];
            JwtClaims jwtClaims = context.getJwtClaims();
            Assert.assertThat((Object)"alice", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getSubject()));
            List<JsonWebStructure> joseObjects = context.getJoseObjects();
            Assert.assertThat((Object)2, (Matcher)CoreMatchers.equalTo((Object)joseObjects.size()));
            Assert.assertTrue((boolean)(joseObjects.get(0) instanceof JsonWebSignature));
            Assert.assertTrue((boolean)(joseObjects.get(1) instanceof JsonWebEncryption));
            ++n2;
        }
    }

    @Test
    public void ctyValueVariationsInNested() throws Exception {
        PublicJsonWebKey sigKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"HVDkXtG_j_JQUm_mNaRPSbsEhr6gdK0a6H4EURypTU0\",\"y\":\"NxdYFS2hl1w8VKf5UTpGXh2YR7KQ8gSBIHu64W0mK8M\",\"crv\":\"P-256\",\"d\":\"ToqTlgJLhI7AQYNLesI2i-08JuaYm2wxTCDiF-VxY4A\"}");
        PublicJsonWebKey encKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"7kaETHB4U9pCdsErbjw11HGv8xcQUmFy3NMuBa_J7Os\",\"y\":\"FZK-vSMpKk9gLWC5wdFjG1W_C7vgJtdm1YfNPZevmCw\",\"crv\":\"P-256\",\"d\":\"spOxtF0qiKrrCTaUs_G04RISjCx7HEgje_I7aihXVMY\"}");
        String jwt = "eyJ6aXAiOiJERUYiLCJhbGciOiJFQ0RILUVTIiwiZW5jIjoiQTEyOENCQy1IUzI1NiIsImN0eSI6ImFwcGxpY2F0aW9uL2p3dCIsImVwayI6eyJrdHkiOiJFQyIsIngiOiJCOUhPbG82UV9LV0NiQjZLbk1RMDFfaHcyRXdaQWNEMmNucEdYYVl5WFBBIiwieSI6InJYS2s3VzM4UXhVOHl4YWZZc3NsUjFWU2JLbDI5T0FNSWxROFBCWXVZcUEiLCJjcnYiOiJQLTI1NiJ9fQ..LcIG9_bnPb43aaps32H6yQ.rsV7ItJWWfNafDJmeLHluKhiwmsU0Mlwut2jwD6y96KpjD-hz_5zBxpXtj6mk8yGZwg2L26XLo8npt_82bhKnMYqlKSRM-3ge2Deg5WPmBCx6Fj0NyCMnoR8oJTn-oxh0OHZICK_85Xz3GptopeA3Hj8ESdsJEI6D4WbXQ7HfGeg8ID9uvTaL8NGOHT4BGY0bB-6nl3qNIY5ULpg-a4a1ou5k9HnM6SRSpVRwpBBUsk.1vqvwv9XAzsQfvragyMXZQ";
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().setEnableLiberalContentTypeHandling().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtContext.getJwtClaims().getStringClaimValue("message")));
        JwtConsumer consumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420219088L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        JwtContext context = consumer.process(jwt);
        JwtClaims jwtClaims = context.getJwtClaims();
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
        consumer.processContext(jwtContext);
        jwt = "eyJ6aXAiOiJERUYiLCJhbGciOiJFQ0RILUVTIiwiZW5jIjoiQTEyOENCQy1IUzI1NiIsImN0eSI6ImFwcGxpY2F0aW9uL0pXVCIsImVwayI6eyJrdHkiOiJFQyIsIngiOiJxelBlRUl0ZXJmQ0dhTFBpbDU3UmRudERHQVdwdVlBRGtVLUJubkkyTXowIiwieSI6ImNmWUxlc1dneGlfVndCdzdvSzNPT3dabGNrbVRCVmMzcEdnMTNRZ3V5WjQiLCJjcnYiOiJQLTI1NiJ9fQ..ftNMf4CqUSCq8p3L1Y7K1A.Z9K1YIJmSY9du5LUuSs0szCj1PUzq0ZnsEppT8yVPdGVDkDi0elEcsM8dCq8CvYrXG8OFuyp0s8dd2u_fIw4RjMc-aVMBT4ikWDmqb4CA17nC2Hxm6dZFPy3Xx3GnqjiGUIB2JiMOxj6mBZtTSvkKAUvs3Rh4G-87v2hJFpqdLSySqd-rQXL7Dhqxl0Cbu9nZFcYEIk58lpC0H2TN9aP5GtuQYa3BlNuEoEDzIcLhc4.N6VFQ0_UgNqyBsPLyE6MQQ";
        firstPassConsumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().setEnableLiberalContentTypeHandling().build();
        jwtContext = firstPassConsumer.process(jwt);
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtContext.getJwtClaims().getStringClaimValue("message")));
        consumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420219095L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        context = consumer.process(jwt);
        jwtClaims = context.getJwtClaims();
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
        consumer.processContext(jwtContext);
        jwt = "eyJ6aXAiOiJERUYiLCJhbGciOiJFQ0RILUVTIiwiZW5jIjoiQTEyOENCQy1IUzI1NiIsImN0eSI6Imp3dCIsImVwayI6eyJrdHkiOiJFQyIsIngiOiJoTm5zTlRXZWN3TEVRUGVRMlFjZ05WSDJLX0dzTkFUZXNVaENhY2x2OVAwIiwieSI6ImI2V1lSR1V5Z1NBUGo5a0lFYktYTm5ZaDhEbmNrRXB2NDFYbUVnanA4VE0iLCJjcnYiOiJQLTI1NiJ9fQ..VGTURmPYERdJ7q9_5wlENA.91m_JN65XNlp9WsFHaHihhGB7soKNUdeBNpmODVcIiinhPClH00-GTMwfT08VmXEU2djW3Aw_eBAoU7rI_M0ovYbbmAy7UnVRUyCTbkGsQpv7OxYIznemMVMraFuHNmTAF_MU7oM4gPkqKzwuBa0uwd4JhN00bq-jEcLifMPgMvyGvfJ19SXAyrIVA4Otjuii347V5u1GwlB5VBqMiqtBnbMMzR1Fe3X-4-sEgT9BrM.4T3uLGa4Bm5_r-ZNKPzEWg";
        firstPassConsumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().setEnableLiberalContentTypeHandling().build();
        jwtContext = firstPassConsumer.process(jwt);
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtContext.getJwtClaims().getStringClaimValue("message")));
        consumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420219099L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        context = consumer.process(jwt);
        jwtClaims = context.getJwtClaims();
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
        consumer.processContext(jwtContext);
        jwt = "eyJ6aXAiOiJERUYiLCJhbGciOiJFQ0RILUVTIiwiZW5jIjoiQTEyOENCQy1IUzI1NiIsImN0eSI6ImpXdCIsImVwayI6eyJrdHkiOiJFQyIsIngiOiJmYTlJVEh6cEROSG1uV2NDSDVvWGtFYjJ1SncwTXNOU2stQjdFb091WUEwIiwieSI6IkZ1U0RaVXdmb1EtQXB6dEFQRUc1dk40QmZRR2sxWnRMT0FzM1o0a19obmciLCJjcnYiOiJQLTI1NiJ9fQ..FmuORwLWIoNBbRh0XcBzJQ.pSr58DMuRstF3A6xj24yM4KvNgWxtb_QDKuldesTCD-R00BNFwIVx4F51VL5DwR54ITgBZBKdAT4pN6eM-td5VrWBCnSWxFjNrBoDnnRkDfFgq8OjOBaR7k_4zUk41bBikDZ0JOQDWuiaODYBk7PWq0mgotvLPbJ9oc7zfp6lbHqaYXjbzfuD56W_kDYO8zSjiZUGLcYgJDYnO3F8K-QhP02v-0OEpAGrm5SKKV3Txk.Ecojfru8KbkqIw4QvYS3qA";
        firstPassConsumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().setEnableLiberalContentTypeHandling().build();
        jwtContext = firstPassConsumer.process(jwt);
        consumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420220122L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        context = consumer.process(jwt);
        jwtClaims = context.getJwtClaims();
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
        consumer.processContext(jwtContext);
    }

    @Test
    public void ctyRoundTrip() throws JoseException, InvalidJwtException, MalformedClaimException {
        JsonWebKeySet jwks = new JsonWebKeySet("{\"keys\":[{\"kty\":\"oct\",\"kid\":\"hk1\",\"alg\":\"HS256\",\"k\":\"RYCCH0Qai_7Clk_GnfBElTFIa5VJP3pJUDd8g5H0PKs\"},{\"kty\":\"oct\",\"kid\":\"ek1\",\"alg\":\"A128KW\",\"k\":\"Qi38jqNMENlgKaVRbhKWnQ\"}]}");
        SimpleJwkFilter filter = new SimpleJwkFilter();
        filter.setKid("hk1", false);
        JsonWebKey hmacKey = filter.filter(jwks.getJsonWebKeys()).iterator().next();
        filter = new SimpleJwkFilter();
        filter.setKid("ek1", false);
        JsonWebKey encKey = filter.filter(jwks.getJsonWebKeys()).iterator().next();
        JwtClaims claims = new JwtClaims();
        claims.setSubject("subject");
        claims.setAudience("audience");
        claims.setIssuer("issuer");
        claims.setExpirationTimeMinutesInTheFuture(10.0f);
        claims.setNotBeforeMinutesInThePast(5.0f);
        claims.setGeneratedJwtId();
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("HS256");
        jws.setPayload(claims.toJson());
        jws.setKey(hmacKey.getKey());
        jws.setKeyIdHeaderValue(hmacKey.getKeyId());
        String innerJwt = jws.getCompactSerialization();
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setAlgorithmHeaderValue("A128KW");
        jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        jwe.setKey(encKey.getKey());
        jwe.setKeyIdHeaderValue(encKey.getKeyId());
        jwe.setContentTypeHeaderValue("JWT");
        jwe.setPayload(innerJwt);
        String jwt = jwe.getCompactSerialization();
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer("issuer").setExpectedAudience("audience").setRequireSubject().setRequireExpirationTime().setDecryptionKey(encKey.getKey()).setVerificationKey(hmacKey.getKey()).build();
        JwtContext jwtContext = jwtConsumer.process(jwt);
        Assert.assertThat((Object)"subject", (Matcher)CoreMatchers.equalTo((Object)jwtContext.getJwtClaims().getSubject()));
        List<JsonWebStructure> joseObjects = jwtContext.getJoseObjects();
        JsonWebStructure outerJsonWebObject = joseObjects.get(joseObjects.size() - 1);
        Assert.assertTrue((boolean)(outerJsonWebObject instanceof JsonWebEncryption));
        Assert.assertThat((Object)"JWT", (Matcher)CoreMatchers.equalTo((Object)outerJsonWebObject.getContentTypeHeaderValue()));
        Assert.assertThat((Object)"JWT", (Matcher)CoreMatchers.equalTo((Object)outerJsonWebObject.getHeader("cty")));
        Assert.assertThat((Object)"JWT", (Matcher)CoreMatchers.equalTo((Object)outerJsonWebObject.getHeaders().getStringHeaderValue("cty")));
        JsonWebStructure innerJsonWebObject = joseObjects.get(0);
        Assert.assertTrue((boolean)(innerJsonWebObject instanceof JsonWebSignature));
    }

    @Test
    public void nestedBackwards() throws Exception {
        String jwt = "eyJjdHkiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.ZXlKNmFYQWlPaUpFUlVZaUxDSmhiR2NpT2lKRlEwUklMVVZUSWl3aVpXNWpJam9pUVRFeU9FTkNReTFJVXpJMU5pSXNJbVZ3YXlJNmV5SnJkSGtpT2lKRlF5SXNJbmdpT2lKYVIwczNWbkZOUzNKV1VGcEphRXc1UkRsT05tTnpNV0ZhYlU5MVpqbHlUWGhtUm1kRFVURjFaREJuSWl3aWVTSTZJbTAyZW01VlQybEtjMnMwTlRaRVVWb3RjVTEzZEVKblpqQkRNVXh4VDB0dk5HYzNjakpGUTBkQllUZ2lMQ0pqY25ZaU9pSlFMVEkxTmlKOWZRLi4xSndRWThoVFJVczdUMFNpOWM1VE9RLkFOdUpNcFowTU1KLTBrbVdvVHhvRDlxLTA1YUxrMkpvRzMxLXdVZ01ZakdaaWZiWG96SDEzZGRuaXZpWXNtenhMcFdVNU1lQnptN3J3TExTeUlCdjB3LmVEb1lFTEhFWXBnMHFpRzBaeHUtWEE.NctFu0mNSArPnMXakIMQKagWyU4v7733dNhDNK3KwiFP2MahpfaH0LA7x0knRk0sjASRxDuEIW6UZGfPTFOjkw";
        PublicJsonWebKey sigKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"HVDkXtG_j_JQUm_mNaRPSbsEhr6gdK0a6H4EURypTU0\",\"y\":\"NxdYFS2hl1w8VKf5UTpGXh2YR7KQ8gSBIHu64W0mK8M\",\"crv\":\"P-256\",\"d\":\"ToqTlgJLhI7AQYNLesI2i-08JuaYm2wxTCDiF-VxY4A\"}");
        PublicJsonWebKey encKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"7kaETHB4U9pCdsErbjw11HGv8xcQUmFy3NMuBa_J7Os\",\"y\":\"FZK-vSMpKk9gLWC5wdFjG1W_C7vgJtdm1YfNPZevmCw\",\"crv\":\"P-256\",\"d\":\"spOxtF0qiKrrCTaUs_G04RISjCx7HEgje_I7aihXVMY\"}");
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        JwtConsumer consumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420226222L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        JwtContext ctx = consumer.process(jwt);
        consumer.processContext(jwtContext);
        JwtContext[] jwtContextArray = new JwtContext[]{ctx, jwtContext};
        int n = jwtContextArray.length;
        int n2 = 0;
        while (n2 < n) {
            JwtContext context = jwtContextArray[n2];
            JwtClaims jwtClaims = context.getJwtClaims();
            Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
            List<JsonWebStructure> joseObjects = context.getJoseObjects();
            Assert.assertThat((Object)2, (Matcher)CoreMatchers.equalTo((Object)joseObjects.size()));
            Assert.assertTrue((boolean)(joseObjects.get(0) instanceof JsonWebEncryption));
            Assert.assertTrue((boolean)(joseObjects.get(1) instanceof JsonWebSignature));
            ++n2;
        }
    }

    @Test
    public void tripleNesting() throws Exception {
        String jwt = "eyJhbGciOiJQQkVTMi1IUzI1NitBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiY3R5Ijoiand0IiwicDJjIjo4MTkyLCJwMnMiOiJiWE13N0F3YUtITWZ4cWRNIn0.5Qo4mtR0E6AnTsiq-hcH9_RJoZwmWiMl0se_riEr1sdz2IXA-vCkrw.iA7lBH3Tzs4uIJVtekZEfg.jkdleffS8GIen_xt_g3QHAc0cat6UBAODpv6WLJ_ytMw-h0dtV0F77d7k1oWxBQ68Ff83v3Pxsyiqf6K9BQUVyzmI6rZafDStQm1IdTS-rvsiB4qDrx9juMqzu1udPy5N7JGs_CDV31Ky3fWEveAy4kBX46-axdyhP5XFg6xMfJ614mcf_bfo5hIJByZFwqNolNwsHLUTuiUBa4Mdg-tfob692-ox8B2c6w4RqRrLOVA_M3gENoxbLIJGL0WL1OkdQb7fyEsaMzR3urJL1t8LI5Q1pD8wjbiv4VKvc1BqoJSM0h9mLm_GNhTdQGPmevBwWVZ1k1tWJjQw0nU2eFZJi1STDGzK1GRDBD91rZSYD763WHADbxcqxrcri92jtyZrxB22pJXEgkpMlUkxqjCFATV20WSM8aSW4Od9Of9MCnrNTIby_3np4zEq5EpFEkVmH-9PzalKWo5gOHR8Zqnldyz6xcOamP34o_lEh5ddEwAFjGTlJWrDkssMeBjOog3_CXHZhutD9IfCKmIHu6Wk10XkELamiKPmNCe_CMDEdx6o6LrCtfyheOfgpDaZeZZc3Y-TF1o9J3RmCZqB-oHgLEc9mZQrGU6r5UZ4lYyfrAJl2y7Rya87LBGsUjSs7SuIyQKYkH5ek8j_9rhm_3nZhivDchkiWx5J3Pzso5Q3p6hjUfvhpgO2ywtnii45iINi5UAL6O8xqUhxZUJSoMxt1XKwx92bmC9kOoF1ljLm-w.VP_VFGef9SGdxoHCZ01FxQ";
        PublicJsonWebKey sigKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"HVDkXtG_j_JQUm_mNaRPSbsEhr6gdK0a6H4EURypTU0\",\"y\":\"NxdYFS2hl1w8VKf5UTpGXh2YR7KQ8gSBIHu64W0mK8M\",\"crv\":\"P-256\",\"d\":\"ToqTlgJLhI7AQYNLesI2i-08JuaYm2wxTCDiF-VxY4A\"}");
        PublicJsonWebKey encKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"7kaETHB4U9pCdsErbjw11HGv8xcQUmFy3NMuBa_J7Os\",\"y\":\"FZK-vSMpKk9gLWC5wdFjG1W_C7vgJtdm1YfNPZevmCw\",\"crv\":\"P-256\",\"d\":\"spOxtF0qiKrrCTaUs_G04RISjCx7HEgje_I7aihXVMY\"}");
        PbkdfKey passwordIsTaco = new PbkdfKey("taco");
        DecryptionKeyResolver decryptionKeyResolver = new DecryptionKeyResolver(){

            @Override
            public Key resolveKey(JsonWebEncryption jwe, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
                return nestingContext.isEmpty() ? passwordIsTaco : encKey.getPrivateKey();
            }
        };
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setDecryptionKeyResolver(decryptionKeyResolver).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        JwtConsumer consumer = new JwtConsumerBuilder().setDecryptionKeyResolver(decryptionKeyResolver).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420229816L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        JwtContext ctx = consumer.process(jwt);
        consumer.processContext(jwtContext);
        JwtContext[] jwtContextArray = new JwtContext[]{ctx, jwtContext};
        int n = jwtContextArray.length;
        int n2 = 0;
        while (n2 < n) {
            JwtContext context = jwtContextArray[n2];
            JwtClaims jwtClaims = context.getJwtClaims();
            Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
            List<JsonWebStructure> joseObjects = context.getJoseObjects();
            Assert.assertThat((Object)3, (Matcher)CoreMatchers.equalTo((Object)joseObjects.size()));
            Assert.assertTrue((boolean)(joseObjects.get(2) instanceof JsonWebEncryption));
            Assert.assertTrue((boolean)(joseObjects.get(1) instanceof JsonWebEncryption));
            Assert.assertTrue((boolean)(joseObjects.get(0) instanceof JsonWebSignature));
            ++n2;
        }
    }

    @Test
    public void testOnlyEncrypted() throws Exception {
        PublicJsonWebKey sigKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"HVDkXtG_j_JQUm_mNaRPSbsEhr6gdK0a6H4EURypTU0\",\"y\":\"NxdYFS2hl1w8VKf5UTpGXh2YR7KQ8gSBIHu64W0mK8M\",\"crv\":\"P-256\",\"d\":\"ToqTlgJLhI7AQYNLesI2i-08JuaYm2wxTCDiF-VxY4A\"}");
        PublicJsonWebKey encKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"EC\",\"x\":\"7kaETHB4U9pCdsErbjw11HGv8xcQUmFy3NMuBa_J7Os\",\"y\":\"FZK-vSMpKk9gLWC5wdFjG1W_C7vgJtdm1YfNPZevmCw\",\"crv\":\"P-256\",\"d\":\"spOxtF0qiKrrCTaUs_G04RISjCx7HEgje_I7aihXVMY\"}");
        String jwt = "eyJ6aXAiOiJERUYiLCJhbGciOiJFQ0RILUVTIiwiZW5jIjoiQTEyOENCQy1IUzI1NiIsImVwayI6eyJrdHkiOiJFQyIsIngiOiJ3UXdIa1RUci1tUFpaZURDYU8wRjEwNi1NTkg0aFBfX0xrTW5MaElkTVhVIiwieSI6IkF4Ul9VNW1EN1FhMnFia3R5WS0tU1dsMng0N1gxTWJ5S2Rxb1JteUFVS1UiLCJjcnYiOiJQLTI1NiJ9fQ..oeYI_sIoU1LWIUw3z16V_g.J_BlS-qDJnAqw9wzngIQQioTbTGbyFnorVRq1WTO3leFXKKuBmqoWPHqoVSZdzsVeiFkI-F1DesY489MltwGYg.egjQH2w4oHpMgfjg8saXxQ";
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtContext.getJwtClaims().getStringClaimValue("message")));
        JwtConsumer consumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420219088L)).setExpectedAudience("canada").setExpectedIssuer("usa").setRequireExpirationTime().build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtContext, consumer);
        consumer = new JwtConsumerBuilder().setDecryptionKey(encKey.getPrivateKey()).setVerificationKey(sigKey.getPublicKey()).setEvaluationTime(NumericDate.fromSeconds(1420219088L)).setExpectedAudience("canada").setDisableRequireSignature().setExpectedIssuer("usa").setRequireExpirationTime().build();
        JwtContext context = consumer.process(jwt);
        JwtClaims jwtClaims = context.getJwtClaims();
        Assert.assertThat((Object)"eh", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("message")));
        consumer.processContext(jwtContext);
    }

    @Test
    public void encOnlyWithIntegrityIssues() throws Exception {
        String jwt = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..zWNzKpA-QA0BboVl02nz-A.oSy4V6cQ6EnuIMyazDCqc9jEZMC7k8LwLKkrC12Pf-wpFRyDtQjGdIZ_Ndq9JMAnrCbx0bgFSxjKISbXbcnHiA.QsGX3JhHP1Pwy4zQ8Ha9FQ";
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"30WEMkbhwHPBkg_fIfm_4GuzIz5pPZB7_BSfI3dHbbQ\"}");
        JwksDecryptionKeyResolver decryptionKeyResolver = new JwksDecryptionKeyResolver(Collections.singletonList(jsonWebKey));
        JwtConsumer consumer = new JwtConsumerBuilder().setDecryptionKeyResolver(decryptionKeyResolver).setEvaluationTime(NumericDate.fromSeconds(1420230888L)).setExpectedAudience("me").setExpectedIssuer("me").setRequireExpirationTime().setDisableRequireSignature().build();
        JwtClaims jwtClaims = consumer.processToClaims(jwt);
        Assert.assertThat((Object)"value", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("name")));
        jwt = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..zWNzKpA-QA0BboVl02nz-A.eyJpc3MiOiJtZSIsImF1ZCI6Im1lIiwiZXhwIjoxNDIwMjMxNjA2LCJuYW1lIjoidmFsdWUifQ.QsGX3JhHP1Pwy4zQ8Ha9FQ";
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, consumer);
        jwt = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..zWNzKpA-QA0BboVl02nz-A.u1D7JCpDFeRl69G1L-h3IRrmcOXiWLnhr23ugO2kkDqKVNcO1YQ4Xvl9Sag4aYOnkqUbqe6Wdz8KK3d9q178tA.QsGX3JhHP1Pwy4zQ8Ha9FQ";
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, consumer);
    }

    @Test
    public void hmacWithResolver() throws Exception {
        String jwt = "eyJraWQiOiJfMyIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJmcm9tIiwiYXVkIjpbInRvIiwib3J5b3UiXSwiZXhwIjoxNDI0MDQxNTc0LCJzdWIiOiJhYm91dCJ9.jgC4hWHd1C4kkYiVIbung4vg44bQOEv3JkGupnRrYDk";
        JwtConsumer firstPassConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassConsumer.process(jwt);
        String json = "{\"keys\":[{\"kty\":\"oct\",\"kid\":\"_1\",  \"k\":\"9g99cnHIc3kMeR_JbwmAojgUlHIH0GoKz7COz9719x1\"},{\"kty\":\"oct\",\"kid\":\"_2\",  \"k\":\"vvlp7BacRr-a9pOKK7BKxZo88u6cY2o9Lz6-P--_01p\"},{\"kty\":\"oct\",\"kid\":\"_3\",\"k\":\"a991cccx6-7rP5p91nnHi3K-jcDjsFh1o34bIeWA081\"}]}";
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(json);
        JwtConsumer consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1424041569L)).setExpectedAudience("to").setExpectedIssuer("from").setRequireSubject().setVerificationKeyResolver(new JwksVerificationKeyResolver(jsonWebKeySet.getJsonWebKeys())).setRequireExpirationTime().build();
        JwtContext ctx = consumer.process(jwt);
        consumer.processContext(jwtContext);
        JwtContext[] jwtContextArray = new JwtContext[]{ctx, jwtContext};
        int n = jwtContextArray.length;
        int n2 = 0;
        while (n2 < n) {
            JwtContext context = jwtContextArray[n2];
            Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)context.getJoseObjects().size()));
            Assert.assertThat((Object)"about", (Matcher)CoreMatchers.equalTo((Object)context.getJwtClaims().getSubject()));
            ++n2;
        }
    }

    @Test
    public void ifItWereAnIdTokenHint() throws InvalidJwtException, JoseException, MalformedClaimException {
        String keys = "{\n\"keys\": [\n  {\n    \"e\": \"AQAB\",\n    \"kid\": \"PHPOP-00\",\n    \"kty\": \"RSA\",\n    \"n\": \"lqjtB9h9j1yl5Y3pmyt0qRUuGnCSn6HWFXHdlUPwt2xanA8aP5MN5dlRJCVR_sR08pb4taIerowTZ7ShdSaWqkGAqwgJYhM0Nyvj_GO1XIYfWl2u49U8j1sEFGDvNMNYQcX4RwaLU3lbavlYVHx_0W5gvw6XfEvkdWkPEbO3Ik1_cCySBxbaCxKszFP_yKCfRBbSQzrz_ZV6PMU6B0_OSknD7BRaogABdxPu79mUU-_Fk1XSA4gdRd5ccnX6lXiF0ePiI2x7s-RdyrMMT4HrXMYlO7VxraUvK61bNOKuRqoV6K-OdJUbcgziRe0nEidgyOgRTXRgnRkyCp2eMkKXFw\"\n}]}";
        String jwt = "eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOlwvXC9jb25uZWN0Lm9wZW5pZDQudXM6NTQ0M1wvcGhwT3BcL29wLmp3ayIsImtpZCI6IlBIUE9QLTAwIn0.eyJpc3MiOiJodHRwczpcL1wvY29ubmVjdC5vcGVuaWQ0LnVzOjU0NDNcL3BocE9wIiwic3ViIjoiZDRjMTEzOTE3NTA1MmRkNTE1ZmE5MzU4YTVjMmQ0YjRhNGFkYTM2ZDgxNWJiODc4OWEwNDFhNDFmZmZmZGNlYSIsImF1ZCI6WyJSLVJ1ZmpTRFZHQ0dmZFRtSW9iZjJRIl0sImV4cCI6MTQyODQ0NjkwNSwiaWF0IjoxNDI4NDQ2NjA1LCJub25jZSI6IlB1enhKSWtxdjZ6ciIsImF1dGhfdGltZSI6MTQyODQ0NTAxMH0.WYh2Zn3oNys7VIa6bCCw9LcIPD95W5YP4XKiIBcY5gz0Ti3fiwslsbm1wGJB-nJA9AXi1cIywsZs94l7BKJdNdUiJQUuSFRuyHCCDY--7iELwWFIGXSzFkwjUsRAAq9sMWqBO3qm01ganUH4Q9wFuSa-d6GA8ybMy3ymfV1OyNzVpTUqi9HWrRlAw0jUoTVGZA4p7qMzXgZfNF3pyankL2mmeb34ZhFk8S2IAZKFhRKuo0ORJRJ6_Fu9Eq0DvfrvX1RJpA3MKkJ8aiD5N4fcUy7vzgQRCNqsgEaqC-i4-vlNN5uyKP5IUZW-hqh-c6rXVrM-8hpZtCM_Z76eRfv1VQ";
        JwtConsumer consumer = new JwtConsumerBuilder().setVerificationKeyResolver(new JwksVerificationKeyResolver(new JsonWebKeySet(keys).getJsonWebKeys())).setAllowedClockSkewInSeconds(Integer.MAX_VALUE).setExpectedSubject("d4c1139175052dd515fa9358a5c2d4b4a4ada36d815bb8789a041a41ffffdcea").setExpectedAudience("R-RufjSDVGCGfdTmIobf2Q").build();
        JwtContext jwtCtx = consumer.process(jwt);
        Assert.assertThat((Object)jwtCtx.getJwtClaims().getSubject(), (Matcher)CoreMatchers.equalTo((Object)"d4c1139175052dd515fa9358a5c2d4b4a4ada36d815bb8789a041a41ffffdcea"));
        consumer = new JwtConsumerBuilder().setVerificationKeyResolver(new JwksVerificationKeyResolver(new JsonWebKeySet(keys).getJsonWebKeys())).setAllowedClockSkewInSeconds(Integer.MAX_VALUE).setExpectedSubject("NOOOOOOOOOOOOOOOOPE").setExpectedAudience("R-RufjSDVGCGfdTmIobf2Q").build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, consumer);
        consumer = new JwtConsumerBuilder().setVerificationKeyResolver(new JwksVerificationKeyResolver(new JsonWebKeySet(keys).getJsonWebKeys())).setAllowedClockSkewInSeconds(Integer.MAX_VALUE).setSkipDefaultAudienceValidation().build();
        jwtCtx = consumer.process(jwt);
        Assert.assertThat((Object)jwtCtx.getJwtClaims().getAudience().iterator().next(), (Matcher)CoreMatchers.equalTo((Object)"R-RufjSDVGCGfdTmIobf2Q"));
        consumer = new JwtConsumerBuilder().setVerificationKeyResolver(new JwksVerificationKeyResolver(new JsonWebKeySet(keys).getJsonWebKeys())).setAllowedClockSkewInSeconds(Integer.MAX_VALUE).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, consumer);
        consumer = new JwtConsumerBuilder().setVerificationKeyResolver(new JwksVerificationKeyResolver(new JsonWebKeySet(keys).getJsonWebKeys())).setAllowedClockSkewInSeconds(Integer.MAX_VALUE).setExpectedAudience("no", "nope", "no way jose").build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, consumer);
    }

    @Test
    public void relaxDecryptionKeyValidation() throws Exception {
        String jwt = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkExMjhDQkMtSFMyNTYiLCJraWQiOiJhY2MiLCJjdHkiOiJKV1QifQ.KrukndaF2sHb3Y0r311rrYmCrXco-99ZIQ3iLjvCVbbow5MppRTK4DPJUShcndfcIVIFXMYSLGvIJwf39yZRJJ_EvBFnqhOUeCAsUHLGO1yxoQ619jmSh4bCaIicLYeivKaVSQN4Ezc5fvg-Nnv6TBIIgHuWMDU2Ztd96DJRokc.wMg2Eb8izCOUnACqdrcPQA.quFKSN7xQoMJzaYFBVwykQZ8zB3hpW8HtK7pm-4Ggzorno_K-eBQ7fXjRmJ1Jw-kCcmUa8flpnQqpL9jurtlz7DC1ABe0vm2ZkHoJluB6QeSr60Y9rP7kyy_rd3blXT_7t6Wgowo8MumXrrUUxxEQJgXvCmKbd-Rw9sK5jAHEug3zztLXHOX0O0QoxDzTJOsSRtodsu7bTJa-ADvPmK9e0Xp06NRqvx7WuJGKlq3cwQ.DL6yaCdiOUcViN-eZVIwOA";
        JsonWebKeySet jwks = new JsonWebKeySet("{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"acc\",\"n\":\"pkRsP8W09WkolK85OQlq6XTQEoRsulNY6vQsJMluOPErKIOJp6K4cgg5n6Y9NXnswUt0n5suxqlKDHmRRQgU9BGBcqptmCog-0KQKvTqUQJmtDviRTu1aO12Zz_ATEszf8rvPt795xaFvDycCA2YS87lkdIET2ap2qrHCfeWlkk\",\"e\":\"AQAB\",\"d\":\"MnNknV0ycZz9EVCx_lqbNEebs2K3UzpjKrf4hRkR9vlG7T4skM9RRFi2k3jv7cAXVPe-ZYfDA8jujSZ-LAItyPwIO-pbtIeXrKQtvLgP4igsfDMCmvRvNmUuV93Gy9fMBVhEGK_xxVQtJWbdgZsk_v2kMUkX4W2WS_Mbo3YHCwE\",\"p\":\"2BBdLVoi6DP-5JJyTCxdBbaKUjQvVPHXlcqNdaKf2949Nze7IpLoPtkCTVVlTtEvAhYGxuI1i101fK4hGW_IcQ\",\"q\":\"xP_Mg7_SNlzg0eyCzK09mKdagOFfoHKIMoJb9qzOAENnIjt67hpxd7x2h45pX4HM7ObU_1OAl9IYvTqUPhPXWQ\",\"dp\":\"ljx6rchZMWDGQiVaeID4hbpx38sNhmFLaIqZZkyYH4gexMBpzRadiuXWZfOVKALoTukF-VDdrnQ3duSVe1xw4Q\",\"dq\":\"Q23K8s2VhkYELdZmbuhdTQL7V2HM-X46YA9-qtA7MpvfkTgKu7URYYqAh6WXK7miCvR3s21BdrXTAfIrC5R_AQ\",\"qi\":\"iaHGlWvmsQvWyZ5GdAar0WOJi_CNTGCzv9SaVnA83I1ewXvKejYMnzLjetPbopxE2enVicnvjlrDaihJbZ5TYA\"},{\"kty\":\"oct\",\"kid\":\"ltc\",\"k\":\"vJRXGLSNo-jggR8o5yxjzrm_82w-35rpnve0JzEr2sw\"}]}");
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
        JwksDecryptionKeyResolver decryptionKeyResolver = new JwksDecryptionKeyResolver(jwks.getJsonWebKeys());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1432324168L)).setExpectedAudience("a").setExpectedIssuer("i").setExpectedSubject("s").setRequireExpirationTime().setVerificationKeyResolver(verificationKeyResolver).setDecryptionKeyResolver(decryptionKeyResolver).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1432324168L)).setExpectedAudience("a").setExpectedIssuer("i").setExpectedSubject("s").setRequireExpirationTime().setVerificationKeyResolver(verificationKeyResolver).setDecryptionKeyResolver(decryptionKeyResolver).setRelaxDecryptionKeyValidation().build();
        JwtClaims claims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)claims.getClaimsMap().size(), (Matcher)CoreMatchers.equalTo((Object)5));
    }

    @Test
    public void relaxVerificationKeyValidation() throws Exception {
        String jwt = "eyJraWQiOiJlc2MiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhIiwiaXNzIjoiaSIsImV4cCI6MTQzMjMyNTQ5Niwic3ViIjoicyIsIm5iZiI6MTQzMjMyNDgzNn0.16LpzAZyBcokZ4aUaXHn5yN0xQ1zpmLyJVFHu6nH1zY";
        JsonWebKeySet jwks = new JsonWebKeySet("{\"keys\":[{\"kty\":\"oct\",\"kid\":\"esc\",\"k\":\"dbwsHvQsXoZiWpulhZA8dg\"}]}");
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1432324836L)).setExpectedAudience("a").setExpectedIssuer("i").setExpectedSubject("s").setRequireExpirationTime().setVerificationKeyResolver(verificationKeyResolver).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1432324836L)).setExpectedAudience("a").setExpectedIssuer("i").setExpectedSubject("s").setRequireExpirationTime().setVerificationKeyResolver(verificationKeyResolver).setRelaxVerificationKeyValidation().build();
        JwtClaims claims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)claims.getClaimsMap().size(), (Matcher)CoreMatchers.equalTo((Object)5));
    }

    @Test
    public void skipAllDefaultValidators() throws Exception {
        String jwt = "eyJraWQiOiJ4eGMiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhIiwiaXNzIjoiaSIsImV4cCI6MTQzMjMyNzE5NSwic3ViIjoicyIsIm5iZiI6MTQzMjMyNjUzNX0.zfBXCLSysVxY-zT4DNCLXS7IyfKkYv7kCIUKxdIGxdI";
        JsonWebKeySet jwks = new JsonWebKeySet("{\"keys\":[{\"kty\":\"oct\",\"kid\":\"xxc\",\"k\":\"7bLZdrROsprHkX75gCjKLeGj4brDf7TFtcr2h1F_nfc\"}]}");
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setVerificationKeyResolver(verificationKeyResolver).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setVerificationKeyResolver(verificationKeyResolver).setSkipAllDefaultValidators().build();
        JwtClaims claims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)claims.getClaimsMap().size(), (Matcher)CoreMatchers.equalTo((Object)5));
        Validator customValidator = new Validator(){

            @Override
            public String validate(JwtContext jwtContext) throws MalformedClaimException {
                return jwtContext.getJwtClaims().getIssuer().equals("i") ? "i isn't okay as an issuer" : null;
            }
        };
        jwtConsumer = new JwtConsumerBuilder().setVerificationKeyResolver(verificationKeyResolver).setSkipAllDefaultValidators().registerValidator(customValidator).build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setVerificationKeyResolver(verificationKeyResolver).setSkipAllValidators().registerValidator(customValidator).build();
        claims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)claims.getClaimsMap().size(), (Matcher)CoreMatchers.equalTo((Object)5));
    }

    @Test
    public void googleIdTokensAndMultipleIssuers() throws JoseException, InvalidJwtException, MalformedClaimException {
        String jwksUriContent = "{\n \"keys\": [\n  {\n   \"kty\": \"RSA\",\n   \"alg\": \"RS256\",\n   \"use\": \"sig\",\n   \"kid\": \"6faa4e9ec30030784b8942606fb61762ada97253\",\n   \"n\": \"mQFT4IjnxC1yhSqumpxY-BcRNfwfkqbYVHfIJNxTdQiTdVFizapkQEuRvuLLXVBZcTKJftQNEZ4RHbXTJFq5l6MoDPMSHCH_MBLjkYhrHLSdLpmJRb047PgbjVYCRbAEuuf-ejwLPRTdrPCaC3vEm4-UaJgNoVnKpQKCCl4LRhaSdIXrmAv-AKwq7RmTYwP84UcbL379xhvUUnA3BMrNzSeyEPPUeOJO5eAprcSGQztFE2FuqXFPrOMkD_WK9El21UHEwPzpUD-OvTL4LC9w1dImfzU5SC3g1DBz0N3GZawWGoNSH5x6gYereKVmfdPmX6zbV-Lb4mv3Kh8hki2jOw\",\n   \"e\": \"AQAB\"\n  },\n  {\n   \"kty\": \"RSA\",\n   \"alg\": \"RS256\",\n   \"use\": \"sig\",\n   \"kid\": \"104625465f6d4c7d214e3326913c5a5e4505699c\",\n   \"n\": \"qysmso1d2qSWZzMWqmfDHc7vR75gS5MCv1eMhzOrs9axnpyId0TzUQl2o2Su2o0mMtEfiirEhPFHPbFLVX8xc9SJPF6HCTQVS120_1NIjBhkZeiXzW4J6V8HSgL_9gwIwaMjJYv7MB5SpHYIuIrdiUliaxPBCt2xZKqvAcU7G33kvOi7XneQaFxQrj2yxD9WkX-fRWS_0oZwN9-SBtQ84LJHYSgS-nclK2uuSHBI5_OV14r6A5boRU7Hjq7DLDjz7XxxXGqwbU5KYGjBP-_v3OKvWKyTH4zQr24pmGVeTxZ_R1XAitO73cYtqqa25UvKGvFfam8-6VSVjrPC5tFayQ\",\n   \"e\": \"AQAB\"\n  },\n  {\n   \"kty\": \"RSA\",\n   \"alg\": \"RS256\",\n   \"use\": \"sig\",\n   \"kid\": \"d2d90509119f4e7303c5d00647a27f340f928888\",\n   \"n\": \"39CuERDfCaxMvPM_8cu4wj_decxS7OpB7NPUfO4LiH7e3ZXU83SLsSr4roDwgwlx_he4gFOnEjZ10aastOropI7Mx8Aw-EcHyOKgg1dzk3CjunlLc4vMbqSdRbN_UnQWWa-aYgVGOloXDuVT4LegKrgQpKwUJ-IfaTIVGf5kQhYtJgC-LTgBpu99M2wVFQGLLqurNdbTIomWv75whFli3VRuTcb-0lBq0M9D6d3VEn747YS1c8i38e0Kbd9-XcPHWLCmi0tG0RmJ1iWB9rGiima9rU-MmIs7oaMg3COFoqtXiCzAWdVp-lsWIa9d7Ci3aykJpBK2AZ-xjMUg7UZfHw\",\n   \"e\": \"AQAB\"\n  }\n ]\n}";
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1470854975L)).setExpectedAudience("407408718192.apps.googleusercontent.com").setExpectedIssuers(true, "https://accounts.google.com", "accounts.google.com").setRequireSubject().setRequireExpirationTime().setVerificationKeyResolver(new JwksVerificationKeyResolver(new JsonWebKeySet(jwksUriContent).getJsonWebKeys())).build();
        String jwt = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwNDYyNTQ2NWY2ZDRjN2QyMTRlMzMyNjkxM2M1YTVlNDUwNTY5OWMifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhdWQiOiI0MDc0MDg3MTgxOTIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDkzNTgzODE5Nzc2Mzg1MTcyODYiLCJhenAiOiI0MDc0MDg3MTgxOTIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJub25jZSI6ImZmcyIsImlhdCI6MTQ3MDg1NDg3MCwiZXhwIjoxNDcwODU4NDcwfQ.PeIZ-N5SHIwdYBL-LgCxY3wuKeoarfwbiKiVegEB6sD7UB96j-eNTreTCTSywj8DQIOvegEyaxhCHZaVJ7mIwRsTnlstUUR6soe8tu2gjhOqTkqaYeKAqbPov7-M9afY-MgvHe4xndIEh1So54bf1lJ_PzrJnCXHBaCobhs4clhPMqZuy9XlPaZMDJPDfVsbPdHqV6Uxt4KTQQECI_i9j4wP6ks5g1lbKTpyKrXOm4n-25zp1_HlKSEb7kqd-1zTvEz2W0tq741b2STnrZ1RW13Gh4cZzOVSRvqo2oNNq286R22JEHVWjBuR01OiasgyY8QcYPI_8F-K9cAhsNJhcg";
        JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)"ffs", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("nonce")));
        Assert.assertThat((Object)"https://accounts.google.com", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getIssuer()));
        jwt = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjZmYWE0ZTllYzMwMDMwNzg0Yjg5NDI2MDZmYjYxNzYyYWRhOTcyNTMifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXVkIjoiNDA3NDA4NzE4MTkyLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTA5MzU4MzgxOTc3NjM4NTE3Mjg2IiwiYXpwIjoiNDA3NDA4NzE4MTkyLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwibm9uY2UiOiJmZnMiLCJpYXQiOjE0NzA4NTU5ODMsImV4cCI6MTQ3MDg1OTU4M30.lxxxAqApJvQEubL4FXHcJvsG9wu3kxWcCFZTt6OMjB9j_P1KNu6CAgRn-E9T-ACJGpqVjR0GoODlVIdZHF2wnntOxv9hNY7huSPDeWy661nCYuBMJRMcIqx6Hl7M7fCtTEu0ERYRHy9L9-tWnWUyxz3aZVvWQR1LB6P2Zwgv1aZPptoTO5GxyNVIQApHq-BbNtaVd6qa3XDFrLMyq84FYwgGJzCjoM9Vu3YN4S4DZs6M59FC_hMqldOqrkOCDs0Z49-q1pRS3WDZP_5r6gF9AKzyoB2TuEjMGrSHzp3l8YLuzHVCH8gQkiS9uzJESrEbYP9cr5AgMB5e4WGd0n1pXQ";
        jwtClaims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)"ffs", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getStringClaimValue("nonce")));
        Assert.assertThat((Object)"accounts.google.com", (Matcher)CoreMatchers.equalTo((Object)jwtClaims.getIssuer()));
    }

    @Test
    public void roundTripWithMoreLiveDateChecks() throws Exception {
        OctetSequenceJsonWebKey octetSequenceJsonWebKey = OctJwkGenerator.generateJwk(256);
        octetSequenceJsonWebKey.setKeyId("ltc");
        JsonWebKeySet jwks = new JsonWebKeySet(octetSequenceJsonWebKey);
        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setAudience("a");
        jwtClaims.setIssuer("i");
        jwtClaims.setExpirationTimeMinutesInTheFuture(2.0f);
        jwtClaims.setSubject("s");
        jwtClaims.setNotBeforeMinutesInThePast(2.0f);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(jwtClaims.toJson());
        jws.setKey(octetSequenceJsonWebKey.getKey());
        jws.setKeyIdHeaderValue(octetSequenceJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue("HS256");
        String jwt = jws.getCompactSerialization();
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("a").setExpectedIssuer("i").setExpectedSubject("s").setRequireExpirationTime().setVerificationKeyResolver(verificationKeyResolver).build();
        JwtClaims claims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)claims.getClaimsMap().size(), (Matcher)CoreMatchers.equalTo((Object)5));
        jwtClaims = new JwtClaims();
        jwtClaims.setAudience("a");
        jwtClaims.setIssuer("i");
        jwtClaims.setExpirationTimeMinutesInTheFuture(-1.0f);
        jwtClaims.setSubject("s");
        jwtClaims.setNotBeforeMinutesInThePast(3.0f);
        jws = new JsonWebSignature();
        jws.setPayload(jwtClaims.toJson());
        jws.setKey(octetSequenceJsonWebKey.getKey());
        jws.setKeyIdHeaderValue(octetSequenceJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue("HS256");
        jwt = jws.getCompactSerialization();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtConsumer);
        jwtClaims = new JwtClaims();
        jwtClaims.setAudience("a");
        jwtClaims.setIssuer("i");
        jwtClaims.setExpirationTimeMinutesInTheFuture(-1.0f);
        jwtClaims.setSubject("s");
        jws = new JsonWebSignature();
        jws.setPayload(jwtClaims.toJson());
        jws.setKey(octetSequenceJsonWebKey.getKey());
        jws.setKeyIdHeaderValue(octetSequenceJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue("HS256");
        jwt = jws.getCompactSerialization();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtConsumer);
        jwtClaims = new JwtClaims();
        jwtClaims.setAudience("a");
        jwtClaims.setIssuer("i");
        jwtClaims.setExpirationTimeMinutesInTheFuture(20.0f);
        jwtClaims.setSubject("s");
        jwtClaims.setNotBeforeMinutesInThePast(-4.0f);
        jws = new JsonWebSignature();
        jws.setPayload(jwtClaims.toJson());
        jws.setKey(octetSequenceJsonWebKey.getKey());
        jws.setKeyIdHeaderValue(octetSequenceJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue("HS256");
        jwt = jws.getCompactSerialization();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, jwtConsumer);
        jwtClaims = new JwtClaims();
        jwtClaims.setAudience("a");
        jwtClaims.setIssuer("i");
        jwtClaims.setExpirationTimeMinutesInTheFuture(1.0f);
        jwtClaims.setSubject("s");
        jws = new JsonWebSignature();
        jws.setPayload(jwtClaims.toJson());
        jws.setKey(octetSequenceJsonWebKey.getKey());
        jws.setKeyIdHeaderValue(octetSequenceJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue("HS256");
        jwt = jws.getCompactSerialization();
        claims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)claims.getClaimsMap().size(), (Matcher)CoreMatchers.equalTo((Object)4));
    }

    @Test
    public void someBasicAudChecks() throws InvalidJwtException {
        JwtClaims jwtClaims = JwtClaims.parse("{\"aud\":\"example.com\"}");
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.com").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.org", "example.com", "k8HiI26Y7").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.org").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.org", "nope", "nada").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"sub\":\"subject\"}");
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience(false, "example.org", "www.example.org").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience(true, "example.org", "www.example.org").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.org").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"aud\":[\"example.com\", \"usa.org\", \"ca.ca\"]}");
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.org").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.org", "some.other.junk").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("usa.org").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("ca.ca").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("ca.ca", "some.other.thing").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("noway", "ca.ca", "some.other.thing").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("usa.org", "ca.ca", "random").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("usa.org", "ca.ca").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("usa.org", "ca.ca", "example.com").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"aud\":[\"example.com\", 47, false]}");
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.org").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"aud\":20475}");
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.org").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"aud\":{\"aud\":\"example.org\"}}");
        jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("example.org").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
    }

    @Test
    public void someBasicIssChecks() throws InvalidJwtException {
        JwtClaims jwtClaims = JwtClaims.parse("{\"iss\":\"issuer.example.com\"}");
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer(null).build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer(false, null).build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer("issuer.example.com").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer(false, "issuer.example.com").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer("nope.example.com").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"sub\":\"subject\"}");
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer("issuer.example.com").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer(false, "issuer.example.com").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer(false, null).build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"iss\":[\"issuer1\", \"other.one\", \"meh\"]}");
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer("issuer.example.com").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"iss\":[\"issuer1\", \"nope.not\"]}");
        jwtConsumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"iss\":\"accounts.google.com\"}");
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuers(true, "https://accounts.google.com", "accounts.google.com").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"iss\":\"https://accounts.google.com\"}");
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuers(true, "https://fake.google.com", "nope.google.com").build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"iss\":\"d\"}");
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuers(true, "a", "b", "c", "d", "e").build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"iss\":\"x\"}");
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuer(true, null).build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuers(true, new String[0]).build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setExpectedIssuers(true, new String[0]).build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
    }

    @Test
    public void someBasicSubChecks() throws InvalidJwtException {
        JwtClaims jwtClaims = JwtClaims.parse("{\"sub\":\"brian.d.campbell\"}");
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setRequireSubject().build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"name\":\"brian.d.campbell\"}");
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"sub\":724729}");
        jwtConsumer = new JwtConsumerBuilder().setRequireSubject().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"sub\":{\"values\":[\"one\", \"2\"]}}");
        jwtConsumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
    }

    @Test
    public void someBasicJtiChecks() throws InvalidJwtException {
        JwtClaims jwtClaims = JwtClaims.parse("{\"jti\":\"1Y5iLSQfNgcSGt0A4is29\"}");
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().setRequireJwtId().build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"notjti\":\"lbZ_mLS6w3xBSlvW6ULmkV-uLCk\"}");
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtConsumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.goodValidate(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"jti\":55581529751992}");
        jwtConsumer = new JwtConsumerBuilder().setRequireJwtId().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
        jwtClaims = JwtClaims.parse("{\"jti\":[\"S0w3XbslvW6ULmk0\", \"5iLSQfNgcSGt7A4is\"]}");
        jwtConsumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jwtClaims, jwtConsumer);
    }

    @Test
    public void someBasicTimeChecks() throws InvalidJwtException, MalformedClaimException {
        JwtClaims jcs = JwtClaims.parse("{\"sub\":\"brian.d.campbell\"}");
        JwtConsumer consumer = new JwtConsumerBuilder().build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireIssuedAt().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireNotBefore().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        jcs = JwtClaims.parse("{\"sub\":\"brian.d.campbell\", \"exp\":1430602000}");
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430602000L)).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430602000L)).setAllowedClockSkewInSeconds(10).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1430601000L)).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430601000L)).setAllowedClockSkewInSeconds(6000).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1430602002L)).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430602002L)).setAllowedClockSkewInSeconds(1).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430602002L)).setAllowedClockSkewInSeconds(2).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430602002L)).setAllowedClockSkewInSeconds(3).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(0x55454151L)).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(0x55454151L)).setAllowedClockSkewInSeconds(60).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(0x55454151L)).setAllowedClockSkewInSeconds(120).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        jcs = JwtClaims.parse("{\"sub\":\"brian.d.campbell\", \"nbf\":1430602000}");
        consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1430602000L)).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1430601999L)).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1430601983L)).setAllowedClockSkewInSeconds(30).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        consumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1430601983L)).setAllowedClockSkewInSeconds(3000).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        jcs = JwtClaims.parse("{\"sub\":\"brian.d.campbell\", \"nbf\":1430602000, \"iat\":1430602060, \"exp\":1430602600 }");
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setRequireNotBefore().setRequireIssuedAt().setEvaluationTime(NumericDate.fromSeconds(1430602002L)).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        jcs = JwtClaims.parse("{\"sub\":\"brian.d.campbell\", \"nbf\":1430603000, \"iat\":1430602060, \"exp\":1430602600 }");
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430602002L)).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        jcs = JwtClaims.parse("{\"sub\":\"brian.d.campbell\", \"nbf\":1430602000, \"iat\":1430602660, \"exp\":1430602600 }");
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430602002L)).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        jcs = JwtClaims.parse("{\"sub\":\"brian.d.campbell\", \"exp\":1430607201}");
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430600000L)).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430600000L)).setMaxFutureValidityInMinutes(90).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430600000L)).setMaxFutureValidityInMinutes(120).build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
        consumer = new JwtConsumerBuilder().setRequireExpirationTime().setEvaluationTime(NumericDate.fromSeconds(1430600000L)).setMaxFutureValidityInMinutes(120).setAllowedClockSkewInSeconds(20).build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
    }

    @Test
    public void someBasicChecks() throws InvalidJwtException {
        JwtClaims jcs = JwtClaims.parse("{\"sub\":\"subject\", \"iss\":\"issuer\", \"aud\":\"audience\"}");
        JwtConsumer consumer = new JwtConsumerBuilder().setExpectedAudience("audience").setExpectedIssuer("issuer").build();
        SimpleJwtConsumerTestHelp.goodValidate(jcs, consumer);
        consumer = new JwtConsumerBuilder().setExpectedAudience("nope").setExpectedIssuer("no way").setRequireSubject().setRequireJwtId().build();
        SimpleJwtConsumerTestHelp.expectValidationFailure(jcs, consumer);
    }

    @Test
    public void testNpeWithNonExtractableKeyDataHS256() throws Exception {
        byte[] raw = Base64Url.decode("hup76LcA9B7pqrEtqyb4EBg6XCcr9r0iOCFF1FeZiJM");
        FakeHsmNonExtractableSecretKeySpec key = new FakeHsmNonExtractableSecretKeySpec(raw, "HmacSHA256");
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(5.0f);
        claims.setSubject("subject");
        claims.setIssuer("issuer");
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue("HS256");
        jws.setKey(key);
        String jwt = jws.getCompactSerialization();
        JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder();
        jwtConsumerBuilder.setAllowedClockSkewInSeconds(60);
        jwtConsumerBuilder.setRequireSubject();
        jwtConsumerBuilder.setExpectedIssuer("issuer");
        jwtConsumerBuilder.setVerificationKey(key);
        JwtConsumer jwtConsumer = jwtConsumerBuilder.build();
        JwtClaims processedClaims = jwtConsumer.processToClaims(jwt);
        System.out.println(processedClaims);
    }

    @Test
    public void testNpeWithNonExtractableKeyDataAxxxKW() throws Exception {
        this.littleJweRoundTrip("A128KW", "A128CBC-HS256", "mmp7iLc1cB7cQrEtqyb9c1");
        this.littleJweRoundTrip("A192KW", "A192CBC-HS384", "X--mSrs-JGaf0ulQQFSoJGH0vjrfe_c1");
        this.littleJweRoundTrip("A256KW", "A256CBC-HS512", "j-DJVQ9ftUV-muUT_-yjP6dB9kuypGeT6lEGpCKOi-c");
    }

    public void testNpeWithNonExtractableKeyDataDirect() throws Exception {
        this.littleJweRoundTrip("dir", "A128CBC-HS256", "j-DJVQ9ftUV-muUT_-yjP6dB9kuypGeT6lEGpCKOi-c");
        this.littleJweRoundTrip("dir", "A192CBC-HS384", "X--mSrs-JGaf0ulQQFSoJGH0vjrfe_c1X--mSrs-JGaf0ulQQFSoJGH0vjrfe_c1");
        this.littleJweRoundTrip("dir", "A256CBC-HS512", "j-DJVQ9ftUV-muUT_-yjP6dB9kuypGeT6lEGpCKOi-cj-DJVQ9ftUV-muUT_-yjP6dB9kuypGeT6lEGpCKOi-c");
        JceProviderTestSupport jceProviderTestSupport = new JceProviderTestSupport();
        jceProviderTestSupport.setEncryptionAlgsNeeded("A128GCM", "A192GCM", "A256GCM");
        jceProviderTestSupport.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                JwtConsumerTest.this.littleJweRoundTrip("dir", "A128GCM", "mmp7iLc1cB7cQrEtqyb9c1");
                JwtConsumerTest.this.littleJweRoundTrip("dir", "A192GCM", "X--mSrs-JGaf0ulQQFSoJGH0vjrfe_c1");
                JwtConsumerTest.this.littleJweRoundTrip("dir", "A256GCM", "j-DJVQ9ftUV-muUT_-yjP6dB9kuypGeT6lEGpCKOi-c");
            }
        });
    }

    private void littleJweRoundTrip(String alg, String enc, String b64uKey) throws Exception {
        byte[] raw = Base64Url.decode(b64uKey);
        FakeHsmNonExtractableSecretKeySpec key = new FakeHsmNonExtractableSecretKeySpec(raw, "AES");
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(5.0f);
        claims.setSubject("subject");
        claims.setIssuer("issuer");
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setPayload(claims.toJson());
        jwe.setAlgorithmHeaderValue(alg);
        jwe.setEncryptionMethodHeaderParameter(enc);
        jwe.setKey(key);
        String jwt = jwe.getCompactSerialization();
        JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder();
        jwtConsumerBuilder.setAllowedClockSkewInSeconds(60);
        jwtConsumerBuilder.setRequireSubject();
        jwtConsumerBuilder.setExpectedIssuer("issuer");
        jwtConsumerBuilder.setDecryptionKey(key);
        jwtConsumerBuilder.setDisableRequireSignature();
        JwtConsumer jwtConsumer = jwtConsumerBuilder.build();
        JwtClaims processedClaims = jwtConsumer.processToClaims(jwt);
        Assert.assertThat((Object)processedClaims.getSubject(), (Matcher)CoreMatchers.equalTo((Object)"subject"));
    }

    @Test
    public void testNpeWithNonExtractableKeyDataAxxxGCMKW() throws Exception {
        JceProviderTestSupport jceProviderTestSupport = new JceProviderTestSupport();
        jceProviderTestSupport.setKeyManagementAlgsNeeded("A128GCMKW", "A192GCMKW", "A256GCMKW");
        jceProviderTestSupport.setEncryptionAlgsNeeded("A128GCM", "A192GCM", "A256GCM");
        jceProviderTestSupport.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                JwtConsumerTest.this.littleJweRoundTrip("A128GCMKW", "A128GCM", "mmp7iLc1cB7cQrEtqyb9c1");
                JwtConsumerTest.this.littleJweRoundTrip("A192GCMKW", "A192GCM", "X--mSrs-JGaf0ulQQFSoJGH0vjrfe_c1");
                JwtConsumerTest.this.littleJweRoundTrip("A256GCMKW", "A256GCM", "j-DJVQ9ftUV-muUT2-yjP6dB9kuypGeT6lEGpCKOi-c");
            }
        });
    }

    @Test
    public void customizationCallbacksWithCritHeaders() throws Exception {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("me");
        claims.setAudience("a");
        claims.setIssuer("i");
        claims.setExpirationTimeMinutesInTheFuture(10.0f);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setKey(ExampleEcKeysFromJws.PRIVATE_256);
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue("ES256");
        jws.setCriticalHeaderNames("fake.meh");
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setPayload(jws.getCompactSerialization());
        jwe.setAlgorithmHeaderValue("RSA-OAEP");
        jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        jwe.setKey(ExampleRsaKeyFromJws.PUBLIC_KEY);
        jwe.setContentTypeHeaderValue("jwt");
        jwe.setCriticalHeaderNames("fake.blah");
        System.out.println(claims);
        String nestedJwt = jwe.getCompactSerialization();
        System.out.println(nestedJwt);
        JwtConsumer consumer = new JwtConsumerBuilder().setDecryptionKey(ExampleRsaKeyFromJws.PRIVATE_KEY).setVerificationKey(ExampleEcKeysFromJws.PUBLIC_256).setExpectedAudience("a").setRequireExpirationTime().build();
        SimpleJwtConsumerTestHelp.expectProcessingFailure(nestedJwt, consumer);
        consumer = new JwtConsumerBuilder().setDecryptionKey(ExampleRsaKeyFromJws.PRIVATE_KEY).setVerificationKey(ExampleEcKeysFromJws.PUBLIC_256).setExpectedAudience("a").setRequireExpirationTime().setJwsCustomizer(new JwsCustomizer(){

            @Override
            public void customize(JsonWebSignature jws, List<JsonWebStructure> nestingContext) {
                jws.setKnownCriticalHeaders("fake.meh");
            }
        }).setJweCustomizer(new JweCustomizer(){

            @Override
            public void customize(JsonWebEncryption jwe, List<JsonWebStructure> nestingContext) {
                jwe.setKnownCriticalHeaders("fake.blah");
            }
        }).build();
        JwtContext ctx = consumer.process(nestedJwt);
        Assert.assertThat((Object)ctx.getJoseObjects().size(), (Matcher)CoreMatchers.equalTo((Object)2));
        Assert.assertThat((Object)ctx.getJwtClaims().getSubject(), (Matcher)CoreMatchers.equalTo((Object)"me"));
        Assert.assertThat((Object)ctx.getJwt(), (Matcher)CoreMatchers.equalTo((Object)nestedJwt));
    }

    @Test
    public void iatBeforeNbfShouldBeOkay() throws Exception {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("me");
        claims.setNotBeforeMinutesInThePast(1.0f);
        claims.setExpirationTimeMinutesInTheFuture(10.0f);
        NumericDate issuedAt = NumericDate.now();
        issuedAt.addSeconds(-120L);
        claims.setIssuedAt(issuedAt);
        claims.setAudience("audience");
        claims.setIssuer("issuer");
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(ExampleEcKeysFromJws.PRIVATE_256);
        jws.setAlgorithmHeaderValue("ES256");
        String jwt = jws.getCompactSerialization();
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("audience").setExpectedIssuer("issuer").setVerificationKey(ExampleEcKeysFromJws.PUBLIC_256).setRequireExpirationTime().setRequireNotBefore().setRequireIssuedAt().build();
        JwtContext ctx = jwtConsumer.process(jwt);
        Assert.assertThat((Object)ctx.getJwtClaims().getSubject(), (Matcher)CoreMatchers.equalTo((Object)"me"));
    }

    @Test
    public void constraintsWereHittingInKeySelectionBeforeJwtConsumerSetThemToBeOkay() throws Exception {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("me");
        claims.setExpirationTimeMinutesInTheFuture(5.0f);
        claims.setAudience("the audience");
        claims.setIssuer("the issuer");
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);
        jws.setAlgorithmHeaderValue("none");
        String jwt = jws.getCompactSerialization();
        JwksVerificationKeyResolver jwksVerificationKeyResolver = new JwksVerificationKeyResolver(Collections.emptyList());
        VerificationKeyResolver resolver = new VerificationKeyResolver(){

            @Override
            public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) {
                try {
                    return jwksVerificationKeyResolver.resolveKey(jws, nestingContext);
                }
                catch (UnresolvableKeyException e) {
                    return null;
                }
            }
        };
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setExpectedAudience("the audience").setExpectedIssuer("the issuer").setRequireExpirationTime().setVerificationKeyResolver(resolver).setJwsAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS).setDisableRequireSignature().build();
        JwtContext ctx = jwtConsumer.process(jwt);
        Assert.assertThat((Object)ctx.getJwtClaims().getSubject(), (Matcher)CoreMatchers.equalTo((Object)"me"));
    }

    static /* synthetic */ void access$0(JwtConsumerTest jwtConsumerTest, String string, String string2, String string3) throws Exception {
        jwtConsumerTest.littleJweRoundTrip(string, string2, string3);
    }
}

