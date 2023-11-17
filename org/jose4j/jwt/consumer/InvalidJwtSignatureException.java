/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import org.jose4j.jwt.consumer.InvalidJwtException;

public class InvalidJwtSignatureException
extends InvalidJwtException {
    public InvalidJwtSignatureException(String message) {
        super(message);
    }
}

