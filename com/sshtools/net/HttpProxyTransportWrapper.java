/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import com.maverick.ssh.SshTransport;
import com.sshtools.net.HttpHeader;
import com.sshtools.net.HttpRequest;
import com.sshtools.net.HttpResponse;
import com.sshtools.net.SocketWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class HttpProxyTransportWrapper
extends SocketWrapper {
    private String proxyHost;
    private int proxyPort;
    private String remoteHost;
    private int remotePort;
    private HttpResponse responseHeader;
    private String username;
    private String password;
    private String userAgent;
    private static int connectionTimeout = 30000;

    private HttpProxyTransportWrapper(String host, int port, String proxyHost, int proxyPort) throws IOException, UnknownHostException {
        super(new Socket());
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.remoteHost = host;
        this.remotePort = port;
        this.socket.connect(new InetSocketAddress(proxyHost, proxyPort), connectionTimeout);
        this.socket.setSoTimeout(connectionTimeout);
    }

    public static void setConnectionTimeout(int connectionTimeout) {
        HttpProxyTransportWrapper.connectionTimeout = connectionTimeout;
    }

    public static int getConnectionTimeout() {
        return connectionTimeout;
    }

    public static HttpProxyTransportWrapper connectViaProxy(String host, int port, String proxyHost, int proxyPort, String username, String password, String userAgent) throws IOException, UnknownHostException {
        int status;
        HttpProxyTransportWrapper socket = new HttpProxyTransportWrapper(host, port, proxyHost, proxyPort);
        socket.username = username;
        socket.password = password;
        socket.userAgent = userAgent;
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            HttpRequest request = new HttpRequest();
            request.setHeaderBegin("CONNECT " + host + ":" + port + " HTTP/1.0");
            request.setHeaderField("User-Agent", userAgent);
            request.setHeaderField("Pragma", "No-Cache");
            request.setHeaderField("Host", host);
            request.setHeaderField("Proxy-Connection", "Keep-Alive");
            if (username != null && !"".equals(username.trim()) && password != null && !"".equals(password.trim())) {
                request.setBasicAuthentication(username, password);
            }
            out.write(request.toString().getBytes());
            out.flush();
            socket.responseHeader = new HttpResponse(in);
            if (socket.responseHeader.getStatus() == 407) {
                String realm = socket.responseHeader.getAuthenticationRealm();
                String method = socket.responseHeader.getAuthenticationMethod();
                if (realm == null) {
                    realm = "";
                }
                if (method.equalsIgnoreCase("basic")) {
                    socket.close();
                    socket = new HttpProxyTransportWrapper(host, port, proxyHost, proxyPort);
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                    request.setBasicAuthentication(username, password);
                    out.write(request.toString().getBytes());
                    out.flush();
                    socket.responseHeader = new HttpResponse(in);
                } else {
                    if (method.equalsIgnoreCase("digest")) {
                        throw new IOException("Digest authentication is not supported");
                    }
                    throw new IOException("'" + method + "' is not supported");
                }
            }
            status = socket.responseHeader.getStatus();
        }
        catch (SocketException e) {
            throw new SocketException("Error communicating with proxy server " + proxyHost + ":" + proxyPort + " (" + e.getMessage() + ")");
        }
        if (status < 200 || status > 299) {
            throw new IOException("Proxy tunnel setup failed: " + socket.responseHeader.getStartLine());
        }
        socket.setSoTimeout(0);
        return socket;
    }

    public String toString() {
        return "HTTPProxySocket [Proxy IP=" + this.socket.getInetAddress() + ",Proxy Port=" + this.getPort() + ",localport=" + this.socket.getLocalPort() + "Remote Host=" + this.remoteHost + "Remote Port=" + String.valueOf(this.remotePort) + "]";
    }

    HttpHeader getResponseHeader() {
        return this.responseHeader;
    }

    @Override
    public String getHost() {
        return this.remoteHost;
    }

    @Override
    public SshTransport duplicate() throws IOException {
        return HttpProxyTransportWrapper.connectViaProxy(this.remoteHost, this.remotePort, this.proxyHost, this.proxyPort, this.username, this.password, this.userAgent);
    }
}

