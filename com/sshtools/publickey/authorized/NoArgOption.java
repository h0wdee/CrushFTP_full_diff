/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.sshtools.publickey.authorized.Option;

class NoArgOption
extends Option<Void> {
    NoArgOption(String name) {
        super(name, null);
    }

    @Override
    public String getFormattedOption() {
        return this.getName();
    }
}

