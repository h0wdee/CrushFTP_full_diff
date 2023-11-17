/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import com.sshtools.net.IPUtils;
import java.io.EOFException;

public class CIDRNetwork {
    private final String network;
    private int networkBits;
    private String networkAddress;
    private String subnetMask;
    private String broadcastAddress;
    private int[] net;
    private int[] subnet;
    private String lastIP;

    public CIDRNetwork(String network) throws IllegalArgumentException {
        int index = network.indexOf("/");
        if (index == -1) {
            index = network.indexOf("\\");
        }
        if (index == -1) {
            network = network + "/32";
            index = network.indexOf("/");
        }
        try {
            this.networkBits = Integer.parseInt(network.substring(index + 1)) - 1;
        }
        catch (NumberFormatException ex) {
            throw new IllegalArgumentException("CIDR network setting invalid! " + network);
        }
        this.network = network;
        this.subnet = IPUtils.createMaskArray(this.networkBits);
        this.net = IPUtils.getByteAddress(network.substring(0, index));
        this.net = IPUtils.calcNetworkNumber(this.net, this.subnet);
        this.networkAddress = IPUtils.createAddressString(this.net);
        this.broadcastAddress = IPUtils.createAddressString(IPUtils.calcBroadcastAddress(this.net, this.networkBits));
    }

    public CIDRNetwork(String ipAddress, String subnetMask) {
        this.subnet = IPUtils.getByteAddress(subnetMask);
        int[] ip = IPUtils.getByteAddress(ipAddress);
        this.net = IPUtils.calcNetworkNumber(ip, this.subnet);
        this.networkAddress = IPUtils.createAddressString(this.net);
        subnetMask = IPUtils.createAddressString(this.subnet);
        if (subnetMask.equals("255.0.0.0")) {
            this.networkBits = 7;
        } else if (subnetMask.equals("255.128.0.0")) {
            this.networkBits = 8;
        } else if (subnetMask.equals("255.192.0.0")) {
            this.networkBits = 9;
        } else if (subnetMask.equals("255.224.0.0")) {
            this.networkBits = 10;
        } else if (subnetMask.equals("255.240.0.0")) {
            this.networkBits = 11;
        } else if (subnetMask.equals("255.248.0.0")) {
            this.networkBits = 12;
        } else if (subnetMask.equals("255.252.0.0")) {
            this.networkBits = 13;
        } else if (subnetMask.equals("255.254.0.0")) {
            this.networkBits = 14;
        } else if (subnetMask.equals("255.255.0.0")) {
            this.networkBits = 15;
        } else if (subnetMask.equals("255.255.128.0")) {
            this.networkBits = 16;
        } else if (subnetMask.equals("255.255.192.0")) {
            this.networkBits = 17;
        } else if (subnetMask.equals("255.255.224.0")) {
            this.networkBits = 18;
        } else if (subnetMask.equals("255.255.240.0")) {
            this.networkBits = 19;
        } else if (subnetMask.equals("255.255.248.0")) {
            this.networkBits = 20;
        } else if (subnetMask.equals("255.255.252.0")) {
            this.networkBits = 21;
        } else if (subnetMask.equals("255.255.254.0")) {
            this.networkBits = 22;
        } else if (subnetMask.equals("255.255.255.0")) {
            this.networkBits = 23;
        } else if (subnetMask.equals("255.255.255.128")) {
            this.networkBits = 24;
        } else if (subnetMask.equals("255.255.255.192")) {
            this.networkBits = 25;
        } else if (subnetMask.equals("255.255.255.224")) {
            this.networkBits = 26;
        } else if (subnetMask.equals("255.255.255.240")) {
            this.networkBits = 27;
        } else if (subnetMask.equals("255.255.255.248")) {
            this.networkBits = 28;
        } else if (subnetMask.equals("255.255.255.252")) {
            this.networkBits = 29;
        }
        this.network = this.networkAddress + "/" + this.getNetworkBits();
        this.broadcastAddress = IPUtils.createAddressString(IPUtils.calcBroadcastAddress(this.net, this.networkBits));
    }

    public String getNextIPAddress(String startAddress, String endAddress) throws EOFException {
        if (this.lastIP == null && (startAddress == null || "".equals(startAddress))) {
            this.lastIP = IPUtils.createAddressString(IPUtils.calcFirstAddress(this.net, this.subnet));
        } else if (this.lastIP == null) {
            this.lastIP = startAddress;
        } else {
            String addressString;
            if (endAddress == null || "".equals(endAddress) ? this.lastIP.equals(addressString = IPUtils.createAddressString(IPUtils.calcLastAddress(this.net, this.networkBits))) : this.lastIP.equals(endAddress)) {
                throw new EOFException("No more IPs available");
            }
            this.lastIP = IPUtils.createAddressString(IPUtils.nextAddress(IPUtils.getByteAddress(this.lastIP)));
        }
        return this.lastIP;
    }

    public String getNetworkAddress() {
        return this.networkAddress;
    }

    public int getNetworkBits() {
        return this.networkBits + 1;
    }

    public String getSubnetMask() {
        return this.subnetMask;
    }

    public String getBroadcastAddress() {
        return this.broadcastAddress;
    }

    public String getCIDRString() {
        return this.network;
    }

    public String toString() {
        return this.getCIDRString();
    }

    public boolean isValidDHCPRange(String startAddress, String endAddress) {
        if (!this.isValidAddressForNetwork(startAddress)) {
            return false;
        }
        if (!this.isValidAddressForNetwork(endAddress)) {
            return false;
        }
        int[] addressOneBytes = IPUtils.getByteAddress(startAddress);
        int[] addressTwoBytes = IPUtils.getByteAddress(endAddress);
        boolean valid = false;
        for (int index = 0; index < addressOneBytes.length; ++index) {
            if ((addressOneBytes[index] ^ this.subnet[index]) >= (addressTwoBytes[index] ^ this.subnet[index])) continue;
            valid = true;
            break;
        }
        return valid;
    }

    public boolean isValidAddressForNetwork(String address) {
        try {
            int subnetValue;
            if (address.equals(this.networkAddress)) {
                return this.networkBits > 30;
            }
            int[] bytes = IPUtils.getByteAddress(address);
            boolean valid = true;
            for (int index = 0; index < bytes.length && (subnetValue = this.subnet[index]) != 0; ++index) {
                if ((bytes[index] & subnetValue) == this.net[index]) continue;
                valid = false;
                break;
            }
            return valid;
        }
        catch (IllegalArgumentException iae) {
            return false;
        }
    }
}

