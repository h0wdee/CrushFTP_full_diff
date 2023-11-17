/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshHmac;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.util.Base64;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

public abstract class AbstractKnownHostsKeyVerification
implements HostKeyVerification {
    private Hashtable<String, Hashtable<String, SshPublicKey>> allowedHosts = new Hashtable();
    private Hashtable<String, Hashtable<String, SshPublicKey>> temporaryHosts = new Hashtable();
    private String knownhosts;
    private boolean hostFileWriteable;
    private boolean hashHosts = true;
    private File knownhostsFile;
    private static final String HASH_MAGIC = "|1|";
    private static final String HASH_DELIM = "|";

    public AbstractKnownHostsKeyVerification() throws SshException {
        this(null);
    }

    public File getKnownHostsFile() {
        return this.knownhostsFile;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public AbstractKnownHostsKeyVerification(String knownhosts) throws SshException {
        InputStream in = null;
        if (knownhosts == null) {
            String homeDir = "";
            try {
                homeDir = System.getProperty("user.home");
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
            this.knownhostsFile = new File(homeDir, ".ssh" + File.separator + "known_hosts");
            knownhosts = this.knownhostsFile.getAbsolutePath();
        } else {
            this.knownhostsFile = new File(knownhosts);
        }
        try {
            if (System.getSecurityManager() != null) {
                System.getSecurityManager().checkRead(knownhosts);
            }
            if (this.knownhostsFile.exists()) {
                String line;
                in = new FileInputStream(this.knownhostsFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                while ((line = reader.readLine()) != null) {
                    if ((line = line.trim()).equals("")) continue;
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    if (!tokens.hasMoreTokens()) {
                        this.onInvalidHostEntry(line);
                        continue;
                    }
                    String host = (String)tokens.nextElement();
                    String algorithm = null;
                    try {
                        if (!tokens.hasMoreTokens()) {
                            this.onInvalidHostEntry(line);
                            continue;
                        }
                        algorithm = (String)tokens.nextElement();
                        Integer.parseInt(algorithm);
                        if (!tokens.hasMoreTokens()) {
                            this.onInvalidHostEntry(line);
                            continue;
                        }
                        String e = (String)tokens.nextElement();
                        if (!tokens.hasMoreTokens()) {
                            this.onInvalidHostEntry(line);
                            continue;
                        }
                        String n = (String)tokens.nextElement();
                        BigInteger publicExponent = new BigInteger(e);
                        BigInteger modulus = new BigInteger(n);
                        SshRsaPublicKey key = ComponentManager.getInstance().createRsaPublicKey(modulus, publicExponent, 1);
                        this.putAllowedKey(host, key, true);
                    }
                    catch (OutOfMemoryError ox) {
                        reader.close();
                        throw new SshException("Error parsing known_hosts file, is your file corrupt? " + this.knownhostsFile.getAbsolutePath(), 17);
                    }
                    catch (NumberFormatException ex) {
                        if (!tokens.hasMoreTokens()) {
                            this.onInvalidHostEntry(line);
                            continue;
                        }
                        if (algorithm.equalsIgnoreCase("@cert-authority") || algorithm.equalsIgnoreCase("@revoked")) continue;
                        String key = (String)tokens.nextElement();
                        try {
                            SshPublicKey pk = SshPublicKeyFileFactory.decodeSSH2PublicKey(algorithm, Base64.decode(key));
                            this.putAllowedKey(host, pk, true);
                        }
                        catch (IOException ex2) {
                            this.onInvalidHostEntry(line);
                        }
                        catch (OutOfMemoryError oex) {
                            reader.close();
                            throw new SshException("Error parsing known_hosts file, is your file corrupt? " + this.knownhostsFile.getAbsolutePath(), 17);
                        }
                    }
                }
                reader.close();
                in.close();
                this.hostFileWriteable = this.knownhostsFile.canWrite();
            } else {
                File parent = new File(this.knownhostsFile.getParent());
                parent.mkdirs();
                FileOutputStream out = new FileOutputStream(this.knownhostsFile);
                out.write(this.toString().getBytes());
                out.close();
                this.hostFileWriteable = true;
            }
            this.knownhosts = knownhosts;
        }
        catch (IOException ioe) {
            this.hostFileWriteable = false;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    public void setHashHosts(boolean hashHosts) {
        this.hashHosts = hashHosts;
    }

    protected void onInvalidHostEntry(String entry) throws SshException {
    }

    public boolean isHostFileWriteable() {
        return this.hostFileWriteable;
    }

    public abstract void onHostKeyMismatch(String var1, SshPublicKey var2, SshPublicKey var3) throws SshException;

    public abstract void onUnknownHost(String var1, SshPublicKey var2) throws SshException;

    public void allowHost(String host, SshPublicKey pk, boolean always) throws SshException {
        if (this.hashHosts && !host.startsWith(HASH_MAGIC)) {
            SshHmac sha1 = (SshHmac)ComponentManager.getInstance().supportedHMacsCS().getInstance("hmac-sha1");
            byte[] hashSalt = new byte[sha1.getMacLength()];
            ComponentManager.getInstance().getRND().nextBytes(hashSalt);
            sha1.init(hashSalt);
            sha1.update(host.getBytes());
            byte[] theHash = sha1.doFinal();
            String names = HASH_MAGIC + Base64.encodeBytes(hashSalt, false) + HASH_DELIM + Base64.encodeBytes(theHash, false);
            this.putAllowedKey(names, pk, always);
        } else {
            this.putAllowedKey(host, pk, always);
        }
        if (always) {
            try {
                this.saveHostFile();
            }
            catch (IOException ex) {
                throw new SshException("knownhosts file could not be saved! " + ex.getMessage(), 5);
            }
        }
    }

    public Hashtable<String, Hashtable<String, SshPublicKey>> allowedHosts() {
        return this.allowedHosts;
    }

    public synchronized void removeAllowedHost(String host) {
        if (this.allowedHosts.containsKey(host)) {
            this.allowedHosts.remove(host);
        }
    }

    @Override
    public boolean verifyHost(String host, SshPublicKey pk) throws SshException {
        return this.verifyHost(host, pk, true);
    }

    private synchronized boolean verifyHost(String host, SshPublicKey pk, boolean validateUnknown) throws SshException {
        String name;
        StringTokenizer tokens;
        String names;
        String fqn = null;
        String ip = null;
        if (AdaptiveConfiguration.getBoolean("knownHosts.enableReverseDNS", true, new String[0])) {
            try {
                InetAddress addr = InetAddress.getByName(host);
                fqn = addr.getHostName();
                ip = addr.getHostAddress();
            }
            catch (UnknownHostException addr) {
                // empty catch block
            }
        }
        Enumeration<String> e = this.allowedHosts.keys();
        while (e.hasMoreElements()) {
            names = e.nextElement();
            if (names.startsWith(HASH_MAGIC)) {
                if (this.checkHash(names, host)) {
                    return this.validateHost(names, pk);
                }
                if (ip != null && this.checkHash(names, ip)) {
                    return this.validateHost(names, pk);
                }
            } else if (names.equals(host)) {
                return this.validateHost(names, pk);
            }
            tokens = new StringTokenizer(names, ",");
            while (tokens.hasMoreElements()) {
                name = (String)tokens.nextElement();
                if (!host.equals(name) && (fqn == null || !name.equals(fqn)) && (ip == null || !name.equals(ip))) continue;
                return this.validateHost(names, pk);
            }
        }
        e = this.temporaryHosts.keys();
        while (e.hasMoreElements()) {
            names = e.nextElement();
            if (names.startsWith(HASH_MAGIC)) {
                if (this.checkHash(names, host)) {
                    return this.validateHost(names, pk);
                }
                if (ip != null && this.checkHash(names, ip)) {
                    return this.validateHost(names, pk);
                }
            } else if (names.equals(host)) {
                return this.validateHost(names, pk);
            }
            tokens = new StringTokenizer(names, ",");
            while (tokens.hasMoreElements()) {
                name = (String)tokens.nextElement();
                if (!host.equals(name) && (fqn == null || !name.equals(fqn)) && (ip == null || !name.equals(ip))) continue;
                return this.validateHost(names, pk);
            }
        }
        if (!validateUnknown) {
            return false;
        }
        this.onUnknownHost(host, pk);
        return this.verifyHost(host, pk, false);
    }

    private boolean checkHash(String names, String host) throws SshException {
        SshHmac sha1 = (SshHmac)ComponentManager.getInstance().supportedHMacsCS().getInstance("hmac-sha1");
        String hashData = names.substring(HASH_MAGIC.length());
        String hashSalt = hashData.substring(0, hashData.indexOf(HASH_DELIM));
        String hashStr = hashData.substring(hashData.indexOf(HASH_DELIM) + 1);
        byte[] theHash = Base64.decode(hashStr);
        sha1.init(Base64.decode(hashSalt));
        sha1.update(host.getBytes());
        byte[] ourHash = sha1.doFinal();
        return Arrays.equals(theHash, ourHash);
    }

    private boolean validateHost(String names, SshPublicKey pk) throws SshException {
        SshPublicKey pub = this.getAllowedKey(names, pk.getAlgorithm());
        if (pub != null && pk.equals(pub)) {
            return true;
        }
        if (pub == null) {
            this.onUnknownHost(names, pk);
        } else {
            this.onHostKeyMismatch(names, pub, pk);
        }
        return this.checkKey(names, pk);
    }

    private boolean checkKey(String host, SshPublicKey key) {
        SshPublicKey pk = this.getAllowedKey(host, key.getAlgorithm());
        return pk != null && pk.equals(key);
    }

    private synchronized SshPublicKey getAllowedKey(String names, String algorithm) {
        Hashtable<String, SshPublicKey> map;
        try {
            for (String name : this.temporaryHosts.keySet()) {
                if (!name.startsWith(HASH_DELIM) || !this.checkHash(name, names)) continue;
                Hashtable<String, SshPublicKey> map2 = this.temporaryHosts.get(name);
                return map2.get(algorithm);
            }
        }
        catch (SshException it) {
            // empty catch block
        }
        if (this.temporaryHosts.containsKey(names)) {
            map = this.temporaryHosts.get(names);
            return map.get(algorithm);
        }
        try {
            for (String name : this.allowedHosts.keySet()) {
                if (!name.startsWith(HASH_DELIM) || !this.checkHash(name, names)) continue;
                Hashtable<String, SshPublicKey> map3 = this.allowedHosts.get(name);
                return map3.get(algorithm);
            }
        }
        catch (SshException it) {
            // empty catch block
        }
        if (this.allowedHosts.containsKey(names)) {
            map = this.allowedHosts.get(names);
            return map.get(algorithm);
        }
        return null;
    }

    private synchronized void putAllowedKey(String host, SshPublicKey key, boolean always) {
        if (always) {
            if (!this.allowedHosts.containsKey(host)) {
                this.allowedHosts.put(host, new Hashtable());
            }
            Hashtable<String, SshPublicKey> keys = this.allowedHosts.get(host);
            keys.put(key.getAlgorithm(), key);
        } else {
            if (!this.temporaryHosts.containsKey(host)) {
                this.temporaryHosts.put(host, new Hashtable());
            }
            Hashtable<String, SshPublicKey> keys = this.temporaryHosts.get(host);
            keys.put(key.getAlgorithm(), key);
        }
    }

    public synchronized void saveHostFile() throws IOException {
        if (!this.hostFileWriteable) {
            throw new IOException("Host file is not writeable.");
        }
        try {
            File f = new File(this.knownhosts);
            FileOutputStream out = new FileOutputStream(f);
            out.write(this.toString().getBytes());
            out.close();
        }
        catch (IOException e) {
            throw new IOException("Could not write to " + this.knownhosts);
        }
    }

    public String toString() {
        StringBuffer knownhostsBuf = new StringBuffer("");
        String eol = System.getProperty("line.separator");
        Enumeration<String> e = this.allowedHosts.keys();
        while (e.hasMoreElements()) {
            String host = e.nextElement();
            Hashtable<String, SshPublicKey> table = this.allowedHosts.get(host);
            Enumeration<String> e2 = table.keys();
            while (e2.hasMoreElements()) {
                String type = e2.nextElement();
                SshPublicKey pk = table.get(type);
                if (pk instanceof SshRsaPublicKey && ((SshRsaPublicKey)pk).getVersion() == 1) {
                    SshRsaPublicKey ssh1 = (SshRsaPublicKey)pk;
                    knownhostsBuf.append(host + " " + String.valueOf(ssh1.getModulus().bitLength()) + " " + ssh1.getPublicExponent() + " " + ssh1.getModulus() + eol);
                    continue;
                }
                try {
                    knownhostsBuf.append(host + " " + pk.getAlgorithm() + " " + Base64.encodeBytes(pk.getEncoded(), true) + eol);
                }
                catch (SshException sshException) {}
            }
        }
        return knownhostsBuf.toString();
    }
}

