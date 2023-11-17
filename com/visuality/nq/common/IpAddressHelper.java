/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class IpAddressHelper {
    public static InetAddress loopbackAddr = IpAddressHelper.getLoopbackAddress();
    public static int MAX_PORT = 65353;
    private static Pattern IPV4_PATTERN = null;
    private static Pattern IPV6_PATTERN = null;
    private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    private static final String ipv6Pattern = "([0-9a-f]{0,4}:){4,7}([0-9a-f]){0,4}";

    public static InetAddress getBroadcastByIp(InetAddress ip) throws NqException {
        if (!(ip instanceof Inet4Address)) {
            throw new NqException("Broadcast unavaible for: " + ip.toString());
        }
        byte[] data = ip.getAddress();
        if ((0xFF & data[0]) > 0 && (0xFF & data[0]) < 224) {
            data[3] = -1;
            if ((0xFF & data[0]) < 192) {
                data[2] = -1;
            }
            if ((0xFF & data[0]) < 128) {
                data[1] = -1;
            }
        }
        try {
            return InetAddress.getByAddress(data);
        }
        catch (UnknownHostException e) {
            throw new NqException(e.getMessage(), -14);
        }
    }

    public static InetAddress getLoopbackAddress() {
        block8: {
            try {
                if (!Utility.isClassSupport("java.net.NetworkInterface")) {
                    InetAddress[] ips;
                    try {
                        ips = InetAddress.getAllByName(Utility.getHostName());
                    }
                    catch (UnknownHostException e) {
                        return null;
                    }
                    for (int i = 0; i < ips.length; ++i) {
                        if (!(ips[i] instanceof Inet4Address) || !ips[i].isLoopbackAddress()) continue;
                        return ips[i];
                    }
                    break block8;
                }
                Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                while (ifaces.hasMoreElements()) {
                    NetworkInterface iface = ifaces.nextElement();
                    Enumeration<InetAddress> inetAddrs = iface.getInetAddresses();
                    while (inetAddrs.hasMoreElements()) {
                        InetAddress inetAddr = inetAddrs.nextElement();
                        if (!inetAddr.isLoopbackAddress() || !(inetAddr instanceof Inet4Address)) continue;
                        return inetAddr;
                    }
                }
            }
            catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static InetAddress getLocalHostIp() {
        try {
            if (!Utility.isClassSupport("java.net.NetworkInterface")) {
                return InetAddress.getLocalHost();
            }
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> inetAddrs = iface.getInetAddresses();
                while (inetAddrs.hasMoreElements()) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (inetAddr.isLoopbackAddress() || inetAddr.isLinkLocalAddress() || inetAddr.isMulticastAddress() || !(inetAddr instanceof Inet4Address)) continue;
                    return inetAddr;
                }
            }
        }
        catch (Exception e) {
            return null;
        }
        return null;
    }

    public static InetAddress[] getAllBroadcasts() throws NqException {
        Enumeration<NetworkInterface> en;
        if (!Utility.isClassSupport("java.net.NetworkInterface") || !Utility.isClassSupport("java.net.InterfaceAddress")) {
            InetAddress[] ips;
            try {
                ips = InetAddress.getAllByName(Utility.getHostName());
            }
            catch (UnknownHostException e) {
                throw new NqException(e.getMessage(), -12);
            }
            ArrayList<InetAddress> addresses = new ArrayList<InetAddress>();
            for (int i = 0; i < ips.length; ++i) {
                if (!(ips[i] instanceof Inet4Address) || ips[i].isLoopbackAddress()) continue;
                addresses.add(IpAddressHelper.getBroadcastByIp(ips[i]));
            }
            return addresses.toArray(new Inet4Address[addresses.size()]);
        }
        try {
            en = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException e) {
            throw new NqException(e.getMessage(), -23);
        }
        Vector<InetAddress> vect = new Vector<InetAddress>();
        while (en.hasMoreElements()) {
            try {
                NetworkInterface iface = en.nextElement();
                Method getInterfaceAddressesMethod = iface.getClass().getMethod("getInterfaceAddresses", new Class[0]);
                List list = (List)getInterfaceAddressesMethod.invoke(iface, new Object[0]);
                Iterator it = list.iterator();
                Class interfaceAddressClass = Utility.isClassForName("java.net.InterfaceAddress");
                Method getAddressMethod = interfaceAddressClass.getMethod("getAddress", new Class[0]);
                Method getBroadcastMethod = interfaceAddressClass.getMethod("getBroadcast", new Class[0]);
                while (it.hasNext()) {
                    InetAddress bcast;
                    Object addr = interfaceAddressClass.cast(it.next());
                    InetAddress ip = (InetAddress)getAddressMethod.invoke(addr, new Object[0]);
                    if (ip.isLoopbackAddress() || null == (bcast = (InetAddress)getBroadcastMethod.invoke(addr, new Object[0]))) continue;
                    vect.add(bcast);
                }
            }
            catch (Exception e) {
                TraceLog.get().error("reflection error : ", e);
            }
        }
        InetAddress[] results = new InetAddress[vect.size()];
        for (int i = 0; i < vect.size(); ++i) {
            results[i] = (InetAddress)vect.get(i);
        }
        return results;
    }

    public static Inet4Address[] getAllInet4Ips() throws NqException {
        InetAddress[] ips = IpAddressHelper.getAllInetIps();
        boolean isAndroid = Utility.isAndroid();
        ArrayList<Inet4Address> addresses = new ArrayList<Inet4Address>();
        for (int i = 0; i < ips.length; ++i) {
            if (!(ips[i] instanceof Inet4Address) || ips[i].isLoopbackAddress() && !isAndroid) continue;
            addresses.add((Inet4Address)ips[i]);
        }
        return addresses.toArray(new Inet4Address[addresses.size()]);
    }

    public static Inet6Address[] getAllInet6Ips() throws NqException {
        InetAddress[] ips = IpAddressHelper.getAllInetIps();
        int count = 0;
        for (int i = 0; i < ips.length; ++i) {
            if (!(ips[i] instanceof Inet6Address) || ips[i].isLoopbackAddress()) continue;
            ++count;
        }
        Inet6Address[] results = new Inet6Address[count];
        count = 0;
        for (int i = 0; i < ips.length; ++i) {
            if (!(ips[i] instanceof Inet6Address) || ips[i].isLoopbackAddress()) continue;
            results[count] = (Inet6Address)ips[i];
            ++count;
        }
        return results;
    }

    public static InetAddress[] getAllInetIps() throws NqException {
        try {
            if (Utility.isAndroid()) {
                InetAddress[] address = new InetAddress[]{InetAddress.getByAddress(Utility.getHostName(), InetAddress.getLocalHost().getAddress())};
                return address;
            }
            return InetAddress.getAllByName(Utility.getHostName());
        }
        catch (UnknownHostException e) {
            try {
                return InetAddress.getAllByName("localhost");
            }
            catch (UnknownHostException e1) {
                throw new NqException(e.getMessage(), -12);
            }
        }
    }

    public static boolean isIpAddress(String name) {
        Matcher m1 = IPV4_PATTERN.matcher(name);
        if (m1.matches()) {
            return true;
        }
        Matcher m2 = IPV6_PATTERN.matcher(name);
        if (m2.matches()) {
            return true;
        }
        return IpAddressHelper.isIpv6Literal(name);
    }

    private static boolean isIpv6Literal(String name) {
        return name.endsWith(".ipv6-literal.net");
    }

    public static InetAddress stringToIp(String name) {
        InetAddress ip = null;
        if (IpAddressHelper.isIpv6Literal(name)) {
            ip = IpAddressHelper.ipv6LiteralToIp(name);
        } else {
            try {
                ip = InetAddress.getByName(name);
            }
            catch (UnknownHostException unknownHostException) {
                // empty catch block
            }
        }
        return ip;
    }

    private static InetAddress ipv6LiteralToIp(String name) {
        String ipStr = "";
        int i = 0;
        while (name.charAt(i) != '.') {
            char c = name.charAt(i);
            switch (c) {
                case '-': {
                    ipStr = ipStr + ':';
                    break;
                }
                case 'S': 
                case 's': {
                    ipStr = ipStr + '%';
                    break;
                }
                default: {
                    ipStr = ipStr + c;
                }
            }
            ++i;
        }
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName(ipStr);
        }
        catch (UnknownHostException unknownHostException) {
            // empty catch block
        }
        return ip;
    }

    public static InetAddress determineLocalIpv6Address() {
        try {
            InetAddress localInetAddress = InetAddress.getLocalHost();
            String hostName = null != localInetAddress ? localInetAddress.getHostName() : "localhost";
            InetAddress[] addresses = InetAddress.getAllByName(hostName);
            if (null != addresses) {
                for (InetAddress adr : addresses) {
                    if (!(adr instanceof Inet6Address)) continue;
                    return adr;
                }
            }
        }
        catch (UnknownHostException e) {
            TraceLog.get().error("Unable to determine any Ipv6 address for this host.");
        }
        return null;
    }

    static {
        try {
            IPV4_PATTERN = Pattern.compile(ipv4Pattern, 2);
            IPV6_PATTERN = Pattern.compile(ipv6Pattern, 2);
        }
        catch (PatternSyntaxException patternSyntaxException) {
            // empty catch block
        }
    }
}

