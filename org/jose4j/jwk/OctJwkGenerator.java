/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.security.SecureRandom;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.ByteUtil;

public class OctJwkGenerator {
    public static OctetSequenceJsonWebKey generateJwk(int keyLengthInBits) {
        return OctJwkGenerator.generateJwk(keyLengthInBits, null);
    }

    public static OctetSequenceJsonWebKey generateJwk(int keyLengthInBits, SecureRandom secureRandom) {
        byte[] bytes = ByteUtil.randomBytes(ByteUtil.byteLength(keyLengthInBits), secureRandom);
        return new OctetSequenceJsonWebKey(new AesKey(bytes));
    }
}

