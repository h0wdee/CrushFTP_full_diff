/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.util.Collection;
import java.util.List;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.SelectorSupport;
import org.jose4j.jwk.SimpleJwkFilter;
import org.jose4j.lang.JoseException;

public class DecryptionJwkSelector {
    public JsonWebKey select(JsonWebEncryption jwe, Collection<JsonWebKey> keys) throws JoseException {
        List<JsonWebKey> jsonWebKeys = this.selectList(jwe, keys);
        return jsonWebKeys.isEmpty() ? null : jsonWebKeys.get(0);
    }

    public List<JsonWebKey> selectList(JsonWebEncryption jwe, Collection<JsonWebKey> keys) throws JoseException {
        SimpleJwkFilter filter = SelectorSupport.commonFilterForInbound(jwe);
        return filter.filter(keys);
    }
}

