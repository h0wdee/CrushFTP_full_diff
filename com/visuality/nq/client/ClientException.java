/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.common.NqException;

public class ClientException
extends NqException {
    private static final long serialVersionUID = 1L;
    public static final int ERR_MOUNTERROR = -101;
    public static final int ERR_SERVERCONNECT = -102;
    public static final int ERR_BADPARAM = -103;
    public static final int ERR_INTERNAL = -104;
    public static final int ERR_ACCESSDENIED = -105;
    public static final int ERR_IO = -106;
    public static final int ERR_BADDATA = -107;
    public static final int ERR_DFSCAHCEOVERFLOW = -108;
    public static final int ERR_TIMEOUT = -109;
    public static final int ERR_NOTSUPPORTED = -110;
    public static final int ERR_CONNECTION = -111;
    public static final int ERR_INVALID_CREDITS = -112;

    protected ClientException(String message) {
        super(message);
    }

    protected ClientException(String message, int errCode) {
        super(message, errCode);
    }

    protected ClientException(String message, int errCode, int traceLogLevel) {
        super(message, errCode, traceLogLevel);
    }

    protected ClientException(String message, StackTraceElement[] stackTrace, int errCode) {
        super(message, stackTrace, errCode);
    }
}

