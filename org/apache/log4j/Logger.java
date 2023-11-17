/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.spi.LoggerContext
 */
package org.apache.log4j;

import org.apache.log4j.Category;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerContext;

public class Logger
extends Category {
    protected Logger(String name) {
        super(PrivateManager.getContext(), name);
    }

    Logger(LoggerContext context, String name) {
        super(context, name);
    }

    public static Logger getLogger(String name) {
        return Category.getInstance(PrivateManager.getContext(), name);
    }

    public static Logger getLogger(Class<?> clazz) {
        return Category.getInstance(PrivateManager.getContext(), clazz);
    }

    public static Logger getRootLogger() {
        return Category.getRoot(PrivateManager.getContext());
    }

    public static Logger getLogger(String name, LoggerFactory factory) {
        return Category.getInstance(PrivateManager.getContext(), name, factory);
    }

    private static class PrivateManager
    extends LogManager {
        private static final String FQCN = Logger.class.getName();

        private PrivateManager() {
        }

        public static LoggerContext getContext() {
            return PrivateManager.getContext((String)FQCN, (boolean)false);
        }

        public static org.apache.logging.log4j.Logger getLogger(String name) {
            return PrivateManager.getLogger((String)FQCN, (String)name);
        }
    }
}

