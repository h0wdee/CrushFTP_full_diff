/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshException;
import java.io.IOException;

public interface Client {
    public void exit() throws SshException, IOException;
}

