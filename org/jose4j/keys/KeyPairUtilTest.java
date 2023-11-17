/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.keys;

import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.EcKeyUtil;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.keys.ExampleRsaJwksFromJwe;
import org.jose4j.keys.KeyPairUtil;
import org.jose4j.keys.RsaKeyUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyPairUtilTest {
    @Test
    public void rsaPublicKeyEncodingDecodingAndSign() throws Exception {
        PublicJsonWebKey publicJsonWebKey = ExampleRsaJwksFromJwe.APPENDIX_A_1;
        String pem = KeyPairUtil.pemEncode(publicJsonWebKey.getPublicKey());
        String expectedPem = "-----BEGIN PUBLIC KEY-----\r\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoahUIoWw0K0usKNuOR6H\r\n4wkf4oBUXHTxRvgb48E+BVvxkeDNjbC4he8rUWcJoZmds2h7M70imEVhRU5djINX\r\ntqllXI4DFqcI1DgjT9LewND8MW2Krf3Spsk/ZkoFnilakGygTwpZ3uesH+PFABNI\r\nUYpOiN15dsQRkgr0vEhxN92i2asbOenSZeyaxziK72UwxrrKoExv6kc5twXTq4h+\r\nQChLOln0/mtUZwfsRaMStPs6mS6XrgxnxbWhojf663tuEQueGC+FCMfra36C9knD\r\nFGzKsNa7LZK2djYgyD3JR/MB/4NUJW/TqOQtwHYbxevoJArm+L5StowjzGy+/bq6\r\nGwIDAQAB\r\n-----END PUBLIC KEY-----";
        Assert.assertThat((Object)pem, (Matcher)CoreMatchers.equalTo((Object)expectedPem));
        RsaKeyUtil rsaKeyUtil = new RsaKeyUtil();
        PublicKey publicKey = rsaKeyUtil.fromPemEncoded(pem);
        Assert.assertThat((Object)publicKey, (Matcher)CoreMatchers.equalTo((Object)publicJsonWebKey.getPublicKey()));
        JwtClaims claims = new JwtClaims();
        claims.setSubject("meh");
        claims.setExpirationTimeMinutesInTheFuture(20.0f);
        claims.setGeneratedJwtId();
        claims.setAudience("you");
        claims.setIssuer("me");
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(publicJsonWebKey.getPrivateKey());
        jws.setAlgorithmHeaderValue("RS256");
        String jwt = jws.getCompactSerialization();
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.debug("The following JWT and public key should be (and were on 11/11/15) usable and produce a valid result at jwt.io (related to http://stackoverflow.com/questions/32744172):\n" + jwt + "\n" + pem);
    }

    @Test
    public void ecPublicKeyEncoding() throws Exception {
        ECPublicKey public256 = ExampleEcKeysFromJws.PUBLIC_256;
        String pemed = KeyPairUtil.pemEncode(public256);
        EcKeyUtil ecKeyUtil = new EcKeyUtil();
        PublicKey publicKey = ecKeyUtil.fromPemEncoded(pemed);
        Assert.assertThat((Object)publicKey, (Matcher)CoreMatchers.equalTo((Object)public256));
    }
}

