/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.OpenSshCertificate;
import com.maverick.ssh.components.jce.SshEd25519PublicKeyJCE;
import com.maverick.util.ByteArrayReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class OpenSshEd25519Certificate
extends OpenSshCertificate
implements SshPublicKey {
    public static final String CERT_TYPE = "ssh-ed25519-cert-v01@openssh.com";
    byte[] nonce;

    public OpenSshEd25519Certificate() {
    }

    public OpenSshEd25519Certificate(PublicKey pub) {
        this.publicKey = new SshEd25519PublicKeyJCE(pub);
    }

    public OpenSshEd25519Certificate(byte[] pk) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchProviderException {
        this.publicKey = new SshEd25519PublicKeyJCE(pk);
    }

    @Override
    public PublicKey getJCEPublicKey() {
        return this.publicKey.getJCEPublicKey();
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.PARANOID;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getAlgorithm() {
        return CERT_TYPE;
    }

    @Override
    public int getBitLength() {
        return 256;
    }

    @Override
    protected void decodePublicKey(ByteArrayReader reader) throws IOException, SshException {
        try {
            byte[] pk = reader.readBinaryString();
            this.publicKey = new SshEd25519PublicKeyJCE(pk);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new SshException("Failed to obtain Ed25519 public key instance", 5, ex);
        }
    }

    @Override
    public boolean verifySignature(byte[] signature, byte[] data) throws SshException {
        return this.publicKey.verifySignature(signature, data);
    }

    public boolean equals(Object obj) {
        if (obj instanceof SshEd25519PublicKeyJCE) {
            try {
                return ((SshPublicKey)obj).getFingerprint().equals(this.getFingerprint());
            }
            catch (SshException sshException) {
                // empty catch block
            }
        }
        return false;
    }

    public int hashCode() {
        try {
            return this.getFingerprint().hashCode();
        }
        catch (SshException ex) {
            return 0;
        }
    }

    @Override
    public String test() {
        try {
            KeyFactory factory = KeyFactory.getInstance("Ed25519", JCEProvider.getBCProvider());
            return factory.getProvider().getName();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public String getSigningAlgorithm() {
        return "ssh-ed25519";
    }
}

