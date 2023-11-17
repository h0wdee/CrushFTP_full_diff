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

import java.math.BigInteger;
import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.keys.BigEndianBigInteger;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.junit.Assert;
import org.junit.Test;

public class BigEndianBigIntegerTest {
    @Test
    public void testExampleStuff() {
        this.basicConversionTest(BigEndianBigInteger.toBase64Url(ExampleRsaKeyFromJws.PUBLIC_KEY.getPublicExponent()));
        this.basicConversionTest(BigEndianBigInteger.toBase64Url(ExampleRsaKeyFromJws.PUBLIC_KEY.getModulus()));
        this.basicConversionTest(BigEndianBigInteger.toBase64Url(ExampleRsaKeyFromJws.PRIVATE_KEY.getPrivateExponent()));
    }

    @Test
    public void testBasicConversions() {
        int i = 0;
        while (i < 500) {
            this.basicConversionTest(i);
            ++i;
        }
    }

    @Test
    public void testBasicConversions2() {
        long l = 200L;
        while (l < Long.MAX_VALUE && l > 0L) {
            int i = -100;
            while (i <= 100) {
                this.basicConversionTest(l + (long)i);
                ++i;
            }
            l *= 2L;
        }
    }

    @Test
    public void testBasicConversionSub0() {
        try {
            this.basicConversionTest(-1L);
            Assert.fail((String)"negitive numbers shouldn't work");
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
    }

    @Test
    public void testBasicConversionSub0MinLong() {
        try {
            this.basicConversionTest(Long.MIN_VALUE);
            Assert.fail((String)"negitive numbers shouldn't work");
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
    }

    @Test
    public void testBasicConversion0() {
        this.basicConversionTest(0L);
    }

    @Test
    public void testBasicConversion1() {
        this.basicConversionTest(129L);
    }

    @Test
    public void testBasicConversion2() {
        this.basicConversionTest(0x800000L);
    }

    @Test
    public void testBasicConversion3() {
        this.basicConversionTest(0x800001L);
    }

    @Test
    public void testBasicConversion4() {
        this.basicConversionTest(8388811L);
    }

    @Test
    public void testBasicConversion5() {
        this.basicConversionTest(0xFFFFFFL);
    }

    @Test
    public void testBasicConversion6() {
        this.basicConversionTest(0x1000001L);
    }

    @Test
    public void testBasicConversionMaxLong() {
        this.basicConversionTest(Long.MAX_VALUE);
    }

    private void basicConversionTest(long i) {
        BigInteger bigInt1 = BigInteger.valueOf(i);
        String b64 = BigEndianBigInteger.toBase64Url(bigInt1);
        BigInteger bigInt2 = BigEndianBigInteger.fromBase64Url(b64);
        Assert.assertThat((Object)bigInt1, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)bigInt2)));
        byte[] bytes = BigEndianBigInteger.toByteArray(bigInt1);
        byte[] bytes2 = this.toByteArrayViaHex(bigInt1);
        Assert.assertArrayEquals((String)("array comp on " + i + " " + Arrays.toString(bytes) + " " + Arrays.toString(bytes2)), (byte[])bytes, (byte[])bytes2);
    }

    @Test
    public void testConversion1() {
        this.basicConversionTest("AQAB");
    }

    @Test
    public void testConversion2() {
        this.basicConversionTest("MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4");
    }

    @Test
    public void testConversion3() {
        this.basicConversionTest("4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM");
    }

    @Test
    public void testConversion4() {
        String s = "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw";
        this.basicConversionTest(s);
    }

    private void basicConversionTest(String urlEncodedBytes) {
        BigInteger bigInt = BigEndianBigInteger.fromBase64Url(urlEncodedBytes);
        String b64 = BigEndianBigInteger.toBase64Url(bigInt);
        Assert.assertEquals((Object)urlEncodedBytes, (Object)b64);
        BigInteger bigInt2 = BigEndianBigInteger.fromBase64Url(b64);
        Assert.assertEquals((Object)bigInt, (Object)bigInt2);
        byte[] bytes = BigEndianBigInteger.toByteArray(bigInt);
        byte[] bytes2 = this.toByteArrayViaHex(bigInt);
        Assert.assertArrayEquals((String)("array comp on " + urlEncodedBytes), (byte[])bytes, (byte[])bytes2);
    }

    private byte[] toByteArrayViaHex(BigInteger bigInteger) {
        int hexRadix = 16;
        String hexString = bigInteger.toString(hexRadix);
        hexString = hexString.length() % 2 != 0 ? "0" + hexString : hexString;
        byte[] bytes = new byte[hexString.length() / 2];
        int idx = 0;
        while (idx < hexString.length()) {
            String hexPart = hexString.substring(idx, idx + 2);
            bytes[idx / 2] = (byte)Short.parseShort(hexPart, hexRadix);
            idx += 2;
        }
        return bytes;
    }

    @Test
    public void minArrayLengthOneByteNumbers() {
        BigInteger[] oneByteBigs;
        BigInteger[] bigIntegerArray = oneByteBigs = new BigInteger[]{BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN, BigInteger.valueOf(127L), BigInteger.valueOf(255L)};
        int n = oneByteBigs.length;
        int n2 = 0;
        while (n2 < n) {
            int[] minArrayLengths;
            BigInteger bi = bigIntegerArray[n2];
            byte[] conciseBytes = BigEndianBigInteger.toByteArray(bi);
            Assert.assertThat((Object)1, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)conciseBytes.length)));
            BigInteger fromConciseByteArray = BigEndianBigInteger.fromBytes(conciseBytes);
            Assert.assertThat((Object)bi, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)fromConciseByteArray)));
            int[] nArray = minArrayLengths = new int[]{1, 2, 3, 5, 66};
            int n3 = minArrayLengths.length;
            int n4 = 0;
            while (n4 < n3) {
                int minArrayLength = nArray[n4];
                byte[] zeroPaddedBytes = BigEndianBigInteger.toByteArray(bi, minArrayLength);
                Assert.assertThat((Object)minArrayLength, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)zeroPaddedBytes.length)));
                BigInteger fromZeroPaddedBytes = BigEndianBigInteger.fromBytes(zeroPaddedBytes);
                Assert.assertThat((Object)bi, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)fromZeroPaddedBytes)));
                ++n4;
            }
            ++n2;
        }
    }

    @Test
    public void minLengthEncoded() {
        String testNumber = "3411573884280259127265394545583489556845492233706098942622874385873783026581606817805506341607692318868814372414764859287098904949502022867291016696377213417";
        String notPadded = "_nJhyQ20ca7Nn0Zvyiq54FfCAblGK7kuduFBTPkxv9eOjiaeGp7V_f3qV1kxS_Il2LY7Tc5l2GSlW_-SzYKxgek";
        String lftPadded = "AP5yYckNtHGuzZ9Gb8oqueBXwgG5Riu5LnbhQUz5Mb_Xjo4mnhqe1f396ldZMUvyJdi2O03OZdhkpVv_ks2CsYHp";
        BigInteger bigInteger1 = BigEndianBigInteger.fromBase64Url(notPadded);
        BigInteger bigInteger2 = BigEndianBigInteger.fromBase64Url(lftPadded);
        Assert.assertThat((Object)bigInteger1, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)bigInteger2)));
        BigInteger fromBase10 = new BigInteger(testNumber);
        Assert.assertThat((Object)fromBase10, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)bigInteger1)));
        String toBase64 = BigEndianBigInteger.toBase64Url(fromBase10);
        Assert.assertThat((Object)toBase64, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)notPadded)));
        String toBase64IncludingLeftPad = BigEndianBigInteger.toBase64Url(fromBase10, 66);
        Assert.assertThat((Object)toBase64IncludingLeftPad, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)lftPadded)));
    }
}

