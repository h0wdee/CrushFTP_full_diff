/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh;

import com.maverick.ssh.ConfigurationCollector;
import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.ssh2.Ssh2Context;
import com.sshtools.net.SocketTransport;
import com.sshtools.net.SocketWrapper;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshCompatibilityUtils {
    static Logger log = LoggerFactory.getLogger(SshCompatibilityUtils.class);

    public static String[] getSupportedHostKeyAlgorithms(String hostname, int port) throws SshException, IOException {
        SshConnector con = SshConnector.createInstance();
        Ssh2Client client = (Ssh2Client)con.connect(new SocketTransport(hostname, port), "guest");
        client.disconnect();
        return client.getRemotePublicKeys();
    }

    public static SshPublicKey getHostKey(String hostname, int port) throws SshException, IOException {
        return SshCompatibilityUtils.getHostKey(hostname, port, null);
    }

    public static Set<SshPublicKey> getSupportedHostKeys(String hostname, int port) throws SshException, IOException {
        HashSet<SshPublicKey> hostkeys = new HashSet<SshPublicKey>();
        for (String algorithm : SshCompatibilityUtils.getSupportedHostKeyAlgorithms(hostname, port)) {
            hostkeys.add(SshCompatibilityUtils.getHostKey(hostname, port, algorithm));
        }
        return hostkeys;
    }

    public static SshPublicKey getHostKey(String hostname, int port, String algorithm) throws SshException, IOException {
        SshConnector con = SshConnector.createInstance();
        if (algorithm != null) {
            ((Ssh2Context)con.getContext(2)).setPreferredPublicKey(algorithm);
        }
        ConfigurationCollector hostKeyCollector = new ConfigurationCollector();
        con.addListener(hostKeyCollector);
        try {
            con.connect(new SocketTransport(hostname, port), "guest");
        }
        catch (SshException | IOException e) {
            log.error("Connection failed", (Throwable)e);
        }
        if (hostKeyCollector.getKey() == null) {
            throw new SshException("Failed to retreive servers host key!", 10);
        }
        return hostKeyCollector.getKey();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ConfigurationCollector getRemoteConfiguration(String hostname, int port) throws SshException, IOException {
        SshConnector con = SshConnector.createInstance();
        ConfigurationCollector hostKeyCollector = new ConfigurationCollector();
        con.addListener(hostKeyCollector);
        SocketTransport transport = new SocketTransport(hostname, port);
        try {
            con.connect(transport, "guest");
        }
        catch (SshException e) {
            log.error("Connection failed", (Throwable)e);
        }
        finally {
            try {
                transport.close();
            }
            catch (Throwable throwable) {}
        }
        if (hostKeyCollector.getKey() == null) {
            throw new SshException("Failed to retreive servers host key!", 10);
        }
        return hostKeyCollector;
    }

    public static SshClient getRemoteClient(String hostname, int port, String username, String password, boolean tcpNoDelay) throws SshException, IOException {
        return SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, tcpNoDelay, "aes256-gcm@openssh.com");
    }

    public static SshClient getRemoteClient(String hostname, int port, String username, String password, boolean tcpNoDelay, String cipher) throws SshException, IOException {
        SshConnector con = SshConnector.createInstance();
        Ssh2Context ctx = (Ssh2Context)con.getContext(2);
        ctx.setPreferredCipherCS(cipher);
        ctx.setPreferredCipherSC(cipher);
        Socket transport = new Socket(hostname, port);
        transport.setTcpNoDelay(tcpNoDelay);
        SshClient ssh = con.connect((SshTransport)new SocketWrapper(transport), username, true);
        ssh.authenticate(new PasswordAuthentication(password));
        if (!ssh.isAuthenticated()) {
            throw new IOException("Bas username or password");
        }
        return ssh;
    }
}

