/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.PInvoke;

public class Environment {
    private static final long library;

    public static String expandEnvironmentString(String var) {
        if (var == null) {
            return null;
        }
        long str = NativeHelper.toNativeString(var, true);
        long buf = Native.malloc(4096);
        long res = NativeHelper.call(library, "ExpandEnvironmentStringsW", str, buf, 4096L);
        String rs = null;
        if (res > 0L && res <= 4096L) {
            rs = NativeHelper.getString(buf, 4096L, true);
        }
        Native.free(str);
        Native.free(buf);
        return rs;
    }

    public static String[] getCommandLine() {
        long res = NativeHelper.call(library, "GetCommandLineW", new long[0]);
        String s = NativeHelper.getString(res, 1024L, true);
        boolean inQuote = false;
        ArrayList<String> args = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if (c == '\"') {
                boolean bl = inQuote = !inQuote;
            }
            if (c == ' ') {
                if (inQuote) {
                    sb.append(c);
                    continue;
                }
                args.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            sb.append(c);
        }
        if (sb.length() > 0) {
            args.add(sb.toString());
        }
        return args.toArray(new String[args.size()]);
    }

    public static Properties getEnvironmentVariables() {
        String s;
        long buf = NativeHelper.call(library, "GetEnvironmentStringsW", new long[0]);
        ByteBuffer bb = NativeHelper.getBuffer(buf, Short.MAX_VALUE);
        Properties p = new Properties();
        while ((s = NativeHelper.getString(bb, true)) != null && s.length() != 0) {
            int idx = s.indexOf(61);
            p.put(s.substring(0, idx), s.substring(idx + 1));
        }
        NativeHelper.call(library, "FreeEnvironmentStringsW", buf);
        return p;
    }

    public static String getEnv(String name) {
        StringBuilder sb = new StringBuilder();
        PInvoke.UIntPtr ptr = new PInvoke.UIntPtr(4096L);
        int res = Environment.GetEnvironmentVariable(name, sb, ptr);
        if (res > 0) {
            return sb.toString();
        }
        return null;
    }

    @PInvoke.DllImport(value="kernel32.dll")
    public static native int GetEnvironmentVariable(String var0, StringBuilder var1, PInvoke.UIntPtr var2);

    public static File[] getLogicalDrives() {
        int len = 1024;
        long buf = Native.malloc(len);
        long res = NativeHelper.call(library, "GetLogicalDriveStringsW", len, buf);
        ByteBuffer bb = NativeHelper.getBuffer(buf, (int)(res + 1L) << 1);
        ArrayList<File> drives = new ArrayList<File>();
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c;
            if ((c = bb.getChar()) == '\u0000') {
                if (sb.length() == 0) break;
                drives.add(new File(sb.toString()));
                sb.setLength(0);
                continue;
            }
            sb.append(c);
        }
        Native.free(buf);
        return drives.toArray(new File[drives.size()]);
    }

    @PInvoke.DllImport(value="kernel32.dll")
    public static native boolean GetVersionEx(OSVERSIONINFOEX var0);

    static {
        PInvoke.bind(Environment.class, "kernel32.dll");
        library = Native.loadLibrary("kernel32.dll");
    }

    public static class OSVERSIONINFOEX
    implements PInvoke.Struct {
        public int sizeOf;
        public int majorVersion;
        public int minorVersion;
        public int buildNumber;
        public int platformId;
        @PInvoke.MarshalAs(sizeConst=128)
        public String csdVersion;
        public short servicePackMajor;
        public short servicePackMinor;
        public short suiteMask;
        public byte productType;
        public byte reserved;
    }
}

