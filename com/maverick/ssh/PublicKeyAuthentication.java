/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshAuthentication;
import com.maverick.ssh.components.SshCertificate;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshPrivateKey;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.util.SshKeyUtils;
import com.sshtools.publickey.InvalidPassphraseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PublicKeyAuthentication
implements SshAuthentication {
    protected String username;
    protected SshPrivateKey privatekey;
    protected SshPublicKey publickey;
    protected boolean authenticating = true;

    public PublicKeyAuthentication() {
    }

    public PublicKeyAuthentication(SshKeyPair pair) {
        this.privatekey = pair.getPrivateKey();
        this.publickey = pair instanceof SshCertificate ? ((SshCertificate)pair).getCertificate() : pair.getPublicKey();
    }

    public PublicKeyAuthentication(File identityFile, String passphrase) throws IOException, InvalidPassphraseException {
        this(SshKeyUtils.getPrivateKeyOrCertificate(identityFile, passphrase));
    }

    public PublicKeyAuthentication(InputStream identityFile, String passphrase) throws IOException, InvalidPassphraseException {
        this(SshKeyUtils.getPrivateKey(identityFile, passphrase));
    }

    public PublicKeyAuthentication(String identityFile, String passphrase) throws IOException, InvalidPassphraseException {
        this(SshKeyUtils.getPrivateKey(identityFile, passphrase));
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public void setPrivateKey(SshPrivateKey privatekey) {
        this.privatekey = privatekey;
    }

    @Override
    public String getMethod() {
        return "publickey";
    }

    public SshPrivateKey getPrivateKey() {
        return this.privatekey;
    }

    public void setPublicKey(SshPublicKey publickey) {
        this.publickey = publickey;
    }

    public SshPublicKey getPublicKey() {
        return this.publickey;
    }

    public void setAuthenticating(boolean authenticating) {
        this.authenticating = authenticating;
    }

    public boolean isAuthenticating() {
        return this.authenticating;
    }
}

