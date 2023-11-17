/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc;

import com.didisoft.pgp.SecureRandomSource;
import java.security.SecureRandom;

public class DefaultSecureRandomSource
implements SecureRandomSource {
    public SecureRandom getSecureRandom() {
        return new SecureRandom();
    }
}

