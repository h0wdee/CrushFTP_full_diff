/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwe.kdf;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.jose4j.jwe.kdf.PasswordBasedKeyDerivationFunction2;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class Pbkdf2CompareToJavaSecretKeyFactorySha1Test {
    @Test
    public void testIterationCount() throws Exception {
        this.deriveAndCompare("somepass", "salty!", 1, 20);
        this.deriveAndCompare("somepass", "salty!", 2, 20);
        this.deriveAndCompare("somepass", "salty!", 3, 20);
        this.deriveAndCompare("somepass", "salty!", 4, 20);
        this.deriveAndCompare("somepass", "salty!", 100, 20);
    }

    @Test
    public void testIterationLength() throws Exception {
        this.deriveAndCompare("password", "sssss", 100, 4);
        this.deriveAndCompare("password", "sssss", 100, 16);
        this.deriveAndCompare("password", "sssss", 100, 20);
        this.deriveAndCompare("password", "sssss", 100, 21);
        this.deriveAndCompare("password", "sssss", 100, 32);
        this.deriveAndCompare("password", "sssss", 100, 64);
        this.deriveAndCompare("password", "sssss", 100, 65);
    }

    @Test
    public void testSomeRandoms() throws Exception {
        this.deriveAndCompare("pwd", "xxx", 1, 40);
        this.deriveAndCompare("alongerpasswordwithmorelettersinit", "abcdefghijklmnopqrstuv1234000001ccd", 10, 16);
        this.deriveAndCompare("password", "yyyy", 10, 1);
        this.deriveAndCompare("ppppppppp", "sssss", 1000, 21);
        this.deriveAndCompare("meh", "andmeh", 100, 20);
    }

    void deriveAndCompare(String p, String s, int c, int dkLen) throws Exception {
        PasswordBasedKeyDerivationFunction2 pbkdf2 = new PasswordBasedKeyDerivationFunction2("HmacSHA1");
        byte[] passwordBytes = StringUtil.getBytesAscii(p);
        byte[] saltBytes = StringUtil.getBytesAscii(s);
        byte[] derived = pbkdf2.derive(passwordBytes, saltBytes, c, dkLen);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec ks = new PBEKeySpec(p.toCharArray(), saltBytes, c, ByteUtil.bitLength(dkLen));
        SecretKey secretKey = f.generateSecret(ks);
        Assert.assertArrayEquals((byte[])secretKey.getEncoded(), (byte[])derived);
    }
}

