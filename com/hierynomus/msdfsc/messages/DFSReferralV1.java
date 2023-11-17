/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdfsc.messages;

import com.hierynomus.msdfsc.messages.DFSReferral;
import com.hierynomus.protocol.commons.Charsets;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;

public class DFSReferralV1
extends DFSReferral {
    DFSReferralV1() {
    }

    DFSReferralV1(int version, DFSReferral.ServerType serverType, String path) {
        super(version, serverType, 0);
        this.path = path;
    }

    @Override
    public void readReferral(SMBBuffer buffer, int referralStartPos) throws Buffer.BufferException {
        this.referralEntryFlags = 0L;
        this.path = buffer.readNullTerminatedString(Charsets.UTF_16);
    }

    @Override
    int writeReferral(SMBBuffer buffer, int entryStartPos, int bufferDataOffset) {
        buffer.putNullTerminatedString(this.path, Charsets.UTF_16);
        return bufferDataOffset;
    }

    @Override
    void writeOffsettedData(SMBBuffer buffer) {
    }

    @Override
    protected int determineSize() {
        return 8 + (this.path.length() + 1) * 2;
    }
}

