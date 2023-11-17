/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.CertificateExtension;

public class NamedCertificateExtension
extends CertificateExtension {
    public NamedCertificateExtension(String name, boolean known) {
        this.setName(name);
        this.setKnown(known);
    }
}

