/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.SshPublicKey;

public interface SshEd25519PublicKey
extends SshPublicKey {
    public byte[] getA();
}

