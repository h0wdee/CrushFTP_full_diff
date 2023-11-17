/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import com.maverick.ssh.SshTransport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SocksProxyTransport
extends Socket
implements SshTransport {
    public static final int SOCKS4 = 4;
    public static final int SOCKS5 = 5;
    private static final int CONNECT = 1;
    private static final int NULL_TERMINATION = 0;
    private static final String[] SOCKSV5_ERROR = new String[]{"Success", "General SOCKS server failure", "Connection not allowed by ruleset", "Network unreachable", "Host unreachable", "Connection refused", "TTL expired", "Command not supported", "Address type not supported"};
    private static final String[] SOCKSV4_ERROR = new String[]{"Request rejected or failed", "SOCKS server cannot connect to identd on the client", "The client program and identd report different user-ids"};
    private String proxyHost;
    private int proxyPort;
    private String remoteHost;
    private int remotePort;
    private int socksVersion;
    private String username;
    private String password;
    private boolean localLookup;
    private String providerDetail;
    private static int connectionTimeout = 30000;

    private SocksProxyTransport(String remoteHost, int remotePort, String proxyHost, int proxyPort, int socksVersion) throws IOException, UnknownHostException {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.socksVersion = socksVersion;
        this.connect(new InetSocketAddress(proxyHost, proxyPort), connectionTimeout);
        this.setSoTimeout(connectionTimeout);
    }

    public static void setConnectionTimeout(int connectionTimeout) {
        SocksProxyTransport.connectionTimeout = connectionTimeout;
    }

    public static int getConnectionTimeout() {
        return connectionTimeout;
    }

    public static SocksProxyTransport connectViaSocks4Proxy(String remoteHost, int remotePort, String proxyHost, int proxyPort, String userId) throws IOException, UnknownHostException {
        SocksProxyTransport proxySocket = new SocksProxyTransport(remoteHost, remotePort, proxyHost, proxyPort, 4);
        proxySocket.username = userId;
        try {
            InputStream proxyIn = proxySocket.getInputStream();
            OutputStream proxyOut = proxySocket.getOutputStream();
            InetAddress hostAddr = InetAddress.getByName(remoteHost);
            proxyOut.write(4);
            proxyOut.write(1);
            proxyOut.write(remotePort >>> 8 & 0xFF);
            proxyOut.write(remotePort & 0xFF);
            proxyOut.write(hostAddr.getAddress());
            proxyOut.write(userId.getBytes());
            proxyOut.write(0);
            proxyOut.flush();
            int res = proxyIn.read();
            if (res == -1) {
                throw new IOException("SOCKS4 server " + proxyHost + ":" + proxyPort + " disconnected");
            }
            if (res != 0) {
                throw new IOException("Invalid response from SOCKS4 server (" + res + ") " + proxyHost + ":" + proxyPort);
            }
            int code = proxyIn.read();
            if (code != 90) {
                if (code > 90 && code < 93) {
                    throw new IOException("SOCKS4 server unable to connect, reason: " + SOCKSV4_ERROR[code - 91]);
                }
                throw new IOException("SOCKS4 server unable to connect, reason: " + code);
            }
            byte[] data = new byte[6];
            if (proxyIn.read(data, 0, 6) != 6) {
                throw new IOException("SOCKS4 error reading destination address/port");
            }
            proxySocket.setProviderDetail(data[2] + "." + data[3] + "." + data[4] + "." + data[5] + ":" + (data[0] << 8 | data[1]));
        }
        catch (SocketException e) {
            throw new SocketException("Error communicating with SOCKS4 server " + proxyHost + ":" + proxyPort + ", " + e.getMessage());
        }
        proxySocket.setSoTimeout(0);
        return proxySocket;
    }

    private void setProviderDetail(String providerDetail) {
        this.providerDetail = providerDetail;
    }

    public static SocksProxyTransport connectViaSocks5Proxy(String remoteHost, int remotePort, String proxyHost, int proxyPort, boolean localLookup, String username, String password) throws IOException, UnknownHostException {
        SocksProxyTransport proxySocket = new SocksProxyTransport(remoteHost, remotePort, proxyHost, proxyPort, 5);
        proxySocket.username = username;
        proxySocket.password = password;
        proxySocket.localLookup = localLookup;
        try {
            InputStream proxyIn = proxySocket.getInputStream();
            OutputStream proxyOut = proxySocket.getOutputStream();
            byte[] request = new byte[]{5, 2, 0, 2};
            proxyOut.write(request);
            proxyOut.flush();
            int res = proxyIn.read();
            if (res == -1) {
                throw new IOException("SOCKS5 server " + proxyHost + ":" + proxyPort + " disconnected");
            }
            if (res != 5) {
                throw new IOException("Invalid response from SOCKS5 server (" + res + ") " + proxyHost + ":" + proxyPort);
            }
            int method = proxyIn.read();
            switch (method) {
                case 0: {
                    break;
                }
                case 2: {
                    SocksProxyTransport.performAuthentication(proxyIn, proxyOut, username, password, proxyHost, proxyPort);
                    break;
                }
                default: {
                    throw new IOException("SOCKS5 server does not support our authentication methods");
                }
            }
            if (localLookup) {
                InetAddress hostAddr;
                try {
                    hostAddr = InetAddress.getByName(remoteHost);
                }
                catch (UnknownHostException e) {
                    throw new IOException("Can't do local lookup on: " + remoteHost + ", try socks5 without local lookup");
                }
                request = new byte[]{5, 1, 0, 1};
                proxyOut.write(request);
                proxyOut.write(hostAddr.getAddress());
            } else {
                request = new byte[]{5, 1, 0, 3};
                proxyOut.write(request);
                proxyOut.write(remoteHost.length());
                proxyOut.write(remoteHost.getBytes());
            }
            proxyOut.write(remotePort >>> 8 & 0xFF);
            proxyOut.write(remotePort & 0xFF);
            proxyOut.flush();
            res = proxyIn.read();
            if (res != 5) {
                throw new IOException("Invalid response from SOCKS5 server (" + res + ") " + proxyHost + ":" + proxyPort);
            }
            int status = proxyIn.read();
            if (status != 0) {
                if (status > 0 && status < 9) {
                    throw new IOException("SOCKS5 server unable to connect, reason: " + SOCKSV5_ERROR[status]);
                }
                throw new IOException("SOCKS5 server unable to connect, reason: " + status);
            }
            proxyIn.read();
            int aType = proxyIn.read();
            byte[] data = new byte[255];
            switch (aType) {
                case 1: {
                    if (proxyIn.read(data, 0, 4) != 4) {
                        throw new IOException("SOCKS5 error reading address");
                    }
                    proxySocket.setProviderDetail(data[0] + "." + data[1] + "." + data[2] + "." + data[3]);
                    break;
                }
                case 3: {
                    int n = proxyIn.read();
                    if (proxyIn.read(data, 0, n) != n) {
                        throw new IOException("SOCKS5 error reading address");
                    }
                    proxySocket.setProviderDetail(new String(data));
                    break;
                }
                default: {
                    throw new IOException("SOCKS5 gave unsupported address type: " + aType);
                }
            }
            if (proxyIn.read(data, 0, 2) != 2) {
                throw new IOException("SOCKS5 error reading port");
            }
            proxySocket.setProviderDetail(proxySocket.getProviderDetail() + ":" + (data[0] << 8 | data[1]));
        }
        catch (SocketException e) {
            throw new SocketException("Error communicating with SOCKS5 server " + proxyHost + ":" + proxyPort + ", " + e.getMessage());
        }
        return proxySocket;
    }

    private String getProviderDetail() {
        return this.providerDetail;
    }

    private static void performAuthentication(InputStream proxyIn, OutputStream proxyOut, String username, String password, String proxyHost, int proxyPort) throws IOException {
        proxyOut.write(1);
        proxyOut.write(username.length());
        proxyOut.write(username.getBytes());
        proxyOut.write(password.length());
        proxyOut.write(password.getBytes());
        int res = proxyIn.read();
        if (res != 1 && res != 5) {
            throw new IOException("Invalid response from SOCKS5 server (" + res + ") " + proxyHost + ":" + proxyPort);
        }
        if (proxyIn.read() != 0) {
            throw new IOException("Invalid username/password for SOCKS5 server");
        }
    }

    @Override
    public String toString() {
        return "SocksProxySocket[addr=" + this.getInetAddress() + ",port=" + this.getPort() + ",localport=" + this.getLocalPort() + "]";
    }

    public static SocksProxyTransport connectViaSocks5Proxy(String remoteHost, int remotePort, String proxyHost, int proxyPort, String username, String password) throws IOException, UnknownHostException {
        return SocksProxyTransport.connectViaSocks5Proxy(remoteHost, remotePort, proxyHost, proxyPort, false, username, password);
    }

    @Override
    public String getHost() {
        return this.remoteHost;
    }

    @Override
    public SshTransport duplicate() throws IOException {
        switch (this.socksVersion) {
            case 4: {
                return SocksProxyTransport.connectViaSocks4Proxy(this.remoteHost, this.remotePort, this.proxyHost, this.proxyPort, this.username);
            }
        }
        return SocksProxyTransport.connectViaSocks5Proxy(this.remoteHost, this.remotePort, this.proxyHost, this.proxyPort, this.localLookup, this.username, this.password);
    }
}

