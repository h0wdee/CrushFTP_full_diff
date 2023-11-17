/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.io.Serializable;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jose4j.base64url.Base64Url;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.lang.HashUtil;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.JsonHelp;
import org.jose4j.lang.StringUtil;

public abstract class JsonWebKey
implements Serializable {
    public static final String KEY_TYPE_PARAMETER = "kty";
    public static final String USE_PARAMETER = "use";
    public static final String KEY_ID_PARAMETER = "kid";
    public static final String ALGORITHM_PARAMETER = "alg";
    public static final String KEY_OPERATIONS = "key_ops";
    private String use;
    private String keyId;
    private String algorithm;
    private List<String> keyOps;
    protected Map<String, Object> otherParameters = new LinkedHashMap<String, Object>();
    protected Key key;

    protected JsonWebKey(Key key) {
        this.key = key;
    }

    protected JsonWebKey(Map<String, Object> params) throws JoseException {
        this.otherParameters.putAll(params);
        this.removeFromOtherParams(KEY_TYPE_PARAMETER, USE_PARAMETER, KEY_ID_PARAMETER, ALGORITHM_PARAMETER, KEY_OPERATIONS);
        this.setUse(JsonWebKey.getString(params, USE_PARAMETER));
        this.setKeyId(JsonWebKey.getString(params, KEY_ID_PARAMETER));
        this.setAlgorithm(JsonWebKey.getString(params, ALGORITHM_PARAMETER));
        if (params.containsKey(KEY_OPERATIONS)) {
            this.keyOps = JsonHelp.getStringArray(params, KEY_OPERATIONS);
        }
    }

    public abstract String getKeyType();

    protected abstract void fillTypeSpecificParams(Map<String, Object> var1, OutputControlLevel var2);

    public PublicKey getPublicKey() {
        try {
            return (PublicKey)this.key;
        }
        catch (Exception e) {
            return null;
        }
    }

    public Key getKey() {
        return this.key;
    }

    public String getUse() {
        return this.use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getKeyId() {
        return this.keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public List<String> getKeyOps() {
        return this.keyOps;
    }

    public void setKeyOps(List<String> keyOps) {
        this.keyOps = keyOps;
    }

    public void setOtherParameter(String name, Object value) {
        this.otherParameters.put(name, value);
    }

    public <T> T getOtherParameterValue(String name, Class<T> type) {
        Object o = this.otherParameters.get(name);
        return type.cast(o);
    }

    protected void removeFromOtherParams(String ... names) {
        String[] stringArray = names;
        int n = names.length;
        int n2 = 0;
        while (n2 < n) {
            String name = stringArray[n2];
            this.otherParameters.remove(name);
            ++n2;
        }
    }

    public Map<String, Object> toParams(OutputControlLevel outputLevel) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(KEY_TYPE_PARAMETER, this.getKeyType());
        this.putIfNotNull(KEY_ID_PARAMETER, this.getKeyId(), params);
        this.putIfNotNull(USE_PARAMETER, this.getUse(), params);
        this.putIfNotNull(KEY_OPERATIONS, this.keyOps, params);
        this.putIfNotNull(ALGORITHM_PARAMETER, this.getAlgorithm(), params);
        this.fillTypeSpecificParams(params, outputLevel);
        params.putAll(this.otherParameters);
        return params;
    }

    public String toJson() {
        return this.toJson(OutputControlLevel.INCLUDE_SYMMETRIC);
    }

    public String toJson(OutputControlLevel outputLevel) {
        Map<String, Object> params = this.toParams(outputLevel);
        return JsonUtil.toJson(params);
    }

    public String toString() {
        return String.valueOf(this.getClass().getName()) + this.toParams(OutputControlLevel.PUBLIC_ONLY);
    }

    public String calculateBase64urlEncodedThumbprint(String hashAlgorithm) {
        byte[] thumbprint = this.calculateThumbprint(hashAlgorithm);
        return Base64Url.encode(thumbprint);
    }

    public byte[] calculateThumbprint(String hashAlgorithm) {
        MessageDigest digest = HashUtil.getMessageDigest(hashAlgorithm);
        String hashInputString = this.produceThumbprintHashInput();
        byte[] hashInputBytes = StringUtil.getBytesUtf8(hashInputString);
        return digest.digest(hashInputBytes);
    }

    protected abstract String produceThumbprintHashInput();

    protected void putIfNotNull(String name, Object value, Map<String, Object> params) {
        if (value != null) {
            params.put(name, value);
        }
    }

    protected static String getString(Map<String, Object> params, String name) throws JoseException {
        return JsonHelp.getStringChecked(params, name);
    }

    protected static String getStringRequired(Map<String, Object> params, String name) throws JoseException {
        return JsonWebKey.getString(params, name, true);
    }

    protected static String getString(Map<String, Object> params, String name, boolean required) throws JoseException {
        String value = JsonWebKey.getString(params, name);
        if (value == null && required) {
            throw new JoseException("Missing required '" + name + "' parameter.");
        }
        return value;
    }

    public static class Factory {
        public static JsonWebKey newJwk(Map<String, Object> params) throws JoseException {
            String kty;
            switch (kty = JsonWebKey.getStringRequired(params, JsonWebKey.KEY_TYPE_PARAMETER)) {
                case "RSA": {
                    return new RsaJsonWebKey(params);
                }
                case "EC": {
                    return new EllipticCurveJsonWebKey(params);
                }
                case "oct": {
                    return new OctetSequenceJsonWebKey(params);
                }
            }
            throw new JoseException("Unknown key type algorithm: '" + kty + "'");
        }

        public static JsonWebKey newJwk(Key key) throws JoseException {
            if (RSAPublicKey.class.isInstance(key)) {
                return new RsaJsonWebKey((RSAPublicKey)key);
            }
            if (ECPublicKey.class.isInstance(key)) {
                return new EllipticCurveJsonWebKey((ECPublicKey)key);
            }
            if (PublicKey.class.isInstance(key)) {
                throw new JoseException("Unsupported or unknown public key " + key);
            }
            return new OctetSequenceJsonWebKey(key);
        }

        public static JsonWebKey newJwk(String json) throws JoseException {
            Map<String, Object> parsed = JsonUtil.parseJson(json);
            return Factory.newJwk(parsed);
        }
    }

    public static enum OutputControlLevel {
        INCLUDE_PRIVATE,
        INCLUDE_SYMMETRIC,
        PUBLIC_ONLY;

    }
}

