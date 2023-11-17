/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import org.jose4j.jca.ProviderContext;
import org.jose4j.jwx.Headers;

class ContentEncryptionHelp {
    ContentEncryptionHelp() {
    }

    static String getCipherProvider(Headers headers, ProviderContext providerCtx) {
        ProviderContext.Context ctx = ContentEncryptionHelp.choseContext(headers, providerCtx);
        return ctx.getCipherProvider();
    }

    static String getMacProvider(Headers headers, ProviderContext providerContext) {
        ProviderContext.Context ctx = ContentEncryptionHelp.choseContext(headers, providerContext);
        return ctx.getMacProvider();
    }

    private static ProviderContext.Context choseContext(Headers headers, ProviderContext providerCtx) {
        boolean isDir = headers != null && "dir".equals(headers.getStringHeaderValue("alg"));
        return isDir ? providerCtx.getSuppliedKeyProviderContext() : providerCtx.getGeneralProviderContext();
    }
}

