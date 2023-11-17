/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.keys;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.jose4j.base64url.Base64;
import org.jose4j.base64url.Base64Url;
import org.jose4j.base64url.SimplePEMEncoder;
import org.jose4j.lang.HashUtil;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;

public class X509Util {
    private static final String FACTORY_TYPE = "X.509";
    private CertificateFactory certFactory;

    public X509Util() {
        try {
            this.certFactory = CertificateFactory.getInstance(FACTORY_TYPE);
        }
        catch (CertificateException e) {
            throw new IllegalStateException("Couldn't find X.509 CertificateFactory!?!", e);
        }
    }

    public X509Util(String provider) throws NoSuchProviderException {
        try {
            this.certFactory = CertificateFactory.getInstance(FACTORY_TYPE, provider);
        }
        catch (CertificateException e) {
            throw new IllegalStateException("Couldn't find X.509 CertificateFactory!?!", e);
        }
    }

    public static X509Util getX509Util(String jcaProvider) throws JoseException {
        if (jcaProvider == null) {
            return new X509Util();
        }
        try {
            return new X509Util(jcaProvider);
        }
        catch (NoSuchProviderException e) {
            throw new JoseException("Provider " + jcaProvider + " not found when creating X509Util.", e);
        }
    }

    public String toBase64(X509Certificate x509Certificate) {
        try {
            byte[] der = x509Certificate.getEncoded();
            return Base64.encode(der);
        }
        catch (CertificateEncodingException e) {
            throw new IllegalStateException("Unexpected problem getting encoded certificate.", e);
        }
    }

    public String toPem(X509Certificate x509Certificate) {
        try {
            byte[] der = x509Certificate.getEncoded();
            return SimplePEMEncoder.encode(der);
        }
        catch (CertificateEncodingException e) {
            throw new IllegalStateException("Unexpected problem getting encoded certificate.", e);
        }
    }

    public X509Certificate fromBase64Der(String b64EncodedDer) throws JoseException {
        byte[] der = Base64.decode(b64EncodedDer);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(der);
        try {
            Certificate certificate = this.certFactory.generateCertificate(byteArrayInputStream);
            return (X509Certificate)certificate;
        }
        catch (CertificateException e) {
            throw new JoseException("Unable to convert " + b64EncodedDer + " value to X509Certificate: " + e, e);
        }
    }

    public static String x5t(X509Certificate certificate) {
        return X509Util.base64urlThumbprint(certificate, "SHA-1");
    }

    public static String x5tS256(X509Certificate certificate) {
        return X509Util.base64urlThumbprint(certificate, "SHA-256");
    }

    private static String base64urlThumbprint(X509Certificate certificate, String hashAlg) {
        byte[] certificateEncoded;
        MessageDigest msgDigest = HashUtil.getMessageDigest(hashAlg);
        try {
            certificateEncoded = certificate.getEncoded();
        }
        catch (CertificateEncodingException e) {
            throw new UncheckedJoseException("Unable to get certificate thumbprint due to unexpected certificate encoding exception.", e);
        }
        byte[] digest = msgDigest.digest(certificateEncoded);
        return Base64Url.encode(digest);
    }
}

