/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.Vector;

public class DMZQueueSocket
extends Socket {
    Socket sock = null;
    InputStream in2 = null;
    OutputStream out2 = null;
    public static Vector data_socks_active = new Vector();
    public static Vector data_socks_available = new Vector();
    String sock_id = Common.makeBoundary(8);

    public DMZQueueSocket(Socket sock) throws IOException {
        this.sock = sock;
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        this.sock.bind(bindpoint);
    }

    @Override
    public synchronized void close() throws IOException {
        this.close2();
    }

    public synchronized void close2() throws IOException {
        System.out.println(String.valueOf(System.currentTimeMillis()) + ":" + 0 + ":SOCK_CLOSE_START:");
        this.sock_id = Common.makeBoundary(8);
        this.in2 = null;
        this.out2 = null;
        if (Common.dmz_mode) {
            Vector data_sock_available = (Vector)Common.System2.get("crushftp.dmz.data_sock_available");
            Properties p = new Properties();
            p.put("time", String.valueOf(System.currentTimeMillis()));
            p.put("sock", this);
            data_sock_available.insertElementAt(p, 0);
        }
        System.out.println(String.valueOf(System.currentTimeMillis()) + ":" + 0 + ":SOCK_CLOSE_END:");
    }

    public synchronized void disconnect() throws IOException {
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
        if (this.in2 == null) {
            class CheckedInputStream
            extends InputStream {
                InputStream in3 = null;
                String sock_id_ref;

                public CheckedInputStream(InputStream in3) {
                    this.sock_id_ref = DMZQueueSocket.this.sock_id;
                    this.in3 = in3;
                }

                @Override
                public int read() throws IOException {
                    if (!DMZQueueSocket.this.sock_id.equals(this.sock_id_ref)) {
                        return -1;
                    }
                    return this.in3.read();
                }

                @Override
                public int read(byte[] b) throws IOException {
                    if (!DMZQueueSocket.this.sock_id.equals(this.sock_id_ref)) {
                        return -1;
                    }
                    return this.in3.read(b);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    if (!DMZQueueSocket.this.sock_id.equals(this.sock_id_ref)) {
                        return -1;
                    }
                    return this.in3.read(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    System.out.println(String.valueOf(System.currentTimeMillis()) + ":" + -1 + ":READ_CLOSE:");
                    if (DMZQueueSocket.this.sock_id.equals(this.sock_id_ref)) {
                        DMZQueueSocket.this.close2();
                    }
                }
            }
            this.in2 = new CheckedInputStream(this.sock.getInputStream());
        }
        return this.in2;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (this.out2 == null) {
            class CheckedOutputStream
            extends OutputStream {
                OutputStream out3 = null;
                String sock_id_ref;

                public CheckedOutputStream(OutputStream out3) {
                    this.sock_id_ref = DMZQueueSocket.this.sock_id;
                    this.out3 = out3;
                }

                @Override
                public void write(int i) throws IOException {
                    if (!DMZQueueSocket.this.sock_id.equals(this.sock_id_ref)) {
                        throw new IOException("1:Socket reference no longer valid, attempted write reuse after closing socket!" + DMZQueueSocket.this.sock);
                    }
                    this.out3.write(i);
                    this.out3.flush();
                }

                @Override
                public void write(byte[] b) throws IOException {
                    if (!DMZQueueSocket.this.sock_id.equals(this.sock_id_ref)) {
                        throw new IOException("2:Socket reference no longer valid, attempted write reuse after closing socket!" + DMZQueueSocket.this.sock);
                    }
                    this.out3.write(b);
                    this.out3.flush();
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    if (!DMZQueueSocket.this.sock_id.equals(this.sock_id_ref)) {
                        throw new IOException("3:Socket reference no longer valid, attempted write reuse after closing socket!" + DMZQueueSocket.this.sock);
                    }
                    this.out3.write(b, off, len);
                    this.out3.flush();
                }

                @Override
                public void close() throws IOException {
                    System.out.println(String.valueOf(System.currentTimeMillis()) + ":" + 0 + ":WRITE_CLOSE:");
                    if (DMZQueueSocket.this.sock_id.equals(this.sock_id_ref)) {
                        DMZQueueSocket.this.close2();
                    }
                }
            }
            this.out2 = new CheckedOutputStream(this.sock.getOutputStream());
        }
        return this.out2;
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

