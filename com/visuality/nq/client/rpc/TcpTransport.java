/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.NqException;

class TcpTransport
extends Dcerpc.Transport {
    protected TcpTransport(Dcerpc rpc, Credentials creds) throws NqException {
        throw new NqException(-22);
    }

    void write(Buffer packet) throws NqException {
    }

    int read(Buffer packet) throws NqException {
        return 0;
    }

    void close() {
    }
}

