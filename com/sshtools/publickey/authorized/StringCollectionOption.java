/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.sshtools.publickey.authorized.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

abstract class StringCollectionOption
extends Option<Collection<String>> {
    StringCollectionOption(String name, String values) {
        super(name, new ArrayList<String>(Arrays.asList(values.split(","))));
    }

    StringCollectionOption(String name, Collection<String> values) {
        super(name, values);
    }

    @Override
    public String getFormattedOption() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getName());
        buf.append("=");
        buf.append('\"');
        int len = buf.length();
        for (String v : (Collection)this.getValue()) {
            if (buf.length() > len) {
                buf.append(',');
            }
            buf.append(v);
        }
        buf.append('\"');
        return buf.toString();
    }
}

