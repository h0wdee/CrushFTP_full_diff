/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce.client;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.client.DiffieHellmanGroupExchangeSha1;

public class DiffieHellmanGroupExchangeSha256
extends DiffieHellmanGroupExchangeSha1 {
    public static final String DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA256 = "diffie-hellman-group-exchange-sha256";

    public DiffieHellmanGroupExchangeSha256() {
        super("SHA-256", SecurityLevel.STRONG, 10);
    }

    @Override
    public String getAlgorithm() {
        return DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA256;
    }
}

