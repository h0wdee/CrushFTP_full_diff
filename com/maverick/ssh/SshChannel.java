/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshIO;
import com.maverick.ssh.message.SshMessageRouter;

public interface SshChannel
extends SshIO {
    public int getChannelId();

    public boolean isClosed();

    public void addChannelEventListener(ChannelEventListener var1);

    public void removeChannelEventListener(ChannelEventListener var1);

    public void setAutoConsumeInput(boolean var1);

    public SshMessageRouter getMessageRouter();

    public void waitForOpen();

    public int getMaximumRemotePacketLength();

    public int getMaximumLocalPacketLength();

    public SshClient getClient();

    public long getRemoteWindow();

    public long getMaximumRemoteWindowSize();

    public long getMaximumLocalWindowSize();
}

