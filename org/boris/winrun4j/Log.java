/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.boris.winrun4j.NativeBinder;
import org.boris.winrun4j.PInvoke;

public class Log {
    public static void info(String msg) {
        Log.LogIt(Level.INFO.level, "[info]", msg);
    }

    public static void warning(String msg) {
        Log.LogIt(Level.WARN.level, "[warn]", msg);
    }

    public static void error(String msg) {
        Log.LogIt(Level.ERROR.level, " [err]", msg);
    }

    public static void error(Throwable t) {
        Log.error(t.getMessage());
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        Log.error(sw.toString());
    }

    @PInvoke.DllImport(entryPoint="Log_LogIt", internal=true, wideChar=false)
    private static native void LogIt(int var0, String var1, String var2);

    static {
        NativeBinder.bind(Log.class);
    }

    public static class Level {
        public static final Level INFO = new Level(0, "info");
        public static final Level WARN = new Level(1, "warning");
        public static final Level ERROR = new Level(2, "error");
        public static final Level NONE = new Level(3, "none");
        private int level;
        private String text;

        private Level(int level, String text) {
            this.level = level;
            this.text = text;
        }

        public int getLevel() {
            return this.level;
        }

        public String getText() {
            return this.text;
        }
    }
}

