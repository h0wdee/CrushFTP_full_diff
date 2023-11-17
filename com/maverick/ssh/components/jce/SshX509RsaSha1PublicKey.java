/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshX509PublicKey;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKey;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;

public class SshX509RsaSha1PublicKey
extends Ssh2RsaPublicKey
implements SshX509PublicKey {
    public static final String X509V3_SIGN_RSA_SHA1 = "x509v3-sign-rsa-sha1";
    Certificate cert;

    public SshX509RsaSha1PublicKey() {
        super(SecurityLevel.WEAK, 101);
    }

    public SshX509RsaSha1PublicKey(Certificate cert) {
        super((RSAPublicKey)cert.getPublicKey());
        this.cert = cert;
    }

    @Override
    public String getAlgorithm() {
        return X509V3_SIGN_RSA_SHA1;
    }

    @Override
    public byte[] getEncoded() throws SshException {
        ByteArrayWriter baw = new ByteArrayWriter();
        try {
            baw.writeString(this.getAlgorithm());
            baw.writeBinaryString(this.cert.getEncoded());
            byte[] byArray = baw.toByteArray();
            return byArray;
        }
        catch (Throwable ex) {
            throw new SshException("Failed to encoded key data", 5, ex);
        }
        finally {
            try {
                baw.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public void init(byte[] blob, int start, int len) throws SshException {
        ByteArrayReader bar = new ByteArrayReader(blob, start, len);
        try {
            String header = bar.readString();
            if (!header.equals(X509V3_SIGN_RSA_SHA1)) {
                throw new SshException("The encoded key is not X509 RSA", 5);
            }
            byte[] encoded = bar.readBinaryString();
            ByteArrayInputStream is = new ByteArrayInputStream(encoded);
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
        finally {
            try {
                bar.close();
            }
            catch (IOException header) {}
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

