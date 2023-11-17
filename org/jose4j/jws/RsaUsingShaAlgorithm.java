/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jws;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import org.jose4j.jws.BaseSignatureAlgorithm;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.jwx.KeyValidationSupport;
import org.jose4j.lang.InvalidKeyException;

public class RsaUsingShaAlgorithm
extends BaseSignatureAlgorithm
implements JsonWebSignatureAlgorithm {
    static final int TRAILER = 1;
    static final String MGF1 = "MGF1";

    public RsaUsingShaAlgorithm(String id, String javaAlgo) {
        super(id, javaAlgo, "RSA");
    }

    @Override
    public void validatePublicKey(PublicKey key) throws InvalidKeyException {
        KeyValidationSupport.checkRsaKeySize(key);
    }

    @Override
    public void validatePrivateKey(PrivateKey privateKey) throws InvalidKeyException {
        KeyValidationSupport.checkRsaKeySize(privateKey);
    }

    public static class RsaPssSha256
    extends RsaUsingShaAlgorithm {
        public RsaPssSha256() {
            super("PS256", "SHA256withRSAandMGF1");
            MGF1ParameterSpec mgf1pec = MGF1ParameterSpec.SHA256;
            PSSParameterSpec pssSpec = new PSSParameterSpec(mgf1pec.getDigestAlgorithm(), RsaUsingShaAlgorithm.MGF1, mgf1pec, 32, 1);
            this.setAlgorithmParameterSpec(pssSpec);
        }
    }

    public static class RsaPssSha384
    extends RsaUsingShaAlgorithm {
        public RsaPssSha384() {
            super("PS384", "SHA384withRSAandMGF1");
            MGF1ParameterSpec mgf1pec = MGF1ParameterSpec.SHA384;
            PSSParameterSpec pssSpec = new PSSParameterSpec(mgf1pec.getDigestAlgorithm(), RsaUsingShaAlgorithm.MGF1, mgf1pec, 48, 1);
            this.setAlgorithmParameterSpec(pssSpec);
        }
    }

    public static class RsaPssSha512
    extends RsaUsingShaAlgorithm {
        public RsaPssSha512() {
            super("PS512", "SHA512withRSAandMGF1");
            MGF1ParameterSpec mgf1pec = MGF1ParameterSpec.SHA512;
            PSSParameterSpec pssSpec = new PSSParameterSpec(mgf1pec.getDigestAlgorithm(), RsaUsingShaAlgorithm.MGF1, mgf1pec, 64, 1);
            this.setAlgorithmParameterSpec(pssSpec);
        }
    }

    public static class RsaSha256
    extends RsaUsingShaAlgorithm {
        public RsaSha256() {
            super("RS256", "SHA256withRSA");
        }
    }

    public static class RsaSha384
    extends RsaUsingShaAlgorithm {
        public RsaSha384() {
            super("RS384", "SHA384withRSA");
        }
    }

    public static class RsaSha512
    extends RsaUsingShaAlgorithm {
        public RsaSha512() {
            super("RS512", "SHA512withRSA");
        }
    }
}

