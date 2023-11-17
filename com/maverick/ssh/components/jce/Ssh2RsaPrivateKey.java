/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.SshRsaPrivateKey;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.Ssh2BaseRsaPrivateKey;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

public class Ssh2RsaPrivateKey
extends Ssh2BaseRsaPrivateKey
implements SshRsaPrivateKey {
    public Ssh2RsaPrivateKey(RSAPrivateKey prv) {
        super(prv);
    }

    public Ssh2RsaPrivateKey(BigInteger modulus, BigInteger privateExponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        super(null);
        KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm("RSA") == null ? KeyFactory.getInstance("RSA") : KeyFactory.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA"));
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(modulus, privateExponent);
        this.prv = (RSAPrivateKey)keyFactory.generatePrivate(spec);
    }

    @Override
    public byte[] sign(byte[] data) throws IOException {
        return this.sign(data, this.getAlgorithm());
    }

    @Override
    public byte[] sign(byte[] data, String signingAlgorithm) throws IOException {
        return super.doSign(data, signingAlgorithm);
    }

    @Override
    public String getAlgorithm() {
        return "ssh-rsa";
    }

    @Override
    public BigInteger getModulus() {
        return ((RSAPrivateKey)this.prv).getModulus();
    }

    @Override
    public BigInteger getPrivateExponent() {
        return ((RSAPrivateKey)this.prv).getPrivateExponent();
    }

    @Override
    public PrivateKey getJCEPrivateKey() {
        return this.prv;
    }

    public int hashCode() {
        return this.prv.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Ssh2RsaPrivateKey) {
            Ssh2RsaPrivateKey other = (Ssh2RsaPrivateKey)obj;
            if (other.prv != null) {
                return other.prv.equals(this.prv);
            }
        }
        return false;
    }
}

