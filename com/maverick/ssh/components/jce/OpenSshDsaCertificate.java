/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.OpenSshCertificate;
import com.maverick.ssh.components.jce.Ssh2DsaPublicKey;
import com.maverick.util.ByteArrayReader;
import com.sshtools.publickey.SshKeyPairGenerator;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public class OpenSshDsaCertificate
extends OpenSshCertificate
implements SshDsaPublicKey {
    public static final String SSH_DSS_CERT_V01 = "ssh-dss-cert-v01@openssh.com";
    byte[] nonce;

    public OpenSshDsaCertificate() {
    }

    public OpenSshDsaCertificate(DSAPublicKey pub) {
        this.publicKey = new Ssh2DsaPublicKey(pub);
    }

    public OpenSshDsaCertificate(BigInteger p, BigInteger q, BigInteger g, BigInteger y) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.publicKey = new Ssh2DsaPublicKey(p, q, g, y);
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.WEAK;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getAlgorithm() {
        return SSH_DSS_CERT_V01;
    }

    @Override
    protected void decodePublicKey(ByteArrayReader reader) throws IOException, SshException {
        try {
            BigInteger p = reader.readBigInteger();
            BigInteger q = reader.readBigInteger();
            BigInteger g = reader.readBigInteger();
            BigInteger y = reader.readBigInteger();
            this.publicKey = new Ssh2DsaPublicKey(p, q, g, y);
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
        if (obj instanceof SshDsaPublicKey) {
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
    public BigInteger getG() {
        return ((Ssh2DsaPublicKey)this.publicKey).getG();
    }

    @Override
    public BigInteger getP() {
        return ((Ssh2DsaPublicKey)this.publicKey).getP();
    }

    @Override
    public BigInteger getQ() {
        return ((Ssh2DsaPublicKey)this.publicKey).getQ();
    }

    @Override
    public BigInteger getY() {
        return ((Ssh2DsaPublicKey)this.publicKey).getY();
    }

    @Override
    public String test() {
        try {
            KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm("DSA") == null ? KeyFactory.getInstance("DSA") : KeyFactory.getInstance("DSA", JCEProvider.getProviderForAlgorithm("DSA"));
            Signature sig = JCEProvider.getProviderForAlgorithm("SHA1WithDSA") == null ? Signature.getInstance("SHA1WithDSA") : Signature.getInstance("SHA1WithDSA", JCEProvider.getProviderForAlgorithm("SHA1WithDSA"));
            return keyFactory.getProvider().getName();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws Exception {
        SshKeyPair pair = SshKeyPairGenerator.generateKeyPair("ssh-dss", 1024);
        Random r = new Random();
        byte[] test = new byte[128];
        int i = 1;
        do {
            r.nextBytes(test);
            System.out.println(String.format("Cycle %d", i++));
        } while (pair.getPublicKey().verifySignature(pair.getPrivateKey().sign(test, pair.getPublicKey().getSigningAlgorithm()), test));
        System.out.println("Bad verification");
    }

    @Override
    public String getSigningAlgorithm() {
        return this.getAlgorithm();
    }

    @Override
    public int getBitLength() {
        return this.publicKey.getBitLength();
    }

    @Override
    public DSAPublicKey getJCEPublicKey() {
        return (DSAPublicKey)this.publicKey.getJCEPublicKey();
    }
}

