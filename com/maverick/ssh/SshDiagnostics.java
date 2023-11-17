/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.TransferCancelledException;
import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.ConfigurationCollector;
import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshCompatibilityUtils;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh2.Ssh2Context;
import com.sshtools.net.SocketTransport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SshDiagnostics {
    static String hostname;
    static int port;

    public static void main(String[] args) throws SshException, IOException, SftpStatusException, ChannelOpenException, TransferCancelledException {
        AdaptiveConfiguration.setGlobalConfig("idleConnectionTimeoutSeconds", 30);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean runAlgorithmTests = false;
        boolean runKex = false;
        boolean runCipher = false;
        boolean runMac = false;
        boolean runKeys = false;
        boolean runCompression = false;
        if (args.length >= 2) {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
            if (args.length > 2) {
                runAlgorithmTests = args[2].equals("all");
                runKex = args[2].equals("kex");
                runCipher = args[2].equals("cipher");
                runMac = args[2].equals("mac");
                runKeys = args[2].equals("keys");
                runCompression = args[2].equals("comp");
            }
        } else {
            System.out.print("Hostname: ");
            hostname = reader.readLine();
            int idx = hostname.indexOf(":");
            if (idx > -1) {
                hostname = hostname.substring(0, idx);
                port = Integer.parseInt(hostname.substring(idx + 1));
            }
        }
        ConfigurationCollector cfg = SshCompatibilityUtils.getRemoteConfiguration(hostname, port);
        System.out.println(String.format("Identification       : %s", cfg.getRemoteIdentification()));
        System.out.println(String.format("Negotiated security  : %s", new Object[]{cfg.getSecurityLevel()}));
        System.out.println(String.format("Maximum security     : %s", new Object[]{cfg.getMaximumSecurity()}));
        System.out.println(String.format("Minimum security     : %s", new Object[]{cfg.getMinimumSecurity()}));
        System.out.println("*****************");
        System.out.println(String.format("Host key             : %s", cfg.getNegotiatedHostKey()));
        System.out.println(String.format("Key exchange         : %s", cfg.getNegotiatedKeyExchange()));
        System.out.println(String.format("Cipher CS            : %s", cfg.getNegotiatedCipherCS()));
        System.out.println(String.format("Cipher SC            : %s", cfg.getNegotiatedCipherSC()));
        System.out.println(String.format("Mac CS               : %s", cfg.getNegotiatedMacCS()));
        System.out.println(String.format("Mac SC               : %s", cfg.getNegotiatedMacSC()));
        System.out.println(String.format("Compression CS       : %s", cfg.getNegotiatedCompressionCS()));
        System.out.println(String.format("Compression SC       : %s", cfg.getNegotiatedCompressionSC()));
        System.out.println("*****************");
        System.out.println("Supports             :");
        System.out.println("Key exchanges");
        for (String obj : cfg.getSupportedKeyExchanges()) {
            System.out.println(String.format("   %s", obj));
        }
        System.out.println("Host keys");
        for (String obj : cfg.getSupportedHostKeys()) {
            System.out.println(String.format("   %s", obj));
        }
        System.out.println("Ciphers");
        for (String obj : cfg.getSupportedCiphers()) {
            System.out.println(String.format("   %s", obj));
        }
        System.out.println("Macs");
        for (String obj : cfg.getSupportedMacs()) {
            System.out.println(String.format("   %s", obj));
        }
        System.out.println("Compression");
        for (String obj : cfg.getSupportedCompressions()) {
            System.out.println(String.format("   %s", obj));
        }
        AdaptiveConfiguration.setGlobalConfig("reconfigureKexOnFailure", false);
        if (runAlgorithmTests || runKex) {
            System.out.println("##### Testing key exchanges #####");
            SshDiagnostics.testKexs(new ArrayList<String>(cfg.getSupportedKeyExchanges()));
        }
        if (runAlgorithmTests || runCipher) {
            System.out.println("##### Testing ciphers #####");
            SshDiagnostics.testCiphers(new ArrayList<String>(cfg.getSupportedCiphers()));
        }
        if (runAlgorithmTests || runMac) {
            System.out.println("##### Testing macs #####");
            SshDiagnostics.testMacs(new ArrayList<String>(cfg.getSupportedMacs()));
        }
        if (runAlgorithmTests || runKeys) {
            System.out.println("##### Testing host keys #####");
            SshDiagnostics.testPublicKeys(new ArrayList<String>(cfg.getSupportedHostKeys()));
        }
        if (runAlgorithmTests) {
            System.out.println("##### Probing all configurations #####");
            SshDiagnostics.probeConfigurations(cfg);
        }
        System.exit(0);
    }

    public static ConfigurationCollector getRemoteConfiguration(String hostname, int port) throws SshException, IOException {
        return SshCompatibilityUtils.getRemoteConfiguration(hostname, port);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void testCiphers(List<String> ciphers) {
        while (!ciphers.isEmpty()) {
            String cipher = ciphers.remove(0);
            try {
                SshConnector con = SshConnector.createInstance();
                Ssh2Context ctx = (Ssh2Context)con.getContext(2);
                if (!ctx.supportedCiphersCS().contains(cipher)) continue;
                ctx.supportedCiphersCS().removeAllBut(cipher);
                ctx.supportedCiphersSC().removeAllBut(cipher);
                SocketTransport transport = new SocketTransport(hostname, port);
                SshClient ssh = null;
                try {
                    ssh = con.connect(transport, "guest");
                    ssh.authenticate(new PasswordAuthentication("guest"));
                    System.out.println(String.format("Established connection with cipher %s", cipher));
                }
                finally {
                    try {
                        transport.close();
                    }
                    catch (Throwable throwable) {}
                    if (ssh == null) continue;
                    ssh.disconnect();
                }
            }
            catch (Throwable e) {
                System.out.println(String.format("Connection failed with cipher %s [%s]", cipher, e.getMessage()));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void testMacs(List<String> macs) {
        while (!macs.isEmpty()) {
            String mac = macs.remove(0);
            try {
                SshConnector con = SshConnector.createInstance();
                Ssh2Context ctx = (Ssh2Context)con.getContext(2);
                if (!ctx.supportedMacsCS().contains(mac)) continue;
                ctx.supportedMacsCS().removeAllBut(mac);
                ctx.supportedMacsSC().removeAllBut(mac);
                SocketTransport transport = new SocketTransport(hostname, port);
                SshClient ssh = null;
                try {
                    ssh = con.connect(transport, "guest");
                    ssh.authenticate(new PasswordAuthentication("guest"));
                    System.out.println(String.format("Established connection with mac %s", mac));
                }
                finally {
                    try {
                        transport.close();
                    }
                    catch (Throwable throwable) {}
                    if (ssh == null) continue;
                    ssh.disconnect();
                }
            }
            catch (Throwable e) {
                System.out.println(String.format("Connection failed with mac %s [%s]", mac, e.getMessage()));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void testKexs(List<String> kexs) {
        while (!kexs.isEmpty()) {
            String kex = kexs.remove(0);
            try {
                SshConnector con = SshConnector.createInstance();
                Ssh2Context ctx = (Ssh2Context)con.getContext(2);
                if (!ctx.supportedKeyExchanges().contains(kex)) continue;
                ctx.supportedKeyExchanges().removeAllBut(kex);
                SocketTransport transport = new SocketTransport(hostname, port);
                SshClient ssh = null;
                try {
                    ssh = con.connect(transport, "user1");
                    System.out.println(String.format("Established connection with kex %s", kex));
                }
                finally {
                    try {
                        transport.close();
                    }
                    catch (Throwable throwable) {}
                    if (ssh == null) continue;
                    ssh.disconnect();
                }
            }
            catch (Throwable e) {
                System.out.println(String.format("Connection failed with kex %s [%s]", kex, e.getMessage()));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void testPublicKeys(List<String> keys) {
        while (!keys.isEmpty()) {
            String key = keys.remove(0);
            try {
                SshConnector con = SshConnector.createInstance();
                Ssh2Context ctx = (Ssh2Context)con.getContext(2);
                if (!ctx.supportedPublicKeys().contains(key)) continue;
                ctx.supportedPublicKeys().removeAllBut(key);
                SocketTransport transport = new SocketTransport(hostname, port);
                SshClient ssh = null;
                try {
                    ssh = con.connect(transport, "guest");
                    ssh.authenticate(new PasswordAuthentication("guest"));
                    System.out.println(String.format("Established connection with key %s", key));
                }
                finally {
                    try {
                        transport.close();
                    }
                    catch (Throwable throwable) {}
                    if (ssh == null) continue;
                    ssh.disconnect();
                }
            }
            catch (Throwable e) {
                System.out.println(String.format("Connection failed with key %s [%s]", key, e.getMessage()));
            }
        }
    }

    private static void probeConfigurations(ConfigurationCollector cfg) throws SshException {
        SshConnector con = SshConnector.createInstance();
        Ssh2Context ctx = (Ssh2Context)con.getContext(2);
        for (String cipher : cfg.getSupportedCiphers()) {
            if (!ctx.supportedCiphersCS().contains(cipher)) continue;
            for (String mac : cfg.getSupportedMacs()) {
                if (!ctx.supportedMacsCS().contains(mac)) continue;
                for (String kex : cfg.getSupportedKeyExchanges()) {
                    if (!ctx.supportedKeyExchanges().contains(kex)) continue;
                    for (String key : cfg.getSupportedHostKeys()) {
                        if (!ctx.supportedPublicKeys().contains(key)) continue;
                        for (String comp : cfg.getSupportedCompressions()) {
                            if (!ctx.supportedCompressionsCS().contains(comp)) continue;
                            SshDiagnostics.testConnection(cipher, mac, kex, key, comp);
                        }
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void testConnection(String cipher, String mac, String kex, String key, String comp) {
        try {
            SshConnector con = SshConnector.createInstance();
            Ssh2Context ctx = (Ssh2Context)con.getContext(2);
            if (!ctx.supportedCiphersCS().contains(cipher)) {
                return;
            }
            if (!ctx.supportedMacsCS().contains(mac)) {
                return;
            }
            if (!ctx.supportedKeyExchanges().contains(kex)) {
                return;
            }
            if (!ctx.supportedPublicKeys().contains(key)) {
                return;
            }
            if (!ctx.supportedCompressionsCS().contains(comp)) {
                return;
            }
            ctx.setPreferredCipherCS(cipher);
            ctx.setPreferredCipherSC(cipher);
            ctx.setPreferredMacCS(mac);
            ctx.setPreferredMacSC(mac);
            ctx.setPreferredKeyExchange(kex);
            ctx.setPreferredPublicKey(key);
            ctx.setPreferredCompressionCS(comp);
            ctx.setPreferredCompressionSC(comp);
            SocketTransport transport = new SocketTransport(hostname, port);
            SshClient ssh = null;
            try {
                ssh = con.connect(transport, "guest");
                ssh.authenticate(new PasswordAuthentication("guest"));
                System.out.println(String.format("Established connection with cipher %s Mac %s Kex %s Key %s Compression %s", cipher, mac, kex, key, comp));
            }
            finally {
                try {
                    transport.close();
                }
                catch (Throwable throwable) {}
                if (ssh != null) {
                    ssh.disconnect();
                }
            }
        }
        catch (Throwable e) {
            System.out.println(String.format("Connection failed with cipher %s Mac %s Kex %s Key %s Compression %s [%s]", cipher, mac, kex, key, comp, e.getMessage()));
            e.printStackTrace();
            SshDiagnostics.testIsolatedComponents(cipher, mac, kex, key, comp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void testIsolatedComponents(String cipher, String mac, String kex, String key, String comp) {
        try {
            SshConnector con = SshConnector.createInstance();
            Ssh2Context ctx = (Ssh2Context)con.getContext(2);
            if (!ctx.supportedCiphersCS().contains(cipher)) {
                return;
            }
            if (!ctx.supportedMacsCS().contains(mac)) {
                return;
            }
            if (!ctx.supportedKeyExchanges().contains(kex)) {
                return;
            }
            if (!ctx.supportedPublicKeys().contains(key)) {
                return;
            }
            if (!ctx.supportedCompressionsCS().contains(comp)) {
                return;
            }
            ctx.supportedCiphersCS().removeAllBut(cipher);
            ctx.supportedCiphersSC().removeAllBut(cipher);
            ctx.supportedMacsCS().removeAllBut(mac);
            ctx.supportedMacsSC().removeAllBut(mac);
            ctx.supportedKeyExchanges().removeAllBut(kex);
            ctx.supportedPublicKeys().removeAllBut(key);
            ctx.supportedCompressionsCS().removeAllBut(comp);
            ctx.supportedCompressionsSC().removeAllBut(comp);
            SocketTransport transport = new SocketTransport(hostname, port);
            SshClient ssh = null;
            try {
                ssh = con.connect(transport, "guest");
                ssh.authenticate(new PasswordAuthentication("guest"));
                System.out.println(String.format("Established connection with isolated cipher %s Mac %s Kex %s Key %s Compression %s", cipher, mac, kex, key, comp));
            }
            finally {
                try {
                    transport.close();
                }
                catch (Throwable throwable) {}
                if (ssh != null) {
                    ssh.disconnect();
                }
            }
        }
        catch (Throwable e) {
            System.out.println(String.format("Connection failed with isolated cipher %s Mac %s Kex %s Key %s Compression %s [%s]", cipher, mac, kex, key, comp, e.getMessage()));
        }
    }

    private static void reportNegotiated(String name, ConfigurationCollector cfg) {
        System.out.println(String.format("%s configuration", name));
        System.out.println(String.format("Key exchange: %s", cfg.getNegotiatedKeyExchange()));
        System.out.println(String.format("Host key    : %s", cfg.getNegotiatedHostKey()));
        System.out.println(String.format("Cipher      : %s,%s", cfg.getNegotiatedCipherCS(), cfg.getNegotiatedCipherSC()));
        System.out.println(String.format("Mac         : %s,%s", cfg.getNegotiatedMacCS(), cfg.getNegotiatedMacSC()));
        System.out.println(String.format("Compression : %s,%s", cfg.getNegotiatedCompressionCS(), cfg.getNegotiatedCompressionSC()));
        System.out.println("#####");
    }

    static {
        port = 22;
    }
}

