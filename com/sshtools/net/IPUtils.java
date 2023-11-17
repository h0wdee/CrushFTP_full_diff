/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import java.util.StringTokenizer;

public abstract class IPUtils {
    private IPUtils() {
    }

    public static int[] calcNetworkNumber(int[] ip, int[] mask) {
        int[] ret = new int[4];
        for (int i = 0; i < 4; ++i) {
            ret[i] = ip[i] & mask[i];
        }
        return ret;
    }

    public static int[] calcLastAddress(int[] in, int mask) {
        int[] ret = new int[4];
        ret = IPUtils.calcBroadcastAddress(in, mask);
        ret[3] = ret[3] - 1;
        return ret;
    }

    public static int[] createMaskArray(int bit) {
        int i;
        int[] mask = new int[4];
        int rem = (bit + 1) / 8;
        int mod = (bit + 1) % 8;
        Integer Int = new Integer(2);
        Integer modInt = new Integer(8 - mod);
        double d = Math.pow(Int.doubleValue(), modInt.doubleValue());
        Double dd = new Double(d);
        for (i = 0; i < rem; ++i) {
            mask[i] = 255;
        }
        if (i < mask.length - 1) {
            mask[i] = 256 - dd.intValue();
            ++i;
            while (i < 4) {
                mask[i] = 0;
                ++i;
            }
        }
        return mask;
    }

    public static int[] calcFirstAddress(int[] ip, int[] mask) {
        int[] ret = new int[4];
        ret = IPUtils.calcNetworkNumber(ip, mask);
        ret[3] = ret[3] + 1;
        return ret;
    }

    public static int[] calcBroadcastAddress(int[] in, int m) {
        int[] ret = new int[4];
        Integer totalBits = new Integer(32);
        Integer bits = new Integer(totalBits - m - 1);
        int[] mask = IPUtils.createMaskArray(m);
        double two = 2.0;
        Double hosts = new Double(Math.pow(two, bits.doubleValue()));
        hosts.intValue();
        int ffOctets = bits / 8;
        Integer modBits = new Integer(bits % 8);
        for (int i = 0; i < 4; ++i) {
            ret[i] = in[i];
            if (i <= 4 - ffOctets - 1) continue;
            ret[i] = 255;
        }
        hosts = new Double(Math.pow(two, modBits.doubleValue()));
        if (ffOctets > 0) {
            ret[4 - ffOctets - 1] = in[4 - ffOctets - 1] + hosts.intValue() - 1;
        } else {
            ret[3] = mask[3] + hosts.intValue() - 1;
        }
        return ret;
    }

    public static int getNumberOfHosts(int[] ip, int m) {
        Integer totalBits = new Integer(32);
        Integer bits = new Integer(totalBits - m - 1);
        double two = 2.0;
        Double hosts = new Double(Math.pow(two, bits.doubleValue()));
        return hosts.intValue() - 2;
    }

    public static String createAddressString(int[] addr) {
        return addr[0] + "." + addr[1] + "." + addr[2] + "." + addr[3];
    }

    public static int[] nextAddress(int[] ip) {
        if (ip[3] == 255) {
            ip[3] = 0;
            if (ip[2] == 255) {
                ip[2] = 0;
                if (ip[1] == 255) {
                    ip[1] = 0;
                    if (ip[0] == 255) {
                        return null;
                    }
                    ip[0] = ip[0] + 1;
                } else {
                    ip[1] = ip[1] + 1;
                }
            } else {
                ip[2] = ip[2] + 1;
            }
        } else {
            ip[3] = ip[3] + 1;
        }
        return ip;
    }

    public static int[] getByteAddress(String ipAddress) {
        StringTokenizer tokens = new StringTokenizer(ipAddress, ".");
        int[] ip = new int[4];
        for (int i = 0; i < ip.length; ++i) {
            if (!tokens.hasMoreTokens()) {
                throw new IllegalArgumentException("IP address must consist of xxx.xxx.xxx.xxx");
            }
            try {
                ip[i] = Integer.parseInt(tokens.nextToken().trim());
                continue;
            }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid IP address " + ipAddress);
            }
        }
        return ip;
    }
}

