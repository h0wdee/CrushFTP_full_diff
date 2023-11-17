/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.nio.ByteBuffer;

public class Native {
    public static final boolean IS_64 = "amd64".equals(System.getProperty("os.arch"));

    public static native long loadLibrary(String var0);

    public static native void freeLibrary(long var0);

    public static native long getProcAddress(long var0, String var2);

    public static native long malloc(int var0);

    public static native void free(long var0);

    public static native ByteBuffer fromPointer(long var0, long var2);

    public static native boolean bind(Class var0, String var1, String var2, long var3);

    public static native long newGlobalRef(Object var0);

    public static native void deleteGlobalRef(long var0);

    public static native long getObjectId(Object var0);

    public static native Object getObject(long var0);

    public static native long getMethodId(Class var0, String var1, String var2, boolean var3);
}

