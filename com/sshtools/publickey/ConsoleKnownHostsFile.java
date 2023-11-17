/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshKeyFingerprint;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.util.SshKeyUtils;
import com.sshtools.publickey.KnownHostsFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ConsoleKnownHostsFile
extends KnownHostsFile {
    public ConsoleKnownHostsFile() throws SshException, IOException {
    }

    public ConsoleKnownHostsFile(File file) throws SshException, IOException {
        super(file);
    }

    @Override
    protected void onRevokedKey(String host, SshPublicKey key) {
        System.out.println("The host key supplied by " + host + " (" + key.getAlgorithm() + ") with fingerprint " + SshKeyUtils.getFingerprint(key) + " has been revoked");
    }

    @Override
    protected void onHostKeyMismatch(String host, List<SshPublicKey> allowedHostKey, SshPublicKey actual) throws SshException {
        try {
            System.out.println("The host key supplied by " + host + "(" + actual.getAlgorithm() + ") is: " + actual.getFingerprint());
            System.out.println("The current allowed keys for " + host + "(" + actual.getAlgorithm() + ") are:");
            for (SshPublicKey key : allowedHostKey) {
                System.out.println(SshKeyUtils.getFormattedKey(key, ""));
            }
            this.getResponse(host, actual);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUnknownHost(String host, SshPublicKey pk) {
        try {
            System.out.println("The host " + host + " is currently unknown to the system");
            System.out.println("The host key (" + pk.getAlgorithm() + ") fingerprint is: " + SshKeyFingerprint.getFingerprint(pk.getEncoded(), "SHA256"));
            this.getResponse(host, pk);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onInvalidHostEntry(String entry) throws SshException {
        System.out.println("Invalid host entry in " + this.getKnownHostsFile().getAbsolutePath());
        System.out.println(entry);
    }

    private void getResponse(String host, SshPublicKey pk) throws SshException, IOException {
        String response = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO") || response.equalsIgnoreCase("ALWAYS") && this.isHostFileWriteable())) {
            String options;
            String string = options = this.isHostFileWriteable() ? "Yes|No|Always" : "Yes|No";
            if (!this.isHostFileWriteable()) {
                System.out.println("Always option disabled, host file is not writeable");
            }
            System.out.print("Do you want to allow this host key? [" + options + "]: ");
            try {
                response = reader.readLine();
            }
            catch (IOException ex) {
                throw new SshException("Failed to read response", 5);
            }
        }
        if (response.equalsIgnoreCase("YES")) {
            this.allowHost(host, pk, false);
        }
        if (response.equalsIgnoreCase("ALWAYS") && this.isHostFileWriteable()) {
            this.allowHost(host, pk, true);
        }
    }
}

