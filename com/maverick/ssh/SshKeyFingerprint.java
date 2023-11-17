/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.util.Base64;

public class SshKeyFingerprint {
    public static final String MD5_FINGERPRINT = "MD5";
    public static final String SHA1_FINGERPRINT = "SHA-1";
    public static final String SHA256_FINGERPRINT = "SHA256";
    private static String defaultHashAlgoritm = "SHA256";
    private static Encoding defaultEncoding = null;
    static char[] VOWELS = new char[]{'a', 'e', 'i', 'o', 'u', 'y'};
    static char[] CONSONANTS = new char[]{'b', 'c', 'd', 'f', 'g', 'h', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'z', 'x'};
    static char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static void setDefaultHashAlgorithm(String defaultHashAlgorithm) {
        defaultHashAlgoritm = defaultHashAlgorithm;
    }

    public static void setDefaultEncoding(Encoding encoding) {
        defaultEncoding = encoding;
    }

    public static String getFingerprint(SshPublicKey key) {
        try {
            return SshKeyFingerprint.getFingerprint(key.getEncoded(), defaultHashAlgoritm, defaultEncoding);
        }
        catch (SshException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static String getFingerprint(SshPublicKey key, String algorithm) throws SshException {
        return SshKeyFingerprint.getFingerprint(key.getEncoded(), algorithm, defaultEncoding);
    }

    public static String getFingerprint(SshPublicKey key, String algorithm, Encoding encoding) throws SshException {
        return SshKeyFingerprint.getFingerprint(key.getEncoded(), algorithm, encoding);
    }

    public static String getFingerprint(byte[] encoded) throws SshException {
        return SshKeyFingerprint.getFingerprint(encoded, defaultHashAlgoritm, defaultEncoding);
    }

    public static String getFingerprint(byte[] encoded, String algorithm) throws SshException {
        return SshKeyFingerprint.getFingerprint(encoded, algorithm, defaultEncoding);
    }

    public static String getFingerprint(byte[] encoded, String algorithm, Encoding encoding) throws SshException {
        if (encoding == null) {
            encoding = defaultEncoding == null ? (algorithm.equals(SHA256_FINGERPRINT) ? Encoding.BASE64 : Encoding.HEXIDECIMAL) : defaultEncoding;
        }
        Digest digest = (Digest)ComponentManager.getInstance().supportedDigests().getInstance(algorithm);
        digest.putBytes(encoded);
        byte[] hash = digest.doFinal();
        StringBuffer buf = new StringBuffer();
        buf.append(algorithm);
        buf.append(":");
        switch (encoding) {
            case BASE64: {
                buf.append(Base64.encodeBytes(hash, true));
                while (buf.charAt(buf.length() - 1) == '=') {
                    buf.delete(buf.length() - 1, buf.length());
                }
                break;
            }
            default: {
                for (int i = 0; i < hash.length; ++i) {
                    int ch = hash[i] & 0xFF;
                    if (i > 0) {
                        buf.append(':');
                    }
                    buf.append(HEX[ch >>> 4 & 0xF]);
                    buf.append(HEX[ch & 0xF]);
                }
            }
        }
        return buf.toString();
    }

    public static String getBubbleBabble(SshPublicKey key) {
        try {
            return SshKeyFingerprint.getBubbleBabble(key.getEncoded());
        }
        catch (SshException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static String getBubbleBabble(byte[] encoded) {
        try {
            Digest sha1 = (Digest)ComponentManager.getInstance().supportedDigests().getInstance(SHA1_FINGERPRINT);
            sha1.putBytes(encoded);
            encoded = sha1.doFinal();
            int r = encoded.length / 2 + 1;
            int s = 1;
            StringBuilder b = new StringBuilder();
            b.append('x');
            for (int x = 0; x < r; ++x) {
                if (x + 1 < r || encoded.length % 2 != 0) {
                    b.append(VOWELS[(((encoded[2 * x] & 0xFF) >> 6 & 3) + s) % 6]);
                    b.append(CONSONANTS[(encoded[2 * x] & 0xFF) >> 2 & 0xF]);
                    b.append(VOWELS[((encoded[2 * x] & 0xFF & 3) + s / 6) % 6]);
                    if (x + 1 >= r) continue;
                    b.append(CONSONANTS[(encoded[2 * x + 1] & 0xFF) >> 4 & 0xF]);
                    b.append('-');
                    b.append(CONSONANTS[encoded[2 * x + 1] & 0xFF & 0xF]);
                    s = (s * 5 + ((encoded[2 * x] & 0xFF) * 7 + (encoded[2 * x + 1] & 0xFF))) % 36;
                    continue;
                }
                b.append(VOWELS[s % 6]);
                b.append(CONSONANTS[16]);
                b.append(VOWELS[s / 6]);
            }
            b.append('x');
            return b.toString();
        }
        catch (SshException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static enum Encoding {
        HEXIDECIMAL,
        BASE64;

    }
}

