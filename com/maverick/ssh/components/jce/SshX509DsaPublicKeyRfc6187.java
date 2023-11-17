/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshX509PublicKey;
import com.maverick.ssh.components.jce.Ssh2DsaPublicKey;
import com.maverick.ssh.components.jce.SshX509RsaPublicKeyRfc6187;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.DSAPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshX509DsaPublicKeyRfc6187
extends Ssh2DsaPublicKey
implements SshX509PublicKey {
    public static final String X509V3_SSH_DSS = "x509v3-ssh-dss";
    static Logger log = LoggerFactory.getLogger(SshX509RsaPublicKeyRfc6187.class);
    Certificate[] certs;

    public SshX509DsaPublicKeyRfc6187() {
    }

    public SshX509DsaPublicKeyRfc6187(Certificate[] chain) {
        super((DSAPublicKey)chain[0].getPublicKey());
        this.certs = chain;
    }

    @Override
    public void init(byte[] blob, int start, int len) throws SshException {
        ByteArrayReader reader = new ByteArrayReader(blob, start, len);
        try {
            String alg = reader.readString();
            if (!alg.equals(this.getAlgorithm())) {
                throw new SshException("Public key blob is not a " + this.getAlgorithm() + " formatted key [" + alg + "]", 4);
            }
            int certificateCount = (int)reader.readInt();
            if (log.isDebugEnabled()) {
                log.debug("Expecting chain of " + certificateCount);
            }
            if (certificateCount <= 0) {
                throw new SshException("There are no certificats present in the public key blob", 17);
            }
            this.certs = new Certificate[certificateCount];
            for (int i = 0; i < certificateCount; ++i) {
                byte[] certBlob = reader.readBinaryString();
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                this.certs[i] = certFactory.generateCertificate(new ByteArrayInputStream(certBlob));
            }
            this.pubkey = (DSAPublicKey)this.certs[0].getPublicKey();
        }
        catch (CertificateException ex) {
            throw new SshException("Failed to generate or read certificate from public key blob", 5, ex);
        }
        catch (IOException ex) {
            throw new SshException("Failed to read public key blob; expected format " + this.getAlgorithm(), 5, ex);
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public Certificate getCertificate() {
        return this.certs[0];
    }

    @Override
    public Certificate[] getCertificateChain() {
        return this.certs;
    }

    @Override
    public String getAlgorithm() {
        return X509V3_SSH_DSS;
    }

    @Override
    public byte[] getEncoded() throws SshException {
        ByteArrayWriter writer = new ByteArrayWriter();
        try {
            writer.writeString(this.getAlgorithm());
            writer.writeInt(this.certs.length);
            for (Certificate c : this.certs) {
                writer.writeBinaryString(c.getEncoded());
            }
            writer.writeInt(0);
            byte[] byArray = writer.toByteArray();
            return byArray;
        }
        catch (CertificateEncodingException certificateEncodingException) {
            throw new SshException("Failed to encode certificate chain", 5, certificateEncodingException);
        }
        catch (IOException iOException) {
            throw new SshException("Failed to write certificate chain", 5, iOException);
        }
        finally {
            try {
                writer.close();
            }
            catch (IOException iOException) {}
        }
    }
}

