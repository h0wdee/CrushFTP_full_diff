/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.lang;

import java.util.Arrays;
import junit.framework.TestCase;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;

public class ByteUtilTest
extends TestCase {
    public void testLeftRight() {
        byte[] byArray = new byte[10];
        byArray[1] = 1;
        byArray[2] = 2;
        byArray[3] = 3;
        byArray[4] = 4;
        byArray[5] = 5;
        byArray[6] = 6;
        byArray[7] = 7;
        byArray[8] = 8;
        byArray[9] = 9;
        byte[] fullCekBytes = byArray;
        byte[] hmacKeyBytes = new byte[fullCekBytes.length / 2];
        byte[] encKeyBytes = new byte[fullCekBytes.length / 2];
        System.arraycopy(fullCekBytes, 0, hmacKeyBytes, 0, hmacKeyBytes.length);
        System.arraycopy(fullCekBytes, hmacKeyBytes.length, encKeyBytes, 0, encKeyBytes.length);
        byte[] left = ByteUtil.leftHalf(fullCekBytes);
        byte[] right = ByteUtil.rightHalf(fullCekBytes);
        ByteUtilTest.assertTrue((boolean)Arrays.equals(hmacKeyBytes, left));
        ByteUtilTest.assertTrue((boolean)Arrays.equals(encKeyBytes, right));
    }

    public void testGetBytesLong() {
        long value = 408L;
        byte[] bytes = ByteUtil.getBytes(value);
        int[] integers = ByteUtil.convertSignedTwosCompToUnsigned(bytes);
        ByteUtilTest.assertEquals((int)8, (int)integers.length);
        int i = 0;
        while (i < 6) {
            ByteUtilTest.assertEquals((int)0, (int)integers[i]);
            ++i;
        }
        ByteUtilTest.assertEquals((int)1, (int)integers[6]);
        ByteUtilTest.assertEquals((int)152, (int)integers[7]);
    }

    public void testConcat1() {
        byte[] first = new byte[2];
        byte[] second = new byte[10];
        byte[] third = new byte[15];
        byte[] result = ByteUtil.concat(first, second, third);
        ByteUtilTest.assertEquals((int)(first.length + second.length + third.length), (int)result.length);
        ByteUtilTest.assertTrue((boolean)Arrays.equals(new byte[result.length], result));
    }

    public void testConcat2() {
        byte[] first = new byte[]{1, 2, 7};
        byte[] second = new byte[]{38, 101};
        byte[] third = new byte[]{5, 6, 7};
        byte[] result = ByteUtil.concat(first, second, third);
        ByteUtilTest.assertEquals((int)(first.length + second.length + third.length), (int)result.length);
        ByteUtilTest.assertTrue((boolean)Arrays.equals(new byte[]{1, 2, 7, 38, 101, 5, 6, 7}, result));
    }

    public void testConcat3() {
        byte[] first = new byte[]{1, 2, 7};
        byte[] second = new byte[]{};
        byte[] third = new byte[]{5, 6, 7};
        byte[] fourth = new byte[]{};
        byte[] result = ByteUtil.concat(first, second, third);
        ByteUtilTest.assertEquals((int)(first.length + second.length + third.length + fourth.length), (int)result.length);
        ByteUtilTest.assertTrue((boolean)Arrays.equals(new byte[]{1, 2, 7, 5, 6, 7}, result));
    }

    public void testGetBytesOne() {
        byte[] bytes = ByteUtil.getBytes(1);
        ByteUtilTest.assertEquals((int)4, (int)bytes.length);
        ByteUtilTest.assertEquals((int)0, (int)bytes[0]);
        ByteUtilTest.assertEquals((int)0, (int)bytes[1]);
        ByteUtilTest.assertEquals((int)0, (int)bytes[2]);
        ByteUtilTest.assertEquals((int)1, (int)bytes[3]);
    }

    public void testGetBytesTwo() {
        byte[] bytes = ByteUtil.getBytes(2);
        ByteUtilTest.assertEquals((int)4, (int)bytes.length);
        ByteUtilTest.assertEquals((int)0, (int)bytes[0]);
        ByteUtilTest.assertEquals((int)0, (int)bytes[1]);
        ByteUtilTest.assertEquals((int)0, (int)bytes[2]);
        ByteUtilTest.assertEquals((int)2, (int)bytes[3]);
    }

    public void testGetBytesMax() {
        byte[] bytes = ByteUtil.getBytes(Integer.MAX_VALUE);
        ByteUtilTest.assertEquals((int)4, (int)bytes.length);
    }

    public void testConvert() throws JoseException {
        int i = 0;
        while (i < 256) {
            byte b = ByteUtil.getByte(i);
            int anInt = ByteUtil.getInt(b);
            ByteUtilTest.assertEquals((int)i, (int)anInt);
            ++i;
        }
    }

    public void testConvert2() throws JoseException {
        boolean keepGoing = true;
        byte b = -128;
        while (keepGoing) {
            int i = ByteUtil.getInt(b);
            byte aByte = ByteUtil.getByte(i);
            ByteUtilTest.assertEquals((byte)b, (byte)aByte);
            if (b == 127) {
                keepGoing = false;
            }
            b = (byte)(b + 1);
        }
    }

    public void testEquals0() {
        byte[] bytes1 = ByteUtil.randomBytes(32);
        byte[] bytes2 = new byte[bytes1.length];
        bytes1[0] = 1;
        this.compareTest(bytes1, bytes2, false);
        System.arraycopy(bytes1, 0, bytes2, 0, bytes1.length);
        this.compareTest(bytes1, bytes2, true);
    }

    public void testRandomBytesNullSecRan() {
        byte[] bytes = ByteUtil.randomBytes(4, null);
        ByteUtilTest.assertTrue((bytes.length == 4 ? 1 : 0) != 0);
    }

    public void testEquals1() {
        this.compareTest(new byte[]{-1}, new byte[]{1}, false);
    }

    public void testEquals2() {
        this.compareTest("good", "good", true);
    }

    public void testEquals3() {
        this.compareTest("baad", "good", false);
    }

    public void testEquals3b() {
        this.compareTest("bad", "good", false);
    }

    public void testEquals4() {
        this.compareTest("", "niner", false);
    }

    public void testEquals5() {
        this.compareTest("foo", "bar", false);
    }

    public void testEquals6() {
        this.compareTest(new byte[]{-1, 123, 7, 1}, new byte[]{-1, 123, 7, 1}, true);
    }

    public void testEquals7() {
        this.compareTest(new byte[]{-1, 123, -19, 1}, new byte[]{-1, 123, 7, 1}, false);
    }

    public void testEquals8() {
        this.compareTest(new byte[]{-1, 123, 7, 1, -32}, new byte[]{-1, 123, 7, 1}, false);
    }

    public void testEquals9() {
        byte[] byArray = new byte[5];
        byArray[0] = -1;
        byArray[1] = 123;
        byArray[2] = 7;
        byArray[3] = 1;
        this.compareTest(new byte[]{-1, 123, 7, 1}, byArray, false);
    }

    public void testEquals10() {
        byte[] byArray = new byte[5];
        byArray[0] = -1;
        byArray[1] = 123;
        byArray[2] = 7;
        byArray[3] = 1;
        this.compareTest(null, byArray, false);
    }

    public void testEquals11() {
        this.compareTest(new byte[]{-1, 123, 7, 1}, null, false);
    }

    public void testEquals12() {
        byte[] byArray = new byte[5];
        byArray[0] = -1;
        byArray[1] = 123;
        byArray[2] = 7;
        byArray[3] = 1;
        this.compareTest(new byte[0], byArray, false);
    }

    public void testEquals13() {
        this.compareTest(new byte[]{-1, 123, 7, 1}, new byte[0], false);
    }

    public void testEquals14() {
        this.compareTest(new byte[0], new byte[0], true);
    }

    public void testEquals15() {
        this.compareTest((byte[])null, (byte[])null, true);
    }

    private void compareTest(String first, String second, boolean shouldMatch) {
        this.compareTest(StringUtil.getBytesUtf8(first), StringUtil.getBytesUtf8(second), shouldMatch);
    }

    private void compareTest(byte[] first, byte[] second, boolean shouldMatch) {
        ByteUtilTest.assertEquals((boolean)shouldMatch, (boolean)ByteUtil.secureEquals(first, second));
    }
}

