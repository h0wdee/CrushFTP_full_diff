/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce.client;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.DiffieHellmanGroups;
import com.maverick.ssh.components.jce.client.DiffieHellmanGroup;

public class DiffieHellmanGroup15Sha512
extends DiffieHellmanGroup {
    public static final String DIFFIE_HELLMAN_GROUP15_SHA512 = "diffie-hellman-group15-sha512";

    public DiffieHellmanGroup15Sha512() {
        super(DIFFIE_HELLMAN_GROUP15_SHA512, "SHA-512", DiffieHellmanGroups.group15, SecurityLevel.PARANOID, 15);
    }
}

