/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.PGPException;

public class WrongPasswordException
extends PGPException {
    private static final long serialVersionUID = 6436097197694402592L;

    public WrongPasswordException(String string) {
        super(string);
    }

    public WrongPasswordException(String string, Exception exception) {
        super(string, exception);
    }
}

