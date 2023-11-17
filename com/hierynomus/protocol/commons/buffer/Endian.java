/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.commons.buffer;

import com.hierynomus.protocol.commons.Charsets;
import com.hierynomus.protocol.commons.buffer.Buffer;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public abstract class Endian {
    private static final byte[] NULL_TERMINATOR = new byte[]{0, 0};
    public static final Endian LE = new Little();
    public static final Endian BE = new Big();

    <T extends Buffer<T>> String readNullTerminatedUtf16String(Buffer<T> buffer, Charset charset) throws Buffer.BufferException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[2];
        buffer.readRawBytes(bytes);
        while (bytes[0] != 0 || bytes[1] != 0) {
            baos.write(bytes, 0, 2);
            buffer.readRawBytes(bytes);
        }
        return new String(baos.toByteArray(), charset);
    }

    <T extends Buffer<T>> String readUtf16String(Buffer<T> buffer, int length, Charset charset) throws Buffer.BufferException {
        byte[] stringBytes = new byte[length * 2];
        buffer.readRawBytes(stringBytes);
        return new String(stringBytes, charset);
    }

    <T extends Buffer<T>> void writeNullTerminatedUtf16String(Buffer<T> buffer, String string) {
        this.writeUtf16String(buffer, string);
        buffer.putRawBytes(NULL_TERMINATOR);
    }

    public abstract <T extends Buffer<T>> void writeUInt16(Buffer<T> var1, int var2);

    public abstract <T extends Buffer<T>> int readUInt16(Buffer<T> var1) throws Buffer.BufferException;

    public abstract <T extends Buffer<T>> void writeUInt24(Buffer<T> var1, int var2);

    public abstract <T extends Buffer<T>> int readUInt24(Buffer<T> var1) throws Buffer.BufferException;

    public abstract <T extends Buffer<T>> void writeUInt32(Buffer<T> var1, long var2);

    public abstract <T extends Buffer<T>> long readUInt32(Buffer<T> var1) throws Buffer.BufferException;

    public abstract <T extends Buffer<T>> void writeUInt64(Buffer<T> var1, long var2);

    public abstract <T extends Buffer<T>> long readUInt64(Buffer<T> var1) throws Buffer.BufferException;

    public abstract <T extends Buffer<T>> void writeLong(Buffer<T> var1, long var2);

    public abstract <T extends Buffer<T>> long readLong(Buffer<T> var1) throws Buffer.BufferException;

    public abstract <T extends Buffer<T>> void writeUtf16String(Buffer<T> var1, String var2);

    public abstract <T extends Buffer<T>> String readUtf16String(Buffer<T> var1, int var2) throws Buffer.BufferException;

    public abstract <T extends Buffer<T>> String readNullTerminatedUtf16String(Buffer<T> var1) throws Buffer.BufferException;

    private static class Little
    extends Endian {
        private Little() {
        }

        @Override
        public <T extends Buffer<T>> void writeUInt16(Buffer<T> buffer, int uint16) {
            if (uint16 < 0 || uint16 > 65535) {
                throw new IllegalArgumentException("Invalid uint16 value: " + uint16);
            }
            buffer.putRawBytes(new byte[]{(byte)uint16, (byte)(uint16 >> 8)});
        }

        @Override
        public <T extends Buffer<T>> int readUInt16(Buffer<T> buffer) throws Buffer.BufferException {
            byte[] b = buffer.readRawBytes(2);
            return b[0] & 0xFF | b[1] << 8 & 0xFF00;
        }

        @Override
        public <T extends Buffer<T>> void writeUInt24(Buffer<T> buffer, int uint24) {
            if (uint24 < 0 || uint24 > 0xFFFFFF) {
                throw new IllegalArgumentException("Invalid uint24 value: " + uint24);
            }
            buffer.putRawBytes(new byte[]{(byte)uint24, (byte)(uint24 >> 8), (byte)(uint24 >> 16)});
        }

        @Override
        public <T extends Buffer<T>> int readUInt24(Buffer<T> buffer) throws Buffer.BufferException {
            byte[] b = buffer.readRawBytes(3);
            return b[0] & 0xFF | b[1] << 8 & 0xFF00 | b[2] << 16 & 0xFF0000;
        }

        @Override
        public <T extends Buffer<T>> void writeUInt32(Buffer<T> buffer, long uint32) {
            if (uint32 < 0L || uint32 > 0xFFFFFFFFL) {
                throw new IllegalArgumentException("Invalid uint32 value: " + uint32);
            }
            buffer.putRawBytes(new byte[]{(byte)uint32, (byte)(uint32 >> 8), (byte)(uint32 >> 16), (byte)(uint32 >> 24)});
        }

        @Override
        public <T extends Buffer<T>> long readUInt32(Buffer<T> buffer) throws Buffer.BufferException {
            byte[] b = buffer.readRawBytes(4);
            return (long)b[0] & 0xFFL | (long)(b[1] << 8) & 0xFF00L | (long)(b[2] << 16) & 0xFF0000L | (long)(b[3] << 24) & 0xFF000000L;
        }

        @Override
        public <T extends Buffer<T>> void writeUInt64(Buffer<T> buffer, long uint64) {
            if (uint64 < 0L) {
                throw new IllegalArgumentException("Invalid uint64 value: " + uint64);
            }
            this.writeLong(buffer, uint64);
        }

        @Override
        public <T extends Buffer<T>> long readUInt64(Buffer<T> buffer) throws Buffer.BufferException {
            long uint64 = (this.readUInt32(buffer) & 0xFFFFFFFFL) + (this.readUInt32(buffer) << 32);
            if (uint64 < 0L) {
                throw new Buffer.BufferException("Cannot handle values > 9223372036854775807");
            }
            return uint64;
        }

        @Override
        public <T extends Buffer<T>> void writeLong(Buffer<T> buffer, long longVal) {
            buffer.putRawBytes(new byte[]{(byte)longVal, (byte)(longVal >> 8), (byte)(longVal >> 16), (byte)(longVal >> 24), (byte)(longVal >> 32), (byte)(longVal >> 40), (byte)(longVal >> 48), (byte)(longVal >> 56)});
        }

        @Override
        public <T extends Buffer<T>> long readLong(Buffer<T> buffer) throws Buffer.BufferException {
            long result = 0L;
            byte[] bytes = buffer.readRawBytes(8);
            for (int i = 7; i >= 0; --i) {
                result <<= 8;
                result |= (long)(bytes[i] & 0xFF);
            }
            return result;
        }

        @Override
        public <T extends Buffer<T>> String readUtf16String(Buffer<T> buffer, int length) throws Buffer.BufferException {
            return this.readUtf16String(buffer, length, Charsets.UTF_16LE);
        }

        @Override
        public <T extends Buffer<T>> String readNullTerminatedUtf16String(Buffer<T> buffer) throws Buffer.BufferException {
            return this.readNullTerminatedUtf16String(buffer, Charsets.UTF_16LE);
        }

        @Override
        public <T extends Buffer<T>> void writeUtf16String(Buffer<T> buffer, String string) {
            byte[] bytes = string.getBytes(Charsets.UTF_16LE);
            buffer.putRawBytes(bytes);
        }

        public String toString() {
            return "little endian";
        }
    }

    private static class Big
    extends Endian {
        private Big() {
        }

        @Override
        public <T extends Buffer<T>> void writeUInt16(Buffer<T> buffer, int uint16) {
            if (uint16 < 0 || uint16 > 65535) {
                throw new IllegalArgumentException("Invalid uint16 value: " + uint16);
            }
            buffer.putRawBytes(new byte[]{(byte)(uint16 >> 8), (byte)uint16});
        }

        @Override
        public <T extends Buffer<T>> int readUInt16(Buffer<T> buffer) throws Buffer.BufferException {
            byte[] b = buffer.readRawBytes(2);
            return b[0] << 8 & 0xFF00 | b[1] & 0xFF;
        }

        @Override
        public <T extends Buffer<T>> void writeUInt24(Buffer<T> buffer, int uint24) {
            if (uint24 < 0 || uint24 > 0xFFFFFF) {
                throw new IllegalArgumentException("Invalid uint24 value: " + uint24);
            }
            buffer.putRawBytes(new byte[]{(byte)(uint24 >> 16), (byte)(uint24 >> 8), (byte)uint24});
        }

        @Override
        public <T extends Buffer<T>> int readUInt24(Buffer<T> buffer) throws Buffer.BufferException {
            byte[] b = buffer.readRawBytes(3);
            return b[0] << 16 & 0xFF0000 | b[1] << 8 & 0xFF00 | b[2] & 0xFF;
        }

        @Override
        public <T extends Buffer<T>> void writeUInt32(Buffer<T> buffer, long uint32) {
            if (uint32 < 0L || uint32 > 0xFFFFFFFFL) {
                throw new IllegalArgumentException("Invalid uint32 value: " + uint32);
            }
            buffer.putRawBytes(new byte[]{(byte)(uint32 >> 24), (byte)(uint32 >> 16), (byte)(uint32 >> 8), (byte)uint32});
        }

        @Override
        public <T extends Buffer<T>> long readUInt32(Buffer<T> buffer) throws Buffer.BufferException {
            byte[] b = buffer.readRawBytes(4);
            return (long)(b[0] << 24) & 0xFF000000L | (long)(b[1] << 16) & 0xFF0000L | (long)(b[2] << 8) & 0xFF00L | (long)b[3] & 0xFFL;
        }

        @Override
        public <T extends Buffer<T>> void writeUInt64(Buffer<T> buffer, long uint64) {
            if (uint64 < 0L) {
                throw new IllegalArgumentException("Invalid uint64 value: " + uint64);
            }
            this.writeLong(buffer, uint64);
        }

        @Override
        public <T extends Buffer<T>> long readUInt64(Buffer<T> buffer) throws Buffer.BufferException {
            long uint64 = (this.readUInt32(buffer) << 32) + (this.readUInt32(buffer) & 0xFFFFFFFFL);
            if (uint64 < 0L) {
                throw new Buffer.BufferException("Cannot handle values > 9223372036854775807");
            }
            return uint64;
        }

        @Override
        public <T extends Buffer<T>> void writeLong(Buffer<T> buffer, long longVal) {
            buffer.putRawBytes(new byte[]{(byte)(longVal >> 56), (byte)(longVal >> 48), (byte)(longVal >> 40), (byte)(longVal >> 32), (byte)(longVal >> 24), (byte)(longVal >> 16), (byte)(longVal >> 8), (byte)longVal});
        }

        @Override
        public <T extends Buffer<T>> long readLong(Buffer<T> buffer) throws Buffer.BufferException {
            long result = 0L;
            byte[] b = buffer.readRawBytes(8);
            for (int i = 0; i < 8; ++i) {
                result <<= 8;
                result |= (long)(b[i] & 0xFF);
            }
            return result;
        }

        @Override
        public <T extends Buffer<T>> String readUtf16String(Buffer<T> buffer, int length) throws Buffer.BufferException {
            return this.readUtf16String(buffer, length, Charsets.UTF_16BE);
        }

        @Override
        public <T extends Buffer<T>> String readNullTerminatedUtf16String(Buffer<T> buffer) throws Buffer.BufferException {
            return this.readNullTerminatedUtf16String(buffer, Charsets.UTF_16BE);
        }

        @Override
        public <T extends Buffer<T>> void writeUtf16String(Buffer<T> buffer, String string) {
            byte[] bytes = string.getBytes(Charsets.UTF_16BE);
            buffer.putRawBytes(bytes);
        }

        public String toString() {
            return "big endian";
        }
    }
}

