/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.lang;

import org.jose4j.lang.JoseException;

public class UnresolvableKeyException
extends JoseException {
    public UnresolvableKeyException(String message) {
        super(message);
    }

    public UnresolvableKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}

