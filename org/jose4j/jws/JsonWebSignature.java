/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jws;

import java.security.Key;
import java.util.Map;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmFactory;
import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.IntegrityException;
import org.jose4j.lang.InvalidAlgorithmException;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;

public class JsonWebSignature
extends JsonWebStructure {
    public static final short COMPACT_SERIALIZATION_PARTS = 3;
    private String payload;
    private String payloadCharEncoding = "UTF-8";
    private String encodedPayload;
    private Boolean validSignature;

    public JsonWebSignature() {
        if (!Boolean.getBoolean("org.jose4j.jws.default-allow-none")) {
            this.setAlgorithmConstraints(AlgorithmConstraints.DISALLOW_NONE);
        }
    }

    @Override
    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    protected void setCompactSerializationParts(String[] parts) throws JoseException {
        if (parts.length != 3) {
            throw new JoseException("A JWS Compact Serialization must have exactly 3 parts separated by period ('.') characters");
        }
        this.setEncodedHeader(parts[0]);
        this.setEncodedPayload(parts[1]);
        this.setSignature(this.base64url.base64UrlDecode(parts[2]));
    }

    @Override
    public String getCompactSerialization() throws JoseException {
        this.sign();
        return CompactSerializer.serialize(this.getSigningInput(), this.getEncodedSignature());
    }

    @Override
    public void setFlattenedJsonSerialization(String json) throws JoseException {
        Map<String, Object> parsedJson = JsonUtil.parseJson(json);
        if (!(parsedJson.containsKey("protected") && parsedJson.containsKey("payload") && parsedJson.containsKey("signature"))) {
            throw new JoseException("JWS does not contain all required fields ('protected', 'payload', 'signature'");
        }
        this.setEncodedHeader((String)parsedJson.get("protected"));
        this.setEncodedPayload((String)parsedJson.get("payload"));
        this.setSignature(this.base64url.base64UrlDecode((String)parsedJson.get("signature")));
    }

    @Override
    public String getFlattenedJsonSerialization() throws JoseException {
        this.sign();
        Map json = JsonUtil.CONTAINER_FACTORY.createObjectContainer();
        json.put("protected", this.getEncodedHeader());
        json.put("payload", this.getEncodedPayload());
        json.put("signature", this.getEncodedSignature());
        return JsonUtil.toJson(json);
    }

    public String getDetachedContentCompactSerialization() throws JoseException {
        this.sign();
        return CompactSerializer.serialize(this.getEncodedHeader(), "", this.getEncodedSignature());
    }

    public void sign() throws JoseException {
        JsonWebSignatureAlgorithm algorithm = this.getAlgorithm();
        Key signingKey = this.getKey();
        if (this.isDoKeyValidation()) {
            algorithm.validateSigningKey(signingKey);
        }
        byte[] inputBytes = this.getSigningInputBytes();
        byte[] signatureBytes = algorithm.sign(signingKey, inputBytes, this.getProviderCtx());
        this.setSignature(signatureBytes);
    }

    @Override
    protected void onNewKey() {
        this.validSignature = null;
    }

    public boolean verifySignature() throws JoseException {
        JsonWebSignatureAlgorithm algorithm = this.getAlgorithm();
        Key verificationKey = this.getKey();
        if (this.isDoKeyValidation()) {
            algorithm.validateVerificationKey(verificationKey);
        }
        if (this.validSignature == null) {
            this.checkCrit();
            byte[] signatureBytes = this.getSignature();
            byte[] inputBytes = this.getSigningInputBytes();
            this.validSignature = algorithm.verifySignature(signatureBytes, verificationKey, inputBytes, this.getProviderCtx());
        }
        return this.validSignature;
    }

    @Override
    public JsonWebSignatureAlgorithm getAlgorithm() throws InvalidAlgorithmException {
        return this.getAlgorithm(true);
    }

    @Override
    public JsonWebSignatureAlgorithm getAlgorithmNoConstraintCheck() throws InvalidAlgorithmException {
        return this.getAlgorithm(false);
    }

    private JsonWebSignatureAlgorithm getAlgorithm(boolean checkConstraints) throws InvalidAlgorithmException {
        String algo = this.getAlgorithmHeaderValue();
        if (algo == null) {
            throw new InvalidAlgorithmException("Signature algorithm header (alg) not set.");
        }
        if (checkConstraints) {
            this.getAlgorithmConstraints().checkConstraint(algo);
        }
        AlgorithmFactoryFactory factoryFactory = AlgorithmFactoryFactory.getInstance();
        AlgorithmFactory<JsonWebSignatureAlgorithm> jwsAlgorithmFactory = factoryFactory.getJwsAlgorithmFactory();
        return jwsAlgorithmFactory.getAlgorithm(algo);
    }

    private byte[] getSigningInputBytes() throws JoseException {
        String signingInput = this.getSigningInput();
        return StringUtil.getBytesAscii(signingInput);
    }

    private String getSigningInput() throws JoseException {
        return CompactSerializer.serialize(this.getEncodedHeader(), this.getEncodedPayload());
    }

    @Override
    public String getPayload() throws JoseException {
        if (!Boolean.getBoolean("org.jose4j.jws.getPayload-skip-verify") && !this.verifySignature()) {
            throw new IntegrityException("JWS signature is invalid.");
        }
        return this.payload;
    }

    public String getUnverifiedPayload() {
        return this.payload;
    }

    public String getPayloadCharEncoding() {
        return this.payloadCharEncoding;
    }

    public void setPayloadCharEncoding(String payloadCharEncoding) {
        this.payloadCharEncoding = payloadCharEncoding;
    }

    public String getKeyType() throws InvalidAlgorithmException {
        return this.getAlgorithmNoConstraintCheck().getKeyType();
    }

    public KeyPersuasion getKeyPersuasion() throws InvalidAlgorithmException {
        return this.getAlgorithmNoConstraintCheck().getKeyPersuasion();
    }

    public void setEncodedPayload(String encodedPayload) {
        this.encodedPayload = encodedPayload;
        this.setPayload(this.base64url.base64UrlDecodeToString(encodedPayload, this.payloadCharEncoding));
    }

    public String getEncodedPayload() {
        return this.encodedPayload != null ? this.encodedPayload : this.base64url.base64UrlEncode(this.payload, this.getPayloadCharEncoding());
    }

    public String getEncodedSignature() {
        return this.base64url.base64UrlEncode(this.getSignature());
    }

    protected byte[] getSignature() {
        return this.getIntegrity();
    }

    protected void setSignature(byte[] signature) {
        this.setIntegrity(signature);
    }
}

