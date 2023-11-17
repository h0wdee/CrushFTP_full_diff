/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.SftpFileFilter;
import java.util.regex.Pattern;

public class RegexSftpFileFilter
implements SftpFileFilter {
    Pattern p;

    public RegexSftpFileFilter(String filter) {
        this.p = Pattern.compile(filter);
    }

    @Override
    public boolean matches(String name) {
        return this.p.matcher(name).matches();
    }
}

