/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.util;

public class ArrayUtil {
    private static final int ZEROES_LEN = 500;
    private static byte[] zeroes = new byte[500];

    private ArrayUtil() {
    }

    public static void clear(byte[] byArray) {
        ArrayUtil.clear(byArray, 0, byArray.length);
    }

    public static void clear(byte[] byArray, int n, int n2) {
        if (n2 <= 500) {
            System.arraycopy(zeroes, 0, byArray, n, n2);
        } else {
            System.arraycopy(zeroes, 0, byArray, n, 500);
            int n3 = n2 / 2;
            for (int i = 500; i < n2; i += i) {
                System.arraycopy(byArray, n, byArray, n + i, i <= n3 ? i : n2 - i);
            }
        }
    }

    public static int toInt(short s, short s2) {
        return s & 0xFFFF | s2 << 16;
    }

    public static short toShort(byte by, byte by2) {
        return (short)(by & 0xFF | by2 << 8);
    }

    public static byte[] toBytes(int n) {
        byte[] byArray = new byte[4];
        for (int i = 3; i >= 0; --i) {
            byArray[i] = (byte)(n & 0xFF);
            n >>>= 8;
        }
        return byArray;
    }

    public static byte[] toBytes(short[] sArray, int n, int n2) {
        byte[] byArray = new byte[2 * n2];
        int n3 = 0;
        for (int i = n; i < n + n2; ++i) {
            byArray[n3++] = (byte)(sArray[i] >>> 8 & 0xFF);
            byArray[n3++] = (byte)(sArray[i] & 0xFF);
        }
        return byArray;
    }

    public static byte[] toBytes(short[] sArray) {
        return ArrayUtil.toBytes(sArray, 0, sArray.length);
    }

    public static short[] toShorts(byte[] byArray, int n, int n2) {
        short[] sArray = new short[n2 / 2];
        int n3 = 0;
        for (int i = n; i < n + n2 - 1; i += 2) {
            sArray[n3++] = (short)((byArray[i] & 0xFF) << 8 | byArray[i + 1] & 0xFF);
        }
        return sArray;
    }

    public static short[] toShorts(byte[] byArray) {
        return ArrayUtil.toShorts(byArray, 0, byArray.length);
    }

    public static boolean areEqual(byte[] byArray, byte[] byArray2) {
        int n = byArray.length;
        if (n != byArray2.length) {
            return false;
        }
        for (int i = 0; i < n; ++i) {
            if (byArray[i] == byArray2[i]) continue;
            return false;
        }
        return true;
    }

    public static boolean areEqual(int[] nArray, int[] nArray2) {
        int n = nArray.length;
        if (n != nArray2.length) {
            return false;
        }
        for (int i = 0; i < n; ++i) {
            if (nArray[i] == nArray2[i]) continue;
            return false;
        }
        return true;
    }

    public static int compared(byte[] byArray, byte[] byArray2, boolean bl) {
        int n = byArray.length;
        if (n < byArray2.length) {
            return -1;
        }
        if (n > byArray2.length) {
            return 1;
        }
        if (bl) {
            for (int i = n - 1; i >= 0; --i) {
                int n2 = byArray[i] & 0xFF;
                int n3 = byArray2[i] & 0xFF;
                if (n2 < n3) {
                    return -1;
                }
                if (n2 <= n3) continue;
                return 1;
            }
        } else {
            for (int i = 0; i < n; ++i) {
                int n4 = byArray[i] & 0xFF;
                int n5 = byArray2[i] & 0xFF;
                if (n4 < n5) {
                    return -1;
                }
                if (n4 <= n5) continue;
                return 1;
            }
        }
        return 0;
    }

    public static boolean isText(byte[] byArray) {
        int n = byArray.length;
        if (n == 0) {
            return false;
        }
        block3: for (int i = 0; i < n; ++i) {
            int n2 = byArray[i] & 0xFF;
            if (n2 >= 32 && n2 <= 127) continue;
            switch (n2) {
                case 7: 
                case 8: 
                case 9: 
                case 10: 
                case 11: 
                case 12: 
                case 13: 
                case 26: 
                case 27: 
                case 155: {
                    continue block3;
                }
                default: {
                    return false;
                }
            }
        }
        return true;
    }
}

