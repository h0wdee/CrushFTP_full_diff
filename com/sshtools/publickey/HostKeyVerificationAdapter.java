/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.components.SshPublicKey;
import com.sshtools.publickey.HostKeyVerificationListener;
import java.util.List;

public class HostKeyVerificationAdapter
implements HostKeyVerificationListener {
    @Override
    public void onInvalidHostEntry(String line) {
    }

    @Override
    public void onRevokedKey(String host, SshPublicKey pk) {
    }

    @Override
    public void onHostKeyMismatch(String host, List<SshPublicKey> allowedHostKeys, SshPublicKey pk) {
    }

    @Override
    public void onUnknownHost(String host, SshPublicKey pk) {
    }
}

