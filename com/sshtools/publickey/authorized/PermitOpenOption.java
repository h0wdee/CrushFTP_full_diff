/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.sshtools.publickey.authorized.StringCollectionOption;
import java.util.Collection;

public class PermitOpenOption
extends StringCollectionOption {
    PermitOpenOption(String values) {
        super("permitopen", values);
    }

    public PermitOpenOption(Collection<String> values) {
        super("permitopen", values);
    }
}

