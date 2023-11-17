/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshRsaPrivateCrtKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.SshSecureRandomGenerator;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import java.io.IOException;
import java.math.BigInteger;

class Ssh1RsaPrivateKeyFile
implements SshPrivateKeyFile {
    public static final String IDENTIFIER = "SSH PRIVATE KEY FILE FORMAT 1.1\n";
    String comment;
    byte[] formattedkey;

    Ssh1RsaPrivateKeyFile(byte[] formattedkey) throws IOException {
        if (!Ssh1RsaPrivateKeyFile.isFormatted(formattedkey)) {
            throw new IOException("SSH1 RSA Key required");
        }
        this.formattedkey = formattedkey;
    }

    Ssh1RsaPrivateKeyFile(SshKeyPair pair, String passphrase, String comment) throws IOException {
        this.formattedkey = this.encryptKey(pair, passphrase, comment);
    }

    @Override
    public boolean supportsPassphraseChange() {
        return true;
    }

    @Override
    public String getType() {
        return "SSH1";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isPassphraseProtected() {
        boolean bl;
        ByteArrayReader bar = new ByteArrayReader(this.formattedkey);
        try {
            byte[] id = new byte[IDENTIFIER.length()];
            bar.read(id);
            String idStr = new String(id);
            bar.read();
            if (!idStr.equals(IDENTIFIER)) {
                boolean bl2 = false;
                return bl2;
            }
            int cipherType = bar.read();
            bl = cipherType != 0;
        }
        catch (IOException ex) {
            boolean bl3 = false;
            return bl3;
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
        return bl;
    }

    @Override
    public SshKeyPair toKeyPair(String passphrase) throws IOException, InvalidPassphraseException {
        return this.parse(this.formattedkey, passphrase);
    }

    public static boolean isFormatted(byte[] formattedkey) {
        String tmp = new String(formattedkey);
        return tmp.startsWith(IDENTIFIER.trim());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SshKeyPair parse(byte[] formattedkey, String passphrase) throws IOException, InvalidPassphraseException {
        try (ByteArrayReader bar = new ByteArrayReader(formattedkey);){
            SshKeyPair sshKeyPair;
            byte[] id = new byte[IDENTIFIER.length()];
            bar.read(id);
            String idStr = new String(id);
            bar.read();
            if (!idStr.equals(IDENTIFIER)) {
                throw new IOException("RSA key file corrupt");
            }
            int cipherType = bar.read();
            if (cipherType != 3 && cipherType != 0) {
                throw new IOException("Private key cipher type is not supported!");
            }
            bar.readInt();
            bar.readInt();
            BigInteger n = bar.readMPINT();
            BigInteger e = bar.readMPINT();
            SshRsaPublicKey publickey = ComponentManager.getInstance().createRsaPublicKey(n, e, 1);
            this.comment = bar.readString();
            byte[] rest = new byte[8192];
            int len = bar.read(rest);
            byte[] encrypted = new byte[len];
            System.arraycopy(rest, 0, encrypted, 0, len);
            if (cipherType == 3) {
                SshCipher cipher = (SshCipher)ComponentManager.getInstance().supportedSsh1CiphersCS().getInstance("3");
                byte[] iv = new byte[cipher.getBlockSize()];
                cipher.init(1, iv, this.makePassphraseKey(passphrase));
                cipher.transform(encrypted, 0, encrypted, 0, encrypted.length);
            }
            bar.close();
            bar = new ByteArrayReader(encrypted);
            try {
                byte c1 = (byte)bar.read();
                byte c2 = (byte)bar.read();
                byte c11 = (byte)bar.read();
                byte c22 = (byte)bar.read();
                if (c1 != c11 || c2 != c22) {
                    throw new InvalidPassphraseException();
                }
                BigInteger d = bar.readMPINT();
                BigInteger u = bar.readMPINT();
                BigInteger p = bar.readMPINT();
                BigInteger q = bar.readMPINT();
                SshKeyPair pair = new SshKeyPair();
                pair.setPrivateKey(ComponentManager.getInstance().createRsaPrivateCrtKey(publickey.getModulus(), publickey.getPublicExponent(), d, p, q, u));
                pair.setPublicKey(publickey);
                sshKeyPair = pair;
            }
            catch (Throwable throwable) {
                try {
                    bar.close();
                    throw throwable;
                }
                catch (SshException e2) {
                    throw new SshIOException(e2);
                }
            }
            bar.close();
            return sshKeyPair;
        }
    }

    public byte[] encryptKey(SshKeyPair pair, String passphrase, String comment) throws IOException {
        try (ByteArrayWriter baw = new ByteArrayWriter();){
            if (pair.getPrivateKey() instanceof SshRsaPrivateCrtKey) {
                SshRsaPrivateCrtKey privatekey = (SshRsaPrivateCrtKey)pair.getPrivateKey();
                byte[] c = new byte[2];
                SshSecureRandomGenerator rnd = ComponentManager.getInstance().getRND();
                rnd.nextBytes(c);
                baw.write(c[0]);
                baw.write(c[1]);
                baw.write(c[0]);
                baw.write(c[1]);
                baw.writeMPINT(privatekey.getPrivateExponent());
                baw.writeMPINT(privatekey.getCrtCoefficient());
                baw.writeMPINT(privatekey.getPrimeP());
                baw.writeMPINT(privatekey.getPrimeQ());
                byte[] encrypted = baw.toByteArray();
                c = new byte[8 - encrypted.length % 8 + encrypted.length];
                System.arraycopy(encrypted, 0, c, 0, encrypted.length);
                encrypted = c;
                int cipherType = 3;
                SshCipher cipher = (SshCipher)ComponentManager.getInstance().supportedSsh1CiphersCS().getInstance("3");
                byte[] iv = new byte[cipher.getBlockSize()];
                cipher.init(0, iv, this.makePassphraseKey(passphrase));
                cipher.transform(encrypted, 0, encrypted, 0, encrypted.length);
                baw.reset();
                baw.write(IDENTIFIER.getBytes());
                baw.write(0);
                baw.write(cipherType);
                baw.writeInt(0);
                baw.writeInt(0);
                baw.writeMPINT(privatekey.getModulus());
                baw.writeMPINT(privatekey.getPublicExponent());
                baw.writeString(comment);
                baw.write(encrypted, 0, encrypted.length);
                byte[] byArray = baw.toByteArray();
                return byArray;
            }
            try {
                throw new IOException("RSA Private key required!");
            }
            catch (SshException e) {
                throw new SshIOException(e);
            }
        }
    }

    @Override
    public void changePassphrase(String oldpassphrase, String newpassphrase) throws IOException, InvalidPassphraseException {
        this.formattedkey = this.encryptKey(this.parse(this.formattedkey, oldpassphrase), newpassphrase, this.comment);
    }

    @Override
    public byte[] getFormattedKey() {
        return this.formattedkey;
    }

    private byte[] makePassphraseKey(String passphrase) throws SshException {
        Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance("MD5");
        byte[] key = new byte[32];
        hash.putBytes(passphrase.getBytes());
        byte[] digest = hash.doFinal();
        System.arraycopy(digest, 0, key, 0, 16);
        System.arraycopy(digest, 0, key, 16, 16);
        return key;
    }
}

