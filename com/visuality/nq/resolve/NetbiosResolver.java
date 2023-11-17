/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.NqException;
import com.visuality.nq.common.ResolverMethod;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.resolve.NameMessage;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosName;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Vector;

public class NetbiosResolver
extends ResolverMethod {
    public NetbiosResolver(boolean isMulticast) {
        super(isMulticast);
    }

    public String ipToHost(InetAddress ip) throws NqException {
        NameMessage msg = new NameMessage(this.multicast);
        try {
            msg.queryByIp(ip);
            if (null != msg.getName()) {
                return msg.getName().getName();
            }
            return null;
        }
        catch (NetbiosException e) {
            return null;
        }
    }

    public InetAddress[] hostToIp(String host) throws NqException {
        Vector ips;
        int idx = host.indexOf(46);
        if (-1 != idx) {
            host = host.substring(0, idx);
        }
        if (!((ips = this.internalHostToIp(host, 28)) != null && ips.size() != 0 || (ips = this.internalHostToIp(host, 0)) != null && ips.size() != 0)) {
            return null;
        }
        HashSet<InetAddress> list = new HashSet<InetAddress>();
        for (int i = 0; i < ips.size(); ++i) {
            list.add((InetAddress)ips.elementAt(i));
        }
        return list.toArray(new InetAddress[0]);
    }

    private Vector internalHostToIp(String host, int role) throws NqException {
        NameMessage msg = new NameMessage(this.multicast, new NetbiosName(host, role));
        Vector ips = null;
        try {
            msg.queryByName();
            TraceLog.get().message("after query");
            ips = msg.getIps();
        }
        catch (NetbiosException e) {
            TraceLog.get().error("NetbiosException = ", e, e.getErrCode());
        }
        if (ips == null || ips.size() == 0) {
            return null;
        }
        return ips;
    }

    public String getDCNameByDomain(String domain) throws NqException {
        return null;
    }

    public long getTimeout() {
        return this.multicast ? 2000L : 1000L;
    }

    public String printParams() {
        return "NetBios";
    }

    private static class GetByIpRunner
    implements Runnable {
        private InetAddress ip;
        private NameMessage origin;

        protected GetByIpRunner(InetAddress ip, NameMessage origin) {
            this.ip = ip;
            this.origin = origin;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            block5: {
                NameMessage msg = new NameMessage();
                try {
                    msg.queryByIp(this.ip);
                    if (msg.getName() == null) break block5;
                    NameMessage nameMessage = this.origin;
                    synchronized (nameMessage) {
                        this.origin.setName(msg.getName());
                        this.origin.notify();
                    }
                }
                catch (NqException nqException) {
                    // empty catch block
                }
            }
        }
    }
}

