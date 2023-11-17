/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.exceptions;

import com.didisoft.pgp.PGPException;

public class DetachedSignatureException
extends PGPException {
    private static final long serialVersionUID = -4230922249681682790L;

    public DetachedSignatureException(String string) {
        super(string);
    }

    public DetachedSignatureException(String string, Exception exception) {
        super(string, exception);
    }
}

