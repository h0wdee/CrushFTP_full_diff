/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.VRL;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Vector;
import javax.net.ssl.SSLSocketFactory;

public class DMZSocket
extends Socket {
    Socket sock = null;

    public DMZSocket(VRL u, String dmz_item) throws IOException {
        this.sock = DMZSocket.getDmzHostPortSock(dmz_item);
        String host_port = String.valueOf(u.getHost()) + ":" + u.getPort();
        Common.sockLog(this.sock, "Got DMZSocket:" + dmz_item + ":" + host_port);
        this.sock.getOutputStream().write("E".getBytes());
        this.sock.getOutputStream().write(host_port.getBytes("UTF8").length);
        this.sock.getOutputStream().write(host_port.getBytes("UTF8"));
        this.sock.getOutputStream().flush();
    }

    public static Socket getDmzHostPortSock(String dmz_item) throws IOException {
        Vector dmzs = (Vector)Common.System2.get("crushftp.dmz.hosts");
        if (dmzs == null || dmzs.size() == 0) {
            throw new IOException("No DMZs available.");
        }
        dmzs = (Vector)dmzs.clone();
        String name_host_port = "";
        if (dmz_item.equalsIgnoreCase("true")) {
            name_host_port = dmzs.elementAt((int)(Math.random() * (double)dmzs.size() - 1.0)).toString();
        } else {
            int x = 0;
            while (x < dmzs.size()) {
                String dmz_name_host_port = dmzs.elementAt(x).toString();
                if (dmz_name_host_port.split(":")[0].trim().equalsIgnoreCase(dmz_item.trim())) {
                    name_host_port = dmz_name_host_port;
                    break;
                }
                ++x;
            }
        }
        if (name_host_port.equals("")) {
            throw new IOException("DMZ instance not found " + dmz_item + " in active list:" + dmzs);
        }
        Socket sock = null;
        if (System.getProperty("crushftp.dmz.ssl", "true").equals("true") && name_host_port.split(":")[3].equals("false")) {
            SSLSocketFactory factory = (SSLSocketFactory)Common.System2.get("crushftp.dmz.factory");
            sock = factory.createSocket(name_host_port.split(":")[1], Integer.parseInt(name_host_port.split(":")[2]));
            Common.sockLog(sock, "Got DMZHostPortSockSSL:" + dmz_item + ":" + name_host_port);
        } else {
            sock = name_host_port.split(":")[3].equals("true") ? new Socket("127.0.0.1", Integer.parseInt(name_host_port.split(":")[2])) : new Socket(name_host_port.split(":")[1], Integer.parseInt(name_host_port.split(":")[2]));
            Common.sockLog(sock, "Got DMZHostPortSockPlain:" + dmz_item + ":" + name_host_port);
        }
        return sock;
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        this.sock.bind(bindpoint);
    }

    @Override
    public synchronized void close() throws IOException {
        this.sock.close();
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        this.sock.connect(endpoint, timeout);
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        this.sock.connect(endpoint);
    }

    @Override
    public SocketChannel getChannel() {
        return this.sock.getChannel();
    }

    @Override
    public InetAddress getInetAddress() {
        return this.sock.getInetAddress();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.sock.getInputStream();
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return this.sock.getKeepAlive();
    }

    @Override
    public InetAddress getLocalAddress() {
        return this.sock.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return this.sock.getLocalPort();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return this.sock.getLocalSocketAddress();
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return this.sock.getOOBInline();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.sock.getOutputStream();
    }

    @Override
    public int getPort() {
        return this.sock.getPort();
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return this.sock.getReceiveBufferSize();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return this.sock.getRemoteSocketAddress();
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return this.sock.getReuseAddress();
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return this.sock.getSendBufferSize();
    }

    @Override
    public int getSoLinger() throws SocketException {
        return this.sock.getSoLinger();
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return this.sock.getSoTimeout();
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return this.sock.getTcpNoDelay();
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return this.sock.getTrafficClass();
    }

    @Override
    public boolean isBound() {
        return this.sock.isBound();
    }

    @Override
    public boolean isClosed() {
        return this.sock.isClosed();
    }

    @Override
    public boolean isConnected() {
        return this.sock.isConnected();
    }

    @Override
    public boolean isInputShutdown() {
        return this.sock.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return this.sock.isOutputShutdown();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        this.sock.sendUrgentData(data);
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        this.sock.setKeepAlive(on);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        this.sock.setOOBInline(on);
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        this.sock.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        this.sock.setReceiveBufferSize(size);
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        this.sock.setReuseAddress(on);
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        this.sock.setSendBufferSize(size);
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        this.sock.setSoLinger(on, linger);
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        this.sock.setSoTimeout(timeout);
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        this.sock.setTcpNoDelay(on);
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        this.sock.setTrafficClass(tc);
    }

    @Override
    public void shutdownInput() throws IOException {
        this.sock.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        this.sock.shutdownOutput();
    }

    @Override
    public String toString() {
        return this.sock.toString();
    }
}

