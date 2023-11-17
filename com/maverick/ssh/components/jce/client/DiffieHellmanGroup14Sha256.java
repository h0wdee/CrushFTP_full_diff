/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce.client;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.DiffieHellmanGroups;
import com.maverick.ssh.components.jce.client.DiffieHellmanGroup;

public class DiffieHellmanGroup14Sha256
extends DiffieHellmanGroup {
    public static final String DIFFIE_HELLMAN_GROUP14_SHA256 = "diffie-hellman-group14-sha256";

    public DiffieHellmanGroup14Sha256() {
        super(DIFFIE_HELLMAN_GROUP14_SHA256, "SHA-256", DiffieHellmanGroups.group14, SecurityLevel.STRONG, 14);
    }
}

