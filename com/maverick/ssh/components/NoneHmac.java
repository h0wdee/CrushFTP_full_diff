/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshHmac;

public class NoneHmac
implements SshHmac {
    @Override
    public int getMacSize() {
        return 0;
    }

    @Override
    public int getMacLength() {
        return 0;
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
    public void generate(long sequenceNo, byte[] data, int offset, int len, byte[] output, int start) {
    }

    @Override
    public void init(byte[] keydata) throws SshException {
    }

    @Override
    public boolean verify(long sequenceNo, byte[] data, int start, int len, byte[] mac, int offset) {
        return true;
    }

    @Override
    public void update(byte[] b) {
    }

    @Override
    public byte[] doFinal() {
        return new byte[0];
    }

    @Override
    public String getAlgorithm() {
        return "none";
    }

    @Override
    public boolean isETM() {
        return false;
    }
}

