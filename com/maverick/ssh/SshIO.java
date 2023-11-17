/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SshIO {
    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;

    public void close() throws IOException;
}

