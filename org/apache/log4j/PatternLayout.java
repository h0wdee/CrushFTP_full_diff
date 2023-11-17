/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class PatternLayout
extends Layout {
    public PatternLayout(String pattern) {
    }

    @Override
    public String format(LoggingEvent event) {
        return "";
    }

    @Override
    public boolean ignoresThrowable() {
        return true;
    }
}

