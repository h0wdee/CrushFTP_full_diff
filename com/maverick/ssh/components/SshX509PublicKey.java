/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import java.security.cert.Certificate;

public interface SshX509PublicKey {
    public Certificate getCertificate();

    public Certificate[] getCertificateChain();
}

