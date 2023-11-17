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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.EcdsaUsingShaAlgorithm;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class EcdsaUsingShaAlgorithmTest {
    @Test
    public void testEncodingDecoding() throws IOException {
        int[] rints = new int[]{14, 209, 33, 83, 121, 99, 108, 72, 60, 47, 127, 21, 88, 7, 212, 2, 163, 178, 40, 3, 58, 249, 124, 126, 23, 129, 154, 195, 22, 158, 166, 101};
        int[] sints = new int[]{197, 10, 7, 211, 140, 60, 112, 229, 216, 241, 45, 175, 8, 74, 84, 128, 166, 101, 144, 197, 242, 147, 80, 154, 143, 63, 127, 138, 131, 163, 84, 213};
        byte[] rbytes = ByteUtil.convertUnsignedToSignedTwosComp(rints);
        byte[] sbytes = ByteUtil.convertUnsignedToSignedTwosComp(sints);
        int capacity = 64;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(rbytes);
        buffer.put(sbytes);
        byte[] concatedBytes = buffer.array();
        byte[] derEncoded = EcdsaUsingShaAlgorithm.convertConcatenatedToDer(concatedBytes);
        Assert.assertFalse((boolean)Arrays.equals(concatedBytes, derEncoded));
        byte[] backToConcated = EcdsaUsingShaAlgorithm.convertDerToConcatenated(derEncoded, capacity);
        Assert.assertTrue((boolean)Arrays.equals(concatedBytes, backToConcated));
    }

    @Test
    public void simpleConcatenationWithLength() throws IOException {
        byte[] noPad = new byte[]{1, 2};
        byte[] der = EcdsaUsingShaAlgorithm.convertConcatenatedToDer(noPad);
        int outputLength = 16;
        byte[] concatenated = EcdsaUsingShaAlgorithm.convertDerToConcatenated(der, outputLength);
        Assert.assertThat((Object)outputLength, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)concatenated.length)));
        Assert.assertThat((Object)concatenated[7], (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)noPad[0])));
        Assert.assertThat((Object)concatenated[15], (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)noPad[1])));
        System.out.println(Arrays.toString(concatenated));
    }

    @Test
    public void simpleConcatenationWithDiffLengths() throws IOException {
        byte[] byArray = new byte[8];
        byArray[4] = 1;
        byArray[5] = 1;
        byArray[6] = 1;
        byArray[7] = 1;
        byte[] a = byArray;
        byte[] b = new byte[]{2, 2, 2, 2, 2, 2, 2, 2};
        byte[] der = EcdsaUsingShaAlgorithm.convertConcatenatedToDer(ByteUtil.concat(a, b));
        int outputLength = 16;
        byte[] concatenated = EcdsaUsingShaAlgorithm.convertDerToConcatenated(der, outputLength);
        Assert.assertThat((Object)outputLength, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)concatenated.length)));
        int halfLength = outputLength / 2;
        byte[] first = ByteUtil.subArray(concatenated, 0, halfLength);
        byte[] second = ByteUtil.subArray(concatenated, halfLength, halfLength);
        Assert.assertArrayEquals((byte[])a, (byte[])first);
        Assert.assertArrayEquals((byte[])b, (byte[])second);
    }

    @Test
    public void simpleConcatenationWithVeryDiffLengths() throws IOException {
        byte[] byArray = new byte[16];
        byArray[15] = 1;
        byte[] a = byArray;
        byte[] b = new byte[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
        byte[] der = EcdsaUsingShaAlgorithm.convertConcatenatedToDer(ByteUtil.concat(a, b));
        int outputLength = 32;
        byte[] concatenated = EcdsaUsingShaAlgorithm.convertDerToConcatenated(der, outputLength);
        Assert.assertThat((Object)outputLength, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)concatenated.length)));
        int halfLength = outputLength / 2;
        byte[] first = ByteUtil.subArray(concatenated, 0, halfLength);
        byte[] second = ByteUtil.subArray(concatenated, halfLength, halfLength);
        Assert.assertArrayEquals((byte[])a, (byte[])first);
        Assert.assertArrayEquals((byte[])b, (byte[])second);
    }

    @Test
    public void tooShortPreviously() throws Exception {
        String encoded = "7w6JjwMqcWmTFaZfrOc5kSSj5WOi0vDbMoGqcLWUL5QrTmJ_KOPMkNOjNll4pRITxuyZo_owOswnDM4dYdS7ypoPHOL13XDfdffG7sdwjXA6JthsItlk6l43Xtqt2ytJKqUMC-J7K5Cn1izOeuqzsI18Go9jcEEw5eUdQhR77OjfCA";
        byte[] decoded = Base64Url.decode(encoded);
        byte[] der = EcdsaUsingShaAlgorithm.convertConcatenatedToDer(decoded);
        int outputLength = 132;
        byte[] concatenated = EcdsaUsingShaAlgorithm.convertDerToConcatenated(der, outputLength);
        Assert.assertThat((Object)outputLength, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)concatenated.length)));
        Assert.assertThat((Object)0, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)concatenated[0])));
        Assert.assertThat((Object)0, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)concatenated[66])));
    }

    @Test
    public void backwardCompatibility() throws JoseException {
        String jwk = "{\"kty\":\"EC\",\"x\":\"APlFpj7M-Kj8ArsYMbJS-6rn1XkugUwngk_iTVe_KfLs6pVIb4LYz-gJ2SytwsoNkSbwq6NuNXB3kFsiYXmG0pf2\",\"y\":\"AebLEK2Hn_vLyDFCzQYGBrGF7eJPh2b01vZ_rK1UOXT9slDvNFK5y6yUSkG4qrVg5P0xwuw25AReYwtvwYQr8uvV\",\"crv\":\"P-521\",\"d\":\"AL-txDgStuoyYEJ3-NyMNeTjlwcoQxbck659Snelqza-Vhd166l3Bfh4A0o42DqetfknQBeE-upPEliNEtEvv9dN\"}";
        String cs = "eyJhbGciOiJFUzUxMiJ9.ZG9lcyBpdCBtYXR0ZXIgd2hhdCdzIGluIGhlcmU_IEkgZG9uOyd0IGtub3cuLi4.zv6B3bm8xz6EKfQaaW-0sVVD7MYoym-cXrq2SaDGI9_EZkP244jQk1xtyX6uK8JlSXXRlYR7WJ2rCM8NOr_ZHB5b7VaJnOnJkzRnh3-ncI46Dhj-cbqsVqZvvylkWDxhoodVkhAPT2wnkbfS6mYHjmYzWI1YF2ub5klAunLjn8jFdg";
        this.check(jwk, cs);
        jwk = "{\"kty\":\"EC\",\"x\":\"ACDqsfERDEacSJUa-3M2TxIp05yVHl5yuURP0WhZvi4xfMiRsyqooEWhA9PtHEko1ELvaM0bR0hNavo597HtP5_q\",\"y\":\"AW90m8N4e9YUwYG-Yxkf5T2rR5fiECj-A0p1DVUJNJ8BFPr5OGG1z3GO_PMxC-7LCj8gfqr6Wc8a1ViqIt6OE8Nr\",\"crv\":\"P-521\",\"d\":\"AGS5ZSjsn_ou9mqkutgJAUKz5Hx7XATfHvNTUv_1CAHN08LVBU_1R2TEtJanWe72w3d22ylwHTPoogAbRQdhTyYC\"}\n";
        cs = "eyJhbGciOiJFUzUxMiJ9.ZG9lcyBpdCBtYXR0ZXIgd2hhdCdzIGluIGhlcmU_IEkgZG9uOyd0IGtub3cuLi4.k-m9qenb1rrmhpavhQ6PeklKRXn7Tu7J9Asycgj4gUELLTGHE96Di5_euQF0avKkVrorDuDdtzi-q0hnzq38ArKTpbkjRqdMonQdhFTXroP6HCkSrlSWFUTxvtsoaa-VorugOxPe1wZSHafmaWotbqDJ2jXA3sSC1H3jVxx1SxXGRg";
        this.check(jwk, cs);
        jwk = "{\"kty\":\"EC\",\"x\":\"AQ8WdkBzMgfuWCWvGIpGkyi-DZgw4a1wmTZVg9YjUzSUj8NKLDcYnUgsr4op7z8dW8WUib6dC4EGXISaye1Svp6S\",\"y\":\"AMr47PiklLy_Py-QgB1jOsoVlbujFwDuM6vdTorColeNVWw2FQi-oUN-Pt8ga9mD1LDgAC96lTSybpgTu9G1P_ir\",\"crv\":\"P-521\",\"d\":\"AaDOIsjeA20NpIDcQN6yBZ-I1XEOQSsolqsZBSWllmNjVfefggm-Erjz4UdWrgKVdZNlD5px3i5L30dhWZc-45kC\"}\n";
        cs = "eyJhbGciOiJFUzUxMiJ9.ZG9lcyBpdCBtYXR0ZXIgd2hhdCdzIGluIGhlcmU_IEkgZG9uOyd0IGtub3cuLi4.waSI2xpnm4zQeAyyRLDmoq5nf_tj9SoSxLvXWcYhpNX56UVM3PyyCkX5aIzGH25kJ-W-10QzF-tR8PoIHxlNEMgfJFGHW4Bjexe-juNyvnETJbDyipP_i4t0wuUIVJ1J43ihHvLhXiWgfivNjwfVikMC3mTWdyzUxwrjG4M0XaUC-w";
        this.check(jwk, cs);
    }

    private void check(String jwkJson, String cs) throws JoseException {
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(jwkJson);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(cs);
        jws.setKey(jwk.getKey());
        Assert.assertTrue((boolean)jws.verifySignature());
    }
}

