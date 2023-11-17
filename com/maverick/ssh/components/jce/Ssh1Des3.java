/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.jce.Ssh1Des;
import java.io.IOException;

public class Ssh1Des3
extends SshCipher {
    Ssh1Des des1 = new Ssh1Des();
    Ssh1Des des2 = new Ssh1Des();
    Ssh1Des des3 = new Ssh1Des();
    int mode;

    public Ssh1Des3() throws IOException {
        super("3DES", SecurityLevel.WEAK);
    }

    @Override
    public int getBlockSize() {
        return 8;
    }

    @Override
    public String getAlgorithm() {
        return "3des";
    }

    @Override
    public void init(int mode, byte[] iv, byte[] keydata) throws IOException {
        byte[] key = new byte[8];
        this.mode = mode;
        System.arraycopy(keydata, 0, key, 0, 8);
        this.des1.init(mode == 0 ? 0 : 1, iv, key);
        System.arraycopy(keydata, 8, key, 0, 8);
        this.des2.init(mode == 0 ? 1 : 0, iv, key);
        System.arraycopy(keydata, 16, key, 0, 8);
        this.des3.init(mode == 0 ? 0 : 1, iv, key);
    }

    @Override
    public int getKeyLength() {
        return 24;
    }

    @Override
    public void transform(byte[] src, int start, byte[] dest, int offset, int len) throws IOException {
        if (this.mode == 0) {
            this.des1.transform(src, start, dest, offset, len);
            this.des2.transform(dest, offset, dest, offset, len);
            this.des3.transform(dest, offset, dest, offset, len);
        } else {
            this.des3.transform(src, start, dest, offset, len);
            this.des2.transform(dest, offset, dest, offset, len);
            this.des1.transform(dest, offset, dest, offset, len);
        }
    }

    @Override
    public String getProviderName() {
        return "None";
    }
}

