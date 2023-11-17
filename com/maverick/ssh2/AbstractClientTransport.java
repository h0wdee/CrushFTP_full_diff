/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 */
package com.maverick.ssh2;

import com.maverick.ssh.SshException;
import com.maverick.ssh2.Ssh2Context;
import org.slf4j.Logger;

public interface AbstractClientTransport {
    public void disconnect(int var1, String var2);

    public Ssh2Context getContext();

    public byte[] nextMessage(long var1) throws SshException;

    public void sendMessage(byte[] var1, boolean var2) throws SshException;

    public String getIdent();

    public String getHost();

    public void info(Logger var1, String var2, Object ... var3);

    public void debug(Logger var1, String var2, Object ... var3);

    public void error(Logger var1, String var2, Exception var3);
}

