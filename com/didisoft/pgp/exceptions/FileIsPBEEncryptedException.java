/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.PGPException;

public class FileIsPBEEncryptedException
extends PGPException {
    private static final long serialVersionUID = 6762754694787728462L;

    public FileIsPBEEncryptedException(String string) {
        super(string);
    }

    public FileIsPBEEncryptedException(String string, Exception exception) {
        super(string, exception);
    }
}

