/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.PGPException;

public class NonPGPDataException
extends PGPException {
    private static final long serialVersionUID = -4989400714535054834L;

    public NonPGPDataException(String string) {
        super(string);
    }

    public NonPGPDataException(String string, Exception exception) {
        super(string, exception);
    }
}

