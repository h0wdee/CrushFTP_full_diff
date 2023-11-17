/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.Client;
import com.maverick.ssh.ForwardingRequestListener;
import com.maverick.ssh.SshAuthentication;
import com.maverick.ssh.SshClientConnector;
import com.maverick.ssh.SshClientListener;
import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshSession;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.SshTunnel;

public interface SshClient
extends Client {
    public void connect(SshTransport var1, SshContext var2, SshClientConnector var3, String var4, String var5, String var6, boolean var7) throws SshException;

    public int authenticate(SshAuthentication var1) throws SshException;

    public SshSession openSessionChannel() throws SshException, ChannelOpenException;

    public SshSession openSessionChannel(long var1) throws SshException, ChannelOpenException;

    public SshSession openSessionChannel(ChannelEventListener var1) throws SshException, ChannelOpenException;

    public SshSession openSessionChannel(ChannelEventListener var1, long var2) throws SshException, ChannelOpenException;

    public SshSession openSessionChannel(int var1, int var2, ChannelEventListener var3) throws ChannelOpenException, SshException;

    public SshSession openSessionChannel(int var1, int var2, ChannelEventListener var3, long var4) throws ChannelOpenException, SshException;

    public SshTunnel openForwardingChannel(String var1, int var2, String var3, int var4, String var5, int var6, SshTransport var7, ChannelEventListener var8) throws SshException, ChannelOpenException;

    public SshClient openRemoteClient(String var1, int var2, String var3, SshClientConnector var4) throws SshException, ChannelOpenException;

    public SshClient openRemoteClient(String var1, int var2, String var3) throws SshException, ChannelOpenException;

    public int requestRemoteForwarding(String var1, int var2, String var3, int var4, ForwardingRequestListener var5) throws SshException;

    public boolean cancelRemoteForwarding(String var1, int var2) throws SshException;

    public void disconnect();

    public void addListener(SshClientListener var1);

    public boolean isAuthenticated();

    public boolean isConnected();

    public String getRemoteIdentification();

    public String getUsername();

    public SshClient duplicate() throws SshException;

    public SshContext getContext();

    public int getChannelCount();

    public int getVersion();

    public boolean isBuffered();

    public SshTransport getTransport();

    public void addAttribute(String var1, Object var2);

    public Object getAttribute(String var1);

    public <T> T getAttribute(String var1, T var2);

    public boolean hasAttribute(String var1);

    public String getIdent();

    public String getHost();

    public String getUuid();
}

