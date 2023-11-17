/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.storage;

import java.io.IOException;
import java.io.InputStream;

public interface IKeyStoreStorage {
    public InputStream getInputStream() throws IOException;

    public void store(InputStream var1, int var2) throws IOException;
}

