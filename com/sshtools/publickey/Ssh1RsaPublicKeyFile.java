/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.sshtools.publickey.SshPublicKeyFile;
import java.io.IOException;
import java.math.BigInteger;
import java.util.StringTokenizer;

class Ssh1RsaPublicKeyFile
implements SshPublicKeyFile {
    String formattedkey;

    public Ssh1RsaPublicKeyFile(byte[] formattedkey) throws IOException {
        this.formattedkey = new String(formattedkey);
        this.toPublicKey();
    }

    public Ssh1RsaPublicKeyFile(SshPublicKey key) throws IOException {
        if (!(key instanceof SshRsaPublicKey)) {
            throw new IOException("SSH1 public keys must be rsa");
        }
        this.formattedkey = String.valueOf(((SshRsaPublicKey)key).getModulus().bitLength()) + " " + ((SshRsaPublicKey)key).getPublicExponent() + " " + ((SshRsaPublicKey)key).getModulus();
        this.toPublicKey();
    }

    @Override
    public byte[] getFormattedKey() {
        return this.formattedkey.getBytes();
    }

    @Override
    public SshPublicKey toPublicKey() throws IOException {
        StringTokenizer tokens = new StringTokenizer(this.formattedkey.trim(), " ");
        try {
            Integer.parseInt((String)tokens.nextElement());
            String e = (String)tokens.nextElement();
            String n = (String)tokens.nextElement();
            BigInteger publicExponent = new BigInteger(e);
            BigInteger modulus = new BigInteger(n);
            return ComponentManager.getInstance().createRsaPublicKey(modulus, publicExponent, 1);
        }
        catch (Throwable ex) {
            throw new IOException("Invalid SSH1 public key format");
        }
    }

    @Override
    public String getComment() {
        return "";
    }

    @Override
    public String getOptions() {
        return null;
    }

    public static boolean isFormatted(byte[] formattedkey) {
        try {
            StringTokenizer tokens = new StringTokenizer(new String(formattedkey, "UTF-8"), " ");
            Integer.parseInt((String)tokens.nextElement());
            tokens.nextElement();
            tokens.nextElement();
            return true;
        }
        catch (Throwable ex) {
            return false;
        }
    }
}

