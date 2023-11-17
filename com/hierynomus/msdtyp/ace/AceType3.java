/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdtyp.ace;

import com.hierynomus.msdtyp.SID;
import com.hierynomus.msdtyp.ace.ACE;
import com.hierynomus.msdtyp.ace.AceHeader;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;
import java.util.Arrays;

class AceType3
extends ACE {
    private long accessMask;
    private SID sid;
    private byte[] applicationData;

    AceType3(AceHeader header, long accessMask, SID sid, byte[] applicationData) {
        super(header);
        this.accessMask = accessMask;
        this.sid = sid;
        this.applicationData = applicationData;
    }

    @Override
    protected void writeBody(SMBBuffer buffer) {
        buffer.putUInt32(this.accessMask);
        this.sid.write(buffer);
        buffer.putRawBytes(this.applicationData);
    }

    static AceType3 read(AceHeader header, SMBBuffer buffer, int aceStartPos) throws Buffer.BufferException {
        long accessMask = buffer.readUInt32();
        SID sid = SID.read(buffer);
        int applicationDataSize = header.getAceSize() - (buffer.rpos() - aceStartPos);
        byte[] applicationData = buffer.readRawBytes(applicationDataSize);
        return new AceType3(header, accessMask, sid, applicationData);
    }

    public String toString() {
        return String.format("AceType3{type=%s, flags=%s, access=0x%x, sid=%s, data=%s}", this.aceHeader.getAceType(), this.aceHeader.getAceFlags(), this.accessMask, this.sid, Arrays.toString(this.applicationData));
    }

    @Override
    public SID getSid() {
        return this.sid;
    }

    @Override
    public long getAccessMask() {
        return this.accessMask;
    }

    public byte[] getApplicationData() {
        return this.applicationData;
    }
}

