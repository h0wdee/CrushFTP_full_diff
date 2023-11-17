/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.NqException;
import com.visuality.nq.common.ResolverMethod;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.resolve.DnsMessage;
import com.visuality.nq.resolve.NetbiosException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

public class DnsResolver
extends ResolverMethod {
    private InetAddress server;
    private String serverName;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DnsResolver(boolean isMulticast, InetAddress serverIp) {
        super(isMulticast);
        TraceLog.get().enter("serverIp = ", serverIp, 250);
        this.server = serverIp;
        DnsMessage msg = null;
        try {
            msg = new DnsMessage(this.server);
            this.serverName = msg.queryByIp(this.server);
            if (-1 != this.serverName.indexOf(46)) {
                this.serverName = this.serverName.substring(this.serverName.indexOf(46) + 1, this.serverName.length());
            }
        }
        catch (SocketException e) {
        }
        catch (NqException e) {
        }
        finally {
            if (null != msg) {
                msg.terminate();
            }
            TraceLog.get().exit();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String ipToHost(InetAddress ip) throws NqException {
        TraceLog.get().enter("ip = ", ip, 250);
        DnsMessage msg = null;
        try {
            msg = new DnsMessage(this.server);
            String host = msg.queryByIp(ip);
            TraceLog.get().exit("host = " + host, 250);
            String string = host;
            return string;
        }
        catch (SocketException e1) {
            TraceLog.get().exit("host = null", 250);
            String string = null;
            return string;
        }
        catch (NetbiosException e) {
            TraceLog.get().exit("host = null", 250);
            String string = null;
            return string;
        }
        finally {
            if (null != msg) {
                msg.terminate();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public InetAddress[] hostToIp(String host) throws NqException {
        Vector ips;
        block14: {
            DnsMessage msg;
            TraceLog.get().enter("host = " + host, 250);
            try {
                msg = new DnsMessage(this.server);
            }
            catch (SocketException e) {
                TraceLog.get().exit(e, 250);
                throw new NqException(e.getMessage(), -12);
            }
            ips = null;
            try {
                if (host.toLowerCase().contains(".")) {
                    msg.queryByName(host);
                    ips = msg.getIps();
                    break block14;
                }
                try {
                    msg.queryByName(host);
                    ips = msg.getIps();
                }
                catch (NetbiosException e) {
                    ips = null;
                }
                if (null == ips && null != this.serverName && 0 < this.serverName.length() && !host.toLowerCase().contains(this.serverName.toLowerCase())) {
                    host = host + "." + this.serverName;
                    msg.queryByName(host);
                    ips = msg.getIps();
                }
            }
            catch (NetbiosException e) {
                ips = null;
            }
            finally {
                msg.terminate();
            }
        }
        if (ips == null || ips.size() == 0) {
            TraceLog.get().exit("ip = null", 250);
            return null;
        }
        HashSet<InetAddress> list = new HashSet<InetAddress>();
        for (int i = 0; i < ips.size(); ++i) {
            list.add((InetAddress)ips.elementAt(i));
        }
        if (TraceLog.get().canLog(250)) {
            TraceLog.get().message("host's ip = " + Arrays.toString(list.toArray(new InetAddress[0])), 250);
        }
        TraceLog.get().exit(250);
        return list.toArray(new InetAddress[0]);
    }

    public String getDCNameByDomain(String domain) throws NqException {
        TraceLog.get().enter("domain = " + domain, 250);
        DnsMessage msg = null;
        try {
            msg = new DnsMessage(this.server);
            msg.queryDCByDomain(domain);
            TraceLog.get().exit("name = " + msg.getName(), 250);
            String string = msg.getName();
            return string;
        }
        catch (NetbiosException e) {
            TraceLog.get().exit("name = null", 250);
            String string = null;
            return string;
        }
        catch (SocketException e) {
            TraceLog.get().exit(e, 250);
            throw new NqException(e.getMessage(), -12);
        }
        finally {
            if (null != msg) {
                msg.terminate();
            }
        }
    }

    public long getTimeout() {
        return 1000L;
    }

    public String printParams() {
        return "DNS: " + this.server;
    }
}

