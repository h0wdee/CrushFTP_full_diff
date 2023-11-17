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
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.jce.AbstractKeyExchange;
import com.maverick.ssh.components.jce.JCEComponentManager;
import com.maverick.ssh2.SshKeyExchangeClient;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rsa1024Sha1
extends SshKeyExchangeClient
implements AbstractKeyExchange {
    private static Logger log = LoggerFactory.getLogger(Rsa1024Sha1.class);
    public static final String RSA_1024_SHA1 = "rsa1024-sha1";
    static final int SSH_MSG_KEXRSA_PUBKEY = 30;
    static final int SSH_MSG_KEXRSA_SECRET = 31;
    static final int SSH_MSG_KEXRSA_DONE = 32;
    Cipher cipher;
    byte[] tk;
    byte[] encryptedSecret;
    private String clientId;
    private String serverId;
    private byte[] clientKexInit;
    private byte[] serverKexInit;

    public Rsa1024Sha1() {
        super("SHA-1", SecurityLevel.WEAK, 99);
    }

    @Override
    public String getAlgorithm() {
        return RSA_1024_SHA1;
    }

    @Override
    public String getProvider() {
        return "";
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

    void initCrypto() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding");
    }

    @Override
    public void performClientExchange(String clientIdentification, String serverIdentification, byte[] clientKexInit, byte[] serverKexInit) throws SshException {
        this.clientId = clientIdentification;
        this.serverId = serverIdentification;
        this.clientKexInit = clientKexInit;
        this.serverKexInit = serverKexInit;
        try {
            this.initCrypto();
        }
        catch (Exception ex) {
            throw new SshException(ex, 16);
        }
        byte[] tmp = this.transport.nextMessage(0L);
        if (tmp[0] != 30) {
            this.transport.disconnect(3, "Key exchange failed");
            throw new SshException("Key exchange failed [id=" + tmp[0] + "]", 5);
        }
        byte[] s = new byte[80];
        try (ByteArrayWriter msg = new ByteArrayWriter();){
            try (ByteArrayReader r = new ByteArrayReader(tmp);){
                r.skip(1L);
                this.hostKey = r.readBinaryString();
                this.tk = r.readBinaryString();
            }
            SshRsaPublicKey key = (SshRsaPublicKey)SshPublicKeyFileFactory.decodeSSH2PublicKey(this.tk);
            JCEComponentManager.getSecureRandom().nextBytes(s);
            this.cipher.init(1, key.getJCEPublicKey());
            try (ByteArrayWriter w = new ByteArrayWriter();){
                w.writeBinaryString(s);
                msg.write(31);
                this.encryptedSecret = this.cipher.doFinal(w.toByteArray());
                msg.writeBinaryString(this.encryptedSecret);
            }
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Sending SSH_MSG_KEXRSA_SECRET", new Object[0]);
            }
            this.transport.sendMessage(msg.toByteArray(), true);
        }
        catch (Throwable ex) {
            throw new SshException("Failed to write SSH_MSG_KEXRSA_SECRET to message buffer", 5);
        }
        tmp = this.transport.nextMessage(0L);
        if (tmp[0] != 32) {
            this.transport.disconnect(3, "Key exchange failed");
            throw new SshException("Key exchange failed [id=" + tmp[0] + "]", 5);
        }
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Received SSH_MSG_KEXRSA_DONE", new Object[0]);
        }
        ByteArrayReader bar = new ByteArrayReader(tmp, 1, tmp.length - 1);
        try {
            this.signature = bar.readBinaryString();
            this.secret = new BigInteger(s);
            this.calculateExchangeHash();
        }
        catch (IOException ex) {
            log.error("Key exchange failed", (Throwable)ex);
            throw new SshException("Failed to read SSH_MSG_KEXRSA_DONE", 5);
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
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
        hash.putInt(this.tk.length);
        hash.putBytes(this.tk);
        hash.putInt(this.encryptedSecret.length);
        hash.putBytes(this.encryptedSecret);
        hash.putBigInteger(this.secret);
        this.exchangeHash = hash.doFinal();
    }

    @Override
    public boolean isKeyExchangeMessage(int messageid) {
        switch (messageid) {
            case 30: 
            case 31: 
            case 32: {
                return true;
            }
        }
        return false;
    }
}

