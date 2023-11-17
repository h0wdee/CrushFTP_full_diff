/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwx;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jose4j.base64url.Base64Url;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.JsonHelp;

public class Headers {
    protected Base64Url base64url = new Base64Url();
    private Map<String, Object> headerMap = new LinkedHashMap<String, Object>();
    private String header;
    private String encodedHeader;

    public String getFullHeaderAsJsonString() {
        if (this.header == null) {
            this.header = JsonUtil.toJson(this.headerMap);
        }
        return this.header;
    }

    public String getEncodedHeader() {
        if (this.encodedHeader == null) {
            String headerAsString = this.getFullHeaderAsJsonString();
            this.encodedHeader = this.base64url.base64UrlEncodeUtf8ByteRepresentation(headerAsString);
        }
        return this.encodedHeader;
    }

    public void setStringHeaderValue(String name, String value) {
        this.setObjectHeaderValue(name, value);
    }

    public void setObjectHeaderValue(String name, Object value) {
        this.headerMap.put(name, value);
        this.header = null;
        this.encodedHeader = null;
    }

    public void setJwkHeaderValue(String name, JsonWebKey jwk) {
        Map<String, Object> jwkParams = jwk.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        this.setObjectHeaderValue(name, jwkParams);
    }

    public String getStringHeaderValue(String headerName) {
        return JsonHelp.getString(this.headerMap, headerName);
    }

    public Long getLongHeaderValue(String headerName) {
        return JsonHelp.getLong(this.headerMap, headerName);
    }

    public Object getObjectHeaderValue(String name) {
        return this.headerMap.get(name);
    }

    public JsonWebKey getJwkHeaderValue(String name) throws JoseException {
        Object objectHeaderValue = this.getObjectHeaderValue(name);
        Map jwkParams = (Map)objectHeaderValue;
        return JsonWebKey.Factory.newJwk(jwkParams);
    }

    public PublicJsonWebKey getPublicJwkHeaderValue(String name, String jcaProvider) throws JoseException {
        Object objectHeaderValue = this.getObjectHeaderValue(name);
        Map jwkParams = (Map)objectHeaderValue;
        return PublicJsonWebKey.Factory.newPublicJwk(jwkParams, jcaProvider);
    }

    public void setFullHeaderAsJsonString(String header) throws JoseException {
        this.encodedHeader = null;
        this.header = header;
        this.headerMap = JsonUtil.parseJson(header);
    }

    void setEncodedHeader(String encodedHeader) throws JoseException {
        this.encodedHeader = encodedHeader;
        this.setFullHeaderAsJsonString(this.base64url.base64UrlDecodeToUtf8String(this.encodedHeader));
    }
}

