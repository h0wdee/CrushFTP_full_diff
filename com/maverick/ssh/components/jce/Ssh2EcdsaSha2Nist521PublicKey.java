/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPublicKey;

public class Ssh2EcdsaSha2Nist521PublicKey
extends Ssh2EcdsaSha2NistPublicKey {
    public Ssh2EcdsaSha2Nist521PublicKey() {
        super("ecdsa-sha2-nistp521", "SHA512withECDSA", "secp521r1", "nistp521");
    }

    @Override
    public byte[] getOid() {
        return new byte[]{43, -127, 4, 0, 35};
    }
}

