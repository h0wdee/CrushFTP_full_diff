/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.message;

import com.maverick.ssh.SshException;
import com.maverick.ssh.message.SshMessage;
import java.io.IOException;

public class SshChannelMessage
extends SshMessage {
    int channelid;

    public SshChannelMessage(byte[] msg) throws SshException {
        super(msg);
        try {
            this.channelid = (int)this.readInt();
        }
        catch (IOException ex) {
            throw new SshException(5, (Throwable)ex);
        }
    }

    int getChannelId() {
        return this.channelid;
    }
}

