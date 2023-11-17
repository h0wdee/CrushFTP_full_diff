/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.sshtools.publickey.authorized.Option;

abstract class StringOption
extends Option<String> {
    StringOption(String name, String value) {
        super(name, value);
    }

    @Override
    public String getFormattedOption() {
        return this.getName() + "=\"" + (String)this.getValue() + "\"";
    }
}

