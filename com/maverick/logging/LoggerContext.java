/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import com.maverick.logging.Log;

public interface LoggerContext {
    public boolean isLogging(Log.Level var1);

    public void log(Log.Level var1, String var2, Throwable var3, Object ... var4);

    public void raw(Log.Level var1, String var2);

    public void close();

    public void newline();
}

