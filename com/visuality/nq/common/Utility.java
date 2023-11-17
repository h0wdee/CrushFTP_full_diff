/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Utility {
    static final Set<Character> illegalDNSChars;
    private static int pid;
    private static final String PROCESS_HANDLE = "java.lang.ProcessHandle";

    public static int getThreadId() {
        return (int)Thread.currentThread().getId();
    }

    public static void generateUuid() {
    }

    public static void zeroUuid() {
    }

    public static void uuidRead() {
    }

    public static void uuidWrite() {
    }

    public static long getCurrentTimeInSec() {
        return System.currentTimeMillis() / 1000L;
    }

    public static void timeRead() {
    }

    public static void timeWrite() {
    }

    public static String getNetbiosNameFromFQN(String fqn) {
        String res = null;
        if (null == fqn) {
            return null;
        }
        if (IpAddressHelper.isIpAddress(fqn)) {
            return fqn;
        }
        int idx = fqn.indexOf(46);
        res = idx == -1 ? fqn : fqn.substring(0, idx);
        return res.toUpperCase();
    }

    public static double getRuntineVerstion() {
        double version = Double.parseDouble(System.getProperty("java.specification.version"));
        return version;
    }

    public static String getHostName() {
        return Utility.getHostName(false);
    }

    public static String getHostName(boolean inAuth) {
        String hostName;
        if (Utility.isAndroid()) {
            String hostName2 = " ";
            try {
                hostName2 = Utility.getAndroidHostName();
            }
            catch (Exception e) {
                TraceLog.get().message("Unable to extract Android hostname: ", e, 2000);
            }
            return hostName2;
        }
        try {
            hostName = Config.jnq.getString("HOSTNAME");
        }
        catch (NqException e1) {
            hostName = null;
        }
        if (null != hostName && hostName.length() > 0 && !hostName.equals("DO_NOT_USE")) {
            return hostName;
        }
        if (null == hostName || !hostName.equals("DO_NOT_USE") || !inAuth) {
            Map<String, String> env = System.getenv();
            if (env.containsKey("COMPUTERNAME")) {
                hostName = env.get("COMPUTERNAME");
            } else if (env.containsKey("HOSTNAME")) {
                hostName = env.get("HOSTNAME");
            } else {
                try {
                    hostName = IpAddressHelper.getLocalHostIp().getHostName();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return Utility.getNetbiosNameFromFQN(hostName);
        }
        return "";
    }

    public static boolean isAndroid() {
        String vmname = System.getProperty("java.vm.name").toLowerCase();
        if (vmname.startsWith("dalvik")) {
            try {
                Config.jnq.set("BINDWELLKNOWNPORTS", false);
            }
            catch (NqException nqException) {
                // empty catch block
            }
            return true;
        }
        return false;
    }

    public static String getAndroidHostName() throws Exception {
        String strClassName = "android.os.Build";
        String strMethodName = "getString";
        String hostName = "";
        Class<?> cl = Class.forName(strClassName);
        Method getString = cl.getDeclaredMethod(strMethodName, String.class);
        getString.setAccessible(true);
        hostName = getString.invoke(null, "net.hostname").toString();
        getString.setAccessible(false);
        return hostName;
    }

    public static boolean isClassSupport(String className) {
        try {
            Class.forName(className);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Class isClassForName(String kerberosClass) {
        try {
            return Class.forName(kerberosClass);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static int getPid() {
        if (-1 == pid) {
            Class[] noparams = new Class[]{};
            Class procClass = Utility.isClassForName(PROCESS_HANDLE);
            if (null != procClass) {
                try {
                    if (null != procClass.getDeclaredMethod("current", noparams) && null != procClass.getDeclaredMethod("pid", noparams)) {
                        Method getProcessId = procClass.getDeclaredMethod("current", noparams);
                        Object processHandle = getProcessId.invoke(procClass, new Object[0]);
                        Method pidMethod = procClass.getDeclaredMethod("pid", noparams);
                        long mypid = (Long)pidMethod.invoke(processHandle, new Object[0]);
                        pid = (int)mypid;
                        return pid;
                    }
                }
                catch (Exception e) {
                    TraceLog.get().message("PID calculation failed with Exception ", e, 10);
                    pid = Utility.generateRandomPid();
                }
            }
            try {
                if (Utility.isClassSupport("java.lang.management.ManagementFactory")) {
                    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                    Field jvm = runtime.getClass().getDeclaredField("jvm");
                    jvm.setAccessible(true);
                    Object mgmt = jvm.get(runtime);
                    Method getProcessId = mgmt.getClass().getDeclaredMethod("getProcessId", new Class[0]);
                    getProcessId.setAccessible(true);
                    pid = (Integer)getProcessId.invoke(mgmt, new Object[0]);
                } else {
                    Class environment = Utility.isClassForName("android.os.Process");
                    Method getPid = environment.getMethod("myPid", new Class[0]);
                    Object[] noparamsObject = new Object[]{};
                    pid = (Integer)getPid.invoke(environment, noparamsObject);
                }
            }
            catch (Exception e) {
                TraceLog.get().message("PID calculation failed with Exception ", e, 10);
                pid = Utility.generateRandomPid();
            }
        }
        return pid;
    }

    public static int generateRandomPid() {
        Random rand = new Random();
        int result = rand.nextInt(1000000) + 1;
        return result;
    }

    public static Throwable throwableInitCauseException(Throwable src, Throwable cause) {
        src.initCause(cause);
        return src;
    }

    public static void waitABit(int milliSeconds) {
        if (milliSeconds == 0) {
            return;
        }
        try {
            Thread.sleep(milliSeconds);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    public static boolean isValidDnsDomainName(String name) {
        String nameToTest;
        if (null == name || 0 == name.length()) {
            return true;
        }
        for (char c : name.toCharArray()) {
            if (!illegalDNSChars.contains(Character.valueOf(c))) continue;
            return false;
        }
        int ptr = name.indexOf(46);
        String string = nameToTest = ptr > 0 ? name.substring(0, ptr) : name;
        return nameToTest.length() <= 15;
    }

    static {
        String illegalDNSNameCharacters = "\\/:*?\"<>|";
        illegalDNSChars = new HashSet<Character>();
        for (char c : "\\/:*?\"<>|".toCharArray()) {
            illegalDNSChars.add(Character.valueOf(c));
        }
        pid = -1;
    }

    public static class ConstructorInfo {
        private ClassInfo classInfo;
        private Constructor constructor;
        private Class[] argsTypes;

        public ConstructorInfo(ClassInfo classInfo, Class[] argsTypes) {
            this.classInfo = classInfo;
            this.argsTypes = argsTypes;
        }

        private void initConstructor() {
            if (null == this.constructor && null != this.classInfo.getSavedClass()) {
                try {
                    this.constructor = this.classInfo.getSavedClass().getConstructor(this.argsTypes);
                }
                catch (Exception e) {
                    TraceLog.get().error("Get constructor failed: ", e);
                }
            }
        }

        public Object newInstance(Object ... args) throws NqException {
            Object res = null;
            this.initConstructor();
            if (null != this.constructor) {
                try {
                    res = this.constructor.newInstance(args);
                }
                catch (Exception e) {
                    NqException nqe;
                    if (e.getCause() instanceof NqException) {
                        nqe = (NqException)e.getCause();
                    } else {
                        nqe = new NqException("Create new instance failed: " + e.getMessage(), -23);
                        nqe.initCause(e);
                    }
                    throw nqe;
                }
            }
            return res;
        }
    }

    public static class MethodInfo {
        private ClassInfo classInfo;
        private String methodName;
        private Method method;
        private Class[] argsTypes;

        public MethodInfo(ClassInfo classInfo, String methodName, Class[] argsTypes) {
            this.classInfo = classInfo;
            this.methodName = methodName;
            this.argsTypes = argsTypes;
        }

        private void initMethod() {
            if (null == this.method && null != this.classInfo.getSavedClass()) {
                try {
                    this.method = this.classInfo.getSavedClass().getMethod(this.methodName, this.argsTypes);
                }
                catch (Exception e) {
                    TraceLog.get().error("Get method " + this.methodName + " failed: ", e);
                }
            }
        }

        public Object invokeMethod(Object instance, Object ... args) throws NqException {
            Object res = null;
            this.initMethod();
            if (null != this.method) {
                try {
                    res = this.method.invoke(instance, args);
                }
                catch (Exception e) {
                    NqException nqe;
                    if (e.getCause() instanceof NqException) {
                        nqe = (NqException)e.getCause();
                    } else {
                        nqe = new NqException("Invoke method " + this.methodName + " failed: " + e.getMessage(), -23);
                        nqe.initCause(e);
                    }
                    throw nqe;
                }
            }
            return res;
        }

        public boolean invokeBooleanMethod(Object ... args) {
            Object res = null;
            try {
                res = this.invokeMethod(null, args);
            }
            catch (NqException nqException) {
                // empty catch block
            }
            return Boolean.TRUE.equals(res);
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static class ClassInfo {
        private String className;
        private Class<?> c;
        private boolean isClassSupported = true;

        public ClassInfo(String className) {
            this.className = className;
        }

        public void initClass() {
            if (this.isClassSupported && null == this.c) {
                this.c = Utility.isClassForName(this.className);
                if (null == this.c) {
                    this.isClassSupported = false;
                }
            }
        }

        public Class<?> getSavedClass() {
            this.initClass();
            return this.c;
        }

        public boolean isClassSupported() {
            this.initClass();
            return this.isClassSupported;
        }
    }
}

