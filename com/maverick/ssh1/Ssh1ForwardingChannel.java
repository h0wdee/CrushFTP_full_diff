/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh1;

import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.SshTunnel;
import com.maverick.ssh1.Ssh1Channel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Ssh1ForwardingChannel
extends Ssh1Channel
implements SshTunnel {
    public static final int LOCAL_FORWARDING = 1;
    public static final int REMOTE_FORWARDING = 2;
    public static final int X11_FORWARDING = 3;
    protected static final String X11AUTH_PROTO = "MIT-MAGIC-COOKIE-1";
    String host;
    int port;
    String listeningAddress;
    int listeningPort;
    String originatingHost;
    int originatingPort;
    int type;
    SshTransport transport;
    SshContext context;

    Ssh1ForwardingChannel(SshContext context, String host, int port, String listeningAddress, int listeningPort, String originatingHost, int originatingPort, int type, SshTransport transport) throws SshException {
        this.context = context;
        this.host = host;
        this.port = port;
        this.listeningAddress = listeningAddress;
        this.listeningPort = listeningPort;
        this.originatingHost = originatingHost;
        this.originatingPort = originatingPort;
        this.type = type;
        this.transport = this.isX11() ? new BufferedX11Transport(transport) : transport;
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
    public String getListeningAddress() {
        return this.listeningAddress;
    }

    @Override
    public int getListeningPort() {
        return this.listeningPort;
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
    public boolean isLocal() {
        return this.type == 1;
    }

    @Override
    public boolean isX11() {
        return this.type == 3;
    }

    @Override
    public SshTransport getTransport() {
        return this.transport;
    }

    @Override
    public boolean isLocalEOF() {
        return this.isClosed();
    }

    @Override
    public boolean isRemoteEOF() {
        return this.isClosed();
    }

    @Override
    public SshTransport duplicate() throws IOException {
        throw new SshIOException(new SshException("SSH tunnels cannot be duplicated!", 4));
    }

    class BufferedX11OutputStream
    extends OutputStream {
        byte[] buf = new byte[1024];
        boolean hasSpoofedCookie = false;
        int idx = 0;
        int requiredLength = 12;
        int protocolLength;
        int cookieLength;
        OutputStream out;

        BufferedX11OutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void write(int ch) throws IOException {
            this.write(new byte[]{(byte)ch}, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                if (Ssh1ForwardingChannel.this.isX11() && !this.hasSpoofedCookie) {
                    int n;
                    if (this.idx < 12) {
                        n = this.readMore(b, off, len);
                        len -= n;
                        off += n;
                        if (this.requiredLength == 0) {
                            if (this.buf[0] == 66) {
                                this.protocolLength = (this.buf[6] & 0xFF) << 8 | this.buf[7] & 0xFF;
                                this.cookieLength = (this.buf[8] & 0xFF) << 8 | this.buf[9] & 0xFF;
                            } else if (this.buf[0] == 108) {
                                this.protocolLength = (this.buf[7] & 0xFF) << 8 | this.buf[6] & 0xFF;
                                this.cookieLength = (this.buf[9] & 0xFF) << 8 | this.buf[8] & 0xFF;
                            } else {
                                throw new SshIOException(new SshException("Corrupt X11 authentication packet", 3));
                            }
                            this.requiredLength = this.protocolLength + 3 & 0xFFFFFFFC;
                            this.requiredLength += this.cookieLength + 3 & 0xFFFFFFFC;
                            if (this.requiredLength + this.idx > this.buf.length) {
                                throw new SshIOException(new SshException("Corrupt X11 authentication packet", 3));
                            }
                            if (this.requiredLength == 0) {
                                throw new SshIOException(new SshException("X11 authentication cookie not found", 3));
                            }
                        }
                    }
                    if (len > 0) {
                        n = this.readMore(b, off, len);
                        len -= n;
                        off += n;
                        if (this.requiredLength == 0) {
                            byte[] fakeCookie = Ssh1ForwardingChannel.this.context.getX11AuthenticationCookie();
                            String protoStr = new String(this.buf, 12, this.protocolLength);
                            byte[] recCookie = new byte[fakeCookie.length];
                            this.protocolLength = this.protocolLength + 3 & 0xFFFFFFFC;
                            System.arraycopy(this.buf, 12 + this.protocolLength, recCookie, 0, fakeCookie.length);
                            if (!Ssh1ForwardingChannel.X11AUTH_PROTO.equals(protoStr) || !this.compareCookies(fakeCookie, recCookie, fakeCookie.length)) {
                                throw new SshIOException(new SshException("Incorrect X11 cookie", 3));
                            }
                            byte[] realCookie = Ssh1ForwardingChannel.this.context.getX11RealCookie();
                            if (realCookie.length != this.cookieLength) {
                                throw new SshIOException(new SshException("Invalid X11 cookie", 3));
                            }
                            System.arraycopy(realCookie, 0, this.buf, 12 + this.protocolLength, realCookie.length);
                            this.hasSpoofedCookie = true;
                            this.out.write(this.buf, 0, this.idx);
                            this.buf = null;
                        }
                    }
                    if (!this.hasSpoofedCookie || len == 0) {
                        return;
                    }
                }
                this.out.write(b, off, len);
            }
            catch (SshException e) {
                throw new SshIOException(e);
            }
        }

        private boolean compareCookies(byte[] src, byte[] dst, int len) {
            int i;
            for (i = 0; i < len && src[i] == dst[i]; ++i) {
            }
            return i == len;
        }

        private int readMore(byte[] b, int off, int len) {
            if (len > this.requiredLength) {
                System.arraycopy(b, off, this.buf, this.idx, this.requiredLength);
                this.idx += this.requiredLength;
                len = this.requiredLength;
                this.requiredLength = 0;
            } else {
                System.arraycopy(b, off, this.buf, this.idx, len);
                this.idx += len;
                this.requiredLength -= len;
            }
            return len;
        }
    }

    class BufferedX11Transport
    implements SshTransport {
        SshTransport transport;
        BufferedX11OutputStream out;

        BufferedX11Transport(SshTransport transport) throws SshException {
            try {
                this.transport = transport;
                this.out = new BufferedX11OutputStream(transport.getOutputStream());
            }
            catch (IOException ex) {
                throw new SshException(ex);
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.transport.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() {
            return this.out;
        }

        @Override
        public String getHost() {
            return this.transport.getHost();
        }

        @Override
        public int getPort() {
            return this.transport.getPort();
        }

        @Override
        public SshTransport duplicate() throws IOException {
            return this.transport.duplicate();
        }

        @Override
        public void close() throws IOException {
            this.transport.close();
        }
    }
}

