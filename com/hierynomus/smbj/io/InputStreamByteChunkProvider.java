/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.io;

import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.io.ByteChunkProvider;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamByteChunkProvider
extends ByteChunkProvider {
    private BufferedInputStream is;

    public InputStreamByteChunkProvider(InputStream is) {
        this.is = is instanceof BufferedInputStream ? (BufferedInputStream)is : new BufferedInputStream(is);
    }

    @Override
    protected int getChunk(byte[] chunk) throws IOException {
        int count;
        int read;
        if (this.is == null) {
            return -1;
        }
        for (count = 0; count < 65536 && (read = this.is.read(chunk, count, 65536 - count)) != -1; count += read) {
        }
        return count;
    }

    @Override
    public int bytesLeft() {
        try {
            if (this.is != null) {
                return this.is.available();
            }
            return -1;
        }
        catch (IOException e) {
            throw new SMBRuntimeException(e);
        }
    }

    @Override
    public boolean isAvailable() {
        return this.bytesLeft() > 0;
    }

    @Override
    public void close() throws IOException {
        if (this.is != null) {
            try {
                this.is.close();
            }
            finally {
                this.is = null;
            }
        }
    }
}

