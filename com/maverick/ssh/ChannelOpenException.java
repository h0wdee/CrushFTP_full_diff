/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

public class ChannelOpenException
extends Exception {
    private static final long serialVersionUID = 6894031873211048989L;
    public static final int ADMINISTRATIVIVELY_PROHIBITED = 1;
    public static final int CONNECT_FAILED = 2;
    public static final int UNKNOWN_CHANNEL_TYPE = 3;
    public static final int RESOURCE_SHORTAGE = 4;
    int reason;

    public ChannelOpenException(String msg, int reason) {
        super(msg);
        this.reason = reason;
    }

    public int getReason() {
        return this.reason;
    }
}

