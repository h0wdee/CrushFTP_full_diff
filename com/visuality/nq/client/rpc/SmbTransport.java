/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.User;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;

class SmbTransport
extends Dcerpc.Transport {
    private Dcerpc rpc;
    private Mount ipc = null;

    public SmbTransport(Dcerpc rpc, Credentials creds) throws NqException {
        this.rpc = rpc;
        TraceLog.get().enter(2000);
        File.Params params = new File.Params(135397791, 7, 1);
        try {
            this.ipc = null == rpc.server ? new Mount(rpc.serverName, "IPC$", creds, false) : new Mount(rpc.server, "IPC$", creds, false);
            rpc.create(this.ipc, rpc.rpcDescr.name, params);
        }
        catch (NqException e) {
            if (-1073741790 == e.getErrCode()) {
                this.close();
                try {
                    this.ipc = new Mount(rpc.serverName, "IPC$");
                    rpc.create(this.ipc, rpc.rpcDescr.name, params);
                }
                catch (NqException e1) {
                    TraceLog.get().caught(e1, 2000);
                    this.close();
                    throw e1;
                }
            }
            TraceLog.get().caught(e, 2000);
            this.close();
            throw e;
        }
        TraceLog.get().exit(2000);
    }

    void close() {
        TraceLog.get().enter(2000);
        if (null != this.ipc) {
            User user = (User)this.ipc.getServer().users.get(this.rpc.credentials.getKey());
            if (null != user) {
                TraceLog.get().message("user = ", user, 2000);
                try {
                    user.logoff();
                }
                catch (NqException e) {
                    TraceLog.get().error("Unable to logoff ipc user ", user);
                }
            }
            this.ipc.close();
            User.checkRemoveUser(this.ipc.getServer(), this.rpc.credentials.getKey());
            this.ipc = null;
        }
        TraceLog.get().exit(2000);
    }

    void write(Buffer packet) throws NqException {
        this.rpc.setPosition(0L);
        this.rpc.write(packet);
    }

    int read(Buffer buffer) throws NqException {
        return (int)this.rpc.read(buffer);
    }
}

