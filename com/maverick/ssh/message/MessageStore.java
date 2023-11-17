/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.message;

import com.maverick.ssh.message.Message;
import com.maverick.ssh.message.MessageObserver;

public interface MessageStore {
    public Message hasMessage(MessageObserver var1);
}

