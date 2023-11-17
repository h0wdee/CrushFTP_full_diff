/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.sftp.SftpFileAttributes;
import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.TransferCancelledException;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.ConfigurationCollector;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshCompatibilityUtils;
import com.maverick.ssh.SshException;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.util.IOUtil;
import com.sshtools.scp.ScpClient;
import com.sshtools.sftp.SftpClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class SshReporter {
    public static void main(String[] args) throws SshException, IOException, SftpStatusException, ChannelOpenException, TransferCancelledException {
        String password;
        String username;
        String hostname;
        int port = 22;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        if (args.length >= 4) {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
            username = args[2];
            password = args[3];
        } else {
            System.out.print("Hostname: ");
            hostname = reader.readLine();
            int idx = hostname.indexOf(":");
            if (idx > -1) {
                hostname = hostname.substring(0, idx);
                port = Integer.parseInt(hostname.substring(idx + 1));
            }
            System.out.print("Username: ");
            username = reader.readLine();
            System.out.print("Password: ");
            password = reader.readLine();
        }
        ConfigurationCollector cfg = SshCompatibilityUtils.getRemoteConfiguration(hostname, port);
        System.out.println(String.format("%s on port %d identifies as %s", hostname, port, cfg.getRemoteIdentification()));
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
        SshReporter.reportNegotiated("Default", cfg);
        String size = "250MB";
        SshReporter.generateLargeFile("file.dat", size);
        SshReporter.probeSFTP(SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, true));
        SshReporter.reportSFTP("SFTP with TCP No Delay 32k blocksize with 16 max requests", "file.dat", size, 32768, 16, SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, true));
        SshReporter.reportSFTP("SFTP 32k blocksize with 16 max requests", "file.dat", size, 32768, 16, SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, false));
        SshReporter.reportSFTP("SFTP with TCP No Delay 16k blocksize with 16 max requests", "file.dat", size, 16384, 16, SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, true));
        SshReporter.reportSFTP("SFTP 16k blocksize with 16 max requests", "file.dat", size, 16384, 16, SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, false));
        SshReporter.reportSFTP("SFTP with TCP No Delay 8k blocksize with 16 max requests", "file.dat", size, 8192, 16, SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, true));
        SshReporter.reportSFTP("SFTP 8k blocksize with 16 max requests", "file.dat", size, 8192, 16, SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, false));
        SshReporter.reportSCP("SCP with TCP No Delay", "file.dat", size, SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, true));
        SshReporter.reportSCP("SCP", "file.dat", size, SshCompatibilityUtils.getRemoteClient(hostname, port, username, password, false));
        System.exit(0);
    }

    private static void probeSFTP(SshClient ssh) throws SftpStatusException, SshException, ChannelOpenException {
        SftpClient sftp = new SftpClient(ssh);
        System.out.println("##### SFTP Configuration");
        System.out.println("Local window: " + sftp.getSubsystemChannel().getMaximumLocalWindowSize());
        System.out.println("Local packet: " + sftp.getSubsystemChannel().getMaximumLocalPacketLength());
        System.out.println("Remote window: " + sftp.getSubsystemChannel().getMaximumRemoteWindowSize());
        System.out.println("Remote packet: " + sftp.getSubsystemChannel().getMaximumRemotePacketLength());
        System.out.println("CipherCS: " + ((Ssh2Client)ssh).getCipherInUseCS());
        System.out.println("CipherSC: " + ((Ssh2Client)ssh).getCipherInUseSC());
        System.out.println("MacCS: " + ((Ssh2Client)ssh).getMacInUseCS());
        System.out.println("MacSC: " + ((Ssh2Client)ssh).getMacInUseSC());
        System.out.println("#####");
        ssh.disconnect();
    }

    private static void reportSCP(String testName, String filename, String size, SshClient ssh) throws SftpStatusException, SshException, ChannelOpenException, FileNotFoundException, TransferCancelledException {
        System.out.println("##### " + testName);
        ScpClient scp = new ScpClient(ssh);
        File local = new File(System.getProperty("user.dir"), filename);
        System.out.println("Uploading " + size + " File");
        long started = System.currentTimeMillis();
        scp.put(local.getAbsolutePath(), filename, false);
        long ended = System.currentTimeMillis();
        System.out.println("Upload took " + (double)(ended - started) / 1000.0 + " seconds");
        System.out.println("Downloading " + size + " File");
        started = System.currentTimeMillis();
        scp.get(local.getAbsolutePath(), filename, false);
        ended = System.currentTimeMillis();
        System.out.println("Download took " + (double)(ended - started) / 1000.0 + " seconds");
        ssh.disconnect();
        System.out.println("#####");
    }

    private static void reportSFTP(String testName, String filename, String size, int blocksize, int maxRequests, SshClient ssh) throws SftpStatusException, SshException, ChannelOpenException, FileNotFoundException, TransferCancelledException {
        System.out.println("##### " + testName);
        SftpClient sftp = new SftpClient(ssh);
        System.out.println("Block size: " + blocksize);
        System.out.println("Max Requests: " + maxRequests);
        sftp.setBlockSize(blocksize);
        sftp.lcd(System.getProperty("user.dir"));
        File original = new File(filename);
        String uploadFile = filename + ".upload";
        System.out.println("Uploading " + size + " File");
        long started = System.currentTimeMillis();
        sftp.put(filename, uploadFile);
        long ended = System.currentTimeMillis();
        System.out.println("Upload took " + (double)(ended - started) / 1000.0 + " seconds");
        SftpFileAttributes attrs = sftp.stat(uploadFile);
        System.out.println("Transferred file size " + (original.length() == attrs.getSize().longValue() ? "MATCHES" : " DOES NOT MATCH"));
        started = System.currentTimeMillis();
        sftp.get(uploadFile, filename + ".download");
        ended = System.currentTimeMillis();
        System.out.println("Download took " + (double)(ended - started) / 1000.0 + " seconds");
        File transferred = new File(filename + ".download");
        System.out.println("Transferred file size " + (original.length() == transferred.length() ? "MATCHES" : " DOES NOT MATCH"));
        ssh.disconnect();
        System.out.println("Optimized Block: " + System.getProperty("maverick.optimizedBlock"));
        System.out.println("Final Block: " + System.getProperty("maverick.finalBlock"));
        System.out.println("Round Trip: " + System.getProperty("maverick.blockRoundtrip"));
        System.out.println("#####");
    }

    private static void generateLargeFile(String name, String size) throws IOException {
        System.out.println("Generating " + size + " file");
        File f = new File(name);
        byte[] tmp = new byte[32768];
        new Random().nextBytes(tmp);
        long s = IOUtil.fromByteSize(size);
        try (FileOutputStream out = new FileOutputStream(f);){
            int i = 0;
            while ((long)i < s) {
                out.write(tmp);
                out.flush();
                i += tmp.length;
            }
        }
        System.out.println("#####");
        System.out.println("Test file is " + f.length() + " bytes");
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
}

