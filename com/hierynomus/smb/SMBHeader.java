/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smb;

import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;

public interface SMBHeader {
    public void writeTo(SMBBuffer var1);

    public void readFrom(Buffer<?> var1) throws Buffer.BufferException;
}

