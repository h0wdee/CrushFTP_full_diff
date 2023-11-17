/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.components.ComponentFactory;

public class IncompatibleAlgorithm {
    ComponentType type;
    String[] localAlgorithms;
    String[] remoteAlgorithms;
    ComponentFactory<?> factory;

    public IncompatibleAlgorithm(ComponentFactory<?> factory, ComponentType type, String[] localAlgorithms, String[] remoteAlgorithms) {
        this.factory = factory;
        this.type = type;
        this.localAlgorithms = localAlgorithms;
        this.remoteAlgorithms = remoteAlgorithms;
    }

    public ComponentType getType() {
        return this.type;
    }

    public String[] getLocalAlgorithms() {
        return this.localAlgorithms;
    }

    public String[] getRemoteAlgorithms() {
        return this.remoteAlgorithms;
    }

    public ComponentFactory<?> getComponentFactory() {
        return this.factory;
    }

    public static enum ComponentType {
        CIPHER_CS,
        CIPHER_SC,
        MAC_CS,
        MAC_SC,
        KEYEXCHANGE,
        PUBLICKEY,
        COMPRESSION_CS,
        COMPRESSION_SC;

    }
}

