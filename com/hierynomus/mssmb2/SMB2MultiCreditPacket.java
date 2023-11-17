/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2;

import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMB2MessageCommandCode;
import com.hierynomus.mssmb2.SMB2Packet;

public class SMB2MultiCreditPacket
extends SMB2Packet {
    private int maxPayloadSize;

    public SMB2MultiCreditPacket(int structureSize, SMB2Dialect dialect, SMB2MessageCommandCode messageType, long sessionId, long treeId, int maxPayloadSize) {
        super(structureSize, dialect, messageType, sessionId, treeId);
        this.maxPayloadSize = maxPayloadSize;
    }

    @Override
    public int getMaxPayloadSize() {
        return this.maxPayloadSize;
    }

    protected int getPayloadSize() {
        return Math.min(this.maxPayloadSize, 65536 * this.getCreditsAssigned());
    }
}

