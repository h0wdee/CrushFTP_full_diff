/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.events;

import com.maverick.events.Event;
import com.maverick.events.EventListener;

public interface EventService {
    public void addListener(EventListener var1);

    public void addListener(String var1, EventListener var2);

    public void removeListener(String var1);

    public void fireEvent(Event var1);

    public void removeListener(EventListener var1);

    public void registerEventCodeDescriptor(Class var1);

    public String getEventName(Integer var1);
}

