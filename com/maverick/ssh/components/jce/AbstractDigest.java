/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.util.ByteArrayWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AbstractDigest
implements Digest {
    MessageDigest digest;
    final String jceAlgorithm;
    final SecurityLevel securityLevel;
    final int priority;

    public AbstractDigest(String jceAlgorithm, SecurityLevel securityLevel, int priority) throws NoSuchAlgorithmException {
        this.securityLevel = securityLevel;
        this.priority = priority;
        this.jceAlgorithm = jceAlgorithm;
        this.digest = JCEProvider.getProviderForAlgorithm(jceAlgorithm) == null ? MessageDigest.getInstance(jceAlgorithm) : MessageDigest.getInstance(jceAlgorithm, JCEProvider.getProviderForAlgorithm(jceAlgorithm));
    }

    @Override
    public byte[] doFinal() {
        return this.digest.digest();
    }

    @Override
    public void putBigInteger(BigInteger bi) {
        byte[] data = bi.toByteArray();
        this.putInt(data.length);
        this.putBytes(data);
    }

    @Override
    public void putByte(byte b) {
        this.digest.update(b);
    }

    @Override
    public void putBytes(byte[] data) {
        this.digest.update(data, 0, data.length);
    }

    @Override
    public void putBytes(byte[] data, int offset, int len) {
        this.digest.update(data, offset, len);
    }

    @Override
    public void putInt(int i) {
        this.putBytes(ByteArrayWriter.encodeInt(i));
    }

    @Override
    public void putString(String str) {
        this.putInt(str.length());
        this.putBytes(str.getBytes());
    }

    @Override
    public void reset() {
        this.digest.reset();
    }

    public String getProvider() {
        if (this.digest == null) {
            return null;
        }
        return this.digest.getProvider().getName();
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return this.securityLevel;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public String getAlgorithm() {
        return this.jceAlgorithm;
    }
}

