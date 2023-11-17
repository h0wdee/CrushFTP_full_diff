/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractHmac;

public class HmacRipeMd160_at_openssh_dot_com
extends AbstractHmac {
    public HmacRipeMd160_at_openssh_dot_com() {
        super("HmacRipeMd160", 20, SecurityLevel.WEAK, 1);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-ripemd160@openssh.com";
    }
}

