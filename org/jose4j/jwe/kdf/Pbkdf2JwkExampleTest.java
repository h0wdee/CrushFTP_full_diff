/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwe.kdf;

import java.io.IOException;
import org.jose4j.jwe.kdf.PasswordBasedKeyDerivationFunction2;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class Pbkdf2JwkExampleTest {
    @Test
    public void testThePbdkfPartFromJwkAppendixC() throws IOException, JoseException {
        String pass = "Thus from my lips, by yours, my sin is purged.";
        int[] nArray = new int[35];
        nArray[0] = 80;
        nArray[1] = 66;
        nArray[2] = 69;
        nArray[3] = 83;
        nArray[4] = 50;
        nArray[5] = 45;
        nArray[6] = 72;
        nArray[7] = 83;
        nArray[8] = 50;
        nArray[9] = 53;
        nArray[10] = 54;
        nArray[11] = 43;
        nArray[12] = 65;
        nArray[13] = 49;
        nArray[14] = 50;
        nArray[15] = 56;
        nArray[16] = 75;
        nArray[17] = 87;
        nArray[19] = 217;
        nArray[20] = 96;
        nArray[21] = 147;
        nArray[22] = 112;
        nArray[23] = 150;
        nArray[24] = 117;
        nArray[25] = 70;
        nArray[26] = 247;
        nArray[27] = 127;
        nArray[28] = 8;
        nArray[29] = 155;
        nArray[30] = 137;
        nArray[31] = 174;
        nArray[32] = 42;
        nArray[33] = 80;
        nArray[34] = 215;
        byte[] saltValue = ByteUtil.convertUnsignedToSignedTwosComp(nArray);
        int iterationCount = 4096;
        PasswordBasedKeyDerivationFunction2 pbkdf2 = new PasswordBasedKeyDerivationFunction2("HmacSHA256");
        byte[] derived = pbkdf2.derive(StringUtil.getBytesUtf8(pass), saltValue, iterationCount, 16);
        byte[] expectedDerived = ByteUtil.convertUnsignedToSignedTwosComp(new int[]{110, 171, 169, 92, 129, 92, 109, 117, 233, 242, 116, 233, 170, 14, 24, 75});
        Assert.assertArrayEquals((byte[])expectedDerived, (byte[])derived);
    }
}

