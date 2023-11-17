/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.NqException;

public class NetbiosException
extends NqException {
    private static final long serialVersionUID = 1L;
    public static final int ERR_SEND_ERROR = -501;
    public static final int ERR_RECEIVE_ERROR = -502;
    public static final int ERR_COMMUNICATION = -503;
    public static final int ERR_INVALID_SOCKET = -504;
    public static final int ERR_TIMEOUT = -505;
    public static final int ERR_GENERAL_ERROR = -506;
    public static final int ERR_NO_MORE_DATA = -507;
    public static final int ERR_NOT_NETBIOS_NAME = -508;
    public static final int ERR_ILLEGAL_DATAGRAM_DESTINATION = -509;

    public NetbiosException(String message) {
        super(message);
    }

    public NetbiosException(String message, int code) {
        super(message, code);
    }

    public NetbiosException(int code) {
        super(code);
    }

    protected String messageByCode(int code) {
        switch (code) {
            case 7: {
                return "Name conflict";
            }
            case 1: {
                return "Incorrect datagram format";
            }
            case 3: {
                return "Bad name or not found";
            }
            case 4: {
                return "Not supported";
            }
            case 5: {
                return "Operation refused";
            }
            case 2: {
                return "Server failure";
            }
        }
        return super.messageByCode(code);
    }
}

