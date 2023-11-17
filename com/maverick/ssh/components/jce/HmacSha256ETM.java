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

public class HmacSha256ETM
extends AbstractHmac {
    public HmacSha256ETM() {
        super("HmacSha256", 32, SecurityLevel.STRONG, 0);
    }

    protected HmacSha256ETM(int size) {
        super("HmacSha256", 32, size, SecurityLevel.STRONG, 0);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-sha2-256-etm@openssh.com";
    }

    @Override
    public boolean isETM() {
        return true;
    }

    @Override
    public void init(byte[] keydata) throws SshException {
        try {
            this.mac = JCEProvider.getProviderForAlgorithm(this.jceAlgorithm) == null ? Mac.getInstance(this.jceAlgorithm) : Mac.getInstance(this.jceAlgorithm, JCEProvider.getProviderForAlgorithm(this.jceAlgorithm));
            byte[] key = new byte[System.getProperty("miscomputes.ssh2.hmac.keys", "false").equalsIgnoreCase("true") ? 16 : 32];
            System.arraycopy(keydata, 0, key, 0, key.length);
            SecretKeySpec keyspec = new SecretKeySpec(key, this.jceAlgorithm);
            this.mac.init(keyspec);
        }
        catch (Throwable t) {
            throw new SshException(t);
        }
    }
}

