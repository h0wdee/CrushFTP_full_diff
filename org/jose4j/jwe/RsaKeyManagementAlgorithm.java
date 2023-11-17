/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwe.CipherUtil;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwe.WrappingKeyManagementAlgorithm;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwx.KeyValidationSupport;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public class RsaKeyManagementAlgorithm
extends WrappingKeyManagementAlgorithm
implements KeyManagementAlgorithm {
    public RsaKeyManagementAlgorithm(String javaAlg, String alg) {
        super(javaAlg, alg);
        this.setKeyType("RSA");
        this.setKeyPersuasion(KeyPersuasion.ASYMMETRIC);
    }

    @Override
    public void validateEncryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        PublicKey pk = KeyValidationSupport.castKey(managementKey, PublicKey.class);
        KeyValidationSupport.checkRsaKeySize(pk);
    }

    @Override
    public void validateDecryptionKey(Key managementKey, ContentEncryptionAlgorithm contentEncryptionAlg) throws InvalidKeyException {
        PrivateKey pk = KeyValidationSupport.castKey(managementKey, RSAPrivateKey.class);
        KeyValidationSupport.checkRsaKeySize(pk);
    }

    @Override
    public boolean isAvailable() {
        try {
            return CipherUtil.getCipher(this.getJavaAlgorithm(), null) != null;
        }
        catch (JoseException e) {
            return false;
        }
    }

    public static class Rsa1_5
    extends RsaKeyManagementAlgorithm
    implements KeyManagementAlgorithm {
        public Rsa1_5() {
            super("RSA/ECB/PKCS1Padding", "RSA1_5");
        }
    }

    public static class RsaOaep
    extends RsaKeyManagementAlgorithm
    implements KeyManagementAlgorithm {
        public RsaOaep() {
            super("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", "RSA-OAEP");
        }
    }

    public static class RsaOaep256
    extends RsaKeyManagementAlgorithm
    implements KeyManagementAlgorithm {
        public RsaOaep256() {
            super("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "RSA-OAEP-256");
            this.setAlgorithmParameterSpec(new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        }

        @Override
        public boolean isAvailable() {
            try {
                JsonWebKey jwk = JsonWebKey.Factory.newJwk("{\"kty\":\"RSA\",\"n\":\"sXchDaQebHnPiGvyDOAT4saGEUetSyo9MKLOoWFsueri23bOdgWp4Dy1WlUzewbgBHod5pcM9H95GQRV3JDXboIRROSBigeC5yjU1hGzHHyXss8UDprecbAYxknTcQkhslANGRUZmdTOQ5qTRsLAt6BTYuyvVRdhS8exSZEy_c4gs_7svlJJQ4H9_NxsiIoLwAEk7-Q3UXERGYw_75IDrGA84-lA_-Ct4eTlXHBIY2EaV7t7LjJaynVJCpkv4LKjTTAumiGUIuQhrNhZLuF_RJLqHpM2kgWFLU7-VTdL1VbC2tejvcI2BlMkEpk1BzBZI0KQB0GaDWFLN-aEAw3vRw\",\"e\":\"AQAB\"}");
                ContentEncryptionKeyDescriptor cekDesc = new ContentEncryptionKeyDescriptor(16, "AES");
                ContentEncryptionKeys contentEncryptionKeys = this.manageForEncrypt(jwk.getKey(), cekDesc, null, null, new ProviderContext());
                return contentEncryptionKeys != null;
            }
            catch (JoseException e) {
                return false;
            }
        }
    }
}

