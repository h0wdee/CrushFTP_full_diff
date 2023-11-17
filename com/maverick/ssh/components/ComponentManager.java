/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentFactory;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.NoneCipher;
import com.maverick.ssh.components.NoneHmac;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComponentManager {
    static Logger log = LoggerFactory.getLogger(ComponentManager.class);
    private static boolean perContextAlgorithmPreferences = true;
    private static boolean enableNoneCipher = false;
    private static boolean enableNoneMac = false;
    Set<String> disabledAlgorithms = new HashSet<String>();
    SecurityLevel securityLevel = SecurityLevel.WEAK;
    private static ComponentManager instance;
    ComponentFactory<SshCipher> ssh1ciphersSC;
    ComponentFactory<SshCipher> ssh2ciphersSC;
    ComponentFactory<SshCipher> ssh1ciphersCS;
    ComponentFactory<SshCipher> ssh2ciphersCS;
    ComponentFactory<SshHmac> hmacsCS;
    ComponentFactory<SshHmac> hmacsSC;
    ComponentFactory<SshKeyExchange> clientKeyexchanges;
    ComponentFactory<SshKeyExchange> serverKeyexchanges;
    ComponentFactory<SshPublicKey> publickeys;
    ComponentFactory<Digest> digests;
    static Object lock;

    public void disableAlgorithm(String algorithm) {
        this.disabledAlgorithms.add(algorithm);
    }

    public boolean isDisabled(String algorithm) {
        return this.disabledAlgorithms.contains(algorithm);
    }

    public void enableAlgorithm(String algorithm) {
        this.disabledAlgorithms.remove(algorithm);
    }

    public static boolean isEnableNoneCipher() {
        return enableNoneCipher;
    }

    public static void setEnableNoneCipher(boolean enableNoneCipher) {
        ComponentManager.enableNoneCipher = enableNoneCipher;
    }

    public static boolean isEnableNoneMac() {
        return enableNoneMac;
    }

    public static void setEnableNoneMac(boolean enableNoneCipher) {
        enableNoneMac = enableNoneCipher;
    }

    @Deprecated
    public static void setPerContextAlgorithmPreferences(boolean enable) {
        perContextAlgorithmPreferences = enable;
    }

    public static boolean getPerContextAlgorithmPreferences() {
        return perContextAlgorithmPreferences;
    }

    public static ComponentManager getInstance() {
        Object object = lock;
        synchronized (object) {
            if (instance != null) {
                return instance;
            }
            try {
                Class<?> cls = Class.forName("com.maverick.ssh.components.jce.JCEComponentManager");
                instance = (ComponentManager)cls.newInstance();
                instance.init();
                return instance;
            }
            catch (Throwable e) {
                throw new RuntimeException("Unable to locate a cryptographic provider", e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void reset() {
        Object object = lock;
        synchronized (object) {
            instance = null;
            ComponentManager.getInstance();
        }
    }

    protected void init() throws SshException {
        if (log.isInfoEnabled()) {
            log.info("Initializing SSH1 server->client ciphers");
        }
        this.ssh1ciphersSC = new ComponentFactory(this);
        this.initializeSsh1CipherFactory(this.ssh1ciphersSC);
        if (log.isInfoEnabled()) {
            log.info("Initializing SSH1 client-server ciphers");
        }
        this.ssh1ciphersCS = new ComponentFactory(this);
        this.initializeSsh1CipherFactory(this.ssh1ciphersCS);
        if (log.isInfoEnabled()) {
            log.info("Initializing SSH2 server->client ciphers");
        }
        this.ssh2ciphersSC = new ComponentFactory(this);
        this.initializeSsh2CipherFactory(this.ssh2ciphersSC);
        if (enableNoneCipher) {
            this.ssh2ciphersSC.add("none", NoneCipher.class);
            if (log.isInfoEnabled()) {
                log.info("   none will be a supported cipher");
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Initializing SSH2 client->server ciphers");
        }
        this.ssh2ciphersCS = new ComponentFactory(this);
        this.initializeSsh2CipherFactory(this.ssh2ciphersCS);
        if (enableNoneCipher) {
            this.ssh2ciphersCS.add("none", NoneCipher.class);
            if (log.isInfoEnabled()) {
                log.info("   none will be a supported cipher");
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Initializing SSH2 server->client HMACs");
        }
        this.hmacsSC = new ComponentFactory(this);
        this.initializeHmacFactory(this.hmacsSC);
        if (enableNoneMac) {
            this.hmacsSC.add("none", NoneHmac.class);
            if (log.isInfoEnabled()) {
                log.info("   none will be a supported hmac");
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Initializing SSH2 client->server HMACs");
        }
        this.hmacsCS = new ComponentFactory(this);
        this.initializeHmacFactory(this.hmacsCS);
        if (enableNoneMac) {
            this.hmacsCS.add("none", NoneHmac.class);
            if (log.isInfoEnabled()) {
                log.info("   none will be a supported hmac");
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Initializing public keys");
        }
        this.publickeys = new ComponentFactory(this);
        this.initializePublicKeyFactory(this.publickeys);
        if (log.isInfoEnabled()) {
            log.info("Initializing digests");
        }
        this.digests = new ComponentFactory(this);
        this.initializeDigestFactory(this.digests);
        if (log.isInfoEnabled()) {
            log.info("Initializing SSH2 key exchanges");
        }
        this.clientKeyexchanges = new ComponentFactory(this);
        this.serverKeyexchanges = new ComponentFactory(this);
        this.initializeKeyExchangeFactory(this.clientKeyexchanges, this.serverKeyexchanges);
        if (log.isInfoEnabled()) {
            log.info("Initializing Secure Random Number Generator");
        }
        this.getRND().nextInt();
    }

    protected abstract void initializeSsh1CipherFactory(ComponentFactory<SshCipher> var1);

    protected abstract void initializeSsh2CipherFactory(ComponentFactory<SshCipher> var1);

    protected abstract void initializeHmacFactory(ComponentFactory<SshHmac> var1);

    protected abstract void initializePublicKeyFactory(ComponentFactory<SshPublicKey> var1);

    protected abstract void initializeKeyExchangeFactory(ComponentFactory<SshKeyExchange> var1, ComponentFactory<SshKeyExchange> var2);

    protected abstract void initializeDigestFactory(ComponentFactory<Digest> var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setInstance(ComponentManager instance) {
        Object object = lock;
        synchronized (object) {
            ComponentManager.instance = instance;
        }
    }

    @Deprecated
    public ComponentFactory<SshCipher> supportedSsh1CiphersSC(boolean clone) {
        if (clone || perContextAlgorithmPreferences) {
            return (ComponentFactory)this.ssh1ciphersSC.clone();
        }
        return this.ssh1ciphersSC;
    }

    @Deprecated
    public ComponentFactory<SshCipher> supportedSsh1CiphersSC() {
        return this.supportedSsh1CiphersSC(perContextAlgorithmPreferences);
    }

    @Deprecated
    public ComponentFactory<SshCipher> supportedSsh1CiphersCS(boolean clone) {
        if (clone || perContextAlgorithmPreferences) {
            return (ComponentFactory)this.ssh1ciphersCS.clone();
        }
        return this.ssh1ciphersCS;
    }

    @Deprecated
    public ComponentFactory<SshCipher> supportedSsh1CiphersCS() {
        return this.supportedSsh1CiphersCS(perContextAlgorithmPreferences);
    }

    @Deprecated
    public ComponentFactory<SshCipher> supportedSsh2CiphersSC(boolean clone) {
        if (clone || perContextAlgorithmPreferences) {
            return (ComponentFactory)this.ssh2ciphersSC.clone();
        }
        return this.ssh2ciphersSC;
    }

    @Deprecated
    public ComponentFactory<SshCipher> supportedSsh2CiphersSC() {
        return this.supportedSsh2CiphersSC(perContextAlgorithmPreferences);
    }

    @Deprecated
    public ComponentFactory<SshCipher> supportedSsh2CiphersCS(boolean clone) {
        if (clone || perContextAlgorithmPreferences) {
            return (ComponentFactory)this.ssh2ciphersCS.clone();
        }
        return this.ssh2ciphersCS;
    }

    @Deprecated
    public ComponentFactory<SshCipher> supportedSsh2CiphersCS() {
        return this.supportedSsh2CiphersCS(perContextAlgorithmPreferences);
    }

    @Deprecated
    public ComponentFactory<SshHmac> supportedHMacsSC(boolean clone) {
        if (clone || perContextAlgorithmPreferences) {
            return (ComponentFactory)this.hmacsSC.clone();
        }
        return this.hmacsSC;
    }

    @Deprecated
    public ComponentFactory<SshHmac> supportedHMacsSC() {
        return this.supportedHMacsSC(perContextAlgorithmPreferences);
    }

    @Deprecated
    public ComponentFactory<SshHmac> supportedHMacsCS(boolean clone) {
        if (clone || perContextAlgorithmPreferences) {
            return (ComponentFactory)this.hmacsCS.clone();
        }
        return this.hmacsCS;
    }

    @Deprecated
    public ComponentFactory<SshHmac> supportedHMacsCS() {
        return this.supportedHMacsCS(perContextAlgorithmPreferences);
    }

    @Deprecated
    public ComponentFactory<SshKeyExchange> supportedKeyExchanges(boolean serverMode, boolean clone) {
        if (clone || perContextAlgorithmPreferences) {
            return (ComponentFactory)(serverMode ? this.serverKeyexchanges.clone() : this.clientKeyexchanges.clone());
        }
        return serverMode ? this.serverKeyexchanges : this.clientKeyexchanges;
    }

    @Deprecated
    public ComponentFactory<SshKeyExchange> supportedKeyExchanges(boolean serverMode) {
        return this.supportedKeyExchanges(serverMode, perContextAlgorithmPreferences);
    }

    @Deprecated
    public ComponentFactory<SshPublicKey> supportedPublicKeys(boolean clone) {
        if (clone || perContextAlgorithmPreferences) {
            return (ComponentFactory)this.publickeys.clone();
        }
        return this.publickeys;
    }

    @Deprecated
    public ComponentFactory<SshPublicKey> supportedPublicKeys() {
        return this.supportedPublicKeys(perContextAlgorithmPreferences);
    }

    @Deprecated
    public ComponentFactory<Digest> supportedDigests(boolean clone) {
        if (clone || perContextAlgorithmPreferences) {
            return (ComponentFactory)this.digests.clone();
        }
        return this.digests;
    }

    @Deprecated
    public ComponentFactory<Digest> supportedDigests() {
        return this.supportedDigests(perContextAlgorithmPreferences);
    }

    public abstract SshKeyPair generateRsaKeyPair(int var1, int var2) throws SshException;

    public abstract SshKeyPair generateEcdsaKeyPair(int var1) throws SshException;

    public abstract SshRsaPublicKey createRsaPublicKey(BigInteger var1, BigInteger var2, int var3) throws SshException;

    public abstract SshRsaPublicKey createSsh2RsaPublicKey() throws SshException;

    public abstract SshRsaPrivateKey createRsaPrivateKey(BigInteger var1, BigInteger var2) throws SshException;

    public abstract SshRsaPrivateCrtKey createRsaPrivateCrtKey(BigInteger var1, BigInteger var2, BigInteger var3, BigInteger var4, BigInteger var5, BigInteger var6) throws SshException;

    public abstract SshRsaPrivateCrtKey createRsaPrivateCrtKey(BigInteger var1, BigInteger var2, BigInteger var3, BigInteger var4, BigInteger var5, BigInteger var6, BigInteger var7, BigInteger var8) throws SshException;

    public abstract SshKeyPair generateDsaKeyPair(int var1) throws SshException;

    public abstract SshDsaPublicKey createDsaPublicKey(BigInteger var1, BigInteger var2, BigInteger var3, BigInteger var4) throws SshException;

    public abstract SshDsaPublicKey createDsaPublicKey();

    public abstract SshDsaPrivateKey createDsaPrivateKey(BigInteger var1, BigInteger var2, BigInteger var3, BigInteger var4, BigInteger var5) throws SshException;

    public abstract SshSecureRandomGenerator getRND() throws SshException;

    public abstract SshKeyPair[] loadKeystore(File var1, String var2, String var3, String var4) throws IOException;

    public abstract SshKeyPair[] loadKeystore(InputStream var1, String var2, String var3, String var4) throws IOException;

    public abstract SshKeyPair[] loadKeystore(File var1, String var2, String var3, String var4, String var5) throws IOException;

    public abstract SshKeyPair[] loadKeystore(InputStream var1, String var2, String var3, String var4, String var5) throws IOException;

    public abstract SshKeyPair generateEd25519KeyPair() throws SshException;

    public abstract SshKeyPair generateEd448KeyPair() throws SshException;

    public void setMinimumSecurityLevel(SecurityLevel securityLevel) throws SshException {
        this.setMinimumSecurityLevel(securityLevel, false);
    }

    public void setMinimumSecurityLevel(SecurityLevel securityLevel, boolean managedSecurity) throws SshException {
        if (log.isInfoEnabled()) {
            log.info("Configuring {} Security", (Object)securityLevel.name());
        }
        this.setMinimumSecurityLevel(securityLevel, this.ssh2ciphersCS, "Client->Server Ciphers", managedSecurity);
        this.setMinimumSecurityLevel(securityLevel, this.ssh2ciphersSC, "Server->Client Ciphers", managedSecurity);
        this.setMinimumSecurityLevel(securityLevel, this.hmacsCS, "Client->Server Macs", managedSecurity);
        this.setMinimumSecurityLevel(securityLevel, this.hmacsSC, "Server->Client Macs", managedSecurity);
        this.setMinimumSecurityLevel(securityLevel, this.publickeys, "Public Keys", managedSecurity);
        if (this.clientKeyexchanges.hasComponents()) {
            this.setMinimumSecurityLevel(securityLevel, this.clientKeyexchanges, "Client->Server KEX", managedSecurity);
        }
        if (this.serverKeyexchanges.hasComponents()) {
            this.setMinimumSecurityLevel(securityLevel, this.serverKeyexchanges, "Server->Client KEX", managedSecurity);
        }
    }

    private void setMinimumSecurityLevel(SecurityLevel securityLevel, ComponentFactory<?> componentFactory, String name, boolean managedSecurity) throws SshException {
        this.securityLevel = securityLevel;
        if (log.isInfoEnabled()) {
            log.info("Configuring {}", (Object)name);
        }
        componentFactory.configureSecurityLevel(securityLevel);
        if (log.isInfoEnabled()) {
            log.info(componentFactory.list(""));
        }
    }

    public SecurityLevel getSecurityLevel() {
        return this.securityLevel;
    }

    static {
        lock = new Object();
    }
}

