/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.HmacSha256;

public class HmacSha256_96
extends HmacSha256 {
    public HmacSha256_96() {
        super(12);
    }

    @Override
    public String getAlgorithm() {
        return "hmac-sha2-256-96";
    }
}

