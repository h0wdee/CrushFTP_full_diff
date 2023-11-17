/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.PGPException;

public class WrongPrivateKeyException
extends PGPException {
    private static final long serialVersionUID = 92099035468530110L;

    public WrongPrivateKeyException(String string) {
        super(string);
    }

    public WrongPrivateKeyException(String string, Exception exception) {
        super(string, exception);
    }
}

