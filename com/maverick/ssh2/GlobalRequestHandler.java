/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.ssh.SshException;
import com.maverick.ssh2.GlobalRequest;

public interface GlobalRequestHandler {
    public String[] supportedRequests();

    public boolean processGlobalRequest(GlobalRequest var1) throws SshException;
}

