/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwe.kdf;

import java.util.Arrays;
import junit.framework.TestCase;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jwe.kdf.ConcatKeyDerivationFunction;
import org.jose4j.jwe.kdf.KdfUtil;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.StringUtil;

public class ConcatKeyDerivationFunctionTest
extends TestCase {
    public void testGetReps() {
        ConcatKeyDerivationFunction kdf = new ConcatKeyDerivationFunction("SHA-256");
        ConcatKeyDerivationFunctionTest.assertEquals((long)1L, (long)kdf.getReps(256));
        ConcatKeyDerivationFunctionTest.assertEquals((long)2L, (long)kdf.getReps(384));
        ConcatKeyDerivationFunctionTest.assertEquals((long)2L, (long)kdf.getReps(512));
        ConcatKeyDerivationFunctionTest.assertEquals((long)4L, (long)kdf.getReps(1024));
        ConcatKeyDerivationFunctionTest.assertEquals((long)5L, (long)kdf.getReps(1032));
        ConcatKeyDerivationFunctionTest.assertEquals((long)8L, (long)kdf.getReps(2048));
        ConcatKeyDerivationFunctionTest.assertEquals((long)9L, (long)kdf.getReps(2056));
    }

    public void testGetDatalenData() {
        String apu = "QWxpY2U";
        KdfUtil kdfUtil = new KdfUtil();
        byte[] apuDatalenData = kdfUtil.getDatalenDataFormat(apu);
        byte[] byArray = new byte[9];
        byArray[3] = 5;
        byArray[4] = 65;
        byArray[5] = 108;
        byArray[6] = 105;
        byArray[7] = 99;
        byArray[8] = 101;
        ConcatKeyDerivationFunctionTest.assertTrue((boolean)Arrays.equals(apuDatalenData, byArray));
        String apv = "Qm9i";
        byte[] apvDatalenData = kdfUtil.getDatalenDataFormat(apv);
        byte[] byArray2 = new byte[7];
        byArray2[3] = 3;
        byArray2[4] = 66;
        byArray2[5] = 111;
        byArray2[6] = 98;
        ConcatKeyDerivationFunctionTest.assertTrue((boolean)Arrays.equals(apvDatalenData, byArray2));
        ConcatKeyDerivationFunctionTest.assertTrue((boolean)Arrays.equals(kdfUtil.prependDatalen(new byte[0]), new byte[4]));
        ConcatKeyDerivationFunctionTest.assertTrue((boolean)Arrays.equals(kdfUtil.prependDatalen(null), new byte[4]));
    }

    public void testKdf1() throws Exception {
        String derivedKey = "pgs50IOZ6BxfqvTSie4t9OjWxGr4whiHo1v9Dti93CRiJE2PP60FojLatVVrcjg3BxpuFjnlQxL97GOwAfcwLA";
        byte[] z = Base64Url.decode("Sq8rGLm4rEtzScmnSsY5r1n-AqBl_iBU8FxN80Uc0S0");
        System.out.println(Base64Url.encode(z));
        KdfUtil kdfUtil = new KdfUtil();
        int keyDatalen = 512;
        String alg = "A256CBC-HS512";
        byte[] algId = kdfUtil.prependDatalen(StringUtil.getBytesUtf8(alg));
        byte[] partyU = new byte[4];
        byte[] partyV = new byte[4];
        byte[] pub = ByteUtil.getBytes(keyDatalen);
        byte[] priv = ByteUtil.EMPTY_BYTES;
        ConcatKeyDerivationFunction myConcatKdf = new ConcatKeyDerivationFunction("SHA-256", null);
        byte[] kdfed = myConcatKdf.kdf(z, keyDatalen, algId, partyU, partyV, pub, priv);
        ConcatKeyDerivationFunctionTest.assertEquals((String)derivedKey, (String)Base64Url.encode(kdfed));
    }

    public void testKdf2() throws Exception {
        String derivedKey = "vphyobtvExGXF7TaOvAkx6CCjHQNYamP2ET8xkhTu-0";
        byte[] z = Base64Url.decode("LfkHot2nGTVlmfxbgxQfMg");
        System.out.println(Base64Url.encode(z));
        KdfUtil kdfUtil = new KdfUtil(null);
        int keyDatalen = 256;
        String alg = "A128CBC-HS256";
        byte[] algId = kdfUtil.prependDatalen(StringUtil.getBytesUtf8(alg));
        byte[] partyU = new byte[4];
        byte[] partyV = new byte[4];
        byte[] pub = ByteUtil.getBytes(keyDatalen);
        byte[] priv = ByteUtil.EMPTY_BYTES;
        ConcatKeyDerivationFunction myConcatKdf = new ConcatKeyDerivationFunction("SHA-256", null);
        byte[] kdfed = myConcatKdf.kdf(z, keyDatalen, algId, partyU, partyV, pub, priv);
        ConcatKeyDerivationFunctionTest.assertEquals((String)derivedKey, (String)Base64Url.encode(kdfed));
    }

    public void testKdf3() throws Exception {
        String derivedKey = "yRbmmZJpxv3H1aq3FgzESa453frljIaeMz6pt5rQZ4Q5Hs-4RYoFRXFh_qBsbTjlsj8JxIYTWj-cp5LKtgi1fBRsf_5yTEcLDv4pKH2fNxjbEOKuVVDWA1_Qv2IkEC0_QSi3lSSELcJaNX-hDG8occ7oQv-w8lg6lLJjg58kOes";
        byte[] z = Base64Url.decode("KSDnQpf2iurUsAbcuI4YH-FKfk2gecN6cWHTYlBzrd8");
        KdfUtil kdfUtil = new KdfUtil(null);
        int keyDatalen = 1024;
        String alg = "meh";
        byte[] algId = kdfUtil.prependDatalen(StringUtil.getBytesUtf8(alg));
        byte[] byArray = new byte[9];
        byArray[3] = 5;
        byArray[4] = 65;
        byArray[5] = 108;
        byArray[6] = 105;
        byArray[7] = 99;
        byArray[8] = 101;
        byte[] partyU = byArray;
        byte[] byArray2 = new byte[7];
        byArray2[3] = 3;
        byArray2[4] = 66;
        byArray2[5] = 111;
        byArray2[6] = 98;
        byte[] partyV = byArray2;
        byte[] pub = ByteUtil.getBytes(keyDatalen);
        byte[] priv = ByteUtil.EMPTY_BYTES;
        ConcatKeyDerivationFunction myConcatKdf = new ConcatKeyDerivationFunction("SHA-256");
        byte[] kdfed = myConcatKdf.kdf(z, keyDatalen, algId, partyU, partyV, pub, priv);
        ConcatKeyDerivationFunctionTest.assertEquals((String)derivedKey, (String)Base64Url.encode(kdfed));
    }

    public void testKdf4() throws Exception {
        String derivedKey = "SNOvl6h5iSYWJ_EhlnvK8o6om9iyR8HkKMQtQYGkYKkVY0HFMleoUm-H6-kLz8sW";
        byte[] z = Base64Url.decode("zp9Hot2noTVlmfxbkXqfn1");
        KdfUtil kdfUtil = new KdfUtil();
        int keyDatalen = 384;
        String alg = "A192CBC-HS384";
        byte[] algId = kdfUtil.prependDatalen(StringUtil.getBytesUtf8(alg));
        byte[] partyU = new byte[4];
        byte[] partyV = new byte[4];
        byte[] pub = ByteUtil.getBytes(keyDatalen);
        byte[] priv = ByteUtil.EMPTY_BYTES;
        ConcatKeyDerivationFunction myConcatKdf = new ConcatKeyDerivationFunction("SHA-256");
        byte[] kdfed = myConcatKdf.kdf(z, keyDatalen, algId, partyU, partyV, pub, priv);
        ConcatKeyDerivationFunctionTest.assertEquals((String)derivedKey, (String)Base64Url.encode(kdfed));
    }
}

