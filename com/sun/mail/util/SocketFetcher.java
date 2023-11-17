/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util;

import com.sun.mail.util.MailLogger;
import com.sun.mail.util.MailSSLSocketFactory;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketConnectException;
import com.sun.mail.util.WriteTimeoutSocket;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SocketFetcher {
    private static MailLogger logger = new MailLogger(SocketFetcher.class, "socket", "DEBUG SocketFetcher", PropUtil.getBooleanSystemProperty("mail.socket.debug", false), System.out);

    private SocketFetcher() {
    }

    public static Socket getSocket(String host, int port, Properties props, String prefix, boolean useSSL) throws IOException {
        int to;
        int localport;
        InetAddress localaddr;
        Socket socket;
        int cto;
        block21: {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("getSocket, host " + host + ", port " + port + ", prefix " + prefix + ", useSSL " + useSSL);
            }
            if (prefix == null) {
                prefix = "socket";
            }
            if (props == null) {
                props = new Properties();
            }
            cto = PropUtil.getIntProperty(props, prefix + ".connectiontimeout", -1);
            socket = null;
            String localaddrstr = props.getProperty(prefix + ".localaddress", null);
            localaddr = null;
            if (localaddrstr != null) {
                localaddr = InetAddress.getByName(localaddrstr);
            }
            localport = PropUtil.getIntProperty(props, prefix + ".localport", 0);
            boolean fb = PropUtil.getBooleanProperty(props, prefix + ".socketFactory.fallback", true);
            int sfPort = -1;
            String sfErr = "unknown socket factory";
            to = PropUtil.getIntProperty(props, prefix + ".timeout", -1);
            try {
                String sfClass;
                Object sfo;
                SocketFactory sf = null;
                String sfPortName = null;
                if (useSSL) {
                    sfo = props.get(prefix + ".ssl.socketFactory");
                    if (sfo instanceof SocketFactory) {
                        sf = (SocketFactory)sfo;
                        sfErr = "SSL socket factory instance " + sf;
                    }
                    if (sf == null) {
                        sfClass = props.getProperty(prefix + ".ssl.socketFactory.class");
                        sf = SocketFetcher.getSocketFactory(sfClass);
                        sfErr = "SSL socket factory class " + sfClass;
                    }
                    sfPortName = ".ssl.socketFactory.port";
                }
                if (sf == null) {
                    sfo = props.get(prefix + ".socketFactory");
                    if (sfo instanceof SocketFactory) {
                        sf = (SocketFactory)sfo;
                        sfErr = "socket factory instance " + sf;
                    }
                    if (sf == null) {
                        sfClass = props.getProperty(prefix + ".socketFactory.class");
                        sf = SocketFetcher.getSocketFactory(sfClass);
                        sfErr = "socket factory class " + sfClass;
                    }
                    sfPortName = ".socketFactory.port";
                }
                if (sf != null) {
                    sfPort = PropUtil.getIntProperty(props, prefix + sfPortName, -1);
                    if (sfPort == -1) {
                        sfPort = port;
                    }
                    socket = SocketFetcher.createSocket(localaddr, localport, host, sfPort, cto, to, props, prefix, sf, useSSL);
                }
            }
            catch (SocketTimeoutException sex) {
                throw sex;
            }
            catch (Exception ex) {
                Throwable t;
                if (fb) break block21;
                if (ex instanceof InvocationTargetException && (t = ((InvocationTargetException)ex).getTargetException()) instanceof Exception) {
                    ex = (Exception)t;
                }
                if (ex instanceof IOException) {
                    throw (IOException)ex;
                }
                throw new SocketConnectException("Using " + sfErr, ex, host, sfPort, cto);
            }
        }
        if (socket == null) {
            socket = SocketFetcher.createSocket(localaddr, localport, host, port, cto, to, props, prefix, null, useSSL);
        } else if (to >= 0) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("set socket read timeout " + to);
            }
            socket.setSoTimeout(to);
        }
        return socket;
    }

    public static Socket getSocket(String host, int port, Properties props, String prefix) throws IOException {
        return SocketFetcher.getSocket(host, port, props, prefix, false);
    }

    private static Socket createSocket(InetAddress localaddr, int localport, String host, int port, int cto, int to, Properties props, String prefix, SocketFactory sf, boolean useSSL) throws IOException {
        int writeTimeout;
        Socket socket = null;
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("create socket: prefix " + prefix + ", localaddr " + localaddr + ", localport " + localport + ", host " + host + ", port " + port + ", connection timeout " + cto + ", timeout " + to + ", socket factory " + sf + ", useSSL " + useSSL);
        }
        String socksHost = props.getProperty(prefix + ".socks.host", null);
        int socksPort = 1080;
        String err = null;
        if (socksHost != null) {
            int i = socksHost.indexOf(58);
            if (i >= 0) {
                socksHost = socksHost.substring(0, i);
                try {
                    socksPort = Integer.parseInt(socksHost.substring(i + 1));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
            socksPort = PropUtil.getIntProperty(props, prefix + ".socks.port", socksPort);
            err = "Using SOCKS host, port: " + socksHost + ", " + socksPort;
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("socks host " + socksHost + ", port " + socksPort);
            }
        }
        if (sf != null && !(sf instanceof SSLSocketFactory)) {
            socket = sf.createSocket();
        }
        if (socket == null) {
            if (socksHost != null) {
                socket = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksHost, socksPort)));
            } else if (PropUtil.getBooleanProperty(props, prefix + ".usesocketchannels", false)) {
                logger.finer("using SocketChannels");
                socket = SocketChannel.open().socket();
            } else {
                socket = new Socket();
            }
        }
        if (to >= 0) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("set socket read timeout " + to);
            }
            socket.setSoTimeout(to);
        }
        if ((writeTimeout = PropUtil.getIntProperty(props, prefix + ".writetimeout", -1)) != -1) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("set socket write timeout " + writeTimeout);
            }
            socket = new WriteTimeoutSocket(socket, writeTimeout);
        }
        if (localaddr != null) {
            socket.bind(new InetSocketAddress(localaddr, localport));
        }
        try {
            logger.finest("connecting...");
            if (cto >= 0) {
                socket.connect(new InetSocketAddress(host, port), cto);
            } else {
                socket.connect(new InetSocketAddress(host, port));
            }
            logger.finest("success!");
        }
        catch (IOException ex) {
            logger.log(Level.FINEST, "connection failed", ex);
            throw new SocketConnectException(err, ex, host, port, cto);
        }
        if ((useSSL || sf instanceof SSLSocketFactory) && !(socket instanceof SSLSocket)) {
            SSLSocketFactory ssf;
            String trusted = props.getProperty(prefix + ".ssl.trust");
            if (trusted != null) {
                try {
                    MailSSLSocketFactory msf = new MailSSLSocketFactory();
                    if (trusted.equals("*")) {
                        msf.setTrustAllHosts(true);
                    } else {
                        msf.setTrustedHosts(trusted.split("\\s+"));
                    }
                    ssf = msf;
                }
                catch (GeneralSecurityException gex) {
                    IOException ioex = new IOException("Can't create MailSSLSocketFactory");
                    ioex.initCause(gex);
                    throw ioex;
                }
            } else {
                ssf = sf instanceof SSLSocketFactory ? (SSLSocketFactory)sf : (SSLSocketFactory)SSLSocketFactory.getDefault();
            }
            socket = ssf.createSocket(socket, host, port, true);
            sf = ssf;
        }
        SocketFetcher.configureSSLSocket(socket, host, props, prefix, sf);
        return socket;
    }

    private static SocketFactory getSocketFactory(String sfClass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (sfClass == null || sfClass.length() == 0) {
            return null;
        }
        ClassLoader cl = SocketFetcher.getContextClassLoader();
        Class<?> clsSockFact = null;
        if (cl != null) {
            try {
                clsSockFact = Class.forName(sfClass, false, cl);
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
        }
        if (clsSockFact == null) {
            clsSockFact = Class.forName(sfClass);
        }
        Method mthGetDefault = clsSockFact.getMethod("getDefault", new Class[0]);
        SocketFactory sf = (SocketFactory)mthGetDefault.invoke(new Object(), new Object[0]);
        return sf;
    }

    @Deprecated
    public static Socket startTLS(Socket socket) throws IOException {
        return SocketFetcher.startTLS(socket, new Properties(), "socket");
    }

    @Deprecated
    public static Socket startTLS(Socket socket, Properties props, String prefix) throws IOException {
        InetAddress a = socket.getInetAddress();
        String host = a.getHostName();
        return SocketFetcher.startTLS(socket, host, props, prefix);
    }

    public static Socket startTLS(Socket socket, String host, Properties props, String prefix) throws IOException {
        int port = socket.getPort();
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("startTLS host " + host + ", port " + port);
        }
        String sfErr = "unknown socket factory";
        try {
            String sfClass;
            SSLSocketFactory ssf = null;
            SocketFactory sf = null;
            Object sfo = props.get(prefix + ".ssl.socketFactory");
            if (sfo instanceof SocketFactory) {
                sf = (SocketFactory)sfo;
                sfErr = "SSL socket factory instance " + sf;
            }
            if (sf == null) {
                sfClass = props.getProperty(prefix + ".ssl.socketFactory.class");
                sf = SocketFetcher.getSocketFactory(sfClass);
                sfErr = "SSL socket factory class " + sfClass;
            }
            if (sf != null && sf instanceof SSLSocketFactory) {
                ssf = (SSLSocketFactory)sf;
            }
            if (ssf == null) {
                sfo = props.get(prefix + ".socketFactory");
                if (sfo instanceof SocketFactory) {
                    sf = (SocketFactory)sfo;
                    sfErr = "socket factory instance " + sf;
                }
                if (sf == null) {
                    sfClass = props.getProperty(prefix + ".socketFactory.class");
                    sf = SocketFetcher.getSocketFactory(sfClass);
                    sfErr = "socket factory class " + sfClass;
                }
                if (sf != null && sf instanceof SSLSocketFactory) {
                    ssf = (SSLSocketFactory)sf;
                }
            }
            if (ssf == null) {
                String trusted = props.getProperty(prefix + ".ssl.trust");
                if (trusted != null) {
                    try {
                        MailSSLSocketFactory msf = new MailSSLSocketFactory();
                        if (trusted.equals("*")) {
                            msf.setTrustAllHosts(true);
                        } else {
                            msf.setTrustedHosts(trusted.split("\\s+"));
                        }
                        ssf = msf;
                        sfErr = "mail SSL socket factory";
                    }
                    catch (GeneralSecurityException gex) {
                        IOException ioex = new IOException("Can't create MailSSLSocketFactory");
                        ioex.initCause(gex);
                        throw ioex;
                    }
                } else {
                    ssf = (SSLSocketFactory)SSLSocketFactory.getDefault();
                    sfErr = "default SSL socket factory";
                }
            }
            socket = ssf.createSocket(socket, host, port, true);
            SocketFetcher.configureSSLSocket(socket, host, props, prefix, ssf);
        }
        catch (Exception ex) {
            Throwable t;
            if (ex instanceof InvocationTargetException && (t = ((InvocationTargetException)ex).getTargetException()) instanceof Exception) {
                ex = (Exception)t;
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            IOException ioex = new IOException("Exception in startTLS using " + sfErr + ": host, port: " + host + ", " + port + "; Exception: " + ex);
            ioex.initCause(ex);
            throw ioex;
        }
        return socket;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void configureSSLSocket(Socket socket, String host, Properties props, String prefix, SocketFactory sf) throws IOException {
        MailSSLSocketFactory msf;
        if (!(socket instanceof SSLSocket)) {
            return;
        }
        SSLSocket sslsocket = (SSLSocket)socket;
        String protocols = props.getProperty(prefix + ".ssl.protocols", null);
        if (protocols != null) {
            sslsocket.setEnabledProtocols(SocketFetcher.stringArray(protocols));
        } else {
            String[] prots = sslsocket.getEnabledProtocols();
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("SSL enabled protocols before " + Arrays.asList(prots));
            }
            ArrayList<String> eprots = new ArrayList<String>();
            for (int i = 0; i < prots.length; ++i) {
                if (prots[i] == null || prots[i].startsWith("SSL")) continue;
                eprots.add(prots[i]);
            }
            sslsocket.setEnabledProtocols(eprots.toArray(new String[eprots.size()]));
        }
        String ciphers = props.getProperty(prefix + ".ssl.ciphersuites", null);
        if (ciphers != null) {
            sslsocket.setEnabledCipherSuites(SocketFetcher.stringArray(ciphers));
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("SSL enabled protocols after " + Arrays.asList(sslsocket.getEnabledProtocols()));
            logger.finer("SSL enabled ciphers after " + Arrays.asList(sslsocket.getEnabledCipherSuites()));
        }
        sslsocket.startHandshake();
        boolean idCheck = PropUtil.getBooleanProperty(props, prefix + ".ssl.checkserveridentity", false);
        if (idCheck) {
            SocketFetcher.checkServerIdentity(host, sslsocket);
        }
        if (sf instanceof MailSSLSocketFactory && !(msf = (MailSSLSocketFactory)sf).isServerTrusted(host, sslsocket)) {
            try {
                sslsocket.close();
                Object var11_12 = null;
            }
            catch (Throwable throwable) {
                Object var11_13 = null;
                throw new IOException("Server is not trusted: " + host);
            }
            throw new IOException("Server is not trusted: " + host);
        }
    }

    private static void checkServerIdentity(String server, SSLSocket sslSocket) throws IOException {
        try {
            Certificate[] certChain = sslSocket.getSession().getPeerCertificates();
            if (certChain != null && certChain.length > 0 && certChain[0] instanceof X509Certificate && SocketFetcher.matchCert(server, (X509Certificate)certChain[0])) {
                return;
            }
        }
        catch (SSLPeerUnverifiedException e) {
            sslSocket.close();
            IOException ioex = new IOException("Can't verify identity of server: " + server);
            ioex.initCause(e);
            throw ioex;
        }
        sslSocket.close();
        throw new IOException("Can't verify identity of server: " + server);
    }

    private static boolean matchCert(String server, X509Certificate cert) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("matchCert server " + server + ", cert " + cert);
        }
        try {
            Class<?> hnc = Class.forName("sun.security.util.HostnameChecker");
            Method getInstance = hnc.getMethod("getInstance", Byte.TYPE);
            Object hostnameChecker = getInstance.invoke(new Object(), (byte)2);
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("using sun.security.util.HostnameChecker");
            }
            Method match = hnc.getMethod("match", String.class, X509Certificate.class);
            try {
                match.invoke(hostnameChecker, server, cert);
                return true;
            }
            catch (InvocationTargetException cex) {
                logger.log(Level.FINER, "HostnameChecker FAIL", cex);
                return false;
            }
        }
        catch (Exception ex) {
            logger.log(Level.FINER, "NO sun.security.util.HostnameChecker", ex);
            try {
                Collection<List<?>> names = cert.getSubjectAlternativeNames();
                if (names != null) {
                    boolean foundName = false;
                    for (List<?> nameEnt : names) {
                        Integer type = (Integer)nameEnt.get(0);
                        if (type != 2) continue;
                        foundName = true;
                        String name = (String)nameEnt.get(1);
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("found name: " + name);
                        }
                        if (!SocketFetcher.matchServer(server, name)) continue;
                        return true;
                    }
                    if (foundName) {
                        return false;
                    }
                }
            }
            catch (CertificateParsingException names) {
                // empty catch block
            }
            Pattern p = Pattern.compile("CN=([^,]*)");
            Matcher m = p.matcher(cert.getSubjectX500Principal().getName());
            return m.find() && SocketFetcher.matchServer(server, m.group(1).trim());
        }
    }

    private static boolean matchServer(String server, String name) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("match server " + server + " with " + name);
        }
        if (name.startsWith("*.")) {
            String tail = name.substring(2);
            if (tail.length() == 0) {
                return false;
            }
            int off = server.length() - tail.length();
            if (off < 1) {
                return false;
            }
            return server.charAt(off - 1) == '.' && server.regionMatches(true, off, tail, 0, tail.length());
        }
        return server.equalsIgnoreCase(name);
    }

    private static String[] stringArray(String s) {
        StringTokenizer st = new StringTokenizer(s);
        ArrayList<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    private static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>(){

            @Override
            public ClassLoader run() {
                ClassLoader cl = null;
                try {
                    cl = Thread.currentThread().getContextClassLoader();
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
                return cl;
            }
        });
    }
}

