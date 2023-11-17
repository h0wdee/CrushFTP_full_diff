/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.NqException;
import java.net.InetAddress;

public abstract class ResolverMethod {
    public boolean multicast = true;

    public ResolverMethod(boolean isMulticast) {
        this.multicast = isMulticast;
    }

    public abstract long getTimeout();

    public abstract String ipToHost(InetAddress var1) throws NqException;

    public abstract InetAddress[] hostToIp(String var1) throws NqException;

    public abstract String getDCNameByDomain(String var1) throws NqException;

    public abstract String printParams();
}

