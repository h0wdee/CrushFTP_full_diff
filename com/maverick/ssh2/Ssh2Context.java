/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh2;

import com.maverick.ssh.DefaultSecurityPolicy;
import com.maverick.ssh.ForwardingRequestListener;
import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SecurityPolicy;
import com.maverick.ssh.SshClientConnector;
import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentFactory;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.SshHmac;
import com.maverick.ssh.components.SshKeyExchange;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.compression.NoneCompression;
import com.maverick.ssh.compression.SshCompression;
import com.maverick.ssh2.BannerDisplay;
import com.maverick.ssh2.MaverickCallbackHandler;
import com.sshtools.zlib.OpenSSHZLibCompression;
import com.sshtools.zlib.ZLibCompression;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssh2Context
implements SshContext {
    static Logger log = LoggerFactory.getLogger(Ssh2Context.class);
    ComponentFactory<SshCompression> compressionsCS;
    ComponentFactory<SshCompression> compressionsSC;
    ComponentFactory<SshCipher> ciphersCS;
    ComponentFactory<SshCipher> ciphersSC;
    ComponentFactory<SshKeyExchange> keyExchanges;
    ComponentFactory<SshHmac> macCS;
    ComponentFactory<SshHmac> macSC;
    ComponentFactory<SshPublicKey> publicKeys;
    public static final String CIPHER_TRIPLEDES_CBC = "3des-cbc";
    public static final String CIPHER_TRIPLEDES_CTR = "3des-ctr";
    public static final String CIPHER_BLOWFISH_CBC = "blowfish-cbc";
    public static final String CIPHER_AES128_CBC = "aes128-cbc";
    public static final String CIPHER_AES192_CBC = "aes192-cbc";
    public static final String CIPHER_AES256_CBC = "aes256-cbc";
    public static final String CIPHER_AES128_CTR = "aes128-ctr";
    public static final String CIPHER_AES192_CTR = "aes192-ctr";
    public static final String CIPHER_AES256_CTR = "aes256-ctr";
    public static final String CIPHER_ARCFOUR = "arcfour";
    public static final String CIPHER_ARCFOUR_128 = "arcfour128";
    public static final String CIPHER_ARCFOUR_256 = "arcfour256";
    public static final String CIPHER_AES_GCM_128 = "aes128-gcm@openssh.com";
    public static final String CIPHER_AES_GCM_256 = "aes256-gcm@openssh.com";
    public static final String CIPHER_CHACHA20_POLY1305 = "chacha20-poly1305@openssh.com";
    public static final String HMAC_SHA1 = "hmac-sha1";
    public static final String HMAC_SHA1_ETM = "hmac-sha1-etm@openssh.com";
    public static final String HMAC_SHA1_96 = "hmac-sha1-96";
    public static final String HMAC_MD5 = "hmac-md5";
    public static final String HMAC_MD5_ETM = "hmac-md5-etm@openssh.com";
    public static final String HMAC_MD5_96 = "hmac-md5-96";
    public static final String HMAC_SHA256 = "hmac-sha2-256";
    public static final String HMAC_SHA256_ETM = "hmac-sha2-256-etm@openssh.com";
    public static final String HMAC_SHA256_96 = "hmac-sha2-256-96";
    public static final String HMAC_SHA512 = "hmac-sha2-512";
    public static final String HMAC_SHA512_ETM = "hmac-sha2-512-etm@openssh.com";
    public static final String HMAC_SHA512_96 = "hmac-sha2-512-96";
    public static final String HMAC_RIPEMD160 = "hmac-ripemd160";
    public static final String HMAC_RIPEMD160_ETM = "hmac-ripemd160-etm@openssh.com";
    public static final String COMPRESSION_NONE = "none";
    public static final String COMPRESSION_ZLIB = "zlib";
    public static final String COMPRESSION_ZLIB_OPENSSH = "zlib@openssh.com";
    public static final String KEX_DIFFIE_HELLMAN_GROUP1_SHA1 = "diffie-hellman-group1-sha1";
    public static final String KEX_DIFFIE_HELLMAN_GROUP14_SHA1 = "diffie-hellman-group14-sha1";
    public static final String KEX_DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA1 = "diffie-hellman-group-exchange-sha1";
    public static final String KEX_DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA256 = "diffie-hellman-group-exchange-sha256";
    public static final String KEX_DIFFIE_HELLMAN_ECDH_NISTP_256 = "ecdh-sha2-nistp256";
    public static final String KEX_DIFFIE_HELLMAN_ECDH_NISTP_384 = "ecdh-sha2-nistp384";
    public static final String KEX_DIFFIE_HELLMAN_ECDH_NISTP_521 = "ecdh-sha2-nistp521";
    public static final String KEX_CURVE_25519_SHA256_LIBSSH_ORG = "curve25519-sha256@libssh.org";
    public static final String KEX_CURVE_25519_SHA256 = "curve25519-sha256";
    public static final String KEX_RSA_2048_SHA256 = "rsa2048-sha256";
    public static final String KEX_RSA_1024_SHA1 = "rsa1024-sha1";
    public static final String KEX_DIFFIE_HELLMAN_GROUP14_SHA256 = "diffie-hellman-group14-sha256";
    public static final String KEX_DIFFIE_HELLMAN_GROUP15_SHA512 = "diffie-hellman-group15-sha512";
    public static final String KEX_DIFFIE_HELLMAN_GROUP16_SHA512 = "diffie-hellman-group16-sha512";
    public static final String KEX_DIFFIE_HELLMAN_GROUP17_SHA512 = "diffie-hellman-group17-sha512";
    public static final String KEX_DIFFIE_HELLMAN_GROUP18_SHA512 = "diffie-hellman-group18-sha512";
    public static final String PUBLIC_KEY_SSHDSS = "ssh-dss";
    public static final String PUBLIC_KEY_SSHRSA = "ssh-rsa";
    public static final String PUBLIC_KEY_X509_SIGN_RSA = "x509v3-sign-rsa";
    public static final String PUBLIC_KEY_X509_SIGN_RSA_SHA1 = "x509v3-sign-rsa-sha1";
    public static final String PUBLIC_KEY_X509_SIGN_DSA = "x509v3-sign-dss";
    public static final String PUBLIC_KEY_ECDSA_256 = "ecdsa-sha2-nistp256";
    public static final String PUBLIC_KEY_ECDSA_384 = "ecdsa-sha2-nistp384";
    public static final String PUBLIC_KEY_ECDSA_521 = "ecdsa-sha2-nistp521";
    public static final String PUBLIC_KEY_ED25519 = "ssh-ed25519";
    public static final String PUBLIC_KEY_RSA_SHA256 = "rsa-sha2-256";
    public static final String PUBLIC_KEY_RSA_SHA512 = "rsa-sha2-512";
    String prefCipherCS = "aes256-gcm@openssh.com";
    String prefCipherSC = "aes256-gcm@openssh.com";
    String prefMacCS = "hmac-sha2-512";
    String prefMacSC = "hmac-sha2-512";
    String prefCompressionCS = "none";
    String prefCompressionSC = "none";
    String prefKeyExchange = "curve25519-sha256@libssh.org";
    String prefPublicKey = "ssh-ed25519";
    String sftpProvider = "/usr/libexec/sftp-server";
    boolean preferKeyboardInteractiveOverPassword = true;
    int maxChannels = 100;
    BannerDisplay bannerdisplay;
    HostKeyVerification verify;
    String xDisplay = null;
    byte[] x11FakeCookie = null;
    byte[] x11RealCookie = null;
    ForwardingRequestListener x11Listener = null;
    String jceProvider = "";
    int maxPacketLength = 131328;
    boolean keyReExchangeDisabled = false;
    int partialMessageTimeout = 30000;
    int keepAliveMaxDataLength = 128;
    int idleAuthenticationTimeoutSeconds = 60;
    int idleConnectionTimeoutSeconds = 0;
    boolean sendIgnorePacketOnIdle = true;
    boolean treatIdleConnectionAsError = false;
    int socketTimeout = 10000;
    int messageTimeout = 60000;
    SshClientConnector con;
    int dhGroupExchangeKeySize = 2048;
    boolean dhGroupExchangeBackwardCompatible = false;
    boolean triedBackwardsCompatibility = false;
    boolean triedMinimalKeyExchangePacket = false;
    MaverickCallbackHandler gsscall = null;
    int maxNumPacketsBeforeReKey = Integer.MAX_VALUE;
    int maxNumBytesBeforeReKey = Integer.MAX_VALUE;
    int sessionMaxWindowSpace = 131072;
    int sessionMaxPacketSize = 34000;
    int sftpMaxWindowSpace = Integer.MAX_VALUE;
    int sftpMaxPacketSize = 35000;
    boolean enableETM = true;
    boolean enableNonStandardAlgorithms = true;
    int minDHGroupSize = 1024;
    int maxDHGroupSize = 8192;
    ExecutorService executor;
    boolean allowHostKeyUpdates = true;
    boolean limitPublicKeysToKnownHosts = true;
    SecurityPolicy securityPolicy;
    boolean supportsSHA1Signatures = true;
    Map<String, String> temporarySettings = new HashMap<String, String>();

    public Ssh2Context() throws SshException {
        this(new DefaultSecurityPolicy(SecurityLevel.WEAK, false));
    }

    public Ssh2Context(SecurityPolicy securityPolicy) throws SshException {
        this.securityPolicy = securityPolicy;
        try {
            this.ciphersCS = ComponentManager.getInstance().supportedSsh2CiphersCS(securityPolicy.isManagedSecurity());
            this.ciphersSC = ComponentManager.getInstance().supportedSsh2CiphersSC(securityPolicy.isManagedSecurity());
            this.keyExchanges = ComponentManager.getInstance().supportedKeyExchanges(false, securityPolicy.isManagedSecurity());
            this.macCS = ComponentManager.getInstance().supportedHMacsCS(securityPolicy.isManagedSecurity());
            this.macSC = ComponentManager.getInstance().supportedHMacsSC(securityPolicy.isManagedSecurity());
            this.publicKeys = ComponentManager.getInstance().supportedPublicKeys(securityPolicy.isManagedSecurity());
            this.ciphersCS.configureSecurityLevel(securityPolicy.getMinimumSecurityLevel());
            this.ciphersSC.configureSecurityLevel(securityPolicy.getMinimumSecurityLevel());
            this.keyExchanges.configureSecurityLevel(securityPolicy.getMinimumSecurityLevel());
            this.macCS.configureSecurityLevel(securityPolicy.getMinimumSecurityLevel());
            this.macSC.configureSecurityLevel(securityPolicy.getMinimumSecurityLevel());
            this.publicKeys.configureSecurityLevel(securityPolicy.getMinimumSecurityLevel());
            this.prefCipherCS = this.ciphersCS.order().iterator().next();
            this.prefCipherSC = this.ciphersSC.order().iterator().next();
            this.prefMacCS = this.macCS.order().iterator().next();
            this.prefMacSC = this.macSC.order().iterator().next();
            this.prefKeyExchange = this.keyExchanges.order().iterator().next();
            this.prefPublicKey = this.publicKeys.order().iterator().next();
            if (log.isDebugEnabled()) {
                log.debug("Creating compression factory");
            }
            this.compressionsSC = new ComponentFactory(ComponentManager.getInstance());
            if (log.isDebugEnabled()) {
                log.debug("Adding None Compression");
            }
            this.compressionsSC.add(COMPRESSION_NONE, NoneCompression.class);
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Adding ZLib Compression");
                }
                this.compressionsSC.add(COMPRESSION_ZLIB, ZLibCompression.class);
                this.compressionsSC.add(COMPRESSION_ZLIB_OPENSSH, OpenSSHZLibCompression.class);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.compressionsCS = new ComponentFactory(ComponentManager.getInstance());
            if (log.isDebugEnabled()) {
                log.debug("Adding None Compression");
            }
            this.compressionsCS.add(COMPRESSION_NONE, NoneCompression.class);
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Adding ZLib Compression");
                }
                this.compressionsCS.add(COMPRESSION_ZLIB, ZLibCompression.class);
                this.compressionsCS.add(COMPRESSION_ZLIB_OPENSSH, OpenSSHZLibCompression.class);
            }
            catch (Throwable throwable) {}
        }
        catch (Throwable t) {
            throw new SshException(t.getMessage() != null ? t.getMessage() : t.getClass().getName(), 5, t);
        }
        if (log.isDebugEnabled()) {
            log.debug("Completed Ssh2Context creation");
        }
    }

    public String getTemporaryValue(String name, String defaultValue) {
        String val = this.temporarySettings.get(name);
        if (val == null) {
            val = defaultValue;
        }
        return val;
    }

    public boolean getTemporaryValue(String name, boolean defaultValue) {
        return Boolean.valueOf(this.getTemporaryValue(name, String.valueOf(defaultValue)));
    }

    public void setTemporaryValue(String name, String value) {
        this.temporarySettings.put(name, value);
    }

    public void setTemporaryValue(String name, boolean value) {
        this.setTemporaryValue(name, String.valueOf(value));
    }

    public int getMaximumPacketLength() {
        return this.maxPacketLength;
    }

    public void setGssCallback(MaverickCallbackHandler gsscall) {
        this.gsscall = gsscall;
    }

    public MaverickCallbackHandler getGssCallback() {
        return this.gsscall;
    }

    public void setMaximumPacketLength(int maxPacketLength) {
        if (maxPacketLength < 35000) {
            throw new IllegalArgumentException("The minimum packet length supported must be 35,000 bytes or greater!");
        }
        this.maxPacketLength = maxPacketLength;
    }

    @Override
    public void setChannelLimit(int maxChannels) {
        this.maxChannels = maxChannels;
    }

    @Override
    public int getChannelLimit() {
        return this.maxChannels;
    }

    @Override
    public void setX11Display(String xDisplay) {
        this.xDisplay = xDisplay;
    }

    @Override
    public String getX11Display() {
        return this.xDisplay;
    }

    @Override
    public byte[] getX11AuthenticationCookie() throws SshException {
        if (this.x11FakeCookie == null) {
            this.x11FakeCookie = new byte[16];
            ComponentManager.getInstance().getRND().nextBytes(this.x11FakeCookie);
        }
        return this.x11FakeCookie;
    }

    @Override
    public void setX11AuthenticationCookie(byte[] x11FakeCookie) {
        this.x11FakeCookie = x11FakeCookie;
    }

    @Override
    public void setX11RealCookie(byte[] x11RealCookie) {
        this.x11RealCookie = x11RealCookie;
    }

    @Override
    public byte[] getX11RealCookie() throws SshException {
        if (this.x11RealCookie == null) {
            this.x11RealCookie = this.getX11AuthenticationCookie();
        }
        return this.x11RealCookie;
    }

    public void disableETM() {
        this.enableETM = false;
    }

    public void enableETM() {
        this.enableETM = true;
    }

    public boolean isEnableETM() {
        return this.enableETM;
    }

    public void disableNonStandardAlgorithms() {
        this.enableNonStandardAlgorithms = false;
    }

    public void enableNonStandardAlgorithms() {
        this.enableNonStandardAlgorithms = true;
    }

    public boolean isNonStandardAlgorithmsEnabled() {
        return this.enableNonStandardAlgorithms;
    }

    @Override
    public void setX11RequestListener(ForwardingRequestListener x11Listener) {
        this.x11Listener = x11Listener;
    }

    @Override
    public ForwardingRequestListener getX11RequestListener() {
        return this.x11Listener;
    }

    public BannerDisplay getBannerDisplay() {
        return this.bannerdisplay;
    }

    public void setBannerDisplay(BannerDisplay bannerdisplay) {
        this.bannerdisplay = bannerdisplay;
    }

    public ComponentFactory<SshCipher> supportedCiphersSC() {
        return this.ciphersSC;
    }

    public ComponentFactory<SshCipher> supportedCiphersCS() {
        return this.ciphersCS;
    }

    public String getPreferredCipherCS() {
        return this.prefCipherCS;
    }

    private void verifyManagedSecurity() throws SshException {
        if (this.securityPolicy.isManagedSecurity()) {
            throw new SshException("You are using a managed instance. Preferences are not allowed.", 4);
        }
    }

    public void setPreferredCipherCS(String name) throws SshException {
        if (name == null) {
            return;
        }
        this.verifyManagedSecurity();
        if (!this.ciphersCS.contains(name)) {
            throw new SshException(name + " is not supported", 7);
        }
        this.setCipherPreferredPositionCS(name, 0);
    }

    public String getPreferredCipherSC() {
        return this.prefCipherSC;
    }

    public String getCiphersCS() {
        return this.ciphersCS.list(this.prefCipherCS);
    }

    public String getCiphersSC() {
        return this.ciphersSC.list(this.prefCipherSC);
    }

    public String getMacsCS() {
        return this.macCS.list(this.prefMacCS);
    }

    public String getMacsSC() {
        return this.macSC.list(this.prefMacSC);
    }

    public String getPublicKeys() {
        return this.publicKeys.list(this.prefPublicKey);
    }

    public String getKeyExchanges() {
        return this.keyExchanges.list(this.prefKeyExchange);
    }

    public void setPreferredCipherSC(int[] order) throws SshException {
        this.verifyManagedSecurity();
        this.prefCipherSC = this.ciphersSC.createNewOrdering(order);
    }

    public void setPreferredCipherCS(int[] order) throws SshException {
        this.verifyManagedSecurity();
        this.prefCipherCS = this.ciphersCS.createNewOrdering(order);
    }

    public void setCipherPreferredPositionCS(String name, int position) throws SshException {
        this.verifyManagedSecurity();
        this.prefCipherCS = this.ciphersCS.changePositionofAlgorithm(name, position);
    }

    public void setCipherPreferredPositionSC(String name, int position) throws SshException {
        this.verifyManagedSecurity();
        this.prefCipherSC = this.ciphersSC.changePositionofAlgorithm(name, position);
    }

    public void setMacPreferredPositionSC(String name, int position) throws SshException {
        this.verifyManagedSecurity();
        this.prefMacSC = this.macSC.changePositionofAlgorithm(name, position);
    }

    public void setMacPreferredPositionCS(String name, int position) throws SshException {
        this.verifyManagedSecurity();
        this.prefMacCS = this.macCS.changePositionofAlgorithm(name, position);
    }

    public void setPreferredMacSC(int[] order) throws SshException {
        this.verifyManagedSecurity();
        this.prefCipherSC = this.macSC.createNewOrdering(order);
    }

    public void setPreferredMacCS(int[] order) throws SshException {
        this.verifyManagedSecurity();
        this.prefCipherSC = this.macCS.createNewOrdering(order);
    }

    public void setPreferredCipherSC(String name) throws SshException {
        if (name == null) {
            return;
        }
        this.verifyManagedSecurity();
        if (!this.ciphersSC.contains(name)) {
            throw new SshException(name + " is not supported", 7);
        }
        this.setCipherPreferredPositionSC(name, 0);
    }

    public ComponentFactory<SshHmac> supportedMacsSC() {
        return this.macSC;
    }

    public ComponentFactory<SshHmac> supportedMacsCS() {
        return this.macCS;
    }

    public String getPreferredMacCS() {
        return this.prefMacCS;
    }

    public void setPreferredMacCS(String name) throws SshException {
        if (name == null) {
            return;
        }
        this.verifyManagedSecurity();
        if (!this.macCS.contains(name)) {
            throw new SshException(name + " is not supported", 7);
        }
        this.setMacPreferredPositionCS(name, 0);
    }

    public String getPreferredMacSC() {
        return this.prefMacSC;
    }

    public void setPreferredMacSC(String name) throws SshException {
        if (name == null) {
            return;
        }
        this.verifyManagedSecurity();
        if (!this.macSC.contains(name)) {
            throw new SshException(name + " is not supported", 7);
        }
        this.setMacPreferredPositionSC(name, 0);
    }

    public ComponentFactory<SshCompression> supportedCompressionsSC() {
        return this.compressionsSC;
    }

    public ComponentFactory<SshCompression> supportedCompressionsCS() {
        return this.compressionsCS;
    }

    public String getPreferredCompressionCS() {
        return this.prefCompressionCS;
    }

    public void setPreferredCompressionCS(String name) throws SshException {
        if (name == null) {
            return;
        }
        if (!this.compressionsCS.contains(name)) {
            throw new SshException(name + " is not supported", 7);
        }
        this.prefCompressionCS = name;
    }

    public String getPreferredCompressionSC() {
        return this.prefCompressionSC;
    }

    public void setPreferredCompressionSC(String name) throws SshException {
        if (name == null) {
            return;
        }
        if (!this.compressionsSC.contains(name)) {
            throw new SshException(name + " is not supported", 7);
        }
        this.prefCompressionSC = name;
    }

    public void enableCompression() throws SshException {
        this.supportedCompressionsCS().changePositionofAlgorithm(COMPRESSION_ZLIB, 0);
        this.supportedCompressionsCS().changePositionofAlgorithm(COMPRESSION_ZLIB_OPENSSH, 1);
        this.prefCompressionCS = this.supportedCompressionsCS().changePositionofAlgorithm(COMPRESSION_NONE, 2);
        this.supportedCompressionsSC().changePositionofAlgorithm(COMPRESSION_ZLIB, 0);
        this.supportedCompressionsSC().changePositionofAlgorithm(COMPRESSION_ZLIB_OPENSSH, 1);
        this.prefCompressionSC = this.supportedCompressionsSC().changePositionofAlgorithm(COMPRESSION_NONE, 2);
    }

    public void disableCompression() throws SshException {
        this.supportedCompressionsCS().changePositionofAlgorithm(COMPRESSION_NONE, 0);
        this.supportedCompressionsCS().changePositionofAlgorithm(COMPRESSION_ZLIB, 1);
        this.prefCompressionCS = this.supportedCompressionsCS().changePositionofAlgorithm(COMPRESSION_ZLIB_OPENSSH, 2);
        this.supportedCompressionsSC().changePositionofAlgorithm(COMPRESSION_NONE, 0);
        this.supportedCompressionsSC().changePositionofAlgorithm(COMPRESSION_ZLIB, 1);
        this.prefCompressionSC = this.supportedCompressionsSC().changePositionofAlgorithm(COMPRESSION_ZLIB_OPENSSH, 2);
    }

    public ComponentFactory<SshKeyExchange> supportedKeyExchanges() {
        return this.keyExchanges;
    }

    public String getPreferredKeyExchange() {
        return this.prefKeyExchange;
    }

    public void setPreferredKeyExchange(String name) throws SshException {
        if (name == null) {
            return;
        }
        this.verifyManagedSecurity();
        if (!this.keyExchanges.contains(name)) {
            throw new SshException(name + " is not supported", 7);
        }
        this.setKeyExchangePreferredPosition(name, 0);
    }

    public ComponentFactory<SshPublicKey> supportedPublicKeys() {
        return this.publicKeys;
    }

    public String getPreferredPublicKey() {
        return this.prefPublicKey;
    }

    public void setPreferredPublicKey(String name) throws SshException {
        if (name == null) {
            return;
        }
        this.verifyManagedSecurity();
        if (!this.publicKeys.contains(name)) {
            throw new SshException(name + " is not supported", 7);
        }
        this.setPublicKeyPreferredPosition(name, 0);
    }

    @Override
    public void setHostKeyVerification(HostKeyVerification verify) {
        this.verify = verify;
    }

    @Override
    public HostKeyVerification getHostKeyVerification() {
        return this.verify;
    }

    @Override
    public void setSFTPProvider(String sftpProvider) {
        this.sftpProvider = sftpProvider;
    }

    @Override
    public String getSFTPProvider() {
        return this.sftpProvider;
    }

    public void setPartialMessageTimeout(int partialMessageTimeout) {
        this.partialMessageTimeout = partialMessageTimeout;
    }

    public int getPartialMessageTimeout() {
        return this.partialMessageTimeout;
    }

    public boolean isKeyReExchangeDisabled() {
        return this.keyReExchangeDisabled;
    }

    public void setKeyReExchangeDisabled(boolean keyReExchangeDisabled) {
        this.keyReExchangeDisabled = keyReExchangeDisabled;
    }

    public void setPublicKeyPreferredPosition(String name, int position) throws SshException {
        this.prefPublicKey = this.publicKeys.changePositionofAlgorithm(name, position);
    }

    public void setKeyExchangePreferredPosition(String name, int position) throws SshException {
        this.prefKeyExchange = this.keyExchanges.changePositionofAlgorithm(name, position);
    }

    public int getIdleConnectionTimeoutSeconds() {
        return this.idleConnectionTimeoutSeconds;
    }

    public void setIdleConnectionTimeoutSeconds(int idleConnectionTimeoutSeconds) {
        this.idleConnectionTimeoutSeconds = idleConnectionTimeoutSeconds;
    }

    public void setTreatIdleConnectionAsError(boolean treatIdleConnectionAsError) {
        this.treatIdleConnectionAsError = treatIdleConnectionAsError;
    }

    public boolean isTreatIdleConnectionAsError() {
        return this.treatIdleConnectionAsError;
    }

    public boolean isDHGroupExchangeBackwardsCompatible() {
        return this.dhGroupExchangeBackwardCompatible;
    }

    public int getDHGroupExchangeKeySize() {
        return this.dhGroupExchangeKeySize;
    }

    public void setDHGroupExchangeKeySize(int dhGroupExchangeKeySize) {
        if (dhGroupExchangeKeySize < 1024 || dhGroupExchangeKeySize > 8192) {
            throw new IllegalArgumentException("DH group exchange key size must be between 1024 and 8192");
        }
        this.dhGroupExchangeKeySize = dhGroupExchangeKeySize;
    }

    public void setDHGroupExchangeBackwardsCompatible(boolean dhGroupExchangeBackwardCompatible) {
        this.dhGroupExchangeBackwardCompatible = dhGroupExchangeBackwardCompatible;
    }

    public boolean isSendIgnorePacketOnIdle() {
        return this.sendIgnorePacketOnIdle;
    }

    public void setSendIgnorePacketOnIdle(boolean sendIgnorePacketOnIdle) {
        this.sendIgnorePacketOnIdle = sendIgnorePacketOnIdle;
    }

    public int getKeepAliveMaxDataLength() {
        return this.keepAliveMaxDataLength;
    }

    public void setKeepAliveMaxDataLength(int keepAliveMaxDataLength) {
        if (keepAliveMaxDataLength < 8) {
            throw new IllegalArgumentException("There must be at least 8 bytes of random data");
        }
        this.keepAliveMaxDataLength = keepAliveMaxDataLength;
    }

    public int getSocketTimeout() {
        return this.socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    @Override
    public void setMessageTimeout(int messageTimeout) {
        this.messageTimeout = messageTimeout;
    }

    @Override
    public int getMessageTimeout() {
        return this.messageTimeout;
    }

    @Override
    @Deprecated
    public void enableFIPSMode() throws SshException {
        int i;
        if (log.isInfoEnabled()) {
            log.info("Enabling FIPS mode");
        }
        if (!this.keyExchanges.contains(KEX_DIFFIE_HELLMAN_GROUP14_SHA1)) {
            throw new SshException("Cannot enable FIPS mode because diffie-hellman-group14-sha1 keyexchange was not supported by this configuration. Install a JCE Provider that supports a prime size of 2048 bits (for example BouncyCastle provider)", 4);
        }
        Vector<String> allowed = new Vector<String>();
        if (this.dhGroupExchangeKeySize < 2048) {
            this.dhGroupExchangeKeySize = 2048;
        }
        allowed.addElement(KEX_DIFFIE_HELLMAN_GROUP14_SHA1);
        allowed.addElement(KEX_DIFFIE_HELLMAN_GROUP14_SHA256);
        allowed.addElement(KEX_DIFFIE_HELLMAN_GROUP15_SHA512);
        allowed.addElement(KEX_DIFFIE_HELLMAN_GROUP16_SHA512);
        allowed.addElement(KEX_DIFFIE_HELLMAN_GROUP17_SHA512);
        allowed.addElement(KEX_DIFFIE_HELLMAN_GROUP18_SHA512);
        allowed.addElement(KEX_DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA1);
        allowed.addElement(KEX_DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA256);
        allowed.addElement(KEX_DIFFIE_HELLMAN_ECDH_NISTP_256);
        allowed.addElement(KEX_DIFFIE_HELLMAN_ECDH_NISTP_384);
        allowed.addElement(KEX_DIFFIE_HELLMAN_ECDH_NISTP_521);
        String[] names = this.keyExchanges.toArray();
        for (i = 0; i < names.length; ++i) {
            if (allowed.contains(names[i])) continue;
            if (log.isInfoEnabled()) {
                log.info("Removing key exchange " + names[i]);
            }
            this.keyExchanges.remove(names[i]);
        }
        this.keyExchanges.lockComponents();
        allowed.clear();
        allowed.addElement(CIPHER_AES128_CBC);
        allowed.addElement(CIPHER_AES192_CBC);
        allowed.addElement(CIPHER_AES256_CBC);
        allowed.addElement(CIPHER_TRIPLEDES_CBC);
        allowed.addElement(CIPHER_AES128_CTR);
        allowed.addElement(CIPHER_AES192_CTR);
        allowed.addElement(CIPHER_AES256_CTR);
        allowed.addElement(CIPHER_TRIPLEDES_CTR);
        names = this.ciphersCS.toArray();
        for (i = 0; i < names.length; ++i) {
            if (allowed.contains(names[i])) continue;
            if (log.isInfoEnabled()) {
                log.info("Removing cipher client->server " + names[i]);
            }
            this.ciphersCS.remove(names[i]);
        }
        this.ciphersCS.lockComponents();
        names = this.ciphersSC.toArray();
        for (i = 0; i < names.length; ++i) {
            if (allowed.contains(names[i])) continue;
            if (log.isInfoEnabled()) {
                log.info("Removing cipher server->client " + names[i]);
            }
            this.ciphersSC.remove(names[i]);
        }
        this.ciphersSC.lockComponents();
        allowed.clear();
        allowed.addElement(PUBLIC_KEY_SSHRSA);
        allowed.addElement(PUBLIC_KEY_ECDSA_256);
        allowed.addElement(PUBLIC_KEY_ECDSA_384);
        allowed.addElement(PUBLIC_KEY_ECDSA_521);
        names = this.publicKeys.toArray();
        for (i = 0; i < names.length; ++i) {
            if (allowed.contains(names[i])) continue;
            if (log.isInfoEnabled()) {
                log.info("Removing public key " + names[i]);
            }
            this.publicKeys.remove(names[i]);
        }
        this.publicKeys.lockComponents();
        allowed.clear();
        allowed.addElement(HMAC_SHA1);
        allowed.addElement(HMAC_SHA256);
        allowed.addElement("hmac-sha256@ssh.com");
        names = this.macCS.toArray();
        for (i = 0; i < names.length; ++i) {
            if (allowed.contains(names[i])) continue;
            if (log.isInfoEnabled()) {
                log.info("Removing mac client->server " + names[i]);
            }
            this.macCS.remove(names[i]);
        }
        this.macCS.lockComponents();
        names = this.macSC.toArray();
        for (i = 0; i < names.length; ++i) {
            if (allowed.contains(names[i])) continue;
            if (log.isInfoEnabled()) {
                log.info("Removing mac server->client " + names[i]);
            }
            this.macSC.remove(names[i]);
        }
        this.macCS.lockComponents();
    }

    public int getSftpMaxWindowSpace() {
        return this.sftpMaxWindowSpace;
    }

    public int getSftpMaxPacketSize() {
        return this.sftpMaxPacketSize;
    }

    public void setSftpMaxWindowSpace(int sftpMaxWindowSpace) {
        this.sftpMaxWindowSpace = sftpMaxWindowSpace;
    }

    public void setSftpMaxPacketSize(int sftpMaxPacketSize) {
        this.sftpMaxPacketSize = sftpMaxPacketSize;
    }

    public int getSessionMaxWindowSpace() {
        return this.sessionMaxWindowSpace;
    }

    public int getSessionMaxPacketSize() {
        return this.sessionMaxPacketSize;
    }

    public void setSessionMaxWindowSpace(int sessionMaxWindowSpace) {
        this.sessionMaxWindowSpace = sessionMaxWindowSpace;
    }

    public void setSessionMaxPacketSize(int sessionMaxPacketSize) {
        this.sessionMaxPacketSize = sessionMaxPacketSize;
    }

    public int getMaxNumPacketsBeforeReKey() {
        return this.maxNumPacketsBeforeReKey;
    }

    public int getMaxNumBytesBeforeReKey() {
        return this.maxNumBytesBeforeReKey;
    }

    public void setMaxNumBytesBeforeReKey(int maxNumBytesBeforeReKey) {
        this.maxNumBytesBeforeReKey = maxNumBytesBeforeReKey;
    }

    public void setMaxNumPacketsBeforeReKey(int maxNumPacketsBeforeReKey) {
        this.maxNumPacketsBeforeReKey = maxNumPacketsBeforeReKey;
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.executor;
    }

    @Override
    public void setExecutorService(ExecutorService executor) {
        this.executor = executor;
    }

    public void setPreferKeyboardInteractiveOverPassword(boolean preferKeyboardInteractiveOverPassword) {
        this.preferKeyboardInteractiveOverPassword = preferKeyboardInteractiveOverPassword;
    }

    public boolean isPreferKeyboardInteractiveOverPassword() {
        return this.preferKeyboardInteractiveOverPassword;
    }

    public void setMinDHGroupSize(int minDHGroupSize) {
        if (minDHGroupSize < 1024) {
            throw new IllegalArgumentException("Minimum DH group size must be at least 1024 bits");
        }
        this.minDHGroupSize = minDHGroupSize;
    }

    public int getMinDHGroupSize() {
        return this.minDHGroupSize;
    }

    public void setMaxDHGroupSize(int maxDHGroupSize) {
        this.maxDHGroupSize = maxDHGroupSize;
    }

    public int getMaxDHGroupSize() {
        return this.maxDHGroupSize;
    }

    public boolean allowHostKeyUpdates() {
        return this.allowHostKeyUpdates;
    }

    public void setAllowHostKeyUpdates(boolean value) {
        this.allowHostKeyUpdates = value;
    }

    public SecurityPolicy getSecurityPolicy() {
        return this.securityPolicy;
    }

    public boolean isLimitPublicKeysToKnownHosts() {
        return this.limitPublicKeysToKnownHosts;
    }

    public void setLimitPublicKeysToKnownHosts(boolean limitPublicKeysToKnownHosts) {
        this.limitPublicKeysToKnownHosts = limitPublicKeysToKnownHosts;
    }

    @Deprecated
    public boolean isTriedBackwardsCompatibility() {
        return this.triedBackwardsCompatibility;
    }

    @Deprecated
    public void setTriedBackwardsCompatibility(boolean triedBackwardsCompatibility) {
        this.triedBackwardsCompatibility = triedBackwardsCompatibility;
    }

    @Deprecated
    public boolean isTriedMinimalKeyExchangePacket() {
        return this.triedMinimalKeyExchangePacket;
    }

    @Deprecated
    public void setTriedMinimalKeyExchangePacket(boolean triedMinimalKeyExchangePacket) {
        this.triedMinimalKeyExchangePacket = triedMinimalKeyExchangePacket;
    }

    @Override
    public boolean isSHA1SignaturesSupported() {
        return this.supportsSHA1Signatures;
    }

    @Override
    public void setSHA1SignaturesSupported(boolean supportSHA1Signatures) {
        this.supportsSHA1Signatures = supportSHA1Signatures;
    }

    public int getIdleAuthenticationTimeoutSeconds() {
        return this.idleAuthenticationTimeoutSeconds;
    }

    public void setIdleAuthenticationTimeoutSeconds(int idleAuthenticationTimeoutSeconds) {
        this.idleAuthenticationTimeoutSeconds = idleAuthenticationTimeoutSeconds;
    }
}

