/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.jce.AbstractHmac;

public class HmacMD596
extends AbstractHmac {
    public HmacMD596() {
        super("HmacMD5", 16, 12, SecurityLevel.WEAK, 0);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-md5-96";
    }
}

