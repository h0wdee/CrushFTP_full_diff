/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jcraft.jzlib.ZStream
 */
package com.sshtools.zlib;

import com.jcraft.jzlib.ZStream;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.compression.SshCompression;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ZLibCompression
implements SshCompression {
    private static final int BUF_SIZE = 65535;
    ByteArrayOutputStream compressOut = new ByteArrayOutputStream(65535);
    ByteArrayOutputStream uncompressOut = new ByteArrayOutputStream(65535);
    private ZStream stream;
    private byte[] inflated_buf = new byte[65535];
    private byte[] tmpbuf = new byte[65535];

    public ZLibCompression() {
        this.stream = new ZStream();
    }

    @Override
    public String getAlgorithm() {
        return "zlib";
    }

    @Override
    public void init(int type, int level) {
        if (type == 1) {
            this.stream.deflateInit(level);
        } else if (type == 0) {
            this.stream.inflateInit();
        }
    }

    @Override
    public byte[] compress(byte[] buf, int start, int len) throws IOException {
        this.compressOut.reset();
        this.stream.next_in = buf;
        this.stream.next_in_index = start;
        this.stream.avail_in = len - start;
        do {
            this.stream.next_out = this.tmpbuf;
            this.stream.next_out_index = 0;
            this.stream.avail_out = 65535;
            int status = this.stream.deflate(1);
            switch (status) {
                case 0: {
                    this.compressOut.write(this.tmpbuf, 0, 65535 - this.stream.avail_out);
                    break;
                }
                default: {
                    throw new IOException("compress: deflate returnd " + status);
                }
            }
        } while (this.stream.avail_out == 0);
        return this.compressOut.toByteArray();
    }

    @Override
    public byte[] uncompress(byte[] buffer, int start, int length) throws IOException {
        int status;
        this.uncompressOut.reset();
        this.stream.next_in = buffer;
        this.stream.next_in_index = start;
        this.stream.avail_in = length;
        block4: while (true) {
            this.stream.next_out = this.inflated_buf;
            this.stream.next_out_index = 0;
            this.stream.avail_out = 65535;
            status = this.stream.inflate(1);
            switch (status) {
                case 0: {
                    this.uncompressOut.write(this.inflated_buf, 0, 65535 - this.stream.avail_out);
                    continue block4;
                }
                case -5: {
                    return this.uncompressOut.toByteArray();
                }
            }
            break;
        }
        throw new IOException("uncompress: inflate returnd " + status);
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.NONE;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isDelayed() {
        return false;
    }
}

