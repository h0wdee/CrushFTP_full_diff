/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.lang;

import java.security.Provider;
import java.security.Security;

public class BouncyCastleProviderHelp {
    private static final String BC_PROVIDER_FQCN = "org.bouncycastle.jce.provider.BouncyCastleProvider";

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean enableBouncyCastleProvider() {
        try {
            Class<?> bcProvider = Class.forName(BC_PROVIDER_FQCN);
            Provider[] providerArray = Security.getProviders();
            int n = providerArray.length;
            int n2 = 0;
            while (true) {
                if (n2 >= n) {
                    Security.addProvider((Provider)bcProvider.newInstance());
                    return true;
                }
                Provider provider = providerArray[n2];
                if (bcProvider.isInstance(provider)) {
                    return true;
                }
                ++n2;
            }
        }
        catch (Exception e) {
            return false;
        }
    }
}

