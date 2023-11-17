/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jws;

import java.security.Key;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class ChangingKeyTest {
    @Test
    public void testOnNewKey() throws Exception {
        JsonWebKey jwk = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"9el2Km2s5LHVQqUCWIdvwMsclQqQc6CwObMnCpCC8jY\"}");
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization("eyJhbGciOiJIUzI1NiJ9.c2lnaA.2yUt5UtfsRK1pnN0KTTv7gzHTxwDqDz2OkFSqlbQ40A");
        jws.setKey(new HmacKey(new byte[32]));
        Assert.assertThat((Object)false, (Matcher)CoreMatchers.equalTo((Object)jws.verifySignature()));
        jws.setKey(jwk.getKey());
        Assert.assertThat((Object)true, (Matcher)CoreMatchers.equalTo((Object)jws.verifySignature()));
        jws.setKey(new HmacKey(ByteUtil.randomBytes(32)));
        Assert.assertThat((Object)false, (Matcher)CoreMatchers.equalTo((Object)jws.verifySignature()));
        jws.setKey(null);
        try {
            jws.verifySignature();
        }
        catch (JoseException joseException) {
            // empty catch block
        }
    }

    @Test
    public void testSetKeyWithNPEonEqualsImpl() {
        Key key = new Key(){

            @Override
            public String getAlgorithm() {
                return null;
            }

            @Override
            public String getFormat() {
                return null;
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }

            public boolean equals(Object obj) {
                if (obj == null) {
                    throw new NullPointerException();
                }
                return super.equals(obj);
            }
        };
        JsonWebSignature jws = new JsonWebSignature();
        jws.setKey(key);
    }
}

