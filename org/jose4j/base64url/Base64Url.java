/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.base64url;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.jose4j.lang.StringUtil;

public class Base64Url {
    private Base64 base64urlCodec = new Base64(-1, null, true);

    public String base64UrlDecodeToUtf8String(String encodedValue) {
        return this.base64UrlDecodeToString(encodedValue, "UTF-8");
    }

    public String base64UrlDecodeToString(String encodedValue, String charsetName) {
        byte[] bytes = this.base64UrlDecode(encodedValue);
        return StringUtil.newString(bytes, charsetName);
    }

    public byte[] base64UrlDecode(String encodedValue) {
        return this.base64urlCodec.decode(encodedValue);
    }

    public String base64UrlEncodeUtf8ByteRepresentation(String value) {
        return this.base64UrlEncode(value, "UTF-8");
    }

    public String base64UrlEncode(String value, String charsetName) {
        byte[] bytes = StringUtil.getBytesUnchecked(value, charsetName);
        return this.base64UrlEncode(bytes);
    }

    public String base64UrlEncode(byte[] bytes) {
        return this.base64urlCodec.encodeToString(bytes);
    }

    private static Base64Url getOne() {
        return new Base64Url();
    }

    public static String decodeToUtf8String(String encodedValue) {
        return Base64Url.getOne().base64UrlDecodeToString(encodedValue, "UTF-8");
    }

    public static String decodeToString(String encodedValue, String charsetName) {
        return Base64Url.getOne().base64UrlDecodeToString(encodedValue, charsetName);
    }

    public static byte[] decode(String encodedValue) {
        return Base64Url.getOne().base64UrlDecode(encodedValue);
    }

    public static String encodeUtf8ByteRepresentation(String value) {
        return Base64Url.getOne().base64UrlEncodeUtf8ByteRepresentation(value);
    }

    public static String encode(String value, String charsetName) {
        return Base64Url.getOne().base64UrlEncode(value, charsetName);
    }

    public static String encode(byte[] bytes) {
        return Base64Url.getOne().base64UrlEncode(bytes);
    }
}

