/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.lookup.StrSubstitutor
 *  org.apache.logging.log4j.util.LoaderUtil
 */
package org.apache.log4j.helpers;

import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.util.LoaderUtil;

public class OptionConverter {
    static String DELIM_START = "${";
    static char DELIM_STOP = (char)125;
    static int DELIM_START_LEN = 2;
    static int DELIM_STOP_LEN = 1;
    private static final Logger LOGGER = LogManager.getLogger(OptionConverter.class);
    private static final CharMap[] charMap = new CharMap[]{new CharMap('n', '\n'), new CharMap('r', '\r'), new CharMap('t', '\t'), new CharMap('f', '\f'), new CharMap('\b', '\b'), new CharMap('\"', '\"'), new CharMap('\'', '\''), new CharMap('\\', '\\')};

    private OptionConverter() {
    }

    public static String[] concatanateArrays(String[] l, String[] r) {
        int len = l.length + r.length;
        String[] a = new String[len];
        System.arraycopy(l, 0, a, 0, l.length);
        System.arraycopy(r, 0, a, l.length, r.length);
        return a;
    }

    public static String convertSpecialChars(String s) {
        int len = s.length();
        StringBuilder sbuf = new StringBuilder(len);
        int i = 0;
        while (i < len) {
            char c;
            if ((c = s.charAt(i++)) == '\\') {
                c = s.charAt(i++);
                for (CharMap entry : charMap) {
                    if (entry.key != c) continue;
                    c = entry.replacement;
                }
            }
            sbuf.append(c);
        }
        return sbuf.toString();
    }

    public static String getSystemProperty(String key, String def) {
        try {
            return System.getProperty(key, def);
        }
        catch (Throwable e) {
            LOGGER.debug("Was not allowed to read system property \"{}\".", (Object)key);
            return def;
        }
    }

    public static boolean toBoolean(String value, boolean dEfault) {
        if (value == null) {
            return dEfault;
        }
        String trimmedVal = value.trim();
        if ("true".equalsIgnoreCase(trimmedVal)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmedVal)) {
            return false;
        }
        return dEfault;
    }

    public static Level toLevel(String value, Level defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        int hashIndex = (value = value.trim()).indexOf(35);
        if (hashIndex == -1) {
            if ("NULL".equalsIgnoreCase(value)) {
                return null;
            }
            return Level.toLevel(value, defaultValue);
        }
        Level result = defaultValue;
        String clazz = value.substring(hashIndex + 1);
        String levelName = value.substring(0, hashIndex);
        if ("NULL".equalsIgnoreCase(levelName)) {
            return null;
        }
        LOGGER.debug("toLevel:class=[" + clazz + "]:pri=[" + levelName + "]");
        try {
            Class customLevel = LoaderUtil.loadClass((String)clazz);
            Class[] paramTypes = new Class[]{String.class, Level.class};
            Method toLevelMethod = customLevel.getMethod("toLevel", paramTypes);
            Object[] params = new Object[]{levelName, defaultValue};
            Object o = toLevelMethod.invoke(null, params);
            result = (Level)o;
        }
        catch (ClassNotFoundException e) {
            LOGGER.warn("custom level class [" + clazz + "] not found.");
        }
        catch (NoSuchMethodException e) {
            LOGGER.warn("custom level class [" + clazz + "] does not have a class function toLevel(String, Level)", (Throwable)e);
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof InterruptedException || e.getTargetException() instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.warn("custom level class [" + clazz + "] could not be instantiated", (Throwable)e);
        }
        catch (ClassCastException e) {
            LOGGER.warn("class [" + clazz + "] is not a subclass of org.apache.log4j.Level", (Throwable)e);
        }
        catch (IllegalAccessException e) {
            LOGGER.warn("class [" + clazz + "] cannot be instantiated due to access restrictions", (Throwable)e);
        }
        catch (RuntimeException e) {
            LOGGER.warn("class [" + clazz + "], level [" + levelName + "] conversion failed.", (Throwable)e);
        }
        return result;
    }

    public static Object instantiateByClassName(String className, Class<?> superClass, Object defaultValue) {
        if (className != null) {
            try {
                Object obj = LoaderUtil.newInstanceOf((String)className);
                if (!superClass.isAssignableFrom(obj.getClass())) {
                    LOGGER.error("A \"{}\" object is not assignable to a \"{}\" variable", (Object)className, (Object)superClass.getName());
                    return defaultValue;
                }
                return obj;
            }
            catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                LOGGER.error("Could not instantiate class [" + className + "].", (Throwable)e);
            }
        }
        return defaultValue;
    }

    public static String substVars(String val, Properties props) throws IllegalArgumentException {
        return StrSubstitutor.replace((Object)val, (Properties)props);
    }

    public static org.apache.logging.log4j.Level convertLevel(String level, org.apache.logging.log4j.Level defaultLevel) {
        Level l = OptionConverter.toLevel(level, null);
        return l != null ? OptionConverter.convertLevel(l) : defaultLevel;
    }

    public static org.apache.logging.log4j.Level convertLevel(Level level) {
        if (level == null) {
            return org.apache.logging.log4j.Level.ERROR;
        }
        if (level.isGreaterOrEqual(Level.FATAL)) {
            return org.apache.logging.log4j.Level.FATAL;
        }
        if (level.isGreaterOrEqual(Level.ERROR)) {
            return org.apache.logging.log4j.Level.ERROR;
        }
        if (level.isGreaterOrEqual(Level.WARN)) {
            return org.apache.logging.log4j.Level.WARN;
        }
        if (level.isGreaterOrEqual(Level.INFO)) {
            return org.apache.logging.log4j.Level.INFO;
        }
        if (level.isGreaterOrEqual(Level.DEBUG)) {
            return org.apache.logging.log4j.Level.DEBUG;
        }
        if (level.isGreaterOrEqual(Level.TRACE)) {
            return org.apache.logging.log4j.Level.TRACE;
        }
        return org.apache.logging.log4j.Level.ALL;
    }

    public static Level convertLevel(org.apache.logging.log4j.Level level) {
        if (level == null) {
            return Level.ERROR;
        }
        switch (level.getStandardLevel()) {
            case FATAL: {
                return Level.FATAL;
            }
            case WARN: {
                return Level.WARN;
            }
            case INFO: {
                return Level.INFO;
            }
            case DEBUG: {
                return Level.DEBUG;
            }
            case TRACE: {
                return Level.TRACE;
            }
            case ALL: {
                return Level.ALL;
            }
            case OFF: {
                return Level.OFF;
            }
        }
        return Level.ERROR;
    }

    public static String findAndSubst(String key, Properties props) {
        String value = props.getProperty(key);
        if (value == null) {
            return null;
        }
        try {
            return OptionConverter.substVars(value, props);
        }
        catch (IllegalArgumentException e) {
            LOGGER.error("Bad option value [{}].", (Object)value, (Object)e);
            return value;
        }
    }

    private static class CharMap {
        final char key;
        final char replacement;

        public CharMap(char key, char replacement) {
            this.key = key;
            this.replacement = replacement;
        }
    }
}

