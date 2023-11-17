/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;

public abstract class LogFactory {
    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";
    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.impl.LogFactoryImpl";
    public static final String FACTORY_PROPERTIES = "commons-logging.properties";
    protected static final String SERVICE_ID = "META-INF/services/org.apache.commons.logging.LogFactory";
    protected static Hashtable factories = new Hashtable();
    static /* synthetic */ Class class$org$apache$commons$logging$LogFactory;
    static /* synthetic */ Class class$java$lang$Thread;

    protected LogFactory() {
    }

    public abstract Object getAttribute(String var1);

    public abstract String[] getAttributeNames();

    public abstract Log getInstance(Class var1) throws LogConfigurationException;

    public abstract Log getInstance(String var1) throws LogConfigurationException;

    public abstract void release();

    public abstract void removeAttribute(String var1);

    public abstract void setAttribute(String var1, Object var2);

    public static LogFactory getFactory() throws LogConfigurationException {
        String factoryClass;
        Properties props;
        LogFactory factory;
        ClassLoader contextClassLoader;
        block19: {
            contextClassLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(){

                public Object run() {
                    return LogFactory.getContextClassLoader();
                }
            });
            factory = LogFactory.getCachedFactory(contextClassLoader);
            if (factory != null) {
                return factory;
            }
            props = null;
            try {
                InputStream stream = LogFactory.getResourceAsStream(contextClassLoader, FACTORY_PROPERTIES);
                if (stream != null) {
                    props = new Properties();
                    props.load(stream);
                    stream.close();
                }
            }
            catch (IOException e) {
            }
            catch (SecurityException e) {
                // empty catch block
            }
            try {
                factoryClass = System.getProperty(FACTORY_PROPERTY);
                if (factoryClass != null) {
                    factory = LogFactory.newFactory(factoryClass, contextClassLoader);
                }
            }
            catch (SecurityException e) {
                // empty catch block
            }
            if (factory == null) {
                try {
                    BufferedReader rd;
                    InputStream is = LogFactory.getResourceAsStream(contextClassLoader, SERVICE_ID);
                    if (is == null) break block19;
                    try {
                        rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    }
                    catch (UnsupportedEncodingException e) {
                        rd = new BufferedReader(new InputStreamReader(is));
                    }
                    String factoryClassName = rd.readLine();
                    rd.close();
                    if (factoryClassName != null && !"".equals(factoryClassName)) {
                        factory = LogFactory.newFactory(factoryClassName, contextClassLoader);
                    }
                }
                catch (Exception ex) {
                    // empty catch block
                }
            }
        }
        if (factory == null && props != null && (factoryClass = props.getProperty(FACTORY_PROPERTY)) != null) {
            factory = LogFactory.newFactory(factoryClass, contextClassLoader);
        }
        if (factory == null) {
            factory = LogFactory.newFactory(FACTORY_DEFAULT, (class$org$apache$commons$logging$LogFactory == null ? (class$org$apache$commons$logging$LogFactory = LogFactory.class$(FACTORY_PROPERTY)) : class$org$apache$commons$logging$LogFactory).getClassLoader());
        }
        if (factory != null) {
            LogFactory.cacheFactory(contextClassLoader, factory);
            if (props != null) {
                Enumeration<?> names = props.propertyNames();
                while (names.hasMoreElements()) {
                    String name = (String)names.nextElement();
                    String value = props.getProperty(name);
                    factory.setAttribute(name, value);
                }
            }
        }
        return factory;
    }

    public static Log getLog(Class clazz) throws LogConfigurationException {
        return LogFactory.getFactory().getInstance(clazz);
    }

    public static Log getLog(String name) throws LogConfigurationException {
        return LogFactory.getFactory().getInstance(name);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void release(ClassLoader classLoader) {
        Hashtable hashtable = factories;
        synchronized (hashtable) {
            LogFactory factory = (LogFactory)factories.get(classLoader);
            if (factory != null) {
                factory.release();
                factories.remove(classLoader);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void releaseAll() {
        Hashtable hashtable = factories;
        synchronized (hashtable) {
            Enumeration elements = factories.elements();
            while (elements.hasMoreElements()) {
                LogFactory element = (LogFactory)elements.nextElement();
                element.release();
            }
            factories.clear();
        }
    }

    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        ClassLoader classLoader = null;
        try {
            Method method = (class$java$lang$Thread == null ? (class$java$lang$Thread = LogFactory.class$("java.lang.Thread")) : class$java$lang$Thread).getMethod("getContextClassLoader", null);
            try {
                classLoader = (ClassLoader)method.invoke(Thread.currentThread(), null);
            }
            catch (IllegalAccessException e) {
                throw new LogConfigurationException("Unexpected IllegalAccessException", e);
            }
            catch (InvocationTargetException e) {
                if (!(e.getTargetException() instanceof SecurityException)) {
                    throw new LogConfigurationException("Unexpected InvocationTargetException", e.getTargetException());
                }
            }
        }
        catch (NoSuchMethodException e) {
            classLoader = (class$org$apache$commons$logging$LogFactory == null ? (class$org$apache$commons$logging$LogFactory = LogFactory.class$(FACTORY_PROPERTY)) : class$org$apache$commons$logging$LogFactory).getClassLoader();
        }
        return classLoader;
    }

    private static LogFactory getCachedFactory(ClassLoader contextClassLoader) {
        LogFactory factory = null;
        if (contextClassLoader != null) {
            factory = (LogFactory)factories.get(contextClassLoader);
        }
        return factory;
    }

    private static void cacheFactory(ClassLoader classLoader, LogFactory factory) {
        if (classLoader != null && factory != null) {
            factories.put(classLoader, factory);
        }
    }

    protected static LogFactory newFactory(final String factoryClass, final ClassLoader classLoader) throws LogConfigurationException {
        Object result = AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                try {
                    block9: {
                        if (classLoader != null) {
                            try {
                                return (LogFactory)classLoader.loadClass(factoryClass).newInstance();
                            }
                            catch (ClassNotFoundException ex) {
                                if (classLoader == (class$org$apache$commons$logging$LogFactory == null ? (class$org$apache$commons$logging$LogFactory = LogFactory.class$(LogFactory.FACTORY_PROPERTY)) : class$org$apache$commons$logging$LogFactory).getClassLoader()) {
                                    throw ex;
                                }
                            }
                            catch (NoClassDefFoundError e) {
                                if (classLoader == (class$org$apache$commons$logging$LogFactory == null ? (class$org$apache$commons$logging$LogFactory = LogFactory.class$(LogFactory.FACTORY_PROPERTY)) : class$org$apache$commons$logging$LogFactory).getClassLoader()) {
                                    throw e;
                                }
                            }
                            catch (ClassCastException e) {
                                if (classLoader != (class$org$apache$commons$logging$LogFactory == null ? (class$org$apache$commons$logging$LogFactory = LogFactory.class$(LogFactory.FACTORY_PROPERTY)) : class$org$apache$commons$logging$LogFactory).getClassLoader()) break block9;
                                throw e;
                            }
                        }
                    }
                    return (LogFactory)Class.forName(factoryClass).newInstance();
                }
                catch (Exception e) {
                    return new LogConfigurationException(e);
                }
            }
        });
        if (result instanceof LogConfigurationException) {
            throw (LogConfigurationException)result;
        }
        return (LogFactory)result;
    }

    private static InputStream getResourceAsStream(final ClassLoader loader, final String name) {
        return (InputStream)AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                if (loader != null) {
                    return loader.getResourceAsStream(name);
                }
                return ClassLoader.getSystemResourceAsStream(name);
            }
        });
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        }
        catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }
}

