/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.SshCipher;
import java.io.IOException;

public class NoneCipher
extends SshCipher {
    public NoneCipher() {
        super("none", SecurityLevel.NONE);
    }

    @Override
    public int getBlockSize() {
        return 8;
    }

    @Override
    public int getKeyLength() {
        return 8;
    }

    @Override
    public void init(int mode, byte[] iv, byte[] keydata) throws IOException {
    }

    @Override
    public void transform(byte[] src, int start, byte[] dest, int offset, int len) throws IOException {
    }

    @Override
    public String getProviderName() {
        return "None";
    }
}

