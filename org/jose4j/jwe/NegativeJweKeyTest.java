/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwe;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import junit.framework.TestCase;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.keys.ExampleRsaJwksFromJwe;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NegativeJweKeyTest
extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(NegativeJweKeyTest.class);

    public void testProduceA128KW() throws JoseException {
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", this.aesKey(1));
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", this.aesKey(5));
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", this.aesKey(17));
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", this.aesKey(24));
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", this.aesKey(32));
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey());
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey());
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", ExampleEcKeysFromJws.PRIVATE_256);
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", ExampleEcKeysFromJws.PUBLIC_256);
    }

    public void testProduceA192KW() throws JoseException {
        this.expectBadKeyFailOnProduce("A192KW", "A192CBC-HS384", this.aesKey(1));
        this.expectBadKeyFailOnProduce("A192KW", "A192CBC-HS384", this.aesKey(5));
        this.expectBadKeyFailOnProduce("A192KW", "A192CBC-HS384", this.aesKey(16));
        this.expectBadKeyFailOnProduce("A192KW", "A192CBC-HS384", this.aesKey(23));
        this.expectBadKeyFailOnProduce("A192KW", "A192CBC-HS384", this.aesKey(32));
        this.expectBadKeyFailOnProduce("A192KW", "A192CBC-HS384", ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey());
        this.expectBadKeyFailOnProduce("A192KW", "A192CBC-HS384", ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey());
        this.expectBadKeyFailOnProduce("A192KW", "A192CBC-HS384", ExampleEcKeysFromJws.PRIVATE_256);
        this.expectBadKeyFailOnProduce("A192KW", "A192CBC-HS384", ExampleEcKeysFromJws.PUBLIC_256);
    }

    public void testProduceA256KW() throws JoseException {
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", this.aesKey(1));
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", this.aesKey(5));
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", this.aesKey(16));
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", this.aesKey(24));
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", this.aesKey(31));
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", this.aesKey(33));
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey());
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", ExampleRsaJwksFromJwe.APPENDIX_A_2.getPublicKey());
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", ExampleEcKeysFromJws.PRIVATE_521);
        this.expectBadKeyFailOnProduce("A256KW", "A256CBC-HS512", ExampleEcKeysFromJws.PUBLIC_521);
    }

    public void testProduceDirAndAes128() throws JoseException {
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", this.aesKey(1));
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", this.aesKey(7));
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", this.aesKey(8));
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", this.aesKey(16));
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", this.aesKey(24));
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", this.aesKey(31));
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", this.aesKey(33));
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", this.aesKey(48));
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", this.aesKey(64));
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey());
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", ExampleRsaJwksFromJwe.APPENDIX_A_2.getPublicKey());
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", ExampleEcKeysFromJws.PRIVATE_521);
        this.expectBadKeyFailOnProduce("dir", "A128CBC-HS256", ExampleEcKeysFromJws.PUBLIC_521);
    }

    public void testProduceDirAndAes192() throws JoseException {
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", this.aesKey(1));
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", this.aesKey(7));
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", this.aesKey(8));
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", this.aesKey(16));
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", this.aesKey(24));
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", this.aesKey(32));
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", this.aesKey(47));
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", this.aesKey(49));
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", this.aesKey(64));
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey());
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", ExampleRsaJwksFromJwe.APPENDIX_A_2.getPublicKey());
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", ExampleEcKeysFromJws.PRIVATE_521);
        this.expectBadKeyFailOnProduce("dir", "A192CBC-HS384", ExampleEcKeysFromJws.PUBLIC_521);
    }

    public void testProduceDirAndAes256() throws JoseException {
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", this.aesKey(1));
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", this.aesKey(7));
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", this.aesKey(8));
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", this.aesKey(16));
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", this.aesKey(24));
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", this.aesKey(32));
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", this.aesKey(48));
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", this.aesKey(63));
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", this.aesKey(65));
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey());
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", ExampleRsaJwksFromJwe.APPENDIX_A_2.getPublicKey());
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", ExampleEcKeysFromJws.PRIVATE_521);
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", ExampleEcKeysFromJws.PUBLIC_521);
    }

    public void testNullAesKey() throws JoseException {
        this.expectBadKeyFailOnProduce("A128KW", "A128CBC-HS256", null);
        this.expectBadKeyFailOnProduce("dir", "A256CBC-HS512", null);
    }

    public void testConsumeKeySizeMismatch1() throws JoseException {
        String cs = "eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.piLjD7hmzHarXM2IjTo8OjXj419Ah1MmF-xCQI3NUjResRSzodogQw.Hk5d-oTNmRz14KE97aV-Fg.xQbNIstt09YIBUmM6YZObw.HT-xvG9FLP6MxwORQLgxxg";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(24));
    }

    public void testConsumeKeySizeMismatch2() throws JoseException {
        String cs = "eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.mBJfKY7jaysbRL-KPckey-8n20rnGv7TWN2xxWg9bwcsod-aWQXnig.I4Rm3UTzpohTSBFxXCz3rA.-1CpOM9RVaSYsVbw6Okhdg.UnvHRrgyOtndiYGOtv_m-Q\n";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(32));
    }

    public void testConsumeKeySizeMismatch3() throws JoseException {
        String cs = "eyJhbGciOiJBMTkyS1ciLCJlbmMiOiJBMTkyQ0JDLUhTMzg0In0.V6ia6W1lFpeeJErBy9G7BdUXiAdh__5FFFM8RNu_bqD15Yn2JqF7YlhuTwbmsxjpxAFl4u-gEC4.Rob8WxK9RFfz4HlnPDD6AA.QFezmkSMy0tWf3-ck_T8og.RwrijoudPY5JJbiVCiYvwhEsptZyQjTk";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(16));
    }

    public void testConsumeKeySizeMismatch4() throws JoseException {
        String cs = " eyJhbGciOiJBMTkyS1ciLCJlbmMiOiJBMTkyQ0JDLUhTMzg0In0.zxDbVmrW2AlF1R3twiqrXD16dqe5tzcgA-1-5-Kltdk1trcxxs3FWfS5KYAe7E-n_Ibdrtx4Jyg._nVmQa1RPbStDTJyD-6vmg.fMFd4wNNmxJzwPTxxIas6Q.0ZAuXetx6u-h5UYQdEED8yvRtGtRVap4";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(32));
    }

    public void testConsumeKeySizeMismatch5() throws JoseException {
        String cs = "eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIn0.CUf42Kh7kiG8EsOu9VUKKT9wA3gsaQxm7SmH6Au-Bpr9qQZyw6cRN-EU2XNBCLK2grnGecZofaapAcsEXazseP_hOlsD85fw.9aTasBSY_Ed_1Gyaf9T1yw.sKtEMcaijs6kzLFuoUAFNg.DufxVwkEcAyfGwZcrP3FJ6H1AsH7Vpdiu-3t9-3y_gs";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(16));
    }

    public void testConsumeKeySizeMismatch6() throws JoseException {
        String cs = "eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIn0.q95oRWEcqb0a31GpOBfUIj9uZHMP51CmfqGcLw1d1rywxPjFKW0uLpDRRFbXz--n3KL9BBNXZCJQ8a1niNbz85B9d3YBzvt1.tAQVsLHcOaZ5-SKzOEFXsg.qSA7hqdSlb6l10R2I_m8eA.4KEjAYDhTLUqRNrgvMRNKWfcdjhnJTw5iGlHozF99i8";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(24));
    }

    public void testConsumeKeySizeMismatch7() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..DhBs7zz8d4DmHeF_OAHxnA.QcLu7u_0Vl6EaOg0UB5YHA.v-0AEf-NgmyqfPLrnyNEyg";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(33));
    }

    public void testConsumeKeySizeMismatch8() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..zvuhYR4uc2eiKziPep3tNQ.ufJ6A7LHTyEspKaV582TTg.Tzlj1Wi7Cdkx-k5ColVgEQ";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(48));
    }

    public void testConsumeKeySizeMismatch9() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..nQWLu7PB-d4ZHazU42ljiQ.FwvbiGVFCZE2wy4dS7sLxg.6jP9_W8L4tBEfHAu18Hj_A";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(64));
    }

    public void testConsumeKeySizeMismatch10() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0In0..D9P2JTGqnAP6W6xN3n-twQ.3wBV0wv_bLTmRVnvX_YnLQ.pgkcaNh7ZjdAcBO1yKtObDQ3HU0rXnzo";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(32));
    }

    public void testConsumeKeySizeMismatch11() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0In0..btkRpNzeamtVj26p4Y3rNA.VHiVCfawJY_2fIMLHlxPFA.UhXTQ1vxaIFiRN_8pGgjdGkxoKUOy03F";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(49));
    }

    public void testConsumeKeySizeMismatch12() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0In0..dtaa23ilH0MAv-DRI_CZdQ.NycLiCtG2lSBoT5yxJLqag.CMR2mhqz8v1dQfCvLduWhC1aAx5QhXY7";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(64));
    }

    public void testConsumeKeySizeMismatch13() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIn0..bvmPzabWZFdbiDrJLDoQVw.i7aWtdTVPhVgVDP0lx8TnA.djK6f7tQ44T8aBAfblXu8qA4j9KHMjomy_Ho0sb4S1g";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(32));
    }

    public void testConsumeKeySizeMismatch14() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIn0..HHgNXhSr5vPQoVyV5rkGzg.grgmRjvvFJ5nNioVRcbJTg.AE7e8fHqFOI91Y52W9kpUNqr1jKGq3DoSVCjyjq3mVo";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(48));
    }

    public void testConsumeKeySizeMismatch15() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIn0..VC5VhumdESJ0-4I0c8bW2A.2tFEdedX4JdxiXf3RGL2eA.KMIsWU0rWGdO4YAvp-3TX1Q-aMAQqXsDwXBQKu-BJgo";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(65));
    }

    public void testUnexpectedEncryptedKey() throws JoseException {
        String cs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.F6EJj1gzyuttczguncypZOk31wMnVajr1IpS-ZnXMeW72QqurUKlBA.SzF11wzK9JfHTsfPbPgixw.7wGWU2oy1fPXf_HoGGfuqCwMLwkvOOjgFF4YA_iwzUUqkwLX5tEOUq76Qgk7LSg6cgc8VK-4ZEqaaFLwwnQ9gw.3C7wAt7-OSgD6QMkccW48A";
        this.expectBadKeyFailOnConsume(cs, this.aesKey(32));
    }

    public void testRsaTooSmall() throws JoseException {
        RsaJsonWebKey rsaJsonWebKey = (RsaJsonWebKey)PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"RSA\",\"n\":\"hIOFEUa93kqVnqoaA1r5qj3tLhnSyQ9njLrlcJrynwt2LYfIhntUZPfS2fiHhLGzww7GamLAXwDfGZo0dY6V3cglENl6yroBWhYu15IgHVAeP1V_5m1gJ9hiWNUR3i5zhNNUR1Ewdo0E52amiRb1-xXRcxhcRlybfRcEMJEgm0c\",\"e\":\"AQAB\",\"d\":\"RhNK7jzrsT7d6n7nrLiSaM3AvG1Zg4vK5af8J1U5UpP8Fc3FZCCaG57WeQAtoiVa-563nJDGTDcow-BBN52EcG_7SRJtXc6Zk5og330nqIy0OoP2GRPJKOg6zB45RsDQmxklezrlWCMdwZIzjxyB_vDMx59uXK_i66iVXjFoqZk\",\"p\":\"7aIngX0swanIMJk-GpmJVxL7vF6Zx0RfmimOE6BJKi7COHR7ectpQtfmYhLMBtMpHF1qnuaa4vlM3S9xLHGlIw\",\"q\":\"jsF0PrAmuixIUgCinmh2-FYmBySG8B8Kv_Llj81kKRiNM35Pv_W_zrkb_oxyEMzOc9Z2_gkqhEfYZulnBVCtjQ\",\"dp\":\"ab1f6uSyR7Ku28E0u01aqZ5O2fEWaG7qQ4T-LYmDRPvtfIWIdBepTQ8Y-sb2dor7nh2LVg2zGhBovXtg1q_zFQ\",\"dq\":\"GPpaZ5mUvSCAavC3g3YN0vfn4XoPrjYQQHO0nQu4CcTE-AyS0aijLf2Pm2NhlfTv7q7I1TwvV0Pm5mLSZsiuBQ\",\"qi\":\"gVD_SEwVbiHvZAm3aqynOfMnObl8bBe1qDDNThVO3yUL8tghkKizEu1Ey_sYal-luDu9zcEFUkbrV-7jTqFUVg\"}\n");
        this.expectBadKeyFailOnProduce("RSA-OAEP", "A128CBC-HS256", rsaJsonWebKey.getPublicKey());
        this.expectBadKeyFailOnProduce("RSA1_5", "A128CBC-HS256", rsaJsonWebKey.getPublicKey());
        this.expectBadKeyFailOnConsume("eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkExMjhDQkMtSFMyNTYifQ.Ti9oxDdTy9hk3j5XOu0lPuus3pC6ZPsBY4LubTOKS6kX1XAR16u2yvcf5csZpB-3CK3UL5JQl1kye2QVytWH79FLg2R3Zfjpd21AFkjxkkI6Cl9UQjPJCO7oiYnKkBdbMiSwcdGl2z6OHpZNcqHH6jQ4BVk-zDPbg3Vj25X19vE.pZyCrX1Aae9kvKEyCvUTfA.H7qnqcNKWAVhd-xAVdAgkw.kDaHS6qIiKxAH4Z316EJ6w", rsaJsonWebKey.getPrivateKey());
    }

    public void testNullRsaKey() throws JoseException {
        this.expectBadKeyFailOnProduce("RSA-OAEP", "A128CBC-HS256", null);
        this.expectBadKeyFailOnConsume("eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkExMjhDQkMtSFMyNTYifQ.Ti9oxDdTy9hk3j5XOu0lPuus3pC6ZPsBY4LubTOKS6kX1XAR16u2yvcf5csZpB-3CK3UL5JQl1kye2QVytWH79FLg2R3Zfjpd21AFkjxkkI6Cl9UQjPJCO7oiYnKkBdbMiSwcdGl2z6OHpZNcqHH6jQ4BVk-zDPbg3Vj25X19vE.pZyCrX1Aae9kvKEyCvUTfA.H7qnqcNKWAVhd-xAVdAgkw.kDaHS6qIiKxAH4Z316EJ6w", null);
    }

    public void testRsaPubPriMixup() throws JoseException {
        PrivateKey pk = ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey();
        this.expectBadKeyFailOnProduce("RSA-OAEP", "A128CBC-HS256", pk);
        PublicKey publicKey = ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey();
        this.expectBadKeyFailOnConsume("eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkExMjhDQkMtSFMyNTYifQ.Ti9oxDdTy9hk3j5XOu0lPuus3pC6ZPsBY4LubTOKS6kX1XAR16u2yvcf5csZpB-3CK3UL5JQl1kye2QVytWH79FLg2R3Zfjpd21AFkjxkkI6Cl9UQjPJCO7oiYnKkBdbMiSwcdGl2z6OHpZNcqHH6jQ4BVk-zDPbg3Vj25X19vE.pZyCrX1Aae9kvKEyCvUTfA.H7qnqcNKWAVhd-xAVdAgkw.kDaHS6qIiKxAH4Z316EJ6w", publicKey);
    }

    public void testRsaWithWrongType() throws JoseException {
        this.expectBadKeyFailOnProduce("RSA-OAEP", "A128CBC-HS256", this.aesKey(256));
        this.expectBadKeyFailOnProduce("RSA-OAEP", "A128CBC-HS256", ExampleEcKeysFromJws.PUBLIC_521);
        this.expectBadKeyFailOnProduce("RSA-OAEP", "A128CBC-HS256", ExampleEcKeysFromJws.PRIVATE_256);
    }

    public void testEcWithWrongTypeAndNull() throws JoseException {
        String[] css;
        PrivateKey privateRsaKey = ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey();
        PublicKey publicRsaKey = ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey();
        String[] stringArray = new String[]{"ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A192KW", "ECDH-ES+A256KW"};
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String ecAlg = stringArray[n2];
            this.expectBadKeyFailOnProduce(ecAlg, "A128CBC-HS256", publicRsaKey);
            this.expectBadKeyFailOnProduce(ecAlg, "A128CBC-HS256", privateRsaKey);
            this.expectBadKeyFailOnProduce(ecAlg, "A128CBC-HS256", this.aesKey(32));
            this.expectBadKeyFailOnProduce(ecAlg, "A128CBC-HS256", ExampleEcKeysFromJws.PRIVATE_256);
            this.expectBadKeyFailOnProduce(ecAlg, "A128CBC-HS256", null);
            ++n2;
        }
        String csEcdh = "eyJhbGciOiJFQ0RILUVTIiwiZW5jIjoiQTEyOENCQy1IUzI1NiIsImVwayI6eyJrdHkiOiJFQyIsIngiOiI5YlNnNjhoM1hmemJsbUJyeURhdWlySzd2YmNkRFpIajZqcFktdkU4MUhnIiwieSI6IndrbDhpWUlZeTJoSFpuLWxyS1c1Y1JIVzl4cDlac1RrZVR3MEZjLW4xS2MiLCJjcnYiOiJQLTI1NiJ9fQ..A2GLJCjWdS0XRhHUIS3UWg.LbQGCOYAqMDPiXuOw9mNpKF1Uqx_eff8K-HvJL0LXDA.iLuskqi6_UOkDyJz3Z56bg";
        String csEcdhKw = "eyJhbGciOiJFQ0RILUVTK0ExMjhLVyIsImVuYyI6IkExMjhDQkMtSFMyNTYiLCJlcGsiOnsia3R5IjoiRUMiLCJ4IjoiX3BnSFZHOXhlWHd1elRxbkRfNnQzNm9MTGFlZmFQdlRlR2NHdHpZeXZ0NCIsInkiOiI4eHR6dFluNFNqaUVQSGlkemVpVzBWeDhFaEd0eVN4WVVDMXZjdTg0OXJnIiwiY3J2IjoiUC0yNTYifX0.LzVyMpb8WwfGpkVFUDd_kzJuaP4myaqR36xewhqwC4Ykl91kbh8pMA._lZgmx258W8L2sHkE8gaxw.fmZowRNnxfgpqImyyB_o4zCQ4Z6LLBtIEQBLZkELOqim5RM6O9t66_ZvAZphevbL.DiRDsaE4g7ZcTQVxya19og";
        String[] stringArray2 = css = new String[]{csEcdh, csEcdhKw};
        int n3 = css.length;
        int n4 = 0;
        while (n4 < n3) {
            String cs = stringArray2[n4];
            this.expectBadKeyFailOnConsume(cs, publicRsaKey);
            this.expectBadKeyFailOnConsume(cs, privateRsaKey);
            this.expectBadKeyFailOnConsume(cs, this.aesKey(16));
            this.expectBadKeyFailOnConsume(cs, ExampleEcKeysFromJws.PUBLIC_256);
            ++n4;
        }
    }

    private void expectBadKeyFailOnConsume(String cs, Key key) throws JoseException {
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setCompactSerialization(cs);
        jwe.setKey(key);
        try {
            String plaintextString = jwe.getPlaintextString();
            NegativeJweKeyTest.fail((String)("plaintextString w/ " + jwe.getHeaders().getFullHeaderAsJsonString() + " should have failed due to bad key (" + key + ") but gave " + plaintextString));
        }
        catch (InvalidKeyException e) {
            log.debug("Expected exception due to invalid key: {}", (Object)e.toString());
        }
    }

    private void expectBadKeyFailOnProduce(String alg, String enc, Key key) throws JoseException {
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setPlaintext("PLAIN OLD TEXT");
        jwe.setAlgorithmHeaderValue(alg);
        jwe.setEncryptionMethodHeaderParameter(enc);
        jwe.setKey(key);
        try {
            String cs = jwe.getCompactSerialization();
            NegativeJweKeyTest.fail((String)("getCompactSerialization w/ " + alg + "/" + enc + " should have failed due to bad key (" + key + ") but gave " + cs));
        }
        catch (InvalidKeyException e) {
            log.debug("Expected exception due to invalid key: {}", (Object)e.toString());
        }
    }

    private AesKey aesKey(int byteLength) {
        return new AesKey(new byte[byteLength]);
    }
}

