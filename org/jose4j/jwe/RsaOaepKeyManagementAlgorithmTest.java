/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwe;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Arrays;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContextTest;
import org.jose4j.jwa.AlgorithmFactory;
import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jwa.JceProviderTestSupport;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwe.RsaKeyManagementAlgorithm;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.ExampleRsaJwksFromJwe;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RsaOaepKeyManagementAlgorithmTest {
    static final String JWK_JSON = "{\"kty\":\"RSA\",\"n\":\"2cQJH1f6yF9DcGa8Cmbnhn4LHLs5L6kNb2rxkrNFZArJLRaKvaC3tMCKZ8ZgIpO9bVMPx5UMjJoaf7p9O5BSApVqA2J10fUbdSIomCcDwvGo0eyhty0DILLWTMXzGEVM3BXzuJQoeDkuUCXXcCwA4Msyyd2OHVu-pB2OrGv6fcjHwjINty3UoKm08lCvAevBKHsuA-FFwQII9bycvRx5wRqFUjdMAyiOmLYBHBaJSi11g3HVexMcb29v14PSlVzdGUMN8oboa-zcIyaPrIiczLqAkSXQNdEFHrjsJHfFeNMfOblLM7icKN_tyWujYeItt4kqUIimPn5dHjwgcQYE7w\",\"e\":\"AQAB\",\"d\":\"dyUz3ItVceX1Tv1WqtZMnKA_0jN5gWMcL7ayf5JISAlCssGfnUre2C10TH0UQjbVMIh-nLMnD5KNJw9Qz5MR28oGG932Gq7hm__ZeA34l-OCe4DdpgwhpvVSHOU9MS1RdSUpmPavAcA_X6ikrAHXZSaoHhxzUgrNTpvBYQMfJUv_492fStIseQ9rwAMOpCWOiWMZOQm3KJVTLLunXdKf_UxmzmKXYKYZWke3AWIzUqnOfqIjfDTMunF4UWU0zKlhcsaQNmYMVrJGajD1bJdy_dbUU3LE8sx-bdkUI6oBk-sFtTTVyVdQcetG9kChJ5EnY5R6tt_4_xFG5kxzTo6qaQ\",\"p\":\"7yQmgE60SL7QrXpAJhChLgKnXWi6C8tVx1lA8FTpphpLaCtK-HbgBVHCprC2CfaM1mxFJZahxgFjC9ehuV8OzMNyFs8kekS82EsQGksi8HJPxyR1fU6ATa36ogPG0nNaqm3EDmYyjowhntgBz2OkbFAsTMHTdna-pZBRJa9lm5U\",\"q\":\"6R4dzo9LwHLO73EMQPQsmwXjVOvAS5W6rgQ-BCtMhec_QosAXIVE3AGyfweqZm6rurXCVFykDLwJ30GepLQ8nTlzeV6clx0x70saGGKKVmCsHuVYWwgIRyJTrt4SX29NQDZ_FE52NlO3OhPkj1ExSk_pGMqGRFd26K8g0jJsXXM\",\"dp\":\"VByn-hs0qB2Ncmb8ZycUOgWu7ljmjz1up1ZKU_3ZzJWVDkej7-6H7vcJ-u1OqgRxFv4v9_-aWPWl68VlWbkIkJbx6vniv6qrrXwBZu4klOPwEYBOXsucrzXRYOjpJp5yNl2zRslFYQQC00bwpAxNCdfNLRZDlXhAqCUxlYqyt10\",\"dq\":\"MJFbuGtWZvQEdRJicS3uFSY25LxxRc4eJJ8xpIC44rT5Ew4Otzf0zrlzzM92Cv1HvhCcOiNK8nRCwkbTnJEIh-EuU70IdttYSfilqSruk2x0r8Msk1qrDtbyBF60CToRKC2ycDKgolTyuaDnX4yU7lyTvdyD-L0YQwYpmmFy_k0\",\"qi\":\"vy7XCwZ3jyMGik81TIZDAOQKC8FVUc0TG5KVYfti4tgwzUqFwtuB8Oc1ctCKRbE7uZUPwZh4OsCTLqIvqBQda_kaxOxo5EF7iXj6yHmZ2s8P_Z_u3JLuh-oAT_6kmbLx6CAO0DbtKtxp24Ivc1hDfqSwWORgN1AOrSRCmE3nwxg\"}";
    public static final String EXAMPLE_PAYLOAD = "Well, as of this moment, they're on DOUBLE SECRET PROBATION!";

    @Test
    public void testJweExampleA1() throws JoseException {
        String encodedEncryptedKey = "OKOawDo13gRp2ojaHV7LFpZcgV7T6DVZKTyKOMTYUmKoTCVJRgckCL9kiMT03JGeipsEdY3mx_etLbbWSrFr05kLzcSr4qKAq7YN7e9jwQRb23nfa6c9d-StnImGyFDbSv04uVuxIp5Zms1gNxKKK2Da14B8S4rzVRltdYwam_lDp5XnZAYpQdb76FdIKLaVmqgfwX7XWRxv2322i-vDxRfqNzo_tETKzpVLzfiwQyeyPGLBIO56YJ7eObdv0je81860ppamavo35UgoRdbYaBcoh9QcfylQr66oc6vFWXRcZ_ZT2LawVCWTIy3brGPi6UklfCpIMfIjf7iGdXKHzg";
        Base64Url base64Url = new Base64Url();
        byte[] encryptedKey = base64Url.base64UrlDecode(encodedEncryptedKey);
        RsaKeyManagementAlgorithm.RsaOaep keyManagementAlgorithm = new RsaKeyManagementAlgorithm.RsaOaep();
        PrivateKey privateKey = ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey();
        ContentEncryptionKeyDescriptor cekDesc = new ContentEncryptionKeyDescriptor(32, "AES");
        Key key = keyManagementAlgorithm.manageForDecrypt(privateKey, encryptedKey, cekDesc, null, ProviderContextTest.EMPTY_CONTEXT);
        byte[] cekBytes = ByteUtil.convertUnsignedToSignedTwosComp(new int[]{177, 161, 244, 128, 84, 143, 225, 115, 63, 180, 3, 255, 107, 154, 212, 246, 138, 7, 110, 91, 112, 46, 34, 105, 47, 130, 203, 46, 122, 234, 64, 252});
        byte[] encoded = key.getEncoded();
        Assert.assertTrue((String)Arrays.toString(encoded), (boolean)Arrays.equals(cekBytes, encoded));
    }

    @Test
    public void testRoundTrip() throws JoseException {
        RsaKeyManagementAlgorithm.RsaOaep oaep = new RsaKeyManagementAlgorithm.RsaOaep();
        ContentEncryptionKeyDescriptor cekDesc = new ContentEncryptionKeyDescriptor(16, "AES");
        PublicKey publicKey = ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey();
        ContentEncryptionKeys contentEncryptionKeys = oaep.manageForEncrypt(publicKey, cekDesc, null, null, ProviderContextTest.EMPTY_CONTEXT);
        byte[] encryptedKey = contentEncryptionKeys.getEncryptedKey();
        PrivateKey privateKey = ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey();
        Key key = oaep.manageForDecrypt(privateKey, encryptedKey, cekDesc, null, ProviderContextTest.EMPTY_CONTEXT);
        byte[] cek = contentEncryptionKeys.getContentEncryptionKey();
        Assert.assertTrue((boolean)Arrays.equals(cek, key.getEncoded()));
    }

    @Test
    public void test256RoundTrip() throws Exception {
        JceProviderTestSupport jceProviderTestSupport = new JceProviderTestSupport();
        jceProviderTestSupport.setKeyManagementAlgsNeeded("RSA-OAEP-256");
        jceProviderTestSupport.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                if (!RsaOaepKeyManagementAlgorithmTest.this.doubleCheckRsaOaep256()) {
                    return;
                }
                RsaJsonWebKey jwk = (RsaJsonWebKey)PublicJsonWebKey.Factory.newPublicJwk(RsaOaepKeyManagementAlgorithmTest.JWK_JSON);
                JsonWebEncryption jwe = new JsonWebEncryption();
                jwe.setAlgorithmHeaderValue("RSA-OAEP-256");
                jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
                jwe.setKey(jwk.getPublicKey());
                String payloadIn = RsaOaepKeyManagementAlgorithmTest.EXAMPLE_PAYLOAD;
                jwe.setPayload(payloadIn);
                String compactSerialization = jwe.getCompactSerialization();
                jwe = new JsonWebEncryption();
                jwe.setCompactSerialization(compactSerialization);
                jwe.setKey(jwk.getPrivateKey());
                String payloadOut = jwe.getPayload();
                Assert.assertEquals((Object)payloadIn, (Object)payloadOut);
            }
        });
    }

    private boolean doubleCheckRsaOaep256() {
        String rsaOaep256;
        AlgorithmFactory<KeyManagementAlgorithm> jweKeyManagementAlgorithmFactory = AlgorithmFactoryFactory.getInstance().getJweKeyManagementAlgorithmFactory();
        boolean available = jweKeyManagementAlgorithmFactory.isAvailable(rsaOaep256 = "RSA-OAEP-256");
        if (!available) {
            Logger log = LoggerFactory.getLogger(this.getClass());
            log.warn("\n\n\n\n**not** testing " + rsaOaep256 + " because it is unavailable due to the underlying JCA provider set up " + Arrays.toString(Security.getProviders()) + "\n\n\n\n");
        }
        return available;
    }

    @Test
    public void testWorkingExampleFromMailList() throws Exception {
        String cs = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.fL5IL5cMCjjU9G9_ZjsD2XO0HIwTOwbVwulcZVw31_rx2qTcHzbYhIvrvbcVLTfJzn8xbQ3UEL442ZgZ1PcFYKENYePXiEyvYxPN8dmvj_OfLSJDEqR6kvwOb6nghGtxfzdB_VRvFt2eehbCA3gWpiOYHHvSTFdBPGx2KZHQisLz3oZR8EWiZ1woEpHy8a7FoQ2zzuDlZEJQOUrh09b_EJxmcE2jL6wmEtgabyxy3VgWg3GqSPUISlJZV9HThuVJezzktJdpntRDnAPUqjc8IwByGpMleIQcPuBUseRRPr_OsroOJ6eTl5DuFCmBOKb-eNNw5v-GEcVYr1w7X9oXoA.0frdIwx8P8UAzh1s9_PgOA.RAzILH0xfs0yxzML1CzzGExCfE2_wzWKs0FVuXfM8R5H68yTqTbqIqRCp2feAH5GSvluzmztk2_CkGNSjAyoaw.4nMUXOgmgWvM-08tIZ-h5w";
        JceProviderTestSupport jceProviderTestSupport = new JceProviderTestSupport();
        jceProviderTestSupport.setKeyManagementAlgsNeeded("RSA-OAEP-256");
        jceProviderTestSupport.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                if (!RsaOaepKeyManagementAlgorithmTest.this.doubleCheckRsaOaep256()) {
                    return;
                }
                RsaJsonWebKey jwk = (RsaJsonWebKey)PublicJsonWebKey.Factory.newPublicJwk(RsaOaepKeyManagementAlgorithmTest.JWK_JSON);
                JsonWebEncryption jwe = new JsonWebEncryption();
                jwe.setCompactSerialization("eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.fL5IL5cMCjjU9G9_ZjsD2XO0HIwTOwbVwulcZVw31_rx2qTcHzbYhIvrvbcVLTfJzn8xbQ3UEL442ZgZ1PcFYKENYePXiEyvYxPN8dmvj_OfLSJDEqR6kvwOb6nghGtxfzdB_VRvFt2eehbCA3gWpiOYHHvSTFdBPGx2KZHQisLz3oZR8EWiZ1woEpHy8a7FoQ2zzuDlZEJQOUrh09b_EJxmcE2jL6wmEtgabyxy3VgWg3GqSPUISlJZV9HThuVJezzktJdpntRDnAPUqjc8IwByGpMleIQcPuBUseRRPr_OsroOJ6eTl5DuFCmBOKb-eNNw5v-GEcVYr1w7X9oXoA.0frdIwx8P8UAzh1s9_PgOA.RAzILH0xfs0yxzML1CzzGExCfE2_wzWKs0FVuXfM8R5H68yTqTbqIqRCp2feAH5GSvluzmztk2_CkGNSjAyoaw.4nMUXOgmgWvM-08tIZ-h5w");
                jwe.setKey(jwk.getPrivateKey());
                String payloadOut = jwe.getPayload();
                Assert.assertEquals((Object)RsaOaepKeyManagementAlgorithmTest.EXAMPLE_PAYLOAD, (Object)payloadOut);
            }
        });
    }

    static /* synthetic */ boolean access$0(RsaOaepKeyManagementAlgorithmTest rsaOaepKeyManagementAlgorithmTest) {
        return rsaOaepKeyManagementAlgorithmTest.doubleCheckRsaOaep256();
    }
}

