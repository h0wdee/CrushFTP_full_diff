/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.Buffer;

public abstract class SmbPacket {
    public abstract int getSize();

    public abstract int writeBuffer(Buffer var1);
}

