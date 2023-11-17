/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.base64url;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;

public class SimplePEMEncoder {
    public static String encode(byte[] bytes) {
        return SimplePEMEncoder.getCodec().encodeToString(bytes);
    }

    public static byte[] decode(String encoded) {
        return SimplePEMEncoder.getCodec().decode(encoded);
    }

    static Base64 getCodec() {
        return new Base64(64);
    }
}

