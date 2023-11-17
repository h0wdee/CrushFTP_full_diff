/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.HmacSha512;

public class HmacSha512_96
extends HmacSha512 {
    public HmacSha512_96() {
        super(12);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-sha2-512-96";
    }
}

