/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMB2Header;
import com.hierynomus.mssmb2.SMB2MessageFlag;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBPacketData;

public class SMB2PacketData
extends SMBPacketData<SMB2Header> {
    public SMB2PacketData(byte[] data) throws Buffer.BufferException {
        super(new SMB2Header(), data);
    }

    public long getSequenceNumber() {
        return ((SMB2Header)this.getHeader()).getMessageId();
    }

    protected boolean isSuccess() {
        long statusCode = ((SMB2Header)this.getHeader()).getStatusCode();
        return NtStatus.isSuccess(statusCode) && statusCode != NtStatus.STATUS_PENDING.getValue();
    }

    public boolean isIntermediateAsyncResponse() {
        return EnumWithValue.EnumUtils.isSet(((SMB2Header)this.getHeader()).getFlags(), SMB2MessageFlag.SMB2_FLAGS_ASYNC_COMMAND) && ((SMB2Header)this.getHeader()).getStatusCode() == NtStatus.STATUS_PENDING.getValue();
    }
}

