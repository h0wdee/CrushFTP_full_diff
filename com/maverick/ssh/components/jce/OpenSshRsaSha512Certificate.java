/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.OpenSshCertificate;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKey;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKeySHA512;
import com.maverick.util.ByteArrayReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class OpenSshRsaSha512Certificate
extends OpenSshCertificate
implements SshRsaPublicKey {
    public static final String SSH_RSA_CERT_V01 = "rsa-sha2-512-cert-v01@openssh.com";
    byte[] nonce;

    public OpenSshRsaSha512Certificate() {
    }

    public OpenSshRsaSha512Certificate(RSAPublicKey pubKey) {
        this.publicKey = new Ssh2RsaPublicKeySHA512(pubKey);
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.PARANOID;
    }

    @Override
    public int getPriority() {
        return SecurityLevel.PARANOID.ordinal() * 1000 + 2;
    }

    public OpenSshRsaSha512Certificate(BigInteger modulus, BigInteger publicExponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.publicKey = new Ssh2RsaPublicKeySHA512(modulus, publicExponent);
    }

    @Override
    public int getBitLength() {
        return this.publicKey.getBitLength();
    }

    @Override
    protected void decodePublicKey(ByteArrayReader reader) throws IOException, SshException {
        try {
            BigInteger e = reader.readBigInteger();
            BigInteger n = reader.readBigInteger();
            this.publicKey = new Ssh2RsaPublicKeySHA512(n, e);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new SshException("Failed to obtain RSA public key instance", 5, ex);
        }
    }

    @Override
    public String getAlgorithm() {
        return SSH_RSA_CERT_V01;
    }

    @Override
    public String getEncodingAlgorithm() {
        return "ssh-rsa-cert-v01@openssh.com";
    }

    @Override
    public boolean verifySignature(byte[] signature, byte[] data) throws SshException {
        return this.publicKey.verifySignature(signature, data);
    }

    public boolean equals(Object obj) {
        if (obj instanceof SshRsaPublicKey) {
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
    public int getVersion() {
        return 2;
    }

    @Override
    public PublicKey getJCEPublicKey() {
        return this.publicKey.getJCEPublicKey();
    }

    @Override
    public String test() {
        try {
            KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm("RSA") == null ? KeyFactory.getInstance("RSA") : KeyFactory.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA"));
            Cipher cipher = JCEProvider.getProviderForAlgorithm("RSA_Cipher") == null ? Cipher.getInstance("RSA") : Cipher.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA_Cipher"));
            Signature s = JCEProvider.getProviderForAlgorithm("SHA1WithRSA") == null ? Signature.getInstance("SHA1WithRSA") : Signature.getInstance("SHA1WithRSA", JCEProvider.getProviderForAlgorithm("SHA1WithRSA"));
            return keyFactory.getProvider().getName();
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public String getSigningAlgorithm() {
        return "rsa-sha2-512";
    }

    @Override
    public BigInteger getModulus() {
        return ((Ssh2RsaPublicKey)this.publicKey).getModulus();
    }

    @Override
    public BigInteger getPublicExponent() {
        return ((Ssh2RsaPublicKey)this.publicKey).getPublicExponent();
    }

    @Override
    public BigInteger doPublic(BigInteger input) throws SshException {
        return ((Ssh2RsaPublicKey)this.publicKey).doPublic(input);
    }
}

