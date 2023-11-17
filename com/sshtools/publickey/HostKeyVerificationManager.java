/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HostKeyVerificationManager
implements HostKeyVerification {
    List<HostKeyVerification> verifiers = new ArrayList<HostKeyVerification>();

    public HostKeyVerificationManager(Collection<? extends HostKeyVerification> verifiers) {
        this.verifiers.addAll(verifiers);
    }

    public HostKeyVerificationManager(HostKeyVerification verif) {
        this.verifiers.add(verif);
    }

    public HostKeyVerificationManager(HostKeyVerification ... verifs) {
        this.verifiers.addAll(Arrays.asList(verifs));
    }

    public void addVerifier(HostKeyVerification verif) {
        this.verifiers.add(verif);
    }

    @Override
    public boolean verifyHost(String host, SshPublicKey pk) throws SshException {
        for (HostKeyVerification v : this.verifiers) {
            if (!v.verifyHost(host, pk)) continue;
            return true;
        }
        return true;
    }
}

