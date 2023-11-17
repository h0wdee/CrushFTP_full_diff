/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SecureComponent;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshComponent;

public interface SshHmac
extends SshComponent,
SecureComponent {
    public int getMacSize();

    public int getMacLength();

    public void generate(long var1, byte[] var3, int var4, int var5, byte[] var6, int var7);

    public void init(byte[] var1) throws SshException;

    public boolean verify(long var1, byte[] var3, int var4, int var5, byte[] var6, int var7);

    public void update(byte[] var1);

    public byte[] doFinal();

    @Override
    public String getAlgorithm();

    public boolean isETM();
}

