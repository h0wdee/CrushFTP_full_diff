/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdtyp.ace;

import com.hierynomus.msdtyp.SID;
import com.hierynomus.msdtyp.ace.AceHeader;
import com.hierynomus.msdtyp.ace.AceType2;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;
import java.util.Arrays;
import java.util.UUID;

class AceType4
extends AceType2 {
    private byte[] applicationData;

    private AceType4(AceHeader header) {
        super(header);
    }

    AceType4(AceHeader header, long accessMask, UUID objectType, UUID inheritedObjectType, SID sid, byte[] applicationData) {
        super(header, accessMask, objectType, inheritedObjectType, sid);
        this.applicationData = applicationData;
    }

    @Override
    void writeBody(SMBBuffer buffer) {
        super.writeBody(buffer);
        buffer.putRawBytes(this.applicationData);
    }

    @Override
    protected void readBody(SMBBuffer buffer, int aceStartPos) throws Buffer.BufferException {
        super.readBody(buffer, aceStartPos);
        int applicationDataSize = this.aceHeader.getAceSize() - (buffer.rpos() - aceStartPos);
        this.applicationData = buffer.readRawBytes(applicationDataSize);
    }

    static AceType4 read(AceHeader header, SMBBuffer buffer, int aceStartPos) throws Buffer.BufferException {
        AceType4 ace = new AceType4(header);
        ace.readBody(buffer, aceStartPos);
        return ace;
    }

    @Override
    public String toString() {
        return String.format("AceType4{type=%s, flags=%s, access=0x%x, objectType=%s, inheritedObjectType=%s, sid=%s, data=%s}", this.aceHeader.getAceType(), this.aceHeader.getAceFlags(), this.accessMask, this.objectType, this.inheritedObjectType, this.sid, Arrays.toString(this.applicationData));
    }

    public byte[] getApplicationData() {
        return this.applicationData;
    }
}

