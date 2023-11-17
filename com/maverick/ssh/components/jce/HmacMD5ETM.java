/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractHmac;

public class HmacMD5ETM
extends AbstractHmac {
    public HmacMD5ETM() {
        super("HmacMD5", 16, SecurityLevel.WEAK, 0);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-md5-etm@openssh.com";
    }

    @Override
    public boolean isETM() {
        return true;
    }
}

