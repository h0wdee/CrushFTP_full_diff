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

public class DMZQueueSocket_CheckedInputStream
extends Socket {
    Socket sock = null;
    InputStream in2 = null;
    OutputStream out2 = null;
    public static Vector active_data_socks = new Vector();
    String sock_id = Common.makeBoundary(8);

    public DMZQueueSocket_CheckedInputStream(Socket sock) throws IOException {
        this.sock = sock;
        active_data_socks.addElement(sock);
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        this.sock.bind(bindpoint);
    }

    @Override
    public synchronized void close() throws IOException {
        this.close2(true);
    }

    public synchronized void close2(boolean write_0) throws IOException {
        System.out.println(String.valueOf(System.currentTimeMillis()) + ":" + 0 + ":SOCK_CLOSE_START:" + write_0);
        try {
            if (write_0) {
                this.getOutputStream().write(new byte[0]);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
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
        System.out.println(String.valueOf(System.currentTimeMillis()) + ":" + 0 + ":SOCK_CLOSE_END:" + write_0);
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
                int buf_pos;
                byte[] buf;
                StringBuffer sb;

                public CheckedInputStream(InputStream in3) {
                    this.sock_id_ref = DMZQueueSocket_CheckedInputStream.this.sock_id;
                    this.buf_pos = -1;
                    this.buf = null;
                    this.sb = new StringBuffer(7);
                    this.in3 = in3;
                }

                @Override
                public int read() throws IOException {
                    throw new IOException("Cannot read single byte in CheckedInputStream!");
                }

                @Override
                public int read(byte[] b) throws IOException {
                    return this.read(b, 0, b.length);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    int got2;
                    if (this.buf_pos >= 0) {
                        return this.read_buff(b, off, len);
                    }
                    if (!DMZQueueSocket_CheckedInputStream.this.sock_id.equals(this.sock_id_ref)) {
                        return -1;
                    }
                    this.sb.setLength(0);
                    int x = 0;
                    while (x < 6) {
                        this.sb.append(new String(new byte[]{(byte)this.in3.read()}, "UTF8"));
                        ++x;
                    }
                    int avail = Integer.parseInt(this.sb.toString().trim());
                    if (avail == 0) {
                        System.out.println(String.valueOf(System.currentTimeMillis()) + ":" + avail + ":Closed");
                        DMZQueueSocket_CheckedInputStream.this.close2(true);
                        return -1;
                    }
                    byte[] b_temp = new byte[avail];
                    for (int got = 0; got < avail; got += got2) {
                        got2 = this.in3.read(b_temp, got, avail - got);
                        if (got2 > 0) {
                            continue;
                        }
                        System.out.println(String.valueOf(System.currentTimeMillis()) + ":" + avail + ":CheckedInputStream closed on read2! " + got + " < " + avail);
                        throw new IOException("CheckedInputStream closed on read2! " + got + " < " + avail);
                    }
                    if (b.length - off < avail) {
                        this.buf = b_temp;
                        this.buf_pos = 0;
                        return this.read_buff(b, off, len);
                    }
                    System.arraycopy(b_temp, 0, b, off, avail);
                    return avail;
                }

                public int read_buff(byte[] b, int off, int len) throws IOException {
                    if (b.length - off < this.buf.length - this.buf_pos) {
                        System.arraycopy(this.buf, this.buf_pos, b, off, b.length - off);
                        this.buf_pos += b.length - off;
                        if (this.buf.length == this.buf_pos) {
                            this.buf_pos = -1;
                        }
                        int avail = b.length - off;
                        return avail;
                    }
                    System.arraycopy(this.buf, this.buf_pos, b, off, this.buf.length - this.buf_pos);
                    int avail = this.buf.length - this.buf_pos;
                    this.buf_pos = -1;
                    return avail;
                }

                @Override
                public void close() throws IOException {
                    System.out.println(String.valueOf(System.currentTimeMillis()) + ":" + -1 + ":READ_CLOSE:");
                    DMZQueueSocket_CheckedInputStream.this.close2(false);
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
                    this.sock_id_ref = DMZQueueSocket_CheckedInputStream.this.sock_id;
                    this.out3 = out3;
                }

                @Override
                public void write(int i) throws IOException {
                    throw new IOException("Cannot write single byte in CheckedOutputStream!");
                }

                @Override
                public void write(byte[] b) throws IOException {
                    this.write(b, 0, b.length);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    if (!DMZQueueSocket_CheckedInputStream.this.sock_id.equals(this.sock_id_ref)) {
                        throw new IOException("Socket reference no longer valid, attempted write reuse after closing socket!" + DMZQueueSocket_CheckedInputStream.this.sock);
                    }
                    String len_str = (String.valueOf(len) + "      ").substring(0, 6);
                    this.out3.write(len_str.getBytes("UTF8"));
                    this.out3.write(b, off, len);
                    this.out3.flush();
                }

                @Override
                public void close() throws IOException {
                    DMZQueueSocket_CheckedInputStream.this.close2(true);
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

