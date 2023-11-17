/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.message;

import com.maverick.ssh.message.Message;

public interface MessageObserver {
    public boolean wantsNotification(Message var1);
}

