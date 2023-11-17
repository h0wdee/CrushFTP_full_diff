/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshKeyExchange;
import com.maverick.ssh2.AbstractClientTransport;
import java.math.BigInteger;

public abstract class SshKeyExchangeClient
implements SshKeyExchange {
    protected final String hashAlgorithm;
    private final SecurityLevel securityLevel;
    private final int priority;
    protected BigInteger secret;
    protected byte[] exchangeHash;
    protected byte[] hostKey;
    protected byte[] signature;
    protected AbstractClientTransport transport;

    protected SshKeyExchangeClient(String hashAlgorithm, SecurityLevel securityLevel, int priority) {
        this.hashAlgorithm = hashAlgorithm;
        this.securityLevel = securityLevel;
        this.priority = priority;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return this.securityLevel;
    }

    @Override
    public int getPriority() {
        return this.securityLevel.ordinal() * 1000 + this.priority * this.securityLevel.ordinal();
    }

    @Override
    public abstract String getAlgorithm();

    public byte[] getExchangeHash() {
        return this.exchangeHash;
    }

    public byte[] getHostKey() {
        return this.hostKey;
    }

    public BigInteger getSecret() {
        return this.secret;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    @Override
    public String getHashAlgorithm() {
        return this.hashAlgorithm;
    }

    public void init(AbstractClientTransport transport, boolean ignoreFirstPacket) {
        this.transport = transport;
    }

    public abstract void performClientExchange(String var1, String var2, byte[] var3, byte[] var4) throws SshException;

    public abstract boolean isKeyExchangeMessage(int var1);

    public void reset() {
        this.exchangeHash = null;
        this.hostKey = null;
        this.signature = null;
        this.secret = null;
    }
}

