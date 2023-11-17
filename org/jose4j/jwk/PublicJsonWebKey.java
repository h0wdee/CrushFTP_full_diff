/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.math.BigInteger;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.BigEndianBigInteger;
import org.jose4j.keys.X509Util;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.JsonHelp;

public abstract class PublicJsonWebKey
extends JsonWebKey {
    public static final String X509_CERTIFICATE_CHAIN_PARAMETER = "x5c";
    public static final String X509_THUMBPRINT_PARAMETER = "x5t";
    public static final String X509_SHA256_THUMBPRINT_PARAMETER = "x5t#S256";
    public static final String X509_URL_PARAMETER = "x5u";
    protected boolean writeOutPrivateKeyToJson;
    protected PrivateKey privateKey;
    protected String jcaProvider;
    private List<X509Certificate> certificateChain;
    private String x5t;
    private String x5tS256;
    private String x5u;

    protected PublicJsonWebKey(PublicKey publicKey) {
        super(publicKey);
    }

    protected PublicJsonWebKey(Map<String, Object> params) throws JoseException {
        this(params, null);
    }

    protected PublicJsonWebKey(Map<String, Object> params, String jcaProvider) throws JoseException {
        super(params);
        this.jcaProvider = jcaProvider;
        if (params.containsKey(X509_CERTIFICATE_CHAIN_PARAMETER)) {
            List<String> x5cStrings = JsonHelp.getStringArray(params, X509_CERTIFICATE_CHAIN_PARAMETER);
            this.certificateChain = new ArrayList<X509Certificate>(x5cStrings.size());
            X509Util x509Util = X509Util.getX509Util(jcaProvider);
            for (String b64EncodedDer : x5cStrings) {
                X509Certificate x509Certificate = x509Util.fromBase64Der(b64EncodedDer);
                this.certificateChain.add(x509Certificate);
            }
        }
        this.x5t = PublicJsonWebKey.getString(params, X509_THUMBPRINT_PARAMETER);
        this.x5tS256 = PublicJsonWebKey.getString(params, X509_SHA256_THUMBPRINT_PARAMETER);
        this.x5u = PublicJsonWebKey.getString(params, X509_URL_PARAMETER);
        this.removeFromOtherParams(X509_CERTIFICATE_CHAIN_PARAMETER, X509_SHA256_THUMBPRINT_PARAMETER, X509_THUMBPRINT_PARAMETER, X509_URL_PARAMETER);
    }

    protected abstract void fillPublicTypeSpecificParams(Map<String, Object> var1);

    protected abstract void fillPrivateTypeSpecificParams(Map<String, Object> var1);

    @Override
    protected void fillTypeSpecificParams(Map<String, Object> params, JsonWebKey.OutputControlLevel outputLevel) {
        this.fillPublicTypeSpecificParams(params);
        if (this.certificateChain != null) {
            X509Util x509Util = new X509Util();
            ArrayList<String> x5cStrings = new ArrayList<String>(this.certificateChain.size());
            for (X509Certificate cert : this.certificateChain) {
                String b64EncodedDer = x509Util.toBase64(cert);
                x5cStrings.add(b64EncodedDer);
            }
            params.put(X509_CERTIFICATE_CHAIN_PARAMETER, x5cStrings);
        }
        this.putIfNotNull(X509_THUMBPRINT_PARAMETER, this.x5t, params);
        this.putIfNotNull(X509_SHA256_THUMBPRINT_PARAMETER, this.x5tS256, params);
        this.putIfNotNull(X509_URL_PARAMETER, this.x5u, params);
        if (this.writeOutPrivateKeyToJson || outputLevel == JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE) {
            this.fillPrivateTypeSpecificParams(params);
        }
    }

    @Override
    public PublicKey getPublicKey() {
        return (PublicKey)this.key;
    }

    public void setWriteOutPrivateKeyToJson(boolean writeOutPrivateKeyToJson) {
        this.writeOutPrivateKeyToJson = writeOutPrivateKeyToJson;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public List<X509Certificate> getCertificateChain() {
        return this.certificateChain;
    }

    public X509Certificate getLeafCertificate() {
        return this.certificateChain != null && !this.certificateChain.isEmpty() ? this.certificateChain.get(0) : null;
    }

    public String getX509CertificateSha1Thumbprint() {
        return this.getX509CertificateSha1Thumbprint(false);
    }

    public String getX509CertificateSha1Thumbprint(boolean allowFallbackDeriveFromX5c) {
        X509Certificate leafCertificate;
        String result = this.x5t;
        if (result == null && allowFallbackDeriveFromX5c && (leafCertificate = this.getLeafCertificate()) != null) {
            result = X509Util.x5t(leafCertificate);
        }
        return result;
    }

    public String getX509CertificateSha256Thumbprint() {
        return this.getX509CertificateSha256Thumbprint(false);
    }

    public String getX509CertificateSha256Thumbprint(boolean allowFallbackDeriveFromX5c) {
        X509Certificate leafCertificate;
        String result = this.x5tS256;
        if (result == null && allowFallbackDeriveFromX5c && (leafCertificate = this.getLeafCertificate()) != null) {
            result = X509Util.x5tS256(leafCertificate);
        }
        return result;
    }

    public String getX509Url() {
        return this.x5u;
    }

    public void setCertificateChain(List<X509Certificate> certificateChain) {
        this.checkForBareKeyCertMismatch();
        this.certificateChain = certificateChain;
    }

    public void setX509CertificateSha1Thumbprint(String x5t) {
        this.x5t = x5t;
    }

    public void setX509CertificateSha256Thumbprint(String x5tS2) {
        this.x5tS256 = x5tS2;
    }

    public void setX509Url(String x5u) {
        this.x5u = x5u;
    }

    void checkForBareKeyCertMismatch() {
        boolean certAndBareKeyMismatch;
        X509Certificate leafCertificate = this.getLeafCertificate();
        boolean bl = certAndBareKeyMismatch = leafCertificate != null && !leafCertificate.getPublicKey().equals(this.getPublicKey());
        if (certAndBareKeyMismatch) {
            throw new IllegalArgumentException("The key in the first certificate MUST match the bare public key represented by other members of the JWK. Public key = " + this.getPublicKey() + " cert = " + leafCertificate);
        }
    }

    public void setCertificateChain(X509Certificate ... certificates) {
        this.setCertificateChain(Arrays.asList(certificates));
    }

    BigInteger getBigIntFromBase64UrlEncodedParam(Map<String, Object> params, String parameterName, boolean required) throws JoseException {
        String base64UrlValue = PublicJsonWebKey.getString(params, parameterName, required);
        return BigEndianBigInteger.fromBase64Url(base64UrlValue);
    }

    void putBigIntAsBase64UrlEncodedParam(Map<String, Object> params, String parameterName, BigInteger value) {
        String base64UrlValue = BigEndianBigInteger.toBase64Url(value);
        params.put(parameterName, base64UrlValue);
    }

    void putBigIntAsBase64UrlEncodedParam(Map<String, Object> params, String parameterName, BigInteger value, int minLength) {
        String base64UrlValue = BigEndianBigInteger.toBase64Url(value, minLength);
        params.put(parameterName, base64UrlValue);
    }

    public static class Factory {
        public static PublicJsonWebKey newPublicJwk(Map<String, Object> params, String jcaProvider) throws JoseException {
            String kty;
            switch (kty = PublicJsonWebKey.getStringRequired(params, "kty")) {
                case "RSA": {
                    return new RsaJsonWebKey(params, jcaProvider);
                }
                case "EC": {
                    return new EllipticCurveJsonWebKey(params, jcaProvider);
                }
            }
            throw new JoseException("Unknown key type (for public keys): '" + kty + "'");
        }

        public static PublicJsonWebKey newPublicJwk(Map<String, Object> params) throws JoseException {
            return Factory.newPublicJwk(params, null);
        }

        public static PublicJsonWebKey newPublicJwk(Key publicKey) throws JoseException {
            JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(publicKey);
            return (PublicJsonWebKey)jsonWebKey;
        }

        public static PublicJsonWebKey newPublicJwk(String json) throws JoseException {
            return Factory.newPublicJwk(json, null);
        }

        public static PublicJsonWebKey newPublicJwk(String json, String jcaProvider) throws JoseException {
            Map<String, Object> parsed = JsonUtil.parseJson(json);
            return Factory.newPublicJwk(parsed, jcaProvider);
        }
    }
}

