/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LoggerContext
 *  org.apache.logging.log4j.spi.LoggerContext
 */
package org.apache.log4j.legacy.core;

import org.apache.logging.log4j.core.LoggerContext;

public final class ContextUtil {
    private ContextUtil() {
    }

    public static void reconfigure(org.apache.logging.log4j.spi.LoggerContext ctx) {
        if (ctx instanceof LoggerContext) {
            ((LoggerContext)ctx).reconfigure();
        }
    }
}

