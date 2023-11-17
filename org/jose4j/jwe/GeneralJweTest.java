/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Test
 */
package org.jose4j.jwe;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.keys.ExampleRsaJwksFromJwe;
import org.jose4j.lang.JoseException;
import org.junit.Test;

public class GeneralJweTest {
    @Test(expected=NullPointerException.class)
    public void tryEncryptWithNullPlainText() throws JoseException {
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setAlgorithmHeaderValue("RSA1_5");
        jwe.setKeyIdHeaderValue("meh");
        jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        jwe.setKey(ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey());
        String compactSerialization = jwe.getCompactSerialization();
        System.out.println(compactSerialization);
    }
}

