/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import java.io.IOException;

public interface SocketTimeoutSupport {
    public void setSoTimeout(int var1) throws IOException;

    public int getSoTimeout() throws IOException;
}

