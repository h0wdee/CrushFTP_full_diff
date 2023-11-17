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
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshKeyExchange;
import com.maverick.ssh.components.jce.ECUtils;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh2.SshKeyExchangeClient;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import javax.crypto.KeyAgreement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffieHellmanEcdh
extends SshKeyExchangeClient
implements SshKeyExchange {
    private static final Logger log = LoggerFactory.getLogger(DiffieHellmanEcdh.class);
    public static final int SSH_MSG_KEX_ECDH_INIT = 30;
    public static final int SSH_MSG_KEX_ECDH_REPLY = 31;
    String name;
    String curve;
    byte[] Q_S;
    byte[] Q_C;
    String clientId;
    String serverId;
    byte[] clientKexInit;
    byte[] serverKexInit;
    KeyAgreement keyAgreement;
    KeyPairGenerator keyGen;
    KeyPair keyPair;

    protected DiffieHellmanEcdh(String name, String curve, String hashAlgorithm, int priority) {
        super(hashAlgorithm, SecurityLevel.STRONG, priority);
        this.name = name;
        this.curve = curve;
    }

    @Override
    public String getAlgorithm() {
        return this.name;
    }

    private void initCrypto() throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, SshException {
        ComponentManager.getInstance().supportedDigests().getInstance(this.getHashAlgorithm());
        this.keyGen = JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()) == null ? KeyPairGenerator.getInstance(JCEProvider.getECDSAAlgorithmName()) : KeyPairGenerator.getInstance(JCEProvider.getECDSAAlgorithmName(), JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()));
        this.keyAgreement = JCEProvider.getProviderForAlgorithm("ECDH") == null ? KeyAgreement.getInstance("ECDH") : KeyAgreement.getInstance("ECDH", JCEProvider.getProviderForAlgorithm("ECDH"));
        ECGenParameterSpec namedSpec = new ECGenParameterSpec(this.curve);
        this.keyGen.initialize(namedSpec);
        this.keyPair = this.keyGen.generateKeyPair();
        this.keyAgreement.init(this.keyPair.getPrivate());
    }

    @Override
    public void test() {
        try {
            this.initCrypto();
        }
        catch (Throwable e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void performClientExchange(String clientId, String serverId, byte[] clientKexInit, byte[] serverKexInit) throws SshException {
        this.clientId = clientId;
        this.serverId = serverId;
        this.clientKexInit = clientKexInit;
        this.serverKexInit = serverKexInit;
        try {
            this.initCrypto();
            ECPublicKey ec = (ECPublicKey)this.keyPair.getPublic();
            this.Q_C = ECUtils.toByteArray(ec.getW(), ec.getParams().getCurve());
            try (ByteArrayWriter msg = new ByteArrayWriter();){
                msg.write(30);
                msg.writeBinaryString(this.Q_C);
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Sending SSH_MSG_KEX_ECDH_INIT", new Object[0]);
                }
                this.transport.sendMessage(msg.toByteArray(), true);
            }
            byte[] resp = this.transport.nextMessage(0L);
            if (resp[0] != 31) {
                throw new SshException("Expected SSH_MSG_KEX_ECDH_REPLY but got message id " + resp[0], 9);
            }
            try (ByteArrayReader reply = new ByteArrayReader(resp, 1, resp.length - 1);){
                this.hostKey = reply.readBinaryString();
                this.Q_S = reply.readBinaryString();
                this.signature = reply.readBinaryString();
                this.keyAgreement.doPhase(ECUtils.decodeKey(this.Q_S, this.curve), true);
                byte[] tmp = this.keyAgreement.generateSecret();
                if ((tmp[0] & 0x80) == 128) {
                    byte[] tmp2 = new byte[tmp.length + 1];
                    System.arraycopy(tmp, 0, tmp2, 1, tmp.length);
                    tmp = tmp2;
                }
                this.secret = new BigInteger(tmp);
            }
            this.calculateExchangeHash();
        }
        catch (Exception e) {
            this.transport.error(log, "Key exchange failed", e);
            throw new SshException("Failed to process key exchange", 57354, e);
        }
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

    protected void calculateExchangeHash() throws SshException {
        Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance(this.getHashAlgorithm());
        hash.putString(this.clientId);
        hash.putString(this.serverId);
        hash.putInt(this.clientKexInit.length);
        hash.putBytes(this.clientKexInit);
        hash.putInt(this.serverKexInit.length);
        hash.putBytes(this.serverKexInit);
        hash.putInt(this.hostKey.length);
        hash.putBytes(this.hostKey);
        hash.putInt(this.Q_C.length);
        hash.putBytes(this.Q_C);
        hash.putInt(this.Q_S.length);
        hash.putBytes(this.Q_S);
        hash.putBigInteger(this.secret);
        this.exchangeHash = hash.doFinal();
    }

    @Override
    public String getProvider() {
        return this.keyGen.getProvider().getName();
    }
}

