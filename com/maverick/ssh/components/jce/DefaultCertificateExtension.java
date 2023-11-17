/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.CertificateExtension;

public class DefaultCertificateExtension
extends CertificateExtension {
    public DefaultCertificateExtension(String name, byte[] value) {
        this.setName(name);
        this.setStoredValue(value);
    }
}

