/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.components.SshPublicKey;
import java.io.IOException;

public interface SshPublicKeyFile {
    public SshPublicKey toPublicKey() throws IOException;

    public String getComment();

    public byte[] getFormattedKey() throws IOException;

    public String getOptions();
}

