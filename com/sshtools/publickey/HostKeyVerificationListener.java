/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.components.SshPublicKey;
import java.util.List;

public interface HostKeyVerificationListener {
    public void onInvalidHostEntry(String var1);

    public void onRevokedKey(String var1, SshPublicKey var2);

    public void onHostKeyMismatch(String var1, List<SshPublicKey> var2, SshPublicKey var3);

    public void onUnknownHost(String var1, SshPublicKey var2);
}

