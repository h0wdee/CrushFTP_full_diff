/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.Entry;

public class CommentEntry
extends Entry<String> {
    public CommentEntry(String value) {
        super(value);
    }

    @Override
    public String getFormattedEntry() {
        return (String)this.value;
    }
}

