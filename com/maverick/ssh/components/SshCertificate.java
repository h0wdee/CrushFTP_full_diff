/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.jce.OpenSshCertificate;

public class SshCertificate
extends SshKeyPair {
    public static final int SSH_CERT_TYPE_USER = 1;
    public static final int SSH_CERT_TYPE_HOST = 2;
    OpenSshCertificate certificate;

    public SshCertificate(SshKeyPair pair, OpenSshCertificate certificate) {
        this.certificate = certificate;
        this.setPrivateKey(pair.getPrivateKey());
        this.setPublicKey(pair.getPublicKey());
    }

    public boolean isUserCertificate() {
        return this.certificate.isUserCertificate();
    }

    public boolean isHostCertificate() {
        return this.certificate.isHostCertificate();
    }

    public OpenSshCertificate getCertificate() {
        return this.certificate;
    }
}

