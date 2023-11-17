/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.config.file;

import com.sshtools.config.file.SshdConfigFileCursor;

public interface EntryBuilder<T, P> {
    public P end();

    public SshdConfigFileCursor cursor();
}

