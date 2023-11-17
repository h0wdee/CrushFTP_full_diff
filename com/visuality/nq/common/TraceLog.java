/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.InternalTraceLog;
import com.visuality.nq.common.NqException;
import com.visuality.nq.config.Config;
import java.util.Arrays;

public abstract class TraceLog {
    public static final String INFO = "I";
    public static final String ERROR = "E";
    public static final String ENTER = "F";
    public static final String LEAVE = "L";
    private static TraceLog theTraceLog = new InternalTraceLog();
    protected int threshold = 500;
    public static final int LEVEL_ASSERT = 5;
    public static final int LEVEL_ERROR = 10;
    public static final int LEVEL_API = 200;
    public static final int LEVEL_PROTOCOL = 250;
    public static final int LEVEL_FUNCTION = 300;
    public static final int LEVEL_ESSENTIAL = 700;
    public static final int LEVEL_INFO = 1000;
    public static final int LEVEL_TOOL = 1500;
    public static final int LEVEL_DEBUG = 2000;
    private static final int DUMP_BLOCKSIZE = 16;

    public static TraceLog get() {
        return theTraceLog;
    }

    public static void set(TraceLog theTraceLog) {
        TraceLog.theTraceLog.stop();
        TraceLog.theTraceLog = theTraceLog;
    }

    public void message(String text, Object obj, int level) {
        if (this.canLog(level)) {
            String objString = null == obj ? "null" : obj.toString();
            this.message(text + objString, level);
        }
    }

    public abstract void message(String var1, int var2);

    public void message(Object obj, int level) {
        if (this.canLog(level)) {
            String objString = null == obj ? "null" : obj.toString();
            this.message(objString, level);
        }
    }

    public void message(Object obj) {
        this.message(obj, 1000);
    }

    public abstract void error(String var1, int var2, int var3);

    public void error(String text, Object obj, int level, int status) {
        if (this.canLog(level)) {
            String objString = null == obj ? "null" : obj.toString();
            this.error(text + objString, level, status);
        }
    }

    public void error(String text, int status) {
        this.error(text, 10, status);
    }

    public void error(String text, Object obj, int status) {
        if (this.canLog(10)) {
            String objString = null == obj ? "null" : obj.toString();
            this.error(text + objString, 10, status);
        }
    }

    public void error(String text) {
        this.error(text, 10, 0);
    }

    public void error(String text, Object obj) {
        if (this.canLog(10)) {
            String objString = null == obj ? "null" : obj.toString();
            this.error(text + objString, 10, 0);
        }
    }

    public abstract void enter(int var1);

    public void enter(Object obj, int level) {
        this.enter(level);
        this.message(obj, level);
    }

    public void enter(String text, Object obj, int level) {
        if (this.canLog(level)) {
            this.enter(text + obj, level);
        }
    }

    public void enter() {
        this.enter(300);
    }

    public abstract void exit(int var1);

    public void exit(Object obj, int level) {
        this.exit(level);
        this.message(obj, level);
    }

    public void exit(String text, Object obj, int level) {
        if (this.canLog(level)) {
            String objString = null == obj ? "null" : obj.toString();
            this.exit(text + objString, level);
        }
    }

    public void exit() {
        this.exit(300);
    }

    public abstract void start(int var1);

    public void start() {
        this.start(300);
    }

    public abstract void stop(int var1);

    public void stop() {
        this.stop(300);
    }

    public void dump(String text, int level, byte[] data, int offset, int length) {
        this.message(text + "dump :", level);
        int i = 0;
        while (length > 0) {
            byte[] temp = new byte[16];
            Arrays.fill(temp, (byte)0);
            System.arraycopy(data, offset, temp, 0, length > 16 ? 16 : length);
            Integer[] toPrint = new Integer[17];
            toPrint[0] = i;
            for (int n = 0; n < temp.length; ++n) {
                toPrint[n + 1] = 0xFF & temp[n];
            }
            this.message(String.format("%8d  %02X %02X %02X %02X %02X %02X %02X %02X   %02X %02X %02X %02X %02X %02X %02X %02X", toPrint), level);
            i += 16;
            offset += 16;
            length = length > 16 ? length - 16 : 0;
        }
    }

    public void dump(String text, byte[] data, int offset, int length) {
        this.dump(text, 1000, data, offset, length);
    }

    public void dump(String text, int level, byte[] data) {
        this.dump(text, level, data, 0, data.length);
    }

    public void dump(String text, byte[] data) {
        this.dump(text, 1000, data, 0, data.length);
    }

    public abstract void caught(Exception var1, int var2);

    public void caught(Exception ex) {
        this.caught(ex, 300);
    }

    protected TraceLog() {
        try {
            this.threshold = Config.jnq.getInt("LOGTHRESHOLD");
        }
        catch (NqException e) {
            System.err.println(e.getMessage() + "; using default value");
            Config.Param param = (Config.Param)Config.jnq.getParamCache().get("LOGTHRESHOLD");
            this.threshold = (Integer)param.getDefaultValue();
        }
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public static String formatExceptionMessage(NqException e) {
        return "Exception thrown; " + e.getMessage() + ", error code = " + e.getErrCode();
    }

    public abstract boolean canLog(int var1);
}

