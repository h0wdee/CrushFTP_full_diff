/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.util.IOUtil;
import com.sshtools.publickey.KnownHostsKeyVerification;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnownHostsFile
extends KnownHostsKeyVerification {
    static Logger log = LoggerFactory.getLogger(KnownHostsFile.class);
    File file;

    public KnownHostsFile(File file) throws SshException {
        this.file = file;
        try (FileInputStream in = new FileInputStream(file);){
            this.load(in);
        }
        catch (IOException e) {
            throw new SshException(e);
        }
    }

    public void store() throws IOException {
        IOUtil.writeStringToFile(this.file, this.toString(), "UTF-8");
    }

    public File getKnownHostsFile() {
        return this.file;
    }

    @Override
    public boolean isHostFileWriteable() {
        return this.file.canWrite();
    }

    public KnownHostsFile() throws SshException {
        this(new File(new File(System.getProperty("user.home"), ".ssh"), "known_hosts"));
    }

    @Override
    protected void onInvalidHostEntry(String entry) throws SshException {
    }

    @Override
    protected void onHostKeyMismatch(String host, List<SshPublicKey> allowedHostKey, SshPublicKey actualHostKey) throws SshException {
    }

    @Override
    protected void onUnknownHost(String host, SshPublicKey key) throws SshException {
    }

    @Override
    protected void onRevokedKey(String host, SshPublicKey key) {
    }

    @Override
    protected void onHostKeyUpdated(Set<String> names, SshPublicKey key) {
        this.save();
    }

    @Override
    protected void onHostKeyAdded(Set<String> names, SshPublicKey key) {
        this.save();
    }

    @Override
    protected void onHostKeyRemoved(Set<String> names, SshPublicKey key) {
        this.save();
    }

    protected void save() {
        try {
            this.store();
        }
        catch (IOException e) {
            log.error("Failed to store known_hosts file", (Throwable)e);
        }
    }
}

