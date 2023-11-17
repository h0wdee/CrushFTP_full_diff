/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKey;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

public class Ssh2RsaPublicKeySHA512
extends Ssh2RsaPublicKey {
    public Ssh2RsaPublicKeySHA512() {
        super(SecurityLevel.STRONG, 512);
    }

    public Ssh2RsaPublicKeySHA512(BigInteger modulus, BigInteger publicExponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        super(modulus, publicExponent, SecurityLevel.STRONG, 512);
    }

    public Ssh2RsaPublicKeySHA512(RSAPublicKey pubKey) {
        super(pubKey, SecurityLevel.STRONG, 512);
    }

    public Ssh2RsaPublicKeySHA512(SshRsaPublicKey publicKey) {
        this((RSAPublicKey)publicKey.getJCEPublicKey());
    }

    @Override
    public String getSigningAlgorithm() {
        return "rsa-sha2-512";
    }

    @Override
    public String getAlgorithm() {
        return "rsa-sha2-512";
    }

    @Override
    public String getEncodingAlgorithm() {
        return "ssh-rsa";
    }
}

