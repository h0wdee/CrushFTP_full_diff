/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.SecureRandom;
import org.jose4j.lang.ByteUtil;

public class InitializationVectorHelp {
    static byte[] iv(int byteLength, byte[] ivOverride, SecureRandom secureRandom) {
        return ivOverride == null ? ByteUtil.randomBytes(byteLength, secureRandom) : ivOverride;
    }
}

