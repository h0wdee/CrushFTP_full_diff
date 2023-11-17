/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshHmac;
import com.maverick.ssh.components.jce.JCEProvider;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public abstract class AbstractHmac
implements SshHmac {
    protected Mac mac;
    protected int macSize;
    protected int macLength;
    protected String jceAlgorithm;
    private final SecurityLevel securityLevel;
    int priority;

    public AbstractHmac(String jceAlgorithm, int macLength, SecurityLevel securityLevel, int priority) {
        this(jceAlgorithm, macLength, macLength, securityLevel, priority);
    }

    public AbstractHmac(String jceAlgorithm, int macSize, int outputLength, SecurityLevel securityLevel, int priority) {
        this.jceAlgorithm = jceAlgorithm;
        this.macSize = macSize;
        this.macLength = outputLength;
        this.securityLevel = securityLevel;
        this.priority = priority;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return this.securityLevel;
    }

    @Override
    public int getPriority() {
        return this.securityLevel.ordinal() * 1000 + (this.isETM() && this.securityLevel.ordinal() > SecurityLevel.WEAK.ordinal() ? 100 : this.priority);
    }

    @Override
    public void generate(long sequenceNo, byte[] data, int offset, int len, byte[] output, int start) {
        byte[] sequenceBytes = new byte[]{(byte)(sequenceNo >> 24), (byte)(sequenceNo >> 16), (byte)(sequenceNo >> 8), (byte)(sequenceNo >> 0)};
        this.mac.update(sequenceBytes);
        this.mac.update(data, offset, len);
        byte[] tmp = this.mac.doFinal();
        System.arraycopy(tmp, 0, output, start, this.macLength);
    }

    @Override
    public void update(byte[] b) {
        this.mac.update(b);
    }

    @Override
    public byte[] doFinal() {
        return this.mac.doFinal();
    }

    @Override
    public abstract String getAlgorithm();

    public String getProvider() {
        if (this.mac == null) {
            return null;
        }
        return this.mac.getProvider().getName();
    }

    @Override
    public int getMacSize() {
        return this.macSize;
    }

    @Override
    public int getMacLength() {
        return this.macLength;
    }

    @Override
    public boolean isETM() {
        return false;
    }

    @Override
    public void init(byte[] keydata) throws SshException {
        try {
            this.mac = JCEProvider.getProviderForAlgorithm(this.jceAlgorithm) == null ? Mac.getInstance(this.jceAlgorithm) : Mac.getInstance(this.jceAlgorithm, JCEProvider.getProviderForAlgorithm(this.jceAlgorithm));
            byte[] key = new byte[this.macSize];
            System.arraycopy(keydata, 0, key, 0, key.length);
            SecretKeySpec keyspec = new SecretKeySpec(key, this.jceAlgorithm);
            this.mac.init(keyspec);
        }
        catch (Throwable t) {
            throw new SshException(t);
        }
    }

    @Override
    public boolean verify(long sequenceNo, byte[] data, int start, int len, byte[] mac, int offset) {
        int length = this.getMacLength();
        byte[] generated = new byte[length];
        this.generate(sequenceNo, data, start, len, generated, 0);
        for (int i = 0; i < generated.length; ++i) {
            if (mac[i + offset] == generated[i]) continue;
            return false;
        }
        return true;
    }
}

