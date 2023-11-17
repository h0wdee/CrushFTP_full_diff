/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.builders;

import org.apache.log4j.builders.Holder;

@Deprecated
public class BooleanHolder
extends Holder<Boolean> {
    public BooleanHolder() {
        super(Boolean.FALSE);
    }

    @Override
    public void set(Boolean value) {
        if (value != null) {
            super.set(value);
        }
    }
}

