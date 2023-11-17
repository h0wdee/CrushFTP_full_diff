/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce.client;

import com.maverick.ssh.components.jce.client.DiffieHellmanEcdh;

public class DiffieHellmanEcdhNistp521
extends DiffieHellmanEcdh {
    public DiffieHellmanEcdhNistp521() {
        super("ecdh-sha2-nistp521", "secp521r1", "SHA-512", 3);
    }
}

