/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.event;

import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

public abstract class ConnectionAdapter
implements ConnectionListener {
    public void opened(ConnectionEvent e) {
    }

    public void disconnected(ConnectionEvent e) {
    }

    public void closed(ConnectionEvent e) {
    }
}

