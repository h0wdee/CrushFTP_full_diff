/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshRsaPrivateCrtKey;
import com.maverick.ssh.components.jce.JCEProvider;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import javax.crypto.Cipher;

public class Ssh2RsaPrivateCrtKey
implements SshRsaPrivateCrtKey {
    protected RSAPrivateCrtKey prv;

    public Ssh2RsaPrivateCrtKey(RSAPrivateCrtKey prv) {
        this.prv = prv;
    }

    public Ssh2RsaPrivateCrtKey(BigInteger modulus, BigInteger publicExponent, BigInteger privateExponent, BigInteger primeP, BigInteger primeQ, BigInteger primeExponentP, BigInteger primeExponentQ, BigInteger crtCoefficient) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = JCEProvider.getProviderForAlgorithm("RSA") == null ? KeyFactory.getInstance("RSA") : KeyFactory.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA"));
        RSAPrivateCrtKeySpec spec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
        this.prv = (RSAPrivateCrtKey)keyFactory.generatePrivate(spec);
    }

    @Override
    public BigInteger doPrivate(BigInteger input) throws SshException {
        try {
            Cipher cipher = JCEProvider.getProviderForAlgorithm("RSA_Cipher") == null ? Cipher.getInstance("RSA") : Cipher.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA_Cipher"));
            cipher.init(2, (Key)this.prv, JCEProvider.getSecureRandom());
            return new BigInteger(cipher.doFinal(input.toByteArray()));
        }
        catch (Throwable e) {
            throw new SshException(e);
        }
    }

    @Override
    public BigInteger getCrtCoefficient() {
        return this.prv.getCrtCoefficient();
    }

    @Override
    public BigInteger getPrimeExponentP() {
        return this.prv.getPrimeExponentP();
    }

    @Override
    public BigInteger getPrimeExponentQ() {
        return this.prv.getPrimeExponentQ();
    }

    @Override
    public BigInteger getPrimeP() {
        return this.prv.getPrimeP();
    }

    @Override
    public BigInteger getPrimeQ() {
        return this.prv.getPrimeQ();
    }

    @Override
    public BigInteger getPublicExponent() {
        return this.prv.getPublicExponent();
    }

    @Override
    public BigInteger getModulus() {
        return this.prv.getModulus();
    }

    @Override
    public BigInteger getPrivateExponent() {
        return this.prv.getPrivateExponent();
    }

    @Override
    public byte[] sign(byte[] data) throws IOException {
        return this.sign(data, this.getAlgorithm());
    }

    @Override
    public byte[] sign(byte[] msg, String signingAlgorithm) throws IOException {
        switch (signingAlgorithm) {
            case "rsa-sha2-256": {
                try {
                    Signature l_sig = JCEProvider.getProviderForAlgorithm("SHA256WithRSA") == null ? Signature.getInstance("SHA256WithRSA") : Signature.getInstance("SHA256WithRSA", JCEProvider.getProviderForAlgorithm("SHA256WithRSA"));
                    l_sig.initSign(this.prv);
                    l_sig.update(msg);
                    return l_sig.sign();
                }
                catch (Exception e) {
                    throw new IOException("Failed to sign data! " + e.getMessage());
                }
            }
            case "rsa-sha2-512": {
                try {
                    Signature l_sig = JCEProvider.getProviderForAlgorithm("SHA512WithRSA") == null ? Signature.getInstance("SHA512WithRSA") : Signature.getInstance("SHA512WithRSA", JCEProvider.getProviderForAlgorithm("SHA512WithRSA"));
                    l_sig.initSign(this.prv);
                    l_sig.update(msg);
                    return l_sig.sign();
                }
                catch (Exception e) {
                    throw new IOException("Failed to sign data! " + e.getMessage());
                }
            }
        }
        try {
            Signature l_sig = JCEProvider.getProviderForAlgorithm("SHA1WithRSA") == null ? Signature.getInstance("SHA1WithRSA") : Signature.getInstance("SHA1WithRSA", JCEProvider.getProviderForAlgorithm("SHA1WithRSA"));
            l_sig.initSign(this.prv);
            l_sig.update(msg);
            return l_sig.sign();
        }
        catch (Exception e) {
            throw new IOException("Failed to sign data! " + e.getMessage());
        }
    }

    @Override
    public String getAlgorithm() {
        return "ssh-rsa";
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
        if (obj instanceof Ssh2RsaPrivateCrtKey) {
            Ssh2RsaPrivateCrtKey other = (Ssh2RsaPrivateCrtKey)obj;
            if (other.prv != null) {
                return other.prv.equals(this.prv);
            }
        }
        return false;
    }
}

