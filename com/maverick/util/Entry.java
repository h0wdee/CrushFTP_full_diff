/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.IOException;

public abstract class Entry<T> {
    protected T value;

    protected Entry(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public abstract String getFormattedEntry() throws IOException;
}

