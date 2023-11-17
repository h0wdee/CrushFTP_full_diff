/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce.client;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.DiffieHellmanGroups;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.jce.AbstractKeyExchange;
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
import java.security.spec.InvalidKeySpecException;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffieHellmanGroup14Sha1
extends SshKeyExchangeClient
implements AbstractKeyExchange {
    private static Logger log = LoggerFactory.getLogger(DiffieHellmanGroup14Sha1.class);
    public static final String DIFFIE_HELLMAN_GROUP14_SHA1 = "diffie-hellman-group14-sha1";
    static final int SSH_MSG_KEXDH_INIT = 30;
    static final int SSH_MSG_KEXDH_REPLY = 31;
    static final BigInteger ONE = BigInteger.valueOf(1L);
    static final BigInteger TWO;
    static final BigInteger g;
    static final BigInteger p;
    BigInteger e = null;
    BigInteger f = null;
    BigInteger y = null;
    String clientId;
    String serverId;
    byte[] clientKexInit;
    byte[] serverKexInit;
    KeyPairGenerator dhKeyPairGen;
    KeyAgreement dhKeyAgreement;
    KeyFactory dhKeyFactory;
    KeyPair dhKeyPair;

    public DiffieHellmanGroup14Sha1() {
        super("SHA-1", SecurityLevel.WEAK, 14);
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
    }

    void initCrypto() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.dhKeyFactory = JCEProvider.getDHKeyFactory();
        this.dhKeyPairGen = JCEProvider.getDHKeyGenerator();
        this.dhKeyAgreement = JCEProvider.getDHKeyAgreement();
        DHParameterSpec dhSkipParamSpec = new DHParameterSpec(p, g);
        this.dhKeyPairGen.initialize(dhSkipParamSpec);
        this.dhKeyPair = this.dhKeyPairGen.generateKeyPair();
        this.dhKeyAgreement.init(this.dhKeyPair.getPrivate());
    }

    @Override
    public void performClientExchange(String clientIdentification, String serverIdentification, byte[] clientKexInit, byte[] serverKexInit) throws SshException {
        this.clientId = clientIdentification;
        this.serverId = serverIdentification;
        this.clientKexInit = clientKexInit;
        this.serverKexInit = serverKexInit;
        int retry = 3;
        do {
            if (retry == 0) {
                this.transport.disconnect(3, "Failed to generate key exchange value");
                throw new SshException("Key exchange failed to generate e value", 5);
            }
            try {
                this.initCrypto();
            }
            catch (Exception ex) {
                throw new SshException(ex, 16);
            }
            --retry;
            this.e = ((DHPublicKey)this.dhKeyPair.getPublic()).getY();
        } while (!DiffieHellmanGroups.verifyParameters(this.e, p));
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            this.dhKeyAgreement.init(this.dhKeyPair.getPrivate());
            msg.write(30);
            msg.writeBigInteger(this.e);
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Sending SSH_MSG_KEXDH_INIT", new Object[0]);
            }
            this.transport.sendMessage(msg.toByteArray(), true);
        }
        catch (IOException ex) {
            throw new SshException("Failed to write SSH_MSG_KEXDH_INIT to message buffer", 5);
        }
        catch (InvalidKeyException e1) {
            throw new SshException("JCE reported Diffie Hellman invalid key", 16);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
        byte[] tmp = this.transport.nextMessage(0L);
        if (tmp[0] != 31) {
            this.transport.disconnect(3, "Key exchange failed [id=" + tmp[0] + "]");
            throw new SshException("Key exchange failed [id=" + tmp[0] + "]", 5);
        }
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Received SSH_MSG_KEXDH_REPLY", new Object[0]);
        }
        ByteArrayReader bar = new ByteArrayReader(tmp, 1, tmp.length - 1);
        try {
            this.hostKey = bar.readBinaryString();
            this.f = bar.readBigInteger();
            this.signature = bar.readBinaryString();
            if (!DiffieHellmanGroups.verifyParameters(this.f, p)) {
                throw new SshException(String.format("Key exchange detected invalid f value %s", this.f.toString(16)), 3);
            }
            DHPublicKeySpec spec = new DHPublicKeySpec(this.f, p, g);
            DHPublicKey key = (DHPublicKey)this.dhKeyFactory.generatePublic(spec);
            this.dhKeyAgreement.doPhase(key, true);
            tmp = this.dhKeyAgreement.generateSecret();
            if ((tmp[0] & 0x80) == 128) {
                byte[] tmp2 = new byte[tmp.length + 1];
                System.arraycopy(tmp, 0, tmp2, 1, tmp.length);
                tmp = tmp2;
            }
            this.secret = new BigInteger(tmp);
            if (!DiffieHellmanGroups.verifyParameters(this.secret, p)) {
                throw new SshException(String.format("Key exchange detected invalid k value %s", this.e.toString(16)), 3);
            }
            this.calculateExchangeHash();
        }
        catch (IOException | InvalidKeyException | InvalidKeySpecException ex) {
            this.transport.error(log, "Key exchange failed", ex);
            throw new SshException("Failed to read SSH_MSG_KEXDH_REPLY", 5);
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public String getProvider() {
        if (this.dhKeyAgreement != null) {
            return this.dhKeyAgreement.getProvider().getName();
        }
        return "";
    }

    protected void calculateExchangeHash() throws SshException {
        Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance("SHA-1");
        hash.putString(this.clientId);
        hash.putString(this.serverId);
        hash.putInt(this.clientKexInit.length);
        hash.putBytes(this.clientKexInit);
        hash.putInt(this.serverKexInit.length);
        hash.putBytes(this.serverKexInit);
        hash.putInt(this.hostKey.length);
        hash.putBytes(this.hostKey);
        hash.putBigInteger(this.e);
        hash.putBigInteger(this.f);
        hash.putBigInteger(this.secret);
        this.exchangeHash = hash.doFinal();
    }

    @Override
    public String getAlgorithm() {
        return DIFFIE_HELLMAN_GROUP14_SHA1;
    }

    @Override
    public boolean isKeyExchangeMessage(int messageid) {
        switch (messageid) {
            case 30: 
            case 31: {
                return true;
            }
        }
        return false;
    }

    static {
        g = TWO = BigInteger.valueOf(2L);
        p = DiffieHellmanGroups.group14;
    }
}

