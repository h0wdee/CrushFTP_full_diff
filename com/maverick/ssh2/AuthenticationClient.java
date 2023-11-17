/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.ssh.SshAuthentication;
import com.maverick.ssh.SshException;
import com.maverick.ssh2.AuthenticationProtocol;
import com.maverick.ssh2.AuthenticationResult;

public interface AuthenticationClient
extends SshAuthentication {
    public void authenticate(AuthenticationProtocol var1, String var2) throws SshException, AuthenticationResult;
}

