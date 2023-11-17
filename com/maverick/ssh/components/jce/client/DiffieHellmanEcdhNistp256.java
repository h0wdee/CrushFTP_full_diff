/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce.client;

import com.maverick.ssh.components.jce.client.DiffieHellmanEcdh;

public class DiffieHellmanEcdhNistp256
extends DiffieHellmanEcdh {
    public DiffieHellmanEcdhNistp256() {
        super("ecdh-sha2-nistp256", "secp256r1", "SHA-256", 1);
    }
}

