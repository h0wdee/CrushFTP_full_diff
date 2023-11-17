/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwx;

import java.security.Key;
import java.security.interfaces.RSAKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidKeyException;

public class KeyValidationSupport {
    public static final int MIN_RSA_KEY_LENGTH = 2048;

    public static void checkRsaKeySize(Key key) throws InvalidKeyException {
        RSAKey rsaKey;
        int size;
        if (key == null) {
            throw new InvalidKeyException("The RSA key must not be null.");
        }
        if (key instanceof RSAKey && (size = (rsaKey = (RSAKey)((Object)key)).getModulus().bitLength()) < 2048) {
            throw new InvalidKeyException("An RSA key of size 2048 bits or larger MUST be used with the all JOSE RSA algorithms (given key was only " + size + " bits).");
        }
    }

    public static <K extends Key> K castKey(Key key, Class<K> type) throws InvalidKeyException {
        KeyValidationSupport.notNull(key);
        try {
            return (K)((Key)type.cast(key));
        }
        catch (ClassCastException e) {
            throw new InvalidKeyException("Invalid key " + e);
        }
    }

    public static void notNull(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("The key must not be null.");
        }
    }

    public static void cekNotAllowed(byte[] cekOverride, String alg) throws InvalidKeyException {
        if (cekOverride != null) {
            throw new InvalidKeyException("An explicit content encryption key cannot be used with " + alg);
        }
    }

    public static void validateAesWrappingKey(Key managementKey, String joseAlg, int expectedKeyByteLength) throws InvalidKeyException {
        int managementKeyByteLength;
        KeyValidationSupport.notNull(managementKey);
        String alg = managementKey.getAlgorithm();
        if (!"AES".equals(alg)) {
            throw new InvalidKeyException("Invalid key for JWE " + joseAlg + ", expected an " + "AES" + " key but an " + alg + " key was provided.");
        }
        if (managementKey.getEncoded() != null && (managementKeyByteLength = managementKey.getEncoded().length) != expectedKeyByteLength) {
            throw new InvalidKeyException("Invalid key for JWE " + joseAlg + ", expected a " + ByteUtil.bitLength(expectedKeyByteLength) + " bit key but a " + ByteUtil.bitLength(managementKeyByteLength) + " bit key was provided.");
        }
    }
}

