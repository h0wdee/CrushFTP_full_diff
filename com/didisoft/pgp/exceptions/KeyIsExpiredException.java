/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.PGPException;

public class KeyIsExpiredException
extends PGPException {
    private static final long serialVersionUID = -4155092216675786068L;

    public KeyIsExpiredException(String string) {
        super(string);
    }

    public KeyIsExpiredException(String string, Exception exception) {
        super(string, exception);
    }
}

