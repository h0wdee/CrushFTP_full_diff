/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import com.maverick.logging.Log;
import com.maverick.logging.LoggerContext;

public interface RootLoggerContext
extends LoggerContext {
    public void enableConsole(Log.Level var1);

    public String getProperty(String var1, String var2);

    public void shutdown();
}

