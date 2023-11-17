/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.ShellWriter;
import java.io.IOException;

public interface ShellStartupTrigger {
    public boolean canStartShell(String var1, ShellWriter var2) throws IOException;
}

