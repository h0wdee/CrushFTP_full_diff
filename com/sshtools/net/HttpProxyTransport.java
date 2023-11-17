/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.net;

import com.maverick.ssh.SshTransport;
import com.sshtools.net.HttpHeader;
import com.sshtools.net.HttpRequest;
import com.sshtools.net.HttpResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpProxyTransport
extends Socket
implements SshTransport {
    static Logger log = LoggerFactory.getLogger(HttpProxyTransport.class);
    private String proxyHost;
    private int proxyPort;
    private String remoteHost;
    private int remotePort;
    private HttpResponse responseHeader;
    private String username;
    private String password;
    private String userAgent;
    private HttpRequest request = new HttpRequest();
    private Hashtable<String, String> optionalHeaders;
    private static int connectionTimeout = 30000;

    private HttpProxyTransport(String host, int port, String proxyHost, int proxyPort) throws IOException, UnknownHostException {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.remoteHost = host;
        this.remotePort = port;
        this.connect(new InetSocketAddress(proxyHost, proxyPort), connectionTimeout);
        this.setSoTimeout(connectionTimeout);
    }

    public static void setConnectionTimeout(int connectionTimeout) {
        HttpProxyTransport.connectionTimeout = connectionTimeout;
    }

    public static int getConnectionTimeout() {
        return connectionTimeout;
    }

    public static HttpProxyTransport connectViaProxy(String host, int port, String proxyHost, int proxyPort, String username, String password, String userAgent) throws IOException, UnknownHostException {
        return HttpProxyTransport.connectViaProxy(host, port, proxyHost, proxyPort, username, password, userAgent, null);
    }

    public static HttpProxyTransport connectViaProxy(String host, int port, String proxyHost, int proxyPort, String username, String password, String userAgent, Hashtable<String, String> optionalHeaders) throws IOException, UnknownHostException {
        int status;
        HttpProxyTransport socket = new HttpProxyTransport(host, port, proxyHost, proxyPort);
        socket.username = username;
        socket.password = password;
        socket.userAgent = userAgent;
        socket.optionalHeaders = optionalHeaders;
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            socket.request.setHeaderBegin("CONNECT " + host + ":" + port + " HTTP/1.0");
            socket.request.setHeaderField("User-Agent", userAgent);
            socket.request.setHeaderField("Pragma", "No-Cache");
            socket.request.setHeaderField("Host", host);
            socket.request.setHeaderField("Proxy-Connection", "Keep-Alive");
            if (optionalHeaders != null) {
                Enumeration<String> e = optionalHeaders.keys();
                while (e.hasMoreElements()) {
                    String h = e.nextElement();
                    socket.request.setHeaderField(h, optionalHeaders.get(h));
                }
            }
            if (username != null && !"".equals(username.trim()) && password != null && !"".equals(password.trim())) {
                socket.request.setBasicAuthentication(username, password);
            }
            if (log.isDebugEnabled()) {
                log.debug("Sending HTTP Proxy Request...");
                log.debug(socket.request.toString());
            }
            out.write(socket.request.toString().getBytes());
            out.flush();
            socket.responseHeader = new HttpResponse(in);
            if (log.isDebugEnabled()) {
                log.debug("Received {} status from HTTP Proxy", (Object)socket.responseHeader.getStatus());
            }
            if (socket.responseHeader.getStatus() == 407) {
                String realm = socket.responseHeader.getAuthenticationRealm();
                String method = socket.responseHeader.getAuthenticationMethod();
                if (realm == null) {
                    realm = "";
                }
                if (method.equalsIgnoreCase("basic")) {
                    socket.close();
                    socket = new HttpProxyTransport(host, port, proxyHost, proxyPort);
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                    socket.request.setHeaderBegin("CONNECT " + host + ":" + port + " HTTP/1.0");
                    socket.request.setHeaderField("User-Agent", userAgent);
                    socket.request.setHeaderField("Pragma", "No-Cache");
                    socket.request.setHeaderField("Host", host);
                    socket.request.setHeaderField("Proxy-Connection", "Keep-Alive");
                    socket.request.setBasicAuthentication(username, password);
                    if (log.isDebugEnabled()) {
                        log.debug("Sending HTTP Proxy Request...");
                        log.debug(socket.request.toString());
                    }
                    out.write(socket.request.toString().getBytes());
                    out.flush();
                    socket.responseHeader = new HttpResponse(in);
                } else {
                    if (method.equalsIgnoreCase("digest")) {
                        throw new IOException("Digest authentication is not supported");
                    }
                    throw new IOException("'" + method + "' is not supported");
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Received {} status from HTTP Proxy", (Object)socket.responseHeader.getStatus());
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

    @Override
    public String toString() {
        return "HTTPProxySocket [Proxy IP=" + this.getInetAddress() + ",Proxy Port=" + this.getPort() + ",localport=" + this.getLocalPort() + "Remote Host=" + this.remoteHost + "Remote Port=" + String.valueOf(this.remotePort) + "]";
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
        return HttpProxyTransport.connectViaProxy(this.remoteHost, this.remotePort, this.proxyHost, this.proxyPort, this.username, this.password, this.userAgent, this.optionalHeaders);
    }
}

