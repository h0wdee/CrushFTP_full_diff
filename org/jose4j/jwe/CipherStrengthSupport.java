/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import org.jose4j.lang.ByteUtil;

public class CipherStrengthSupport {
    public static boolean isAvailable(String algorithm, int keyByteLength) {
        boolean isAvailable;
        int bitKeyLength = ByteUtil.bitLength(keyByteLength);
        try {
            int maxAllowedKeyLength = Cipher.getMaxAllowedKeyLength(algorithm);
            isAvailable = bitKeyLength <= maxAllowedKeyLength;
        }
        catch (NoSuchAlgorithmException e) {
            isAvailable = false;
        }
        return isAvailable;
    }
}

