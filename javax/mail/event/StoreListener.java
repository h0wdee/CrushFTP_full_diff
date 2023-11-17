/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.event;

import java.util.EventListener;
import javax.mail.event.StoreEvent;

public interface StoreListener
extends EventListener {
    public void notification(StoreEvent var1);
}

