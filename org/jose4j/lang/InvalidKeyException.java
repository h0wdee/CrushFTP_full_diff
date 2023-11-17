/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.lang;

import org.jose4j.lang.JoseException;

public class InvalidKeyException
extends JoseException {
    public InvalidKeyException(String message) {
        super(message);
    }

    public InvalidKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}

