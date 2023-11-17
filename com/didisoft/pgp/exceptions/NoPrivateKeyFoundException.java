/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.exceptions.WrongPrivateKeyException;

public class NoPrivateKeyFoundException
extends WrongPrivateKeyException {
    private static final long serialVersionUID = 2794256673127079299L;

    public NoPrivateKeyFoundException(String string) {
        super(string);
    }

    public NoPrivateKeyFoundException(String string, Exception exception) {
        super(string, exception);
    }
}

