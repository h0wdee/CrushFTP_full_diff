/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.util.OptionConverter
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.config;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.config.PropertySetterException;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.OptionHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.OptionConverter;
import org.apache.logging.log4j.status.StatusLogger;

public class PropertySetter {
    private static final PropertyDescriptor[] EMPTY_PROPERTY_DESCRIPTOR_ARRAY = new PropertyDescriptor[0];
    private static Logger LOGGER = StatusLogger.getLogger();
    protected Object obj;
    protected PropertyDescriptor[] props;

    public PropertySetter(Object obj) {
        this.obj = obj;
    }

    public static void setProperties(Object obj, Properties properties, String prefix) {
        new PropertySetter(obj).setProperties(properties, prefix);
    }

    protected void introspect() {
        try {
            BeanInfo bi = Introspector.getBeanInfo(this.obj.getClass());
            this.props = bi.getPropertyDescriptors();
        }
        catch (IntrospectionException ex) {
            LOGGER.error("Failed to introspect {}: {}", this.obj, (Object)ex.getMessage());
            this.props = EMPTY_PROPERTY_DESCRIPTOR_ARRAY;
        }
    }

    public void setProperties(Properties properties, String prefix) {
        int len = prefix.length();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith(prefix) || key.indexOf(46, len + 1) > 0) continue;
            String value = OptionConverter.findAndSubst((String)key, (Properties)properties);
            if (("layout".equals(key = key.substring(len)) || "errorhandler".equals(key)) && this.obj instanceof Appender) continue;
            PropertyDescriptor prop = this.getPropertyDescriptor(Introspector.decapitalize(key));
            if (prop != null && OptionHandler.class.isAssignableFrom(prop.getPropertyType()) && prop.getWriteMethod() != null) {
                OptionHandler opt = (OptionHandler)OptionConverter.instantiateByKey((Properties)properties, (String)(prefix + key), prop.getPropertyType(), null);
                PropertySetter setter = new PropertySetter(opt);
                setter.setProperties(properties, prefix + key + ".");
                try {
                    prop.getWriteMethod().invoke(this.obj, opt);
                }
                catch (InvocationTargetException ex) {
                    if (ex.getTargetException() instanceof InterruptedException || ex.getTargetException() instanceof InterruptedIOException) {
                        Thread.currentThread().interrupt();
                    }
                    LOGGER.warn("Failed to set property [{}] to value \"{}\".", (Object)key, (Object)value, (Object)ex);
                }
                catch (IllegalAccessException | RuntimeException ex) {
                    LOGGER.warn("Failed to set property [{}] to value \"{}\".", (Object)key, (Object)value, (Object)ex);
                }
                continue;
            }
            this.setProperty(key, value);
        }
        this.activate();
    }

    public void setProperty(String name, String value) {
        if (value == null) {
            return;
        }
        PropertyDescriptor prop = this.getPropertyDescriptor(name = Introspector.decapitalize(name));
        if (prop == null) {
            LOGGER.warn("No such property [" + name + "] in " + this.obj.getClass().getName() + ".");
        } else {
            try {
                this.setProperty(prop, name, value);
            }
            catch (PropertySetterException ex) {
                LOGGER.warn("Failed to set property [{}] to value \"{}\".", (Object)name, (Object)value, (Object)ex.rootCause);
            }
        }
    }

    public void setProperty(PropertyDescriptor prop, String name, String value) throws PropertySetterException {
        Object arg;
        Method setter = prop.getWriteMethod();
        if (setter == null) {
            throw new PropertySetterException("No setter for property [" + name + "].");
        }
        Class<?>[] paramTypes = setter.getParameterTypes();
        if (paramTypes.length != 1) {
            throw new PropertySetterException("#params for setter != 1");
        }
        try {
            arg = this.convertArg(value, paramTypes[0]);
        }
        catch (Throwable t) {
            throw new PropertySetterException("Conversion to type [" + paramTypes[0] + "] failed. Reason: " + t);
        }
        if (arg == null) {
            throw new PropertySetterException("Conversion to type [" + paramTypes[0] + "] failed.");
        }
        LOGGER.debug("Setting property [" + name + "] to [" + arg + "].");
        try {
            setter.invoke(this.obj, arg);
        }
        catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof InterruptedException || ex.getTargetException() instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            throw new PropertySetterException(ex);
        }
        catch (IllegalAccessException | RuntimeException ex) {
            throw new PropertySetterException(ex);
        }
    }

    protected Object convertArg(String val, Class<?> type) {
        if (val == null) {
            return null;
        }
        String v = val.trim();
        if (String.class.isAssignableFrom(type)) {
            return val;
        }
        if (Integer.TYPE.isAssignableFrom(type)) {
            return Integer.parseInt(v);
        }
        if (Long.TYPE.isAssignableFrom(type)) {
            return Long.parseLong(v);
        }
        if (Boolean.TYPE.isAssignableFrom(type)) {
            if ("true".equalsIgnoreCase(v)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(v)) {
                return Boolean.FALSE;
            }
        } else {
            if (Priority.class.isAssignableFrom(type)) {
                return org.apache.log4j.helpers.OptionConverter.toLevel(v, Level.DEBUG);
            }
            if (ErrorHandler.class.isAssignableFrom(type)) {
                return OptionConverter.instantiateByClassName((String)v, ErrorHandler.class, null);
            }
        }
        return null;
    }

    protected PropertyDescriptor getPropertyDescriptor(String name) {
        if (this.props == null) {
            this.introspect();
        }
        for (PropertyDescriptor prop : this.props) {
            if (!name.equals(prop.getName())) continue;
            return prop;
        }
        return null;
    }

    public void activate() {
        if (this.obj instanceof OptionHandler) {
            ((OptionHandler)this.obj).activateOptions();
        }
    }
}

