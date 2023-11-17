/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.security.Key;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

public class OctetSequenceJsonWebKey
extends JsonWebKey {
    public static final String KEY_TYPE = "oct";
    public static final String KEY_VALUE_MEMBER_NAME = "k";
    private byte[] octetSequence;

    public OctetSequenceJsonWebKey(Key key) {
        super(key);
        this.octetSequence = key.getEncoded();
    }

    public OctetSequenceJsonWebKey(Map<String, Object> params) throws JoseException {
        super(params);
        Base64Url base64Url = new Base64Url();
        String b64KeyBytes = OctetSequenceJsonWebKey.getStringRequired(params, KEY_VALUE_MEMBER_NAME);
        this.octetSequence = base64Url.base64UrlDecode(b64KeyBytes);
        String alg = "AES";
        this.key = new SecretKeySpec(this.octetSequence, alg);
        this.removeFromOtherParams(KEY_VALUE_MEMBER_NAME);
    }

    @Override
    public String getKeyType() {
        return KEY_TYPE;
    }

    public byte[] getOctetSequence() {
        return this.octetSequence;
    }

    private String getEncoded() {
        return Base64Url.encode(this.octetSequence);
    }

    @Override
    protected void fillTypeSpecificParams(Map<String, Object> params, JsonWebKey.OutputControlLevel outputLevel) {
        if (JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC.compareTo(outputLevel) >= 0) {
            params.put(KEY_VALUE_MEMBER_NAME, this.getEncoded());
        }
    }

    @Override
    protected String produceThumbprintHashInput() {
        String template = "{\"k\":\"%s\",\"kty\":\"oct\"}";
        String k = this.getEncoded();
        return String.format(template, k);
    }
}

