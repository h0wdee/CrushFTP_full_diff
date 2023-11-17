/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples.customlogger;

import com.visuality.nq.common.TraceLog;

public class CustomLogger
extends TraceLog {
    private static int stackDepth = 0;

    public void message(String text, int level) {
        this.logit("MESSAGE => level=" + level + ", " + text);
    }

    public void error(String text, int level, int status) {
        this.logit("ERROR => level=" + level + ", " + text);
    }

    public void enter(int level) {
        this.logit("ENTER => level=" + level);
    }

    public void exit(int level) {
        this.logit("EXIT => level=" + level);
    }

    public void start(int level) {
        this.logit("START => level=" + level);
    }

    public void stop(int level) {
        this.logit("STOP => level=" + level);
    }

    public void caught(Exception ex, int level) {
        this.logit("EXCEPTION CAUGHT => level=" + level + ", " + ex);
    }

    private void logit(String messageToLog) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stack = new Throwable().getStackTrace();
        int depth = stackDepth;
        if (stack.length > (depth = this.calculateDepth(depth, stack)) && depth >= 0) {
            sb.append(stack[depth].getFileName());
            sb.append(";");
            sb.append(stack[depth].getMethodName());
            sb.append(";");
            sb.append(stack[depth].getLineNumber());
            sb.append(";");
            sb.append(messageToLog);
        }
        System.out.println(sb.toString());
    }

    private int calculateDepth(int depth, StackTraceElement[] stack) {
        if (depth >= stack.length && (depth = stack.length - 1) < 0) {
            return depth;
        }
        if (stack[depth].getFileName().equals("CustomLogger.java")) {
            depth += 2;
        }
        if (stack[depth].getFileName().equals("InternalTraceLog.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("TraceLog.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("NqException.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("SmbException.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("ClientException.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("NetbiosException.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("TestException.java")) {
            ++depth;
        }
        return depth;
    }

    public boolean canLog(int level) {
        return true;
    }

    static {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stack.length; ++i) {
            if (!stack[i].getClassName().equals(CustomLogger.class.getName())) continue;
            stackDepth = i + 1;
            break;
        }
    }
}

