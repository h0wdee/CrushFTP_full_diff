/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.storage;

import com.didisoft.pgp.storage.IKeyStoreStorage;
import java.io.IOException;
import java.io.InputStream;

public class InMemoryKeyStorage
implements IKeyStoreStorage {
    public InputStream getInputStream() throws IOException {
        return null;
    }

    public void store(InputStream inputStream, int n) throws IOException {
    }
}

