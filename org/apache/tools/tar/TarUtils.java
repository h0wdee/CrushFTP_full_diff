/*
 * Decompiled with CFR 0.152.
 */
package org.apache.tools.tar;

public class TarUtils {
    private static final int BYTE_MASK = 255;

    public static long parseOctal(byte[] byArray, int n, int n2) {
        long l = 0L;
        boolean bl = true;
        int n3 = n + n2;
        for (int i = n; i < n3 && byArray[i] != 0; ++i) {
            if (byArray[i] == 32 || byArray[i] == 48) {
                if (bl) continue;
                if (byArray[i] == 32) break;
            }
            bl = false;
            l = (l << 3) + (long)(byArray[i] - 48);
        }
        return l;
    }

    public static StringBuffer parseName(byte[] byArray, int n, int n2) {
        StringBuffer stringBuffer = new StringBuffer(n2);
        int n3 = n + n2;
        for (int i = n; i < n3 && byArray[i] != 0; ++i) {
            stringBuffer.append((char)byArray[i]);
        }
        return stringBuffer;
    }

    public static int getNameBytes(StringBuffer stringBuffer, byte[] byArray, int n, int n2) {
        int n3;
        for (n3 = 0; n3 < n2 && n3 < stringBuffer.length(); ++n3) {
            byArray[n + n3] = (byte)stringBuffer.charAt(n3);
        }
        while (n3 < n2) {
            byArray[n + n3] = 0;
            ++n3;
        }
        return n + n2;
    }

    public static int getOctalBytes(long l, byte[] byArray, int n, int n2) {
        int n3 = n2 - 1;
        byArray[n + n3] = 0;
        byArray[n + --n3] = 32;
        --n3;
        if (l == 0L) {
            byArray[n + n3] = 48;
            --n3;
        } else {
            for (long i = l; n3 >= 0 && i > 0L; i >>= 3, --n3) {
                byArray[n + n3] = (byte)(48 + (byte)(i & 7L));
            }
        }
        while (n3 >= 0) {
            byArray[n + n3] = 32;
            --n3;
        }
        return n + n2;
    }

    public static int getOctalBytesUnix(long l, byte[] byArray, int n, int n2) {
        int n3 = n2 - 1;
        byArray[n + n3] = 0;
        --n3;
        if (l == 0L) {
            byArray[n + n3] = 48;
            --n3;
        } else {
            for (long i = l; n3 >= 0 && i > 0L; i >>= 3, --n3) {
                byArray[n + n3] = (byte)(48 + (byte)(i & 7L));
            }
        }
        while (n3 >= 0) {
            byArray[n + n3] = 48;
            --n3;
        }
        return n + n2;
    }

    public static int getOctalCHKSumUnix(long l, byte[] byArray, int n, int n2) {
        int n3 = n2 - 1;
        byArray[n + n3] = 0;
        byArray[n + --n3] = 32;
        --n3;
        if (l == 0L) {
            byArray[n + n3] = 48;
            --n3;
        } else {
            for (long i = l; n3 >= 0 && i > 0L; i >>= 3, --n3) {
                byArray[n + n3] = (byte)(48 + (byte)(i & 7L));
            }
        }
        while (n3 >= 0) {
            byArray[n + n3] = 48;
            --n3;
        }
        return n + n2;
    }

    public static int getLongOctalBytes(long l, byte[] byArray, int n, int n2) {
        byte[] byArray2 = new byte[n2 + 1];
        TarUtils.getOctalBytes(l, byArray2, 0, n2 + 1);
        System.arraycopy(byArray2, 0, byArray, n, n2);
        return n + n2;
    }

    public static int getLongOctalBytesUnix(long l, byte[] byArray, int n, int n2) {
        byte[] byArray2 = new byte[n2];
        TarUtils.getOctalBytesUnix(l, byArray2, 0, n2);
        System.arraycopy(byArray2, 0, byArray, n, n2);
        return n + n2;
    }

    public static int getCheckSumOctalBytes(long l, byte[] byArray, int n, int n2) {
        TarUtils.getOctalBytes(l, byArray, n, n2);
        byArray[n + n2 - 1] = 32;
        byArray[n + n2 - 2] = 0;
        return n + n2;
    }

    public static int getCheckSumOctalBytesUnix(long l, byte[] byArray, int n, int n2) {
        TarUtils.getOctalCHKSumUnix(l, byArray, n, n2);
        byArray[n + n2 - 1] = 32;
        byArray[n + n2 - 2] = 0;
        return n + n2;
    }

    public static long computeCheckSum(byte[] byArray) {
        long l = 0L;
        for (int i = 0; i < byArray.length; ++i) {
            l += (long)(0xFF & byArray[i]);
        }
        return l;
    }
}

