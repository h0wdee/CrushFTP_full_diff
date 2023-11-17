/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.sshtools.publickey.authorized.StringCollectionOption;
import java.util.Collection;

public class PrincipalsOption
extends StringCollectionOption {
    PrincipalsOption(String values) {
        super("principals", values);
    }

    public PrincipalsOption(Collection<String> values) {
        super("principals", values);
    }
}

