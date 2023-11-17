/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractHmac;

public class HmacMD5
extends AbstractHmac {
    public HmacMD5() {
        super("HmacMD5", 16, SecurityLevel.WEAK, 0);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-md5";
    }
}

