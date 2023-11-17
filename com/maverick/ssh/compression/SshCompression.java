/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.compression;

import com.maverick.ssh.SecureComponent;
import com.maverick.ssh.components.SshComponent;
import java.io.IOException;

public interface SshCompression
extends SshComponent,
SecureComponent {
    public static final int INFLATER = 0;
    public static final int DEFLATER = 1;

    public void init(int var1, int var2);

    public boolean isDelayed();

    public byte[] compress(byte[] var1, int var2, int var3) throws IOException;

    public byte[] uncompress(byte[] var1, int var2, int var3) throws IOException;

    @Override
    public String getAlgorithm();
}

