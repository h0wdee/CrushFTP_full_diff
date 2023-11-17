/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce.client;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.DiffieHellmanGroups;
import com.maverick.ssh.components.jce.client.DiffieHellmanGroup;

public class DiffieHellmanGroup18Sha512
extends DiffieHellmanGroup {
    public static final String DIFFIE_HELLMAN_GROUP18_SHA512 = "diffie-hellman-group18-sha512";

    public DiffieHellmanGroup18Sha512() {
        super(DIFFIE_HELLMAN_GROUP18_SHA512, "SHA-512", DiffieHellmanGroups.group18, SecurityLevel.PARANOID, 18);
    }

    @Override
    protected void afterSentParameterE() throws SshException {
        if (AdaptiveConfiguration.getBoolean("simulateServerError", false, this.transport.getHost(), this.transport.getIdent())) {
            throw new SshException("Simulated invalid DH value", 3);
        }
    }
}

