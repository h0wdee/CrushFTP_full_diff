/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.event;

import java.util.EventListener;
import javax.mail.event.ConnectionEvent;

public interface ConnectionListener
extends EventListener {
    public void opened(ConnectionEvent var1);

    public void disconnected(ConnectionEvent var1);

    public void closed(ConnectionEvent var1);
}

