/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.mac;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.Mac;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;

public class MacUtil {
    public static final String HMAC_SHA256 = "HmacSHA256";
    public static final String HMAC_SHA384 = "HmacSHA384";
    public static final String HMAC_SHA512 = "HmacSHA512";

    public static Mac getInitializedMac(String algorithm, Key key) throws JoseException {
        return MacUtil.getInitializedMac(algorithm, key, null);
    }

    public static Mac getInitializedMac(String algorithm, Key key, String provider) throws JoseException {
        Mac mac = MacUtil.getMac(algorithm, provider);
        MacUtil.initMacWithKey(mac, key);
        return mac;
    }

    public static Mac getMac(String algorithm) throws JoseException {
        return MacUtil.getMac(algorithm, null);
    }

    public static Mac getMac(String algorithm, String provider) throws JoseException {
        try {
            return provider == null ? Mac.getInstance(algorithm) : Mac.getInstance(algorithm, provider);
        }
        catch (NoSuchAlgorithmException e) {
            throw new JoseException("Unable to get a MAC implementation of algorithm name: " + algorithm, e);
        }
        catch (NoSuchProviderException e) {
            throw new JoseException("Unable to get a MAC implementation of algorithm name: " + algorithm + " using provider " + provider, e);
        }
    }

    public static void initMacWithKey(Mac mac, Key key) throws InvalidKeyException {
        try {
            mac.init(key);
        }
        catch (java.security.InvalidKeyException e) {
            throw new InvalidKeyException("Key is not valid for " + mac.getAlgorithm(), e);
        }
    }
}

