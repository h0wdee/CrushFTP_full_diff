/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.jce.provider.BouncyCastleProvider
 *  lw.bouncycastle.openpgp.PGPEncryptedDataGenerator
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPOnePassSignature
 *  lw.bouncycastle.openpgp.PGPPrivateKey
 *  lw.bouncycastle.openpgp.PGPPublicKey
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPSignature
 *  lw.bouncycastle.openpgp.PGPSignatureGenerator
 *  lw.bouncycastle.openpgp.PGPV3SignatureGenerator
 *  lw.bouncycastle.openpgp.operator.KeyFingerPrintCalculator
 *  lw.bouncycastle.openpgp.operator.PBEDataDecryptorFactory
 *  lw.bouncycastle.openpgp.operator.PBEKeyEncryptionMethodGenerator
 *  lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor
 *  lw.bouncycastle.openpgp.operator.PBESecretKeyEncryptor
 *  lw.bouncycastle.openpgp.operator.PGPContentSignerBuilder
 *  lw.bouncycastle.openpgp.operator.PGPContentVerifierBuilderProvider
 *  lw.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder
 *  lw.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider
 *  lw.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory
 *  lw.bouncycastle.openpgp.operator.PublicKeyKeyEncryptionMethodGenerator
 *  lw.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
 *  lw.bouncycastle.openpgp.operator.bc.BcPBEDataDecryptorFactory
 *  lw.bouncycastle.openpgp.operator.bc.BcPBEKeyEncryptionMethodGenerator
 *  lw.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder
 *  lw.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder
 *  lw.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder
 *  lw.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider
 *  lw.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder
 *  lw.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider
 *  lw.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory
 *  lw.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator
 *  lw.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator
 *  lw.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder
 *  lw.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider
 *  lw.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder
 *  lw.bouncycastle.openpgp.operator.jcajce.JcePBEDataDecryptorFactoryBuilder
 *  lw.bouncycastle.openpgp.operator.jcajce.JcePBEKeyEncryptionMethodGenerator
 *  lw.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder
 *  lw.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder
 *  lw.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder
 *  lw.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder
 *  lw.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator
 */
package com.didisoft.pgp.bc;

import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.bc.IOUtil;
import java.io.IOException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import lw.bouncycastle.jce.provider.BouncyCastleProvider;
import lw.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import lw.bouncycastle.openpgp.PGPOnePassSignature;
import lw.bouncycastle.openpgp.PGPPrivateKey;
import lw.bouncycastle.openpgp.PGPPublicKey;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPSignature;
import lw.bouncycastle.openpgp.PGPSignatureGenerator;
import lw.bouncycastle.openpgp.PGPV3SignatureGenerator;
import lw.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import lw.bouncycastle.openpgp.operator.PBEDataDecryptorFactory;
import lw.bouncycastle.openpgp.operator.PBEKeyEncryptionMethodGenerator;
import lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import lw.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import lw.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import lw.bouncycastle.openpgp.operator.PGPContentVerifierBuilderProvider;
import lw.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import lw.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider;
import lw.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import lw.bouncycastle.openpgp.operator.PublicKeyKeyEncryptionMethodGenerator;
import lw.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import lw.bouncycastle.openpgp.operator.bc.BcPBEDataDecryptorFactory;
import lw.bouncycastle.openpgp.operator.bc.BcPBEKeyEncryptionMethodGenerator;
import lw.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import lw.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import lw.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import lw.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import lw.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import lw.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import lw.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import lw.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import lw.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import lw.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import lw.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import lw.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import lw.bouncycastle.openpgp.operator.jcajce.JcePBEDataDecryptorFactoryBuilder;
import lw.bouncycastle.openpgp.operator.jcajce.JcePBEKeyEncryptionMethodGenerator;
import lw.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import lw.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import lw.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import lw.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import lw.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

public class BCFactory {
    private boolean useJce = false;

    public BCFactory(boolean bl) {
        this.setUseJce(bl);
    }

    public PGPPublicKeyRing CreatePGPPublicKeyRing(byte[] byArray) throws IOException {
        return new PGPPublicKeyRing(byArray, this.CreateKeyFingerPrintCalculator());
    }

    public KeyFingerPrintCalculator CreateKeyFingerPrintCalculator() {
        if (this.isUseJce()) {
            return new JcaKeyFingerprintCalculator();
        }
        return new BcKeyFingerprintCalculator();
    }

    public PBESecretKeyDecryptor CreatePBESecretKeyDecryptor(char[] cArray) throws lw.bouncycastle.openpgp.PGPException {
        if (this.isUseJce()) {
            return new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(cArray);
        }
        return new BcPBESecretKeyDecryptorBuilder((PGPDigestCalculatorProvider)new BcPGPDigestCalculatorProvider()).build(cArray);
    }

    public PBESecretKeyDecryptor CreatePBESecretKeyDecryptor(String string) throws lw.bouncycastle.openpgp.PGPException {
        if (this.isUseJce()) {
            return new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(string == null ? "".toCharArray() : string.toCharArray());
        }
        return new BcPBESecretKeyDecryptorBuilder((PGPDigestCalculatorProvider)new BcPGPDigestCalculatorProvider()).build(string == null ? "".toCharArray() : string.toCharArray());
    }

    public PBESecretKeyEncryptor CreatePBESecretKeyEncryptor(String string, int n) throws lw.bouncycastle.openpgp.PGPException {
        if (this.isUseJce()) {
            return new JcePBESecretKeyEncryptorBuilder(n).setProvider("BC").build(string == null ? "".toCharArray() : string.toCharArray());
        }
        return new BcPBESecretKeyEncryptorBuilder(n).build(string == null ? "".toCharArray() : string.toCharArray());
    }

    public PBEDataDecryptorFactory CreatePBEDataDecryptorFactory(String string) throws lw.bouncycastle.openpgp.PGPException {
        if (this.isUseJce()) {
            return new JcePBEDataDecryptorFactoryBuilder(new JcaPGPDigestCalculatorProviderBuilder().setProvider("BC").build()).build(string == null ? new char[]{} : string.toCharArray());
        }
        return new BcPBEDataDecryptorFactory(string == null ? new char[]{} : string.toCharArray(), new BcPGPDigestCalculatorProvider());
    }

    public PGPSignatureGenerator CreatePGPSignatureGenerator(int n, int n2) {
        return new PGPSignatureGenerator(this.CreatePGPContentSignerBuilder(n, n2));
    }

    public PGPV3SignatureGenerator CreatePGPV3SignatureGenerator(int n, int n2) {
        return new PGPV3SignatureGenerator(this.CreatePGPContentSignerBuilder(n, n2));
    }

    public PGPContentSignerBuilder CreatePGPContentSignerBuilder(int n, int n2) {
        if (this.isUseJce()) {
            return new JcaPGPContentSignerBuilder(n, n2);
        }
        return new BcPGPContentSignerBuilder(n, n2);
    }

    public void initSign(PGPSignatureGenerator pGPSignatureGenerator, int n, PGPPrivateKey pGPPrivateKey) throws PGPException {
        try {
            pGPSignatureGenerator.init(n, pGPPrivateKey);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    public void initSign(PGPV3SignatureGenerator pGPV3SignatureGenerator, int n, PGPPrivateKey pGPPrivateKey) throws PGPException {
        try {
            pGPV3SignatureGenerator.init(n, pGPPrivateKey);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    public PublicKeyKeyEncryptionMethodGenerator CreatePublicKeyKeyEncryptionMethodGenerator(PGPPublicKey pGPPublicKey) {
        if (this.isUseJce()) {
            return new JcePublicKeyKeyEncryptionMethodGenerator(pGPPublicKey);
        }
        return new BcPublicKeyKeyEncryptionMethodGenerator(pGPPublicKey);
    }

    public void initVerify(PGPSignature pGPSignature, PGPPublicKey pGPPublicKey) throws PGPException {
        try {
            pGPSignature.init(this.CreatePGPContentVerifierBuilderProvider(), pGPPublicKey);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    public void initVerify(PGPOnePassSignature pGPOnePassSignature, PGPPublicKey pGPPublicKey) throws PGPException {
        try {
            pGPOnePassSignature.init(this.CreatePGPContentVerifierBuilderProvider(), pGPPublicKey);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    public PGPContentVerifierBuilderProvider CreatePGPContentVerifierBuilderProvider() {
        if (this.isUseJce()) {
            return new JcaPGPContentVerifierBuilderProvider().setProvider("BC");
        }
        return new BcPGPContentVerifierBuilderProvider();
    }

    public PBEKeyEncryptionMethodGenerator CreatePBEKeyEncryptionMethodGenerator(String string) {
        if (this.isUseJce()) {
            return new JcePBEKeyEncryptionMethodGenerator(string == null ? new char[]{} : string.toCharArray());
        }
        return new BcPBEKeyEncryptionMethodGenerator(string == null ? new char[]{} : string.toCharArray());
    }

    public PBEKeyEncryptionMethodGenerator CreatePBEKeyEncryptionMethodGenerator(char[] cArray) {
        if (this.useJce) {
            return new JcePBEKeyEncryptionMethodGenerator(cArray);
        }
        return new BcPBEKeyEncryptionMethodGenerator(cArray);
    }

    public PGPEncryptedDataGenerator CreatePGPEncryptedDataGenerator(int n, boolean bl, SecureRandom secureRandom, boolean bl2) {
        return new PGPEncryptedDataGenerator(this.CreatePGPDataEncryptorBuilder(n, bl, secureRandom), bl2);
    }

    public PGPEncryptedDataGenerator CreatePGPEncryptedDataGenerator(int n, boolean bl, SecureRandom secureRandom) {
        return new PGPEncryptedDataGenerator(this.CreatePGPDataEncryptorBuilder(n, bl, secureRandom));
    }

    public PGPDataEncryptorBuilder CreatePGPDataEncryptorBuilder(int n, boolean bl, SecureRandom secureRandom) {
        if (this.isUseJce()) {
            JcePGPDataEncryptorBuilder jcePGPDataEncryptorBuilder = new JcePGPDataEncryptorBuilder(n);
            jcePGPDataEncryptorBuilder.setSecureRandom(secureRandom);
            jcePGPDataEncryptorBuilder.setWithIntegrityPacket(bl);
            jcePGPDataEncryptorBuilder.setProvider("BC");
            return jcePGPDataEncryptorBuilder;
        }
        BcPGPDataEncryptorBuilder bcPGPDataEncryptorBuilder = new BcPGPDataEncryptorBuilder(n);
        bcPGPDataEncryptorBuilder.setSecureRandom(secureRandom);
        bcPGPDataEncryptorBuilder.setWithIntegrityPacket(bl);
        return bcPGPDataEncryptorBuilder;
    }

    public PublicKeyDataDecryptorFactory CreatePublicKeyDataDecryptorFactory(PGPPrivateKey pGPPrivateKey) {
        if (this.isUseJce()) {
            return new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(pGPPrivateKey);
        }
        return new BcPublicKeyDataDecryptorFactory(pGPPrivateKey);
    }

    public boolean isUseJce() {
        return this.useJce;
    }

    public void setUseJce(boolean bl) {
        this.useJce = bl;
        if (bl && Security.getProvider("BC") == null) {
            Security.addProvider((Provider)new BouncyCastleProvider());
        }
    }
}

