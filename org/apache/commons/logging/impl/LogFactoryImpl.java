/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.logging.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

public class LogFactoryImpl
extends LogFactory {
    public static final String LOG_PROPERTY = "org.apache.commons.logging.Log";
    protected static final String LOG_PROPERTY_OLD = "org.apache.commons.logging.log";
    protected Hashtable attributes = new Hashtable();
    protected Hashtable instances = new Hashtable();
    private String logClassName;
    protected Constructor logConstructor = null;
    protected Class[] logConstructorSignature = new Class[]{class$java$lang$String == null ? (class$java$lang$String = LogFactoryImpl.class$("java.lang.String")) : class$java$lang$String};
    protected Method logMethod = null;
    protected Class[] logMethodSignature = new Class[]{class$org$apache$commons$logging$LogFactory == null ? (class$org$apache$commons$logging$LogFactory = LogFactoryImpl.class$("org.apache.commons.logging.LogFactory")) : class$org$apache$commons$logging$LogFactory};
    static /* synthetic */ Class class$java$lang$String;
    static /* synthetic */ Class class$org$apache$commons$logging$LogFactory;
    static /* synthetic */ Class class$org$apache$commons$logging$Log;

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public String[] getAttributeNames() {
        Vector<String> names = new Vector<String>();
        Enumeration keys = this.attributes.keys();
        while (keys.hasMoreElements()) {
            names.addElement((String)keys.nextElement());
        }
        String[] results = new String[names.size()];
        int i = 0;
        while (i < results.length) {
            results[i] = (String)names.elementAt(i);
            ++i;
        }
        return results;
    }

    public Log getInstance(Class clazz) throws LogConfigurationException {
        return this.getInstance(clazz.getName());
    }

    public Log getInstance(String name) throws LogConfigurationException {
        Log instance = (Log)this.instances.get(name);
        if (instance == null) {
            instance = this.newInstance(name);
            this.instances.put(name, instance);
        }
        return instance;
    }

    public void release() {
        this.instances.clear();
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            this.attributes.remove(name);
        } else {
            this.attributes.put(name, value);
        }
    }

    protected String getLogClassName() {
        if (this.logClassName != null) {
            return this.logClassName;
        }
        this.logClassName = (String)this.getAttribute(LOG_PROPERTY);
        if (this.logClassName == null) {
            this.logClassName = (String)this.getAttribute(LOG_PROPERTY_OLD);
        }
        if (this.logClassName == null) {
            try {
                this.logClassName = System.getProperty(LOG_PROPERTY);
            }
            catch (SecurityException e) {
                // empty catch block
            }
        }
        if (this.logClassName == null) {
            try {
                this.logClassName = System.getProperty(LOG_PROPERTY_OLD);
            }
            catch (SecurityException e) {
                // empty catch block
            }
        }
        if (this.logClassName == null && this.isLog4JAvailable()) {
            this.logClassName = "org.apache.commons.logging.impl.Log4JLogger";
        }
        if (this.logClassName == null && this.isJdk14Available()) {
            this.logClassName = "org.apache.commons.logging.impl.Jdk14Logger";
        }
        if (this.logClassName == null) {
            this.logClassName = "org.apache.commons.logging.impl.SimpleLog";
        }
        return this.logClassName;
    }

    protected Constructor getLogConstructor() throws LogConfigurationException {
        if (this.logConstructor != null) {
            return this.logConstructor;
        }
        String logClassName = this.getLogClassName();
        Class logClass = null;
        try {
            logClass = LogFactoryImpl.loadClass(logClassName);
            if (logClass == null) {
                throw new LogConfigurationException("No suitable Log implementation for " + logClassName);
            }
            if (!(class$org$apache$commons$logging$Log == null ? (class$org$apache$commons$logging$Log = LogFactoryImpl.class$(LOG_PROPERTY)) : class$org$apache$commons$logging$Log).isAssignableFrom(logClass)) {
                throw new LogConfigurationException("Class " + logClassName + " does not implement Log");
            }
        }
        catch (Throwable t) {
            throw new LogConfigurationException(t);
        }
        try {
            this.logMethod = logClass.getMethod("setLogFactory", this.logMethodSignature);
        }
        catch (Throwable t) {
            this.logMethod = null;
        }
        try {
            this.logConstructor = logClass.getConstructor(this.logConstructorSignature);
            return this.logConstructor;
        }
        catch (Throwable t) {
            throw new LogConfigurationException("No suitable Log constructor " + this.logConstructorSignature + " for " + logClassName, t);
        }
    }

    private static Class loadClass(final String name) throws ClassNotFoundException {
        Object result = AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                ClassLoader threadCL = LogFactoryImpl.access$000();
                if (threadCL != null) {
                    try {
                        return threadCL.loadClass(name);
                    }
                    catch (ClassNotFoundException ex) {
                        // empty catch block
                    }
                }
                try {
                    return Class.forName(name);
                }
                catch (ClassNotFoundException e) {
                    return e;
                }
            }
        });
        if (result instanceof Class) {
            return (Class)result;
        }
        throw (ClassNotFoundException)result;
    }

    protected boolean isJdk14Available() {
        try {
            LogFactoryImpl.loadClass("java.util.logging.Logger");
            LogFactoryImpl.loadClass("org.apache.commons.logging.impl.Jdk14Logger");
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }

    protected boolean isLog4JAvailable() {
        try {
            LogFactoryImpl.loadClass("org.apache.log4j.Logger");
            LogFactoryImpl.loadClass("org.apache.commons.logging.impl.Log4JLogger");
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }

    protected Log newInstance(String name) throws LogConfigurationException {
        Log instance = null;
        try {
            Object[] params = new Object[]{name};
            instance = (Log)this.getLogConstructor().newInstance(params);
            if (this.logMethod != null) {
                params[0] = this;
                this.logMethod.invoke(instance, params);
            }
            return instance;
        }
        catch (Throwable t) {
            throw new LogConfigurationException(t);
        }
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        }
        catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    static /* synthetic */ ClassLoader access$000() throws LogConfigurationException {
        return LogFactory.getContextClassLoader();
    }
}

