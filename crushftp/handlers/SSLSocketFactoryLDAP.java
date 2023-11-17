/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.Common;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.SocketFactory;

public class SSLSocketFactoryLDAP
extends SocketFactory {
    public static SocketFactory getDefault() {
        return new SSLSocketFactoryLDAP();
    }

    @Override
    public Socket createSocket() throws IOException {
        return new Socket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return Common.getSSLSocket("", "", "", true, new Socket(host, port), host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return Common.getSSLSocket("", "", "", true, new Socket(host, port), host.getHostAddress(), port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return Common.getSSLSocket("", "", "", true, new Socket(host, port), host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return Common.getSSLSocket("", "", "", true, new Socket(localAddress.getHostAddress(), port), localAddress.getHostAddress(), port);
    }
}

