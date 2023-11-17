/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import java.io.IOException;

public interface ShellWriter {
    public void interrupt() throws IOException;

    public void type(String var1) throws IOException;

    public void carriageReturn() throws IOException;

    public void typeAndReturn(String var1) throws IOException;
}

