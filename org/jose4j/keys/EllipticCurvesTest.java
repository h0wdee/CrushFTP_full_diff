/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.keys;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.keys.X509Util;
import org.junit.Assert;
import org.junit.Test;

public class EllipticCurvesTest {
    @Test
    public void testGetName() throws Exception {
        String b64d = "MIIBbjCCARKgAwIBAgIGAT0hzf2zMAwGCCqGSM49BAMCBQAwPDENMAsGA1UEBhMEbnVsbDErMCkGA1UEAxMiYXV0by1nZW5lcmF0ZWQgd3JhcHBlciBjZXJ0aWZpY2F0ZTAeFw0xMzAyMjgxNzE2MjBaFw0xNDAyMjgxNzE2MjBaMDwxDTALBgNVBAYTBG51bGwxKzApBgNVBAMTImF1dG8tZ2VuZXJhdGVkIHdyYXBwZXIgY2VydGlmaWNhdGUwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAARwLMpLp9BHKkFoGUE25feUccsQMJQY8JlFV7DIC596FBdjvcbxvfiStEDkcA4WOZThyQnPZlrPKqc2A4QuQRDmMAwGCCqGSM49BAMCBQADSAAwRQIhAPladiFs6XVS7fqfuvC8DEY0kmaoKWuGE30AA88NsIYzAiB9gUEGxDjEiLrjgjl9ds7n+7iBDhS4C5V2MpTG2QND5A==";
        X509Util x5u = new X509Util();
        X509Certificate x509Certificate = x5u.fromBase64Der(b64d);
        PublicKey publicKey = x509Certificate.getPublicKey();
        ECPublicKey ecPublicKey = (ECPublicKey)publicKey;
        String name = EllipticCurves.getName(ecPublicKey.getParams().getCurve());
        Assert.assertEquals((Object)"P-256", (Object)name);
    }

    @Test
    public void testNames() throws Exception {
        LinkedHashMap<String, String> names = new LinkedHashMap<String, String>();
        names.put("secp256r1", "P-256");
        names.put("secp384r1", "P-384");
        names.put("secp521r1", "P-521");
        for (Map.Entry e : names.entrySet()) {
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec((String)e.getKey());
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(ecGenParameterSpec);
            KeyPair keyPair = kpg.generateKeyPair();
            ECPublicKey ecpub = (ECPublicKey)keyPair.getPublic();
            ECParameterSpec params = ecpub.getParams();
            Assert.assertEquals(e.getValue(), (Object)EllipticCurves.getName(params.getCurve()));
        }
    }
}

