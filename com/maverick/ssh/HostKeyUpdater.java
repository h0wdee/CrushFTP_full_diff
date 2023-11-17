/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;

public interface HostKeyUpdater {
    public boolean isKnownHost(String var1, SshPublicKey var2) throws SshException;

    public void updateHostKey(String var1, SshPublicKey var2) throws SshException;
}

