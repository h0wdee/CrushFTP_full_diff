/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.jce.SHA1Digest;
import com.maverick.ssh.components.jce.SHA256Digest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class Utils {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        return Utils.bytesToHex(bytes, 0, bytes.length);
    }

    public static String bytesToHex(byte[] bytes, int off, int len) {
        return Utils.bytesToHex(bytes, off, len, 0, false, false);
    }

    public static String bytesToHex(byte[] bytes, int bytesPerLine, boolean separateBytes, boolean showText) {
        return Utils.bytesToHex(bytes, 0, bytes.length, bytesPerLine, separateBytes, showText);
    }

    public static String bytesToHex(byte[] bytes, int off, int len, int bytesPerLine, boolean separateBytes, boolean showText) {
        StringBuffer buffer = new StringBuffer();
        StringBuffer text = new StringBuffer();
        if (bytesPerLine == 0) {
            bytesPerLine = len;
        }
        int remaining = len;
        int lines = len / bytesPerLine;
        for (int i = 0; i < lines; ++i) {
            for (int j = 0; j < bytesPerLine; ++j) {
                int v = bytes[off + i * bytesPerLine + j] & 0xFF;
                buffer.append(hexArray[v >>> 4]);
                buffer.append(hexArray[v & 0xF]);
                if (showText) {
                    if (v >= 32 && v <= 126) {
                        text.append((char)v);
                    } else {
                        text.append(".");
                    }
                }
                if (separateBytes) {
                    buffer.append(" ");
                }
                --remaining;
            }
            if (showText) {
                buffer.append(" [ ");
                buffer.append(text.toString());
                buffer.append(" ]");
                text.setLength(0);
            }
            if (bytesPerLine >= len) continue;
            buffer.append(System.lineSeparator());
        }
        while (remaining > 0) {
            int v = bytes[off + (len - remaining)] & 0xFF;
            buffer.append(hexArray[v >>> 4]);
            buffer.append(hexArray[v & 0xF]);
            if (showText) {
                if (v >= 32 && v <= 126) {
                    text.append((char)v);
                } else {
                    text.append(".");
                }
            }
            if (separateBytes) {
                buffer.append(" ");
            }
            --remaining;
        }
        return buffer.toString();
    }

    public static byte[] stripLeadingZeros(byte[] data) {
        int x;
        for (x = 0; x < data.length && data[x] == 0; ++x) {
        }
        if ((data[x] & 0x80) != 0) {
            --x;
        }
        if (x > 0) {
            byte[] tmp = new byte[data.length - x];
            System.arraycopy(data, x, tmp, 0, tmp.length);
            return tmp;
        }
        return data;
    }

    public static int nearestMultipleOf(int length, int i) {
        int difference = length % i;
        if (difference == 0) {
            return length;
        }
        if (difference < i / 2) {
            return length - difference;
        }
        return length + (i - difference);
    }

    public static String csv(String ... elements) {
        return Utils.csv(Arrays.asList(elements));
    }

    public static String csv(Collection<String> elements) {
        return Utils.csv(",", elements);
    }

    public static String randomAlphaNumericString(int length) {
        return new BigInteger(length * 8, new Random()).toString(32).substring(0, length);
    }

    public static String csv(String separator, String ... elements) {
        return Utils.csv(separator, Arrays.asList(elements));
    }

    public static String csv(String separator, Collection<String> elements) {
        StringBuffer b = new StringBuffer();
        for (String element : elements) {
            if (b.length() > 0) {
                b.append(separator);
            }
            b.append(element);
        }
        return b.toString();
    }

    public static boolean isNotBlank(String base) {
        return base != null && !"".equals(base.trim());
    }

    public static boolean isBlank(String base) {
        return base == null || "".equals(base.trim());
    }

    public static byte[] getUTF8Bytes(String value) {
        try {
            return value.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Your environment does not appear to support UTF-8 encoding");
        }
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have an even length");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String exec(String ... cmd) throws IOException, InterruptedException {
        String line;
        Process process = new ProcessBuilder(cmd).start();
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }
        int exitVal = process.waitFor();
        if (exitVal == 0) {
            return output.toString();
        }
        throw new IOException("Unexpected exit code " + exitVal + "[" + output.toString() + "]");
    }

    public static String before(String line, char ch) {
        int idx = line.indexOf(ch);
        if (idx > -1) {
            return line.substring(0, idx);
        }
        return line;
    }

    public static String after(String line, char ch) {
        int idx = line.indexOf(ch);
        if (idx > -1) {
            return line.substring(idx + 1);
        }
        return "";
    }

    public static byte[] sha1(byte[] data) {
        try {
            return Utils.digest(new SHA1Digest(), data);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] sha256(byte[] data) {
        try {
            return Utils.digest(new SHA256Digest(), data);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] sha512(byte[] data) {
        try {
            return Utils.digest(new SHA256Digest(), data);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] digest(Digest digest, byte[] data) {
        digest.putBytes(data);
        return digest.doFinal();
    }

    public static String[] toArray(String ... values) {
        return values;
    }

    public static boolean nonNull(Object result) {
        return result != null;
    }
}

