/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.TraceLog;
import com.visuality.nq.resolve.Matches;
import com.visuality.nq.resolve.ProbeThread;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class WsdProbe {
    Collection<Matches> matches = new CopyOnWriteArraySet<Matches>();
    public static String PROBE_TYPE = "wsdp:Device";
    public static String PROBE_MESSAGE = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:pub=\"http://schemas.microsoft.com/windows/pub/2005/07\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" xmlns:wsd=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\" xmlns:wsdp=\"http://schemas.xmlsoap.org/ws/2006/02/devprof\"><soap:Header><wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action><wsa:MessageID>urn:uuid:UUID</wsa:MessageID><wsa:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To></soap:Header><soap:Body><wsd:Probe><wsd:Types>" + PROBE_TYPE + "</wsd:Types></wsd:Probe></soap:Body></soap:Envelope>";
    public static final String IPv4_MULTICAST_ADDRESS = "239.255.255.250";
    public static final String IPv6_MULTICAST_ADDRESS = "[FF02::C]";
    public static final int WS_DISCOVERY_PORT = 3702;
    public static final int PORT_BASE = 20000;
    public static final int PORT_OFFSET = 40000;
    private String uuid;

    public Collection<Matches> probe(int milliseconds) {
        TraceLog.get().enter("WS-Discovery starting with timeout of " + milliseconds, 700);
        int ws_timeout = milliseconds;
        ExecutorService executorService = Executors.newCachedThreadPool();
        Collection<InetAddress> inetAddresses = this.findAllInterfaces();
        boolean isFirstTimeProbeMessageToBeDisplayed = true;
        this.uuid = UUID.randomUUID().toString();
        TraceLog.get().message("UUID for PROBE XML strings = " + this.uuid, 700);
        for (InetAddress address : inetAddresses) {
            byte[] probeXML = this.createProbeMessage();
            if (isFirstTimeProbeMessageToBeDisplayed) {
                isFirstTimeProbeMessageToBeDisplayed = false;
                TraceLog.get().message("Probe Message sent -> ", probeXML, 2000);
            }
            int port = new SecureRandom().nextInt(20000) + 40000;
            DatagramSocket server = null;
            try {
                server = new DatagramSocket(port, address);
            }
            catch (SocketException e1) {
                if (TraceLog.get().canLog(700)) {
                    TraceLog.get().message("Cannot create DatagramSocket: " + e1.getMessage() + "for address = " + address + ", port = " + port, 700);
                }
                port = new SecureRandom().nextInt(20000) + 40000;
                try {
                    server = new DatagramSocket(port, address);
                }
                catch (SocketException e) {
                    TraceLog.get().message("Cannot create DatagramSocket: for address = " + address + ", port = " + port + ", ", e, 700);
                    continue;
                }
            }
            ProbeThread runner = new ProbeThread(server, this.matches, ws_timeout, this.uuid);
            executorService.execute(runner);
            try {
                if (address instanceof Inet4Address) {
                    server.send(new DatagramPacket(probeXML, probeXML.length, InetAddress.getByName(IPv4_MULTICAST_ADDRESS), 3702));
                    continue;
                }
                server.send(new DatagramPacket(probeXML, probeXML.length, InetAddress.getByName(IPv6_MULTICAST_ADDRESS), 3702));
            }
            catch (UnknownHostException uhe) {
                TraceLog.get().error("For address = " + address + ", port = " + port + ", unknown host exception: ", uhe, 700, 0);
            }
            catch (IOException ioe) {
                TraceLog.get().error("For address = " + address + ", port = " + port + ", IO exception: ", ioe, 700, 0);
            }
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(ws_timeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ignored) {
            // empty catch block
        }
        TraceLog.get().exit("WS-Discovery completed, number of items found = " + this.matches.size(), 700);
        return this.matches;
    }

    private final byte[] createProbeMessage() {
        String probe = PROBE_MESSAGE.replaceAll("<wsa:MessageID>urn:uuid:UUID</wsa:MessageID>", "<wsa:MessageID>urn:uuid:" + this.uuid + "</wsa:MessageID>");
        byte[] bytes = probe.getBytes();
        return bytes;
    }

    private Collection<InetAddress> findAllInterfaces() {
        ArrayList<InetAddress> inetAddressList = new ArrayList<InetAddress>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface anInterface = interfaces.nextElement();
                    Enumeration<InetAddress> addrs = anInterface.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress addr = addrs.nextElement();
                        inetAddressList.add(addr);
                    }
                }
            }
        }
        catch (SocketException se) {
            se.printStackTrace();
        }
        return inetAddressList;
    }

    public Collection<Matches> getMatches() {
        return this.matches;
    }
}

