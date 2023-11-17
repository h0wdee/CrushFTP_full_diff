/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.transport.tcp.async;

import com.hierynomus.protocol.Packet;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketBufferReader {
    private static final int NO_PACKET_LENGTH = -1;
    private static final int HEADER_SIZE = 4;
    private static final int READ_BUFFER_CAPACITY = 9000;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(9000);
    private byte[] currentPacketBytes;
    private int currentPacketLength = -1;
    private int currentPacketOffset = 0;

    public <P extends Packet<?>> PacketBufferReader() {
        this.readBuffer.order(ByteOrder.BIG_ENDIAN);
    }

    public byte[] readNext() {
        this.readBuffer.flip();
        byte[] bytes = null;
        if (this.isAwaitingHeader() && this.isHeaderAvailable()) {
            this.currentPacketLength = this.readPacketHeader();
            this.currentPacketBytes = new byte[this.currentPacketLength];
            bytes = this.readPacketBody();
        } else if (!this.isAwaitingHeader()) {
            bytes = this.readPacketBody();
        }
        this.readBuffer.compact();
        if (bytes != null) {
            this.currentPacketBytes = null;
            this.currentPacketOffset = 0;
            this.currentPacketLength = -1;
        }
        return bytes;
    }

    private int readPacketHeader() {
        return this.readBuffer.getInt() & 0xFFFFFF;
    }

    private boolean isHeaderAvailable() {
        return this.readBuffer.remaining() >= 4;
    }

    public ByteBuffer getBuffer() {
        return this.readBuffer;
    }

    private boolean isAwaitingHeader() {
        return this.currentPacketLength == -1;
    }

    private byte[] readPacketBody() {
        int length = this.currentPacketLength - this.currentPacketOffset;
        if (length > this.readBuffer.remaining()) {
            length = this.readBuffer.remaining();
        }
        this.readBuffer.get(this.currentPacketBytes, this.currentPacketOffset, length);
        this.currentPacketOffset += length;
        if (this.currentPacketOffset == this.currentPacketLength) {
            return this.currentPacketBytes;
        }
        return null;
    }
}

