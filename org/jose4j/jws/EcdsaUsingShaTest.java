/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jws;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import junit.framework.TestCase;
import org.jose4j.jws.JwsTestSupport;
import org.jose4j.keys.EcKeyUtil;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

public class EcdsaUsingShaTest
extends TestCase {
    EcKeyUtil keyUtil = new EcKeyUtil();

    public void testP256RoundTripGenKeys() throws JoseException {
        KeyPair keyPair1 = this.keyUtil.generateKeyPair(EllipticCurves.P256);
        KeyPair keyPair2 = this.keyUtil.generateKeyPair(EllipticCurves.P256);
        String algo = "ES256";
        PrivateKey priv1 = keyPair1.getPrivate();
        PublicKey pub1 = keyPair1.getPublic();
        PrivateKey priv2 = keyPair2.getPrivate();
        PublicKey pub2 = keyPair2.getPublic();
        JwsTestSupport.testBasicRoundTrip("PAYLOAD!!!", algo, priv1, pub1, priv2, pub2);
    }

    public void testP384RoundTripGenKeys() throws JoseException {
        KeyPair keyPair1 = this.keyUtil.generateKeyPair(EllipticCurves.P384);
        KeyPair keyPair2 = this.keyUtil.generateKeyPair(EllipticCurves.P384);
        String algo = "ES384";
        PrivateKey priv1 = keyPair1.getPrivate();
        PublicKey pub1 = keyPair1.getPublic();
        PrivateKey priv2 = keyPair2.getPrivate();
        PublicKey pub2 = keyPair2.getPublic();
        JwsTestSupport.testBasicRoundTrip("The umlaut ( /??mla?t/ uum-lowt) refers to a sound shift.", algo, priv1, pub1, priv2, pub2);
    }

    public void testP521RoundTripGenKeys() throws JoseException {
        KeyPair keyPair1 = this.keyUtil.generateKeyPair(EllipticCurves.P521);
        KeyPair keyPair2 = this.keyUtil.generateKeyPair(EllipticCurves.P521);
        String algo = "ES512";
        PrivateKey priv1 = keyPair1.getPrivate();
        PublicKey pub1 = keyPair1.getPublic();
        PrivateKey priv2 = keyPair2.getPrivate();
        PublicKey pub2 = keyPair2.getPublic();
        JwsTestSupport.testBasicRoundTrip("?????", algo, priv1, pub1, priv2, pub2);
    }

    public void testP256RoundTripExampleKeysAndGenKeys() throws JoseException {
        String algo = "ES256";
        ECPrivateKey priv1 = ExampleEcKeysFromJws.PRIVATE_256;
        ECPublicKey pub1 = ExampleEcKeysFromJws.PUBLIC_256;
        KeyPair keyPair = this.keyUtil.generateKeyPair(EllipticCurves.P256);
        PrivateKey priv2 = keyPair.getPrivate();
        PublicKey pub2 = keyPair.getPublic();
        JwsTestSupport.testBasicRoundTrip("something here", algo, priv1, pub1, priv2, pub2);
    }

    public void testP521RoundTripExampleKeysAndGenKeys() throws JoseException {
        String algo = "ES512";
        ECPrivateKey priv1 = ExampleEcKeysFromJws.PRIVATE_521;
        ECPublicKey pub1 = ExampleEcKeysFromJws.PUBLIC_521;
        KeyPair keyPair = this.keyUtil.generateKeyPair(EllipticCurves.P521);
        PrivateKey priv2 = keyPair.getPrivate();
        PublicKey pub2 = keyPair.getPublic();
        JwsTestSupport.testBasicRoundTrip("touch\ufffd", algo, priv1, pub1, priv2, pub2);
    }

    public void testBadKeys() throws JoseException {
        String cs256 = "eyJhbGciOiJFUzI1NiJ9.UEFZTE9BRCEhIQ.WcL6cqkJSkzwK4Y85Lj96l-_WVmII6foW8d7CJNgdgDxi6NnTdXQD1Ze2vdXGcErIu9sJX9EXkmiaHSd0GQkgA";
        String cs384 = "eyJhbGciOiJFUzM4NCJ9.VGhlIHVtbGF1dCAoIC8_P21sYT90LyB1dW0tbG93dCkgcmVmZXJzIHRvIGEgc291bmQgc2hpZnQu.UO2zG037CLktsDeHJ71w48DmTMmCjsEEKhFGSE1uBQUG8rRZousdJR8p2rykZglU2RdWG48AE4Rf5_WfiZuP5ANC_bLgiOz1rwlSe6ds2romfdQ-enn7KTvr9Cmqt2Ot";
        String cs512 = "eyJhbGciOiJFUzUxMiJ9.Pz8_Pz8.AJS7SrxiK6zpJkXjV4iWM_oUcE294hV3RK-y5uQD2Otx-UwZNFEH6L66ww5ukQ7R1rykiWd9PNjzlzrgwfJqF2KyASmO6Hz7dZr9EYPIX6rrEpWjsp1tDJ0_Hq45Rk2eJ5z3cFTIpVu6V7CGXwVWvVCDQzcGpmZIFR939aI49Z_HWT7b";
        String[] stringArray = new String[]{cs256, cs384, cs512};
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String cs = stringArray[n2];
            JwsTestSupport.testBadKeyOnVerify(cs, ExampleRsaKeyFromJws.PRIVATE_KEY);
            JwsTestSupport.testBadKeyOnVerify(cs, null);
            JwsTestSupport.testBadKeyOnVerify(cs, new HmacKey(new byte[2048]));
            JwsTestSupport.testBadKeyOnVerify(cs, ExampleRsaKeyFromJws.PUBLIC_KEY);
            JwsTestSupport.testBadKeyOnVerify(cs, ExampleEcKeysFromJws.PRIVATE_256);
            JwsTestSupport.testBadKeyOnVerify(cs, ExampleEcKeysFromJws.PRIVATE_521);
            ++n2;
        }
        JwsTestSupport.testBadKeyOnVerify(cs256, ExampleEcKeysFromJws.PUBLIC_521);
        JwsTestSupport.testBadKeyOnVerify(cs384, ExampleEcKeysFromJws.PUBLIC_521);
        JwsTestSupport.testBadKeyOnVerify(cs384, ExampleEcKeysFromJws.PUBLIC_256);
        JwsTestSupport.testBadKeyOnVerify(cs512, ExampleEcKeysFromJws.PUBLIC_256);
    }
}

