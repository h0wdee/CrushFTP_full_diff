/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.lang;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class StringUtil {
    public static final String UTF_8 = "UTF-8";
    public static final String US_ASCII = "US-ASCII";

    public static String newStringUtf8(byte[] bytes) {
        return StringUtil.newString(bytes, UTF_8);
    }

    public static String newStringUsAscii(byte[] bytes) {
        return StringUtil.newString(bytes, US_ASCII);
    }

    public static String newString(byte[] bytes, String charsetName) {
        try {
            return bytes == null ? null : new String(bytes, charsetName);
        }
        catch (UnsupportedEncodingException e) {
            throw StringUtil.newISE(charsetName);
        }
    }

    public static String newString(byte[] bytes, Charset charset) {
        return bytes == null ? null : new String(bytes, charset);
    }

    public static byte[] getBytesUtf8(String string) {
        return StringUtil.getBytesUnchecked(string, UTF_8);
    }

    public static byte[] getBytesAscii(String string) {
        return StringUtil.getBytesUnchecked(string, US_ASCII);
    }

    public static byte[] getBytes(String string, Charset charset) {
        return string == null ? null : string.getBytes(charset);
    }

    public static byte[] getBytesUnchecked(String string, String charsetName) {
        try {
            return string == null ? null : string.getBytes(charsetName);
        }
        catch (UnsupportedEncodingException e) {
            throw StringUtil.newISE(charsetName);
        }
    }

    private static IllegalStateException newISE(String charsetName) {
        return new IllegalStateException("Unknown or unsupported character set name: " + charsetName);
    }
}

