/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.Layout
 */
package org.apache.log4j.bridge;

import org.apache.log4j.Layout;
import org.apache.log4j.bridge.LogEventAdapter;
import org.apache.log4j.spi.LoggingEvent;

public class LayoutWrapper
extends Layout {
    private final org.apache.logging.log4j.core.Layout<?> layout;

    public LayoutWrapper(org.apache.logging.log4j.core.Layout<?> layout) {
        this.layout = layout;
    }

    @Override
    public String format(LoggingEvent event) {
        return this.layout.toSerializable(((LogEventAdapter)event).getEvent()).toString();
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    public org.apache.logging.log4j.core.Layout<?> getLayout() {
        return this.layout;
    }
}

