/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.SshDsaPrivateKey;
import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshRsaPrivateKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.sshtools.publickey.Base64EncodedFileFormat;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import java.io.IOException;
import java.math.BigInteger;

class SshtoolsPrivateKeyFile
extends Base64EncodedFileFormat
implements SshPrivateKeyFile {
    public static String BEGIN = "---- BEGIN SSHTOOLS ENCRYPTED PRIVATE KEY ----";
    public static String END = "---- END SSHTOOLS ENCRYPTED PRIVATE KEY ----";
    private int cookie;
    byte[] keyblob;

    SshtoolsPrivateKeyFile(byte[] formattedkey) throws IOException {
        super(BEGIN, END);
        this.cookie = 1391688382;
        this.keyblob = this.getKeyBlob(formattedkey);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    SshtoolsPrivateKeyFile(SshKeyPair pair, String passphrase, String comment) throws IOException {
        block5: {
            super(BEGIN, END);
            this.cookie = 1391688382;
            this.setHeaderValue("Comment", comment);
            try (ByteArrayWriter baw = new ByteArrayWriter();){
                if (pair.getPrivateKey() instanceof SshDsaPrivateKey) {
                    SshDsaPrivateKey key = (SshDsaPrivateKey)pair.getPrivateKey();
                    SshDsaPublicKey pub = key.getPublicKey();
                    baw.writeString("ssh-dss");
                    baw.writeBigInteger(pub.getP());
                    baw.writeBigInteger(pub.getQ());
                    baw.writeBigInteger(pub.getG());
                    baw.writeBigInteger(key.getX());
                    this.keyblob = this.encryptKey(baw.toByteArray(), passphrase);
                    break block5;
                }
                if (pair.getPrivateKey() instanceof SshRsaPrivateKey) {
                    SshRsaPrivateKey pri = (SshRsaPrivateKey)pair.getPrivateKey();
                    SshRsaPublicKey pub = (SshRsaPublicKey)pair.getPublicKey();
                    baw.writeString("ssh-rsa");
                    baw.writeBigInteger(pub.getPublicExponent());
                    baw.writeBigInteger(pub.getModulus());
                    baw.writeBigInteger(pri.getPrivateExponent());
                    this.keyblob = this.encryptKey(baw.toByteArray(), passphrase);
                    break block5;
                }
                throw new IOException("Unsupported private key type!");
            }
        }
    }

    public static boolean isFormatted(byte[] key) {
        return SshtoolsPrivateKeyFile.isFormatted(key, BEGIN, END);
    }

    @Override
    public String getType() {
        return "SSHTools";
    }

    @Override
    public boolean supportsPassphraseChange() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isPassphraseProtected() {
        ByteArrayReader bar = new ByteArrayReader(this.keyblob);
        try {
            String type = bar.readString();
            if (type.equals("none")) {
                boolean bl = false;
                return bl;
            }
            if (type.equalsIgnoreCase("3des-cbc")) {
                boolean bl = true;
                return bl;
            }
        }
        catch (IOException iOException) {
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
        return false;
    }

    private byte[] encryptKey(byte[] key, String passphrase) throws IOException {
        ByteArrayWriter baw = new ByteArrayWriter();
        ByteArrayWriter data = new ByteArrayWriter();
        try {
            String type = "none";
            if (passphrase != null && !passphrase.trim().equals("")) {
                type = "3DES-CBC";
                byte[] keydata = this.makePassphraseKey(passphrase);
                byte[] iv = new byte[8];
                ComponentManager.getInstance().getRND().nextBytes(iv);
                SshCipher cipher = (SshCipher)ComponentManager.getInstance().supportedSsh2CiphersCS().getInstance("3des-cbc");
                cipher.init(0, iv, keydata);
                baw.writeString(type);
                baw.write(iv);
                data.writeInt(this.cookie);
                data.writeBinaryString(key);
                if (data.size() % cipher.getBlockSize() != 0) {
                    int length = cipher.getBlockSize() - data.size() % cipher.getBlockSize();
                    byte[] padding = new byte[length];
                    for (int i = 0; i < length; ++i) {
                        padding[i] = (byte)length;
                    }
                    data.write(padding);
                }
                byte[] blob = data.toByteArray();
                cipher.transform(blob, 0, blob, 0, blob.length);
                baw.writeBinaryString(blob);
                byte[] byArray = baw.toByteArray();
                return byArray;
            }
            baw.writeString(type);
            baw.writeBinaryString(key);
            byte[] byArray = baw.toByteArray();
            return byArray;
        }
        catch (SshException e) {
            throw new SshIOException(e);
        }
        finally {
            baw.close();
            data.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] decryptKey(String passphrase) throws IOException, InvalidPassphraseException {
        try (ByteArrayReader bar = new ByteArrayReader(this.keyblob);){
            byte[] decryptedkey;
            block11: {
                String type = bar.readString();
                if (type.equalsIgnoreCase("3des-cbc")) {
                    byte[] keydata = this.makePassphraseKey(passphrase);
                    byte[] iv = new byte[8];
                    if (type.equals("3DES-CBC")) {
                        bar.read(iv);
                    }
                    decryptedkey = bar.readBinaryString();
                    SshCipher cipher = (SshCipher)ComponentManager.getInstance().supportedSsh2CiphersCS().getInstance("3des-cbc");
                    cipher.init(1, iv, keydata);
                    cipher.transform(decryptedkey, 0, decryptedkey, 0, decryptedkey.length);
                    try (ByteArrayReader data = new ByteArrayReader(decryptedkey);){
                        if (data.readInt() == (long)this.cookie) {
                            decryptedkey = data.readBinaryString();
                            break block11;
                        }
                        throw new InvalidPassphraseException();
                    }
                }
                decryptedkey = bar.readBinaryString();
            }
            byte[] byArray = decryptedkey;
            return byArray;
        }
    }

    @Override
    public byte[] getFormattedKey() throws IOException {
        return this.formatKey(this.keyblob);
    }

    @Override
    public SshKeyPair toKeyPair(String passphrase) throws IOException, InvalidPassphraseException {
        try (ByteArrayReader bar = new ByteArrayReader(this.decryptKey(passphrase));){
            String algorithm = bar.readString();
            if (algorithm.equals("ssh-dss")) {
                BigInteger p = bar.readBigInteger();
                BigInteger q = bar.readBigInteger();
                BigInteger g = bar.readBigInteger();
                BigInteger x = bar.readBigInteger();
                SshDsaPrivateKey prv = ComponentManager.getInstance().createDsaPrivateKey(p, q, g, x, g.modPow(x, p));
                SshKeyPair pair = new SshKeyPair();
                pair.setPublicKey(prv.getPublicKey());
                pair.setPrivateKey(ComponentManager.getInstance().createDsaPrivateKey(p, q, g, x, g.modPow(x, p)));
                SshKeyPair sshKeyPair = pair;
                return sshKeyPair;
            }
            if (algorithm.equals("ssh-rsa")) {
                BigInteger publicExponent = bar.readBigInteger();
                BigInteger modulus = bar.readBigInteger();
                BigInteger privateExponent = bar.readBigInteger();
                SshKeyPair pair = new SshKeyPair();
                pair.setPublicKey(ComponentManager.getInstance().createRsaPublicKey(modulus, publicExponent, 2));
                pair.setPrivateKey(ComponentManager.getInstance().createRsaPrivateKey(modulus, privateExponent));
                SshKeyPair sshKeyPair = pair;
                return sshKeyPair;
            }
            try {
                throw new IOException("Unsupported private key algorithm type " + algorithm);
            }
            catch (SshException e) {
                throw new SshIOException(e);
            }
        }
    }

    @Override
    public void changePassphrase(String oldpassphrase, String newpassphrase) throws IOException, InvalidPassphraseException {
        this.keyblob = this.encryptKey(this.decryptKey(oldpassphrase), newpassphrase);
    }

    private byte[] makePassphraseKey(String passphrase) throws SshException {
        Digest md5 = (Digest)ComponentManager.getInstance().supportedDigests().getInstance("MD5");
        md5.putBytes(passphrase.getBytes());
        byte[] key1 = md5.doFinal();
        md5.reset();
        md5.putBytes(passphrase.getBytes());
        md5.putBytes(key1);
        byte[] key2 = md5.doFinal();
        byte[] key = new byte[32];
        System.arraycopy(key1, 0, key, 0, 16);
        System.arraycopy(key2, 0, key, 16, 16);
        return key;
    }
}

