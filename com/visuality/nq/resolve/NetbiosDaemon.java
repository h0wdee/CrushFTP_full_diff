/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Resolver;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import com.visuality.nq.resolve.DnsMessage;
import com.visuality.nq.resolve.DnsResolver;
import com.visuality.nq.resolve.ExternalDatagramService;
import com.visuality.nq.resolve.ExternalLlmnrService;
import com.visuality.nq.resolve.ExternalNameService;
import com.visuality.nq.resolve.ExternalSessionService;
import com.visuality.nq.resolve.InternalDatagramService;
import com.visuality.nq.resolve.InternalNameService;
import com.visuality.nq.resolve.NameCache;
import com.visuality.nq.resolve.NameMessage;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosName;
import com.visuality.nq.resolve.NetbiosResolver;
import java.net.InetAddress;
import java.net.SocketException;

public final class NetbiosDaemon {
    private static volatile NetbiosDaemon theDaemon = null;
    private static volatile boolean isTheDaemonStopped = false;
    private static final Object theDaemonObj = new Object();
    static InetAddress[] broadcasts;
    static NameCache internalNames;
    static NameCache externalNames;
    public InternalNameService internalName = null;
    public InternalDatagramService internalDatagram = null;
    public ExternalNameService externalName;
    public ExternalDatagramService externalDatagrame;
    public ExternalSessionService externalSession = null;
    public ExternalLlmnrService externalLlmnr;
    public static final int DYNAMICPORT = 0;
    public static final int EXTERNALNAMESERVICEPORT = 137;
    public static final int EXTERNALDATAGRAMSERVICEPORT = 138;
    public static final int EXTERNALSESSIONSERVICEPORT = 139;
    public static final int EXTERNALLLMNRPORT = 5355;
    static boolean doRegistration;
    private static int datagramTimeout;

    private NetbiosDaemon() throws NqException, SocketException {
        TraceLog.get().enter(300);
        boolean isExternalOnly = Config.jnq.getBool("ISUSEEXTERNALONLY");
        boolean isInternalOnly = Config.jnq.getBool("ISUSEINTERNALONLY");
        if (isExternalOnly && isInternalOnly) {
            TraceLog.get().error("ISUSEEXTERNALONLY and ISUSEEXTERNALONLY are set to TRUE");
            TraceLog.get().exit("ISUSEEXTERNALONLY and ISUSEEXTERNALONLY are set to TRUE", 300);
            throw new NqException("ISUSEEXTERNALONLY and ISUSEEXTERNALONLY are set to TRUE", -20);
        }
        this.internalName = this.getInternalName(isExternalOnly);
        this.internalDatagram = this.getInternalDatagram(isExternalOnly);
        boolean isBindWellKnown = Config.jnq.getBool("BINDWELLKNOWNPORTS");
        boolean isSessionServiceDisabled = Config.jnq.getBool("DISABLESESSIONSERVICE");
        int port = isBindWellKnown ? 137 : 0;
        this.externalName = this.getExternalName(port);
        doRegistration = Config.jnq.getBool("REGISTERHOST");
        port = isBindWellKnown ? 138 : 0;
        this.externalDatagrame = this.getExternalDatagram(port);
        if (!isSessionServiceDisabled) {
            port = isBindWellKnown ? 139 : 0;
            this.externalSession = this.getExternalSession(port);
        }
        port = isBindWellKnown ? 5355 : 0;
        this.externalLlmnr = this.getExternalLlmnr(port);
        if (!isExternalOnly) {
            Resolver.registerMethod(new NetbiosResolver(false));
            Resolver.registerMethod(new NetbiosResolver(true));
        }
        InetAddress[] ips = Config.getDns();
        if (!isExternalOnly && null != ips) {
            for (int i = 0; i < ips.length; ++i) {
                Resolver.registerMethod(new DnsResolver(false, ips[i]));
            }
        }
        TraceLog.get().exit(300);
    }

    private InternalNameService getInternalName(boolean isExternalOnly) throws NqException, SocketException {
        InternalNameService internalNameService;
        block4: {
            internalNameService = null;
            try {
                internalNameService = new InternalNameService(isExternalOnly ? 0 : Config.jnq.getInt("INTERNALNAMESERVICEPORT"), IpAddressHelper.loopbackAddr);
            }
            catch (SocketException e) {
                if (null == internalNameService) {
                    internalNameService = new InternalNameService(0, IpAddressHelper.loopbackAddr);
                }
            }
            catch (IllegalArgumentException e) {
                if (null != internalNameService) break block4;
                internalNameService = new InternalNameService(0, IpAddressHelper.loopbackAddr);
            }
        }
        return internalNameService;
    }

    private InternalDatagramService getInternalDatagram(boolean isExternalOnly) throws SocketException, NqException {
        InternalDatagramService internalDatagram;
        block4: {
            internalDatagram = null;
            try {
                internalDatagram = new InternalDatagramService(isExternalOnly ? 0 : Config.jnq.getInt("INTERNALDATAGRAMSERVICEPORT"), IpAddressHelper.loopbackAddr);
            }
            catch (SocketException e) {
                if (null == internalDatagram) {
                    internalDatagram = new InternalDatagramService(0, IpAddressHelper.loopbackAddr);
                }
            }
            catch (IllegalArgumentException e) {
                if (null != internalDatagram) break block4;
                internalDatagram = new InternalDatagramService(0, IpAddressHelper.loopbackAddr);
            }
        }
        return internalDatagram;
    }

    private ExternalNameService getExternalName(int port) throws SocketException {
        ExternalNameService externalName = null;
        try {
            externalName = new ExternalNameService(port);
        }
        catch (SocketException ex) {
            TraceLog.get().error("Unable to start name service: ", ex);
            externalName = new ExternalNameService();
        }
        catch (IllegalArgumentException ex) {
            TraceLog.get().error("Unable to start name service: ", ex);
            externalName = new ExternalNameService();
        }
        return externalName;
    }

    private ExternalDatagramService getExternalDatagram(int port) throws SocketException {
        ExternalDatagramService externalDatagrame = null;
        try {
            externalDatagrame = new ExternalDatagramService(port);
        }
        catch (SocketException ex) {
            TraceLog.get().error("Unable to start datagram service: ", ex);
            externalDatagrame = new ExternalDatagramService();
        }
        catch (IllegalArgumentException ex) {
            TraceLog.get().error("Unable to start datagram service: ", ex);
            externalDatagrame = new ExternalDatagramService();
        }
        return externalDatagrame;
    }

    private ExternalSessionService getExternalSession(int port) throws SocketException {
        ExternalSessionService externalSession = null;
        try {
            externalSession = new ExternalSessionService(port);
        }
        catch (SocketException ex) {
            TraceLog.get().error("Unable to start session service: ", ex);
            externalSession = new ExternalSessionService();
        }
        catch (IllegalArgumentException ex) {
            TraceLog.get().error("Unable to start session service: ", ex);
            externalSession = new ExternalSessionService();
        }
        return externalSession;
    }

    private ExternalLlmnrService getExternalLlmnr(int port) throws SocketException {
        ExternalLlmnrService externalLlmnr = null;
        try {
            externalLlmnr = new ExternalLlmnrService(port);
        }
        catch (SocketException ex) {
            TraceLog.get().error("Unable to start LLMNR responder: ", ex);
            externalLlmnr = new ExternalLlmnrService();
        }
        catch (IllegalArgumentException ex) {
            TraceLog.get().error("Unable to start LLMNR responder: ", ex);
            externalLlmnr = new ExternalLlmnrService();
        }
        return externalLlmnr;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void start() {
        TraceLog.get().enter(300);
        Object object = theDaemonObj;
        synchronized (object) {
            if (null == theDaemon) {
                try {
                    NetbiosDaemon.detectHostBcasts();
                    theDaemon = new NetbiosDaemon();
                    isTheDaemonStopped = false;
                    if (doRegistration) {
                        try {
                            int role = Config.jnq.getBool("ISSERVER") ? 32 : 0;
                            new NameMessage(new NetbiosName(Utility.getHostName(), role)).register();
                            InetAddress[] ips = Config.getDns();
                            if (null != ips) {
                                for (int i = 0; i < ips.length; ++i) {
                                    DnsMessage msg = null;
                                    try {
                                        msg = new DnsMessage(ips[i]);
                                        msg.registerHost();
                                        continue;
                                    }
                                    catch (Exception e) {
                                        continue;
                                    }
                                    finally {
                                        if (null != msg) {
                                            msg.terminate();
                                        }
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                            TraceLog.get().error("Unable to create NameMessage or DnsMessage: ", e);
                        }
                    }
                    Runtime.getRuntime().addShutdownHook(new Thread(new DaemonStopper()));
                    Thread.sleep(200L);
                }
                catch (Exception ex) {
                    TraceLog.get().error("Unable to start NetBIOS Daemon: ", ex);
                }
            }
        }
        TraceLog.get().exit(300);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void stop() {
        InetAddress[] ips = Config.getDns();
        if (doRegistration) {
            if (null != ips) {
                for (int i = 0; i < ips.length; ++i) {
                    DnsMessage msg = null;
                    try {
                        msg = new DnsMessage(ips[i]);
                        msg.unregisterHost();
                        continue;
                    }
                    catch (Exception exception) {
                        continue;
                    }
                    finally {
                        if (null != msg) {
                            msg.terminate();
                        }
                    }
                }
            }
            try {
                int role = Config.jnq.getBool("ISSERVER") ? 32 : 0;
                new NameMessage(new NetbiosName(Utility.getHostName(), role)).release();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        Object object = theDaemonObj;
        synchronized (object) {
            if (null == theDaemon) {
                return;
            }
            if (null != NetbiosDaemon.theDaemon.internalName) {
                NetbiosDaemon.theDaemon.internalName.stop();
            }
            if (null != NetbiosDaemon.theDaemon.internalDatagram) {
                NetbiosDaemon.theDaemon.internalDatagram.stop();
            }
            if (null != NetbiosDaemon.theDaemon.externalName) {
                NetbiosDaemon.theDaemon.externalName.stop();
            }
            if (null != NetbiosDaemon.theDaemon.externalDatagrame) {
                NetbiosDaemon.theDaemon.externalDatagrame.stop();
            }
            if (null != NetbiosDaemon.theDaemon.externalSession) {
                NetbiosDaemon.theDaemon.externalSession.stop();
            }
            if (null != NetbiosDaemon.theDaemon.externalLlmnr) {
                NetbiosDaemon.theDaemon.externalLlmnr.stop();
            }
            theDaemon = null;
            isTheDaemonStopped = true;
        }
    }

    public static InetAddress[] getHostBcasts() {
        return broadcasts;
    }

    private static void detectHostBcasts() throws NetbiosException {
        try {
            broadcasts = IpAddressHelper.getAllBroadcasts();
        }
        catch (Exception e) {
            TraceLog.get().error("Failed to detect self IPs: ", e);
            throw new NetbiosException("Failed to detect self IPs: " + e.getMessage(), -503);
        }
    }

    public static int getDatagramTimeout() {
        return datagramTimeout;
    }

    public static void setDatagramTimeout(int datagramTimeout) {
        NetbiosDaemon.datagramTimeout = datagramTimeout;
    }

    public static NetbiosDaemon getTheDaemon() throws NetbiosException {
        if (isTheDaemonStopped) {
            throw new NetbiosException("The daemon has already stopped.", -503);
        }
        if (null == theDaemon) {
            NetbiosDaemon.start();
        }
        return theDaemon;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setTheDaemon(NetbiosDaemon theDaemon) {
        Object object = theDaemonObj;
        synchronized (object) {
            NetbiosDaemon.theDaemon = theDaemon;
        }
    }

    static {
        internalNames = new NameCache();
        externalNames = new NameCache();
        doRegistration = false;
        datagramTimeout = 1500;
    }

    private static class DaemonStopper
    implements Runnable {
        private DaemonStopper() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            Object object = theDaemonObj;
            synchronized (object) {
                if (null != theDaemon) {
                    NetbiosDaemon.stop();
                }
            }
        }
    }
}

