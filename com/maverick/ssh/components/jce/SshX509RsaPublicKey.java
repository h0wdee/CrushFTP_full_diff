/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshX509PublicKey;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKey;
import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;

public class SshX509RsaPublicKey
extends Ssh2RsaPublicKey
implements SshX509PublicKey {
    public static final String X509V3_SIGN_RSA = "x509v3-sign-rsa";
    Certificate cert;

    public SshX509RsaPublicKey() {
        super(SecurityLevel.WEAK, 101);
    }

    public SshX509RsaPublicKey(Certificate cert) {
        super((RSAPublicKey)cert.getPublicKey(), SecurityLevel.WEAK, 101);
        this.cert = cert;
    }

    @Override
    public String getAlgorithm() {
        return X509V3_SIGN_RSA;
    }

    @Override
    public String getSigningAlgorithm() {
        return this.getAlgorithm();
    }

    @Override
    public byte[] getEncoded() throws SshException {
        try {
            return this.cert.getEncoded();
        }
        catch (Throwable ex) {
            throw new SshException("Failed to encoded key data", 5, ex);
        }
    }

    @Override
    public void init(byte[] blob, int start, int len) throws SshException {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(blob, start, len);
            CertificateFactory cf = JCEProvider.getProviderForAlgorithm("X.509") == null ? CertificateFactory.getInstance("X.509") : CertificateFactory.getInstance("X.509", JCEProvider.getProviderForAlgorithm("X.509"));
            this.cert = cf.generateCertificate(is);
            if (!(this.cert.getPublicKey() instanceof RSAPublicKey)) {
                throw new SshException("Certificate public key is not an RSA public key!", 4);
            }
            this.pubKey = (RSAPublicKey)this.cert.getPublicKey();
        }
        catch (Throwable ex) {
            throw new SshException(ex.getMessage(), 16, ex);
        }
    }

    @Override
    public Certificate getCertificate() {
        return this.cert;
    }

    @Override
    public Certificate[] getCertificateChain() {
        return new Certificate[]{this.cert};
    }
}

