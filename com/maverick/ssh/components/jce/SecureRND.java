/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshSecureRandomGenerator;
import com.maverick.ssh.components.jce.JCEProvider;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecureRND
implements SshSecureRandomGenerator {
    SecureRandom rnd = JCEProvider.getSecureRandom();

    @Override
    public void nextBytes(byte[] bytes) {
        this.rnd.nextBytes(bytes);
    }

    @Override
    public void nextBytes(byte[] bytes, int off, int len) throws SshException {
        try {
            byte[] tmp = new byte[len];
            this.rnd.nextBytes(tmp);
            System.arraycopy(tmp, 0, bytes, off, len);
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new SshException("ArrayIndexOutOfBoundsException: Index " + off + " on actual array length " + bytes.length + " with len=" + len, 5);
        }
    }

    @Override
    public int nextInt() {
        return this.rnd.nextInt();
    }
}

