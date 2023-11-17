/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.SshPrivateKey;

public interface SshEd448PrivateKey
extends SshPrivateKey {
    public byte[] getSeed();
}

