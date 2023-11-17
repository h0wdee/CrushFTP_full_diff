/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.events;

public class EventException
extends RuntimeException {
    private static final long serialVersionUID = 8920551654296049197L;

    public EventException() {
    }

    public EventException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public EventException(String arg0) {
        super(arg0);
    }

    public EventException(Throwable arg0) {
        super(arg0);
    }
}

