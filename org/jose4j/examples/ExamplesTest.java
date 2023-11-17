/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Test
 */
package org.jose4j.examples;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.List;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.keys.X509Util;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.X509VerificationKeyResolver;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.junit.Test;

public class ExamplesTest {
    @Test
    public void nestedJwtRoundTripExample() throws JoseException, InvalidJwtException, MalformedClaimException {
        EllipticCurveJsonWebKey senderJwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        senderJwk.setKeyId("sender's key");
        EllipticCurveJsonWebKey receiverJwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        receiverJwk.setKeyId("receiver's key");
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("sender");
        claims.setAudience("receiver");
        claims.setExpirationTimeMinutesInTheFuture(10.0f);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(2.0f);
        claims.setSubject("subject");
        claims.setClaim("email", "mail@example.com");
        List<String> groups = Arrays.asList("group-1", "other-group", "group-3");
        claims.setStringListClaim("groups", groups);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(senderJwk.getPrivateKey());
        jws.setKeyIdHeaderValue(senderJwk.getKeyId());
        jws.setAlgorithmHeaderValue("ES256");
        String innerJwt = jws.getCompactSerialization();
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setAlgorithmHeaderValue("ECDH-ES+A128KW");
        String encAlg = "A128CBC-HS256";
        jwe.setEncryptionMethodHeaderParameter(encAlg);
        jwe.setKey(receiverJwk.getPublicKey());
        jwe.setKeyIdHeaderValue(receiverJwk.getKeyId());
        jwe.setContentTypeHeaderValue("JWT");
        jwe.setPayload(innerJwt);
        String jwt = jwe.getCompactSerialization();
        System.out.println("JWT: " + jwt);
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime().setMaxFutureValidityInMinutes(300).setRequireSubject().setExpectedIssuer("sender").setExpectedAudience("receiver").setDecryptionKey(receiverJwk.getPrivateKey()).setVerificationKey(senderJwk.getPublicKey()).build();
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            System.out.println("JWT validation succeeded! " + jwtClaims);
        }
        catch (InvalidJwtException e) {
            System.out.println("Invalid JWT! " + e);
        }
    }

    @Test
    public void jwtRoundTripExample() throws JoseException, InvalidJwtException, MalformedClaimException {
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        rsaJsonWebKey.setKeyId("k1");
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("Issuer");
        claims.setAudience("Audience");
        claims.setExpirationTimeMinutesInTheFuture(10.0f);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(2.0f);
        claims.setSubject("subject");
        claims.setClaim("email", "mail@example.com");
        List<String> groups = Arrays.asList("group-one", "other-group", "group-three");
        claims.setStringListClaim("groups", groups);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue("RS256");
        String jwt = jws.getCompactSerialization();
        System.out.println("JWT: " + jwt);
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime().setAllowedClockSkewInSeconds(30).setRequireSubject().setExpectedIssuer("Issuer").setExpectedAudience("Audience").setVerificationKey(rsaJsonWebKey.getKey()).build();
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            System.out.println("JWT validation succeeded! " + jwtClaims);
        }
        catch (InvalidJwtException e) {
            System.out.println("Invalid JWT! " + e);
        }
        HttpsJwks httpsJkws = new HttpsJwks("https://example.com/jwks");
        HttpsJwksVerificationKeyResolver httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
        jwtConsumer = new JwtConsumerBuilder().setVerificationKeyResolver(httpsJwksKeyResolver).build();
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(rsaJsonWebKey);
        JwksVerificationKeyResolver jwksResolver = new JwksVerificationKeyResolver(jsonWebKeySet.getJsonWebKeys());
        jwtConsumer = new JwtConsumerBuilder().setVerificationKeyResolver(jwksResolver).build();
        X509Util x509Util = new X509Util();
        X509Certificate certificate = x509Util.fromBase64Der("MIIDQjCCAiqgAwIBAgIGATz/FuLiMA0GCSqGSIb3DQEBBQUAMGIxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDTzEPMA0GA1UEBxMGRGVudmVyMRwwGgYDVQQKExNQaW5nIElkZW50aXR5IENvcnAuMRcwFQYDVQQDEw5CcmlhbiBDYW1wYmVsbDAeFw0xMzAyMjEyMzI5MTVaFw0xODA4MTQyMjI5MTVaMGIxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDTzEPMA0GA1UEBxMGRGVudmVyMRwwGgYDVQQKExNQaW5nIElkZW50aXR5IENvcnAuMRcwFQYDVQQDEw5CcmlhbiBDYW1wYmVsbDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAL64zn8/QnHYMeZ0LncoXaEde1fiLm1jHjmQsF/449IYALM9if6amFtPDy2yvz3YlRij66s5gyLCyO7ANuVRJx1NbgizcAblIgjtdf/u3WG7K+IiZhtELto/A7Fck9Ws6SQvzRvOE8uSirYbgmj6He4iO8NCyvaK0jIQRMMGQwsU1quGmFgHIXPLfnpnfajr1rVTAwtgV5LEZ4Iel+W1GC8ugMhyr4/p1MtcIM42EA8BzE6ZQqC7VPqPvEjZ2dbZkaBhPbiZAS3YeYBRDWm1p1OZtWamT3cEvqqPpnjL1XyW+oyVVkaZdklLQp2Btgt9qr21m42f4wTw+Xrp6rCKNb0CAwEAATANBgkqhkiG9w0BAQUFAAOCAQEAh8zGlfSlcI0o3rYDPBB07aXNswb4ECNIKG0CETTUxmXl9KUL+9gGlqCz5iWLOgWsnrcKcY0vXPG9J1r9AqBNTqNgHq2G03X09266X5CpOe1zFo+Owb1zxtp3PehFdfQJ610CDLEaS9V9Rqp17hCyybEpOGVwe8fnk+fbEL2Bo3UPGrpsHzUoaGpDftmWssZkhpBJKVMJyf/RuP2SmmaIzmnw9JiSlYhzo4tpzd5rFXhjRbg4zW9C+2qok+2+qDM1iJ684gPHMIY8aLWrdgQTxkumGmTqgawR+N5MDtdPTEQ0XfIBc2cJEUyMTY5MPvACWpkA6SdS4xSvdXK3IVfOWA==");
        X509Certificate otherCertificate = x509Util.fromBase64Der("MIICUDCCAbkCBETczdcwDQYJKoZIhvcNAQEFBQAwbzELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkNPMQ8wDQYDVQQHEwZEZW52ZXIxFTATBgNVBAoTDFBpbmdJZGVudGl0eTEXMBUGA1UECxMOQnJpYW4gQ2FtcGJlbGwxEjAQBgNVBAMTCWxvY2FsaG9zdDAeFw0wNjA4MTExODM1MDNaFw0zMzEyMjcxODM1MDNaMG8xCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDTzEPMA0GA1UEBxMGRGVudmVyMRUwEwYDVQQKEwxQaW5nSWRlbnRpdHkxFzAVBgNVBAsTDkJyaWFuIENhbXBiZWxsMRIwEAYDVQQDEwlsb2NhbGhvc3QwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAJLrpeiY/Ai2gGFxNY8Tm/QSO8qgPOGKDMAT08QMyHRlxW8fpezfBTAtKcEsztPzwYTLWmf6opfJT+5N6cJKacxWchn/dRrzV2BoNuz1uo7wlpRqwcaOoi6yHuopNuNO1ms1vmlv3POq5qzMe6c1LRGADyZhi0KejDX6+jVaDiUTAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAMojbPEYJiIWgQzZcQJCQeodtKSJl5+lA8MWBBFFyZmvZ6jUYglIQdLlc8Pu6JF2j/hZEeTI87z/DOT6UuqZA83gZcy6re4wMnZvY2kWX9CsVWDCaZhnyhjBNYfhcOf0ZychoKShaEpTQ5UAGwvYYcbqIWC04GAZYVsZxlPl9hoA=");
        X509VerificationKeyResolver x509VerificationKeyResolver = new X509VerificationKeyResolver(certificate, otherCertificate);
        x509VerificationKeyResolver.setTryAllOnNoThumbHeader(true);
        jwtConsumer = new JwtConsumerBuilder().setVerificationKeyResolver(x509VerificationKeyResolver).build();
        jws.setX509CertSha1ThumbprintHeaderValue(certificate);
        JwtConsumer firstPassJwtConsumer = new JwtConsumerBuilder().setSkipAllValidators().setDisableRequireSignature().setSkipSignatureVerification().build();
        JwtContext jwtContext = firstPassJwtConsumer.process(jwt);
        String issuer = jwtContext.getJwtClaims().getIssuer();
        Key verificationKey = rsaJsonWebKey.getKey();
        JwtConsumer secondPassJwtConsumer = new JwtConsumerBuilder().setExpectedIssuer(issuer).setVerificationKey(verificationKey).setRequireExpirationTime().setAllowedClockSkewInSeconds(30).setRequireSubject().setExpectedAudience("Audience").build();
        secondPassJwtConsumer.processContext(jwtContext);
    }

    @Test
    public void jwsSigningExample() throws JoseException {
        String examplePayload = "This is some text that is to be signed.";
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(examplePayload);
        jws.setAlgorithmHeaderValue("ES256");
        ECPrivateKey privateKey = ExampleEcKeysFromJws.PRIVATE_256;
        jws.setKey(privateKey);
        String jwsCompactSerialization = jws.getCompactSerialization();
        System.out.println(jwsCompactSerialization);
    }

    @Test
    public void jwsVerificationExample() throws JoseException {
        String compactSerialization = "eyJhbGciOiJFUzI1NiJ9.VGhpcyBpcyBzb21lIHRleHQgdGhhdCBpcyB0byBiZSBzaWduZWQu.GHiNd8EgKa-2A4yJLHyLCqlwoSxwqv2rzGrvUTxczTYDBeUHUwQRB3P0dp_DALL0jQIDz2vQAT_cnWTIW98W_A";
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(compactSerialization);
        ECPublicKey publicKey = ExampleEcKeysFromJws.PUBLIC_256;
        jws.setKey(publicKey);
        boolean signatureVerified = jws.verifySignature();
        System.out.println("JWS Signature is valid: " + signatureVerified);
        String payload = jws.getPayload();
        System.out.println("JWS payload: " + payload);
    }

    @Test
    public void parseJwksAndVerifyJwsExample() throws JoseException {
        String jsonWebKeySetJson = "{\"keys\":[{\"kty\":\"EC\",\"use\":\"sig\",\"kid\":\"the key\",\"x\":\"amuk6RkDZi-48mKrzgBN_zUZ_9qupIwTZHJjM03qL-4\",\"y\":\"ZOESj6_dpPiZZR-fJ-XVszQta28Cjgti7JudooQJ0co\",\"crv\":\"P-256\"},{\"kty\":\"EC\",\"use\":\"sig\", \"kid\":\"other key\",\"x\":\"eCNZgiEHUpLaCNgYIcvWzfyBlzlaqEaWbt7RFJ4nIBA\",\"y\":\"UujFME4pNk-nU4B9h4hsetIeSAzhy8DesBgWppiHKPM\",\"crv\":\"P-256\"}]}";
        String compactSerialization = "eyJhbGciOiJFUzI1NiIsImtpZCI6InRoZSBrZXkifQ.UEFZTE9BRCE.Oq-H1lk5G0rl6oyNM3jR5S0-BZQgTlamIKMApq3RX8Hmh2d2XgB4scvsMzGvE-OlEmDY9Oy0YwNGArLpzXWyjw";
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(compactSerialization);
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jsonWebKeySetJson);
        VerificationJwkSelector jwkSelector = new VerificationJwkSelector();
        JsonWebKey jwk = jwkSelector.select(jws, jsonWebKeySet.getJsonWebKeys());
        jws.setKey(jwk.getKey());
        boolean signatureVerified = jws.verifySignature();
        System.out.println("JWS Signature is valid: " + signatureVerified);
        String payload = jws.getPayload();
        System.out.println("JWS payload: " + payload);
    }

    @Test
    public void jweRoundTripExample() throws JoseException {
        String message = "Well, as of this moment, they're on DOUBLE SECRET PROBATION!";
        String jwkJson = "{\"kty\":\"oct\",\"k\":\"Fdh9u8rINxfivbrianbbVT1u232VQBZYKx1HGAGPt2I\"}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(jwkJson);
        JsonWebEncryption senderJwe = new JsonWebEncryption();
        senderJwe.setPlaintext(message);
        senderJwe.setAlgorithmHeaderValue("dir");
        senderJwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        senderJwe.setKey(jwk.getKey());
        String compactSerialization = senderJwe.getCompactSerialization();
        System.out.println("JWE compact serialization: " + compactSerialization);
        JsonWebEncryption receiverJwe = new JsonWebEncryption();
        receiverJwe.setCompactSerialization(compactSerialization);
        receiverJwe.setKey(jwk.getKey());
        String plaintext = receiverJwe.getPlaintextString();
        System.out.println("plaintext: " + plaintext);
    }

    @Test
    public void helloWorld() throws JoseException {
        AesKey key = new AesKey(ByteUtil.randomBytes(16));
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setPayload("Hello World!");
        jwe.setAlgorithmHeaderValue("A128KW");
        jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        jwe.setKey(key);
        String serializedJwe = jwe.getCompactSerialization();
        System.out.println("Serialized Encrypted JWE: " + serializedJwe);
        jwe = new JsonWebEncryption();
        jwe.setKey(key);
        jwe.setCompactSerialization(serializedJwe);
        System.out.println("Payload: " + jwe.getPayload());
    }
}

