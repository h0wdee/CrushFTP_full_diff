/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKey;
import com.maverick.ssh1.Rsa;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

public class Ssh1RsaPublicKey
extends Ssh2RsaPublicKey {
    public Ssh1RsaPublicKey(RSAPublicKey pub) {
        super(pub, SecurityLevel.WEAK, 0);
    }

    public Ssh1RsaPublicKey(BigInteger modulus, BigInteger publicExponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        super(modulus, publicExponent, SecurityLevel.WEAK, 0);
    }

    @Override
    public byte[] getEncoded() throws SshException {
        try {
            byte[] n = this.getModulus().toByteArray();
            byte[] e = this.getPublicExponent().toByteArray();
            int nCutZero = n[0] == 0 ? 1 : 0;
            int eCutZero = e[0] == 0 ? 1 : 0;
            byte[] blob = new byte[n.length + e.length - nCutZero - eCutZero];
            System.arraycopy(n, nCutZero, blob, 0, n.length - nCutZero);
            System.arraycopy(e, eCutZero, blob, n.length - nCutZero, e.length - eCutZero);
            return blob;
        }
        catch (Throwable t) {
            throw new SshException("Ssh1RsaPublicKey.getEncoded() caught an exception: " + t.getMessage() != null ? t.getMessage() : t.getClass().getName(), 5);
        }
    }

    @Override
    public String getAlgorithm() {
        return "rsa1";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public BigInteger doPublic(BigInteger input) {
        input = Rsa.padPKCS1(input, 2, (this.getModulus().bitLength() + 7) / 8);
        return Rsa.doPublic(input, this.getModulus(), this.getPublicExponent());
    }
}

