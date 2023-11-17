/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.core.appender.ConsoleAppender$Target
 *  org.apache.logging.log4j.core.config.ConfigurationException
 *  org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder
 *  org.apache.logging.log4j.core.config.builder.api.ComponentBuilder
 *  org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder
 *  org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
 *  org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder
 *  org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder
 *  org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder
 *  org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration
 *  org.apache.logging.log4j.core.lookup.ConfigurationStrSubstitutor
 *  org.apache.logging.log4j.core.lookup.StrSubstitutor
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.lookup.ConfigurationStrSubstitutor;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.status.StatusLogger;

public class Log4j1ConfigurationParser {
    private static final String COMMA_DELIMITED_RE = "\\s*,\\s*";
    private static final String ROOTLOGGER = "rootLogger";
    private static final String ROOTCATEGORY = "rootCategory";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private final Properties properties = new Properties();
    private StrSubstitutor strSubstitutorProperties;
    private StrSubstitutor strSubstitutorSystem;
    private final ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

    public ConfigurationBuilder<BuiltConfiguration> buildConfigurationBuilder(InputStream input) throws IOException {
        try {
            this.properties.load(input);
            this.strSubstitutorProperties = new ConfigurationStrSubstitutor(this.properties);
            this.strSubstitutorSystem = new ConfigurationStrSubstitutor(System.getProperties());
            String rootCategoryValue = this.getLog4jValue(ROOTCATEGORY);
            String rootLoggerValue = this.getLog4jValue(ROOTLOGGER);
            if (rootCategoryValue == null && rootLoggerValue == null) {
                this.warn("Missing rootCategory or rootLogger in " + input);
            }
            this.builder.setConfigurationName("Log4j1");
            String debugValue = this.getLog4jValue("debug");
            if (Boolean.parseBoolean(debugValue)) {
                this.builder.setStatusLevel(Level.DEBUG);
            }
            this.buildRootLogger(this.getLog4jValue(ROOTCATEGORY));
            this.buildRootLogger(this.getLog4jValue(ROOTLOGGER));
            Map<String, String> appenderNameToClassName = this.buildClassToPropertyPrefixMap();
            for (Map.Entry<String, String> entry : appenderNameToClassName.entrySet()) {
                String appenderName = entry.getKey();
                String appenderClass = entry.getValue();
                this.buildAppender(appenderName, appenderClass);
            }
            this.buildLoggers("log4j.category.");
            this.buildLoggers("log4j.logger.");
            this.buildProperties();
            return this.builder;
        }
        catch (IllegalArgumentException e) {
            throw new ConfigurationException((Throwable)e);
        }
    }

    private void buildProperties() {
        for (Map.Entry<Object, Object> entry : new TreeMap<Object, Object>(this.properties).entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith("log4j.") || key.equals(ROOTCATEGORY) || key.equals(ROOTLOGGER)) continue;
            this.builder.addProperty(key, Objects.toString(entry.getValue(), ""));
        }
    }

    private void warn(String string) {
        System.err.println(string);
    }

    private Map<String, String> buildClassToPropertyPrefixMap() {
        String prefix = "log4j.appender.";
        int preLength = "log4j.appender.".length();
        HashMap<String, String> map = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : this.properties.entrySet()) {
            String key;
            Object keyObj = entry.getKey();
            if (keyObj == null || !(key = keyObj.toString()).startsWith("log4j.appender.") || key.indexOf(46, preLength) >= 0) continue;
            String name = key.substring(preLength);
            Object value = entry.getValue();
            if (value == null) continue;
            map.put(name, value.toString());
        }
        return map;
    }

    private void buildAppender(String appenderName, String appenderClass) {
        switch (appenderClass) {
            case "org.apache.log4j.ConsoleAppender": {
                this.buildConsoleAppender(appenderName);
                break;
            }
            case "org.apache.log4j.FileAppender": {
                this.buildFileAppender(appenderName);
                break;
            }
            case "org.apache.log4j.DailyRollingFileAppender": {
                this.buildDailyRollingFileAppender(appenderName);
                break;
            }
            case "org.apache.log4j.RollingFileAppender": {
                this.buildRollingFileAppender(appenderName);
                break;
            }
            case "org.apache.log4j.varia.NullAppender": {
                this.buildNullAppender(appenderName);
                break;
            }
            default: {
                this.reportWarning("Unknown appender class: " + appenderClass + "; ignoring appender: " + appenderName);
            }
        }
    }

    private void buildConsoleAppender(String appenderName) {
        AppenderComponentBuilder appenderBuilder = this.builder.newAppender(appenderName, "Console");
        String targetValue = this.getLog4jAppenderValue(appenderName, "Target", "System.out");
        if (targetValue != null) {
            ConsoleAppender.Target target;
            switch (targetValue) {
                case "System.out": {
                    target = ConsoleAppender.Target.SYSTEM_OUT;
                    break;
                }
                case "System.err": {
                    target = ConsoleAppender.Target.SYSTEM_ERR;
                    break;
                }
                default: {
                    this.reportWarning("Unknown value for console Target: " + targetValue);
                    target = null;
                }
            }
            if (target != null) {
                appenderBuilder.addAttribute("target", (Enum)target);
            }
        }
        this.buildAttribute(appenderName, (ComponentBuilder)appenderBuilder, "Follow", "follow");
        if (FALSE.equalsIgnoreCase(this.getLog4jAppenderValue(appenderName, "ImmediateFlush"))) {
            this.reportWarning("ImmediateFlush=false is not supported on Console appender");
        }
        this.buildAppenderLayout(appenderName, appenderBuilder);
        this.builder.add(appenderBuilder);
    }

    private void buildFileAppender(String appenderName) {
        AppenderComponentBuilder appenderBuilder = this.builder.newAppender(appenderName, "File");
        this.buildFileAppender(appenderName, appenderBuilder);
        this.builder.add(appenderBuilder);
    }

    private void buildFileAppender(String appenderName, AppenderComponentBuilder appenderBuilder) {
        this.buildMandatoryAttribute(appenderName, (ComponentBuilder)appenderBuilder, "File", "fileName");
        this.buildAttribute(appenderName, (ComponentBuilder)appenderBuilder, "Append", "append");
        this.buildAttribute(appenderName, (ComponentBuilder)appenderBuilder, "BufferedIO", "bufferedIo");
        this.buildAttribute(appenderName, (ComponentBuilder)appenderBuilder, "BufferSize", "bufferSize");
        this.buildAttribute(appenderName, (ComponentBuilder)appenderBuilder, "ImmediateFlush", "immediateFlush");
        this.buildAppenderLayout(appenderName, appenderBuilder);
    }

    private void buildDailyRollingFileAppender(String appenderName) {
        AppenderComponentBuilder appenderBuilder = this.builder.newAppender(appenderName, "RollingFile");
        this.buildFileAppender(appenderName, appenderBuilder);
        String fileName = this.getLog4jAppenderValue(appenderName, "File");
        String datePattern = this.getLog4jAppenderValue(appenderName, "DatePattern", fileName + "'.'yyyy-MM-dd");
        appenderBuilder.addAttribute("filePattern", fileName + "%d{" + datePattern + "}");
        ComponentBuilder triggeringPolicy = this.builder.newComponent("Policies").addComponent(this.builder.newComponent("TimeBasedTriggeringPolicy").addAttribute("modulate", true));
        appenderBuilder.addComponent(triggeringPolicy);
        appenderBuilder.addComponent(this.builder.newComponent("DefaultRolloverStrategy").addAttribute("max", Integer.MAX_VALUE));
        this.builder.add(appenderBuilder);
    }

    private void buildRollingFileAppender(String appenderName) {
        AppenderComponentBuilder appenderBuilder = this.builder.newAppender(appenderName, "RollingFile");
        this.buildFileAppender(appenderName, appenderBuilder);
        String fileName = this.getLog4jAppenderValue(appenderName, "File");
        appenderBuilder.addAttribute("filePattern", fileName + ".%i");
        String maxFileSizeString = this.getLog4jAppenderValue(appenderName, "MaxFileSize", "10485760");
        String maxBackupIndexString = this.getLog4jAppenderValue(appenderName, "MaxBackupIndex", "1");
        ComponentBuilder triggeringPolicy = this.builder.newComponent("Policies").addComponent(this.builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", maxFileSizeString));
        appenderBuilder.addComponent(triggeringPolicy);
        appenderBuilder.addComponent(this.builder.newComponent("DefaultRolloverStrategy").addAttribute("max", maxBackupIndexString));
        this.builder.add(appenderBuilder);
    }

    private void buildAttribute(String componentName, ComponentBuilder componentBuilder, String sourceAttributeName, String targetAttributeName) {
        String attributeValue = this.getLog4jAppenderValue(componentName, sourceAttributeName);
        if (attributeValue != null) {
            componentBuilder.addAttribute(targetAttributeName, attributeValue);
        }
    }

    private void buildAttributeWithDefault(String componentName, ComponentBuilder componentBuilder, String sourceAttributeName, String targetAttributeName, String defaultValue) {
        String attributeValue = this.getLog4jAppenderValue(componentName, sourceAttributeName, defaultValue);
        componentBuilder.addAttribute(targetAttributeName, attributeValue);
    }

    private void buildMandatoryAttribute(String componentName, ComponentBuilder componentBuilder, String sourceAttributeName, String targetAttributeName) {
        String attributeValue = this.getLog4jAppenderValue(componentName, sourceAttributeName);
        if (attributeValue != null) {
            componentBuilder.addAttribute(targetAttributeName, attributeValue);
        } else {
            this.reportWarning("Missing " + sourceAttributeName + " for " + componentName);
        }
    }

    private void buildNullAppender(String appenderName) {
        AppenderComponentBuilder appenderBuilder = this.builder.newAppender(appenderName, "Null");
        this.builder.add(appenderBuilder);
    }

    private void buildAppenderLayout(String name, AppenderComponentBuilder appenderBuilder) {
        String layoutClass = this.getLog4jAppenderValue(name, "layout", null);
        if (layoutClass != null) {
            switch (layoutClass) {
                case "org.apache.log4j.PatternLayout": 
                case "org.apache.log4j.EnhancedPatternLayout": {
                    String pattern = this.getLog4jAppenderValue(name, "layout.ConversionPattern", null).replace("%x", "%ndc").replace("%X", "%properties");
                    appenderBuilder.add(this.newPatternLayout(pattern));
                    break;
                }
                case "org.apache.log4j.SimpleLayout": {
                    appenderBuilder.add(this.newPatternLayout("%level - %m%n"));
                    break;
                }
                case "org.apache.log4j.TTCCLayout": {
                    String pattern = "%r ";
                    if (Boolean.parseBoolean(this.getLog4jAppenderValue(name, "layout.ThreadPrinting", TRUE))) {
                        pattern = pattern + "[%t] ";
                    }
                    pattern = pattern + "%p ";
                    if (Boolean.parseBoolean(this.getLog4jAppenderValue(name, "layout.CategoryPrefixing", TRUE))) {
                        pattern = pattern + "%c ";
                    }
                    if (Boolean.parseBoolean(this.getLog4jAppenderValue(name, "layout.ContextPrinting", TRUE))) {
                        pattern = pattern + "%notEmpty{%ndc }";
                    }
                    pattern = pattern + "- %m%n";
                    appenderBuilder.add(this.newPatternLayout(pattern));
                    break;
                }
                case "org.apache.log4j.HTMLLayout": {
                    LayoutComponentBuilder htmlLayout = this.builder.newLayout("HtmlLayout");
                    htmlLayout.addAttribute("title", this.getLog4jAppenderValue(name, "layout.Title", "Log4J Log Messages"));
                    htmlLayout.addAttribute("locationInfo", Boolean.parseBoolean(this.getLog4jAppenderValue(name, "layout.LocationInfo", FALSE)));
                    appenderBuilder.add(htmlLayout);
                    break;
                }
                case "org.apache.log4j.xml.XMLLayout": {
                    LayoutComponentBuilder xmlLayout = this.builder.newLayout("Log4j1XmlLayout");
                    xmlLayout.addAttribute("locationInfo", Boolean.parseBoolean(this.getLog4jAppenderValue(name, "layout.LocationInfo", FALSE)));
                    xmlLayout.addAttribute("properties", Boolean.parseBoolean(this.getLog4jAppenderValue(name, "layout.Properties", FALSE)));
                    appenderBuilder.add(xmlLayout);
                    break;
                }
                default: {
                    this.reportWarning("Unknown layout class: " + layoutClass);
                }
            }
        }
    }

    private LayoutComponentBuilder newPatternLayout(String pattern) {
        LayoutComponentBuilder layoutBuilder = this.builder.newLayout("PatternLayout");
        if (pattern != null) {
            layoutBuilder.addAttribute("pattern", pattern);
        }
        return layoutBuilder;
    }

    private void buildRootLogger(String rootLoggerValue) {
        if (rootLoggerValue == null) {
            return;
        }
        String[] rootLoggerParts = rootLoggerValue.split(COMMA_DELIMITED_RE);
        String rootLoggerLevel = this.getLevelString(rootLoggerParts, Level.ERROR.name());
        RootLoggerComponentBuilder loggerBuilder = this.builder.newRootLogger(rootLoggerLevel);
        Object[] sortedAppenderNames = Arrays.copyOfRange(rootLoggerParts, 1, rootLoggerParts.length);
        Arrays.sort(sortedAppenderNames);
        for (Object appender : sortedAppenderNames) {
            loggerBuilder.add(this.builder.newAppenderRef((String)appender));
        }
        this.builder.add(loggerBuilder);
    }

    private String getLevelString(String[] loggerParts, String defaultLevel) {
        return loggerParts.length > 0 ? loggerParts[0] : defaultLevel;
    }

    private void buildLoggers(String prefix) {
        int preLength = prefix.length();
        for (Map.Entry<Object, Object> entry : this.properties.entrySet()) {
            String key;
            Object keyObj = entry.getKey();
            if (keyObj == null || !(key = keyObj.toString()).startsWith(prefix)) continue;
            String name = key.substring(preLength);
            Object value = entry.getValue();
            if (value == null) continue;
            String valueStr = value.toString();
            String[] split = valueStr.split(COMMA_DELIMITED_RE);
            String level = this.getLevelString(split, null);
            if (level == null) {
                this.warn("Level is missing for entry " + entry);
                continue;
            }
            LoggerComponentBuilder newLogger = this.builder.newLogger(name, level);
            if (split.length > 1) {
                Object[] sortedAppenderNames = Arrays.copyOfRange(split, 1, split.length);
                Arrays.sort(sortedAppenderNames);
                for (Object appenderName : sortedAppenderNames) {
                    newLogger.add(this.builder.newAppenderRef((String)appenderName));
                }
            }
            this.builder.add(newLogger);
        }
    }

    private String getLog4jAppenderValue(String appenderName, String attributeName) {
        return this.getProperty("log4j.appender." + appenderName + "." + attributeName);
    }

    private String getProperty(String key) {
        String value = this.properties.getProperty(key);
        String sysValue = this.strSubstitutorSystem.replace(value);
        return this.strSubstitutorProperties.replace(sysValue);
    }

    private String getProperty(String key, String defaultValue) {
        String value = this.getProperty(key);
        return value == null ? defaultValue : value;
    }

    private String getLog4jAppenderValue(String appenderName, String attributeName, String defaultValue) {
        return this.getProperty("log4j.appender." + appenderName + "." + attributeName, defaultValue);
    }

    private String getLog4jValue(String key) {
        return this.getProperty("log4j." + key);
    }

    private void reportWarning(String msg) {
        StatusLogger.getLogger().warn("Log4j 1 configuration parser: " + msg);
    }
}

