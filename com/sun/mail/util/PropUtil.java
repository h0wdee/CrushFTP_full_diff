/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util;

import java.util.Properties;
import javax.mail.Session;

public class PropUtil {
    private PropUtil() {
    }

    public static int getIntProperty(Properties props, String name, int def) {
        return PropUtil.getInt(PropUtil.getProp(props, name), def);
    }

    public static boolean getBooleanProperty(Properties props, String name, boolean def) {
        return PropUtil.getBoolean(PropUtil.getProp(props, name), def);
    }

    public static int getIntSessionProperty(Session session, String name, int def) {
        return PropUtil.getInt(PropUtil.getProp(session.getProperties(), name), def);
    }

    public static boolean getBooleanSessionProperty(Session session, String name, boolean def) {
        return PropUtil.getBoolean(PropUtil.getProp(session.getProperties(), name), def);
    }

    public static boolean getBooleanSystemProperty(String name, boolean def) {
        try {
            return PropUtil.getBoolean(PropUtil.getProp(System.getProperties(), name), def);
        }
        catch (SecurityException securityException) {
            try {
                String value = System.getProperty(name);
                if (value == null) {
                    return def;
                }
                if (def) {
                    return !value.equalsIgnoreCase("false");
                }
                return value.equalsIgnoreCase("true");
            }
            catch (SecurityException sex) {
                return def;
            }
        }
    }

    private static Object getProp(Properties props, String name) {
        Object val = props.get(name);
        if (val != null) {
            return val;
        }
        return props.getProperty(name);
    }

    private static int getInt(Object value, int def) {
        if (value == null) {
            return def;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (value instanceof Integer) {
            return (Integer)value;
        }
        return def;
    }

    private static boolean getBoolean(Object value, boolean def) {
        if (value == null) {
            return def;
        }
        if (value instanceof String) {
            if (def) {
                return !((String)value).equalsIgnoreCase("false");
            }
            return ((String)value).equalsIgnoreCase("true");
        }
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        return def;
    }
}

