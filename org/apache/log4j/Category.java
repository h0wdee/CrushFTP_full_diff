/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.message.LocalizedMessage
 *  org.apache.logging.log4j.message.MapMessage
 *  org.apache.logging.log4j.message.Message
 *  org.apache.logging.log4j.message.ObjectMessage
 *  org.apache.logging.log4j.message.SimpleMessage
 *  org.apache.logging.log4j.spi.AbstractLoggerAdapter
 *  org.apache.logging.log4j.spi.ExtendedLogger
 *  org.apache.logging.log4j.spi.LoggerContext
 *  org.apache.logging.log4j.util.Strings
 */
package org.apache.log4j;

import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.RenderedMessage;
import org.apache.log4j.helpers.NullEnumeration;
import org.apache.log4j.legacy.core.CategoryUtil;
import org.apache.log4j.or.ObjectRenderer;
import org.apache.log4j.or.RendererSupport;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.message.LocalizedMessage;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.AbstractLoggerAdapter;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.Strings;

public class Category {
    private static PrivateAdapter adapter;
    private static final Map<LoggerContext, ConcurrentMap<String, Logger>> CONTEXT_MAP;
    private static final String FQCN;
    private static final boolean isCoreAvailable;
    private final Map<Class<?>, ObjectRenderer> rendererMap;
    protected ResourceBundle bundle = null;
    private final org.apache.logging.log4j.Logger logger;

    protected Category(LoggerContext context, String name) {
        this.logger = context.getLogger(name);
        this.rendererMap = ((RendererSupport)((Object)LogManager.getLoggerRepository())).getRendererMap();
    }

    protected Category(String name) {
        this(PrivateManager.getContext(), name);
    }

    private Category(org.apache.logging.log4j.Logger logger) {
        this.logger = logger;
        this.rendererMap = ((RendererSupport)((Object)LogManager.getLoggerRepository())).getRendererMap();
    }

    public static Category getInstance(String name) {
        return Category.getInstance(PrivateManager.getContext(), name, adapter);
    }

    static Logger getInstance(LoggerContext context, String name) {
        return Category.getInstance(context, name, adapter);
    }

    static Logger getInstance(LoggerContext context, String name, LoggerFactory factory) {
        ConcurrentMap<String, Logger> loggers = Category.getLoggersMap(context);
        Logger logger = (Logger)loggers.get(name);
        if (logger != null) {
            return logger;
        }
        logger = factory.makeNewLoggerInstance(name);
        Logger prev = loggers.putIfAbsent(name, logger);
        return prev == null ? logger : prev;
    }

    static Logger getInstance(LoggerContext context, String name, PrivateAdapter factory) {
        ConcurrentMap<String, Logger> loggers = Category.getLoggersMap(context);
        Logger logger = (Logger)loggers.get(name);
        if (logger != null) {
            return logger;
        }
        logger = factory.newLogger(name, context);
        Logger prev = loggers.putIfAbsent(name, logger);
        return prev == null ? logger : prev;
    }

    public static Category getInstance(Class clazz) {
        return Category.getInstance(clazz.getName());
    }

    static Logger getInstance(LoggerContext context, Class clazz) {
        return Category.getInstance(context, clazz.getName());
    }

    public final String getName() {
        return this.logger.getName();
    }

    org.apache.logging.log4j.Logger getLogger() {
        return this.logger;
    }

    public final Category getParent() {
        if (!isCoreAvailable) {
            return null;
        }
        org.apache.logging.log4j.Logger parent = CategoryUtil.getParent(this.logger);
        LoggerContext loggerContext = CategoryUtil.getLoggerContext(this.logger);
        if (parent == null || loggerContext == null) {
            return null;
        }
        ConcurrentMap<String, Logger> loggers = Category.getLoggersMap(loggerContext);
        Logger l = (Logger)loggers.get(parent.getName());
        return l == null ? new Category(parent) : l;
    }

    public static Category getRoot() {
        return Category.getInstance("");
    }

    static Logger getRoot(LoggerContext context) {
        return Category.getInstance(context, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ConcurrentMap<String, Logger> getLoggersMap(LoggerContext context) {
        Map<LoggerContext, ConcurrentMap<String, Logger>> map = CONTEXT_MAP;
        synchronized (map) {
            ConcurrentMap<String, Logger> map2 = CONTEXT_MAP.get(context);
            if (map2 == null) {
                map2 = new ConcurrentHashMap<String, Logger>();
                CONTEXT_MAP.put(context, map2);
            }
            return map2;
        }
    }

    @Deprecated
    public static Enumeration getCurrentCategories() {
        return LogManager.getCurrentLoggers();
    }

    public final Level getEffectiveLevel() {
        switch (this.logger.getLevel().getStandardLevel()) {
            case ALL: {
                return Level.ALL;
            }
            case TRACE: {
                return Level.TRACE;
            }
            case DEBUG: {
                return Level.DEBUG;
            }
            case INFO: {
                return Level.INFO;
            }
            case WARN: {
                return Level.WARN;
            }
            case ERROR: {
                return Level.ERROR;
            }
            case FATAL: {
                return Level.FATAL;
            }
        }
        return Level.OFF;
    }

    public final Priority getChainedPriority() {
        return this.getEffectiveLevel();
    }

    public final Level getLevel() {
        return this.getEffectiveLevel();
    }

    private String getLevelStr(Priority priority) {
        return priority == null ? null : priority.levelStr;
    }

    public void setLevel(Level level) {
        this.setLevel(this.getLevelStr(level));
    }

    public final Level getPriority() {
        return this.getEffectiveLevel();
    }

    public void setPriority(Priority priority) {
        this.setLevel(this.getLevelStr(priority));
    }

    private void setLevel(String levelStr) {
        if (isCoreAvailable) {
            CategoryUtil.setLevel(this.logger, org.apache.logging.log4j.Level.toLevel((String)levelStr));
        }
    }

    public void debug(Object message) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.DEBUG, message, null);
    }

    public void debug(Object message, Throwable t) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.DEBUG, message, t);
    }

    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    public void error(Object message) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.ERROR, message, null);
    }

    public void error(Object message, Throwable t) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.ERROR, message, t);
    }

    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    public void warn(Object message) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.WARN, message, null);
    }

    public void warn(Object message, Throwable t) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.WARN, message, t);
    }

    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    public void fatal(Object message) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.FATAL, message, null);
    }

    public void fatal(Object message, Throwable t) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.FATAL, message, t);
    }

    public boolean isFatalEnabled() {
        return this.logger.isFatalEnabled();
    }

    public void info(Object message) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.INFO, message, null);
    }

    public void info(Object message, Throwable t) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.INFO, message, t);
    }

    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    public void trace(Object message) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.TRACE, message, null);
    }

    public void trace(Object message, Throwable t) {
        this.maybeLog(FQCN, org.apache.logging.log4j.Level.TRACE, message, t);
    }

    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    public boolean isEnabledFor(Priority level) {
        org.apache.logging.log4j.Level lvl = org.apache.logging.log4j.Level.toLevel((String)level.toString());
        return this.isEnabledFor(lvl);
    }

    public void addAppender(Appender appender) {
    }

    public void callAppenders(LoggingEvent event) {
    }

    public Enumeration getAllAppenders() {
        return NullEnumeration.getInstance();
    }

    public Appender getAppender(String name) {
        return null;
    }

    public boolean isAttached(Appender appender) {
        return false;
    }

    public void removeAllAppenders() {
    }

    public void removeAppender(Appender appender) {
    }

    public void removeAppender(String name) {
    }

    public static void shutdown() {
    }

    public void forcedLog(String fqcn, Priority level, Object message, Throwable t) {
        org.apache.logging.log4j.Level lvl = org.apache.logging.log4j.Level.toLevel((String)level.toString());
        if (this.logger instanceof ExtendedLogger) {
            Message msg = message instanceof Message ? (Message)message : (message instanceof Map ? new MapMessage((Map)message) : new ObjectMessage(message));
            ((ExtendedLogger)this.logger).logMessage(fqcn, lvl, null, msg, t);
        } else {
            ObjectRenderer renderer = this.get(message.getClass());
            Message msg = message instanceof Message ? (Message)message : (renderer != null ? new RenderedMessage(renderer, message) : new ObjectMessage(message));
            this.logger.log(lvl, msg, t);
        }
    }

    public boolean exists(String name) {
        return PrivateManager.getContext().hasLogger(name);
    }

    public boolean getAdditivity() {
        return isCoreAvailable ? CategoryUtil.isAdditive(this.logger) : false;
    }

    public void setAdditivity(boolean additivity) {
        if (isCoreAvailable) {
            CategoryUtil.setAdditivity(this.logger, additivity);
        }
    }

    public void setResourceBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public ResourceBundle getResourceBundle() {
        LoggerContext ctx;
        if (this.bundle != null) {
            return this.bundle;
        }
        String name = this.logger.getName();
        if (isCoreAvailable && (ctx = CategoryUtil.getLoggerContext(this.logger)) != null) {
            ConcurrentMap<String, Logger> loggers = Category.getLoggersMap(ctx);
            while ((name = Category.getSubName(name)) != null) {
                ResourceBundle rb;
                Logger subLogger = (Logger)loggers.get(name);
                if (subLogger == null || (rb = subLogger.bundle) == null) continue;
                return rb;
            }
        }
        return null;
    }

    private static String getSubName(String name) {
        if (Strings.isEmpty((CharSequence)name)) {
            return null;
        }
        int i = name.lastIndexOf(46);
        return i > 0 ? name.substring(0, i) : "";
    }

    public void assertLog(boolean assertion, String msg) {
        if (!assertion) {
            this.error(msg);
        }
    }

    public void l7dlog(Priority priority, String key, Throwable t) {
        if (this.isEnabledFor(priority)) {
            LocalizedMessage msg = new LocalizedMessage(this.bundle, key, null);
            this.forcedLog(FQCN, priority, msg, t);
        }
    }

    public void l7dlog(Priority priority, String key, Object[] params, Throwable t) {
        if (this.isEnabledFor(priority)) {
            LocalizedMessage msg = new LocalizedMessage(this.bundle, key, params);
            this.forcedLog(FQCN, priority, msg, t);
        }
    }

    public void log(Priority priority, Object message, Throwable t) {
        if (this.isEnabledFor(priority)) {
            MapMessage msg = message instanceof Map ? new MapMessage((Map)message) : new ObjectMessage(message);
            this.forcedLog(FQCN, priority, msg, t);
        }
    }

    public void log(Priority priority, Object message) {
        if (this.isEnabledFor(priority)) {
            MapMessage msg = message instanceof Map ? new MapMessage((Map)message) : new ObjectMessage(message);
            this.forcedLog(FQCN, priority, msg, null);
        }
    }

    public void log(String fqcn, Priority priority, Object message, Throwable t) {
        if (this.isEnabledFor(priority)) {
            ObjectMessage msg = new ObjectMessage(message);
            this.forcedLog(fqcn, priority, msg, t);
        }
    }

    private void maybeLog(String fqcn, org.apache.logging.log4j.Level level, Object message, Throwable throwable) {
        if (this.logger.isEnabled(level)) {
            SimpleMessage msg;
            if (message instanceof String) {
                msg = new SimpleMessage((String)message);
            } else if (message instanceof CharSequence) {
                msg = new SimpleMessage((CharSequence)message);
            } else if (message instanceof Map) {
                Map map = (Map)message;
                msg = new MapMessage(map);
            } else {
                msg = new ObjectMessage(message);
            }
            if (this.logger instanceof ExtendedLogger) {
                ((ExtendedLogger)this.logger).logMessage(fqcn, level, null, (Message)msg, throwable);
            } else {
                this.logger.log(level, (Message)msg, throwable);
            }
        }
    }

    private boolean isEnabledFor(org.apache.logging.log4j.Level level) {
        return this.logger.isEnabled(level);
    }

    private <T> ObjectRenderer get(Class<T> clazz) {
        ObjectRenderer renderer = null;
        for (Class<T> c = clazz; c != null; c = c.getSuperclass()) {
            renderer = this.rendererMap.get(c);
            if (renderer != null) {
                return renderer;
            }
            renderer = this.searchInterfaces(c);
            if (renderer == null) continue;
            return renderer;
        }
        return null;
    }

    ObjectRenderer searchInterfaces(Class<?> c) {
        Class<?>[] ia;
        ObjectRenderer renderer = this.rendererMap.get(c);
        if (renderer != null) {
            return renderer;
        }
        for (Class<?> clazz : ia = c.getInterfaces()) {
            renderer = this.searchInterfaces(clazz);
            if (renderer == null) continue;
            return renderer;
        }
        return null;
    }

    static {
        boolean available;
        adapter = new PrivateAdapter();
        CONTEXT_MAP = new WeakHashMap<LoggerContext, ConcurrentMap<String, Logger>>();
        FQCN = Category.class.getName();
        try {
            available = Class.forName("org.apache.logging.log4j.core.Logger") != null;
        }
        catch (Exception ex) {
            available = false;
        }
        isCoreAvailable = available;
    }

    private static class PrivateManager
    extends org.apache.logging.log4j.LogManager {
        private static final String FQCN = Category.class.getName();

        private PrivateManager() {
        }

        public static LoggerContext getContext() {
            return PrivateManager.getContext((String)FQCN, (boolean)false);
        }

        public static org.apache.logging.log4j.Logger getLogger(String name) {
            return PrivateManager.getLogger((String)FQCN, (String)name);
        }
    }

    private static class PrivateAdapter
    extends AbstractLoggerAdapter<Logger> {
        private PrivateAdapter() {
        }

        protected Logger newLogger(String name, LoggerContext context) {
            return new Logger(context, name);
        }

        protected LoggerContext getContext() {
            return PrivateManager.getContext();
        }
    }
}

