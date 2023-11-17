/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.event;

import javax.mail.Message;
import javax.mail.event.MailEvent;
import javax.mail.event.MessageChangedListener;

public class MessageChangedEvent
extends MailEvent {
    public static final int FLAGS_CHANGED = 1;
    public static final int ENVELOPE_CHANGED = 2;
    protected int type;
    protected transient Message msg;
    private static final long serialVersionUID = -4974972972105535108L;

    public MessageChangedEvent(Object source, int type, Message msg) {
        super(source);
        this.msg = msg;
        this.type = type;
    }

    public int getMessageChangeType() {
        return this.type;
    }

    public Message getMessage() {
        return this.msg;
    }

    public void dispatch(Object listener) {
        ((MessageChangedListener)listener).messageChanged(this);
    }
}

