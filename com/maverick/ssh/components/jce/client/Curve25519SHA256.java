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
import com.maverick.ssh.components.Curve25519;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshKeyExchange;
import com.maverick.ssh.components.jce.JCEComponentManager;
import com.maverick.ssh2.SshKeyExchangeClient;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Curve25519SHA256
extends SshKeyExchangeClient
implements SshKeyExchange {
    private static final Logger log = LoggerFactory.getLogger(Curve25519SHA256.class);
    public static final int SSH_MSG_KEX_ECDH_INIT = 30;
    public static final int SSH_MSG_KEX_ECDH_REPLY = 31;
    String name;
    byte[] f;
    byte[] privateKey;
    byte[] e;
    String clientId;
    String serverId;
    byte[] clientKexInit;
    byte[] serverKexInit;

    public Curve25519SHA256() {
        this("curve25519-sha256");
    }

    public Curve25519SHA256(String name) {
        super("SHA-256", SecurityLevel.PARANOID, 99);
        this.name = name;
    }

    @Override
    public String getAlgorithm() {
        return this.name;
    }

    private void initCrypto() throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, SshException {
        this.e = new byte[32];
        this.privateKey = new byte[32];
        JCEComponentManager.getSecureRandom().nextBytes(this.privateKey);
        Curve25519.keygen(this.e, null, this.privateKey);
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
            try (ByteArrayWriter msg = new ByteArrayWriter();){
                msg.write(30);
                msg.writeBinaryString(this.e);
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
                this.f = reply.readBinaryString();
                this.signature = reply.readBinaryString();
                byte[] k = new byte[32];
                Curve25519.curve(k, this.privateKey, this.f);
                this.secret = new BigInteger(1, k);
            }
            this.calculateExchangeHash();
        }
        catch (Exception e) {
            this.transport.error(log, "Key exchange failed", e);
            throw new SshException("Failed to process key exchange", 5, e);
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
        hash.putInt(this.e.length);
        hash.putBytes(this.e);
        hash.putInt(this.f.length);
        hash.putBytes(this.f);
        hash.putBigInteger(this.secret);
        this.exchangeHash = hash.doFinal();
    }

    @Override
    public String getProvider() {
        return "JADAPTIVE";
    }
}

