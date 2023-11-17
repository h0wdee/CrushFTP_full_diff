/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

public class JsonWebKeySet {
    public static final String JWK_SET_MEMBER_NAME = "keys";
    private List<JsonWebKey> keys;

    public JsonWebKeySet(String json) throws JoseException {
        Map<String, Object> parsed = JsonUtil.parseJson(json);
        List jwkParamMapList = (List)parsed.get(JWK_SET_MEMBER_NAME);
        if (jwkParamMapList == null) {
            throw new JoseException("The JSON JWKS content does not include the keys member.");
        }
        this.keys = new ArrayList<JsonWebKey>(jwkParamMapList.size());
        for (Map jwkParamsMap : jwkParamMapList) {
            try {
                JsonWebKey jwk = JsonWebKey.Factory.newJwk(jwkParamsMap);
                this.keys.add(jwk);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public JsonWebKeySet(JsonWebKey ... keys) {
        this(Arrays.asList(keys));
    }

    public JsonWebKeySet(List<? extends JsonWebKey> keys) {
        this.keys = new ArrayList<JsonWebKey>(keys.size());
        for (JsonWebKey jsonWebKey : keys) {
            this.keys.add(jsonWebKey);
        }
    }

    public void addJsonWebKey(JsonWebKey jsonWebKey) {
        this.keys.add(jsonWebKey);
    }

    public List<JsonWebKey> getJsonWebKeys() {
        return this.keys;
    }

    public JsonWebKey findJsonWebKey(String keyId, String keyType, String use, String algorithm) {
        List<JsonWebKey> found = this.findJsonWebKeys(keyId, keyType, use, algorithm);
        return found.isEmpty() ? null : found.iterator().next();
    }

    public List<JsonWebKey> findJsonWebKeys(String keyId, String keyType, String use, String algorithm) {
        ArrayList<JsonWebKey> found = new ArrayList<JsonWebKey>();
        for (JsonWebKey jwk : this.keys) {
            boolean isMeetsCriteria = true;
            if (keyId != null) {
                isMeetsCriteria = keyId.equals(jwk.getKeyId());
            }
            if (use != null) {
                isMeetsCriteria &= use.equals(jwk.getUse());
            }
            if (keyType != null) {
                isMeetsCriteria &= keyType.equals(jwk.getKeyType());
            }
            if (algorithm != null) {
                isMeetsCriteria &= algorithm.equals(jwk.getAlgorithm());
            }
            if (!isMeetsCriteria) continue;
            found.add(jwk);
        }
        return found;
    }

    public String toJson() {
        return this.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
    }

    public String toJson(JsonWebKey.OutputControlLevel outputControlLevel) {
        LinkedList<Map<String, Object>> keyList = new LinkedList<Map<String, Object>>();
        for (JsonWebKey key : this.keys) {
            Map<String, Object> params = key.toParams(outputControlLevel);
            keyList.add(params);
        }
        LinkedHashMap<String, LinkedList<Map<String, Object>>> jwks = new LinkedHashMap<String, LinkedList<Map<String, Object>>>();
        jwks.put(JWK_SET_MEMBER_NAME, keyList);
        return JsonUtil.toJson(jwks);
    }
}

