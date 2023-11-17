/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.publickey;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.HostKeyUpdater;
import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshHmac;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh.components.jce.OpenSshCertificate;
import com.maverick.ssh.components.jce.Ssh1RsaPublicKey;
import com.maverick.util.Base64;
import com.maverick.util.SshKeyUtils;
import com.sshtools.publickey.HostKeyVerificationListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnownHostsKeyVerification
implements HostKeyVerification,
HostKeyUpdater {
    private static Logger log = LoggerFactory.getLogger(KnownHostsKeyVerification.class);
    LinkedList<HostFileEntry> entries = new LinkedList();
    Set<KeyEntry> keyEntries = new LinkedHashSet<KeyEntry>();
    Set<KeyEntry> revokedEntries = new LinkedHashSet<KeyEntry>();
    Map<SshPublicKey, List<KeyEntry>> entriesByPublicKey = new HashMap<SshPublicKey, List<KeyEntry>>();
    List<CertAuthorityEntry> certificateAuthorities = new ArrayList<CertAuthorityEntry>();
    List<HostKeyVerificationListener> listeners = new ArrayList<HostKeyVerificationListener>();
    private boolean hashHosts = false;
    private boolean useCanonicalHostname;
    private boolean useReverseDNS = this.useCanonicalHostname = AdaptiveConfiguration.getBoolean("knownHosts.enableReverseDNS", true, new String[0]);
    private static final String HASH_MAGIC = "|1|";
    private static final String HASH_DELIM = "|";
    Pattern nonStandard = Pattern.compile("\\[([^\\]]+)\\]:([\\d]{1,5})");

    public KnownHostsKeyVerification(InputStream in) throws SshException, IOException {
        this.load(in);
    }

    public KnownHostsKeyVerification(String knownhosts) throws SshException, IOException {
        this.load(new ByteArrayInputStream(Utils.getUTF8Bytes(knownhosts)));
    }

    public KnownHostsKeyVerification() {
    }

    public void addListener(HostKeyVerificationListener listener) {
        this.listeners.add(listener);
    }

    public synchronized void clear() {
        if (log.isDebugEnabled()) {
            log.debug("Clearing known hosts");
        }
        this.entries.clear();
        this.keyEntries.clear();
        this.revokedEntries.clear();
        this.entriesByPublicKey.clear();
        this.certificateAuthorities.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public synchronized void load(InputStream in) throws SshException, IOException {
        this.clear();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        block7: while (true) {
            String line;
            while ((line = reader.readLine()) != null) {
                if ((line = line.trim()).equals("")) {
                    this.entries.add(new BlankEntry());
                    continue;
                }
                if (line.startsWith("#")) {
                    this.entries.add(new CommentEntry(line.substring(1)));
                    continue;
                }
                StringTokenizer tokens = new StringTokenizer(line, " ");
                if (!tokens.hasMoreTokens()) {
                    this.addInvalidEntry(line);
                    continue;
                }
                String host = (String)tokens.nextElement();
                String marker = "";
                if (host.startsWith("@")) {
                    marker = host;
                    host = (String)tokens.nextElement();
                }
                String algorithm = null;
                try {
                    if (!tokens.hasMoreTokens()) {
                        this.addInvalidEntry(line);
                        continue;
                    }
                    algorithm = tokens.nextToken();
                    if (this.loadSsh1PublicKey(host, algorithm, tokens, line)) continue block7;
                    if (!tokens.hasMoreTokens()) {
                        this.addInvalidEntry(line);
                        continue;
                    }
                    SshPublicKey key = SshKeyUtils.getPublicKey(algorithm + " " + tokens.nextToken());
                    StringBuffer comment = new StringBuffer();
                    while (tokens.hasMoreTokens()) {
                        if (comment.length() > 0) {
                            comment.append(" ");
                        }
                        comment.append(tokens.nextToken());
                    }
                    this.loadSsh2PublicKey(host, marker, algorithm, key, comment.toString());
                    continue block7;
                }
                catch (IOException e) {
                    this.addInvalidEntry(line);
                }
                catch (SshException e) {
                    this.addInvalidEntry(line);
                }
                catch (OutOfMemoryError ox) {
                    reader.close();
                    throw new SshException("Error parsing known_hosts file, is your file corrupt?", 17);
                    return;
                }
            }
        }
        finally {
            reader.close();
            in.close();
        }
    }

    private void addInvalidEntry(String line) {
        if (log.isDebugEnabled()) {
            log.debug("Encountered invalid entry {}", (Object)line);
        }
        this.entries.add(new InvalidEntry(line));
        for (HostKeyVerificationListener listener : this.listeners) {
            try {
                listener.onInvalidHostEntry(line);
            }
            catch (Throwable throwable) {}
        }
        try {
            this.onInvalidHostEntry(line);
        }
        catch (SshException sshException) {
            // empty catch block
        }
    }

    private Set<String> getNames(String host) {
        return new LinkedHashSet<String>(Arrays.asList(host.split(",")));
    }

    private void loadSsh2PublicKey(String host, String marker, String algorithm, SshPublicKey key, String comment) throws SshException {
        KeyEntry entry;
        if (marker.equalsIgnoreCase("@cert-authority")) {
            CertAuthorityEntry e = new CertAuthorityEntry(this.getNames(host), key, comment);
            this.certificateAuthorities.add(e);
            entry = e;
        } else {
            entry = marker.equalsIgnoreCase("@revoked") ? new RevokedEntry(this.getNames(host), new Ssh2KeyEntry(this.getNames(host), key, comment)) : new Ssh2KeyEntry(this.getNames(host), key, comment);
        }
        this.addEntry(entry);
    }

    private void addEntry(KeyEntry entry) {
        if (log.isDebugEnabled()) {
            log.debug("Adding known host {} {} {}", new Object[]{entry.getNames(), entry.getKey().getAlgorithm(), SshKeyUtils.getFingerprint(entry.getKey())});
        }
        if (!this.entriesByPublicKey.containsKey(entry.getKey())) {
            this.entriesByPublicKey.put(entry.getKey(), new ArrayList());
        }
        this.entries.add(entry);
        this.entriesByPublicKey.get(entry.getKey()).add(entry);
        if (entry instanceof KeyEntry) {
            this.keyEntries.add(entry);
        }
        if (entry instanceof RevokedEntry) {
            this.revokedEntries.add(entry);
        }
        this.onHostKeyAdded(this.getNames(entry.getNames()), entry.getKey());
    }

    protected void onHostKeyAdded(Set<String> names, SshPublicKey key) {
    }

    public synchronized void setComment(KeyEntry entry, String comment) {
        if (!this.keyEntries.contains(entry)) {
            throw new IllegalArgumentException("KeyEntry provided is no longer in this known_hosts file.");
        }
        entry.comment = comment;
    }

    private boolean loadSsh1PublicKey(String host, String algorithm, StringTokenizer tokens, String line) throws SshException {
        if (!algorithm.matches("[0-9]+")) {
            return false;
        }
        if (!tokens.hasMoreTokens()) {
            this.addInvalidEntry(line);
            return true;
        }
        String e = (String)tokens.nextElement();
        if (!tokens.hasMoreTokens()) {
            this.addInvalidEntry(line);
            return true;
        }
        String n = tokens.nextToken();
        BigInteger publicExponent = new BigInteger(e);
        BigInteger modulus = new BigInteger(n);
        StringBuffer comment = new StringBuffer();
        while (tokens.hasMoreTokens()) {
            if (comment.length() > 0) {
                comment.append(" ");
            }
            comment.append(tokens.nextToken());
        }
        SshRsaPublicKey key = ComponentManager.getInstance().createRsaPublicKey(modulus, publicExponent, 1);
        this.addEntry(new Ssh1KeyEntry(this.getNames(host), key, comment.toString()));
        return true;
    }

    public synchronized void setHashHosts(boolean hashHosts) {
        this.hashHosts = hashHosts;
    }

    protected void onInvalidHostEntry(String entry) throws SshException {
    }

    protected void onHostKeyMismatch(String host, List<SshPublicKey> allowedHostKey, SshPublicKey actualHostKey) throws SshException {
    }

    protected void onUnknownHost(String host, SshPublicKey key) throws SshException {
    }

    protected void onRevokedKey(String host, SshPublicKey key) {
    }

    public synchronized void removeEntries(String host) throws SshException {
        ArrayList<KeyEntry> toRemove = new ArrayList<KeyEntry>();
        for (KeyEntry entry : this.getKeyEntries()) {
            if (!entry.matchesHost(host)) continue;
            toRemove.add(entry);
        }
        this.removeEntry(toRemove.toArray(new KeyEntry[0]));
    }

    public synchronized void removeEntries(String host, SshPublicKey key) throws SshException {
        ArrayList<KeyEntry> toRemove = new ArrayList<KeyEntry>();
        for (KeyEntry entry : this.getKeyEntries()) {
            if (!entry.matchesHost(host) || !entry.getKey().equals(key)) continue;
            toRemove.add(entry);
        }
        this.removeEntry(toRemove.toArray(new KeyEntry[0]));
    }

    public synchronized void removeEntries(String ... hosts) throws SshException {
        for (String host : hosts) {
            this.removeEntries(host);
        }
    }

    public synchronized void removeEntries(SshPublicKey key) {
        List<KeyEntry> toRemove = this.entriesByPublicKey.get(key);
        this.removeEntry(toRemove.toArray(new KeyEntry[0]));
    }

    public synchronized void removeEntry(KeyEntry ... keys) {
        List<KeyEntry> toRemove = Arrays.asList(keys);
        for (KeyEntry keyEntry : toRemove) {
            if (!log.isDebugEnabled()) continue;
            log.debug("Removing known host {} {} {}", new Object[]{keyEntry.getNames(), keyEntry.getKey().getAlgorithm(), SshKeyUtils.getFingerprint(keyEntry.getKey())});
        }
        this.keyEntries.removeAll(toRemove);
        this.revokedEntries.removeAll(toRemove);
        this.entries.removeAll(toRemove);
        for (Map.Entry entry : this.entriesByPublicKey.entrySet()) {
            ((List)entry.getValue()).removeAll(toRemove);
        }
        this.certificateAuthorities.removeAll(toRemove);
        for (Iterator<Object> iterator : keys) {
            this.onHostKeyRemoved(this.getNames(((KeyEntry)((Object)iterator)).getNames()), ((KeyEntry)((Object)iterator)).getKey());
        }
    }

    protected void onHostKeyRemoved(Set<String> names, SshPublicKey key) {
    }

    public boolean isHostFileWriteable() {
        return true;
    }

    public void allowHost(String host, SshPublicKey key, boolean always) throws SshException {
        this.addEntry(key, "", this.resolveNames(host).toArray(new String[0]));
    }

    public synchronized void addEntry(SshPublicKey key, String comment, String ... names) throws SshException {
        if (this.useHashHosts()) {
            for (String name : names) {
                this.addEntry(new Ssh2KeyEntry(new HashSet<String>(Arrays.asList(this.generateHash(name))), key, comment));
            }
        } else {
            this.addEntry(new Ssh2KeyEntry(new HashSet<String>(Arrays.asList(names)), key, comment));
        }
    }

    @Override
    public synchronized boolean verifyHost(String host, SshPublicKey pk) throws SshException {
        return this.verifyHost(host, pk, true);
    }

    public Set<String> getAlgorithmsForHost(String host) throws SshException {
        Set<String> resolvedNames = this.resolveNames(host);
        HashSet<String> algorithms = new HashSet<String>();
        for (CertAuthorityEntry ca : this.certificateAuthorities) {
            if (!ca.matchesHost(resolvedNames)) continue;
            algorithms.add(ca.getKey().getAlgorithm());
        }
        for (KeyEntry entry : this.keyEntries) {
            if (!entry.matchesHost(resolvedNames)) continue;
            algorithms.add(entry.getKey().getAlgorithm());
        }
        return algorithms;
    }

    private synchronized boolean verifyHost(String host, SshPublicKey pk, boolean validateUnknown) throws SshException {
        Set<String> resolvedNames = this.resolveNames(host);
        if (log.isDebugEnabled()) {
            log.debug("Verifying known host for {} {} {}", new Object[]{host, pk.getAlgorithm(), SshKeyUtils.getFingerprint(pk)});
        }
        for (KeyEntry keyEntry : this.revokedEntries) {
            if (!keyEntry.validate(pk, resolvedNames.toArray(new String[0]))) continue;
            for (HostKeyVerificationListener hostKeyVerificationListener : this.listeners) {
                try {
                    hostKeyVerificationListener.onRevokedKey(host, pk);
                }
                catch (Throwable throwable) {}
            }
            if (log.isDebugEnabled()) {
                log.debug("Key has been revoked {} {} {}", new Object[]{host, pk.getAlgorithm(), SshKeyUtils.getFingerprint(pk)});
            }
            this.onRevokedKey(host, pk);
            return false;
        }
        if (pk instanceof OpenSshCertificate) {
            for (CertAuthorityEntry certAuthorityEntry : this.certificateAuthorities) {
                if (!certAuthorityEntry.validate(pk, resolvedNames.toArray(new String[0]))) continue;
                if (log.isDebugEnabled()) {
                    log.debug("Allowing certificate {} {} {}", new Object[]{host, pk.getAlgorithm(), SshKeyUtils.getFingerprint(pk)});
                }
                return true;
            }
        }
        ArrayList<KeyEntry> matches = new ArrayList<KeyEntry>();
        for (KeyEntry keyEntry : this.keyEntries) {
            if (!keyEntry.matchesHost(resolvedNames)) continue;
            if (log.isDebugEnabled()) {
                log.debug("Found key {} {} {}", new Object[]{host, pk.getAlgorithm(), SshKeyUtils.getFingerprint(pk)});
            }
            matches.add(keyEntry);
        }
        if (!matches.isEmpty()) {
            ArrayList<SshPublicKey> arrayList = new ArrayList<SshPublicKey>();
            for (KeyEntry keyEntry : matches) {
                if (keyEntry.validate(pk, resolvedNames.toArray(new String[0]))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Matched key {} {} {}", new Object[]{host, pk.getAlgorithm(), SshKeyUtils.getFingerprint(pk)});
                    }
                    return true;
                }
                arrayList.add(keyEntry.getKey());
            }
            if (!validateUnknown) {
                if (log.isDebugEnabled()) {
                    log.debug("Host is known but key is unknown {} {} {}", new Object[]{host, pk.getAlgorithm(), SshKeyUtils.getFingerprint(pk)});
                }
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("Mismatched key {} {} {}", new Object[]{host, pk.getAlgorithm(), SshKeyUtils.getFingerprint(pk)});
            }
            for (HostKeyVerificationListener hostKeyVerificationListener : this.listeners) {
                try {
                    hostKeyVerificationListener.onHostKeyMismatch(host, arrayList, pk);
                }
                catch (Throwable throwable) {}
            }
            this.onHostKeyMismatch(host, arrayList, pk);
            return this.verifyHost(host, pk, false);
        }
        if (log.isDebugEnabled()) {
            log.debug("Key is unknown {} {} {}", new Object[]{host, pk.getAlgorithm(), SshKeyUtils.getFingerprint(pk)});
        }
        if (!validateUnknown) {
            return false;
        }
        for (HostKeyVerificationListener hostKeyVerificationListener : this.listeners) {
            try {
                hostKeyVerificationListener.onUnknownHost(host, pk);
            }
            catch (Throwable throwable) {}
        }
        this.onUnknownHost(host, pk);
        return this.verifyHost(host, pk, false);
    }

    protected Set<String> resolveNames(String host) {
        String fqn = null;
        String ip = null;
        String resolveHost = host;
        LinkedHashSet<String> resolvedNames = new LinkedHashSet<String>();
        resolvedNames.add(host);
        Matcher m = this.nonStandard.matcher(host);
        boolean nonStandardPorts = m.matches();
        if (nonStandardPorts) {
            resolveHost = m.group(1);
        }
        if (this.useCanonicalHostname() || this.useReverseDNS()) {
            try {
                InetAddress addr = InetAddress.getByName(resolveHost);
                if (this.useCanonicalHostname()) {
                    fqn = nonStandardPorts ? String.format("[%s]:%s", addr.getHostName(), m.group(2)) : addr.getHostName();
                    resolvedNames.add(fqn);
                }
                if (this.useReverseDNS()) {
                    ip = nonStandardPorts ? String.format("[%s]:%s", addr.getHostAddress(), m.group(2)) : addr.getHostAddress();
                    resolvedNames.add(ip);
                }
            }
            catch (UnknownHostException unknownHostException) {
                // empty catch block
            }
        }
        return resolvedNames;
    }

    public boolean useCanonicalHostname() {
        return this.useCanonicalHostname;
    }

    public boolean useReverseDNS() {
        return this.useReverseDNS;
    }

    public boolean useHashHosts() {
        return this.hashHosts;
    }

    private boolean checkHash(String name, String resolvedName) throws SshException {
        SshHmac sha1 = (SshHmac)ComponentManager.getInstance().supportedHMacsCS().getInstance("hmac-sha1");
        String hashData = name.substring(HASH_MAGIC.length());
        String hashSalt = hashData.substring(0, hashData.indexOf(HASH_DELIM));
        String hashStr = hashData.substring(hashData.indexOf(HASH_DELIM) + 1);
        byte[] theHash = Base64.decode(hashStr);
        sha1.init(Base64.decode(hashSalt));
        sha1.update(resolvedName.getBytes());
        byte[] ourHash = sha1.doFinal();
        return Arrays.equals(theHash, ourHash);
    }

    private String generateHash(String host) throws SshException {
        SshHmac sha1 = (SshHmac)ComponentManager.getInstance().supportedHMacsCS().getInstance("hmac-sha1");
        byte[] hashSalt = new byte[sha1.getMacLength()];
        ComponentManager.getInstance().getRND().nextBytes(hashSalt);
        sha1.init(hashSalt);
        sha1.update(host.getBytes());
        byte[] theHash = sha1.doFinal();
        return HASH_MAGIC + Base64.encodeBytes(hashSalt, false) + HASH_DELIM + Base64.encodeBytes(theHash, false);
    }

    public synchronized String toString() {
        StringBuffer buf = new StringBuffer("");
        for (HostFileEntry entry : this.entries) {
            buf.append(entry.getFormattedLine());
            buf.append(System.getProperty("line.separator"));
        }
        return buf.toString();
    }

    public void setUseCanonicalHostnames(boolean value) {
        this.useCanonicalHostname = value;
    }

    public void setUseReverseDNS(boolean value) {
        this.useReverseDNS = value;
    }

    public Set<KeyEntry> getKeyEntries() {
        return this.keyEntries;
    }

    @Override
    public boolean isKnownHost(String host, SshPublicKey key) throws SshException {
        return this.verifyHost(host, key, false);
    }

    @Override
    public void updateHostKey(String host, SshPublicKey key) throws SshException {
        KeyEntry existingEntry = null;
        Set<String> names = this.resolveNames(host);
        for (KeyEntry e : this.getKeyEntries()) {
            if (e.isHashedEntry()) {
                if (!e.matchesHash(e.getNames(), names.toArray(new String[0]))) continue;
                existingEntry = e;
                continue;
            }
            if (!e.matchesHost(names.toArray(new String[0]))) continue;
            existingEntry = e;
        }
        if (existingEntry != null) {
            this.removeEntries(host);
        }
        this.addEntry(key, "", names.toArray(new String[0]));
        if (existingEntry != null) {
            this.onHostKeyUpdated(names, key);
        }
    }

    protected void onHostKeyUpdated(Set<String> names, SshPublicKey key) {
    }

    abstract class NonValidatingFileEntry
    extends HostFileEntry {
        NonValidatingFileEntry() {
        }

        @Override
        boolean canValidate() {
            return false;
        }

        @Override
        boolean validate(SshPublicKey key, String ... resolvedNames) throws SshException {
            throw new UnsupportedOperationException();
        }
    }

    public class BlankEntry
    extends NonValidatingFileEntry {
        @Override
        String getFormattedLine() {
            return "";
        }
    }

    public class InvalidEntry
    extends NonValidatingFileEntry {
        String line;

        InvalidEntry(String line) {
            this.line = line;
        }

        @Override
        String getFormattedLine() {
            return this.line;
        }
    }

    public class CommentEntry
    extends NonValidatingFileEntry {
        String comment;

        CommentEntry(String comment) {
            this.comment = comment;
        }

        @Override
        String getFormattedLine() {
            return String.format("#%s", this.comment);
        }
    }

    public class RevokedEntry
    extends KeyEntry {
        KeyEntry revokedEntry;

        RevokedEntry(Set<String> names, KeyEntry revokedEntry) {
            super(names, revokedEntry.getKey(), revokedEntry.getComment());
            this.revokedEntry = revokedEntry;
        }

        @Override
        String getFormattedLine() {
            return String.format("@revoked %s", this.revokedEntry.getFormattedLine());
        }

        @Override
        boolean canValidate() {
            return true;
        }

        @Override
        public final boolean isRevoked() {
            return true;
        }
    }

    public class CertAuthorityEntry
    extends KeyEntry {
        CertAuthorityEntry(Set<String> names, SshPublicKey key, String comment) {
            super(names, key, comment);
        }

        @Override
        String getFormattedLine() {
            try {
                return String.format("@cert-authority %s %s", this.getNames(), SshKeyUtils.getFormattedKey(this.key, this.comment));
            }
            catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        @Override
        boolean canValidate() {
            return true;
        }

        @Override
        boolean validate(SshPublicKey key, String ... resolvedNames) throws SshException {
            if (this.matchesHost(resolvedNames) && key instanceof OpenSshCertificate) {
                return ((OpenSshCertificate)key).getSignedBy().equals(this.key);
            }
            return false;
        }

        @Override
        public final boolean isCertAuthority() {
            return true;
        }
    }

    public class Ssh2KeyEntry
    extends KeyEntry {
        boolean hashedEntry;

        Ssh2KeyEntry(Set<String> names, SshPublicKey key, String comment) {
            super(names, key, comment);
            this.hashedEntry = false;
            if (names.size() == 1 && names.iterator().next().startsWith(KnownHostsKeyVerification.HASH_DELIM)) {
                this.hashedEntry = true;
            }
        }

        @Override
        public boolean isHashedEntry() {
            return this.hashedEntry;
        }

        @Override
        String getFormattedLine() {
            try {
                return String.format("%s %s", this.getNames(), SshKeyUtils.getFormattedKey(this.key, this.comment)).trim();
            }
            catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    class Ssh1KeyEntry
    extends KeyEntry {
        Ssh1KeyEntry(Set<String> names, SshPublicKey key, String comment) {
            super(names, key, comment);
        }

        @Override
        String getFormattedLine() {
            StringBuffer buf = new StringBuffer();
            buf.append(this.getNames());
            buf.append(" ");
            buf.append(String.valueOf(((Ssh1RsaPublicKey)this.key).getModulus().bitLength()));
            buf.append(" ");
            buf.append(((Ssh1RsaPublicKey)this.key).getPublicExponent());
            buf.append(" ");
            buf.append(((Ssh1RsaPublicKey)this.key).getModulus());
            if (this.comment.length() > 0) {
                buf.append(" ");
                buf.append(this.comment);
            }
            return buf.toString();
        }
    }

    public abstract class KeyEntry
    extends HostFileEntry {
        String comment;
        Set<String> names;
        SshPublicKey key;
        boolean hashedEntry;

        KeyEntry(Set<String> names, SshPublicKey key, String comment) {
            this.hashedEntry = false;
            this.names = names;
            this.key = key;
            this.comment = comment;
            if (names.size() == 1 && names.iterator().next().startsWith(KnownHostsKeyVerification.HASH_DELIM)) {
                this.hashedEntry = true;
            }
        }

        public boolean isHashedEntry() {
            return this.hashedEntry;
        }

        public SshPublicKey getKey() {
            return this.key;
        }

        public String getNames() {
            StringBuffer buf = new StringBuffer();
            for (String name : this.names) {
                if (buf.length() > 0) {
                    buf.append(",");
                }
                buf.append(name);
            }
            return buf.toString();
        }

        boolean matchesHash(String name, String ... resolvedNames) throws SshException {
            for (String resolvedName : resolvedNames) {
                if (!KnownHostsKeyVerification.this.checkHash(name, resolvedName)) continue;
                return true;
            }
            return false;
        }

        boolean matchesHost(Set<String> resolvedNames) throws SshException {
            return this.matchesHost(resolvedNames.toArray(new String[0]));
        }

        boolean matchesHost(String ... resolvedNames) throws SshException {
            boolean success = true;
            boolean matched = false;
            for (String name : this.names) {
                if (name.startsWith(KnownHostsKeyVerification.HASH_MAGIC)) {
                    return this.matchesHash(name, resolvedNames);
                }
                if (name.startsWith("!")) {
                    if (!this.matches(name.substring(1), resolvedNames)) continue;
                    success = false;
                    matched = true;
                    continue;
                }
                if (!this.matches(name, resolvedNames)) continue;
                matched = true;
            }
            if (matched) {
                return success;
            }
            return false;
        }

        @Override
        boolean canValidate() {
            return true;
        }

        @Override
        boolean validate(SshPublicKey key, String ... resolvedNames) throws SshException {
            if (this.matchesHost(resolvedNames)) {
                return key.equals(this.key);
            }
            return false;
        }

        boolean matches(String name, String ... resolvedNames) {
            name = name.replace(".", "\\.");
            name = name.replace("[", "\\[");
            if ((name = name.replace("]", "\\]")).contains("*")) {
                name = name.replace("*", ".*");
            }
            if (name.contains("?")) {
                name = name.replace("?", ".");
            }
            for (String resolvedName : resolvedNames) {
                if (!resolvedName.matches(name)) continue;
                return true;
            }
            return false;
        }

        public String getComment() {
            return this.comment;
        }

        public boolean isRevoked() {
            return false;
        }

        public boolean isCertAuthority() {
            return false;
        }
    }

    public abstract class HostFileEntry {
        abstract String getFormattedLine();

        abstract boolean canValidate();

        abstract boolean validate(SshPublicKey var1, String ... var2) throws SshException;
    }
}

