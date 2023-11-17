/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.sftp.SftpStatusException;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;
import com.maverick.util.IOUtil;
import com.sshtools.net.SocketTransport;
import com.sshtools.sftp.SftpClient;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SshTests {
    public static void main(String[] args) throws IOException, SshException {
        if (args.length < 7) {
            System.out.println("Not enough arguments");
            return;
        }
        long time = TimeUnit.MINUTES.toMillis(Long.parseLong(args[1]));
        String hostname = args[2];
        int port = Integer.parseInt(args[3]);
        String username = args[4];
        String password = args[5];
        String tag = args[6];
        switch (args[0]) {
            case "load-balancer": {
                System.out.println("Starting load balancer ping test");
                SshTests.runLoadBalancerTest(time, hostname, port);
                break;
            }
            case "performance": {
                System.out.println("Starting performance test");
                SshTests.runPerformanceTest(time, hostname, port, username, password, tag);
                break;
            }
            case "random": {
                System.out.println("Starting random file size transfer test");
                SshTests.runRandomTest(time, hostname, port, username, password, tag);
                break;
            }
            case "idle": {
                System.out.println("Starting SFTP idle test");
                SshTests.runSftpIdleTest(time, hostname, port, username, password);
                break;
            }
            case "corrupt": {
                System.out.println("Starting resilience test");
                SshTests.runTransportResilience(time, hostname, port, username, password, tag);
                break;
            }
            default: {
                System.out.println("Unsupported test name " + args[0]);
            }
        }
    }

    private static void runTransportResilience(long time, String hostname, int port, String username, String password, String tag) {
        long started = System.currentTimeMillis();
        while (System.currentTimeMillis() - started < time) {
            try {
                SshTests.runCorruptTransport(hostname, port, username, password, tag);
            }
            catch (SshException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void runCorruptTransport(String hostname, int port, String username, String password, String tag) throws UnknownHostException, IOException, SshException {
        SshClient ssh = SshTests.createSshClient(new CorruptedSocketTransport(hostname, port), username, password);
        File testFile = SshTests.createFile(String.format("corrupt-transfer-%s.dat", tag), IOUtil.fromByteSize(System.getProperty("test.largeFileSize", "500MB")));
        try {
            SftpClient sftp = new SftpClient(ssh);
            for (int i = 0; i < 100; ++i) {
                sftp.put(testFile.getAbsolutePath());
                sftp.get(testFile.getName(), testFile.getName() + ".download");
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            testFile.delete();
            ssh.disconnect();
        }
    }

    private static void runSftpIdleTest(long time, String hostname, int port, String username, String password) throws IOException, SshException {
        long started = System.currentTimeMillis();
        while (System.currentTimeMillis() - started < time) {
            try {
                SftpClient sftp = SshTests.createSftpClient(hostname, port, username, password);
                sftp.ls(1000);
                while (!sftp.isClosed()) {
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }
            catch (Throwable sftp) {
                // empty catch block
            }
            try {
                Thread.sleep(Long.parseLong(System.getProperty("test.inactive.ms", "5000")));
            }
            catch (InterruptedException e) {
                System.err.println("Thread interrupted");
                break;
            }
        }
    }

    private static SshClient createSshClient(String hostname, int port, String username, String password) throws SshException, IOException {
        return SshTests.createSshClient(new SocketTransport(hostname, port), username, password);
    }

    private static SshClient createSshClient(SocketTransport socket, String username, String password) throws SshException {
        SshConnector con = SshConnector.createInstance(SecurityLevel.valueOf(System.getProperty("test.securityLevel", SecurityLevel.STRONG.name())), true);
        SshClient ssh = con.connect((SshTransport)socket, username, true);
        ssh.authenticate(new PasswordAuthentication(password));
        return ssh;
    }

    private static SftpClient createSftpClient(String hostname, int port, String username, String password) throws SshException, IOException, SftpStatusException, ChannelOpenException {
        return new SftpClient(SshTests.createSshClient(hostname, port, username, password));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void runPerformanceTest(long time, String hostname, int port, String username, String password, String tag) throws IOException, SshException {
        File file = new File(String.format("%s-performance.csv", tag));
        FileOutputStream out = new FileOutputStream(file);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));){
            writer.write(String.format("Opened,Started,Total Time,Success,Connection Time,Upload Time,Upload Throughput,Download Time,Download Throughout\n", new Object[0]));
            writer.flush();
            File testFile = SshTests.createFile(String.format("performance-%s.dat", tag), IOUtil.fromByteSize(System.getProperty("test.largeFileSize", "500MB")));
            long started = System.currentTimeMillis();
            while (System.currentTimeMillis() - started < time) {
                boolean success = false;
                long open = System.currentTimeMillis();
                long connected = -1L;
                long total = -1L;
                long put = -1L;
                long get = -1L;
                double putSeconds = -1.0;
                double getSeconds = -1.0;
                double putRate = -1.0;
                double getRate = -1.0;
                SshClient ssh = SshTests.createSshClient(hostname, port, username, password);
                try {
                    SftpClient sftp = new SftpClient(ssh);
                    connected = System.currentTimeMillis();
                    put = System.currentTimeMillis();
                    sftp.put(testFile.getAbsolutePath());
                    put = System.currentTimeMillis() - put;
                    long fileSize = testFile.length();
                    putSeconds = (double)put / 1000.0;
                    putRate = (double)fileSize / putSeconds / 1000.0 / 1000.0;
                    System.out.println(String.format("Upload transferred at %.2fMB/s", putRate));
                    get = System.currentTimeMillis();
                    sftp.get(testFile.getName(), testFile.getName() + ".download");
                    get = System.currentTimeMillis() - get;
                    total = System.currentTimeMillis() - open;
                    success = true;
                    getSeconds = (double)get / 1000.0;
                    getRate = (double)fileSize / getSeconds / 1000.0 / 1000.0;
                    System.out.println(String.format("Download transferred at %.2fMB/s", getRate));
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
                finally {
                    ssh.disconnect();
                }
                try {
                    writer.write(String.format("%d,%d,%d,%s,%d,%.2f,%.2f,%.2f,%.2f\n", open, open - started, total, success ? "SUCCESS" : "FAILURE", connected - open, putSeconds, putRate, getSeconds, getRate));
                    writer.flush();
                }
                catch (IOException e1) {
                    System.err.println("Failed to write to stats file");
                    e1.printStackTrace();
                    break;
                }
            }
        }
        finally {
            try {
                out.flush();
            }
            catch (IOException iOException) {}
            IOUtil.closeStream(out);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void runRandomTest(long time, String hostname, int port, String username, String password, String tag) throws IOException, SshException {
        File file = new File(String.format("%s-random.csv", tag));
        FileOutputStream out = new FileOutputStream(file);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));){
            writer.write(String.format("Opened,Started,Total Time,Success,Connection Time,Upload Time,Upload Throughput,Download Time,Download Throughout\n", new Object[0]));
            writer.flush();
            int count = 0;
            long started = System.currentTimeMillis();
            while (System.currentTimeMillis() - started < time) {
                long size = ThreadLocalRandom.current().nextLong(IOUtil.fromByteSize(System.getProperty("test.minFileSize", "4KB")), IOUtil.fromByteSize(System.getProperty("test.maxFileSize", "50MB")));
                File testFile = SshTests.createFile(String.format("random-%s-%d-%d.dat", tag, count++, size), size);
                File testFileDownload = new File(testFile.getAbsolutePath() + ".download");
                boolean success = false;
                long open = System.currentTimeMillis();
                long connected = -1L;
                long total = -1L;
                long put = -1L;
                long get = -1L;
                double putSeconds = -1.0;
                double getSeconds = -1.0;
                double putRate = -1.0;
                double getRate = -1.0;
                SshClient ssh = SshTests.createSshClient(hostname, port, username, password);
                try {
                    SftpClient sftp = new SftpClient(ssh);
                    connected = System.currentTimeMillis();
                    put = System.currentTimeMillis();
                    sftp.put(testFile.getAbsolutePath());
                    put = System.currentTimeMillis() - put;
                    long fileSize = testFile.length();
                    putSeconds = (double)put / 1000.0;
                    putRate = (double)fileSize / putSeconds / 1000.0 / 1000.0;
                    System.out.println(String.format("Upload transferred at %.2fMB/s", putRate));
                    get = System.currentTimeMillis();
                    sftp.get(testFile.getName(), testFileDownload.getAbsolutePath());
                    get = System.currentTimeMillis() - get;
                    total = System.currentTimeMillis() - open;
                    success = true;
                    getSeconds = (double)get / 1000.0;
                    getRate = (double)fileSize / getSeconds / 1000.0 / 1000.0;
                    System.out.println(String.format("Download transferred at %.2fMB/s", getRate));
                    sftp.rm(testFile.getName());
                }
                catch (Throwable sftp) {
                }
                finally {
                    ssh.disconnect();
                    testFile.delete();
                    testFileDownload.delete();
                }
                try {
                    writer.write(String.format("%d,%d,%d,%d,%s,%d,%.2f,%.2f,%.2f,%.2f\n", open, open - started, total, size, success ? "SUCCESS" : "FAILURE", connected - open, putSeconds, putRate, getSeconds, getRate));
                    writer.flush();
                }
                catch (IOException e1) {
                    System.err.println("Failed to write to stats file");
                    e1.printStackTrace();
                    break;
                }
            }
        }
        finally {
            try {
                out.flush();
            }
            catch (IOException iOException) {}
            IOUtil.closeStream(out);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void runReactionTest(long time, String hostname, int port, String username, String password, String tag) throws IOException, SshException {
        File file = new File(String.format("%-reaction.csv", tag));
        FileOutputStream out = new FileOutputStream(file);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));){
            File testFile = SshTests.createFile(String.format("reaction-%s.dat", tag), IOUtil.fromByteSize("4KB"));
            long started = System.currentTimeMillis();
            while (System.currentTimeMillis() - started < time) {
                boolean success = false;
                long open = System.currentTimeMillis();
                long connected = -1L;
                long total = -1L;
                long put = -1L;
                long get = -1L;
                SshClient ssh = SshTests.createSshClient(hostname, port, username, password);
                try {
                    SftpClient sftp = new SftpClient(ssh);
                    connected = System.currentTimeMillis();
                    put = System.currentTimeMillis();
                    sftp.put(testFile.getAbsolutePath());
                    put = System.currentTimeMillis() - put;
                    get = System.currentTimeMillis();
                    sftp.get(testFile.getName(), testFile.getName() + ".download");
                    get = System.currentTimeMillis() - get;
                    total = System.currentTimeMillis() - open;
                    success = true;
                }
                catch (Throwable sftp) {
                }
                finally {
                    ssh.disconnect();
                }
                try {
                    String msg = String.format("%d,%d,%d,%s,%d\n", open, open - started, total, success ? "SUCCESS" : "FAILURE", connected - open);
                    writer.write(msg);
                    writer.flush();
                    Thread.sleep(10000L);
                }
                catch (InterruptedException e) {
                    System.err.println("Thread interrupted");
                    break;
                }
                catch (IOException e1) {
                    System.err.println("Failed to write to stats file");
                    e1.printStackTrace();
                    break;
                }
            }
        }
        finally {
            try {
                out.flush();
            }
            catch (IOException iOException) {}
            IOUtil.closeStream(out);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static File createFile(String name, Long numOfBytes) throws IOException {
        File file = new File(name);
        FileOutputStream out = new FileOutputStream(file);
        try {
            Random r = new Random();
            byte[] tmp = new byte[32678];
            for (long count = 0L; count < numOfBytes; count += (long)tmp.length) {
                r.nextBytes(tmp);
                out.write(tmp);
            }
        }
        finally {
            IOUtil.closeStream(out);
        }
        return file;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void runLoadBalancerTest(long time, String hostname, int port) throws IOException {
        File file = new File("load-balancer.csv");
        FileOutputStream out = new FileOutputStream(file);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));){
            long started = System.currentTimeMillis();
            while (System.currentTimeMillis() - started < time) {
                boolean success = false;
                long open = System.currentTimeMillis();
                long connected = -1L;
                try {
                    Socket sock = new Socket(hostname, port);
                    sock.getInputStream().read(new byte[256]);
                    success = true;
                    sock.close();
                }
                catch (Throwable sock) {
                    // empty catch block
                }
                connected = System.currentTimeMillis();
                try {
                    String msg = String.format("%d,%d,%s,%d\n", open, open - started, success ? "SUCCESS" : "FAILURE", connected - open);
                    writer.write(msg);
                    writer.flush();
                    Thread.sleep(5000L);
                }
                catch (InterruptedException e) {
                    System.err.println("Thread interrupted");
                    break;
                }
                catch (IOException e1) {
                    System.err.println("Failed to write to stats file");
                    e1.printStackTrace();
                    break;
                }
            }
        }
        finally {
            try {
                out.flush();
            }
            catch (IOException iOException) {}
            IOUtil.closeStream(out);
        }
    }

    static class CorruptedOutputStream
    extends OutputStream {
        OutputStream out;
        long corruptAtPosition;
        Random r = new Random();

        CorruptedOutputStream(OutputStream out, long corruptAtPosition) {
            this.out = out;
            this.corruptAtPosition = corruptAtPosition;
        }

        @Override
        public void write(int b) throws IOException {
            if (this.corruptAtPosition <= 0L) {
                this.out.write((byte)this.r.nextInt());
            } else {
                this.out.write(b);
            }
            --this.corruptAtPosition;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (this.corruptAtPosition <= 0L) {
                byte[] tmp = new byte[len];
                this.r.nextBytes(tmp);
                this.out.write(tmp);
            } else {
                this.out.write(b, off, len);
            }
            this.corruptAtPosition -= (long)len;
        }
    }

    static class CorruptedSocketTransport
    extends SocketTransport {
        @Override
        public OutputStream getOutputStream() throws IOException {
            Random r = new Random();
            return new CorruptedOutputStream(super.getOutputStream(), r.nextInt(131072));
        }

        public CorruptedSocketTransport(String hostname, int port) throws IOException {
            super(hostname, port);
        }
    }
}

