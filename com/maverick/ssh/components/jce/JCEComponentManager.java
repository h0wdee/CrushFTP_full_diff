/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentFactory;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.SshDsaPrivateKey;
import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshHmac;
import com.maverick.ssh.components.SshKeyExchange;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPrivateCrtKey;
import com.maverick.ssh.components.SshRsaPrivateKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.SshSecureRandomGenerator;
import com.maverick.ssh.components.jce.AES128Cbc;
import com.maverick.ssh.components.jce.AES128Ctr;
import com.maverick.ssh.components.jce.AES128Gcm;
import com.maverick.ssh.components.jce.AES192Cbc;
import com.maverick.ssh.components.jce.AES192Ctr;
import com.maverick.ssh.components.jce.AES256Cbc;
import com.maverick.ssh.components.jce.AES256Ctr;
import com.maverick.ssh.components.jce.AES256Gcm;
import com.maverick.ssh.components.jce.AbstractDigest;
import com.maverick.ssh.components.jce.AbstractHmac;
import com.maverick.ssh.components.jce.ArcFour;
import com.maverick.ssh.components.jce.ArcFour128;
import com.maverick.ssh.components.jce.ArcFour256;
import com.maverick.ssh.components.jce.BlowfishCbc;
import com.maverick.ssh.components.jce.ChaCha20Poly1305;
import com.maverick.ssh.components.jce.ECUtils;
import com.maverick.ssh.components.jce.HmacMD5;
import com.maverick.ssh.components.jce.HmacMD596;
import com.maverick.ssh.components.jce.HmacMD5ETM;
import com.maverick.ssh.components.jce.HmacSha1;
import com.maverick.ssh.components.jce.HmacSha196;
import com.maverick.ssh.components.jce.HmacSha1ETM;
import com.maverick.ssh.components.jce.HmacSha256;
import com.maverick.ssh.components.jce.HmacSha256ETM;
import com.maverick.ssh.components.jce.HmacSha256_96;
import com.maverick.ssh.components.jce.HmacSha256_at_ssh_dot_com;
import com.maverick.ssh.components.jce.HmacSha512;
import com.maverick.ssh.components.jce.HmacSha512ETM;
import com.maverick.ssh.components.jce.HmacSha512_96;
import com.maverick.ssh.components.jce.JCEAlgorithms;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.MD5Digest;
import com.maverick.ssh.components.jce.OpenSshDsaCertificate;
import com.maverick.ssh.components.jce.OpenSshEcdsaSha2Nist256Certificate;
import com.maverick.ssh.components.jce.OpenSshEcdsaSha2Nist384Certificate;
import com.maverick.ssh.components.jce.OpenSshEcdsaSha2Nist521Certificate;
import com.maverick.ssh.components.jce.OpenSshEd25519Certificate;
import com.maverick.ssh.components.jce.OpenSshRsaCertificate;
import com.maverick.ssh.components.jce.OpenSshRsaSha256Certificate;
import com.maverick.ssh.components.jce.OpenSshRsaSha512Certificate;
import com.maverick.ssh.components.jce.SHA1Digest;
import com.maverick.ssh.components.jce.SHA256Digest;
import com.maverick.ssh.components.jce.SHA384Digest;
import com.maverick.ssh.components.jce.SHA512Digest;
import com.maverick.ssh.components.jce.SecureRND;
import com.maverick.ssh.components.jce.Ssh1Des;
import com.maverick.ssh.components.jce.Ssh1Des3;
import com.maverick.ssh.components.jce.Ssh1RsaPublicKey;
import com.maverick.ssh.components.jce.Ssh2DsaPrivateKey;
import com.maverick.ssh.components.jce.Ssh2DsaPublicKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2Nist256PublicKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2Nist384PublicKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2Nist521PublicKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPrivateKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPublicKey;
import com.maverick.ssh.components.jce.Ssh2RsaPrivateCrtKey;
import com.maverick.ssh.components.jce.Ssh2RsaPrivateKey;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKey;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKeySHA256;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKeySHA512;
import com.maverick.ssh.components.jce.SshEd25519PrivateKeyJCE;
import com.maverick.ssh.components.jce.SshEd25519PublicKeyJCE;
import com.maverick.ssh.components.jce.SshEd448PrivateKeyJCE;
import com.maverick.ssh.components.jce.SshEd448PublicKeyJCE;
import com.maverick.ssh.components.jce.SshX509DsaPublicKey;
import com.maverick.ssh.components.jce.SshX509DsaPublicKeyRfc6187;
import com.maverick.ssh.components.jce.SshX509EcdsaSha2Nist256Rfc6187;
import com.maverick.ssh.components.jce.SshX509EcdsaSha2Nist384Rfc6187;
import com.maverick.ssh.components.jce.SshX509EcdsaSha2Nist521Rfc6187;
import com.maverick.ssh.components.jce.SshX509Rsa2048Sha256Rfc6187;
import com.maverick.ssh.components.jce.SshX509RsaPublicKey;
import com.maverick.ssh.components.jce.SshX509RsaPublicKeyRfc6187;
import com.maverick.ssh.components.jce.SshX509RsaSha1PublicKey;
import com.maverick.ssh.components.jce.TripleDesCbc;
import com.maverick.ssh.components.jce.TripleDesCtr;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCEComponentManager
extends ComponentManager
implements JCEAlgorithms {
    static Logger log = LoggerFactory.getLogger(JCEComponentManager.class);
    SecureRND rnd;
    static boolean disableTests = false;
    static boolean loadClientKex = true;
    static boolean loadServerKex = true;

    public JCEComponentManager() {
        if (!AdaptiveConfiguration.getBoolean("enableBCProvider", true, new String[0]) || JCEProvider.isBCDisabled()) {
            if (log.isInfoEnabled()) {
                log.info("Automatic configuration of BouncyCastle is disabled");
            }
            JCEProvider.disableBouncyCastle();
            return;
        }
        try {
            JCEProvider.enableBouncyCastle(false);
        }
        catch (IllegalStateException ex) {
            log.error("Bouncycastle JCE not found in classpath");
        }
    }

    public static void disableStartupTests() {
        disableTests = true;
    }

    public static void disableServerKex() {
        loadServerKex = false;
    }

    public static void disableClientKex() {
        loadClientKex = false;
    }

    @Deprecated
    private void enableEd25519Provider() {
        try {
            Class<?> clz = Class.forName("net.i2p.crypto.eddsa.EdDSASecurityProvider");
            Security.addProvider((Provider)clz.newInstance());
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static void initializeDefaultProvider(Provider provider) {
        JCEProvider.initializeDefaultProvider(provider);
    }

    public static void initializeProviderForAlgorithm(String jceAlgorithm, Provider provider) {
        JCEProvider.initializeProviderForAlgorithm(jceAlgorithm, provider);
    }

    public static String getSecureRandomAlgorithm() {
        return JCEProvider.getSecureRandomAlgorithm();
    }

    public static void setSecureRandomAlgorithm(String secureRandomAlgorithm) {
        JCEProvider.setSecureRandomAlgorithm(secureRandomAlgorithm);
    }

    public static Provider getProviderForAlgorithm(String jceAlgorithm) {
        return JCEProvider.getProviderForAlgorithm(jceAlgorithm);
    }

    public static SecureRandom getSecureRandom() {
        return JCEProvider.getSecureRandom();
    }

    @Override
    public SshDsaPrivateKey createDsaPrivateKey(BigInteger p, BigInteger q, BigInteger g, BigInteger x, BigInteger y) throws SshException {
        return new Ssh2DsaPrivateKey(p, q, g, x, y);
    }

    @Override
    public SshDsaPublicKey createDsaPublicKey(BigInteger p, BigInteger q, BigInteger g, BigInteger y) throws SshException {
        try {
            return new Ssh2DsaPublicKey(p, q, g, y);
        }
        catch (Throwable e) {
            throw new SshException(e);
        }
    }

    @Override
    public SshDsaPublicKey createDsaPublicKey() {
        return new Ssh2DsaPublicKey();
    }

    @Override
    public SshRsaPrivateCrtKey createRsaPrivateCrtKey(BigInteger modulus, BigInteger publicExponent, BigInteger privateExponent, BigInteger primeP, BigInteger primeQ, BigInteger crtCoefficient) throws SshException {
        try {
            BigInteger primeExponentP = primeP.subtract(BigInteger.ONE);
            primeExponentP = privateExponent.mod(primeExponentP);
            BigInteger primeExponentQ = primeQ.subtract(BigInteger.ONE);
            primeExponentQ = privateExponent.mod(primeExponentQ);
            return new Ssh2RsaPrivateCrtKey(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
        }
        catch (Throwable e) {
            throw new SshException(e);
        }
    }

    @Override
    public SshRsaPrivateCrtKey createRsaPrivateCrtKey(BigInteger modulus, BigInteger publicExponent, BigInteger privateExponent, BigInteger primeP, BigInteger primeQ, BigInteger primeExponentP, BigInteger primeExponentQ, BigInteger crtCoefficient) throws SshException {
        try {
            return new Ssh2RsaPrivateCrtKey(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
        }
        catch (Throwable e) {
            throw new SshException(e);
        }
    }

    @Override
    public SshRsaPrivateKey createRsaPrivateKey(BigInteger modulus, BigInteger privateExponent) throws SshException {
        try {
            return new Ssh2RsaPrivateKey(modulus, privateExponent);
        }
        catch (Throwable t) {
            throw new SshException(t);
        }
    }

    @Override
    public SshRsaPublicKey createRsaPublicKey(BigInteger modulus, BigInteger publicExponent, int version) throws SshException {
        try {
            switch (version) {
                case 1: {
                    return new Ssh1RsaPublicKey(modulus, publicExponent);
                }
                case 2: {
                    return new Ssh2RsaPublicKey(modulus, publicExponent);
                }
            }
            throw new SshException("Illegal version number " + version, 5);
        }
        catch (Throwable e) {
            throw new SshException(e);
        }
    }

    @Override
    public SshRsaPublicKey createSsh2RsaPublicKey() throws SshException {
        return new Ssh2RsaPublicKey();
    }

    @Override
    public SshKeyPair generateDsaKeyPair(int bits) throws SshException {
        try {
            if (bits < 1024) {
                throw new SshException("The minimum number of bits supported for DSA key generation is 1024", 4);
            }
            KeyPairGenerator keyGen = JCEProvider.getProviderForAlgorithm("DSA") == null ? KeyPairGenerator.getInstance("DSA") : KeyPairGenerator.getInstance("DSA", JCEProvider.getProviderForAlgorithm("DSA"));
            keyGen.initialize(bits);
            KeyPair keypair = keyGen.genKeyPair();
            PrivateKey privateKey = keypair.getPrivate();
            PublicKey publicKey = keypair.getPublic();
            SshKeyPair pair = new SshKeyPair();
            pair.setPrivateKey(new Ssh2DsaPrivateKey((DSAPrivateKey)privateKey, (DSAPublicKey)publicKey));
            pair.setPublicKey(new Ssh2DsaPublicKey((DSAPublicKey)publicKey));
            return pair;
        }
        catch (NoSuchAlgorithmException e) {
            throw new SshException(e);
        }
    }

    @Override
    public SshKeyPair generateRsaKeyPair(int bits, int version) throws SshException {
        try {
            if (bits < 1024) {
                throw new SshException("The minimum number of bits supported for RSA key generation is 1024", 4);
            }
            KeyPairGenerator keyGen = JCEProvider.getProviderForAlgorithm("RSA") == null ? KeyPairGenerator.getInstance("RSA") : KeyPairGenerator.getInstance("RSA", JCEProvider.getProviderForAlgorithm("RSA"));
            keyGen.initialize(bits);
            KeyPair keypair = keyGen.genKeyPair();
            PrivateKey privateKey = keypair.getPrivate();
            PublicKey publicKey = keypair.getPublic();
            SshKeyPair pair = new SshKeyPair();
            if (!(privateKey instanceof RSAPrivateCrtKey)) {
                throw new SshException("RSA key generation requires RSAPrivateCrtKey as private key type.", 16);
            }
            pair.setPrivateKey(new Ssh2RsaPrivateCrtKey((RSAPrivateCrtKey)privateKey));
            if (version == 1) {
                pair.setPublicKey(new Ssh1RsaPublicKey((RSAPublicKey)publicKey));
            } else {
                pair.setPublicKey(new Ssh2RsaPublicKey((RSAPublicKey)publicKey));
            }
            return pair;
        }
        catch (NoSuchAlgorithmException e) {
            throw new SshException(e);
        }
    }

    @Override
    public SshKeyPair generateEcdsaKeyPair(int bits) throws SshException {
        String curve;
        switch (bits) {
            case 256: {
                curve = "secp256r1";
                break;
            }
            case 384: {
                curve = "secp384r1";
                break;
            }
            case 521: {
                curve = "secp521r1";
                break;
            }
            default: {
                throw new SshException("Unsupported size " + bits + " for ECDSA key (256,384,521 supported)", 4);
            }
        }
        try {
            ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(curve);
            KeyPairGenerator g = JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()) == null ? KeyPairGenerator.getInstance(JCEProvider.getECDSAAlgorithmName()) : KeyPairGenerator.getInstance(JCEProvider.getECDSAAlgorithmName(), JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()));
            g.initialize(ecGenSpec, JCEProvider.getSecureRandom());
            KeyPair pair = g.generateKeyPair();
            SshKeyPair p = new SshKeyPair();
            p.setPrivateKey(new Ssh2EcdsaSha2NistPrivateKey((PrivateKey)((ECPrivateKey)pair.getPrivate()), curve));
            p.setPublicKey(new Ssh2EcdsaSha2NistPublicKey((ECPublicKey)pair.getPublic(), curve));
            return p;
        }
        catch (Exception e) {
            throw new SshException(e);
        }
    }

    @Override
    public SshSecureRandomGenerator getRND() throws SshException {
        try {
            return this.rnd == null ? new SecureRND() : this.rnd;
        }
        catch (NoSuchAlgorithmException e) {
            throw new SshException(e);
        }
    }

    @Override
    protected void initializeDigestFactory(ComponentFactory<Digest> digests) {
        if (this.testDigest("MD5", MD5Digest.class)) {
            digests.add("MD5", MD5Digest.class);
        }
        if (this.testDigest("SHA-1", SHA1Digest.class)) {
            digests.add("SHA-1", SHA1Digest.class);
        }
        if (this.testDigest("SHA1", SHA1Digest.class)) {
            digests.add("SHA1", SHA1Digest.class);
        }
        if (this.testDigest("SHA-256", SHA256Digest.class)) {
            digests.add("SHA-256", SHA256Digest.class);
            digests.add("SHA256", SHA256Digest.class);
        }
        if (this.testDigest("SHA-384", SHA384Digest.class)) {
            digests.add("SHA-384", SHA384Digest.class);
            digests.add("SHA384", SHA384Digest.class);
        }
        if (this.testDigest("SHA-512", SHA512Digest.class)) {
            digests.add("SHA-512", SHA512Digest.class);
            digests.add("SHA512", SHA512Digest.class);
        }
    }

    @Override
    protected void initializeHmacFactory(ComponentFactory<SshHmac> hmacs) {
        if (this.testHMac("hmac-sha2-256", HmacSha256.class)) {
            hmacs.add("hmac-sha2-256", HmacSha256.class);
            if (AdaptiveConfiguration.getBoolean("hmac-sha256@ssh.com", false, new String[0])) {
                hmacs.add("hmac-sha256@ssh.com", HmacSha256_at_ssh_dot_com.class);
            }
            hmacs.add("hmac-sha2-256-etm@openssh.com", HmacSha256ETM.class);
        }
        if (this.testHMac("hmac-sha2-256-96", HmacSha256_96.class)) {
            hmacs.add("hmac-sha2-256-96", HmacSha256_96.class);
        }
        if (this.testHMac("hmac-sha2-512", HmacSha512.class)) {
            hmacs.add("hmac-sha2-512", HmacSha512.class);
            if (AdaptiveConfiguration.getBoolean("hmac-sha256@ssh.com", false, new String[0])) {
                hmacs.add("hmac-sha512@ssh.com", HmacSha512.class);
            }
            hmacs.add("hmac-sha2-512-etm@openssh.com", HmacSha512ETM.class);
        }
        if (this.testHMac("hmac-sha2-512-96", HmacSha512_96.class)) {
            hmacs.add("hmac-sha2-512-96", HmacSha512_96.class);
        }
        if (this.testHMac("hmac-sha1", HmacSha1.class)) {
            hmacs.add("hmac-sha1", HmacSha1.class);
            hmacs.add("hmac-sha1-etm@openssh.com", HmacSha1ETM.class);
        }
        if (this.testHMac("hmac-sha1-96", HmacSha196.class)) {
            hmacs.add("hmac-sha1-96", HmacSha196.class);
        }
        if (this.testHMac("hmac-md5", HmacMD5.class)) {
            hmacs.add("hmac-md5", HmacMD5.class);
            hmacs.add("hmac-md5-etm@openssh.com", HmacMD5ETM.class);
        }
        if (this.testHMac("hmac-md5-96", HmacMD596.class)) {
            hmacs.add("hmac-md5-96", HmacMD596.class);
        }
    }

    @Override
    protected void initializeKeyExchangeFactory(ComponentFactory<SshKeyExchange> clientKeyexchanges, ComponentFactory<SshKeyExchange> serverKeyexchanges) {
        if (loadClientKex) {
            this.testClientKeyExchangeAlgorithm("curve25519-sha256", "com.maverick.ssh.components.jce.client.Curve25519SHA256", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("curve25519-sha256@libssh.org", "com.maverick.ssh.components.jce.client.Curve25519SHA256_at_libssh_dot_org", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("diffie-hellman-group-exchange-sha256", "com.maverick.ssh.components.jce.client.DiffieHellmanGroupExchangeSha256", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("diffie-hellman-group18-sha512", "com.maverick.ssh.components.jce.client.DiffieHellmanGroup18Sha512", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("diffie-hellman-group17-sha512", "com.maverick.ssh.components.jce.client.DiffieHellmanGroup17Sha512", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("diffie-hellman-group16-sha512", "com.maverick.ssh.components.jce.client.DiffieHellmanGroup16Sha512", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("diffie-hellman-group15-sha512", "com.maverick.ssh.components.jce.client.DiffieHellmanGroup15Sha512", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("diffie-hellman-group14-sha256", "com.maverick.ssh.components.jce.client.DiffieHellmanGroup14Sha256", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("diffie-hellman-group-exchange-sha1", "com.maverick.ssh.components.jce.client.DiffieHellmanGroupExchangeSha1", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("diffie-hellman-group14-sha1", "com.maverick.ssh.components.jce.client.DiffieHellmanGroup14Sha1", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("ecdh-sha2-nistp256", "com.maverick.ssh.components.jce.client.DiffieHellmanEcdhNistp256", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("ecdh-sha2-nistp384", "com.maverick.ssh.components.jce.client.DiffieHellmanEcdhNistp384", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("ecdh-sha2-nistp521", "com.maverick.ssh.components.jce.client.DiffieHellmanEcdhNistp521", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("diffie-hellman-group1-sha1", "com.maverick.ssh.components.jce.client.DiffieHellmanGroup1Sha1", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("rsa2048-sha256", "com.maverick.ssh.components.jce.client.Rsa2048Sha256", clientKeyexchanges);
            this.testClientKeyExchangeAlgorithm("rsa1024-sha1", "com.maverick.ssh.components.jce.client.Rsa1024Sha1", clientKeyexchanges);
        }
        if (loadServerKex) {
            this.testServerKeyExchangeAlgorithm("curve25519-sha256", "com.maverick.sshd.components.jce.server.Curve25519SHA256", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("curve25519-sha256@libssh.org", "com.maverick.sshd.components.jce.server.Curve25519SHA256_at_libssh_dot_org", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("diffie-hellman-group-exchange-sha256", "com.maverick.sshd.components.jce.server.DiffieHellmanGroupExchangeSha256JCE", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("diffie-hellman-group18-sha512", "com.maverick.sshd.components.jce.server.DiffieHellmanGroup18Sha512JCE", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("diffie-hellman-group17-sha512", "com.maverick.sshd.components.jce.server.DiffieHellmanGroup17Sha512JCE", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("diffie-hellman-group16-sha512", "com.maverick.sshd.components.jce.server.DiffieHellmanGroup16Sha512JCE", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("diffie-hellman-group15-sha512", "com.maverick.sshd.components.jce.server.DiffieHellmanGroup15Sha512JCE", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("diffie-hellman-group14-sha256", "com.maverick.sshd.components.jce.server.DiffieHellmanGroup14Sha256JCE", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("diffie-hellman-group14-sha1", "com.maverick.sshd.components.jce.server.DiffieHellmanGroup14Sha1JCE", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("ecdh-sha2-nistp256", "com.maverick.sshd.components.jce.server.DiffieHellmanEcdhNistp256", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("ecdh-sha2-nistp384", "com.maverick.sshd.components.jce.server.DiffieHellmanEcdhNistp384", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("ecdh-sha2-nistp521", "com.maverick.sshd.components.jce.server.DiffieHellmanEcdhNistp521", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("diffie-hellman-group-exchange-sha1", "com.maverick.sshd.components.jce.server.DiffieHellmanGroupExchangeSha1JCE", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("diffie-hellman-group1-sha1", "com.maverick.sshd.components.jce.server.DiffieHellmanGroup1Sha1JCE", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("rsa2048-sha256", "com.maverick.sshd.components.jce.server.Rsa2048SHA2KeyExchange", serverKeyexchanges);
            this.testServerKeyExchangeAlgorithm("rsa1024-sha1", "com.maverick.sshd.components.jce.server.Rsa1024SHA1KeyExchange", serverKeyexchanges);
        }
    }

    @Override
    protected void initializePublicKeyFactory(ComponentFactory<SshPublicKey> publickeys) {
        this.testPublicKey("ssh-dss", Ssh2DsaPublicKey.class, publickeys);
        this.testPublicKey("ssh-rsa", Ssh2RsaPublicKey.class, publickeys);
        this.testPublicKey("rsa-sha2-256", Ssh2RsaPublicKeySHA256.class, publickeys);
        this.testPublicKey("rsa-sha2-512", Ssh2RsaPublicKeySHA512.class, publickeys);
        this.testPublicKey("x509v3-sign-rsa", SshX509RsaPublicKey.class, publickeys);
        this.testPublicKey("x509v3-sign-dss", SshX509DsaPublicKey.class, publickeys);
        this.testPublicKey("x509v3-sign-rsa-sha1", SshX509RsaSha1PublicKey.class, publickeys);
        this.testPublicKey("x509v3-ssh-rsa", SshX509RsaPublicKeyRfc6187.class, publickeys);
        this.testPublicKey("x509v3-ssh-dss", SshX509DsaPublicKeyRfc6187.class, publickeys);
        this.testPublicKey("x509v3-ecdsa-sha2-nistp256", SshX509EcdsaSha2Nist256Rfc6187.class, publickeys);
        this.testPublicKey("x509v3-ecdsa-sha2-nistp384", SshX509EcdsaSha2Nist384Rfc6187.class, publickeys);
        this.testPublicKey("x509v3-ecdsa-sha2-nistp521", SshX509EcdsaSha2Nist521Rfc6187.class, publickeys);
        this.testPublicKey("ecdsa-sha2-nistp256", Ssh2EcdsaSha2Nist256PublicKey.class, publickeys);
        this.testPublicKey("ecdsa-sha2-nistp384", Ssh2EcdsaSha2Nist384PublicKey.class, publickeys);
        this.testPublicKey("ecdsa-sha2-nistp521", Ssh2EcdsaSha2Nist521PublicKey.class, publickeys);
        this.testPublicKey("x509v3-rsa2048-sha256", SshX509Rsa2048Sha256Rfc6187.class, publickeys);
        this.testPublicKey("ssh-rsa-cert-v01@openssh.com", OpenSshRsaCertificate.class, publickeys);
        this.testPublicKey("rsa-sha2-256-cert-v01@openssh.com", OpenSshRsaSha256Certificate.class, publickeys);
        this.testPublicKey("rsa-sha2-512-cert-v01@openssh.com", OpenSshRsaSha512Certificate.class, publickeys);
        this.testPublicKey("ssh-dss-cert-v01@openssh.com", OpenSshDsaCertificate.class, publickeys);
        this.testPublicKey("ssh-ed25519-cert-v01@openssh.com", OpenSshEd25519Certificate.class, publickeys);
        this.testPublicKey("ecdsa-sha2-nistp256-cert-v01@openssh.com", OpenSshEcdsaSha2Nist256Certificate.class, publickeys);
        this.testPublicKey("ecdsa-sha2-nistp384-cert-v01@openssh.com", OpenSshEcdsaSha2Nist384Certificate.class, publickeys);
        this.testPublicKey("ecdsa-sha2-nistp521-cert-v01@openssh.com", OpenSshEcdsaSha2Nist521Certificate.class, publickeys);
        this.testPublicKey("ssh-ed25519", "com.maverick.ssh.components.jce.SshEd25519PublicKeyJCE", publickeys);
        this.testPublicKey("ssh-ed448", "com.maverick.ssh.components.jce.SshEd448PublicKeyJCE", publickeys);
        this.testPublicKey("rsa-sha2-256-cert-v01@openssh.com", "com.maverick.ssh.components.jce.OpenSshRsaSha256Certificate", publickeys);
        this.testPublicKey("rsa-sha2-512-cert-v01@openssh.com", "com.maverick.ssh.components.jce.OpenSshRsaSha512Certificate", publickeys);
    }

    private boolean testPublicKey(String name, String clzName, ComponentFactory<SshPublicKey> publickeys) {
        if (System.getProperties().containsKey(String.format("disable.%s", name))) {
            if (log.isInfoEnabled()) {
                log.info(String.format("   %s WILL NOT be supported because it has been explicitly disabled by a system property", name));
            }
            return false;
        }
        try {
            Class<?> cls = Class.forName(clzName);
            return this.testPublicKey(name, cls, publickeys);
        }
        catch (Throwable e) {
            if (log.isInfoEnabled()) {
                log.info("   " + name + " will not be supported: " + e.getMessage());
            }
            return false;
        }
    }

    private boolean testPublicKey(String name, Class<? extends SshPublicKey> pub, ComponentFactory<SshPublicKey> publickeys) {
        if (System.getProperties().containsKey(String.format("disable.%s", name))) {
            if (log.isInfoEnabled()) {
                log.info(String.format("   %s WILL NOT be supported because it has been explicitly disabled by a system property", name));
            }
            return false;
        }
        if (disableTests) {
            publickeys.add(name, pub);
            return true;
        }
        try {
            SshPublicKey key = pub.newInstance();
            String provider = key.test();
            if (log.isInfoEnabled()) {
                log.info("   " + name + " will be supported using JCE Provider " + provider);
            }
            publickeys.add(name, pub);
            return true;
        }
        catch (Throwable e) {
            if (log.isInfoEnabled()) {
                log.info("   " + name + " will not be supported: " + e.getMessage());
            }
            return false;
        }
    }

    @Override
    protected void initializeSsh1CipherFactory(ComponentFactory<SshCipher> ciphers) {
        if (this.testJCECipher("ssh1-des", Ssh1Des.class)) {
            ciphers.add("2", Ssh1Des.class);
        }
        if (this.testJCECipher("ssh1-3des", Ssh1Des3.class)) {
            ciphers.add("3", Ssh1Des3.class);
        }
    }

    @Override
    protected void initializeSsh2CipherFactory(ComponentFactory<SshCipher> ciphers) {
        if (this.testJCECipher("chacha20-poly1305@openssh.com", ChaCha20Poly1305.class)) {
            ciphers.add("chacha20-poly1305@openssh.com", ChaCha20Poly1305.class);
        }
        if (this.testJCECipher("aes128-ctr", AES128Ctr.class)) {
            ciphers.add("aes128-ctr", AES128Ctr.class);
        }
        if (this.testJCECipher("aes192-ctr", AES192Ctr.class)) {
            ciphers.add("aes192-ctr", AES192Ctr.class);
        }
        if (this.testJCECipher("aes256-ctr", AES256Ctr.class)) {
            ciphers.add("aes256-ctr", AES256Ctr.class);
        }
        if (this.testJCECipher("3des-ctr", TripleDesCtr.class)) {
            ciphers.add("3des-ctr", TripleDesCtr.class);
        }
        if (this.testJCECipher("3des-cbc", TripleDesCbc.class)) {
            ciphers.add("3des-cbc", TripleDesCbc.class);
        }
        if (this.testJCECipher("blowfish-cbc", BlowfishCbc.class)) {
            ciphers.add("blowfish-cbc", BlowfishCbc.class);
        }
        if (this.testJCECipher("aes128-cbc", AES128Cbc.class)) {
            ciphers.add("aes128-cbc", AES128Cbc.class);
        }
        if (this.testJCECipher("aes192-cbc", AES192Cbc.class)) {
            ciphers.add("aes192-cbc", AES192Cbc.class);
        }
        if (this.testJCECipher("aes256-cbc", AES256Cbc.class)) {
            ciphers.add("aes256-cbc", AES256Cbc.class);
        }
        if (this.testJCECipher("arcfour", ArcFour.class)) {
            ciphers.add("arcfour", ArcFour.class);
        }
        if (this.testJCECipher("arcfour128", ArcFour128.class)) {
            ciphers.add("arcfour128", ArcFour128.class);
        }
        if (this.testJCECipher("arcfour256", ArcFour256.class)) {
            ciphers.add("arcfour256", ArcFour256.class);
        }
        if (this.testJCECipher("aes128-gcm@openssh.com", AES128Gcm.class)) {
            ciphers.add("aes128-gcm@openssh.com", AES128Gcm.class);
        }
        if (this.testJCECipher("aes256-gcm@openssh.com", AES256Gcm.class)) {
            ciphers.add("aes256-gcm@openssh.com", AES256Gcm.class);
        }
    }

    @Override
    public SshKeyPair[] loadKeystore(InputStream in, String alias, String storePassphrase, String keyPassphrase) throws IOException {
        return this.loadKeystore(in, alias, storePassphrase, keyPassphrase, "PKCS12");
    }

    @Override
    public SshKeyPair[] loadKeystore(InputStream in, String alias, String storePassphrase, String keyPassphrase, String storeType) throws IOException {
        try {
            KeyStore keystore = KeyStore.getInstance(storeType);
            keystore.load(in, storePassphrase.toCharArray());
            Key prv = keystore.getKey(alias, keyPassphrase.toCharArray());
            X509Certificate x509 = (X509Certificate)keystore.getCertificate(alias);
            Certificate[] chain = keystore.getCertificateChain(alias);
            String algorithm = prv.getAlgorithm();
            SshKeyPair pair = new SshKeyPair();
            if (algorithm.equals("RSA")) {
                if (x509.getSigAlgName().equalsIgnoreCase("SHA1WithRSA")) {
                    pair.setPublicKey(new SshX509RsaSha1PublicKey(x509));
                    pair.setPrivateKey(new Ssh2RsaPrivateKey((RSAPrivateKey)prv));
                    SshKeyPair pair2 = new SshKeyPair();
                    pair2.setPublicKey(new SshX509RsaPublicKey(x509));
                    pair2.setPrivateKey(new Ssh2RsaPrivateKey((RSAPrivateKey)prv));
                    SshKeyPair pair3 = new SshKeyPair();
                    pair3.setPublicKey(new SshX509RsaPublicKeyRfc6187(chain));
                    pair3.setPrivateKey(new Ssh2RsaPrivateKey((RSAPrivateKey)prv));
                    return new SshKeyPair[]{pair, pair2, pair3};
                }
                if (x509.getSigAlgName().equalsIgnoreCase("SHA256WithRSA") && ((RSAPublicKey)x509.getPublicKey()).getModulus().bitLength() >= 2048) {
                    pair.setPublicKey(new SshX509Rsa2048Sha256Rfc6187(chain));
                    pair.setPrivateKey(new Ssh2RsaPrivateKey((RSAPrivateKey)prv));
                    if (AdaptiveConfiguration.getBoolean("backwardCompatibleSHA2", false, new String[0])) {
                        SshKeyPair pair2 = new SshKeyPair();
                        pair2.setPublicKey(new SshX509RsaPublicKey(x509));
                        pair2.setPrivateKey(new Ssh2RsaPrivateKey((RSAPrivateKey)prv));
                        return new SshKeyPair[]{pair, pair2};
                    }
                    return new SshKeyPair[]{pair};
                }
            } else {
                if (algorithm.equals("DSA")) {
                    pair.setPublicKey(new SshX509DsaPublicKey(x509));
                    pair.setPrivateKey(new Ssh2DsaPrivateKey((DSAPrivateKey)prv, (DSAPublicKey)x509.getPublicKey()));
                    SshKeyPair pair2 = new SshKeyPair();
                    pair2.setPublicKey(new SshX509DsaPublicKeyRfc6187(chain));
                    pair2.setPrivateKey(new Ssh2DsaPrivateKey((DSAPrivateKey)prv));
                    return new SshKeyPair[]{pair, pair2};
                }
                if (algorithm.equals("EC") && x509.getSigAlgName().equals("SHA256withECDSA")) {
                    String curve = ECUtils.getNameFromEncodedKey((PrivateKey)prv);
                    pair.setPublicKey(new Ssh2EcdsaSha2NistPublicKey((ECPublicKey)x509.getPublicKey(), curve));
                    switch (curve) {
                        case "secp256r1": {
                            pair.setPublicKey(new SshX509EcdsaSha2Nist256Rfc6187(chain));
                            break;
                        }
                        case "secp384r1": {
                            pair.setPublicKey(new SshX509EcdsaSha2Nist384Rfc6187(chain));
                            break;
                        }
                        case "secp521r1": {
                            pair.setPublicKey(new SshX509EcdsaSha2Nist521Rfc6187(chain));
                        }
                    }
                    pair.setPrivateKey(new Ssh2EcdsaSha2NistPrivateKey((PrivateKey)((ECPrivateKey)prv), curve));
                    return new SshKeyPair[]{pair};
                }
            }
            throw new IOException(algorithm + " is an unsupported certificate type");
        }
        catch (Throwable ex) {
            throw new IOException("Could not load keystore from stream: " + ex.getMessage());
        }
    }

    @Override
    public SshKeyPair[] loadKeystore(File keystoreFile, String alias, String storePassphrase, String keyPassphrase) throws IOException {
        return this.loadKeystore(keystoreFile, alias, storePassphrase, keyPassphrase, "PKCS12");
    }

    @Override
    public SshKeyPair[] loadKeystore(File keystoreFile, String alias, String storePassphrase, String keyPassphrase, String storeType) throws IOException {
        return this.loadKeystore(new FileInputStream(keystoreFile), alias, storePassphrase, keyPassphrase, storeType);
    }

    private boolean testDigest(String name) {
        return this.supportedDigests().contains(name);
    }

    private boolean testClientKeyExchangeAlgorithm(String name, String clzName, ComponentFactory<SshKeyExchange> clientKeyexchanges) {
        if (System.getProperties().containsKey(String.format("disable.%s", name))) {
            if (log.isInfoEnabled()) {
                log.info(String.format("   %s WILL NOT be supported because it has been explicitly disabled by a system property", name));
            }
            return false;
        }
        SshKeyExchange c = null;
        try {
            Class<?> cls = Class.forName(clzName);
            if (disableTests) {
                clientKeyexchanges.add(name, cls);
                return true;
            }
            c = (SshKeyExchange)cls.newInstance();
            if (!this.testDigest(c.getHashAlgorithm())) {
                throw new Exception("Hash algorithm " + c.getHashAlgorithm() + " is not supported");
            }
            c.test();
            clientKeyexchanges.add(name, cls);
        }
        catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("   " + name + " (client) will not be supported: " + e.getMessage());
            }
            return false;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (log.isInfoEnabled()) {
            log.info("   " + name + " (client) will be supported using Provider " + c.getProvider());
        }
        return true;
    }

    private boolean testServerKeyExchangeAlgorithm(String name, String clsName, ComponentFactory<SshKeyExchange> serverKeyexchanges) {
        if (System.getProperties().containsKey(String.format("disable.%s", name))) {
            if (log.isInfoEnabled()) {
                log.info(String.format("   %s WILL NOT be supported because it has been explicitly disabled by a system property", name));
            }
            return false;
        }
        SshKeyExchange c = null;
        try {
            Class<?> cls = Class.forName(clsName);
            if (disableTests) {
                serverKeyexchanges.add(name, cls);
                return true;
            }
            c = (SshKeyExchange)cls.newInstance();
            if (!this.testDigest(c.getHashAlgorithm())) {
                throw new Exception("Hash algorithm " + c.getHashAlgorithm() + " is not supported");
            }
            c.test();
            serverKeyexchanges.add(name, cls);
        }
        catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("   " + name + " (server) will not be supported: " + e.getMessage());
            }
            return false;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (log.isInfoEnabled()) {
            log.info("   " + name + " (server) will be supported using Provider " + c.getProvider());
        }
        return true;
    }

    private boolean testJCECipher(String name, Class<? extends SshCipher> cls) {
        if (System.getProperties().containsKey(String.format("disable.%s", name))) {
            if (log.isInfoEnabled()) {
                log.info(String.format("   %s WILL NOT be supported because it has been explicitly disabled by a system property", name));
            }
            return false;
        }
        if (disableTests) {
            return true;
        }
        SshCipher c = null;
        try {
            c = cls.newInstance();
            byte[] tmp = new byte[1024];
            JCEComponentManager.getSecureRandom().nextBytes(tmp);
            c.init(0, tmp, tmp);
            if (log.isInfoEnabled()) {
                log.info("   " + name + " will be supported using Provider " + c.getProviderName());
            }
            return true;
        }
        catch (Throwable e) {
            if (log.isInfoEnabled()) {
                log.info("   " + name + " WILL NOT be supported: " + e.getMessage());
            }
            return false;
        }
    }

    private boolean testDigest(String name, Class<? extends Digest> cls) {
        if (System.getProperties().containsKey(String.format("disable.%s", name))) {
            if (log.isInfoEnabled()) {
                log.info(String.format("   %s WILL NOT be supported because it has been explicitly disabled by a system property", name));
            }
            return false;
        }
        if (disableTests) {
            return true;
        }
        Digest c = null;
        try {
            c = cls.newInstance();
            if (log.isInfoEnabled()) {
                log.info("   " + name + " will be supported using Provider " + ((AbstractDigest)c).getProvider());
            }
            return true;
        }
        catch (Throwable e) {
            if (log.isInfoEnabled()) {
                if (c != null && ((AbstractDigest)c).getProvider() != null) {
                    log.info("   " + name + " WILL NOT be supported from Provider " + ((AbstractDigest)c).getProvider() + ": " + e.getMessage());
                } else {
                    log.info("   " + name + " WILL NOT be supported: " + e.getMessage());
                }
            }
            return false;
        }
    }

    private boolean testHMac(String name, Class<? extends SshHmac> cls) {
        if (System.getProperties().containsKey(String.format("disable.%s", name))) {
            if (log.isInfoEnabled()) {
                log.info(String.format("   %s WILL NOT be supported because it has been explicitly disabled by a system property", name));
            }
            return false;
        }
        if (disableTests) {
            return true;
        }
        SshHmac c = null;
        try {
            c = cls.newInstance();
            byte[] tmp = new byte[1024];
            c.init(tmp);
            if (c instanceof AbstractHmac && log.isInfoEnabled()) {
                log.info("   " + name + " will be supported using Provider " + ((AbstractHmac)c).getProvider());
            }
            return true;
        }
        catch (Throwable e) {
            if (log.isInfoEnabled()) {
                log.info("   " + name + " WILL NOT be supported: " + e.getMessage());
            }
            return false;
        }
    }

    @Override
    public SshKeyPair generateEd25519KeyPair() throws SshException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair kp = keyGen.generateKeyPair();
            SshKeyPair pair = new SshKeyPair();
            pair.setPrivateKey(new SshEd25519PrivateKeyJCE(kp.getPrivate()));
            pair.setPublicKey(new SshEd25519PublicKeyJCE(kp.getPublic()));
            return pair;
        }
        catch (NoSuchAlgorithmException e) {
            if (log.isErrorEnabled()) {
                log.error("ed25519 keys are not supported with the current configuration", (Throwable)e);
            }
            throw new SshException("ed25519 keys are not supported with the current configuration", 4);
        }
    }

    @Override
    public SshKeyPair generateEd448KeyPair() throws SshException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Ed448");
            KeyPair kp = keyGen.generateKeyPair();
            SshKeyPair pair = new SshKeyPair();
            pair.setPrivateKey(new SshEd448PrivateKeyJCE(kp.getPrivate()));
            pair.setPublicKey(new SshEd448PublicKeyJCE(kp.getPublic()));
            return pair;
        }
        catch (NoSuchAlgorithmException e) {
            if (log.isErrorEnabled()) {
                log.error("ed448 keys are not supported with the current configuration", (Throwable)e);
            }
            throw new SshException("ed448 keys are not supported with the current configuration", 4);
        }
    }
}

