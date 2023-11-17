/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import com.maverick.ssh.SshTunnel;
import java.net.SocketAddress;

public interface ForwardingClientListener {
    public static final int LOCAL_FORWARDING = 1;
    public static final int REMOTE_FORWARDING = 2;
    public static final int X11_FORWARDING = 3;

    public boolean checkLocalSourceAddress(SocketAddress var1, String var2, int var3, String var4, int var5);

    public void forwardingStarted(int var1, String var2, String var3, int var4);

    public void forwardingStopped(int var1, String var2, String var3, int var4);

    public void channelFailure(int var1, String var2, String var3, int var4, boolean var5, Throwable var6);

    public void channelOpened(int var1, String var2, SshTunnel var3);

    public void channelClosed(int var1, String var2, SshTunnel var3);
}

