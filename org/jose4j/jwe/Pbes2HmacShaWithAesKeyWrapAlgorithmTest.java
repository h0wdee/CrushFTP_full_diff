/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwe;

import java.security.Key;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContextTest;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.Pbes2HmacShaWithAesKeyWrapAlgorithm;
import org.jose4j.jwx.Headers;
import org.jose4j.keys.PbkdfKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class Pbes2HmacShaWithAesKeyWrapAlgorithmTest {
    public static final int MINIMUM_ITERAION_COUNT = 1000;
    public static final int MINIMUM_SALT_BYTE_LENGTH = 8;

    @Test
    public void combinationOfRoundTrips() throws Exception {
        String[] algs = new String[]{"PBES2-HS256+A128KW", "PBES2-HS384+A192KW", "PBES2-HS256+A128KW"};
        String[] encs = new String[]{"A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512"};
        String password = "password";
        String plaintext = "<insert some witty quote or remark here>";
        String[] stringArray = algs;
        int n = algs.length;
        int n2 = 0;
        while (n2 < n) {
            String alg = stringArray[n2];
            String[] stringArray2 = encs;
            int n3 = encs.length;
            int n4 = 0;
            while (n4 < n3) {
                String enc = stringArray2[n4];
                JsonWebEncryption encryptingJwe = new JsonWebEncryption();
                encryptingJwe.setAlgorithmHeaderValue(alg);
                encryptingJwe.setEncryptionMethodHeaderParameter(enc);
                encryptingJwe.setPayload(plaintext);
                encryptingJwe.setKey(new PbkdfKey(password));
                String compactSerialization = encryptingJwe.getCompactSerialization();
                JsonWebEncryption decryptingJwe = new JsonWebEncryption();
                decryptingJwe.setCompactSerialization(compactSerialization);
                decryptingJwe.setKey(new PbkdfKey(password));
                Assert.assertThat((Object)plaintext, (Matcher)CoreMatchers.equalTo((Object)decryptingJwe.getPayload()));
                ++n4;
            }
            ++n2;
        }
    }

    @Test(expected=InvalidKeyException.class)
    public void testNullKey() throws JoseException {
        JsonWebEncryption encryptingJwe = new JsonWebEncryption();
        encryptingJwe.setAlgorithmHeaderValue("PBES2-HS256+A128KW");
        encryptingJwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        encryptingJwe.setPayload("meh");
        encryptingJwe.getCompactSerialization();
    }

    @Test
    public void testDefaultsMeetMinimumRequiredOrSuggested() throws JoseException {
        JsonWebEncryption encryptingJwe = new JsonWebEncryption();
        encryptingJwe.setAlgorithmHeaderValue("PBES2-HS256+A128KW");
        encryptingJwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        encryptingJwe.setPayload("meh");
        PbkdfKey key = new PbkdfKey("passtheword");
        encryptingJwe.setKey(key);
        String compactSerialization = encryptingJwe.getCompactSerialization();
        System.out.println(compactSerialization);
        JsonWebEncryption decryptingJwe = new JsonWebEncryption();
        decryptingJwe.setCompactSerialization(compactSerialization);
        decryptingJwe.setKey(key);
        decryptingJwe.getPayload();
        Headers headers = decryptingJwe.getHeaders();
        Long iterationCount = headers.getLongHeaderValue("p2c");
        Assert.assertTrue((iterationCount >= 1000L ? 1 : 0) != 0);
        String saltInputString = headers.getStringHeaderValue("p2s");
        Base64Url b = new Base64Url();
        byte[] saltInput = b.base64UrlDecode(saltInputString);
        Assert.assertTrue((saltInput.length >= 8 ? 1 : 0) != 0);
    }

    @Test
    public void testUsingAndSettingDefaults() throws JoseException {
        Pbes2HmacShaWithAesKeyWrapAlgorithm.HmacSha256Aes128 pbes2 = new Pbes2HmacShaWithAesKeyWrapAlgorithm.HmacSha256Aes128();
        Assert.assertTrue((pbes2.getDefaultIterationCount() >= 1000L ? 1 : 0) != 0);
        Assert.assertTrue((pbes2.getDefaultSaltByteLength() >= 8 ? 1 : 0) != 0);
        PbkdfKey key = new PbkdfKey("a password");
        Headers headers = new Headers();
        Key derivedKey = pbes2.deriveForEncrypt(key, headers, ProviderContextTest.EMPTY_CONTEXT);
        Assert.assertThat((Object)derivedKey.getEncoded().length, (Matcher)CoreMatchers.equalTo((Object)16));
        String saltInputString = headers.getStringHeaderValue("p2s");
        byte[] saltInput = Base64Url.decode(saltInputString);
        Assert.assertThat((Object)saltInput.length, (Matcher)CoreMatchers.equalTo((Object)pbes2.getDefaultSaltByteLength()));
        Long iterationCount = headers.getLongHeaderValue("p2c");
        Assert.assertThat((Object)iterationCount, (Matcher)CoreMatchers.equalTo((Object)pbes2.getDefaultIterationCount()));
        Pbes2HmacShaWithAesKeyWrapAlgorithm.HmacSha256Aes128 newPbes2 = new Pbes2HmacShaWithAesKeyWrapAlgorithm.HmacSha256Aes128();
        long newDefaultIterationCount = 1024L;
        newPbes2.setDefaultIterationCount(newDefaultIterationCount);
        int newDefaultSaltByteLength = 16;
        newPbes2.setDefaultSaltByteLength(newDefaultSaltByteLength);
        headers = new Headers();
        derivedKey = newPbes2.deriveForEncrypt(key, headers, ProviderContextTest.EMPTY_CONTEXT);
        saltInputString = headers.getStringHeaderValue("p2s");
        saltInput = Base64Url.decode(saltInputString);
        Assert.assertThat((Object)saltInput.length, (Matcher)CoreMatchers.equalTo((Object)newDefaultSaltByteLength));
        iterationCount = headers.getLongHeaderValue("p2c");
        Assert.assertThat((Object)iterationCount, (Matcher)CoreMatchers.equalTo((Object)newDefaultIterationCount));
        Assert.assertThat((Object)derivedKey.getEncoded().length, (Matcher)CoreMatchers.equalTo((Object)16));
    }

    @Test
    public void testSettingSaltAndIterationCount() throws JoseException {
        String password = "secret word";
        String plaintext = "<insert some witty quote or remark here, again>";
        JsonWebEncryption encryptingJwe = new JsonWebEncryption();
        int saltByteLength = 32;
        String saltInputString = Base64Url.encode(ByteUtil.randomBytes(saltByteLength));
        encryptingJwe.getHeaders().setStringHeaderValue("p2s", saltInputString);
        long iterationCount = 1024L;
        encryptingJwe.getHeaders().setObjectHeaderValue("p2c", iterationCount);
        encryptingJwe.setAlgorithmHeaderValue("PBES2-HS384+A192KW");
        encryptingJwe.setEncryptionMethodHeaderParameter("A192CBC-HS384");
        encryptingJwe.setPayload(plaintext);
        encryptingJwe.setKey(new PbkdfKey(password));
        String compactSerialization = encryptingJwe.getCompactSerialization();
        JsonWebEncryption decryptingJwe = new JsonWebEncryption();
        decryptingJwe.setCompactSerialization(compactSerialization);
        decryptingJwe.setKey(new PbkdfKey(password));
        Assert.assertThat((Object)plaintext, (Matcher)CoreMatchers.equalTo((Object)decryptingJwe.getPayload()));
        String saltInputStringFromHeader = decryptingJwe.getHeader("p2s");
        Assert.assertThat((Object)saltInputString, (Matcher)CoreMatchers.equalTo((Object)saltInputStringFromHeader));
        Assert.assertThat((Object)saltByteLength, (Matcher)CoreMatchers.equalTo((Object)Base64Url.decode(saltInputStringFromHeader).length));
        long iterationCountFromHeader = decryptingJwe.getHeaders().getLongHeaderValue("p2c");
        Assert.assertThat((Object)iterationCount, (Matcher)CoreMatchers.equalTo((Object)iterationCountFromHeader));
    }
}

