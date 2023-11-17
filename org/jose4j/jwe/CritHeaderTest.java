/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwe;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.keys.AesKey;
import org.junit.Assert;
import org.junit.Test;

public class CritHeaderTest {
    @Test
    public void testOnNewKey() throws Exception {
        String headerName = "so.crit";
        String otherHeaderName = "very.crit";
        byte[] byArray = new byte[16];
        byArray[0] = 1;
        byArray[1] = 2;
        byArray[2] = 3;
        byArray[3] = 4;
        byArray[4] = 5;
        byArray[5] = 6;
        byArray[6] = 7;
        byArray[7] = 8;
        byArray[8] = 9;
        byArray[10] = 1;
        byArray[11] = 2;
        byArray[12] = 3;
        byArray[13] = 4;
        byArray[14] = 5;
        byArray[15] = 6;
        AesKey key = new AesKey(byArray);
        String jwecs = "eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2Iiwic28uY3JpdCI6InllcCIsInZlcnkuY3JpdCI6ImVoIiwid2hhdCI6ImV2ZXIiLCJjcml0IjpbInNvLmNyaXQiXX0.kMto4viJ7TE6F9r6BuY7SJVRG04sJJlzCc0N2A-lZBxh5t3hGWTuJA.z3A09USgPKx-aR7hnVPzgA.edAUVi0TmIIPg84LyIbtXQ.waKimIov2wwgINaQ2gWPMA";
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setCompactSerialization(jwecs);
        jwe.setKey(key);
        org.jose4j.jws.CritHeaderTest.expectFail(jwe);
        jwe = new JsonWebEncryption();
        jwe.setCompactSerialization(jwecs);
        jwe.setKey(key);
        jwe.setKnownCriticalHeaders("so.crit", "very.crit");
        Assert.assertThat((Object)"Delayed in ORD", (Matcher)CoreMatchers.equalTo((Object)jwe.getPayload()));
    }
}

