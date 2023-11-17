/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.util;

import com.didisoft.pgp.bc.elgamal.util.ArrayUtil;
import java.io.PrintWriter;

public class Hex {
    private static final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private Hex() {
    }

    public static String toString(byte[] byArray, int n, int n2) {
        char[] cArray = new char[n2 * 2];
        int n3 = 0;
        for (int i = n; i < n + n2; ++i) {
            byte by = byArray[i];
            cArray[n3++] = hexDigits[by >>> 4 & 0xF];
            cArray[n3++] = hexDigits[by & 0xF];
        }
        return new String(cArray);
    }

    public static String toString(byte[] byArray) {
        return Hex.toString(byArray, 0, byArray.length);
    }

    public static String toString(int[] nArray, int n, int n2) {
        char[] cArray = new char[n2 * 8];
        int n3 = 0;
        for (int i = n; i < n + n2; ++i) {
            int n4 = nArray[i];
            cArray[n3++] = hexDigits[n4 >>> 28 & 0xF];
            cArray[n3++] = hexDigits[n4 >>> 24 & 0xF];
            cArray[n3++] = hexDigits[n4 >>> 20 & 0xF];
            cArray[n3++] = hexDigits[n4 >>> 16 & 0xF];
            cArray[n3++] = hexDigits[n4 >>> 12 & 0xF];
            cArray[n3++] = hexDigits[n4 >>> 8 & 0xF];
            cArray[n3++] = hexDigits[n4 >>> 4 & 0xF];
            cArray[n3++] = hexDigits[n4 & 0xF];
        }
        return new String(cArray);
    }

    public static String toString(int[] nArray) {
        return Hex.toString(nArray, 0, nArray.length);
    }

    public static String toReversedString(byte[] byArray, int n, int n2) {
        char[] cArray = new char[n2 * 2];
        int n3 = 0;
        for (int i = n + n2 - 1; i >= n; --i) {
            cArray[n3++] = hexDigits[byArray[i] >>> 4 & 0xF];
            cArray[n3++] = hexDigits[byArray[i] & 0xF];
        }
        return new String(cArray);
    }

    public static String toReversedString(byte[] byArray) {
        return Hex.toReversedString(byArray, 0, byArray.length);
    }

    public static byte[] fromString(String string) {
        int n = string.length();
        byte[] byArray = new byte[(n + 1) / 2];
        int n2 = 0;
        int n3 = 0;
        if (n % 2 == 1) {
            byArray[n3++] = (byte)Hex.fromDigit(string.charAt(n2++));
        }
        while (n2 < n) {
            byArray[n3++] = (byte)(Hex.fromDigit(string.charAt(n2++)) << 4 | Hex.fromDigit(string.charAt(n2++)));
        }
        return byArray;
    }

    public static byte[] fromReversedString(String string) {
        int n = string.length();
        byte[] byArray = new byte[(n + 1) / 2];
        int n2 = 0;
        if (n % 2 == 1) {
            throw new IllegalArgumentException("string must have an even number of digits");
        }
        while (n > 0) {
            byArray[n2++] = (byte)(Hex.fromDigit(string.charAt(--n)) | Hex.fromDigit(string.charAt(--n)) << 4);
        }
        return byArray;
    }

    public static char toDigit(int n) {
        try {
            return hexDigits[n];
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            throw new IllegalArgumentException(n + " is out of range for a hex digit");
        }
    }

    public static int fromDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 65 + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 97 + 10;
        }
        throw new IllegalArgumentException("invalid hex digit '" + c + "'");
    }

    public static String byteToString(int n) {
        char[] cArray = new char[]{hexDigits[n >>> 4 & 0xF], hexDigits[n & 0xF]};
        return new String(cArray);
    }

    public static String shortToString(int n) {
        char[] cArray = new char[]{hexDigits[n >>> 12 & 0xF], hexDigits[n >>> 8 & 0xF], hexDigits[n >>> 4 & 0xF], hexDigits[n & 0xF]};
        return new String(cArray);
    }

    public static String intToString(int n) {
        char[] cArray = new char[8];
        for (int i = 7; i >= 0; --i) {
            cArray[i] = hexDigits[n & 0xF];
            n >>>= 4;
        }
        return new String(cArray);
    }

    public static String longToString(long l) {
        char[] cArray = new char[16];
        for (int i = 15; i >= 0; --i) {
            cArray[i] = hexDigits[(int)l & 0xF];
            l >>>= 4;
        }
        return new String(cArray);
    }

    public static String dumpString(byte[] byArray, int n, int n2, String string) {
        if (byArray == null) {
            return string + "null\n";
        }
        StringBuffer stringBuffer = new StringBuffer(n2 * 3);
        if (n2 > 32) {
            stringBuffer.append(string).append("Hexadecimal dump of ").append(n2).append(" bytes...\n");
        }
        int n3 = n + n2;
        int n4 = Integer.toString(n2).length();
        if (n4 < 4) {
            n4 = 4;
        }
        while (n < n3) {
            int n5;
            if (n2 > 32) {
                String string2 = "         " + n;
                stringBuffer.append(string).append(string2.substring(string2.length() - n4)).append(": ");
            }
            for (n5 = 0; n5 < 32 && n + n5 + 7 < n3; n5 += 8) {
                stringBuffer.append(Hex.toString(byArray, n + n5, 8)).append(' ');
            }
            if (n5 < 32) {
                while (n5 < 32 && n + n5 < n3) {
                    stringBuffer.append(Hex.byteToString(byArray[n + n5]));
                    ++n5;
                }
            }
            stringBuffer.append('\n');
            n += 32;
        }
        return stringBuffer.toString();
    }

    public static String dumpString(byte[] byArray) {
        return byArray == null ? "null\n" : Hex.dumpString(byArray, 0, byArray.length, "");
    }

    public static String dumpString(byte[] byArray, String string) {
        return byArray == null ? "null\n" : Hex.dumpString(byArray, 0, byArray.length, string);
    }

    public static String dumpString(byte[] byArray, int n, int n2) {
        return Hex.dumpString(byArray, n, n2, "");
    }

    public static String dumpString(int[] nArray, int n, int n2, String string) {
        if (nArray == null) {
            return string + "null\n";
        }
        StringBuffer stringBuffer = new StringBuffer(n2 * 3);
        if (n2 > 8) {
            stringBuffer.append(string).append("Hexadecimal dump of ").append(n2).append(" integers...\n");
        }
        int n3 = n + n2;
        int n4 = Integer.toString(n2).length();
        if (n4 < 8) {
            n4 = 8;
        }
        while (n < n3) {
            if (n2 > 8) {
                String string2 = "         " + n;
                stringBuffer.append(string).append(string2.substring(string2.length() - n4)).append(": ");
            }
            for (int i = 0; i < 8 && n < n3; ++i) {
                stringBuffer.append(Hex.intToString(nArray[n++])).append(' ');
            }
            stringBuffer.append('\n');
        }
        return stringBuffer.toString();
    }

    public static String dumpString(int[] nArray) {
        return Hex.dumpString(nArray, 0, nArray.length, "");
    }

    public static String dumpString(int[] nArray, String string) {
        return Hex.dumpString(nArray, 0, nArray.length, string);
    }

    public static String dumpString(int[] nArray, int n, int n2) {
        return Hex.dumpString(nArray, n, n2, "");
    }

    public static void main(String[] stringArray) {
        Hex.self_test(new PrintWriter(System.out, true));
    }

    public static void self_test(PrintWriter printWriter) {
        String string = "Hello. This is a test string with more than 32 characters.";
        byte[] byArray = new byte[string.length()];
        for (int i = 0; i < string.length(); ++i) {
            byArray[i] = (byte)string.charAt(i);
        }
        String string2 = Hex.toString(byArray);
        printWriter.println("Hex.toString(buf) = " + string2);
        byte[] byArray2 = Hex.fromString(string2);
        if (!ArrayUtil.areEqual(byArray, byArray2)) {
            System.out.println("buf != buf2");
        }
        string2 = Hex.toReversedString(byArray);
        printWriter.println("Hex.toReversedString(buf) = " + string2);
        byArray2 = Hex.fromReversedString(string2);
        if (!ArrayUtil.areEqual(byArray, byArray2)) {
            printWriter.println("buf != buf2");
        }
        printWriter.print("Hex.dumpString(buf, 0, 28) =\n" + Hex.dumpString(byArray, 0, 28));
        printWriter.print("Hex.dumpString(null) =\n" + Hex.dumpString(null));
        printWriter.print(Hex.dumpString(byArray, "+++"));
        printWriter.flush();
    }
}

