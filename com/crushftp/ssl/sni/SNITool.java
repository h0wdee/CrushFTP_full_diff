/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.ssl.sni;

import com.crushftp.ssl.sni.SNIReady;
import com.crushftp.ssl.sni.SSLCapabilities;
import com.crushftp.ssl.sni.SSLExplorer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SNITool {
    public static void main(String[] args) {
        try {
            System.out.println(SNITool.check(new ServerSocket(Integer.parseInt(args[0])).accept()));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static Properties check(Socket socket) {
        try {
            new SNIReady().test();
        }
        catch (Throwable e) {
            return null;
        }
        Properties result = new Properties();
        try {
            InputStream ins = socket.getInputStream();
            byte[] buffer = new byte[255];
            int position = 0;
            SSLCapabilities capabilities = null;
            while (position < 5) {
                int count = 5 - position;
                int n = ins.read(buffer, position, count);
                if (n < 0) {
                    throw new Exception("unexpected end of stream!");
                }
                position += n;
            }
            int recordLength = SSLExplorer.getRequiredSize(buffer, 0, position);
            if (buffer.length < recordLength) {
                buffer = Arrays.copyOf(buffer, recordLength);
            }
            while (position < recordLength) {
                int count = recordLength - position;
                int n = ins.read(buffer, position, count);
                if (n < 0) {
                    throw new Exception("unexpected end of stream!");
                }
                position += n;
            }
            capabilities = SSLExplorer.explore(buffer, 0, recordLength);
            result.put("buffer", new ByteArrayInputStream(buffer, 0, position));
            if (capabilities != null) {
                List serverNames = capabilities.getServerNames();
                Vector<String> names = new Vector<String>();
                result.put("names", names);
                int x = 0;
                while (x < serverNames.size()) {
                    names.addElement(String.valueOf(((SNIServerName)serverNames.get(x)).getType()) + ":" + new String(((SNIServerName)serverNames.get(x)).getEncoded(), "UTF8"));
                    ++x;
                }
            }
        }
        catch (Exception e) {
            result.put("error", e);
        }
        return result;
    }

    public static SSLSocket makeSocket(Socket sock, SSLSocketFactory factory, ByteArrayInputStream bais, String disabled_ciphers) throws IOException {
        class PreferredCipherSuiteSSLSocketFactory
        extends SSLSocketFactory {
            private final SSLSocketFactory delegate;
            private String disabled_ciphers;

            public PreferredCipherSuiteSSLSocketFactory(SSLSocketFactory delegate, String disabled_ciphers) {
                this.delegate = delegate;
                this.disabled_ciphers = disabled_ciphers;
            }

            @Override
            public String[] getDefaultCipherSuites() {
                return SNITool.setEnabledCiphers(this.disabled_ciphers, null, null, this.delegate);
            }

            @Override
            public String[] getSupportedCipherSuites() {
                return SNITool.setEnabledCiphers(this.disabled_ciphers, null, null, this.delegate);
            }

            @Override
            public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
                Socket socket = this.delegate.createSocket(arg0, arg1);
                ((SSLSocket)socket).setEnabledCipherSuites(SNITool.setEnabledCiphers(this.disabled_ciphers, null, null, this.delegate));
                return socket;
            }

            @Override
            public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
                Socket socket = this.delegate.createSocket(arg0, arg1);
                ((SSLSocket)socket).setEnabledCipherSuites(SNITool.setEnabledCiphers(this.disabled_ciphers, null, null, this.delegate));
                return socket;
            }

            @Override
            public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3) throws IOException {
                Socket socket = this.delegate.createSocket(arg0, arg1, arg2, arg3);
                ((SSLSocket)socket).setEnabledCipherSuites(SNITool.setEnabledCiphers(this.disabled_ciphers, null, null, this.delegate));
                return socket;
            }

            @Override
            public Socket createSocket(Socket arg0, InputStream arg1, boolean arg2) throws IOException {
                Socket socket = this.delegate.createSocket(arg0, arg1, arg2);
                ((SSLSocket)socket).setEnabledCipherSuites(SNITool.setEnabledCiphers(this.disabled_ciphers, null, null, this.delegate));
                return socket;
            }

            @Override
            public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException {
                Socket socket = this.delegate.createSocket(arg0, arg1, arg2, arg3);
                ((SSLSocket)socket).setEnabledCipherSuites(SNITool.setEnabledCiphers(this.disabled_ciphers, null, null, this.delegate));
                return socket;
            }

            @Override
            public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
                Socket socket = this.delegate.createSocket(arg0, arg1, arg2, arg3);
                ((SSLSocket)socket).setEnabledCipherSuites(SNITool.setEnabledCiphers(this.disabled_ciphers, null, null, this.delegate));
                return socket;
            }
        }
        factory = new PreferredCipherSuiteSSLSocketFactory(factory, disabled_ciphers);
        return (SSLSocket)factory.createSocket(sock, bais, true);
    }

    public static String[] setEnabledCiphers(String disabled_ciphers, SSLSocket sock, SSLServerSocket serverSock, SSLSocketFactory factory) {
        String[] ciphers = null;
        if (disabled_ciphers.equals("")) {
            if (sock != null) {
                ciphers = sock.getSupportedCipherSuites();
            }
            if (serverSock != null) {
                ciphers = serverSock.getSupportedCipherSuites();
            }
            if (factory != null) {
                ciphers = factory.getSupportedCipherSuites();
            }
        } else {
            disabled_ciphers = disabled_ciphers.toUpperCase();
            Vector<String> enabled_ciphers = new Vector<String>();
            if (sock != null) {
                ciphers = sock.getSupportedCipherSuites();
            }
            if (serverSock != null) {
                ciphers = serverSock.getSupportedCipherSuites();
            }
            if (factory != null) {
                ciphers = factory.getSupportedCipherSuites();
            }
            int x = 0;
            while (x < ciphers.length) {
                if (disabled_ciphers.indexOf("(" + ciphers[x].toUpperCase() + ")") < 0 && ciphers[x].toUpperCase().indexOf("EXPORT") < 0) {
                    enabled_ciphers.addElement(ciphers[x]);
                }
                ++x;
            }
            try {
                SSLParameters sslp = null;
                if (sock != null) {
                    sslp = sock.getSSLParameters();
                }
                if (serverSock != null) {
                    Method getSSLParameters = SSLServerSocket.class.getDeclaredMethod("getSSLParameters", null);
                    sslp = (SSLParameters)getSSLParameters.invoke(serverSock, null);
                }
                Vector<String> enabled_ciphers2 = new Vector<String>();
                int x2 = 1;
                while (x2 < 100) {
                    int pos = disabled_ciphers.indexOf(String.valueOf(x2) + ";");
                    if (pos >= 0) {
                        String cipher = disabled_ciphers.substring(pos, disabled_ciphers.indexOf(")", pos));
                        if (enabled_ciphers.indexOf(cipher = cipher.substring(cipher.indexOf(";") + 1).trim()) >= 0) {
                            enabled_ciphers2.addElement(cipher);
                        }
                    }
                    ++x2;
                }
                x2 = 0;
                while (x2 < enabled_ciphers.size()) {
                    if (enabled_ciphers2.indexOf(enabled_ciphers.elementAt(x2).toString()) < 0) {
                        enabled_ciphers2.addElement(enabled_ciphers.elementAt(x2).toString());
                    }
                    ++x2;
                }
                enabled_ciphers = enabled_ciphers2;
                Method setUseCipherSuitesOrder = SSLParameters.class.getDeclaredMethod("setUseCipherSuitesOrder", Boolean.TYPE);
                setUseCipherSuitesOrder.invoke(sslp, new Boolean(true));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            ciphers = new String[enabled_ciphers.size()];
            x = 0;
            while (x < enabled_ciphers.size()) {
                ciphers[x] = enabled_ciphers.elementAt(x).toString();
                ++x;
            }
            if (sock != null) {
                sock.setEnabledCipherSuites(ciphers);
            }
            if (serverSock != null) {
                serverSock.setEnabledCipherSuites(ciphers);
            }
        }
        return ciphers;
    }
}

