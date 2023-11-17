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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContextTest;
import org.jose4j.jwa.JceProviderTestSupport;
import org.jose4j.jwe.AesGcmContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionParts;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class Aes256GcmContentEncryptionAlgorithmTest {
    @Test
    public void testExampleEncryptFromJweAppendix1() throws Exception {
        JceProviderTestSupport jceProviderTestSupport = new JceProviderTestSupport();
        jceProviderTestSupport.setDoReinitialize(false);
        jceProviderTestSupport.setEncryptionAlgsNeeded("A256GCM");
        jceProviderTestSupport.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                AesGcmContentEncryptionAlgorithm.Aes256Gcm aesGcmContentEncryptionAlg = new AesGcmContentEncryptionAlgorithm.Aes256Gcm();
                String plaintextText = "The true sign of intelligence is not knowledge but imagination.";
                byte[] plainText = StringUtil.getBytesUtf8(plaintextText);
                String encodedHeader = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQ";
                byte[] aad = StringUtil.getBytesAscii(encodedHeader);
                byte[] cek = ByteUtil.convertUnsignedToSignedTwosComp(new int[]{177, 161, 244, 128, 84, 143, 225, 115, 63, 180, 3, 255, 107, 154, 212, 246, 138, 7, 110, 91, 112, 46, 34, 105, 47, 130, 203, 46, 122, 234, 64, 252});
                byte[] iv = ByteUtil.convertUnsignedToSignedTwosComp(new int[]{227, 197, 117, 252, 2, 219, 233, 68, 180, 225, 77, 219});
                ContentEncryptionParts encryptionParts = aesGcmContentEncryptionAlg.encrypt(plainText, aad, cek, iv, null);
                Base64Url base64Url = new Base64Url();
                byte[] ciphertext = encryptionParts.getCiphertext();
                String encodedJweCiphertext = "5eym8TW_c8SuK0ltJ3rpYIzOeDQz7TALvtu6UG9oMo4vpzs9tX_EFShS8iB7j6jiSdiwkIr3ajwQzaBtQD_A";
                Assert.assertThat((Object)encodedJweCiphertext, (Matcher)CoreMatchers.equalTo((Object)base64Url.base64UrlEncode(ciphertext)));
                byte[] authenticationTag = encryptionParts.getAuthenticationTag();
                String encodedAuthenticationTag = "XFBoMYUZodetZdvTiFvSkQ";
                Assert.assertThat((Object)encodedAuthenticationTag, (Matcher)CoreMatchers.equalTo((Object)base64Url.base64UrlEncode(authenticationTag)));
                ContentEncryptionParts parts = new ContentEncryptionParts(iv, ciphertext, authenticationTag);
                byte[] decrypted = aesGcmContentEncryptionAlg.decrypt(parts, aad, cek, null, ProviderContextTest.EMPTY_CONTEXT);
                Assert.assertArrayEquals((byte[])plainText, (byte[])decrypted);
            }
        });
    }
}

