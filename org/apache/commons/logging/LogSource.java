/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.logging;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;

public class LogSource {
    protected static Hashtable logs;
    protected static boolean log4jIsAvailable;
    protected static boolean jdk14IsAvailable;
    protected static Constructor logImplctor;

    private LogSource() {
    }

    public static void setLogImplementation(String classname) throws LinkageError, ExceptionInInitializerError, NoSuchMethodException, SecurityException, ClassNotFoundException {
        try {
            Class<?> logclass = Class.forName(classname);
            Class[] argtypes = new Class[]{"".getClass()};
            logImplctor = logclass.getConstructor(argtypes);
        }
        catch (Throwable t) {
            logImplctor = null;
        }
    }

    public static void setLogImplementation(Class logclass) throws LinkageError, ExceptionInInitializerError, NoSuchMethodException, SecurityException {
        Class[] argtypes = new Class[]{"".getClass()};
        logImplctor = logclass.getConstructor(argtypes);
    }

    public static Log getInstance(String name) {
        Log log = (Log)logs.get(name);
        if (null == log) {
            log = LogSource.makeNewLogInstance(name);
            logs.put(name, log);
        }
        return log;
    }

    public static Log getInstance(Class clazz) {
        return LogSource.getInstance(clazz.getName());
    }

    public static Log makeNewLogInstance(String name) {
        Log log = null;
        try {
            Object[] args = new Object[]{name};
            log = (Log)logImplctor.newInstance(args);
        }
        catch (Throwable t) {
            log = null;
        }
        if (null == log) {
            log = new NoOpLog(name);
        }
        return log;
    }

    public static String[] getLogNames() {
        return logs.keySet().toArray(new String[logs.size()]);
    }

    static {
        block19: {
            logs = new Hashtable();
            log4jIsAvailable = false;
            jdk14IsAvailable = false;
            logImplctor = null;
            try {
                log4jIsAvailable = null != Class.forName("org.apache.log4j.Logger");
            }
            catch (Throwable t) {
                log4jIsAvailable = false;
            }
            try {
                jdk14IsAvailable = null != Class.forName("java.util.logging.Logger") && null != Class.forName("org.apache.commons.logging.impl.Jdk14Logger");
            }
            catch (Throwable t) {
                jdk14IsAvailable = false;
            }
            String name = null;
            try {
                name = System.getProperty("org.apache.commons.logging.log");
                if (name == null) {
                    name = System.getProperty("org.apache.commons.logging.Log");
                }
            }
            catch (Throwable t) {
                // empty catch block
            }
            if (name != null) {
                try {
                    LogSource.setLogImplementation(name);
                }
                catch (Throwable t) {
                    try {
                        LogSource.setLogImplementation("org.apache.commons.logging.impl.NoOpLog");
                    }
                    catch (Throwable u) {}
                }
            } else {
                try {
                    if (log4jIsAvailable) {
                        LogSource.setLogImplementation("org.apache.commons.logging.impl.Log4JLogger");
                        break block19;
                    }
                    if (jdk14IsAvailable) {
                        LogSource.setLogImplementation("org.apache.commons.logging.impl.Jdk14Logger");
                        break block19;
                    }
                    LogSource.setLogImplementation("org.apache.commons.logging.impl.NoOpLog");
                }
                catch (Throwable t) {
                    try {
                        LogSource.setLogImplementation("org.apache.commons.logging.impl.NoOpLog");
                    }
                    catch (Throwable u) {}
                }
            }
        }
    }
}

