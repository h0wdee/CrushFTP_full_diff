/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.sshtools.publickey.Base64EncodedFileFormat;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import java.io.IOException;
import java.math.BigInteger;

class SSHCOMPrivateKeyFile
extends Base64EncodedFileFormat
implements SshPrivateKeyFile {
    static String BEGIN = "---- BEGIN SSH2 ENCRYPTED PRIVATE KEY ----";
    static String END = "---- END SSH2 ENCRYPTED PRIVATE KEY ----";
    byte[] formattedkey;

    SSHCOMPrivateKeyFile(byte[] formattedkey) throws IOException {
        super(BEGIN, END);
        if (!SSHCOMPrivateKeyFile.isFormatted(formattedkey)) {
            throw new IOException("Key is not formatted in the ssh.com format");
        }
        this.formattedkey = formattedkey;
    }

    @Override
    public String getType() {
        return "SSH Communications Security";
    }

    public static boolean isFormatted(byte[] formattedkey) {
        return SSHCOMPrivateKeyFile.isFormatted(formattedkey, BEGIN, END);
    }

    @Override
    public boolean supportsPassphraseChange() {
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isPassphraseProtected() {
        boolean bl;
        byte[] keyblob = this.getKeyBlob(this.formattedkey);
        ByteArrayReader bar = new ByteArrayReader(keyblob);
        try {
            long magic = bar.readInt();
            if (magic != 1064303083L) {
                throw new IOException("Invalid ssh.com key! Magic number not found");
            }
            bar.readInt();
            bar.readString();
            String cipher = bar.readString();
            bl = cipher.equals("3des-cbc");
        }
        catch (Throwable throwable) {
            try {
                bar.close();
                throw throwable;
            }
            catch (IOException iOException) {
                return false;
            }
        }
        bar.close();
        return bl;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public SshKeyPair toKeyPair(String passphrase) throws IOException, InvalidPassphraseException {
        byte[] keyblob = this.getKeyBlob(this.formattedkey);
        boolean wasEncrypted = false;
        try (ByteArrayReader bar = new ByteArrayReader(keyblob);){
            long magic = bar.readInt();
            if (magic != 1064303083L) {
                throw new IOException("Invalid ssh.com key! Magic number not found");
            }
            bar.readInt();
            String type = bar.readString();
            String cipher = bar.readString();
            byte[] blob = bar.readBinaryString();
            try {
                if (!cipher.equals("none")) {
                    if (!cipher.equals("3des-cbc")) {
                        throw new IOException("Unsupported cipher type " + cipher + " in ssh.com private key");
                    }
                    SshCipher c = (SshCipher)ComponentManager.getInstance().supportedSsh2CiphersCS().getInstance("3des-cbc");
                    byte[] iv = new byte[32];
                    byte[] keydata = this.makePassphraseKey(passphrase);
                    c.init(1, iv, keydata);
                    c.transform(blob);
                    wasEncrypted = true;
                }
            }
            catch (SshException e1) {
                throw new SshIOException(e1);
            }
            try (ByteArrayReader data = new ByteArrayReader(blob, 4, blob.length - 4);){
                if (type.startsWith("if-modn{sign{rsa")) {
                    BigInteger e = data.readMPINT32();
                    BigInteger d = data.readMPINT32();
                    BigInteger n = data.readMPINT32();
                    SshKeyPair pair = new SshKeyPair();
                    pair.setPublicKey(ComponentManager.getInstance().createRsaPublicKey(n, e, 2));
                    pair.setPrivateKey(ComponentManager.getInstance().createRsaPrivateKey(n, d));
                    SshKeyPair sshKeyPair = pair;
                    return sshKeyPair;
                }
                if (type.startsWith("dl-modp{sign{dsa")) {
                    long predefined = data.readInt();
                    if (predefined != 0L) {
                        throw new IOException("Unexpected value in DSA key; this is an unsupported feature of ssh.com private keys");
                    }
                    BigInteger p = data.readMPINT32();
                    BigInteger g = data.readMPINT32();
                    BigInteger q = data.readMPINT32();
                    BigInteger y = data.readMPINT32();
                    BigInteger x = data.readMPINT32();
                    SshKeyPair pair = new SshKeyPair();
                    SshDsaPublicKey pub = ComponentManager.getInstance().createDsaPublicKey(p, q, g, y);
                    pair.setPublicKey(pub);
                    pair.setPrivateKey(ComponentManager.getInstance().createDsaPrivateKey(p, q, g, x, pub.getY()));
                    SshKeyPair sshKeyPair = pair;
                    return sshKeyPair;
                }
                throw new IOException("Unsupported ssh.com key type " + type);
            }
        }
    }

    private byte[] makePassphraseKey(String passphrase) throws IOException {
        try (ByteArrayWriter baw = new ByteArrayWriter();){
            Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance("MD5");
            hash.putBytes(passphrase.getBytes());
            byte[] tmp = hash.doFinal();
            hash.reset();
            hash.putBytes(passphrase.getBytes());
            hash.putBytes(tmp);
            baw.write(tmp);
            baw.write(hash.doFinal());
            byte[] byArray = baw.toByteArray();
            return byArray;
        }
    }

    @Override
    public void changePassphrase(String oldpassphrase, String newpassprase) throws IOException {
        throw new IOException("Changing passphrase is not supported by the ssh.com key format engine");
    }

    @Override
    public byte[] getFormattedKey() throws IOException {
        return this.formattedkey;
    }
}

