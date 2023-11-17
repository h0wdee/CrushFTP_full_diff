/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

public interface TransportProtocolListener {
    public void onDisconnect(String var1, int var2);

    public void onIdle(long var1);

    public void onReceivedDisconnect(String var1, int var2);
}

