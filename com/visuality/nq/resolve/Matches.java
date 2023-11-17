/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import java.util.Arrays;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Matches
implements Comparable<Matches> {
    private String ip;
    private boolean isIpv6;
    private WsType[] types;

    public Matches(String ip, boolean isIpv6, WsType[] types) {
        this.ip = ip;
        this.isIpv6 = isIpv6;
        this.types = types;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isIpv6() {
        return this.isIpv6;
    }

    public void setIpv6(boolean isIpv6) {
        this.isIpv6 = isIpv6;
    }

    public WsType[] getTypes() {
        return this.types;
    }

    public void setTypes(WsType[] types) {
        this.types = types;
    }

    public String toString() {
        return "Matches [ip=" + this.ip + ", isIpv6=" + this.isIpv6 + ", types=" + Arrays.toString((Object[])this.types) + "]";
    }

    @Override
    public int compareTo(Matches other) {
        return this.ip.compareTo(other.getIp());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Matches other = (Matches)obj;
        return !(this.ip == null ? other.ip != null : !this.ip.equals(other.ip));
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.ip == null ? 0 : this.ip.hashCode());
        return result;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum WsType {
        Computer,
        Device,
        PrintDeviceType,
        ScanDeviceType,
        Other;

    }
}

