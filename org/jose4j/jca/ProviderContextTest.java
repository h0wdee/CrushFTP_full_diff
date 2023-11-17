/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jca;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.JceProviderTestSupport;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.keys.ExampleRsaJwksFromJwe;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class ProviderContextTest {
    public static final String NO_SUCH_PROVIDER = "-_NO__SUCH__PROVIDER_-";
    public static final ProviderContext EMPTY_CONTEXT = new ProviderContext();

    @Test
    public void testGeneralDefaulting() {
        ProviderContext pc = new ProviderContext();
        Assert.assertNull((Object)pc.getSecureRandom());
        String generalProvider = "some-provider";
        String specificProvider = "some-other-provider";
        ProviderContext.Context[] contextArray = new ProviderContext.Context[]{pc.getGeneralProviderContext(), pc.getSuppliedKeyProviderContext()};
        int n = contextArray.length;
        int n2 = 0;
        while (n2 < n) {
            ProviderContext.Context pcc = contextArray[n2];
            Assert.assertNull((Object)pcc.getGeneralProvider());
            Assert.assertNull((Object)pcc.getCipherProvider());
            Assert.assertNull((Object)pcc.getKeyAgreementProvider());
            Assert.assertNull((Object)pcc.getKeyFactoryProvider());
            Assert.assertNull((Object)pcc.getKeyPairGeneratorProvider());
            Assert.assertNull((Object)pcc.getMacProvider());
            Assert.assertNull((Object)pcc.getMessageDigestProvider());
            Assert.assertNull((Object)pcc.getSignatureProvider());
            pcc.setGeneralProvider(generalProvider);
            Assert.assertThat((Object)pcc.getCipherProvider(), (Matcher)CoreMatchers.equalTo((Object)generalProvider));
            pcc.setCipherProvider(specificProvider);
            Assert.assertThat((Object)pcc.getCipherProvider(), (Matcher)CoreMatchers.equalTo((Object)specificProvider));
            pcc.setGeneralProvider(generalProvider);
            Assert.assertThat((Object)pcc.getKeyAgreementProvider(), (Matcher)CoreMatchers.equalTo((Object)generalProvider));
            pcc.setKeyAgreementProvider(specificProvider);
            Assert.assertThat((Object)pcc.getKeyAgreementProvider(), (Matcher)CoreMatchers.equalTo((Object)specificProvider));
            pcc.setGeneralProvider(generalProvider);
            Assert.assertThat((Object)pcc.getKeyFactoryProvider(), (Matcher)CoreMatchers.equalTo((Object)generalProvider));
            pcc.setKeyFactoryProvider(specificProvider);
            Assert.assertThat((Object)pcc.getKeyFactoryProvider(), (Matcher)CoreMatchers.equalTo((Object)specificProvider));
            pcc.setGeneralProvider(generalProvider);
            Assert.assertThat((Object)pcc.getKeyPairGeneratorProvider(), (Matcher)CoreMatchers.equalTo((Object)generalProvider));
            pcc.setKeyPairGeneratorProvider(specificProvider);
            Assert.assertThat((Object)pcc.getKeyPairGeneratorProvider(), (Matcher)CoreMatchers.equalTo((Object)specificProvider));
            pcc.setGeneralProvider(generalProvider);
            Assert.assertThat((Object)pcc.getMacProvider(), (Matcher)CoreMatchers.equalTo((Object)generalProvider));
            pcc.setMacProvider(specificProvider);
            Assert.assertThat((Object)pcc.getMacProvider(), (Matcher)CoreMatchers.equalTo((Object)specificProvider));
            pcc.setGeneralProvider(generalProvider);
            Assert.assertThat((Object)pcc.getMessageDigestProvider(), (Matcher)CoreMatchers.equalTo((Object)generalProvider));
            pcc.setMessageDigestProvider(specificProvider);
            Assert.assertThat((Object)pcc.getMessageDigestProvider(), (Matcher)CoreMatchers.equalTo((Object)specificProvider));
            pcc.setGeneralProvider(generalProvider);
            Assert.assertThat((Object)pcc.getSignatureProvider(), (Matcher)CoreMatchers.equalTo((Object)generalProvider));
            pcc.setSignatureProvider(specificProvider);
            Assert.assertThat((Object)pcc.getSignatureProvider(), (Matcher)CoreMatchers.equalTo((Object)specificProvider));
            ++n2;
        }
    }

    @Test
    public void kindaLameTestForNonexistentProviderJwsRsa() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload("meh");
        jws.setAlgorithmHeaderValue("RS256");
        jws.setKey(ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey());
        ProviderContext providerCtx = new ProviderContext();
        providerCtx.getSuppliedKeyProviderContext().setSignatureProvider(NO_SUCH_PROVIDER);
        jws.setProviderContext(providerCtx);
        this.expectNoProviderProduce(jws);
        jws.setProviderContext(EMPTY_CONTEXT);
        String jwsCompactSerialization = jws.getCompactSerialization();
        jws = new JsonWebSignature();
        jws.setCompactSerialization(jwsCompactSerialization);
        jws.setKey(ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey());
        jws.setProviderContext(providerCtx);
        this.expectNoProviderConsume(jws);
        jws.setProviderContext(EMPTY_CONTEXT);
        Assert.assertTrue((boolean)jws.verifySignature());
    }

    @Test
    public void kindaLameTestForNonexistentProviderJwsEc() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload("whatever");
        jws.setAlgorithmHeaderValue("ES256");
        jws.setKey(ExampleEcKeysFromJws.PRIVATE_256);
        ProviderContext providerCtx = new ProviderContext();
        providerCtx.getSuppliedKeyProviderContext().setSignatureProvider(NO_SUCH_PROVIDER);
        jws.setProviderContext(providerCtx);
        this.expectNoProviderProduce(jws);
        jws.setProviderContext(EMPTY_CONTEXT);
        String jwsCompactSerialization = jws.getCompactSerialization();
        jws = new JsonWebSignature();
        jws.setCompactSerialization(jwsCompactSerialization);
        jws.setKey(ExampleEcKeysFromJws.PUBLIC_256);
        jws.setProviderContext(providerCtx);
        this.expectNoProviderConsume(jws);
        jws.setProviderContext(EMPTY_CONTEXT);
        Assert.assertTrue((boolean)jws.verifySignature());
    }

    @Test
    public void kindaLameTestForNonexistentProviderJwsHmac() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload("okay");
        jws.setAlgorithmHeaderValue("HS256");
        HmacKey key = new HmacKey(new byte[32]);
        jws.setKey(key);
        ProviderContext providerCtx = new ProviderContext();
        providerCtx.getSuppliedKeyProviderContext().setMacProvider(NO_SUCH_PROVIDER);
        jws.setProviderContext(providerCtx);
        this.expectNoProviderProduce(jws);
        jws.setProviderContext(EMPTY_CONTEXT);
        String jwsCompactSerialization = jws.getCompactSerialization();
        jws = new JsonWebSignature();
        jws.setCompactSerialization(jwsCompactSerialization);
        jws.setKey(key);
        jws.setProviderContext(providerCtx);
        this.expectNoProviderConsume(jws);
        jws.setProviderContext(EMPTY_CONTEXT);
        Assert.assertTrue((boolean)jws.verifySignature());
    }

    void expectNoProviderProduce(JsonWebStructure jwx) {
        try {
            String compactSerialization = jwx.getCompactSerialization();
            Assert.fail((String)("Shouldn't have gotten compact serialization " + compactSerialization));
        }
        catch (JoseException e) {
            Assert.assertThat((Object)e.getMessage(), (Matcher)CoreMatchers.containsString((String)NO_SUCH_PROVIDER));
        }
    }

    void expectNoProviderConsume(JsonWebStructure jwx) {
        try {
            String inside = jwx.getPayload();
            Assert.fail((String)("Shouldn't have gotten payload " + inside));
        }
        catch (JoseException e) {
            Assert.assertThat((Object)e.getMessage(), (Matcher)CoreMatchers.containsString((String)NO_SUCH_PROVIDER));
        }
    }

    @Test
    public void kindaLameTestForNonexistentProviderJweRsaOaepAnd15() throws JoseException {
        String[] stringArray = new String[]{"RSA-OAEP", "RSA1_5"};
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String alg = stringArray[n2];
            JsonWebEncryption jwe = new JsonWebEncryption();
            String payload = "meh";
            jwe.setPayload(payload);
            jwe.setAlgorithmHeaderValue(alg);
            jwe.setKey(ExampleRsaJwksFromJwe.APPENDIX_A_1.getPublicKey());
            jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
            ProviderContext providerCtx = new ProviderContext();
            providerCtx.getSuppliedKeyProviderContext().setCipherProvider(NO_SUCH_PROVIDER);
            jwe.setProviderContext(providerCtx);
            this.expectNoProviderProduce(jwe);
            jwe.setProviderContext(EMPTY_CONTEXT);
            String jwsCompactSerialization = jwe.getCompactSerialization();
            jwe = new JsonWebEncryption();
            jwe.setCompactSerialization(jwsCompactSerialization);
            jwe.setKey(ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey());
            jwe.setProviderContext(providerCtx);
            this.expectNoProviderConsume(jwe);
            jwe.setProviderContext(EMPTY_CONTEXT);
            Assert.assertThat((Object)jwe.getPayload(), (Matcher)CoreMatchers.equalTo((Object)payload));
            ++n2;
        }
    }

    @Test
    public void kindaLameTestForNonexistentProviderJweDirAesMac() throws JoseException {
        String mac = "MAC";
        String cipher = "Cipher";
        String[] stringArray = new String[]{"MAC", "Cipher"};
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String which = stringArray[n2];
            JsonWebEncryption jwe = new JsonWebEncryption();
            String payload = "meh";
            jwe.setPayload(payload);
            jwe.setAlgorithmHeaderValue("dir");
            AesKey key = new AesKey(new byte[32]);
            jwe.setKey(key);
            jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
            ProviderContext providerCtx = new ProviderContext();
            switch (which) {
                case "Cipher": {
                    providerCtx.getSuppliedKeyProviderContext().setCipherProvider(NO_SUCH_PROVIDER);
                    break;
                }
                case "MAC": {
                    providerCtx.getSuppliedKeyProviderContext().setMacProvider(NO_SUCH_PROVIDER);
                    break;
                }
                default: {
                    Assert.fail((String)"shouldn't get here");
                }
            }
            jwe.setProviderContext(providerCtx);
            this.expectNoProviderProduce(jwe);
            jwe = new JsonWebEncryption();
            jwe.setPayload(payload);
            jwe.setAlgorithmHeaderValue("dir");
            jwe.setKey(key);
            jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
            jwe.setProviderContext(EMPTY_CONTEXT);
            String jwsCompactSerialization = jwe.getCompactSerialization();
            jwe = new JsonWebEncryption();
            jwe.setCompactSerialization(jwsCompactSerialization);
            jwe.setKey(key);
            jwe.setProviderContext(providerCtx);
            this.expectNoProviderConsume(jwe);
            jwe.setProviderContext(EMPTY_CONTEXT);
            Assert.assertThat((Object)jwe.getPayload(), (Matcher)CoreMatchers.equalTo((Object)payload));
            ++n2;
        }
    }

    @Test
    public void kindaLameTestForNonexistentProviderJweAesCbcHmac() throws JoseException {
        JsonWebEncryption jwe = new JsonWebEncryption();
        String payload = "meh";
        jwe.setPayload(payload);
        jwe.setAlgorithmHeaderValue("dir");
        AesKey key = new AesKey(new byte[32]);
        jwe.setKey(key);
        jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        ProviderContext providerCtx = new ProviderContext();
        providerCtx.getSuppliedKeyProviderContext().setCipherProvider(NO_SUCH_PROVIDER);
        jwe.setProviderContext(providerCtx);
        this.expectNoProviderProduce(jwe);
        jwe = new JsonWebEncryption();
        jwe.setPayload(payload);
        jwe.setAlgorithmHeaderValue("dir");
        jwe.setKey(key);
        jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        jwe.setProviderContext(EMPTY_CONTEXT);
        String jwsCompactSerialization = jwe.getCompactSerialization();
        jwe = new JsonWebEncryption();
        jwe.setCompactSerialization(jwsCompactSerialization);
        jwe.setKey(key);
        jwe.setProviderContext(providerCtx);
        this.expectNoProviderConsume(jwe);
        jwe.setProviderContext(EMPTY_CONTEXT);
        Assert.assertThat((Object)jwe.getPayload(), (Matcher)CoreMatchers.equalTo((Object)payload));
    }

    @Test
    public void kindaLameTestForNonexistentProviderJweAeskws() throws Exception {
        JceProviderTestSupport support = new JceProviderTestSupport();
        support.setKeyManagementAlgsNeeded("A128GCMKW");
        support.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                String[] stringArray = new String[]{"A128KW", "A128GCMKW"};
                int n = stringArray.length;
                int n2 = 0;
                while (n2 < n) {
                    String alg = stringArray[n2];
                    JsonWebEncryption jwe = new JsonWebEncryption();
                    String payload = "meh";
                    jwe.setPayload(payload);
                    jwe.setAlgorithmHeaderValue(alg);
                    AesKey key = new AesKey(new byte[16]);
                    jwe.setKey(key);
                    jwe.setEncryptionMethodHeaderParameter("A128GCM");
                    ProviderContext providerCtx = new ProviderContext();
                    providerCtx.getSuppliedKeyProviderContext().setCipherProvider(ProviderContextTest.NO_SUCH_PROVIDER);
                    jwe.setProviderContext(providerCtx);
                    ProviderContextTest.this.expectNoProviderProduce(jwe);
                    jwe.setProviderContext(EMPTY_CONTEXT);
                    String jwsCompactSerialization = jwe.getCompactSerialization();
                    jwe = new JsonWebEncryption();
                    jwe.setCompactSerialization(jwsCompactSerialization);
                    jwe.setKey(key);
                    jwe.setProviderContext(providerCtx);
                    ProviderContextTest.this.expectNoProviderConsume(jwe);
                    jwe.setProviderContext(EMPTY_CONTEXT);
                    Assert.assertThat((Object)jwe.getPayload(), (Matcher)CoreMatchers.equalTo((Object)payload));
                    ++n2;
                }
            }
        });
    }

    @Test
    public void kindaLameTestForNonexistentProviderJweEc() throws JoseException {
        String[] algs = new String[]{"ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A256KW", "ECDH-ES+A192KW"};
        String keyFactory = "KF";
        String keyAgreement = "KA";
        String keyPairGenerator = "KPG";
        String[] stringArray = new String[]{"KF", "KA", "KPG"};
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String whichKind = stringArray[n2];
            String[] stringArray2 = algs;
            int n3 = algs.length;
            int n4 = 0;
            while (n4 < n3) {
                String alg = stringArray2[n4];
                JsonWebEncryption jwe = new JsonWebEncryption();
                String payload = "meh";
                jwe.setPayload(payload);
                jwe.setAlgorithmHeaderValue(alg);
                jwe.setKey(ExampleEcKeysFromJws.PUBLIC_256);
                jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
                ProviderContext providerCtx = new ProviderContext();
                switch (whichKind) {
                    case "KA": {
                        providerCtx.getSuppliedKeyProviderContext().setKeyAgreementProvider(NO_SUCH_PROVIDER);
                        break;
                    }
                    case "KF": {
                        providerCtx.getGeneralProviderContext().setKeyFactoryProvider(NO_SUCH_PROVIDER);
                        break;
                    }
                    case "KPG": {
                        providerCtx.getGeneralProviderContext().setKeyPairGeneratorProvider(NO_SUCH_PROVIDER);
                        break;
                    }
                    default: {
                        Assert.fail((String)"shouldn't get here");
                    }
                }
                jwe.setProviderContext(providerCtx);
                if (!whichKind.equals("KF")) {
                    this.expectNoProviderProduce(jwe);
                }
                jwe.setProviderContext(EMPTY_CONTEXT);
                String jwsCompactSerialization = jwe.getCompactSerialization();
                jwe = new JsonWebEncryption();
                jwe.setCompactSerialization(jwsCompactSerialization);
                jwe.setKey(ExampleEcKeysFromJws.PRIVATE_256);
                jwe.setProviderContext(providerCtx);
                if (!whichKind.equals("KPG")) {
                    this.expectNoProviderConsume(jwe);
                }
                jwe.setProviderContext(EMPTY_CONTEXT);
                Assert.assertThat((Object)jwe.getPayload(), (Matcher)CoreMatchers.equalTo((Object)payload));
                ++n4;
            }
            ++n2;
        }
    }

    @Test
    public void kindaLameTestForSelectingProviderJwsRsaWithBC() throws Exception {
        JceProviderTestSupport support = new JceProviderTestSupport();
        support.setUseBouncyCastleRegardlessOfAlgs(true);
        support.setPutBouncyCastleFirst(false);
        support.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                JsonWebSignature jws = new JsonWebSignature();
                jws.setAlgorithmHeaderValue("RS256");
                jws.setPayload("sign this");
                RSAPrivateKey pk = new RSAPrivateKey(){
                    RSAPrivateKey delegateKey = (RSAPrivateKey)ExampleRsaJwksFromJwe.APPENDIX_A_1.getPrivateKey();

                    @Override
                    public String getAlgorithm() {
                        return this.delegateKey.getAlgorithm();
                    }

                    @Override
                    public String getFormat() {
                        return this.delegateKey.getFormat();
                    }

                    @Override
                    public byte[] getEncoded() {
                        return this.delegateKey.getEncoded();
                    }

                    @Override
                    public BigInteger getPrivateExponent() {
                        this.lookAtStackTraceForBC();
                        return this.delegateKey.getPrivateExponent();
                    }

                    @Override
                    public BigInteger getModulus() {
                        return this.delegateKey.getModulus();
                    }

                    private void lookAtStackTraceForBC() {
                        boolean bc = false;
                        StackTraceElement[] stackTraceElementArray = new Exception().getStackTrace();
                        int n = stackTraceElementArray.length;
                        int n2 = 0;
                        while (n2 < n) {
                            StackTraceElement ste = stackTraceElementArray[n2];
                            if (ste.getClassName().contains(".bouncycastle.")) {
                                bc = true;
                            }
                            ++n2;
                        }
                        if (!bc) {
                            throw new IllegalStateException("Bouncy Castle not used!");
                        }
                    }
                };
                jws.setKey(pk);
                ProviderContext pc = new ProviderContext();
                pc.getSuppliedKeyProviderContext().setSignatureProvider("BC");
                jws.setProviderContext(pc);
                jws.getCompactSerialization();
            }
        });
    }

    @Test
    public void kindaLameTestForSelectingProviderForContentEncGcm() throws Exception {
        JceProviderTestSupport support = new JceProviderTestSupport();
        support.setUseBouncyCastleRegardlessOfAlgs(true);
        support.setPutBouncyCastleFirst(false);
        support.runWithBouncyCastleProviderIfNeeded(new JceProviderTestSupport.RunnableTest(){

            @Override
            public void runTest() throws Exception {
                String[] stringArray = new String[]{"A128GCM", "A128GCM", "A256GCM"};
                int n = stringArray.length;
                int n2 = 0;
                while (n2 < n) {
                    String enc = stringArray[n2];
                    JsonWebEncryption jwe = new JsonWebEncryption();
                    jwe.setAlgorithmHeaderValue("A128KW");
                    jwe.setEncryptionMethodHeaderParameter(enc);
                    String payloadIn = "encrypt me";
                    jwe.setPayload("encrypt me");
                    byte[] byArray = new byte[16];
                    byArray[0] = 1;
                    byArray[1] = 2;
                    byArray[2] = 1;
                    byArray[3] = 1;
                    byArray[6] = 1;
                    byArray[7] = 2;
                    byArray[8] = 9;
                    byArray[9] = 1;
                    byArray[11] = 5;
                    byArray[12] = 1;
                    byArray[13] = 7;
                    byArray[14] = 1;
                    byArray[15] = 4;
                    AesKey key = new AesKey(byArray);
                    jwe.setKey(key);
                    ProviderContext pc = new ProviderContext();
                    pc.getGeneralProviderContext().setCipherProvider(ProviderContextTest.NO_SUCH_PROVIDER);
                    jwe.setProviderContext(pc);
                    ProviderContextTest.this.expectNoProviderProduce(jwe);
                    jwe = new JsonWebEncryption();
                    jwe.setAlgorithmHeaderValue("A128KW");
                    jwe.setEncryptionMethodHeaderParameter(enc);
                    jwe.setPayload("encrypt me");
                    jwe.setKey(key);
                    String compactSerialization = jwe.getCompactSerialization();
                    jwe = new JsonWebEncryption();
                    jwe.setCompactSerialization(compactSerialization);
                    jwe.setKey(key);
                    Assert.assertThat((Object)jwe.getPayload(), (Matcher)CoreMatchers.equalTo((Object)"encrypt me"));
                    jwe = new JsonWebEncryption();
                    jwe.setCompactSerialization(compactSerialization);
                    jwe.setKey(key);
                    jwe.setProviderContext(pc);
                    ProviderContextTest.this.expectNoProviderConsume(jwe);
                    ++n2;
                }
            }
        });
    }

    @Test
    public void kindaLameTestForSelectingProviderForContentEncCbcHmac() throws Exception {
        boolean[] blArray = new boolean[2];
        blArray[0] = true;
        boolean[] blArray2 = blArray;
        int n = blArray.length;
        int n2 = 0;
        while (n2 < n) {
            boolean doMac = blArray2[n2];
            String[] stringArray = new String[]{"A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512"};
            int n3 = stringArray.length;
            int n4 = 0;
            while (n4 < n3) {
                String enc = stringArray[n4];
                JsonWebEncryption jwe = new JsonWebEncryption();
                jwe.setAlgorithmHeaderValue("A128KW");
                jwe.setEncryptionMethodHeaderParameter(enc);
                String payloadIn = "encrypt me";
                jwe.setPayload("encrypt me");
                byte[] byArray = new byte[16];
                byArray[0] = 1;
                byArray[1] = 2;
                byArray[2] = 1;
                byArray[3] = 1;
                byArray[6] = 1;
                byArray[7] = 2;
                byArray[8] = 9;
                byArray[9] = 1;
                byArray[11] = 5;
                byArray[12] = 1;
                byArray[13] = 7;
                byArray[14] = 1;
                byArray[15] = 4;
                AesKey key = new AesKey(byArray);
                jwe.setKey(key);
                ProviderContext pc = new ProviderContext();
                ProviderContext.Context providerContext = pc.getGeneralProviderContext();
                if (doMac) {
                    providerContext.setMacProvider(NO_SUCH_PROVIDER);
                } else {
                    providerContext.setCipherProvider(NO_SUCH_PROVIDER);
                }
                jwe.setProviderContext(pc);
                this.expectNoProviderProduce(jwe);
                jwe = new JsonWebEncryption();
                jwe.setAlgorithmHeaderValue("A128KW");
                jwe.setEncryptionMethodHeaderParameter(enc);
                jwe.setPayload("encrypt me");
                jwe.setKey(key);
                String compactSerialization = jwe.getCompactSerialization();
                jwe = new JsonWebEncryption();
                jwe.setCompactSerialization(compactSerialization);
                jwe.setKey(key);
                Assert.assertThat((Object)jwe.getPayload(), (Matcher)CoreMatchers.equalTo((Object)"encrypt me"));
                jwe = new JsonWebEncryption();
                jwe.setCompactSerialization(compactSerialization);
                jwe.setKey(key);
                jwe.setProviderContext(pc);
                this.expectNoProviderConsume(jwe);
                ++n4;
            }
            ++n2;
        }
    }
}

