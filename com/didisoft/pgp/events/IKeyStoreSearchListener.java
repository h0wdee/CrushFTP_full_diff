/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.events;

import com.didisoft.pgp.KeyStore;

public interface IKeyStoreSearchListener {
    public void onKeyNotFound(KeyStore var1, boolean var2, long var3, String var5, String var6);
}

