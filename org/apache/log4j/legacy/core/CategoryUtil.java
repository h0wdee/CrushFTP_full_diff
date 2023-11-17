/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Logger
 *  org.apache.logging.log4j.spi.LoggerContext
 */
package org.apache.log4j.legacy.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContext;

public final class CategoryUtil {
    private CategoryUtil() {
    }

    public static boolean isAdditive(Logger logger) {
        if (logger instanceof org.apache.logging.log4j.core.Logger) {
            return ((org.apache.logging.log4j.core.Logger)logger).isAdditive();
        }
        return false;
    }

    public static void setAdditivity(Logger logger, boolean additivity) {
        if (logger instanceof org.apache.logging.log4j.core.Logger) {
            ((org.apache.logging.log4j.core.Logger)logger).setAdditive(additivity);
        }
    }

    public static Logger getParent(Logger logger) {
        if (logger instanceof org.apache.logging.log4j.core.Logger) {
            return ((org.apache.logging.log4j.core.Logger)logger).getParent();
        }
        return null;
    }

    public static LoggerContext getLoggerContext(Logger logger) {
        if (logger instanceof org.apache.logging.log4j.core.Logger) {
            return ((org.apache.logging.log4j.core.Logger)logger).getContext();
        }
        return null;
    }

    public static void setLevel(Logger logger, Level level) {
        if (logger instanceof org.apache.logging.log4j.core.Logger) {
            ((org.apache.logging.log4j.core.Logger)logger).setLevel(level);
        }
    }
}

