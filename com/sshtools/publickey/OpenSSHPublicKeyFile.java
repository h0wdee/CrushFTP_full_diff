/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.util.Base64;
import com.sshtools.publickey.SshPublicKeyFile;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

public class OpenSSHPublicKeyFile
implements SshPublicKeyFile {
    byte[] formattedkey;
    String comment;
    String options;

    OpenSSHPublicKeyFile(byte[] formattedkey) throws IOException {
        this.formattedkey = formattedkey;
        this.toPublicKey();
    }

    OpenSSHPublicKeyFile(SshPublicKey key, String comment) throws IOException {
        this(key, comment, null);
    }

    OpenSSHPublicKeyFile(SshPublicKey key, String comment, String options) throws IOException {
        try {
            String formatted = options == null ? "" : options + " ";
            formatted = formatted + key.getAlgorithm() + " " + Base64.encodeBytes(key.getEncoded(), true);
            if (comment != null && comment.trim().length() > 0) {
                formatted = formatted + " " + comment;
            }
            this.formattedkey = formatted.getBytes();
        }
        catch (SshException ex) {
            throw new IOException("Failed to encode public key");
        }
    }

    public String toString() {
        return new String(this.formattedkey);
    }

    @Override
    public byte[] getFormattedKey() {
        return this.formattedkey;
    }

    @Override
    public SshPublicKey toPublicKey() throws IOException {
        String line;
        String temp = new String(this.formattedkey);
        BufferedReader r = new BufferedReader(new StringReader(temp));
        temp = "";
        while ((line = r.readLine()) != null) {
            temp = temp + line;
        }
        int i = 0;
        while (i > -1) {
            int f = i;
            if ((i = temp.indexOf(" ", i)) <= -1) continue;
            String algorithm = temp.substring(f, i);
            ++i;
            if (!ComponentManager.getInstance().supportedPublicKeys().contains(algorithm)) continue;
            int i2 = temp.indexOf(" ", i);
            if (i2 != -1) {
                String encoded = temp.substring(i, i2);
                if (temp.length() > i2) {
                    this.comment = temp.substring(i2).trim();
                }
                if (!encoded.matches("(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{0,2}==|[A-Za-z0-9+/]{0,3}=)?")) {
                    throw new IOException("Public key blob does not appear to be base64 encoded data");
                }
                return SshPublicKeyFileFactory.decodeSSH2PublicKey(algorithm, Base64.decode(encoded));
            }
            String encoded = temp.substring(i);
            if (!encoded.matches("(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{0,2}==|[A-Za-z0-9+/]{0,3}=)?")) {
                throw new IOException("Public key blob does not appear to be base64 encoded data");
            }
            return SshPublicKeyFileFactory.decodeSSH2PublicKey(algorithm, Base64.decode(encoded));
        }
        throw new IOException("The format does not appear to be an OpenSSH public key file in the format <algorithm> <base64_blob>");
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public String getOptions() {
        return this.options;
    }

    public static boolean isFormatted(byte[] formattedkey) {
        try {
            String line;
            String temp = new String(formattedkey);
            BufferedReader r = new BufferedReader(new StringReader(temp));
            temp = "";
            while ((line = r.readLine()) != null) {
                temp = temp + line;
            }
            int i = 0;
            while (i > -1) {
                int f = i;
                if ((i = temp.indexOf(" ", i)) <= -1) continue;
                String algorithm = temp.substring(f, i);
                ++i;
                if (!ComponentManager.getInstance().supportedPublicKeys().contains(algorithm)) continue;
                int i2 = temp.indexOf(" ", i);
                if (i2 != -1) {
                    String encoded = temp.substring(i, i2);
                    Pattern p = Pattern.compile("(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{0,2}==|[A-Za-z0-9+/]{0,3}=)?");
                    return p.matcher(encoded).matches();
                }
                String encoded = temp.substring(i);
                return encoded.matches("(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{0,2}==|[A-Za-z0-9+/]{0,3}=)?");
            }
            return false;
        }
        catch (Throwable e) {
            return false;
        }
    }
}

