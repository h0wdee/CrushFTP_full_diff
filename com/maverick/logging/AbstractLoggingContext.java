/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import com.maverick.logging.Log;
import com.maverick.logging.LoggerContext;

public abstract class AbstractLoggingContext
implements LoggerContext {
    Log.Level level = Log.Level.INFO;

    public AbstractLoggingContext() {
    }

    public AbstractLoggingContext(Log.Level level) {
        this.level = level;
    }

    @Override
    public boolean isLogging(Log.Level level) {
        return this.level.ordinal() >= level.ordinal();
    }

    public Log.Level getLevel() {
        return this.level;
    }
}

