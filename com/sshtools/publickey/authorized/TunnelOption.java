/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.sshtools.publickey.authorized.StringOption;

public class TunnelOption
extends StringOption {
    public TunnelOption(String value) {
        super("tunnel", value);
    }

    @Override
    public String getFormattedOption() {
        return this.getName() + "=\"" + (String)this.getValue() + "\"";
    }
}

