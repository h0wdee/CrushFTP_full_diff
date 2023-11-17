/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt;

import org.jose4j.jwt.GeneralJwtException;

public class MalformedClaimException
extends GeneralJwtException {
    public MalformedClaimException(String message) {
        super(message);
    }

    public MalformedClaimException(String message, Throwable cause) {
        super(message, cause);
    }
}

