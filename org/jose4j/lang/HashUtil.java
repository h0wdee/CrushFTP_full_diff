/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.lang;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import org.jose4j.lang.UncheckedJoseException;

public class HashUtil {
    public static final String SHA_256 = "SHA-256";

    public static MessageDigest getMessageDigest(String alg) {
        return HashUtil.getMessageDigest(alg, null);
    }

    public static MessageDigest getMessageDigest(String alg, String provider) {
        try {
            return provider == null ? MessageDigest.getInstance(alg) : MessageDigest.getInstance(alg, provider);
        }
        catch (NoSuchAlgorithmException e) {
            throw new UncheckedJoseException("Unable to get MessageDigest instance with " + alg);
        }
        catch (NoSuchProviderException e) {
            throw new UncheckedJoseException("Unable to get a MessageDigest implementation of algorithm name: " + alg + " using provider " + provider, e);
        }
    }
}

