/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.PGPException;

public class KeyIsRevokedException
extends PGPException {
    private static final long serialVersionUID = -5403405838368315892L;

    public KeyIsRevokedException(String string) {
        super(string);
    }

    public KeyIsRevokedException(String string, Exception exception) {
        super(string, exception);
    }
}

