/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.PInvoke;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class INI {
    public static final String MAIN_CLASS = ":main.class";
    public static final String SERVICE_CLASS = ":service.class";
    public static final String MODULE_NAME = "WinRun4J:module.name";
    public static final String MODULE_INI = "Winrun4J:module.ini";
    public static final String MODULE_DIR = "WinRun4J:module.dir";
    public static final String INI_DIR = "WinRun4J:ini.dir";
    public static final String WORKING_DIR = ":working.directory";
    public static final String SINGLE_INSTANCE = ":single.instance";
    public static final String DDE_ENABLED = ":dde.enabled";
    public static final String DDE_WINDOW_CLASS = ":dde.window.class";
    public static final String DDE_SERVER_NAME = ":dde.server.name";
    public static final String DDE_TOPIC = ":dde.topic";
    public static final String SERVICE_ID = ":service.id";
    public static final String SERVICE_NAME = ":service.name";
    public static final String SERVICE_DESCRIPTION = ":service.description";
    public static final String SERVICE_CONTROLS = ":service.controls";
    public static final String SERVICE_STARTUP = ":service.startup";
    public static final String SERVICE_DEPENDENCY = ":service.dependency";
    public static final String SERVICE_USER = ":service.user";
    public static final String SERVICE_PWD = ":service.password";
    public static final String SERVICE_LOAD_ORDER_GROUP = ":service.loadordergroup";

    public static String getProperty(String key) {
        long k = NativeHelper.toNativeString(key, false);
        long r = NativeHelper.call(0L, "INI_GetProperty", k);
        String res = null;
        if (r != 0L) {
            res = NativeHelper.getString(r, 4096L, false);
        }
        NativeHelper.free(k);
        return res;
    }

    public static String getProperty(String key, String defaultValue) {
        String res = INI.getProperty(key);
        return res != null ? res : defaultValue;
    }

    public static String[] getPropertyKeys() {
        long d = NativeHelper.call(0L, "INI_GetDictionary", new long[0]);
        int n = NativeHelper.getInt(d);
        long keyPtr = NativeHelper.getInt(d + (long)(Native.IS_64 ? 16 : 12));
        String[] res = new String[n];
        int i = 0;
        int offset = 0;
        while (i < n) {
            long ptr = NativeHelper.getPointer(keyPtr + (long)offset);
            res[i] = NativeHelper.getString(ptr, 260L, false);
            ++i;
            offset += NativeHelper.PTR_SIZE;
        }
        return res;
    }

    public static Map<String, String> getProperties() {
        HashMap<String, String> props = new HashMap<String, String>();
        String[] keys = INI.getPropertyKeys();
        for (int i = 0; i < keys.length; ++i) {
            props.put(keys[i], INI.getProperty(keys[i]));
        }
        return props;
    }

    public static String[] getNumberedEntries(String baseKey) {
        String v;
        ArrayList<String> l = new ArrayList<String>();
        int i = 1;
        do {
            if ((v = INI.getProperty(baseKey + "." + i)) == null) continue;
            l.add(v);
        } while (++i <= 10 || v != null);
        return l.toArray(new String[l.size()]);
    }

    static {
        PInvoke.bind(INI.class);
    }
}

