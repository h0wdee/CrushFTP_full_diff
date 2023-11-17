/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.events;

import com.maverick.events.EventObject;
import java.util.HashMap;
import java.util.Map;

public class Event
extends EventObject {
    private final int id;
    private final boolean state;
    private final Map<String, Object> eventAttributes = new HashMap<String, Object>();
    private final String uuid;

    public Event(Object source, int id, boolean state) {
        this(source, id, state, "");
    }

    public Event(Object source, int id, boolean state, String uuid) {
        super(source);
        this.id = id;
        this.state = state;
        this.uuid = uuid;
    }

    @Deprecated
    public Event(Object source, int id, Throwable error) {
        this(source, id, error, "");
    }

    public Event(Object source, int id, Throwable error, String uuid) {
        super(source);
        this.id = id;
        this.state = error == null;
        this.uuid = uuid;
        if (error != null) {
            this.addAttribute("THROWABLE", error);
        }
    }

    public int getId() {
        return this.id;
    }

    public boolean getState() {
        return this.state;
    }

    public Object getAttribute(String key) {
        return this.eventAttributes.get(key);
    }

    public String getAllAttributes() {
        StringBuffer buff = new StringBuffer();
        for (String key : this.eventAttributes.keySet()) {
            Object value = this.eventAttributes.get(key);
            buff.append("|\r\n");
            buff.append(key);
            buff.append(" = ");
            if (value == null) continue;
            buff.append(value.toString());
        }
        return buff.toString();
    }

    public Event addAttribute(String key, Object value) {
        this.eventAttributes.put(key, value);
        return this;
    }

    public String getUuid() {
        return this.uuid;
    }
}

