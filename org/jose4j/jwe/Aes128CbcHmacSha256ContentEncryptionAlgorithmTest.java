/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwe;

import junit.framework.TestCase;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContextTest;
import org.jose4j.jwe.AesCbcHmacSha2ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionParts;
import org.jose4j.jwx.Headers;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;

public class Aes128CbcHmacSha256ContentEncryptionAlgorithmTest
extends TestCase {
    public void testExampleEncryptFromJweAppendix2() throws JoseException {
        String plainTextText = "Live long and prosper.";
        byte[] plainText = StringUtil.getBytesUtf8(plainTextText);
        String encodedHeader = "eyJhbGciOiJSU0ExXzUiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0";
        Base64Url base64url = new Base64Url();
        Headers headers = new Headers();
        headers.setFullHeaderAsJsonString(base64url.base64UrlDecodeToUtf8String(encodedHeader));
        byte[] aad = StringUtil.getBytesAscii(encodedHeader);
        int[] nArray = new int[32];
        nArray[0] = 4;
        nArray[1] = 211;
        nArray[2] = 31;
        nArray[3] = 197;
        nArray[4] = 84;
        nArray[5] = 157;
        nArray[6] = 252;
        nArray[7] = 254;
        nArray[8] = 11;
        nArray[9] = 100;
        nArray[10] = 157;
        nArray[11] = 250;
        nArray[12] = 63;
        nArray[13] = 170;
        nArray[14] = 106;
        nArray[15] = 206;
        nArray[16] = 107;
        nArray[17] = 124;
        nArray[18] = 212;
        nArray[19] = 45;
        nArray[20] = 111;
        nArray[21] = 107;
        nArray[22] = 9;
        nArray[23] = 219;
        nArray[24] = 200;
        nArray[25] = 177;
        nArray[27] = 240;
        nArray[28] = 143;
        nArray[29] = 156;
        nArray[30] = 44;
        nArray[31] = 207;
        int[] ints = nArray;
        byte[] contentEncryptionKeyBytes = ByteUtil.convertUnsignedToSignedTwosComp(ints);
        byte[] iv = ByteUtil.convertUnsignedToSignedTwosComp(new int[]{3, 22, 60, 12, 43, 67, 104, 105, 108, 108, 105, 99, 111, 116, 104, 101});
        AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256 jweContentEncryptionAlg = new AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256();
        ContentEncryptionParts contentEncryptionParts = jweContentEncryptionAlg.encrypt(plainText, aad, contentEncryptionKeyBytes, iv, headers, ProviderContextTest.EMPTY_CONTEXT);
        Base64Url base64Url = new Base64Url();
        byte[] ciphertext = contentEncryptionParts.getCiphertext();
        String encodedJweCiphertext = "KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY";
        Aes128CbcHmacSha256ContentEncryptionAlgorithmTest.assertEquals((String)encodedJweCiphertext, (String)base64Url.base64UrlEncode(ciphertext));
        byte[] authenticationTag = contentEncryptionParts.getAuthenticationTag();
        String encodedAuthenticationTag = "9hH0vgRfYgPnAHOd8stkvw";
        Aes128CbcHmacSha256ContentEncryptionAlgorithmTest.assertEquals((String)encodedAuthenticationTag, (String)base64Url.base64UrlEncode(authenticationTag));
    }

    public void testExampleDecryptFromJweAppendix2() throws JoseException {
        int[] nArray = new int[32];
        nArray[0] = 4;
        nArray[1] = 211;
        nArray[2] = 31;
        nArray[3] = 197;
        nArray[4] = 84;
        nArray[5] = 157;
        nArray[6] = 252;
        nArray[7] = 254;
        nArray[8] = 11;
        nArray[9] = 100;
        nArray[10] = 157;
        nArray[11] = 250;
        nArray[12] = 63;
        nArray[13] = 170;
        nArray[14] = 106;
        nArray[15] = 206;
        nArray[16] = 107;
        nArray[17] = 124;
        nArray[18] = 212;
        nArray[19] = 45;
        nArray[20] = 111;
        nArray[21] = 107;
        nArray[22] = 9;
        nArray[23] = 219;
        nArray[24] = 200;
        nArray[25] = 177;
        nArray[27] = 240;
        nArray[28] = 143;
        nArray[29] = 156;
        nArray[30] = 44;
        nArray[31] = 207;
        int[] ints = nArray;
        byte[] contentEncryptionKeyBytes = ByteUtil.convertUnsignedToSignedTwosComp(ints);
        Base64Url b = new Base64Url();
        String encodedHeader = "eyJhbGciOiJSU0ExXzUiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0";
        Headers headers = new Headers();
        headers.setFullHeaderAsJsonString(Base64Url.decodeToUtf8String(encodedHeader));
        byte[] header = StringUtil.getBytesUtf8(encodedHeader);
        byte[] iv = b.base64UrlDecode("AxY8DCtDaGlsbGljb3RoZQ");
        byte[] ciphertext = b.base64UrlDecode("KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY");
        byte[] tag = b.base64UrlDecode("9hH0vgRfYgPnAHOd8stkvw");
        AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256 jweContentEncryptionAlg = new AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256();
        ContentEncryptionParts encryptionParts = new ContentEncryptionParts(iv, ciphertext, tag);
        byte[] plaintextBytes = jweContentEncryptionAlg.decrypt(encryptionParts, header, contentEncryptionKeyBytes, headers, ProviderContextTest.EMPTY_CONTEXT);
        Aes128CbcHmacSha256ContentEncryptionAlgorithmTest.assertEquals((String)"Live long and prosper.", (String)StringUtil.newStringUtf8(plaintextBytes));
    }

    public void testRoundTrip() throws JoseException {
        String text = "I'm writing this test on a flight to Zurich";
        String encodedHeader = "eyJhbGciOiJSU0ExXzUiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0";
        Headers headers = new Headers();
        headers.setFullHeaderAsJsonString(Base64Url.decodeToUtf8String(encodedHeader));
        byte[] aad = StringUtil.getBytesUtf8(encodedHeader);
        byte[] plaintext = StringUtil.getBytesUtf8(text);
        AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256 contentEncryptionAlg = new AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256();
        ContentEncryptionKeyDescriptor cekDesc = contentEncryptionAlg.getContentEncryptionKeyDescriptor();
        byte[] cek = ByteUtil.randomBytes(cekDesc.getContentEncryptionKeyByteLength());
        ContentEncryptionParts encryptionParts = contentEncryptionAlg.encrypt(plaintext, aad, cek, headers, null, ProviderContextTest.EMPTY_CONTEXT);
        byte[] decrypt = contentEncryptionAlg.decrypt(encryptionParts, aad, cek, null, ProviderContextTest.EMPTY_CONTEXT);
        Aes128CbcHmacSha256ContentEncryptionAlgorithmTest.assertEquals((String)text, (String)StringUtil.newStringUtf8(decrypt));
    }
}

