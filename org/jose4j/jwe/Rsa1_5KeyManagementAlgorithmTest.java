/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwe;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import junit.framework.TestCase;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContextTest;
import org.jose4j.jwe.AesCbcHmacSha2ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.RsaKeyManagementAlgorithm;
import org.jose4j.keys.ExampleRsaJwksFromJwe;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;

public class Rsa1_5KeyManagementAlgorithmTest
extends TestCase {
    public void testJweExampleA2() throws JoseException {
        String encodedEncryptedKey = "UGhIOguC7IuEvf_NPVaXsGMoLOmwvc1GyqlIKOK1nN94nHPoltGRhWhw7Zx0-kFm1NJn8LE9XShH59_i8J0PH5ZZyNfGy2xGdULU7sHNF6Gp2vPLgNZ__deLKxGHZ7PcHALUzoOegEI-8E66jX2E4zyJKx-YxzZIItRzC5hlRirb6Y5Cl_p-ko3YvkkysZIFNPccxRU7qve1WYPxqbb2Yw8kZqa2rMWI5ng8OtvzlV7elprCbuPhcCdZ6XDP0_F8rkXds2vE4X-ncOIM8hAYHHi29NX0mcKiRaD0-D-ljQTP-cFPgwCp6X-nZZd9OHBv-B3oWh2TbqmScqXMR4gp_A";
        Base64Url base64Url = new Base64Url();
        byte[] encryptedKey = base64Url.base64UrlDecode(encodedEncryptedKey);
        RsaKeyManagementAlgorithm.Rsa1_5 keyManagementAlgorithm = new RsaKeyManagementAlgorithm.Rsa1_5();
        PrivateKey privateKey = ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey();
        AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256 contentEncryptionAlgorithm = new AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256();
        ContentEncryptionKeyDescriptor cekDesc = contentEncryptionAlgorithm.getContentEncryptionKeyDescriptor();
        Key key = keyManagementAlgorithm.manageForDecrypt(privateKey, encryptedKey, cekDesc, null, ProviderContextTest.EMPTY_CONTEXT);
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
        byte[] cekBytes = ByteUtil.convertUnsignedToSignedTwosComp(nArray);
        byte[] encoded = key.getEncoded();
        Rsa1_5KeyManagementAlgorithmTest.assertTrue((String)Arrays.toString(encoded), (boolean)Arrays.equals(cekBytes, encoded));
    }

    public void testRoundTrip() throws JoseException {
        RsaKeyManagementAlgorithm.Rsa1_5 rsa = new RsaKeyManagementAlgorithm.Rsa1_5();
        ContentEncryptionKeyDescriptor cekDesc = new ContentEncryptionKeyDescriptor(16, "AES");
        PublicKey publicKey = ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey();
        ContentEncryptionKeys contentEncryptionKeys = rsa.manageForEncrypt(publicKey, cekDesc, null, null, ProviderContextTest.EMPTY_CONTEXT);
        byte[] encryptedKey = contentEncryptionKeys.getEncryptedKey();
        PrivateKey privateKey = ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey();
        Key key = rsa.manageForDecrypt(privateKey, encryptedKey, cekDesc, null, ProviderContextTest.EMPTY_CONTEXT);
        byte[] cek = contentEncryptionKeys.getContentEncryptionKey();
        Rsa1_5KeyManagementAlgorithmTest.assertTrue((boolean)Arrays.equals(cek, key.getEncoded()));
    }
}

