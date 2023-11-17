/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import org.boris.winrun4j.PInvoke;

public class Registry {
    public static final int REG_NONE = 0;
    public static final int REG_SZ = 1;
    public static final int REG_EXPAND_SZ = 2;
    public static final int REG_BINARY = 3;
    public static final int REG_DWORD = 4;
    public static final int REG_DWORD_LITTLE_ENDIAN = 4;
    public static final int REG_DWORD_BIG_ENDIAN = 5;
    public static final int REG_LINK = 6;
    public static final int REG_MULTI_SZ = 7;
    public static final int REG_RESOURCE_LIST = 8;
    public static final int REG_FULL_RESOURCE_DESCRIPTOR = 9;
    public static final int REG_RESOURCE_REQUIREMENTS_LIST = 10;
    public static final int REG_QWORD = 11;
    public static final int REG_QWORD_LITTLE_ENDIAN = 11;

    @PInvoke.DllImport(entryPoint="RegCloseKey")
    public static native int closeKey(long var0);

    @PInvoke.DllImport(entryPoint="RegCreateKeyW")
    public static native int createKey(long var0, String var2, @PInvoke.Out PInvoke.UIntPtr var3);

    @PInvoke.DllImport(entryPoint="RegDeleteKeyW")
    public static native long deleteKey(long var0, String var2);

    @PInvoke.DllImport(entryPoint="RegDeleteValueW")
    public static native long deleteValue(long var0, String var2);

    @PInvoke.DllImport(entryPoint="RegOpenKeyExW")
    public static native int openKeyEx(long var0, String var2, int var3, long var4, PInvoke.UIntPtr var6);

    @PInvoke.DllImport(entryPoint="RegEnumKeyExW")
    public static native int enumKeyEx(long var0, int var2, StringBuilder var3, PInvoke.UIntPtr var4, long var5, long var7, long var9, FILETIME var11);

    @PInvoke.DllImport(entryPoint="RegEnumValue")
    public static native int enumValue(long var0, int var2, StringBuilder var3, PInvoke.UIntPtr var4, long var5, PInvoke.UIntPtr var7, PInvoke.ByteArrayBuilder var8, PInvoke.IntPtr var9);

    public static byte[] queryValueEx(long hKey, String valueName, int maxLen) {
        PInvoke.ByteArrayBuilder bb = new PInvoke.ByteArrayBuilder();
        PInvoke.UIntPtr len = new PInvoke.UIntPtr(maxLen);
        int res = Registry.queryValueEx(hKey, valueName, 0L, null, bb, len);
        if (res == 0) {
            return bb.toArray();
        }
        return null;
    }

    @PInvoke.DllImport(entryPoint="RegQueryValueEx")
    public static native int queryValueEx(long var0, String var2, long var3, PInvoke.UIntPtr var5, PInvoke.ByteArrayBuilder var6, PInvoke.UIntPtr var7);

    @PInvoke.DllImport(entryPoint="RegQueryValueEx")
    public static native int queryValueEx(long var0, String var2, long var3, PInvoke.UIntPtr var5, StringBuilder var6, PInvoke.UIntPtr var7);

    @PInvoke.DllImport(entryPoint="RegQueryInfoKey")
    public static native int queryInfoKey(long var0, StringBuilder var2, PInvoke.UIntPtr var3, long var4, PInvoke.UIntPtr var6, PInvoke.UIntPtr var7, PInvoke.UIntPtr var8, PInvoke.UIntPtr var9, PInvoke.UIntPtr var10, PInvoke.UIntPtr var11, PInvoke.UIntPtr var12, FILETIME var13);

    public static QUERY_INFO queryInfoKey(long hKey) {
        StringBuilder lpClass = new StringBuilder();
        PInvoke.UIntPtr lpcClass = new PInvoke.UIntPtr(255L);
        PInvoke.UIntPtr lpcSubKeys = new PInvoke.UIntPtr();
        PInvoke.UIntPtr lpcMaxSubKeyLen = new PInvoke.UIntPtr();
        PInvoke.UIntPtr lpcMaxClassLen = new PInvoke.UIntPtr();
        PInvoke.UIntPtr lpcValues = new PInvoke.UIntPtr();
        PInvoke.UIntPtr lpcMaxValueNameLen = new PInvoke.UIntPtr();
        PInvoke.UIntPtr lpcMaxValueLen = new PInvoke.UIntPtr();
        PInvoke.UIntPtr lpcbSecurityDescriptor = new PInvoke.UIntPtr();
        FILETIME lastWriteTime = null;
        int res = Registry.queryInfoKey(hKey, lpClass, lpcClass, 0L, lpcSubKeys, lpcMaxSubKeyLen, lpcMaxClassLen, lpcValues, lpcMaxValueNameLen, lpcMaxValueLen, lpcbSecurityDescriptor, lastWriteTime);
        if (res != 0) {
            return null;
        }
        QUERY_INFO info = new QUERY_INFO();
        info.keyClass = lpClass.toString();
        info.subKeyCount = lpcSubKeys.intValue();
        info.maxSubkeyLen = lpcMaxSubKeyLen.intValue();
        info.maxClassLen = lpcMaxClassLen.intValue();
        info.valueCount = lpcValues.intValue();
        info.maxValueNameLen = lpcMaxValueNameLen.intValue();
        info.maxValueLen = lpcMaxValueLen.intValue();
        info.cbSecurityDescriptor = lpcbSecurityDescriptor.intValue();
        info.lastWriteTime = lastWriteTime;
        return info;
    }

    @PInvoke.DllImport(entryPoint="RegSetValueEx")
    public static native long setValueEx(long var0, String var2, int var3, int var4, byte[] var5, int var6);

    static {
        PInvoke.bind(Registry.class, "advapi32.dll");
    }

    public static class FILETIME
    implements PInvoke.Struct {
        public int dwLowDateTime;
        public int dwHighDateTime;
    }

    public static class QUERY_INFO {
        public String keyClass;
        public int subKeyCount;
        public int maxSubkeyLen;
        public int maxClassLen;
        public int valueCount;
        public int maxValueNameLen;
        public int maxValueLen;
        public int cbSecurityDescriptor;
        public FILETIME lastWriteTime;
    }
}

