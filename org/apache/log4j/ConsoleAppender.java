/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class ConsoleAppender
extends AppenderSkeleton {
    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(LoggingEvent theEvent) {
    }
}

