/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.iap;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;

public class LiteralException
extends ProtocolException {
    private static final long serialVersionUID = -6919179828339609913L;

    public LiteralException(Response r) {
        super(r.toString());
        this.response = r;
    }
}

