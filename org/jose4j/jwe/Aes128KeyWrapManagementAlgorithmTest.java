/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwe;

import java.security.Key;
import java.util.Arrays;
import junit.framework.TestCase;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContextTest;
import org.jose4j.jwe.AesCbcHmacSha2ContentEncryptionAlgorithm;
import org.jose4j.jwe.AesKeyWrapManagementAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;

public class Aes128KeyWrapManagementAlgorithmTest
extends TestCase {
    public void testJweExample() throws JoseException {
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
        int[] cekInts = nArray;
        byte[] cekBytes = ByteUtil.convertUnsignedToSignedTwosComp(cekInts);
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("\n     {\"kty\":\"oct\",\n      \"k\":\"GawgguFyGrWKav7AX4VKUg\"\n     }");
        AesKey managementKey = new AesKey(jsonWebKey.getKey().getEncoded());
        AesKeyWrapManagementAlgorithm.Aes128 wrappingKeyManagementAlgorithm = new AesKeyWrapManagementAlgorithm.Aes128();
        AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256 contentEncryptionAlgorithm = new AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256();
        ContentEncryptionKeyDescriptor cekDesc = contentEncryptionAlgorithm.getContentEncryptionKeyDescriptor();
        ContentEncryptionKeys contentEncryptionKeys = wrappingKeyManagementAlgorithm.manageForEnc(managementKey, cekDesc, cekBytes, ProviderContextTest.EMPTY_CONTEXT);
        String encodedEncryptedKeyFromExample = "6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ";
        Base64Url u = new Base64Url();
        String encodedWrapped = u.base64UrlEncode(contentEncryptionKeys.getEncryptedKey());
        Aes128KeyWrapManagementAlgorithmTest.assertEquals((String)encodedEncryptedKeyFromExample, (String)encodedWrapped);
        byte[] encryptedKey = u.base64UrlDecode(encodedEncryptedKeyFromExample);
        Key key = wrappingKeyManagementAlgorithm.manageForDecrypt(managementKey, encryptedKey, cekDesc, null, ProviderContextTest.EMPTY_CONTEXT);
        Aes128KeyWrapManagementAlgorithmTest.assertTrue((boolean)Arrays.equals(cekBytes, key.getEncoded()));
    }
}

