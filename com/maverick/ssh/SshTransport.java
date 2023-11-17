/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshIO;
import java.io.IOException;

public interface SshTransport
extends SshIO {
    public String getHost();

    public int getPort();

    public SshTransport duplicate() throws IOException;
}

