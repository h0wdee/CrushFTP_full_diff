/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.DefaultCertificateExtension;
import com.maverick.ssh.components.jce.EncodedExtension;
import com.maverick.ssh.components.jce.NamedCertificateExtension;
import com.maverick.ssh.components.jce.StringCertificateExtension;
import java.util.ArrayList;
import java.util.List;

public abstract class CertificateExtension
extends EncodedExtension {
    static final CertificateExtension NO_PRESENCE_REQUIRED = new NamedCertificateExtension("no-presence-required", true);
    static final CertificateExtension PERMIT_X11_FORWARDING = new NamedCertificateExtension("permit-X11-forwarding", true);
    static final CertificateExtension PERMIT_AGENT_FORWARDING = new NamedCertificateExtension("permit-agent-forwarding", true);
    static final CertificateExtension PERMIT_PORT_FORWARDING = new NamedCertificateExtension("permit-port-forwarding", true);
    static final CertificateExtension PERMIT_PTY = new NamedCertificateExtension("permit-pty", true);
    static final CertificateExtension PERMIT_USER_RC = new NamedCertificateExtension("permit-user-rc", true);

    public static CertificateExtension createKnownExtension(String name, byte[] value) {
        switch (name) {
            case "no-presence-required": 
            case "permit-X11-forwarding": 
            case "permit-agent-forwarding": 
            case "permit-port-forwarding": 
            case "permit-pty": 
            case "permit-user-rc": {
                return new NamedCertificateExtension(name, true);
            }
            case "force-command": 
            case "source-address": {
                return new StringCertificateExtension(name, value, true);
            }
        }
        return new DefaultCertificateExtension(name, value);
    }

    public static class Builder {
        List<CertificateExtension> tmp = new ArrayList<CertificateExtension>();

        public Builder defaultExtensions() {
            this.tmp.add(PERMIT_X11_FORWARDING);
            this.tmp.add(PERMIT_AGENT_FORWARDING);
            this.tmp.add(PERMIT_PORT_FORWARDING);
            this.tmp.add(PERMIT_PTY);
            this.tmp.add(PERMIT_USER_RC);
            return this;
        }

        public Builder knownExtension(CertificateExtension ext) {
            if (!ext.isKnown()) {
                throw new IllegalArgumentException("Extension instance provided is not a known extension!");
            }
            this.tmp.add(ext);
            return this;
        }

        public Builder customNamedExtension(String name) {
            this.tmp.add(new NamedCertificateExtension(name, false));
            return this;
        }

        public Builder customStringExtension(String name, String value) {
            this.tmp.add(new StringCertificateExtension(name, value, false));
            return this;
        }

        public List<CertificateExtension> build() {
            return this.tmp;
        }
    }
}

