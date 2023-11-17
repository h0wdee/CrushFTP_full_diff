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

public class HmacSha196
extends AbstractHmac {
    public HmacSha196() {
        super("HmacSha1", 20, 12, SecurityLevel.WEAK, 2);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-sha1-96";
    }

    @Override
    public void init(byte[] keydata) throws SshException {
        try {
            this.mac = JCEProvider.getProviderForAlgorithm(this.jceAlgorithm) == null ? Mac.getInstance(this.jceAlgorithm) : Mac.getInstance(this.jceAlgorithm, JCEProvider.getProviderForAlgorithm(this.jceAlgorithm));
            byte[] key = new byte[System.getProperty("miscomputes.ssh2.hmac.keys", "false").equalsIgnoreCase("true") ? 16 : 20];
            System.arraycopy(keydata, 0, key, 0, key.length);
            SecretKeySpec keyspec = new SecretKeySpec(key, this.jceAlgorithm);
            this.mac.init(keyspec);
        }
        catch (Throwable t) {
            throw new SshException(t);
        }
    }
}

