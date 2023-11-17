/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.OpenSshEcdsaCertificate;

public class OpenSshEcdsaSha2Nist521Certificate
extends OpenSshEcdsaCertificate {
    public static final String CERT_TYPE = "ecdsa-sha2-nistp521-cert-v01@openssh.com";

    public OpenSshEcdsaSha2Nist521Certificate() {
        super(CERT_TYPE, "SHA512withECDSA", "secp521r1");
    }

    @Override
    public int getPriority() {
        return super.getPriority() + 100;
    }
}

