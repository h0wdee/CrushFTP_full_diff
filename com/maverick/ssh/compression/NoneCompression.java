/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.compression;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.compression.SshCompression;
import java.io.IOException;

public class NoneCompression
implements SshCompression {
    @Override
    public void init(int type, int level) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] compress(byte[] data, int start, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] uncompress(byte[] data, int start, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAlgorithm() {
        return "none";
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.NONE;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isDelayed() {
        return false;
    }
}

