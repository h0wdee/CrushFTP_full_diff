/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.event;

import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

public abstract class TransportAdapter
implements TransportListener {
    public void messageDelivered(TransportEvent e) {
    }

    public void messageNotDelivered(TransportEvent e) {
    }

    public void messagePartiallyDelivered(TransportEvent e) {
    }
}

