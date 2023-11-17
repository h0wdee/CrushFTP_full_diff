/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPublicKey;

public class Ssh2EcdsaSha2Nist384PublicKey
extends Ssh2EcdsaSha2NistPublicKey {
    public Ssh2EcdsaSha2Nist384PublicKey() {
        super("ecdsa-sha2-nistp384", "SHA384withECDSA", "secp384r1", "nistp384");
    }

    @Override
    public byte[] getOid() {
        return new byte[]{43, -127, 4, 0, 34};
    }
}

