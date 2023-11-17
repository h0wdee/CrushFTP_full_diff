/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.PGPException;

public class FileIsEncryptedException
extends PGPException {
    private static final long serialVersionUID = -7719881786646542875L;

    public FileIsEncryptedException(String string) {
        super(string);
    }

    public FileIsEncryptedException(String string, Exception exception) {
        super(string, exception);
    }
}

