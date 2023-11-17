/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.lang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import org.jose4j.base64url.Base64Url;

public class ByteUtil {
    public static final byte[] EMPTY_BYTES = new byte[0];

    public static byte[] convertUnsignedToSignedTwosComp(int[] ints) {
        byte[] bytes = new byte[ints.length];
        int idx = 0;
        while (idx < ints.length) {
            bytes[idx] = ByteUtil.getByte(ints[idx]);
            ++idx;
        }
        return bytes;
    }

    public static int[] convertSignedTwosCompToUnsigned(byte[] bytes) {
        int[] ints = new int[bytes.length];
        int idx = 0;
        while (idx < bytes.length) {
            ints[idx] = ByteUtil.getInt(bytes[idx]);
            ++idx;
        }
        return ints;
    }

    public static byte getByte(int intValue) {
        byte[] bytes = ByteUtil.getBytes(intValue);
        if (bytes[0] != 0 || bytes[1] != 0 || bytes[2] != 0) {
            throw new IllegalArgumentException("Integer value (" + intValue + ") too large to stuff into one byte.");
        }
        return bytes[3];
    }

    public static byte[] getBytes(int intValue) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(intValue);
        return byteBuffer.array();
    }

    public static byte[] getBytes(long intValue) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putLong(intValue);
        return byteBuffer.array();
    }

    public static int getInt(byte b) {
        return b >= 0 ? b : 256 - ~(b - 1);
    }

    public static boolean secureEquals(byte[] bytes1, byte[] bytes2) {
        bytes1 = bytes1 == null ? EMPTY_BYTES : bytes1;
        bytes2 = bytes2 == null ? EMPTY_BYTES : bytes2;
        int shortest = Math.min(bytes1.length, bytes2.length);
        int longest = Math.max(bytes1.length, bytes2.length);
        int result = 0;
        int i = 0;
        while (i < shortest) {
            result |= bytes1[i] ^ bytes2[i];
            ++i;
        }
        return result == 0 && shortest == longest;
    }

    public static byte[] concat(byte[] ... byteArrays) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[][] byArray = byteArrays;
            int n = byteArrays.length;
            int n2 = 0;
            while (n2 < n) {
                byte[] bytes = byArray[n2];
                byteArrayOutputStream.write(bytes);
                ++n2;
            }
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException e) {
            throw new IllegalStateException("IOEx from ByteArrayOutputStream?!", e);
        }
    }

    public static byte[] subArray(byte[] inputBytes, int startPos, int length) {
        byte[] subArray = new byte[length];
        System.arraycopy(inputBytes, startPos, subArray, 0, subArray.length);
        return subArray;
    }

    public static byte[] leftHalf(byte[] inputBytes) {
        return ByteUtil.subArray(inputBytes, 0, inputBytes.length / 2);
    }

    public static byte[] rightHalf(byte[] inputBytes) {
        int half = inputBytes.length / 2;
        return ByteUtil.subArray(inputBytes, half, half);
    }

    public static int bitLength(byte[] bytes) {
        return ByteUtil.bitLength(bytes.length);
    }

    public static int bitLength(int byteLength) {
        return byteLength * 8;
    }

    public static int byteLength(int numberOfBits) {
        return numberOfBits / 8;
    }

    public static byte[] randomBytes(int length, SecureRandom secureRandom) {
        secureRandom = secureRandom == null ? new SecureRandom() : secureRandom;
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    public static byte[] randomBytes(int length) {
        return ByteUtil.randomBytes(length, null);
    }

    public static String toDebugString(byte[] bytes) {
        Base64Url base64Url = new Base64Url();
        String s = base64Url.base64UrlEncode(bytes);
        int[] ints = ByteUtil.convertSignedTwosCompToUnsigned(bytes);
        return String.valueOf(Arrays.toString(ints)) + "(" + ints.length + "bytes/" + ByteUtil.bitLength(ints.length) + "bits) | base64url encoded: " + s;
    }
}

