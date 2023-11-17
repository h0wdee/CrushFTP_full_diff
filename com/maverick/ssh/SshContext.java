/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.ExecutorServiceProvider;
import com.maverick.ssh.ForwardingRequestListener;
import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.SshException;

public interface SshContext
extends ExecutorServiceProvider {
    public void setChannelLimit(int var1);

    public int getChannelLimit();

    public void setHostKeyVerification(HostKeyVerification var1);

    public HostKeyVerification getHostKeyVerification();

    public void setSFTPProvider(String var1);

    public String getSFTPProvider();

    public void setX11Display(String var1);

    public String getX11Display();

    public byte[] getX11AuthenticationCookie() throws SshException;

    public void setX11AuthenticationCookie(byte[] var1);

    public void setX11RealCookie(byte[] var1);

    public byte[] getX11RealCookie() throws SshException;

    public void setX11RequestListener(ForwardingRequestListener var1);

    public ForwardingRequestListener getX11RequestListener();

    public void setMessageTimeout(int var1);

    public int getMessageTimeout();

    @Deprecated
    public void enableFIPSMode() throws SshException;

    public boolean isSHA1SignaturesSupported();

    public void setSHA1SignaturesSupported(boolean var1);
}

