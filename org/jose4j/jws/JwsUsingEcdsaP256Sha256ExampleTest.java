/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jws;

import junit.framework.TestCase;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.lang.JoseException;

public class JwsUsingEcdsaP256Sha256ExampleTest
extends TestCase {
    String JWS = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.DtEhU3ljbEg8L38VWAfUAqOyKAM6-Xx-F4GawxaepmXFCgfTjDxw5djxLa8ISlSApmWQxfKTUJqPP3-Kg6NU1Q";

    public void testVerifyExample() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(this.JWS);
        jws.setKey(ExampleEcKeysFromJws.PUBLIC_256);
        JwsUsingEcdsaP256Sha256ExampleTest.assertTrue((String)"signature should validate", (boolean)jws.verifySignature());
    }
}

