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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.junit.Assert;
import org.junit.Test;

public class MaintainEncodedPayloadTest {
    @Test
    public void testOddEncodedPayload() throws Exception {
        String funkyToken = "eyJhbGciOiJSUzI1NiJ9.IVRoaXMgaXMgbm8gbG9uZ2VyIGEgdmFjYXRpb24uX.f6qDgGZ8tCVZ_DhlFwWAZvV-Vv5yQOFSAXVv98vOpgkI6YQd6hjCWaeyaWbMWhV__uiWiEY0SutaQw1y71bXvRPfy12YKpyIlRwvos9L5myA--GGc6o88hDjxxc2PLhhhNazR1aSVXIb6wF4PJENb10XDMIuMj9wtzDVnLajS5O3Ptygwx39bRa9XoXrAxbSyEBJSV9nVCQS-wPRaEudDcLRQhKVhMHYJ-3UZn0VVpCz_8KWvw4JOB9jWntS85CPF4RcUaepQJ2pz-8gfCrv2qKHKU36FbmqOwKoQZL1dLXH1wp33k7ESt5zivLVPli3tPDVfBa5BmWAMO1mydqGgw";
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization("eyJhbGciOiJSUzI1NiJ9.IVRoaXMgaXMgbm8gbG9uZ2VyIGEgdmFjYXRpb24uX.f6qDgGZ8tCVZ_DhlFwWAZvV-Vv5yQOFSAXVv98vOpgkI6YQd6hjCWaeyaWbMWhV__uiWiEY0SutaQw1y71bXvRPfy12YKpyIlRwvos9L5myA--GGc6o88hDjxxc2PLhhhNazR1aSVXIb6wF4PJENb10XDMIuMj9wtzDVnLajS5O3Ptygwx39bRa9XoXrAxbSyEBJSV9nVCQS-wPRaEudDcLRQhKVhMHYJ-3UZn0VVpCz_8KWvw4JOB9jWntS85CPF4RcUaepQJ2pz-8gfCrv2qKHKU36FbmqOwKoQZL1dLXH1wp33k7ESt5zivLVPli3tPDVfBa5BmWAMO1mydqGgw");
        jws.setKey(ExampleRsaKeyFromJws.PUBLIC_KEY);
        Assert.assertThat((Object)jws.getPayload(), (Matcher)CoreMatchers.equalTo((Object)"!This is no longer a vacation."));
    }
}

