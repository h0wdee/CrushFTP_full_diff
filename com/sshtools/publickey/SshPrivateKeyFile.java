/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.components.SshKeyPair;
import com.sshtools.publickey.InvalidPassphraseException;
import java.io.IOException;

public interface SshPrivateKeyFile {
    public boolean isPassphraseProtected();

    public SshKeyPair toKeyPair(String var1) throws IOException, InvalidPassphraseException;

    public boolean supportsPassphraseChange();

    public String getType();

    public void changePassphrase(String var1, String var2) throws IOException, InvalidPassphraseException;

    public byte[] getFormattedKey() throws IOException;
}

