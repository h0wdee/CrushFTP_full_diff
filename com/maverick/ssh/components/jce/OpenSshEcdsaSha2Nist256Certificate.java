/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.OpenSshEcdsaCertificate;

public class OpenSshEcdsaSha2Nist256Certificate
extends OpenSshEcdsaCertificate {
    public static final String CERT_TYPE = "ecdsa-sha2-nistp256-cert-v01@openssh.com";

    public OpenSshEcdsaSha2Nist256Certificate() {
        super(CERT_TYPE, "SHA256withECDSA", "secp256r1");
    }

    @Override
    public int getPriority() {
        return super.getPriority() + 100;
    }
}

