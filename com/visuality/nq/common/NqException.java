/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.TraceLog;

public class NqException
extends Exception {
    private static final long serialVersionUID = -5547416435006509277L;
    public static final int ERR_LOCKCOUNT = -11;
    public static final int ERR_REPOSITORYITEM = -13;
    public static final int ERR_SERVERNAME = -12;
    public static final int ERR_SERVERADDRESS = -14;
    public static final int ERR_FALSEALARM = -15;
    public static final int ERR_SOCKET = -16;
    public static final int ERR_MEMORY = -17;
    public static final int ERR_CREDENTIALS = -18;
    public static final int ERR_TIMEOUT = -19;
    public static final int ERR_PARAMETER = -20;
    public static final int ERR_BADFORMAT = -21;
    public static final int ERR_NOSUPPORT = -22;
    public static final int ERR_GENERAL = -23;
    public static final int ERR_SHARENAME = -24;
    public static final int ERR_DFS_CONFIG = -25;
    public static final int ERR_SERVER_TIMEOUT = -26;
    public static final int ERR_SKIP_REFERRAL = -27;
    private int errCode;
    private String msg;
    protected int level = 0;

    public NqException(String message) {
        super(message);
        if (null == message || message.length() == 0) {
            message = TraceLog.formatExceptionMessage(this);
        }
        this.msg = message;
        this.errCode = 0;
    }

    public NqException(int errCode) {
        super("");
        this.msg = this.messageByCode(errCode) + "; " + TraceLog.formatExceptionMessage(this);
        this.errCode = errCode;
    }

    public NqException(String message, int errCode) {
        super(message);
        if (null == message || message.length() == 0) {
            message = TraceLog.formatExceptionMessage(this);
        }
        this.msg = message;
        this.errCode = errCode;
    }

    public NqException(String message, int errCode, int traceLogLevel) {
        super(message);
        if (null == message || message.length() == 0) {
            message = TraceLog.formatExceptionMessage(this);
        }
        this.msg = message;
        this.errCode = errCode;
    }

    public NqException(String message, StackTraceElement[] stackTrace, int errCode) {
        super(message);
        if (null == message || message.length() == 0) {
            message = TraceLog.formatExceptionMessage(this);
        }
        this.msg = message;
        this.errCode = errCode;
        this.setStackTrace(stackTrace);
    }

    public int getErrCode() {
        return this.errCode;
    }

    public String getMessage() {
        return this.msg;
    }

    public String toString() {
        String errText = Math.abs(this.errCode) < 1000 ? ": " + this.errCode : ": +0x" + Integer.toHexString(this.errCode) + " (" + this.errCode + ")";
        return this.getMessage() + errText;
    }

    protected String messageByCode(int code) {
        switch (code) {
            case -11: {
                return "IInternal error (lock is less than zero)";
            }
            case -13: {
                return "Internal error (unable to get item in the repository)";
            }
            case -12: {
                return "Unable to resolve server name";
            }
            case -14: {
                return "Unable to resove IP";
            }
            case -15: {
                return "False alarm  (e.g., reconnecting a connected server)";
            }
            case -16: {
                return "Communication failure";
            }
            case -17: {
                return "Out of resources (e.g., - out of memory)";
            }
            case -18: {
                return "Unable to connect server with current credentials";
            }
            case -19: {
                return "Operation timed out";
            }
            case -20: {
                return "Illegal parameter";
            }
            case -21: {
                return "Illegal message format";
            }
            case -22: {
                return "Not supported";
            }
            case -23: {
                return "Internal error";
            }
        }
        return this.msg;
    }
}

