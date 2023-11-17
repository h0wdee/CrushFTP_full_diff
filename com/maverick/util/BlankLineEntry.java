/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.Entry;

public class BlankLineEntry
extends Entry<Void> {
    public BlankLineEntry() {
        super(null);
    }

    @Override
    public String getFormattedEntry() {
        return "";
    }
}

