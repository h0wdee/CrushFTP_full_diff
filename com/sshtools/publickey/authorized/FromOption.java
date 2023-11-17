/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.sshtools.publickey.authorized.StringCollectionOption;
import java.util.Collection;

public class FromOption
extends StringCollectionOption {
    FromOption(String values) {
        super("from", values);
    }

    public FromOption(Collection<String> values) {
        super("from", values);
    }
}

