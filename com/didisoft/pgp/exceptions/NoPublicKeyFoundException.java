/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.PGPException;

public class NoPublicKeyFoundException
extends PGPException {
    private static final long serialVersionUID = -4979530887461581921L;

    public NoPublicKeyFoundException(String string) {
        super(string);
    }

    public NoPublicKeyFoundException(String string, Exception exception) {
        super(string, exception);
    }
}

