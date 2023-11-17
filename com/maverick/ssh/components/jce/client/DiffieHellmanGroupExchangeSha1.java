/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce.client;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.DiffieHellmanGroups;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.jce.AbstractKeyExchange;
import com.maverick.ssh.components.jce.JCEComponentManager;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh2.SshKeyExchangeClient;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffieHellmanGroupExchangeSha1
extends SshKeyExchangeClient
implements AbstractKeyExchange {
    private static Logger log = LoggerFactory.getLogger(DiffieHellmanGroupExchangeSha1.class);
    public static final String DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA1 = "diffie-hellman-group-exchange-sha1";
    static final int SSH_MSG_KEXDH_GEX_REQUEST_OLD = 30;
    static final int SSH_MSG_KEXDH_GEX_GROUP = 31;
    static final int SSH_MSG_KEXDH_GEX_INIT = 32;
    static final int SSH_MSG_KEXDH_GEX_REPLY = 33;
    static final int SSH_MSG_KEXDH_GEX_REQUEST = 34;
    static final BigInteger ONE = BigInteger.valueOf(1L);
    static final BigInteger TWO = BigInteger.valueOf(2L);
    BigInteger g = TWO;
    BigInteger p;
    BigInteger e = null;
    BigInteger f = null;
    BigInteger y = null;
    BigInteger x = null;
    String clientId;
    String serverId;
    byte[] clientKexInit;
    byte[] serverKexInit;
    KeyPairGenerator dhKeyPairGen;
    KeyAgreement dhKeyAgreement;
    KeyFactory dhKeyFactory;
    KeyPair dhKeyPair;
    static int maxSupportedSize = -1;
    static int minSupportedSize = -1;

    public DiffieHellmanGroupExchangeSha1() {
        this("SHA-1", SecurityLevel.WEAK, 10);
    }

    protected DiffieHellmanGroupExchangeSha1(String algorithm, SecurityLevel securityLevel, int priority) {
        super(algorithm, securityLevel, priority);
    }

    @Override
    public boolean isKeyExchangeMessage(int messageid) {
        switch (messageid) {
            case 30: 
            case 31: 
            case 32: 
            case 33: 
            case 34: {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getAlgorithm() {
        return DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA1;
    }

    @Override
    public void test() {
        try {
            ComponentManager.getInstance().supportedDigests().getInstance(this.getHashAlgorithm());
            this.initCrypto();
        }
        catch (Throwable e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        if (minSupportedSize == -1) {
            Provider provider = this.dhKeyAgreement.getProvider();
            if (provider != null && provider.getName().equals("BC")) {
                minSupportedSize = 1024;
                maxSupportedSize = 8192;
                if (log.isInfoEnabled()) {
                    log.info(String.format("Using BC for DH; prime range is %d to %d bits", minSupportedSize, maxSupportedSize));
                }
            } else {
                for (BigInteger p : DiffieHellmanGroups.allDefaultGroups()) {
                    try {
                        DHParameterSpec dhSkipParamSpec = new DHParameterSpec(p, this.g);
                        this.dhKeyPairGen.initialize(dhSkipParamSpec);
                        KeyPair dhKeyPair = this.dhKeyPairGen.generateKeyPair();
                        this.dhKeyAgreement.init(dhKeyPair.getPrivate());
                        if (minSupportedSize == -1) {
                            minSupportedSize = p.bitLength();
                        }
                        maxSupportedSize = p.bitLength();
                    }
                    catch (Exception e) {}
                }
                if (maxSupportedSize == -1) {
                    throw new IllegalStateException("The diffie hellman algorithm does not appear to be configured correctly on this machine");
                }
                if (maxSupportedSize < 2048) {
                    throw new IllegalStateException(String.format("The maximum supported DH prime is %d bits which is smaller than this algorithm requires", maxSupportedSize));
                }
                if (log.isInfoEnabled()) {
                    log.info(String.format("The supported DH prime range is %d to %d bits", minSupportedSize, maxSupportedSize));
                }
            }
        }
    }

    void initCrypto() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.dhKeyFactory = JCEProvider.getDHKeyFactory();
        this.dhKeyPairGen = JCEProvider.getDHKeyGenerator();
        this.dhKeyAgreement = JCEProvider.getDHKeyAgreement();
    }

    private int maybeLog(String txt, int size) {
        if (log.isDebugEnabled()) {
            this.transport.debug(log, String.format("%s size is %d", txt, size), new Object[0]);
        }
        return size;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void performClientExchange(String clientIdentification, String serverIdentification, byte[] clientKexInit, byte[] serverKexInit) throws SshException {
        try {
            this.initCrypto();
        }
        catch (Exception ex) {
            throw new SshException(ex, 16);
        }
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            this.clientId = clientIdentification;
            this.serverId = serverIdentification;
            this.clientKexInit = clientKexInit;
            this.serverKexInit = serverKexInit;
            boolean disableBackwardsCompatibility = !this.transport.getContext().isDHGroupExchangeBackwardsCompatible();
            int minimumSize = this.maybeLog("Minimum DH prime", AdaptiveConfiguration.getInt("minDHGroupSize", Math.min(maxSupportedSize, Math.max(this.transport.getContext().getMinDHGroupSize(), 1024)), this.transport.getHost(), this.transport.getIdent()));
            int preferredKeySize = this.maybeLog("Preferred DH prime", AdaptiveConfiguration.getInt("dhGroupExchangeKeySize", Math.min(maxSupportedSize, this.transport.getContext().getDHGroupExchangeKeySize()), this.transport.getHost(), this.transport.getIdent()));
            int maximumSize = this.maybeLog("Maximum DH prime", AdaptiveConfiguration.getInt("maxDHGroupSize", Math.min(maxSupportedSize, this.transport.getContext().getMaxDHGroupSize()), this.transport.getHost(), this.transport.getIdent()));
            msg.write(disableBackwardsCompatibility ? 34 : 30);
            if (disableBackwardsCompatibility) {
                msg.writeInt(minimumSize);
                msg.writeInt(preferredKeySize);
                msg.writeInt(maximumSize);
            } else {
                msg.writeInt(Math.min(maxSupportedSize, preferredKeySize));
            }
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Sending " + (disableBackwardsCompatibility ? "SSH_MSG_KEXDH_GEX_REQUEST" : "SSH_MSG_KEXDH_GEX_REQUEST_OLD"), new Object[0]);
            }
            this.transport.sendMessage(msg.toByteArray(), true);
            byte[] tmp = this.transport.nextMessage(0L);
            if (tmp[0] != 31) {
                this.transport.disconnect(3, "Expected SSH_MSG_KEX_GEX_GROUP");
                throw new SshException("Key exchange failed: Expected SSH_MSG_KEX_GEX_GROUP [id=" + tmp[0] + "]", 9);
            }
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Received SSH_MSG_KEXDH_GEX_GROUP", new Object[0]);
            }
            try (ByteArrayReader bar = new ByteArrayReader(tmp, 1, tmp.length - 1);){
                this.p = bar.readBigInteger();
                this.g = bar.readBigInteger();
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, String.format("Received %d bit DH prime with group %s", this.p.bitLength(), this.g.toString(16)), new Object[0]);
                }
                if (this.p.bitLength() > maxSupportedSize) {
                    throw new SshException(String.format("Server sent a prime larger than our configuration can handle! p=%d, max=%d", this.p.bitLength(), maxSupportedSize), 5);
                }
                if (this.g.compareTo(BigInteger.ONE) <= 0) {
                    throw new SshException("Invalid DH g value [" + this.g.toString(16) + "]", 3);
                }
                if (this.p.bitLength() < Math.max(this.transport.getContext().getMinDHGroupSize(), 1024)) {
                    throw new SshException("Minimum DH p value not provided [" + this.p.bitLength() + "]", 3);
                }
                boolean forceJCE = AdaptiveConfiguration.getBoolean("dhForceJCE", false, this.transport.getHost(), this.transport.getIdent());
                boolean forceBypass = AdaptiveConfiguration.getBoolean("dhBypassJCE", false, this.transport.getHost(), this.transport.getIdent());
                try {
                    if (forceJCE) {
                        this.calculateEwithJCE();
                    } else if (forceBypass) {
                        this.calculateE();
                    } else if (this.p.bitLength() % 64 != 0) {
                        this.calculateE();
                    } else {
                        this.calculateEwithJCE();
                    }
                }
                finally {
                    bar.close();
                }
                msg.reset();
                msg.write(32);
                msg.writeBigInteger(this.e);
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Sending SSH_MSG_KEXDH_GEX_INIT", new Object[0]);
                }
                this.transport.sendMessage(msg.toByteArray(), true);
                tmp = this.transport.nextMessage(0L);
                if (tmp[0] != 33) {
                    this.transport.disconnect(3, "Expected SSH_MSG_KEXDH_GEX_REPLY");
                    throw new SshException("Key exchange failed: Expected SSH_MSG_KEXDH_GEX_REPLY [id=" + tmp[0] + "]", 5);
                }
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Received SSH_MSG_KEXDH_GEX_REPLY", new Object[0]);
                }
                bar = new ByteArrayReader(tmp, 1, tmp.length - 1);
                this.hostKey = bar.readBinaryString();
                this.f = bar.readBigInteger();
                this.signature = bar.readBinaryString();
                if (log.isTraceEnabled()) {
                    log.trace("P: " + this.p.toString(16));
                    log.trace("G: " + this.g.toString(16));
                    log.trace("F: " + this.f.toString(16));
                    log.trace("E: " + this.e.toString(16));
                }
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Verifying server DH parameters", new Object[0]);
                }
                if (!DiffieHellmanGroups.verifyParameters(this.f, this.p)) {
                    throw new SshException(String.format("Key exchange detected invalid f value %s", this.f.toString(16)), 3);
                }
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Verified DH parameters. Performing DH calculations", new Object[0]);
                }
                if (AdaptiveConfiguration.getBoolean("dhBypassJCE", false, this.transport.getHost(), this.transport.getIdent()) || this.p.bitLength() % 64 != 0) {
                    this.calculateK();
                } else {
                    this.calculateKwithJCE();
                }
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Verifying calculated DH parameters", new Object[0]);
                }
                if (!DiffieHellmanGroups.verifyParameters(this.secret, this.p)) {
                    throw new SshException(String.format("Key exchange detected invalid k value %s", this.e.toString(16)), 3);
                }
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Calculating exchange hash", new Object[0]);
                }
                this.calculateExchangeHash(disableBackwardsCompatibility, minimumSize, preferredKeySize, maximumSize);
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Completed key exchange calculations", new Object[0]);
                }
            }
        }
        catch (IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            this.transport.error(log, "Key exchange failed", ex);
            throw new SshException("Failed to read SSH_MSG_KEXDH_REPLY", 5);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    private void calculateKwithJCE() throws InvalidKeySpecException, InvalidKeyException, IllegalStateException {
        DHPublicKeySpec spec = new DHPublicKeySpec(this.f, this.p, this.g);
        DHPublicKey key = (DHPublicKey)this.dhKeyFactory.generatePublic(spec);
        this.dhKeyAgreement.doPhase(key, true);
        byte[] tmp = this.dhKeyAgreement.generateSecret();
        if ((tmp[0] & 0x80) == 128) {
            byte[] tmp2 = new byte[tmp.length + 1];
            System.arraycopy(tmp, 0, tmp2, 1, tmp.length);
            tmp = tmp2;
        }
        this.secret = new BigInteger(tmp);
    }

    private void calculateK() {
        this.secret = this.f.modPow(this.x, this.p);
    }

    private void calculateEwithJCE() throws SshException, InvalidKeyException {
        KeyPair dhKeyPair = null;
        int retry = 3;
        do {
            if (retry == 0) {
                this.transport.disconnect(3, "Failed to generate key exchange value");
                throw new SshException("Key exchange failed to generate e value", 5);
            }
            --retry;
            try {
                DHParameterSpec dhSkipParamSpec = new DHParameterSpec(this.p, this.g);
                this.dhKeyPairGen.initialize(dhSkipParamSpec);
                dhKeyPair = this.dhKeyPairGen.generateKeyPair();
                this.dhKeyAgreement.init(dhKeyPair.getPrivate());
                this.e = ((DHPublicKey)dhKeyPair.getPublic()).getY();
            }
            catch (InvalidAlgorithmParameterException ex) {
                throw new SshException("Failed to generate DH value: " + ex.getMessage(), 16, ex);
            }
        } while (this.e.compareTo(ONE) < 0 || this.e.compareTo(this.p.subtract(ONE)) > 0);
    }

    private void calculateE() throws SshException, NoSuchAlgorithmException {
        if (log.isDebugEnabled()) {
            if (AdaptiveConfiguration.getBoolean("dhBypassJCE", false, this.transport.getHost(), this.transport.getIdent())) {
                this.transport.debug(log, "Performing DH e parameter calculation manually because it has been forced by system configuration", new Object[0]);
            } else {
                this.transport.debug(log, String.format("Performing DH e parameter calculation manually because P bit length is not multiple of 64 [%d]", this.p.bitLength()), new Object[0]);
            }
        }
        int retry = 3;
        SecureRandom rnd = JCEComponentManager.getSecureRandom();
        int maxBits = this.p.subtract(BigInteger.ONE).divide(new BigInteger("2")).bitLength();
        do {
            if (retry == 0) {
                this.transport.disconnect(3, "Failed to generate key exchange value");
                throw new SshException("Key exchange failed to generate e value", 5);
            }
            --retry;
            this.x = new BigInteger(maxBits, rnd);
            this.e = this.g.modPow(this.x, this.p);
        } while (!DiffieHellmanGroups.verifyParameters(this.e, this.p));
    }

    @Override
    public String getProvider() {
        if (this.dhKeyAgreement != null) {
            return this.dhKeyAgreement.getProvider().getName();
        }
        return "";
    }

    protected void calculateExchangeHash(boolean disableBackwardsCompatibility, int minimumSize, int preferredKeySize, int maximumSize) throws SshException {
        Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance(this.getHashAlgorithm());
        hash.putString(this.clientId);
        hash.putString(this.serverId);
        hash.putInt(this.clientKexInit.length);
        hash.putBytes(this.clientKexInit);
        hash.putInt(this.serverKexInit.length);
        hash.putBytes(this.serverKexInit);
        hash.putInt(this.hostKey.length);
        hash.putBytes(this.hostKey);
        if (disableBackwardsCompatibility) {
            hash.putInt(minimumSize);
            hash.putInt(preferredKeySize);
            hash.putInt(maximumSize);
        } else {
            hash.putInt(preferredKeySize);
        }
        hash.putBigInteger(this.p);
        hash.putBigInteger(this.g);
        hash.putBigInteger(this.e);
        hash.putBigInteger(this.f);
        hash.putBigInteger(this.secret);
        this.exchangeHash = hash.doFinal();
    }
}

