/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.JCEAlgorithms;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.util.Hashtable;
import javax.crypto.KeyAgreement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCEProvider
implements JCEAlgorithms {
    static Logger log = LoggerFactory.getLogger(JCEProvider.class);
    static Provider defaultProvider = null;
    static Provider bcProvider = null;
    static Hashtable<String, Provider> specficProviders = new Hashtable();
    static String secureRandomAlgorithm = null;
    static Boolean bcEnabled = null;
    static String ecdsaAlgorithmName = "EC";
    static boolean enableSC = false;
    static SecureRandom secureRandom;

    public static void initializeDefaultProvider(Provider provider) {
        defaultProvider = provider;
    }

    public static void initializeDefaultProvider(String name) throws NoSuchProviderException {
        Provider provider = Security.getProvider(name);
        if (provider == null) {
            throw new NoSuchProviderException();
        }
        JCEProvider.initializeDefaultProvider(provider);
    }

    public static void initializeProviderForAlgorithm(String jceAlgorithm, Provider provider) {
        specficProviders.put(jceAlgorithm, provider);
    }

    public static void initializeProviderForAlgorithm(String jceAlgorithm, String name) throws NoSuchProviderException {
        Provider provider = Security.getProvider(name);
        if (provider == null) {
            throw new NoSuchProviderException();
        }
        JCEProvider.initializeProviderForAlgorithm(jceAlgorithm, provider);
    }

    public static String getSecureRandomAlgorithm() {
        return secureRandomAlgorithm;
    }

    public static void setSecureRandomAlgorithm(String secureRandomAlgorithm) {
        JCEProvider.secureRandomAlgorithm = secureRandomAlgorithm;
    }

    public static Provider getProviderForAlgorithm(String jceAlgorithm) {
        if (specficProviders.containsKey(jceAlgorithm)) {
            return specficProviders.get(jceAlgorithm);
        }
        return defaultProvider;
    }

    public static SecureRandom getSecureRandom() {
        if (secureRandom == null) {
            try {
                if (JCEProvider.getSecureRandomAlgorithm() != null) {
                    secureRandom = JCEProvider.getProviderForAlgorithm(JCEProvider.getSecureRandomAlgorithm()) == null ? SecureRandom.getInstance(JCEProvider.getSecureRandomAlgorithm()) : SecureRandom.getInstance(JCEProvider.getSecureRandomAlgorithm(), JCEProvider.getProviderForAlgorithm(JCEProvider.getSecureRandomAlgorithm()));
                    return secureRandom;
                }
                secureRandom = new SecureRandom();
            }
            catch (NoSuchAlgorithmException e) {
                secureRandom = new SecureRandom();
                return secureRandom;
            }
        }
        return secureRandom;
    }

    public static Provider getDefaultProvider() {
        return defaultProvider;
    }

    static void setBCProvider(Provider provider) {
        bcProvider = provider;
    }

    public static boolean hasBCProvider() {
        return bcProvider != null && bcEnabled != null && bcEnabled != false;
    }

    public static Provider getBCProvider() {
        if (bcProvider == null) {
            JCEProvider.configureBC();
        }
        return bcProvider;
    }

    public static void enableSpongyCastle(boolean makeDefault) {
        enableSC = true;
        JCEProvider.enableBouncyCastle(makeDefault);
    }

    public static void enableBouncyCastle(boolean makeDefault) {
        BC_FLAVOR bcFlavor = JCEProvider.configureBC();
        if (bcProvider == null) {
            throw new IllegalStateException("Bouncycastle JCE provider cannot be found on the classpath");
        }
        JCEProvider.enableBouncyCastle(makeDefault, bcFlavor, bcProvider);
    }

    public static void enableBouncyCastle(boolean makeDefault, BC_FLAVOR bcFlavor, Provider provider) {
        bcEnabled = true;
        boolean add = true;
        for (Provider p : Security.getProviders()) {
            if (!p.getName().equals(provider.getName())) continue;
            add = false;
            break;
        }
        if (add) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Adding Bouncycastle %s provider to Security Providers", provider.getName()));
            }
            if (bcFlavor == BC_FLAVOR.SC) {
                Security.insertProviderAt(provider, 1);
            } else {
                Security.addProvider(provider);
            }
        }
        if (!bcFlavor.equals((Object)BC_FLAVOR.SC)) {
            JCEProvider.setECDSAAlgorithmName("ECDSA");
        }
        if (makeDefault) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Configuring Bouncycastle %s provider as default for all algorithms", provider.getName()));
            }
            JCEProvider.initializeDefaultProvider(provider);
        } else {
            if (log.isInfoEnabled()) {
                log.info(String.format("Configuring DH support with Bouncycastle %s provider", provider.getName()));
            }
            JCEProvider.initializeProviderForAlgorithm("DH", bcProvider);
            JCEProvider.initializeProviderForAlgorithm("DH_KeyAgreement", bcProvider);
            JCEProvider.initializeProviderForAlgorithm("DH_KeyFactory", bcProvider);
            JCEProvider.initializeProviderForAlgorithm("DH_KeyGenerator", bcProvider);
        }
        bcProvider = provider;
    }

    private static BC_FLAVOR configureBC() {
        try {
            if (enableSC) {
                Class<?> cls = Class.forName("org.spongycastle.jce.provider.BouncyCastleProvider");
                bcProvider = (Provider)cls.newInstance();
                return BC_FLAVOR.SC;
            }
        }
        catch (Throwable cls) {
            // empty catch block
        }
        try {
            Class<?> cls = Class.forName("org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider");
            bcProvider = (Provider)cls.newInstance();
            return BC_FLAVOR.BCFIPS;
        }
        catch (Throwable t) {
            try {
                Class<?> cls = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                bcProvider = (Provider)cls.newInstance();
                return BC_FLAVOR.BC;
            }
            catch (Throwable f) {
                throw new IllegalStateException("Bouncycastle, BCFIPS or SpongyCastle is not installed");
            }
        }
    }

    public static KeyFactory getDHKeyFactory() throws NoSuchAlgorithmException {
        try {
            if (JCEProvider.getProviderForAlgorithm("DH_KeyFactory") == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Getting key factory algorithm {} from default provider", (Object)"DH");
                }
                return KeyFactory.getInstance("DH");
            }
            if (log.isTraceEnabled()) {
                log.trace("Getting key factory algorithm {} from provider {}", (Object)"DH", (Object)JCEProvider.getProviderForAlgorithm("DH_KeyFactory").getName());
            }
            return KeyFactory.getInstance("DH", JCEProvider.getProviderForAlgorithm("DH_KeyFactory"));
        }
        catch (NoSuchAlgorithmException e) {
            if (log.isTraceEnabled()) {
                log.trace("Failed to find previous algorithm/provider combination", (Throwable)e);
                log.trace("Getting key factory algorithm {} from default provider", (Object)"DH");
            }
            return KeyFactory.getInstance("DH");
        }
    }

    public static KeyAgreement getDHKeyAgreement() throws NoSuchAlgorithmException {
        try {
            if (JCEProvider.getProviderForAlgorithm("DH_KeyAgreement") == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Getting key agreement algorithm {} from default provider", (Object)"DH");
                }
                return KeyAgreement.getInstance("DH");
            }
            if (log.isTraceEnabled()) {
                log.trace("Getting key agreement algorithm {} from provider {}", (Object)"DH", (Object)JCEProvider.getProviderForAlgorithm("DH_KeyAgreement").getName());
            }
            return KeyAgreement.getInstance("DH", JCEProvider.getProviderForAlgorithm("DH_KeyAgreement"));
        }
        catch (NoSuchAlgorithmException e) {
            if (log.isTraceEnabled()) {
                log.trace("Failed to find previous algorithm/provider combination", (Throwable)e);
                log.trace("Getting key agreement algorithm {} from default provider", (Object)"DH");
            }
            return KeyAgreement.getInstance("DH");
        }
    }

    public static KeyPairGenerator getDHKeyGenerator() throws NoSuchAlgorithmException {
        try {
            if (JCEProvider.getProviderForAlgorithm("DH_KeyGenerator") == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Getting key generator algorithm {} from default provider", (Object)"DH");
                }
                return KeyPairGenerator.getInstance("DH");
            }
            if (log.isTraceEnabled()) {
                log.trace("Getting key generator algorithm {} from provider {}", (Object)"DH", (Object)JCEProvider.getProviderForAlgorithm("DH_KeyGenerator").getName());
            }
            return KeyPairGenerator.getInstance("DH", JCEProvider.getProviderForAlgorithm("DH_KeyGenerator"));
        }
        catch (NoSuchAlgorithmException e) {
            if (log.isDebugEnabled()) {
                log.trace("Failed to find previous algorithm/provider combination", (Throwable)e);
                log.trace("Getting key generator algorithm {} from default provider", (Object)"DH");
            }
            return KeyPairGenerator.getInstance("DH");
        }
    }

    public static void disableBouncyCastle() {
        JCEProvider.setECDSAAlgorithmName("EC");
        if (JCEProvider.isBCEnabled()) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Disabling support for Bouncycastle %s provider", bcProvider.getName()));
            }
            Security.removeProvider(bcProvider.getName());
            JCEProvider.initializeDefaultProvider((Provider)null);
            specficProviders.remove("DH");
        }
        bcEnabled = false;
    }

    public static boolean isBCEnabled() {
        if (bcProvider == null) {
            return false;
        }
        return bcProvider != null && bcEnabled != null && bcEnabled != false;
    }

    public static String getECDSAAlgorithmName() {
        return ecdsaAlgorithmName;
    }

    public static void setECDSAAlgorithmName(String ecdsaAlgorithmName) {
        JCEProvider.ecdsaAlgorithmName = ecdsaAlgorithmName;
    }

    public static boolean isBCDisabled() {
        return bcEnabled != null && bcEnabled == false;
    }

    public static KeyFactory getKeyFactory(String alg) throws NoSuchAlgorithmException {
        if (JCEProvider.getProviderForAlgorithm(alg) == null) {
            if (log.isTraceEnabled()) {
                log.trace("Getting key factory algorithm {} from default provider", (Object)alg);
            }
            return KeyFactory.getInstance(alg);
        }
        if (log.isTraceEnabled()) {
            log.trace("Getting key factory algorithm {} from provider {}", (Object)alg, (Object)JCEProvider.getProviderForAlgorithm(alg));
        }
        return KeyFactory.getInstance(alg, JCEProvider.getProviderForAlgorithm(alg));
    }

    public static Signature getSignature(String alg) throws NoSuchAlgorithmException {
        if (JCEProvider.getProviderForAlgorithm(alg) == null) {
            if (log.isTraceEnabled()) {
                log.trace("Getting signature algorithm {} from default provider", (Object)alg);
            }
            return Signature.getInstance(alg);
        }
        if (log.isTraceEnabled()) {
            log.trace("Getting signature algorithm {} from provider {}", (Object)alg, (Object)JCEProvider.getProviderForAlgorithm(alg));
        }
        return Signature.getInstance(alg, JCEProvider.getProviderForAlgorithm(alg));
    }

    public static enum BC_FLAVOR {
        BC,
        BCFIPS,
        SC;

    }
}

