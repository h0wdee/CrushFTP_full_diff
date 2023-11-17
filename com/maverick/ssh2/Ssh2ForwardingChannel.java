/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.SshTunnel;
import com.maverick.ssh.message.SshChannelMessage;
import com.maverick.ssh2.Ssh2Channel;
import java.io.IOException;

class Ssh2ForwardingChannel
extends Ssh2Channel
implements SshTunnel {
    public static final String X11_FORWARDING_CHANNEL = "x11";
    public static final String LOCAL_FORWARDING_CHANNEL = "direct-tcpip";
    public static final String REMOTE_FORWARDING_CHANNEL = "forwarded-tcpip";
    protected static final String X11AUTH_PROTO = "MIT-MAGIC-COOKIE-1";
    SshTransport transport;
    String host;
    int port;
    String listeningAddress;
    int listeningPort;
    String originatingHost;
    int originatingPort;
    byte[] buf = new byte[1024];
    boolean hasSpoofedCookie = false;
    int idx = 0;
    int requiredLength = 12;
    int protocolLength;
    int cookieLength;

    public Ssh2ForwardingChannel(String name, int remotewindow, int remotepacket, String host, int port, String listeningAddress, int listeningPort, String originatingHost, int originatingPort, SshTransport transport) {
        super(name, remotewindow, remotepacket);
        this.transport = transport;
        this.host = host;
        this.port = port;
        this.listeningAddress = listeningAddress;
        this.listeningPort = listeningPort;
        this.originatingHost = originatingHost;
        this.originatingPort = originatingPort;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    public String getConnectedHost() {
        return this.getHost();
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getOriginatingHost() {
        return this.originatingHost;
    }

    @Override
    public int getOriginatingPort() {
        return this.originatingPort;
    }

    @Override
    public String getListeningAddress() {
        return this.listeningAddress;
    }

    @Override
    public int getListeningPort() {
        return this.listeningPort;
    }

    @Override
    public boolean isLocal() {
        return this.getName().equals(LOCAL_FORWARDING_CHANNEL);
    }

    @Override
    public boolean isX11() {
        return this.getName().equals(X11_FORWARDING_CHANNEL);
    }

    @Override
    public SshTransport getTransport() {
        return this.transport;
    }

    @Override
    public boolean isLocalEOF() {
        return this.isLocalEOF;
    }

    @Override
    public boolean isRemoteEOF() {
        return this.isRemoteEOF;
    }

    @Override
    public SshTransport duplicate() throws IOException {
        throw new SshIOException(new SshException("SSH tunnels cannot be duplicated!", 4));
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    protected void processStandardData(int len, SshChannelMessage msg) throws SshException {
        if (this.getName().equals(X11_FORWARDING_CHANNEL) && !this.hasSpoofedCookie) {
            int n;
            if (this.idx < 12) {
                n = this.readMore(msg);
                len -= n;
                if (this.requiredLength == 0) {
                    if (this.buf[0] == 66) {
                        this.protocolLength = (this.buf[6] & 0xFF) << 8 | this.buf[7] & 0xFF;
                        this.cookieLength = (this.buf[8] & 0xFF) << 8 | this.buf[9] & 0xFF;
                    } else if (this.buf[0] == 108) {
                        this.protocolLength = (this.buf[7] & 0xFF) << 8 | this.buf[6] & 0xFF;
                        this.cookieLength = (this.buf[9] & 0xFF) << 8 | this.buf[8] & 0xFF;
                    } else {
                        this.close();
                        throw new SshException("Corrupt X11 authentication packet", 6);
                    }
                    this.requiredLength = this.protocolLength + 3 & 0xFFFFFFFC;
                    this.requiredLength += this.cookieLength + 3 & 0xFFFFFFFC;
                    if (this.requiredLength + this.idx > this.buf.length) {
                        this.close();
                        throw new SshException("Corrupt X11 authentication packet", 6);
                    }
                    if (this.requiredLength == 0) {
                        this.close();
                        throw new SshException("X11 authentication cookie not found", 6);
                    }
                }
            }
            if (len > 0) {
                n = this.readMore(msg);
                len -= n;
                if (this.requiredLength == 0) {
                    byte[] fakeCookie = this.connection.getContext().getX11AuthenticationCookie();
                    String protoStr = new String(this.buf, 12, this.protocolLength);
                    byte[] recCookie = new byte[fakeCookie.length];
                    this.protocolLength = this.protocolLength + 3 & 0xFFFFFFFC;
                    System.arraycopy(this.buf, 12 + this.protocolLength, recCookie, 0, fakeCookie.length);
                    if (!X11AUTH_PROTO.equals(protoStr) || !this.compareCookies(fakeCookie, recCookie, fakeCookie.length)) {
                        this.close();
                        throw new SshException("Incorrect X11 cookie", 6);
                    }
                    byte[] realCookie = this.connection.getContext().getX11RealCookie();
                    if (realCookie.length != this.cookieLength) {
                        throw new SshException("Invalid X11 cookie", 6);
                    }
                    System.arraycopy(realCookie, 0, this.buf, 12 + this.protocolLength, realCookie.length);
                    this.hasSpoofedCookie = true;
                    super.processStandardData(len, msg);
                    this.buf = null;
                }
            }
            if (!this.hasSpoofedCookie || len == 0) {
                return;
            }
        }
        super.processStandardData(len, msg);
    }

    private boolean compareCookies(byte[] src, byte[] dst, int len) {
        int i;
        for (i = 0; i < len && src[i] == dst[i]; ++i) {
        }
        return i == len;
    }

    private int readMore(SshChannelMessage msg) {
        int len = msg.available();
        if (len > this.requiredLength) {
            msg.read(this.buf, this.idx, this.requiredLength);
            this.idx += this.requiredLength;
            len = this.requiredLength;
            this.requiredLength = 0;
        } else {
            msg.read(this.buf, this.idx, len);
            this.idx += len;
            this.requiredLength -= len;
        }
        return len;
    }
}

