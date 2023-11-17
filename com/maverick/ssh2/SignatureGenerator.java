/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;

public interface SignatureGenerator {
    public byte[] sign(SshPublicKey var1, byte[] var2) throws SshException;
}

