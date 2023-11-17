/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import crushftp.handlers.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;

public class SocketProxiedIP
extends Socket {
    Socket sock2 = null;
    int proxy_version = 0;
    boolean skip_write = false;
    OutputStream out = null;
    String user_real_ip = null;
    static final byte[] header_master_proxy_v2;

    static {
        byte[] byArray = new byte[12];
        byArray[0] = 13;
        byArray[1] = 10;
        byArray[2] = 13;
        byArray[3] = 10;
        byArray[5] = 13;
        byArray[6] = 10;
        byArray[7] = 81;
        byArray[8] = 85;
        byArray[9] = 73;
        byArray[10] = 84;
        byArray[11] = 10;
        header_master_proxy_v2 = byArray;
    }

    public SocketProxiedIP(Socket sock2, int proxy_version) {
        this.sock2 = sock2;
        this.proxy_version = proxy_version;
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        this.sock2.bind(bindpoint);
    }

    @Override
    public void close() throws IOException {
        this.sock2.close();
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        this.sock2.connect(endpoint, timeout);
    }

    @Override
    public InetAddress getInetAddress() {
        return this.sock2.getInetAddress();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.sock2.getInputStream();
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return this.sock2.getKeepAlive();
    }

    @Override
    public InetAddress getLocalAddress() {
        return this.sock2.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return this.sock2.getLocalPort();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return this.sock2.getLocalSocketAddress();
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return this.sock2.getOOBInline();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (this.out != null) {
            return this.out;
        }
        class OutputStreamSkipper
        extends OutputStream {
            OutputStream out = null;

            public OutputStreamSkipper(OutputStream out) {
                this.out = out;
            }

            @Override
            public void flush() throws IOException {
                this.out.flush();
            }

            @Override
            public void write(int i) throws IOException {
                this.write(new byte[]{(byte)i}, 0, 1);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int start, int len) throws IOException {
                if (SocketProxiedIP.this.skip_write) {
                    SocketProxiedIP.this.skip_write = false;
                    return;
                }
                this.out.write(b, start, len);
            }

            @Override
            public void close() throws IOException {
                this.out.close();
            }
        }
        this.out = new OutputStreamSkipper(this.sock2.getOutputStream());
        return this.out;
    }

    @Override
    public int getPort() {
        return this.sock2.getPort();
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        return this.sock2.getReceiveBufferSize();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return this.sock2.getRemoteSocketAddress();
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return this.sock2.getReuseAddress();
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        return this.sock2.getSendBufferSize();
    }

    @Override
    public int getSoLinger() throws SocketException {
        return this.sock2.getSoLinger();
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return this.sock2.getSoTimeout();
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return this.sock2.getTcpNoDelay();
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return this.sock2.getTrafficClass();
    }

    @Override
    public boolean isBound() {
        return this.sock2.isBound();
    }

    @Override
    public boolean isClosed() {
        return this.sock2.isClosed();
    }

    @Override
    public boolean isConnected() {
        return this.sock2.isConnected();
    }

    @Override
    public boolean isInputShutdown() {
        return this.sock2.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return this.sock2.isOutputShutdown();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        this.sock2.sendUrgentData(data);
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        this.sock2.setKeepAlive(on);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        this.sock2.setOOBInline(on);
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        this.sock2.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    @Override
    public void setReceiveBufferSize(int size) throws SocketException {
        this.sock2.setReceiveBufferSize(size);
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        this.sock2.setReuseAddress(on);
    }

    @Override
    public void setSendBufferSize(int size) throws SocketException {
        this.sock2.setSendBufferSize(size);
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        this.sock2.setSoLinger(on, linger);
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        this.sock2.setSoTimeout(timeout);
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        this.sock2.setTcpNoDelay(on);
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        this.sock2.setTrafficClass(tc);
    }

    @Override
    public void shutdownInput() throws IOException {
        this.sock2.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        this.sock2.shutdownOutput();
    }

    @Override
    public String toString() {
        return this.sock2.toString();
    }

    public String getRemoteIp() {
        if (this.user_real_ip != null) {
            return this.user_real_ip;
        }
        String ip = this.sock2.getInetAddress().getHostAddress();
        if (this.proxy_version == 1) {
            ip = this.readProxyIP(this.sock2);
        } else if (this.proxy_version == 2) {
            ip = this.readProxyIPV2(this.sock2);
        }
        return ip;
    }

    public void skipWrite() {
        this.skip_write = true;
    }

    public String readProxyIP(Socket sock) {
        if (this.user_real_ip != null) {
            return this.user_real_ip;
        }
        try {
            int prior_timeout = sock.getSoTimeout();
            sock.setSoTimeout(2000);
            InputStream in = sock.getInputStream();
            String line = "";
            byte[] b = new byte[1];
            int read = 1;
            while (!line.endsWith("\r\n") && read > 0) {
                read = in.read(b);
                if (read <= 0) continue;
                line = String.valueOf(line) + new String(b);
            }
            sock.setSoTimeout(prior_timeout);
            this.user_real_ip = line.split(" ")[2].trim();
            return this.user_real_ip;
        }
        catch (Exception e) {
            Log.log("SERVER", 2, "ReadProxyIPv1 failed:" + e);
            Log.log("SERVER", 2, e);
            return null;
        }
    }

    public String readProxyIPV2(Socket sock) {
        if (this.user_real_ip != null) {
            return this.user_real_ip;
        }
        try {
            InputStream in = sock.getInputStream();
            byte[] b_header = new byte[1];
            int loc = 0;
            while (loc < header_master_proxy_v2.length) {
                in.read(b_header);
                if (b_header[0] == header_master_proxy_v2[loc++]) continue;
                throw new Exception("Invalid proxy protocol v2 header");
            }
            byte[] b = new byte[1];
            in.read(b);
            int protocol_version = b[0] >>> 4 & 0xF;
            int command = b[0] & 0xF;
            in.read(b);
            int address_family = b[0] >>> 4 & 0xF;
            int transport_protocol = b[0] & 0xF;
            int i15 = in.read();
            int i16 = in.read();
            int total_len = (i15 << 8) + i16;
            byte[] address_info = new byte[total_len];
            if (in.read(address_info) < address_info.length) {
                throw new Exception("Proxy header read of address_info failed due to insufficient bytes.");
            }
            if (protocol_version != 2) {
                throw new Exception("Proxy protocol version did not indicate v2:" + protocol_version);
            }
            if (command == 0 || address_family == 0) {
                this.user_real_ip = sock.getInetAddress().getHostAddress();
                return this.user_real_ip;
            }
            if (command == 1) {
                if (transport_protocol > 1) {
                    throw new Exception("Transport protocol not supported:" + transport_protocol);
                }
                if (address_family == 1) {
                    this.user_real_ip = InetAddress.getByAddress(Arrays.copyOfRange(address_info, 0, 4)).getHostAddress();
                    return this.user_real_ip;
                }
                if (address_family == 2) {
                    this.user_real_ip = InetAddress.getByAddress(Arrays.copyOfRange(address_info, 0, 16)).getHostAddress();
                    return this.user_real_ip;
                }
                throw new Exception("Unsupported address family:" + address_family);
            }
            throw new Exception("Proxy protocol command not understood.");
        }
        catch (Exception e) {
            try {
                sock.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            Log.log("SERVER", 2, "ReadProxyIPv2 failed:" + e);
            Log.log("SERVER", 2, e);
            return null;
        }
    }
}

