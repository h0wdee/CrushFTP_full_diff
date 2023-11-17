/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshDsaPrivateKey;
import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.Ssh2BaseDsaPrivateKey;
import com.maverick.ssh.components.jce.Ssh2DsaPublicKey;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssh2DsaPrivateKey
extends Ssh2BaseDsaPrivateKey
implements SshDsaPrivateKey {
    static Logger log = LoggerFactory.getLogger(Ssh2DsaPrivateKey.class);
    protected DSAPrivateKey prv;
    protected Ssh2DsaPublicKey pub;

    public Ssh2DsaPrivateKey(DSAPrivateKey prv, DSAPublicKey pub) {
        super(prv);
        this.prv = prv;
        this.pub = new Ssh2DsaPublicKey(pub);
    }

    public Ssh2DsaPrivateKey(DSAPrivateKey prv) throws NoSuchAlgorithmException, InvalidKeySpecException {
        super(prv);
        this.prv = prv;
        this.generatePublic();
    }

    public Ssh2DsaPrivateKey(BigInteger p, BigInteger q, BigInteger g, BigInteger x, BigInteger y) throws SshException {
        super(null);
        try {
            KeyFactory kf = JCEProvider.getProviderForAlgorithm("DSA") == null ? KeyFactory.getInstance("DSA") : KeyFactory.getInstance("DSA", JCEProvider.getProviderForAlgorithm("DSA"));
            DSAPrivateKeySpec spec = new DSAPrivateKeySpec(x, p, q, g);
            this.prv = (DSAPrivateKey)kf.generatePrivate(spec);
            ((Ssh2BaseDsaPrivateKey)this).prv = this.prv;
            this.pub = new Ssh2DsaPublicKey(p, q, g, y);
        }
        catch (Throwable e) {
            throw new SshException(e);
        }
    }

    private void generatePublic() throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger y = this.prv.getParams().getG().modPow(this.prv.getX(), this.prv.getParams().getP());
        this.pub = new Ssh2DsaPublicKey(this.prv.getParams().getP(), this.prv.getParams().getQ(), this.prv.getParams().getG(), y);
    }

    @Override
    public DSAPrivateKey getJCEPrivateKey() {
        return this.prv;
    }

    @Override
    public SshDsaPublicKey getPublicKey() {
        return this.pub;
    }

    @Override
    public BigInteger getX() {
        return this.prv.getX();
    }
}

