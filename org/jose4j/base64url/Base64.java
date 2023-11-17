/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.base64url;

public class Base64 {
    public static String encode(byte[] bytes) {
        return Base64.getCodec().encodeToString(bytes);
    }

    public static byte[] decode(String encoded) {
        return Base64.getCodec().decode(encoded);
    }

    private static org.jose4j.base64url.internal.apache.commons.codec.binary.Base64 getCodec() {
        return new org.jose4j.base64url.internal.apache.commons.codec.binary.Base64();
    }
}

