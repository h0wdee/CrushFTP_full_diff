/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.jce.AbstractHmac;
import com.maverick.ssh.components.jce.JCEProvider;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSha512ETM
extends AbstractHmac {
    public HmacSha512ETM() {
        super("HmacSha512", 64, SecurityLevel.PARANOID, 1);
    }

    protected HmacSha512ETM(int size) {
        super("HmacSha512", 64, size, SecurityLevel.PARANOID, 1);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-sha2-512-etm@openssh.com";
    }

    @Override
    public boolean isETM() {
        return true;
    }

    @Override
    public void init(byte[] keydata) throws SshException {
        try {
            this.mac = JCEProvider.getProviderForAlgorithm(this.jceAlgorithm) == null ? Mac.getInstance(this.jceAlgorithm) : Mac.getInstance(this.jceAlgorithm, JCEProvider.getProviderForAlgorithm(this.jceAlgorithm));
            byte[] key = new byte[64];
            System.arraycopy(keydata, 0, key, 0, key.length);
            SecretKeySpec keyspec = new SecretKeySpec(key, this.jceAlgorithm);
            this.mac.init(keyspec);
        }
        catch (Throwable t) {
            throw new SshException(t);
        }
    }
}

