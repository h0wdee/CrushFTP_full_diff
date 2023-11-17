/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

class PEM {
    public static final String DSA_PRIVATE_KEY = "DSA PRIVATE KEY";
    public static final String RSA_PRIVATE_KEY = "RSA PRIVATE KEY";
    public static final String EC_PRIVATE_KEY = "EC PRIVATE KEY";
    public static final String OPENSSH_PRIVATE_KEY = "OPENSSH PRIVATE KEY";
    protected static final String PEM_BOUNDARY = "-----";
    protected static final String PEM_BEGIN = "-----BEGIN ";
    protected static final String PEM_END = "-----END ";
    protected static final int MAX_LINE_LENGTH = 75;
    protected static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    private static final int MD5_HASH_BYTES = 16;

    PEM() {
    }

    protected static byte[] getKeyFromPassphrase(String passphrase, byte[] iv, int keySize) throws IOException {
        try {
            byte[] passphraseBytes;
            try {
                passphraseBytes = passphrase == null ? new byte[]{} : passphrase.getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new IOException("Mandatory US-ASCII character encoding is not supported by the VM");
            }
            Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance("MD5");
            byte[] key = new byte[keySize];
            int hashesSize = keySize & 0xFFFFFFF0;
            if ((keySize & 0xF) != 0) {
                hashesSize += 16;
            }
            byte[] hashes = new byte[hashesSize];
            int index = 0;
            while (index + 16 <= hashes.length) {
                hash.putBytes(passphraseBytes, 0, passphraseBytes.length);
                hash.putBytes(iv, 0, 8);
                byte[] previous = hash.doFinal();
                System.arraycopy(previous, 0, hashes, index, previous.length);
                index += previous.length;
                hash.putBytes(previous, 0, previous.length);
            }
            System.arraycopy(hashes, 0, key, 0, key.length);
            return key;
        }
        catch (SshException e) {
            throw new SshIOException(e);
        }
    }
}

