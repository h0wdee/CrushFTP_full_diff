/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.CaptureInternal;

public class CaptureInfo {
    private static final int MAXPACKETLEN = 10260;
    private boolean isSMB = false;
    private boolean isReceiving = false;
    private byte[] packetBytes = new byte[10260];
    private BufferWriter packet = new BufferWriter(this.packetBytes, 0, false);
    private int dataLength = 0;
    private CaptureInternal.CaptureHeader captureHeader;

    public boolean isSMB() {
        return this.isSMB;
    }

    public void setSMB(boolean isSMB) {
        this.isSMB = isSMB;
    }

    public boolean isReceiving() {
        return this.isReceiving;
    }

    public void setReceiving(boolean isReceiving) {
        this.isReceiving = isReceiving;
    }

    public byte[] getPacketBytes() {
        return this.packetBytes;
    }

    public BufferWriter getPacket() {
        return this.packet;
    }

    public int getDataLength() {
        return this.dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public CaptureInternal.CaptureHeader getCaptureHeader() {
        return this.captureHeader;
    }

    public void setCaptureHeader(CaptureInternal.CaptureHeader captureHeader) {
        this.captureHeader = captureHeader;
    }

    public String toString() {
        return "CaptureInfo [dataLength=" + this.dataLength + ", isSMB=" + this.isSMB + ", isReceiving=" + this.isReceiving + ", packet=" + this.packet + ", captureHeader=" + this.captureHeader + "]";
    }
}

