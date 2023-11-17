/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshClientAdapter;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.ssh2.Ssh2Context;
import com.sshtools.net.SocketTransport;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SshKeyUtils
extends com.maverick.util.SshKeyUtils {
    public static SshPublicKey getHostKey(String hostname, int port) throws SshException {
        SshConnector con = SshConnector.createInstance();
        HostKeyCollector hostKeyCollector = new HostKeyCollector();
        con.addListener(hostKeyCollector);
        Exception lastError = null;
        try {
            con.connect(new SocketTransport(hostname, port), "guest");
        }
        catch (SshException e) {
            lastError = e;
        }
        catch (IOException e) {
            lastError = e;
        }
        if (hostKeyCollector.getKey() == null) {
            throw new SshException(10, (Throwable)lastError);
        }
        return hostKeyCollector.getKey();
    }

    public static Set<SshPublicKey> getSupportedHostKeys(String hostname, int port) throws SshException, IOException {
        HashSet<SshPublicKey> hostkeys = new HashSet<SshPublicKey>();
        for (String algorithm : SshKeyUtils.getSupportedHostKeyAlgorithms(hostname, port)) {
            hostkeys.add(SshKeyUtils.getHostKey(hostname, port, algorithm));
        }
        return hostkeys;
    }

    public static String[] getSupportedHostKeyAlgorithms(String hostname, int port) throws SshException, IOException {
        SshConnector con = SshConnector.createInstance();
        con.setSupportedVersions(2);
        Ssh2Client client = (Ssh2Client)con.connect(new SocketTransport(hostname, port), "guest");
        client.disconnect();
        return client.getRemotePublicKeys();
    }

    public static SshPublicKey getHostKey(String hostname, int port, String algorithm) throws SshException, IOException {
        SshConnector con = SshConnector.createInstance();
        con.setSupportedVersions(2);
        Ssh2Context context = (Ssh2Context)con.getContext(2);
        context.setPreferredPublicKey(algorithm);
        Ssh2Client client = (Ssh2Client)con.connect(new SocketTransport(hostname, port), "guest");
        client.disconnect();
        return client.getHostKey();
    }

    static class HostKeyCollector
    extends SshClientAdapter {
        SshPublicKey key = null;

        HostKeyCollector() {
        }

        @Override
        public void keyExchangeComplete(SshClient client, SshPublicKey hostkey, String keyExchange, String cipherCS, String cipherSC, String macCS, String macSC, String compressionCS, String compressionSC) {
            this.key = hostkey;
            client.disconnect();
        }

        public SshPublicKey getKey() {
            return this.key;
        }
    }
}

