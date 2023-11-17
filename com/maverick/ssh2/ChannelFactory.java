/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.SshException;
import com.maverick.ssh2.Ssh2Channel;

public interface ChannelFactory {
    public String[] supportedChannelTypes();

    public Ssh2Channel createChannel(String var1, byte[] var2) throws SshException, ChannelOpenException;
}

