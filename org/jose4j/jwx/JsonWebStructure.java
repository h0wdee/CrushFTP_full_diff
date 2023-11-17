/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwx;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.Algorithm;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.jwx.Headers;
import org.jose4j.keys.X509Util;
import org.jose4j.lang.InvalidAlgorithmException;
import org.jose4j.lang.JoseException;

public abstract class JsonWebStructure {
    protected Base64Url base64url = new Base64Url();
    protected Headers headers = new Headers();
    private byte[] integrity;
    private Key key;
    protected boolean doKeyValidation = true;
    protected String rawCompactSerialization;
    private AlgorithmConstraints algorithmConstraints = AlgorithmConstraints.NO_CONSTRAINTS;
    private Set<String> knownCriticalHeaders = Collections.emptySet();
    private static final ProviderContext DEFAULT_PROVIDER_CONTEXT = new ProviderContext();
    private ProviderContext providerCtx = DEFAULT_PROVIDER_CONTEXT;

    public abstract String getCompactSerialization() throws JoseException;

    protected abstract void setCompactSerializationParts(String[] var1) throws JoseException;

    public abstract String getFlattenedJsonSerialization() throws JoseException;

    public abstract void setFlattenedJsonSerialization(String var1) throws JoseException;

    public abstract String getPayload() throws JoseException;

    public abstract void setPayload(String var1);

    public abstract Algorithm getAlgorithm() throws InvalidAlgorithmException;

    public abstract Algorithm getAlgorithmNoConstraintCheck() throws InvalidAlgorithmException;

    public static JsonWebStructure fromCompactSerialization(String cs) throws JoseException {
        JsonWebStructure jsonWebObject;
        String[] parts = CompactSerializer.deserialize(cs);
        if (parts.length == 5) {
            jsonWebObject = new JsonWebEncryption();
        } else if (parts.length == 3) {
            jsonWebObject = new JsonWebSignature();
        } else {
            throw new JoseException("Invalid JOSE Compact Serialization. Expecting either 3 or 5 parts for JWS or JWE respectively but was " + parts.length + ".");
        }
        ((JsonWebStructure)jsonWebObject).setCompactSerializationParts(parts);
        jsonWebObject.rawCompactSerialization = cs;
        return jsonWebObject;
    }

    public void setCompactSerialization(String compactSerialization) throws JoseException {
        String[] parts = CompactSerializer.deserialize(compactSerialization);
        this.setCompactSerializationParts(parts);
        this.rawCompactSerialization = compactSerialization;
    }

    public String getHeader() {
        return this.getHeaders().getFullHeaderAsJsonString();
    }

    protected String getEncodedHeader() {
        return this.headers.getEncodedHeader();
    }

    public void setHeader(String name, String value) {
        this.headers.setStringHeaderValue(name, value);
    }

    protected void setEncodedHeader(String encodedHeader) throws JoseException {
        this.checkNotEmptyPart(encodedHeader, "Encoded Header");
        this.headers.setEncodedHeader(encodedHeader);
    }

    public Headers getHeaders() {
        return this.headers;
    }

    protected void checkNotEmptyPart(String encodedPart, String partName) throws JoseException {
        if (encodedPart == null || encodedPart.length() == 0) {
            throw new JoseException("The " + partName + " cannot be empty.");
        }
    }

    public String getHeader(String name) {
        return this.headers.getStringHeaderValue(name);
    }

    public void setAlgorithmHeaderValue(String alg) {
        this.setHeader("alg", alg);
    }

    public String getAlgorithmHeaderValue() {
        return this.getHeader("alg");
    }

    public void setContentTypeHeaderValue(String cty) {
        this.setHeader("cty", cty);
    }

    public String getContentTypeHeaderValue() {
        return this.getHeader("cty");
    }

    public void setKeyIdHeaderValue(String kid) {
        this.setHeader("kid", kid);
    }

    public String getKeyIdHeaderValue() {
        return this.getHeader("kid");
    }

    public String getX509CertSha1ThumbprintHeaderValue() {
        return this.getHeader("x5t");
    }

    public void setX509CertSha1ThumbprintHeaderValue(String x5t) {
        this.setHeader("x5t", x5t);
    }

    public void setX509CertSha1ThumbprintHeaderValue(X509Certificate certificate) {
        String x5t = X509Util.x5t(certificate);
        this.setX509CertSha1ThumbprintHeaderValue(x5t);
    }

    public String getX509CertSha256ThumbprintHeaderValue() {
        return this.getHeader("x5t#S256");
    }

    public void setX509CertSha256ThumbprintHeaderValue(String x5tS256) {
        this.setHeader("x5t#S256", x5tS256);
    }

    public void setX509CertSha256ThumbprintHeaderValue(X509Certificate certificate) {
        String x5tS256 = X509Util.x5tS256(certificate);
        this.setX509CertSha256ThumbprintHeaderValue(x5tS256);
    }

    public Key getKey() {
        return this.key;
    }

    public void setKey(Key key) {
        boolean same;
        boolean bl = key == null ? this.key == null : (same = this.key != null && key.equals(this.key));
        if (!same) {
            this.onNewKey();
        }
        this.key = key;
    }

    protected void onNewKey() {
    }

    protected byte[] getIntegrity() {
        return this.integrity;
    }

    protected void setIntegrity(byte[] integrity) {
        this.integrity = integrity;
    }

    public boolean isDoKeyValidation() {
        return this.doKeyValidation;
    }

    public void setDoKeyValidation(boolean doKeyValidation) {
        this.doKeyValidation = doKeyValidation;
    }

    protected AlgorithmConstraints getAlgorithmConstraints() {
        return this.algorithmConstraints;
    }

    public void setAlgorithmConstraints(AlgorithmConstraints algorithmConstraints) {
        this.algorithmConstraints = algorithmConstraints;
    }

    public void setCriticalHeaderNames(String ... headerNames) {
        this.headers.setObjectHeaderValue("crit", headerNames);
    }

    public void setKnownCriticalHeaders(String ... knownCriticalHeaders) {
        this.knownCriticalHeaders = new HashSet<String>(Arrays.asList(knownCriticalHeaders));
    }

    protected void checkCrit() throws JoseException {
        Object criticalHeaderObjectValue = this.headers.getObjectHeaderValue("crit");
        if (criticalHeaderObjectValue != null) {
            try {
                for (String criticalHeader : (List)criticalHeaderObjectValue) {
                    if (this.knownCriticalHeaders.contains(criticalHeader)) continue;
                    throw new JoseException("Unrecognized header '" + criticalHeader + "' marked as critical.");
                }
            }
            catch (ClassCastException e) {
                throw new JoseException("crit header value not an array.");
            }
        }
    }

    protected ProviderContext getProviderCtx() {
        return this.providerCtx;
    }

    public void setProviderContext(ProviderContext providerCtx) {
        this.providerCtx = providerCtx;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(this.getHeaders().getFullHeaderAsJsonString());
        if (this.rawCompactSerialization != null) {
            sb.append("->").append(this.rawCompactSerialization);
        }
        return sb.toString();
    }
}

