/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;

public interface SmbSerializable {
    public void read(BufferReader var1) throws NqException;

    public int write(BufferWriter var1) throws NqException;
}

