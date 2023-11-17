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

public class HmacSha256_at_ssh_dot_com
extends AbstractHmac {
    public HmacSha256_at_ssh_dot_com() {
        super("HmacSha256", 32, SecurityLevel.PARANOID, 1);
    }

    protected HmacSha256_at_ssh_dot_com(int size) {
        super("HmacSha256", 32, size, SecurityLevel.PARANOID, 1);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-sha256@ssh.com";
    }

    @Override
    public void init(byte[] keydata) throws SshException {
        try {
            this.mac = JCEProvider.getProviderForAlgorithm(this.jceAlgorithm) == null ? Mac.getInstance(this.jceAlgorithm) : Mac.getInstance(this.jceAlgorithm, JCEProvider.getProviderForAlgorithm(this.jceAlgorithm));
            byte[] key = new byte[16];
            System.arraycopy(keydata, 0, key, 0, key.length);
            SecretKeySpec keyspec = new SecretKeySpec(key, this.jceAlgorithm);
            this.mac.init(keyspec);
        }
        catch (Throwable t) {
            throw new SshException(t);
        }
    }
}

