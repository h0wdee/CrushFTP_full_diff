/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPublicKey;

public class Ssh2EcdsaSha2Nist256PublicKey
extends Ssh2EcdsaSha2NistPublicKey {
    public Ssh2EcdsaSha2Nist256PublicKey() {
        super("ecdsa-sha2-nistp256", "SHA256withECDSA", "secp256r1", "nistp256");
    }

    @Override
    public byte[] getOid() {
        return new byte[]{42, -122, 72, -50, 61, 3, 1, 7};
    }
}

