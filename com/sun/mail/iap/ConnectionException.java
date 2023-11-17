/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.iap;

import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;

public class ConnectionException
extends ProtocolException {
    private transient Protocol p;
    private static final long serialVersionUID = 5749739604257464727L;

    public ConnectionException() {
    }

    public ConnectionException(String s) {
        super(s);
    }

    public ConnectionException(Protocol p, Response r) {
        super(r);
        this.p = p;
    }

    public Protocol getProtocol() {
        return this.p;
    }
}

