/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.sftp;

import com.maverick.ssh.message.Message;
import com.maverick.util.ByteArrayReader;
import java.io.IOException;

public class SftpMessage
extends ByteArrayReader
implements Message {
    int type = this.read();
    int requestId = (int)this.readInt();

    SftpMessage(byte[] msg) throws IOException {
        super(msg);
    }

    public int getType() {
        return this.type;
    }

    @Override
    public int getMessageId() {
        return this.requestId;
    }
}

