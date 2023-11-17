/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.spi.LoggerContext
 */
package org.apache.log4j;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.NullEnumeration;
import org.apache.log4j.legacy.core.ContextUtil;
import org.apache.log4j.or.ObjectRenderer;
import org.apache.log4j.or.RendererSupport;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.logging.log4j.spi.LoggerContext;

public final class LogManager {
    @Deprecated
    public static final String DEFAULT_CONFIGURATION_FILE = "log4j.properties";
    @Deprecated
    public static final String DEFAULT_CONFIGURATION_KEY = "log4j.configuration";
    @Deprecated
    public static final String CONFIGURATOR_CLASS_KEY = "log4j.configuratorClass";
    @Deprecated
    public static final String DEFAULT_INIT_OVERRIDE_KEY = "log4j.defaultInitOverride";
    static final String DEFAULT_XML_CONFIGURATION_FILE = "log4j.xml";
    private static final LoggerRepository REPOSITORY = new Repository();
    private static final boolean isLog4jCore;

    private LogManager() {
    }

    public static Logger getRootLogger() {
        return Category.getInstance(PrivateManager.getContext(), "");
    }

    public static Logger getLogger(String name) {
        return Category.getInstance(PrivateManager.getContext(), name);
    }

    public static Logger getLogger(Class<?> clazz) {
        return Category.getInstance(PrivateManager.getContext(), clazz.getName());
    }

    public static Logger getLogger(String name, LoggerFactory factory) {
        return Category.getInstance(PrivateManager.getContext(), name);
    }

    public static Logger exists(String name) {
        LoggerContext ctx = PrivateManager.getContext();
        if (!ctx.hasLogger(name)) {
            return null;
        }
        return Logger.getLogger(name);
    }

    public static Enumeration getCurrentLoggers() {
        return NullEnumeration.getInstance();
    }

    static void reconfigure() {
        if (isLog4jCore) {
            LoggerContext ctx = PrivateManager.getContext();
            ContextUtil.reconfigure(ctx);
        }
    }

    public static void shutdown() {
    }

    public static void resetConfiguration() {
    }

    public static void setRepositorySelector(RepositorySelector selector, Object guard) throws IllegalArgumentException {
    }

    public static LoggerRepository getLoggerRepository() {
        return REPOSITORY;
    }

    static {
        boolean core = false;
        try {
            if (Class.forName("org.apache.logging.log4j.core.LoggerContext") != null) {
                core = true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        isLog4jCore = core;
    }

    private static class PrivateManager
    extends org.apache.logging.log4j.LogManager {
        private static final String FQCN = LogManager.class.getName();

        private PrivateManager() {
        }

        public static LoggerContext getContext() {
            return PrivateManager.getContext((String)FQCN, (boolean)false);
        }

        public static org.apache.logging.log4j.Logger getLogger(String name) {
            return PrivateManager.getLogger((String)FQCN, (String)name);
        }
    }

    private static class Repository
    implements LoggerRepository,
    RendererSupport {
        private final Map<Class<?>, ObjectRenderer> rendererMap = new HashMap();

        private Repository() {
        }

        @Override
        public Map<Class<?>, ObjectRenderer> getRendererMap() {
            return this.rendererMap;
        }

        @Override
        public void addHierarchyEventListener(HierarchyEventListener listener) {
        }

        @Override
        public boolean isDisabled(int level) {
            return false;
        }

        @Override
        public void setThreshold(Level level) {
        }

        @Override
        public void setThreshold(String val) {
        }

        @Override
        public void emitNoAppenderWarning(Category cat) {
        }

        @Override
        public Level getThreshold() {
            return Level.OFF;
        }

        @Override
        public Logger getLogger(String name) {
            return Category.getInstance(PrivateManager.getContext(), name);
        }

        @Override
        public Logger getLogger(String name, LoggerFactory factory) {
            return Category.getInstance(PrivateManager.getContext(), name);
        }

        @Override
        public Logger getRootLogger() {
            return Category.getRoot(PrivateManager.getContext());
        }

        @Override
        public Logger exists(String name) {
            return LogManager.exists(name);
        }

        @Override
        public void shutdown() {
        }

        @Override
        public Enumeration getCurrentLoggers() {
            return NullEnumeration.getInstance();
        }

        @Override
        public Enumeration getCurrentCategories() {
            return NullEnumeration.getInstance();
        }

        @Override
        public void fireAddAppenderEvent(Category logger, Appender appender) {
        }

        @Override
        public void resetConfiguration() {
        }
    }
}

