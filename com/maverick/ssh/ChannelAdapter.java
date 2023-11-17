/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.SshChannel;

public abstract class ChannelAdapter
implements ChannelEventListener {
    @Override
    public void channelOpened(SshChannel channel) {
    }

    @Override
    public void channelClosing(SshChannel channel) {
    }

    @Override
    public void channelClosed(SshChannel channel) {
    }

    @Override
    public void channelEOF(SshChannel channel) {
    }

    @Override
    public void dataReceived(SshChannel channel, byte[] buf, int off, int len) {
    }

    @Override
    public void dataSent(SshChannel channel, byte[] buf, int off, int len) {
    }

    @Override
    public void extendedDataReceived(SshChannel channel, byte[] data, int off, int len, int extendedDataType) {
    }
}

