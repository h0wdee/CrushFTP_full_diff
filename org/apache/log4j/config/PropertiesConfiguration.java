/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.core.Appender
 *  org.apache.logging.log4j.core.LifeCycle$State
 *  org.apache.logging.log4j.core.LoggerContext
 *  org.apache.logging.log4j.core.config.Configuration
 *  org.apache.logging.log4j.core.config.ConfigurationSource
 *  org.apache.logging.log4j.core.config.LoggerConfig
 *  org.apache.logging.log4j.core.config.status.StatusConfiguration
 *  org.apache.logging.log4j.util.LoaderUtil
 */
package org.apache.log4j.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.bridge.AppenderAdapter;
import org.apache.log4j.bridge.AppenderWrapper;
import org.apache.log4j.config.Log4j1Configuration;
import org.apache.log4j.config.PropertiesConfigurationFactory;
import org.apache.log4j.config.PropertySetter;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.status.StatusConfiguration;
import org.apache.logging.log4j.util.LoaderUtil;

public class PropertiesConfiguration
extends Log4j1Configuration {
    private static final String CATEGORY_PREFIX = "log4j.category.";
    private static final String LOGGER_PREFIX = "log4j.logger.";
    private static final String ADDITIVITY_PREFIX = "log4j.additivity.";
    private static final String ROOT_CATEGORY_PREFIX = "log4j.rootCategory";
    private static final String ROOT_LOGGER_PREFIX = "log4j.rootLogger";
    private static final String APPENDER_PREFIX = "log4j.appender.";
    private static final String LOGGER_REF = "logger-ref";
    private static final String ROOT_REF = "root-ref";
    private static final String APPENDER_REF_TAG = "appender-ref";
    public static final long DEFAULT_DELAY = 60000L;
    public static final String DEBUG_KEY = "log4j.debug";
    private static final String INTERNAL_ROOT_NAME = "root";
    private final Map<String, Appender> registry = new HashMap<String, Appender>();

    public PropertiesConfiguration(LoggerContext loggerContext, ConfigurationSource source, int monitorIntervalSeconds) {
        super(loggerContext, source, monitorIntervalSeconds);
    }

    public void doConfigure() {
        InputStream is = this.getConfigurationSource().getInputStream();
        Properties props = new Properties();
        try {
            props.load(is);
        }
        catch (Exception e) {
            LOGGER.error("Could not read configuration file [{}].", (Object)this.getConfigurationSource().toString(), (Object)e);
            return;
        }
        this.doConfigure(props);
    }

    @Override
    public Configuration reconfigure() {
        try {
            ConfigurationSource source = this.getConfigurationSource().resetInputStream();
            if (source == null) {
                return null;
            }
            PropertiesConfigurationFactory factory = new PropertiesConfigurationFactory();
            PropertiesConfiguration config = (PropertiesConfiguration)factory.getConfiguration(this.getLoggerContext(), source);
            return config == null || config.getState() != LifeCycle.State.INITIALIZING ? null : config;
        }
        catch (IOException ex) {
            LOGGER.error("Cannot locate file {}: {}", (Object)this.getConfigurationSource(), (Object)ex);
            return null;
        }
    }

    private void doConfigure(Properties properties) {
        String status = "error";
        String value = properties.getProperty(DEBUG_KEY);
        if (value == null && (value = properties.getProperty("log4j.configDebug")) != null) {
            LOGGER.warn("[log4j.configDebug] is deprecated. Use [log4j.debug] instead.");
        }
        if (value != null) {
            status = OptionConverter.toBoolean(value, false) ? "debug" : "error";
        }
        StatusConfiguration statusConfig = new StatusConfiguration().withStatus(status);
        statusConfig.initialize();
        this.configureRoot(properties);
        this.parseLoggers(properties);
        LOGGER.debug("Finished configuring.");
    }

    private void configureRoot(Properties props) {
        String effectiveFrefix = ROOT_LOGGER_PREFIX;
        String value = OptionConverter.findAndSubst(ROOT_LOGGER_PREFIX, props);
        if (value == null) {
            value = OptionConverter.findAndSubst(ROOT_CATEGORY_PREFIX, props);
            effectiveFrefix = ROOT_CATEGORY_PREFIX;
        }
        if (value == null) {
            LOGGER.debug("Could not find root logger information. Is this OK?");
        } else {
            LoggerConfig root = this.getRootLogger();
            this.parseLogger(props, root, effectiveFrefix, INTERNAL_ROOT_NAME, value);
        }
    }

    private void parseLoggers(Properties props) {
        Enumeration<?> enumeration = props.propertyNames();
        while (enumeration.hasMoreElements()) {
            String key = Objects.toString(enumeration.nextElement(), null);
            if (!key.startsWith(CATEGORY_PREFIX) && !key.startsWith(LOGGER_PREFIX)) continue;
            String loggerName = null;
            if (key.startsWith(CATEGORY_PREFIX)) {
                loggerName = key.substring(CATEGORY_PREFIX.length());
            } else if (key.startsWith(LOGGER_PREFIX)) {
                loggerName = key.substring(LOGGER_PREFIX.length());
            }
            String value = OptionConverter.findAndSubst(key, props);
            LoggerConfig loggerConfig = this.getLogger(loggerName);
            if (loggerConfig == null) {
                boolean additivity = this.getAdditivityForLogger(props, loggerName);
                loggerConfig = new LoggerConfig(loggerName, Level.ERROR, additivity);
                this.addLogger(loggerName, loggerConfig);
            }
            this.parseLogger(props, loggerConfig, key, loggerName, value);
        }
    }

    private boolean getAdditivityForLogger(Properties props, String loggerName) {
        boolean additivity = true;
        String key = ADDITIVITY_PREFIX + loggerName;
        String value = OptionConverter.findAndSubst(key, props);
        LOGGER.debug("Handling {}=[{}]", (Object)key, (Object)value);
        if (value != null && !value.equals("")) {
            additivity = OptionConverter.toBoolean(value, true);
        }
        return additivity;
    }

    private void parseLogger(Properties props, LoggerConfig logger, String optionKey, String loggerName, String value) {
        LOGGER.debug("Parsing for [{}] with value=[{}].", (Object)loggerName, (Object)value);
        StringTokenizer st = new StringTokenizer(value, ",");
        if (!value.startsWith(",") && !value.equals("")) {
            if (!st.hasMoreTokens()) {
                return;
            }
            String levelStr = st.nextToken();
            LOGGER.debug("Level token is [{}].", (Object)levelStr);
            Level level = levelStr == null ? Level.ERROR : OptionConverter.convertLevel(levelStr, Level.DEBUG);
            logger.setLevel(level);
            LOGGER.debug("Logger {} level set to {}", (Object)loggerName, (Object)level);
        }
        while (st.hasMoreTokens()) {
            String appenderName = st.nextToken().trim();
            if (appenderName == null || appenderName.equals(",")) continue;
            LOGGER.debug("Parsing appender named \"{}\".", (Object)appenderName);
            Appender appender = this.parseAppender(props, appenderName);
            if (appender != null) {
                LOGGER.debug("Adding appender named [{}] to loggerConfig [{}].", (Object)appenderName, (Object)logger.getName());
                logger.addAppender(this.getAppender(appenderName), null, null);
                continue;
            }
            LOGGER.debug("Appender named [{}] not found.", (Object)appenderName);
        }
    }

    public Appender parseAppender(Properties props, String appenderName) {
        Appender appender = this.registry.get(appenderName);
        if (appender != null) {
            LOGGER.debug("Appender \"" + appenderName + "\" was already parsed.");
            return appender;
        }
        String prefix = APPENDER_PREFIX + appenderName;
        String layoutPrefix = prefix + ".layout";
        String filterPrefix = APPENDER_PREFIX + appenderName + ".filter.";
        String className = OptionConverter.findAndSubst(prefix, props);
        appender = this.manager.parseAppender(appenderName, className, prefix, layoutPrefix, filterPrefix, props, this);
        if (appender == null) {
            appender = this.buildAppender(appenderName, className, prefix, layoutPrefix, filterPrefix, props);
        } else {
            this.registry.put(appenderName, appender);
            if (appender instanceof AppenderWrapper) {
                this.addAppender(((AppenderWrapper)appender).getAppender());
            } else {
                this.addAppender((org.apache.logging.log4j.core.Appender)new AppenderAdapter(appender).getAdapter());
            }
        }
        return appender;
    }

    private Appender buildAppender(String appenderName, String className, String prefix, String layoutPrefix, String filterPrefix, Properties props) {
        ErrorHandler eh;
        Appender appender = (Appender)PropertiesConfiguration.newInstanceOf(className, "Appender");
        if (appender == null) {
            return null;
        }
        appender.setName(appenderName);
        appender.setLayout(this.parseLayout(layoutPrefix, appenderName, props));
        String errorHandlerPrefix = prefix + ".errorhandler";
        String errorHandlerClass = OptionConverter.findAndSubst(errorHandlerPrefix, props);
        if (errorHandlerClass != null && (eh = this.parseErrorHandler(props, errorHandlerPrefix, errorHandlerClass, appender)) != null) {
            appender.setErrorHandler(eh);
        }
        this.parseAppenderFilters(props, filterPrefix, appenderName);
        String[] keys = new String[]{layoutPrefix};
        this.addProperties(appender, keys, props, prefix);
        if (appender instanceof AppenderWrapper) {
            this.addAppender(((AppenderWrapper)appender).getAppender());
        } else {
            this.addAppender((org.apache.logging.log4j.core.Appender)new AppenderAdapter(appender).getAdapter());
        }
        this.registry.put(appenderName, appender);
        return appender;
    }

    public Layout parseLayout(String layoutPrefix, String appenderName, Properties props) {
        String layoutClass = OptionConverter.findAndSubst(layoutPrefix, props);
        if (layoutClass == null) {
            return null;
        }
        Layout layout = this.manager.parseLayout(layoutClass, layoutPrefix, props, this);
        if (layout == null) {
            layout = this.buildLayout(layoutPrefix, layoutClass, appenderName, props);
        }
        return layout;
    }

    private Layout buildLayout(String layoutPrefix, String className, String appenderName, Properties props) {
        Layout layout = (Layout)PropertiesConfiguration.newInstanceOf(className, "Layout");
        if (layout == null) {
            return null;
        }
        LOGGER.debug("Parsing layout options for \"{}\".", (Object)appenderName);
        PropertySetter.setProperties(layout, props, layoutPrefix + ".");
        LOGGER.debug("End of parsing for \"{}\".", (Object)appenderName);
        return layout;
    }

    public ErrorHandler parseErrorHandler(Properties props, String errorHandlerPrefix, String errorHandlerClass, Appender appender) {
        ErrorHandler eh = (ErrorHandler)PropertiesConfiguration.newInstanceOf(errorHandlerClass, "ErrorHandler");
        String[] keys = new String[]{errorHandlerPrefix + "." + ROOT_REF, errorHandlerPrefix + "." + LOGGER_REF, errorHandlerPrefix + "." + APPENDER_REF_TAG};
        this.addProperties(eh, keys, props, errorHandlerPrefix);
        return eh;
    }

    public void addProperties(Object obj, String[] keys, Properties props, String prefix) {
        Properties edited = new Properties();
        props.stringPropertyNames().stream().filter(name -> {
            if (name.startsWith(prefix)) {
                for (String key : keys) {
                    if (!name.equals(key)) continue;
                    return false;
                }
                return true;
            }
            return false;
        }).forEach(name -> edited.put(name, props.getProperty((String)name)));
        PropertySetter.setProperties(obj, edited, prefix + ".");
    }

    public Filter parseAppenderFilters(Properties props, String filterPrefix, String appenderName) {
        int fIdx = filterPrefix.length();
        TreeMap<String, List> filters = new TreeMap<String, List>();
        Enumeration<Object> e = props.keys();
        String name = "";
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            if (!key.startsWith(filterPrefix)) continue;
            int dotIdx = key.indexOf(46, fIdx);
            String filterKey = key;
            if (dotIdx != -1) {
                filterKey = key.substring(0, dotIdx);
                name = key.substring(dotIdx + 1);
            }
            List filterOpts = filters.computeIfAbsent(filterKey, k -> new ArrayList());
            if (dotIdx == -1) continue;
            String value = OptionConverter.findAndSubst(key, props);
            filterOpts.add(new NameValue(name, value));
        }
        Filter head = null;
        Filter next = null;
        for (Map.Entry entry : filters.entrySet()) {
            String clazz = props.getProperty((String)entry.getKey());
            Filter filter = null;
            if (clazz != null && (filter = this.manager.parseFilter(clazz, filterPrefix, props, this)) == null) {
                LOGGER.debug("Filter key: [{}] class: [{}] props: {}", entry.getKey(), (Object)clazz, entry.getValue());
                filter = this.buildFilter(clazz, appenderName, (List)entry.getValue());
            }
            if (filter == null) continue;
            if (head == null) {
                head = filter;
            } else {
                next.setNext(filter);
            }
            next = filter;
        }
        return head;
    }

    private Filter buildFilter(String className, String appenderName, List<NameValue> props) {
        Filter filter = (Filter)PropertiesConfiguration.newInstanceOf(className, "Filter");
        if (filter != null) {
            PropertySetter propSetter = new PropertySetter(filter);
            for (NameValue property : props) {
                propSetter.setProperty(property.key, property.value);
            }
            propSetter.activate();
        }
        return filter;
    }

    private static <T> T newInstanceOf(String className, String type) {
        try {
            return (T)LoaderUtil.newInstanceOf((String)className);
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            LOGGER.error("Unable to create {} {} due to {}:{}", (Object)type, (Object)className, (Object)ex.getClass().getSimpleName(), (Object)ex.getMessage());
            return null;
        }
    }

    private static class NameValue {
        String key;
        String value;

        NameValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String toString() {
            return this.key + "=" + this.value;
        }
    }
}

