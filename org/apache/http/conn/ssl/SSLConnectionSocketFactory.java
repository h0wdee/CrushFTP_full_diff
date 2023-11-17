/*
 * Decompiled with CFR 0.152.
 */
package org.apache.http.conn.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.SubjectName;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.Args;
import org.apache.http.util.TextUtils;

@Contract(threading=ThreadingBehavior.SAFE)
public class SSLConnectionSocketFactory
implements LayeredConnectionSocketFactory {
    public static final String TLS = "TLS";
    public static final String SSL = "SSL";
    public static final String SSLV2 = "SSLv2";
    @Deprecated
    public static final X509HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = AllowAllHostnameVerifier.INSTANCE;
    @Deprecated
    public static final X509HostnameVerifier BROWSER_COMPATIBLE_HOSTNAME_VERIFIER = BrowserCompatHostnameVerifier.INSTANCE;
    @Deprecated
    public static final X509HostnameVerifier STRICT_HOSTNAME_VERIFIER = StrictHostnameVerifier.INSTANCE;
    private static final String WEAK_KEY_EXCHANGES = "^(TLS|SSL)_(NULL|ECDH_anon|DH_anon|DH_anon_EXPORT|DHE_RSA_EXPORT|DHE_DSS_EXPORT|DSS_EXPORT|DH_DSS_EXPORT|DH_RSA_EXPORT|RSA_EXPORT|KRB5_EXPORT)_(.*)";
    private static final String WEAK_CIPHERS = "^(TLS|SSL)_(.*)_WITH_(NULL|DES_CBC|DES40_CBC|DES_CBC_40|3DES_EDE_CBC|RC4_128|RC4_40|RC2_CBC_40)_(.*)";
    private static final List<Pattern> WEAK_CIPHER_SUITE_PATTERNS = Collections.unmodifiableList(Arrays.asList(Pattern.compile("^(TLS|SSL)_(NULL|ECDH_anon|DH_anon|DH_anon_EXPORT|DHE_RSA_EXPORT|DHE_DSS_EXPORT|DSS_EXPORT|DH_DSS_EXPORT|DH_RSA_EXPORT|RSA_EXPORT|KRB5_EXPORT)_(.*)", 2), Pattern.compile("^(TLS|SSL)_(.*)_WITH_(NULL|DES_CBC|DES40_CBC|DES_CBC_40|3DES_EDE_CBC|RC4_128|RC4_40|RC2_CBC_40)_(.*)", 2)));
    private final Log log = LogFactory.getLog(this.getClass());
    private final SSLSocketFactory socketfactory;
    private final HostnameVerifier hostnameVerifier;
    private final String[] supportedProtocols;
    private final String[] supportedCipherSuites;

    public static HostnameVerifier getDefaultHostnameVerifier() {
        return new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
    }

    public static SSLConnectionSocketFactory getSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory(SSLContexts.createDefault(), SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    }

    static boolean isWeakCipherSuite(String cipherSuite) {
        for (Pattern pattern : WEAK_CIPHER_SUITE_PATTERNS) {
            if (!pattern.matcher(cipherSuite).matches()) continue;
            return true;
        }
        return false;
    }

    private static String[] split(String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    public static SSLConnectionSocketFactory getSystemSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory((SSLSocketFactory)SSLSocketFactory.getDefault(), SSLConnectionSocketFactory.split(System.getProperty("https.protocols")), SSLConnectionSocketFactory.split(System.getProperty("https.cipherSuites")), SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    }

    public SSLConnectionSocketFactory(SSLContext sslContext) {
        this(sslContext, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    }

    @Deprecated
    public SSLConnectionSocketFactory(SSLContext sslContext, X509HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(), null, null, hostnameVerifier);
    }

    @Deprecated
    public SSLConnectionSocketFactory(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, X509HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(), supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    @Deprecated
    public SSLConnectionSocketFactory(SSLSocketFactory socketfactory, X509HostnameVerifier hostnameVerifier) {
        this(socketfactory, null, null, hostnameVerifier);
    }

    @Deprecated
    public SSLConnectionSocketFactory(SSLSocketFactory socketfactory, String[] supportedProtocols, String[] supportedCipherSuites, X509HostnameVerifier hostnameVerifier) {
        this(socketfactory, supportedProtocols, supportedCipherSuites, (HostnameVerifier)hostnameVerifier);
    }

    public SSLConnectionSocketFactory(SSLContext sslContext, HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(), null, null, hostnameVerifier);
    }

    public SSLConnectionSocketFactory(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(), supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    public SSLConnectionSocketFactory(SSLSocketFactory socketfactory, HostnameVerifier hostnameVerifier) {
        this(socketfactory, null, null, hostnameVerifier);
    }

    public SSLConnectionSocketFactory(SSLSocketFactory socketfactory, String[] supportedProtocols, String[] supportedCipherSuites, HostnameVerifier hostnameVerifier) {
        this.socketfactory = Args.notNull(socketfactory, "SSL socket factory");
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
        this.hostnameVerifier = hostnameVerifier != null ? hostnameVerifier : SSLConnectionSocketFactory.getDefaultHostnameVerifier();
    }

    protected void prepareSocket(SSLSocket socket) throws IOException {
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        return SocketFactory.getDefault().createSocket();
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
        Socket sock;
        Args.notNull(host, "HTTP host");
        Args.notNull(remoteAddress, "Remote address");
        Socket socket2 = sock = socket != null ? socket : this.createSocket(context);
        if (localAddress != null) {
            sock.bind(localAddress);
        }
        try {
            if (connectTimeout > 0 && sock.getSoTimeout() == 0) {
                sock.setSoTimeout(connectTimeout);
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Connecting socket to " + remoteAddress + " with timeout " + connectTimeout);
            }
            sock.connect(remoteAddress, connectTimeout);
        }
        catch (IOException ex) {
            try {
                sock.close();
            }
            catch (IOException ignore) {
                // empty catch block
            }
            throw ex;
        }
        if (sock instanceof SSLSocket) {
            SSLSocket sslsock = (SSLSocket)sock;
            this.log.debug("Starting handshake");
            sslsock.startHandshake();
            this.verifyHostname(sslsock, host.getHostName());
            return sock;
        }
        return this.createLayeredSocket(sock, host.getHostName(), remoteAddress.getPort(), context);
    }

    @Override
    public Socket createLayeredSocket(Socket socket, String target, int port, HttpContext context) throws IOException {
        SSLSocket sslsock = (SSLSocket)this.socketfactory.createSocket(socket, target, port, true);
        if (this.supportedProtocols != null) {
            sslsock.setEnabledProtocols(this.supportedProtocols);
        } else {
            String[] allProtocols = sslsock.getEnabledProtocols();
            ArrayList<String> enabledProtocols = new ArrayList<String>(allProtocols.length);
            for (String protocol : allProtocols) {
                if (protocol.startsWith(SSL)) continue;
                enabledProtocols.add(protocol);
            }
            if (!enabledProtocols.isEmpty()) {
                sslsock.setEnabledProtocols(enabledProtocols.toArray(new String[enabledProtocols.size()]));
            }
        }
        if (this.supportedCipherSuites != null) {
            sslsock.setEnabledCipherSuites(this.supportedCipherSuites);
        } else {
            String[] allCipherSuites = sslsock.getEnabledCipherSuites();
            ArrayList<String> enabledCipherSuites = new ArrayList<String>(allCipherSuites.length);
            for (String cipherSuite : allCipherSuites) {
                if (SSLConnectionSocketFactory.isWeakCipherSuite(cipherSuite)) continue;
                enabledCipherSuites.add(cipherSuite);
            }
            if (!enabledCipherSuites.isEmpty()) {
                sslsock.setEnabledCipherSuites(enabledCipherSuites.toArray(new String[enabledCipherSuites.size()]));
            }
        }
        if (this.log.isDebugEnabled()) {
            this.log.debug("Enabled protocols: " + Arrays.asList(sslsock.getEnabledProtocols()));
            this.log.debug("Enabled cipher suites:" + Arrays.asList(sslsock.getEnabledCipherSuites()));
        }
        this.prepareSocket(sslsock);
        this.log.debug("Starting handshake");
        sslsock.startHandshake();
        this.verifyHostname(sslsock, target);
        return sslsock;
    }

    private void verifyHostname(SSLSocket sslsock, String hostname) throws IOException {
        try {
            X509Certificate x509;
            Certificate[] certs;
            SSLSession session = sslsock.getSession();
            if (session == null) {
                InputStream in = sslsock.getInputStream();
                in.available();
                session = sslsock.getSession();
                if (session == null) {
                    sslsock.startHandshake();
                    session = sslsock.getSession();
                }
            }
            if (session == null) {
                throw new SSLHandshakeException("SSL session not available");
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Secure session established");
                this.log.debug(" negotiated protocol: " + session.getProtocol());
                this.log.debug(" negotiated cipher suite: " + session.getCipherSuite());
                try {
                    certs = session.getPeerCertificates();
                    x509 = (X509Certificate)certs[0];
                    X500Principal peer = x509.getSubjectX500Principal();
                    this.log.debug(" peer principal: " + peer.toString());
                    Collection<List<?>> altNames1 = x509.getSubjectAlternativeNames();
                    if (altNames1 != null) {
                        ArrayList<String> altNames = new ArrayList<String>();
                        for (List<?> aC : altNames1) {
                            if (aC.isEmpty()) continue;
                            altNames.add((String)aC.get(1));
                        }
                        this.log.debug(" peer alternative names: " + altNames);
                    }
                    X500Principal issuer = x509.getIssuerX500Principal();
                    this.log.debug(" issuer principal: " + issuer.toString());
                    Collection<List<?>> altNames2 = x509.getIssuerAlternativeNames();
                    if (altNames2 != null) {
                        ArrayList<String> altNames = new ArrayList<String>();
                        for (List<?> aC : altNames2) {
                            if (aC.isEmpty()) continue;
                            altNames.add((String)aC.get(1));
                        }
                        this.log.debug(" issuer alternative names: " + altNames);
                    }
                }
                catch (Exception ignore) {
                    // empty catch block
                }
            }
            if (!this.hostnameVerifier.verify(hostname, session)) {
                certs = session.getPeerCertificates();
                x509 = (X509Certificate)certs[0];
                List<SubjectName> subjectAlts = DefaultHostnameVerifier.getSubjectAltNames(x509);
                throw new SSLPeerUnverifiedException("Certificate for <" + hostname + "> doesn't match any " + "of the subject alternative names: " + subjectAlts);
            }
        }
        catch (IOException iox) {
            try {
                sslsock.close();
            }
            catch (Exception x) {
                // empty catch block
            }
            throw iox;
        }
    }
}

