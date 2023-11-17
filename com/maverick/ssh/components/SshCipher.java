/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SecureComponent;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.SshComponent;
import java.io.IOException;

public abstract class SshCipher
implements SshComponent,
SecureComponent {
    String algorithm;
    SecurityLevel securityLevel;
    public static final int ENCRYPT_MODE = 0;
    public static final int DECRYPT_MODE = 1;

    public SshCipher(String algorithm, SecurityLevel securityLevel) {
        this.algorithm = algorithm;
        this.securityLevel = securityLevel;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return this.securityLevel;
    }

    @Override
    public int getPriority() {
        return this.securityLevel.ordinal() * 1000 + this.getKeyLength();
    }

    @Override
    public String getAlgorithm() {
        return this.algorithm;
    }

    public abstract int getBlockSize();

    public abstract int getKeyLength();

    public abstract void init(int var1, byte[] var2, byte[] var3) throws IOException;

    public void transform(byte[] data) throws IOException {
        this.transform(data, 0, data, 0, data.length);
    }

    public abstract void transform(byte[] var1, int var2, byte[] var3, int var4, int var5) throws IOException;

    public boolean isMAC() {
        return false;
    }

    public int getMacLength() {
        return 0;
    }

    public abstract String getProviderName();
}

