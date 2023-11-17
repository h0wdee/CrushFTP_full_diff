/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.jose4j.lang.JoseException;

public class CipherUtil {
    static Cipher getCipher(String algorithm, String provider) throws JoseException {
        try {
            return provider == null ? Cipher.getInstance(algorithm) : Cipher.getInstance(algorithm, provider);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new JoseException(e.toString(), e);
        }
        catch (NoSuchProviderException e) {
            throw new JoseException("Unable to get a Cipher implementation of " + algorithm + " using provider " + provider, e);
        }
    }
}

