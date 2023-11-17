/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SecureComponent;
import java.math.BigInteger;

public interface Digest
extends SecureComponent {
    public void putBigInteger(BigInteger var1);

    public void putByte(byte var1);

    public void putBytes(byte[] var1);

    public void putBytes(byte[] var1, int var2, int var3);

    public void putInt(int var1);

    public void putString(String var1);

    public void reset();

    public byte[] doFinal();
}

