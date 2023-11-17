/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.lang;

import java.security.SecureRandom;
import org.jose4j.lang.ByteGenerator;

public class DefaultByteGenerator
implements ByteGenerator {
    private final SecureRandom random = new SecureRandom();

    @Override
    public byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        this.random.nextBytes(bytes);
        return bytes;
    }
}

