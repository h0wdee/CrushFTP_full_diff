/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class SimpleLayout
extends Layout {
    @Override
    public String format(LoggingEvent theEvent) {
        return "";
    }

    @Override
    public boolean ignoresThrowable() {
        return true;
    }
}

