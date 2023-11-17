/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.SftpFileFilter;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class GlobSftpFileFilter
implements SftpFileFilter {
    PathMatcher matcher;

    public GlobSftpFileFilter(String filter) {
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + filter);
    }

    @Override
    public boolean matches(String name) {
        return this.matcher.matches(Paths.get(name, new String[0]));
    }
}

