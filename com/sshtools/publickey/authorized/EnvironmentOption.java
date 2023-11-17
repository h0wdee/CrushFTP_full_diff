/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.sshtools.publickey.authorized.AuthorizedKeyFile;
import com.sshtools.publickey.authorized.StringOption;

public class EnvironmentOption
extends StringOption {
    EnvironmentOption(String value) {
        super("environment", value);
    }

    public EnvironmentOption(String key, String value) {
        super("environment", key + "=" + value);
    }

    public String getEnvironmentName() {
        return AuthorizedKeyFile.splitName((String)this.getValue());
    }

    public String getEnvironmentValue() {
        return AuthorizedKeyFile.splitValue((String)this.getValue());
    }
}

