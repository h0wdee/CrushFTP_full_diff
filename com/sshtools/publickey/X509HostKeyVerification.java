/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.publickey;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshX509PublicKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.util.Arrays;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class X509HostKeyVerification
implements HostKeyVerification {
    static Logger log = LoggerFactory.getLogger(X509HostKeyVerification.class);
    PKIXParameters params;

    public X509HostKeyVerification(boolean enableRevocation) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, InvalidAlgorithmParameterException {
        String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
        FileInputStream is = new FileInputStream(filename);
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        String password = AdaptiveConfiguration.getProperty("trustedCACertsPassword", "changeit", new String[0]);
        keystore.load(is, password.toCharArray());
        this.params = new PKIXParameters(keystore);
        this.params.setRevocationEnabled(enableRevocation);
    }

    public X509HostKeyVerification(Set<TrustAnchor> trustAnchors, boolean enableRevocation) throws InvalidAlgorithmParameterException {
        this.params = new PKIXParameters(trustAnchors);
        this.params.setRevocationEnabled(enableRevocation);
    }

    @Override
    public boolean verifyHost(String host, SshPublicKey pk) throws SshException {
        if (pk instanceof SshX509PublicKey) {
            SshX509PublicKey x509 = (SshX509PublicKey)((Object)pk);
            try {
                return this.validateChain(x509.getCertificateChain());
            }
            catch (Exception e) {
                log.error("Failed to validate certificate chain", (Throwable)e);
            }
        }
        return false;
    }

    private boolean validateChain(Certificate[] certificates) throws CertificateException, NoSuchAlgorithmException, CertPathValidatorException, InvalidAlgorithmParameterException {
        Boolean valid = Boolean.FALSE;
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        CertPath certPath = cf.generateCertPath(Arrays.asList(certificates));
        CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");
        PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult)certPathValidator.validate(certPath, this.params);
        if (null != result) {
            valid = Boolean.TRUE;
        }
        return valid;
    }
}

