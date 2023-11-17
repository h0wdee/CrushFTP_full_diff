/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.message;

import com.maverick.ssh.message.Message;
import com.maverick.util.ByteArrayReader;

public class SshMessage
extends ByteArrayReader
implements Message {
    int messageid;
    byte[] msg;
    SshMessage next;
    SshMessage previous;

    SshMessage() {
        super(new byte[0]);
    }

    public SshMessage(byte[] msg) {
        super(msg);
        this.messageid = this.read();
    }

    @Override
    public int getMessageId() {
        return this.messageid;
    }
}

