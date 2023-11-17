/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.ResolverMethod;
import com.visuality.nq.common.SyncObject;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;
import com.visuality.nq.resolve.NetbiosDaemon;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Vector;

public final class Resolver {
    private static Vector methods = new Vector();
    static boolean externalRegistered = false;
    public final SyncObject syncObj = new SyncObject();
    public volatile int numOfThreadsI2H = 0;
    public volatile int numOfThreadsH2I = 0;
    public volatile int numOfThreadsDC = 0;
    static long MCAST_RESOLUTION_TIMEOUT;
    static long UCAST_RESOLUTION_TIMEOUT;
    static long MCAST_DC_RESOLUTION_TIMEOUT;
    static long UCAST_DC_RESOLUTION_TIMEOUT;
    private String host;
    private LinkedList ips;
    private String dcName;

    public Resolver() {
        NetbiosDaemon.start();
        MCAST_RESOLUTION_TIMEOUT = Resolver.getTimeoutProperty("MULTICAST_RESOLUTION_TIMEOUT", MCAST_RESOLUTION_TIMEOUT);
        UCAST_RESOLUTION_TIMEOUT = Resolver.getTimeoutProperty("UNICAST_RESOLUTION_TIMEOUT", UCAST_RESOLUTION_TIMEOUT);
        MCAST_DC_RESOLUTION_TIMEOUT = Resolver.getTimeoutProperty("MULTICAST_DC_RESOLUTION_TIMEOUT", MCAST_DC_RESOLUTION_TIMEOUT);
        UCAST_DC_RESOLUTION_TIMEOUT = Resolver.getTimeoutProperty("UNICAST_DC_RESOLUTION_TIMEOUT", UCAST_DC_RESOLUTION_TIMEOUT);
        this.ips = new LinkedList();
    }

    private static long getTimeoutProperty(String propertyName, long defaultValue) {
        Integer tmpTO = null;
        try {
            tmpTO = (Integer)Config.jnq.getNE(propertyName);
        }
        catch (Exception e) {
            TraceLog.get().error("Failed to get property " + propertyName, 2000);
        }
        return null != tmpTO && 0 < tmpTO ? (long)tmpTO.intValue() : defaultValue;
    }

    public static void registerMethod(ResolverMethod method) {
        boolean isInternalOnly;
        if (!methods.contains(method)) {
            methods.add(method);
        }
        if (!(isInternalOnly = ((Boolean)Config.jnq.getNE("ISUSEINTERNALONLY")).booleanValue()) && !externalRegistered) {
            externalRegistered = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String ipToHost(InetAddress ip) {
        Resolver e2;
        ResolverMethod aMethod;
        TraceLog.get().enter("ip = ", ip, 700);
        this.host = null;
        boolean threadsRunning = false;
        Enumeration en = methods.elements();
        Resolver resolver = this;
        synchronized (resolver) {
            this.numOfThreadsI2H = 0;
            while (en.hasMoreElements()) {
                aMethod = (ResolverMethod)en.nextElement();
                if (aMethod.multicast) continue;
                ++this.numOfThreadsI2H;
                threadsRunning = true;
                new Thread((Runnable)new IpToHostRunner(aMethod, ip), "Resolver.single.ipToHost").start();
            }
        }
        if (threadsRunning) {
            try {
                this.syncObj.syncWait(UCAST_RESOLUTION_TIMEOUT);
            }
            catch (InterruptedException e2) {
                TraceLog.get().error("Internal error: ", e2, 10, 0);
            }
            e2 = this;
            synchronized (e2) {
                this.numOfThreadsI2H = 0;
            }
        }
        if (null != this.host && !IpAddressHelper.isIpAddress(this.host)) {
            TraceLog.get().exit("host = " + this.host, 700);
            return this.host;
        }
        e2 = this;
        synchronized (e2) {
            threadsRunning = false;
            this.numOfThreadsI2H = 0;
            en = methods.elements();
            while (en.hasMoreElements()) {
                aMethod = (ResolverMethod)en.nextElement();
                if (!aMethod.multicast) continue;
                ++this.numOfThreadsI2H;
                threadsRunning = true;
                new Thread((Runnable)new IpToHostRunner(aMethod, ip), "Resolver.multi.ipToHost").start();
            }
        }
        if (threadsRunning) {
            try {
                this.syncObj.syncWait(MCAST_RESOLUTION_TIMEOUT);
            }
            catch (InterruptedException e3) {
                TraceLog.get().error("Internal error: ", e3, 10, 0);
            }
            resolver = this;
            synchronized (resolver) {
                this.numOfThreadsI2H = 0;
            }
        }
        if (null != this.host && !IpAddressHelper.isIpAddress(this.host)) {
            TraceLog.get().exit("host = " + this.host, 700);
            return this.host;
        }
        TraceLog.get().exit("host = null", 700);
        return null;
    }

    public InetAddress[] hostToIp(String host) {
        return this.hostToIp(host, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public InetAddress[] hostToIp(String host, InetAddress ip) {
        InetAddress[] addrs;
        Resolver e2;
        ResolverMethod aMethod;
        TraceLog.get().enter("host = " + host + ", ip = ", ip, 300);
        boolean threadsRunning = false;
        Enumeration en = methods.elements();
        Resolver resolver = this;
        synchronized (resolver) {
            this.ips.clear();
            if (0 != this.numOfThreadsH2I) {
                TraceLog.get().message("numOfThreadsH2I = " + this.numOfThreadsH2I, 300);
            }
            this.numOfThreadsH2I = 0;
            while (en.hasMoreElements()) {
                aMethod = (ResolverMethod)en.nextElement();
                if (aMethod.multicast) continue;
                ++this.numOfThreadsH2I;
                threadsRunning = true;
                new Thread((Runnable)new HostToIpRunner(aMethod, host, ip), "Resolver.single.hostToIp").start();
            }
        }
        if (threadsRunning) {
            try {
                this.syncObj.syncWait(UCAST_RESOLUTION_TIMEOUT);
            }
            catch (InterruptedException e2) {
                TraceLog.get().error("Internal error: ", e2, 10, 0);
            }
            e2 = this;
            synchronized (e2) {
                this.numOfThreadsH2I = 0;
            }
        }
        if (this.ips.size() == 0) {
            e2 = this;
            synchronized (e2) {
                threadsRunning = false;
                this.numOfThreadsH2I = 0;
                en = methods.elements();
                while (en.hasMoreElements()) {
                    aMethod = (ResolverMethod)en.nextElement();
                    if (!aMethod.multicast) continue;
                    ++this.numOfThreadsH2I;
                    threadsRunning = true;
                    new Thread((Runnable)new HostToIpRunner(aMethod, host, ip), "Resolver.multi.hostToIp").start();
                }
            }
            if (threadsRunning) {
                try {
                    this.syncObj.syncWait(MCAST_RESOLUTION_TIMEOUT);
                }
                catch (InterruptedException e3) {
                    TraceLog.get().error("Internal error: ", e3, 10, 0);
                }
                e2 = this;
                synchronized (e2) {
                    this.numOfThreadsH2I = 0;
                }
            }
            addrs = this.ipsToArray();
            if (TraceLog.get().canLog(300)) {
                TraceLog.get().message("ips = " + Resolver.displayAddrs(addrs), 300);
            }
            TraceLog.get().exit(300);
            return addrs;
        }
        addrs = this.ipsToArray();
        if (TraceLog.get().canLog(300)) {
            TraceLog.get().message("ips = " + Resolver.displayAddrs(addrs), 300);
        }
        TraceLog.get().exit(300);
        return addrs;
    }

    private static String displayAddrs(InetAddress[] addrs) {
        if (null == addrs) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addrs.length; ++i) {
            sb.append(addrs[i].getHostAddress());
            if (addrs.length <= i + 1) continue;
            sb.append(", ");
        }
        return sb.toString();
    }

    private InetAddress[] ipsToArray() {
        if (this.ips.size() > 0) {
            LinkedHashSet<InetAddress> list = new LinkedHashSet<InetAddress>();
            for (int i = 0; i < this.ips.size(); ++i) {
                list.add((InetAddress)this.ips.get(i));
            }
            return list.toArray(new InetAddress[list.size()]);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getDCNameByDomain(String domainName) {
        Resolver e2;
        ResolverMethod aMethod;
        TraceLog.get().enter("domainName = " + domainName, 300);
        this.dcName = null;
        boolean threadsRunning = false;
        Enumeration en = methods.elements();
        Resolver resolver = this;
        synchronized (resolver) {
            this.numOfThreadsDC = 0;
            while (en.hasMoreElements()) {
                aMethod = (ResolverMethod)en.nextElement();
                if (aMethod.multicast) continue;
                ++this.numOfThreadsDC;
                threadsRunning = true;
                new Thread((Runnable)new GetDCRunner(aMethod, domainName), "Resolver.single.getDCNameByDomain").start();
            }
        }
        if (threadsRunning) {
            try {
                this.syncObj.syncWait(UCAST_DC_RESOLUTION_TIMEOUT);
            }
            catch (InterruptedException e2) {
                TraceLog.get().error("Internal error: ", e2, 10, 0);
            }
            e2 = this;
            synchronized (e2) {
                this.numOfThreadsDC = 0;
            }
        }
        if (null != this.dcName) {
            TraceLog.get().exit("dcName = " + this.dcName, 300);
            return this.dcName;
        }
        e2 = this;
        synchronized (e2) {
            threadsRunning = false;
            this.numOfThreadsDC = 0;
            en = methods.elements();
            while (en.hasMoreElements()) {
                aMethod = (ResolverMethod)en.nextElement();
                if (!aMethod.multicast) continue;
                ++this.numOfThreadsDC;
                threadsRunning = true;
                new Thread((Runnable)new GetDCRunner(aMethod, domainName), "Resolver.multi.getDCNameByDomain").start();
            }
        }
        if (threadsRunning) {
            try {
                this.syncObj.syncWait(MCAST_DC_RESOLUTION_TIMEOUT);
            }
            catch (InterruptedException e3) {
                TraceLog.get().error("Internal error: ", e3, 10, 0);
            }
            resolver = this;
            synchronized (resolver) {
                this.numOfThreadsDC = 0;
            }
        }
        TraceLog.get().exit("dcName = " + this.dcName, 300);
        return this.dcName;
    }

    static {
        boolean isInternalOnly = (Boolean)Config.jnq.getNE("ISUSEINTERNALONLY");
        if (!isInternalOnly) {
            Resolver.registerMethod(new ExternalMethod(false));
            Resolver.registerMethod(new ExternalMethod(true));
        }
        MCAST_RESOLUTION_TIMEOUT = 2000L;
        UCAST_RESOLUTION_TIMEOUT = 2000L;
        MCAST_DC_RESOLUTION_TIMEOUT = 2500L;
        UCAST_DC_RESOLUTION_TIMEOUT = 2000L;
    }

    private class GetDCRunner
    implements Runnable {
        private ResolverMethod method;
        private String domain;

        protected GetDCRunner(ResolverMethod method, String domain) {
            this.method = method;
            this.domain = domain;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            String temp = null;
            try {
                temp = this.method.getDCNameByDomain(this.domain);
            }
            catch (NqException ex) {
                TraceLog.get().error("Internal error: ", ex, 10, 0);
            }
            boolean notify = false;
            Resolver resolver = Resolver.this;
            synchronized (resolver) {
                if (Resolver.this.numOfThreadsDC > 0 && (0 == --Resolver.this.numOfThreadsDC || null != temp)) {
                    if (null == Resolver.this.dcName) {
                        Resolver.this.dcName = temp;
                    }
                    notify = true;
                }
            }
            if (notify) {
                Resolver.this.syncObj.syncNotify();
            }
        }
    }

    private class HostToIpRunner
    implements Runnable {
        private ResolverMethod method;
        private String host;
        private InetAddress ip;

        protected HostToIpRunner(ResolverMethod method, String host, InetAddress ip) {
            this.method = method;
            this.host = host;
            this.ip = ip;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            InetAddress[] newIps;
            block17: {
                TraceLog.get().enter("host = " + this.host + ", ip = ", this.ip, 300);
                newIps = null;
                try {
                    newIps = this.method.hostToIp(this.host);
                    if (TraceLog.get().canLog(1000)) {
                        TraceLog.get().message("newIps = " + Resolver.displayAddrs(newIps), 1000);
                    }
                }
                catch (NqException ex) {
                    if (Resolver.this.ips.size() != 0) break block17;
                    TraceLog.get().error(this.method.getClass() + ": " + (this.method.multicast ? "Multicast" : "Unicast") + " " + this.method.printParams(), ex.getErrCode());
                }
            }
            boolean notify = false;
            if (null != newIps) {
                boolean isIpContains = false;
                for (int j = 0; null != this.ip && j < newIps.length; ++j) {
                    if (!Arrays.equals(newIps[j].getAddress(), this.ip.getAddress())) continue;
                    isIpContains = true;
                    if (1 < newIps.length) {
                        ArrayList<InetAddress> tmpList = new ArrayList<InetAddress>(Arrays.asList(newIps));
                        tmpList.remove(j);
                        newIps = tmpList.toArray(new InetAddress[tmpList.size()]);
                        break;
                    }
                    newIps = null;
                    break;
                }
                Resolver resolver = Resolver.this;
                synchronized (resolver) {
                    if (Resolver.this.numOfThreadsH2I > 0 && (0 == --Resolver.this.numOfThreadsH2I || isIpContains || null == this.ip) && !Resolver.this.syncObj.isNotifySent()) {
                        if (null != this.ip) {
                            Resolver.this.ips.addFirst(this.ip);
                        }
                        if (null != newIps) {
                            Resolver.this.ips.addAll(Arrays.asList(newIps));
                        }
                        notify = true;
                    }
                }
            }
            Resolver resolver = Resolver.this;
            synchronized (resolver) {
                if (Resolver.this.numOfThreadsH2I > 0 && 0 == --Resolver.this.numOfThreadsH2I) {
                    notify = true;
                }
            }
            if (notify) {
                Resolver.this.syncObj.syncNotify();
            }
            TraceLog.get().exit(300);
        }
    }

    private class IpToHostRunner
    implements Runnable {
        private ResolverMethod method;
        private InetAddress ip;

        protected IpToHostRunner(ResolverMethod method, InetAddress ip) {
            this.method = method;
            this.ip = ip;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            String temp = null;
            try {
                temp = this.method.ipToHost(this.ip);
            }
            catch (NqException e) {
                TraceLog.get().error("Internal error: ", e, 10, 0);
            }
            boolean notify = false;
            Resolver resolver = Resolver.this;
            synchronized (resolver) {
                if (0 < Resolver.this.numOfThreadsI2H && (0 == --Resolver.this.numOfThreadsI2H || null != temp)) {
                    if (null == Resolver.this.host) {
                        Resolver.this.host = temp;
                    }
                    notify = true;
                }
            }
            if (notify) {
                Resolver.this.syncObj.syncNotify();
            }
        }
    }

    private static class ExternalMethod
    extends ResolverMethod {
        public ExternalMethod(boolean isMulticast) {
            super(isMulticast);
        }

        public String ipToHost(InetAddress ip) {
            String res = ip.getCanonicalHostName();
            return IpAddressHelper.isIpAddress(res) ? null : res;
        }

        public InetAddress[] hostToIp(String host) throws NqException {
            TraceLog.get().enter("host = ", host, 300);
            InetAddress[] addr = new InetAddress[1];
            try {
                addr[0] = InetAddress.getByName(host);
            }
            catch (UnknownHostException e) {
                TraceLog.get().exit("Caught exception = ", e, 2000);
                return null;
            }
            catch (Exception ex) {
                TraceLog.get().exit("Caught exception = ", ex, 2000);
                return null;
            }
            if (TraceLog.get().canLog(300)) {
                TraceLog.get().message("addr = " + Resolver.displayAddrs(addr), 300);
            }
            TraceLog.get().exit(300);
            return addr;
        }

        public String getDCNameByDomain(String domain) throws NqException {
            TraceLog.get().enter("domain = " + domain, 300);
            InetAddress[] addr = new InetAddress[1];
            String res = null;
            addr = this.hostToIp(domain);
            if (null != addr) {
                InetAddress tmpIp = null;
                try {
                    tmpIp = InetAddress.getByAddress(addr[0].getAddress());
                }
                catch (UnknownHostException e) {
                    TraceLog.get().exit("Caught exception = ", e, 2000);
                    return null;
                }
                res = this.ipToHost(tmpIp);
                if (null != res && domain.toLowerCase().equals(res.toLowerCase())) {
                    TraceLog.get().exit("return null", 2000);
                    return null;
                }
            }
            TraceLog.get().exit("res = " + res, 2000);
            return res;
        }

        public long getTimeout() {
            return 2000L;
        }

        public String printParams() {
            return "external";
        }
    }
}

