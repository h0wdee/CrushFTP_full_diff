/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.zlib;

import com.sshtools.zlib.ZLibCompression;

public class OpenSSHZLibCompression
extends ZLibCompression {
    @Override
    public String getAlgorithm() {
        return "zlib@openssh.com";
    }

    @Override
    public boolean isDelayed() {
        return true;
    }
}

