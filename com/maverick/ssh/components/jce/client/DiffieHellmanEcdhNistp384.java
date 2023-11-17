/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce.client;

import com.maverick.ssh.components.jce.client.DiffieHellmanEcdh;

public class DiffieHellmanEcdhNistp384
extends DiffieHellmanEcdh {
    public DiffieHellmanEcdhNistp384() {
        super("ecdh-sha2-nistp384", "secp384r1", "SHA-384", 2);
    }
}

