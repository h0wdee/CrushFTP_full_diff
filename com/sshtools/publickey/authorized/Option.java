/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

public abstract class Option<T> {
    String name;
    T value;

    Option(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public abstract String getFormattedOption();

    public String getName() {
        return this.name;
    }

    public T getValue() {
        return this.value;
    }
}

