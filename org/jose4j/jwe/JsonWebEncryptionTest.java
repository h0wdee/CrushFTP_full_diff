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

import javax.crypto.spec.SecretKeySpec;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.JceProviderTestSupport;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.ExampleRsaJwksFromJwe;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidAlgorithmException;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class JsonWebEncryptionTest {
    @Test
    public void testJweExampleA3() throws JoseException {
        String jweCsFromAppdxA3 = "eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.U0m_YmjN04DJvceFICbCVQ";
        JsonWebEncryption jwe = new JsonWebEncryption();
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("\n{\"kty\":\"oct\",\n \"k\":\"GawgguFyGrWKav7AX4VKUg\"\n}");
        jwe.setCompactSerialization(jweCsFromAppdxA3);
        jwe.setKey(new AesKey(jsonWebKey.getKey().getEncoded()));
        String plaintextString = jwe.getPlaintextString();
        Assert.assertEquals((Object)"Live long and prosper.", (Object)plaintextString);
    }

    @Test
    public void testJweExampleA2() throws JoseException {
        String jweCsFromAppendixA2 = "eyJhbGciOiJSU0ExXzUiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.UGhIOguC7IuEvf_NPVaXsGMoLOmwvc1GyqlIKOK1nN94nHPoltGRhWhw7Zx0-kFm1NJn8LE9XShH59_i8J0PH5ZZyNfGy2xGdULU7sHNF6Gp2vPLgNZ__deLKxGHZ7PcHALUzoOegEI-8E66jX2E4zyJKx-YxzZIItRzC5hlRirb6Y5Cl_p-ko3YvkkysZIFNPccxRU7qve1WYPxqbb2Yw8kZqa2rMWI5ng8OtvzlV7elprCbuPhcCdZ6XDP0_F8rkXds2vE4X-ncOIM8hAYHHi29NX0mcKiRaD0-D-ljQTP-cFPgwCp6X-nZZd9OHBv-B3oWh2TbqmScqXMR4gp_A.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.9hH0vgRfYgPnAHOd8stkvw";
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setKey(ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey());
        jwe.setCompactSerialization(jweCsFromAppendixA2);
        String plaintextString = jwe.getPlaintextString();
        Assert.assertEquals((Object)"Live long and prosper.", (Object)plaintextString);
    }

    @Test
    public void jweExampleA1() throws Exception {
        JceProviderTestSupport jceProviderTestSupport = new JceProviderTestSupport();
        jceProviderTestSupport.setEncryptionAlgsNeeded("A256GCM");
        jceProviderTestSupport.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                String cs = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQ.OKOawDo13gRp2ojaHV7LFpZcgV7T6DVZKTyKOMTYUmKoTCVJRgckCL9kiMT03JGeipsEdY3mx_etLbbWSrFr05kLzcSr4qKAq7YN7e9jwQRb23nfa6c9d-StnImGyFDbSv04uVuxIp5Zms1gNxKKK2Da14B8S4rzVRltdYwam_lDp5XnZAYpQdb76FdIKLaVmqgfwX7XWRxv2322i-vDxRfqNzo_tETKzpVLzfiwQyeyPGLBIO56YJ7eObdv0je81860ppamavo35UgoRdbYaBcoh9QcfylQr66oc6vFWXRcZ_ZT2LawVCWTIy3brGPi6UklfCpIMfIjf7iGdXKHzg.48V1_ALb6US04U3b.5eym8TW_c8SuK0ltJ3rpYIzOeDQz7TALvtu6UG9oMo4vpzs9tX_EFShS8iB7j6jiSdiwkIr3ajwQzaBtQD_A.XFBoMYUZodetZdvTiFvSkQ";
                JsonWebEncryption jwe = new JsonWebEncryption();
                jwe.setCompactSerialization(cs);
                jwe.setKey(ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey());
                String examplePlaintext = "The true sign of intelligence is not knowledge but imagination.";
                Assert.assertThat((Object)examplePlaintext, (Matcher)CoreMatchers.equalTo((Object)jwe.getPlaintextString()));
            }
        });
    }

    @Test
    public void testHappyRoundTripRsa1_5AndAesCbc128() throws JoseException {
        JsonWebEncryption jweForEncrypt = new JsonWebEncryption();
        String plaintext = "Some text that's on double secret probation";
        jweForEncrypt.setPlaintext(plaintext);
        jweForEncrypt.setAlgorithmHeaderValue("RSA1_5");
        jweForEncrypt.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        jweForEncrypt.setKey(ExampleRsaJwksFromJwe.APPENDIX_A_2.getPublicKey());
        String compactSerialization = jweForEncrypt.getCompactSerialization();
        JsonWebEncryption jweForDecrypt = new JsonWebEncryption();
        jweForDecrypt.setCompactSerialization(compactSerialization);
        jweForDecrypt.setKey(ExampleRsaJwksFromJwe.APPENDIX_A_2.getPrivateKey());
        Assert.assertEquals((Object)plaintext, (Object)jweForDecrypt.getPlaintextString());
    }

    @Test
    public void testHappyRoundTripDirectAndAesCbc128() throws JoseException {
        JsonWebEncryption jweForEncrypt = new JsonWebEncryption();
        String plaintext = "Some sensitive info";
        jweForEncrypt.setPlaintext(plaintext);
        jweForEncrypt.setAlgorithmHeaderValue("dir");
        jweForEncrypt.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        ContentEncryptionAlgorithm contentEncryptionAlgorithm = jweForEncrypt.getContentEncryptionAlgorithm();
        ContentEncryptionKeyDescriptor cekDesc = contentEncryptionAlgorithm.getContentEncryptionKeyDescriptor();
        byte[] cekBytes = ByteUtil.randomBytes(cekDesc.getContentEncryptionKeyByteLength());
        SecretKeySpec key = new SecretKeySpec(cekBytes, cekDesc.getContentEncryptionKeyAlgorithm());
        jweForEncrypt.setKey(key);
        String compactSerialization = jweForEncrypt.getCompactSerialization();
        JsonWebEncryption jweForDecrypt = new JsonWebEncryption();
        jweForDecrypt.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, "dir"));
        jweForDecrypt.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, "A128CBC-HS256"));
        jweForDecrypt.setCompactSerialization(compactSerialization);
        jweForDecrypt.setKey(key);
        Assert.assertEquals((Object)plaintext, (Object)jweForDecrypt.getPlaintextString());
    }

    @Test(expected=JoseException.class)
    public void testAcceptingCompactSerializationWithMalformedJWE() throws JoseException {
        String damaged_version_of_jweCsFromAppdxA3 = "eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY";
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setCompactSerialization(damaged_version_of_jweCsFromAppdxA3);
    }

    @Test(expected=InvalidAlgorithmException.class)
    public void testBlackListAlg() throws JoseException {
        String jwecs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..LpJAcwq3RzCs-zPRQzT-jg.IO0ZwAhWnSF05dslZwaBKcHYOAKlSpt_l7Dl5ABrUS0.0KfkxQTFqTQjzfJIm8MNjg";
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"I95jRMEyRvD0t3LRgL1GSWTgkX5jznuhX4mce9bYV_A\"}");
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "dir"));
        jwe.setCompactSerialization(jwecs);
        jwe.setKey(jsonWebKey.getKey());
        jwe.getPayload();
    }

    @Test(expected=InvalidAlgorithmException.class)
    public void testBlackListEncAlg() throws JoseException {
        String jwecs = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..LpJAcwq3RzCs-zPRQzT-jg.IO0ZwAhWnSF05dslZwaBKcHYOAKlSpt_l7Dl5ABrUS0.0KfkxQTFqTQjzfJIm8MNjg";
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"I95jRMEyRvD0t3LRgL1GSWTgkX5jznuhX4mce9bYV_A\"}");
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "A128CBC-HS256"));
        jwe.setCompactSerialization(jwecs);
        jwe.setKey(jsonWebKey.getKey());
        jwe.getPayload();
    }
}

