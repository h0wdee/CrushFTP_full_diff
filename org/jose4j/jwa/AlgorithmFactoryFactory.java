/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwa;

import java.security.Security;
import java.util.Arrays;
import org.jose4j.jwa.AlgorithmFactory;
import org.jose4j.jwe.AesCbcHmacSha2ContentEncryptionAlgorithm;
import org.jose4j.jwe.AesGcmContentEncryptionAlgorithm;
import org.jose4j.jwe.AesGcmKeyEncryptionAlgorithm;
import org.jose4j.jwe.AesKeyWrapManagementAlgorithm;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.DirectKeyManagementAlgorithm;
import org.jose4j.jwe.EcdhKeyAgreementAlgorithm;
import org.jose4j.jwe.EcdhKeyAgreementWithAesKeyWrapAlgorithm;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwe.Pbes2HmacShaWithAesKeyWrapAlgorithm;
import org.jose4j.jwe.RsaKeyManagementAlgorithm;
import org.jose4j.jws.EcdsaUsingShaAlgorithm;
import org.jose4j.jws.HmacUsingShaAlgorithm;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.jws.PlaintextNoneAlgorithm;
import org.jose4j.jws.RsaUsingShaAlgorithm;
import org.jose4j.zip.CompressionAlgorithm;
import org.jose4j.zip.DeflateRFC1951CompressionAlgorithm;

public class AlgorithmFactoryFactory {
    private static final AlgorithmFactoryFactory factoryFactory = new AlgorithmFactoryFactory();
    private AlgorithmFactory<JsonWebSignatureAlgorithm> jwsAlgorithmFactory;
    private AlgorithmFactory<KeyManagementAlgorithm> jweKeyMgmtModeAlgorithmFactory;
    private AlgorithmFactory<ContentEncryptionAlgorithm> jweContentEncryptionAlgorithmFactory;
    private AlgorithmFactory<CompressionAlgorithm> compressionAlgorithmFactory;

    private AlgorithmFactoryFactory() {
        this.initialize();
    }

    void reinitialize() {
        this.initialize();
    }

    private void initialize() {
        String version = System.getProperty("java.version");
        String vendor = System.getProperty("java.vendor");
        String home = System.getProperty("java.home");
        String providers = Arrays.toString(Security.getProviders());
        long startTime = System.currentTimeMillis();
        this.jwsAlgorithmFactory = new AlgorithmFactory<JsonWebSignatureAlgorithm>("alg", JsonWebSignatureAlgorithm.class);
        this.jwsAlgorithmFactory.registerAlgorithm(new PlaintextNoneAlgorithm());
        this.jwsAlgorithmFactory.registerAlgorithm(new HmacUsingShaAlgorithm.HmacSha256());
        this.jwsAlgorithmFactory.registerAlgorithm(new HmacUsingShaAlgorithm.HmacSha384());
        this.jwsAlgorithmFactory.registerAlgorithm(new HmacUsingShaAlgorithm.HmacSha512());
        this.jwsAlgorithmFactory.registerAlgorithm(new EcdsaUsingShaAlgorithm.EcdsaP256UsingSha256());
        this.jwsAlgorithmFactory.registerAlgorithm(new EcdsaUsingShaAlgorithm.EcdsaP384UsingSha384());
        this.jwsAlgorithmFactory.registerAlgorithm(new EcdsaUsingShaAlgorithm.EcdsaP521UsingSha512());
        this.jwsAlgorithmFactory.registerAlgorithm(new RsaUsingShaAlgorithm.RsaSha256());
        this.jwsAlgorithmFactory.registerAlgorithm(new RsaUsingShaAlgorithm.RsaSha384());
        this.jwsAlgorithmFactory.registerAlgorithm(new RsaUsingShaAlgorithm.RsaSha512());
        this.jwsAlgorithmFactory.registerAlgorithm(new RsaUsingShaAlgorithm.RsaPssSha256());
        this.jwsAlgorithmFactory.registerAlgorithm(new RsaUsingShaAlgorithm.RsaPssSha384());
        this.jwsAlgorithmFactory.registerAlgorithm(new RsaUsingShaAlgorithm.RsaPssSha512());
        this.jweKeyMgmtModeAlgorithmFactory = new AlgorithmFactory<KeyManagementAlgorithm>("alg", KeyManagementAlgorithm.class);
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new RsaKeyManagementAlgorithm.Rsa1_5());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new RsaKeyManagementAlgorithm.RsaOaep());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new RsaKeyManagementAlgorithm.RsaOaep256());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new DirectKeyManagementAlgorithm());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new AesKeyWrapManagementAlgorithm.Aes128());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new AesKeyWrapManagementAlgorithm.Aes192());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new AesKeyWrapManagementAlgorithm.Aes256());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new EcdhKeyAgreementAlgorithm());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new EcdhKeyAgreementWithAesKeyWrapAlgorithm.EcdhKeyAgreementWithAes128KeyWrapAlgorithm());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new EcdhKeyAgreementWithAesKeyWrapAlgorithm.EcdhKeyAgreementWithAes192KeyWrapAlgorithm());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new EcdhKeyAgreementWithAesKeyWrapAlgorithm.EcdhKeyAgreementWithAes256KeyWrapAlgorithm());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new Pbes2HmacShaWithAesKeyWrapAlgorithm.HmacSha256Aes128());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new Pbes2HmacShaWithAesKeyWrapAlgorithm.HmacSha384Aes192());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new Pbes2HmacShaWithAesKeyWrapAlgorithm.HmacSha512Aes256());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new AesGcmKeyEncryptionAlgorithm.Aes128Gcm());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new AesGcmKeyEncryptionAlgorithm.Aes192Gcm());
        this.jweKeyMgmtModeAlgorithmFactory.registerAlgorithm(new AesGcmKeyEncryptionAlgorithm.Aes256Gcm());
        this.jweContentEncryptionAlgorithmFactory = new AlgorithmFactory<ContentEncryptionAlgorithm>("enc", ContentEncryptionAlgorithm.class);
        this.jweContentEncryptionAlgorithmFactory.registerAlgorithm(new AesCbcHmacSha2ContentEncryptionAlgorithm.Aes128CbcHmacSha256());
        this.jweContentEncryptionAlgorithmFactory.registerAlgorithm(new AesCbcHmacSha2ContentEncryptionAlgorithm.Aes192CbcHmacSha384());
        this.jweContentEncryptionAlgorithmFactory.registerAlgorithm(new AesCbcHmacSha2ContentEncryptionAlgorithm.Aes256CbcHmacSha512());
        this.jweContentEncryptionAlgorithmFactory.registerAlgorithm(new AesGcmContentEncryptionAlgorithm.Aes128Gcm());
        this.jweContentEncryptionAlgorithmFactory.registerAlgorithm(new AesGcmContentEncryptionAlgorithm.Aes192Gcm());
        this.jweContentEncryptionAlgorithmFactory.registerAlgorithm(new AesGcmContentEncryptionAlgorithm.Aes256Gcm());
        this.compressionAlgorithmFactory = new AlgorithmFactory<CompressionAlgorithm>("zip", CompressionAlgorithm.class);
        this.compressionAlgorithmFactory.registerAlgorithm(new DeflateRFC1951CompressionAlgorithm());
    }

    public static AlgorithmFactoryFactory getInstance() {
        return factoryFactory;
    }

    public AlgorithmFactory<JsonWebSignatureAlgorithm> getJwsAlgorithmFactory() {
        return this.jwsAlgorithmFactory;
    }

    public AlgorithmFactory<KeyManagementAlgorithm> getJweKeyManagementAlgorithmFactory() {
        return this.jweKeyMgmtModeAlgorithmFactory;
    }

    public AlgorithmFactory<ContentEncryptionAlgorithm> getJweContentEncryptionAlgorithmFactory() {
        return this.jweContentEncryptionAlgorithmFactory;
    }

    public AlgorithmFactory<CompressionAlgorithm> getCompressionAlgorithmFactory() {
        return this.compressionAlgorithmFactory;
    }
}

