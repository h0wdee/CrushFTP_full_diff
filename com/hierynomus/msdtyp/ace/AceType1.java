/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdtyp.ace;

import com.hierynomus.msdtyp.SID;
import com.hierynomus.msdtyp.ace.ACE;
import com.hierynomus.msdtyp.ace.AceHeader;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;

class AceType1
extends ACE {
    private long accessMask;
    private SID sid;

    AceType1(AceHeader header, long accessMask, SID sid) {
        super(header);
        this.accessMask = accessMask;
        this.sid = sid;
    }

    @Override
    protected void writeBody(SMBBuffer buffer) {
        buffer.putUInt32(this.accessMask);
        this.sid.write(buffer);
    }

    static AceType1 read(AceHeader header, SMBBuffer buffer) throws Buffer.BufferException {
        long accessMask = buffer.readUInt32();
        SID sid = SID.read(buffer);
        return new AceType1(header, accessMask, sid);
    }

    @Override
    public SID getSid() {
        return this.sid;
    }

    @Override
    public long getAccessMask() {
        return this.accessMask;
    }

    public String toString() {
        return String.format("AceType1{type=%s, flags=%s, access=0x%x, sid=%s}", this.aceHeader.getAceType(), this.aceHeader.getAceFlags(), this.accessMask, this.sid);
    }
}

