/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2.messages;

import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMB2Header;
import com.hierynomus.mssmb2.SMB2MessageCommandCode;
import com.hierynomus.mssmb2.SMB2MessageFlag;
import com.hierynomus.mssmb2.SMB2Packet;
import com.hierynomus.smb.SMBBuffer;

public class SMB2CancelRequest
extends SMB2Packet {
    public SMB2CancelRequest(SMB2Dialect dialect, long messageId, long asyncId) {
        super(4, dialect, SMB2MessageCommandCode.SMB2_CANCEL);
        ((SMB2Header)this.header).setMessageId(messageId);
        if (asyncId != 0L) {
            ((SMB2Header)this.header).setFlag(SMB2MessageFlag.SMB2_FLAGS_ASYNC_COMMAND);
            ((SMB2Header)this.header).setAsyncId(asyncId);
        }
    }

    @Override
    protected void writeTo(SMBBuffer buffer) {
        buffer.putUInt16(this.structureSize);
        buffer.putReserved2();
    }
}

