/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SecureComponent;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshComponent;
import java.io.IOException;

public interface SshKeyExchange
extends SshComponent,
SecureComponent {
    public String getHashAlgorithm();

    public void test() throws IOException, SshException;

    public String getProvider();
}

