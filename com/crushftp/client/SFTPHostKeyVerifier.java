/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.ssh.HostKeyVerification
 *  com.maverick.ssh.SshException
 *  com.maverick.ssh.components.SshPublicKey
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;

public class SFTPHostKeyVerifier
implements HostKeyVerification {
    public static Object knownHostLock = new Object();
    boolean verifyHost = false;
    boolean addNewHost = false;
    String knownHostFile = "";
    String true_host = null;

    public SFTPHostKeyVerifier(String knownHostFile, boolean verifyHost, boolean addNewHost) {
        this.knownHostFile = knownHostFile;
        this.verifyHost = verifyHost;
        this.addNewHost = addNewHost;
    }

    public void setHost(String true_host) {
        this.true_host = true_host;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean verifyHost(String host, SshPublicKey key) throws SshException {
        if (!this.verifyHost) return true;
        if (this.knownHostFile == null) {
            return true;
        }
        Object object = knownHostLock;
        synchronized (object) {
            block40: {
                if (this.true_host != null) {
                    host = this.true_host;
                }
                if (host.indexOf("~") >= 0) {
                    host = host.substring(host.indexOf("~") + 1);
                }
                BufferedReader br = null;
                try {
                    if (!new File_S(this.knownHostFile).exists()) {
                        new FileOutputStream(new File_S(this.knownHostFile)).close();
                    }
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(new File_S(this.knownHostFile))));
                    String line = "";
                    Common.log("SSH_CLIENT", 2, "Checking known host file:" + this.knownHostFile);
                    while ((line = br.readLine()) != null) {
                        StringTokenizer st = new StringTokenizer(line);
                        String[] vhosts = st.nextElement().toString().split(",");
                        boolean hostMatch = false;
                        int x = 0;
                        while (x < vhosts.length && !hostMatch) {
                            String host2 = vhosts[x];
                            if (host2.indexOf("[") >= 0 && host2.indexOf("]") >= host2.indexOf("[") && host.indexOf("[") < 0 && host.indexOf("]") < 0) {
                                if ((host2 = host2.substring(host2.indexOf("[") + 1, host2.indexOf("]")).trim()).equalsIgnoreCase(host.trim())) {
                                    hostMatch = true;
                                }
                            } else if (host2.trim().equalsIgnoreCase(host.trim())) {
                                hostMatch = true;
                            }
                            ++x;
                        }
                        if (!hostMatch) continue;
                        Common.log("SSH_CLIENT", 2, "Found host match:" + this.knownHostFile + ":" + line);
                        if (!key.getAlgorithm().trim().equalsIgnoreCase(st.nextToken().toString().trim())) continue;
                        Common.log("SSH_CLIENT", 2, "Found alg match:" + this.knownHostFile + ":" + key);
                        if (key.getFingerprint().trim().equalsIgnoreCase(st.nextToken().toString().trim())) {
                            Common.log("SSH_CLIENT", 0, "Found match:" + this.knownHostFile + ":" + line);
                            return true;
                        }
                        Common.log("SSH_CLIENT", 0, "Found mismatch:" + this.knownHostFile + ":" + line);
                        return false;
                    }
                    br.close();
                    br = null;
                    Common.log("SSH_CLIENT", 2, "No matches:" + this.knownHostFile);
                    if (!this.addNewHost) return false;
                    Common.log("SSH_CLIENT", 0, "No matches:" + this.knownHostFile + ": Adding new entry.");
                    try (RandomAccessFile out = new RandomAccessFile(new File_S(this.knownHostFile), "rw");){
                        if (out.length() > 0L) {
                            out.seek(out.length() - 1L);
                            byte b = out.readByte();
                            if (b != 10 && b != 13) {
                                out.write("\r\n".getBytes());
                            }
                        }
                        line = String.valueOf(host) + " " + key.getAlgorithm().trim() + " " + key.getFingerprint();
                        out.write((String.valueOf(line) + "\r\n").getBytes());
                        Common.log("SSH_CLIENT", 0, "No matches:" + this.knownHostFile + ": Adding new entry:" + line);
                    }
                }
                catch (Exception e) {
                    Common.log("SSH_CLIENT", 1, e);
                    break block40;
                }
                finally {
                    if (br != null) {
                        try {
                            br.close();
                        }
                        catch (IOException iOException) {}
                    }
                }
                return true;
            }
            return false;
        }
    }
}

