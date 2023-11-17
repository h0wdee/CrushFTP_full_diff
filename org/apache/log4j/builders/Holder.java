/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.builders;

@Deprecated
public class Holder<V> {
    private V value;

    public Holder() {
    }

    public Holder(V defaultValue) {
        this.value = defaultValue;
    }

    public void set(V value) {
        this.value = value;
    }

    public V get() {
        return this.value;
    }
}

