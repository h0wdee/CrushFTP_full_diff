/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshChannel;

public interface ChannelEventListener {
    public void channelOpened(SshChannel var1);

    public void channelClosing(SshChannel var1);

    public void channelClosed(SshChannel var1);

    public void channelEOF(SshChannel var1);

    public void dataReceived(SshChannel var1, byte[] var2, int var3, int var4);

    public void dataSent(SshChannel var1, byte[] var2, int var3, int var4);

    public void extendedDataReceived(SshChannel var1, byte[] var2, int var3, int var4, int var5);
}

