/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.ssh.SshKeyFingerprint;
import com.maverick.ssh.components.SshCertificate;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPrivateKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.jce.OpenSshCertificate;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKeySHA256;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKeySHA512;
import com.maverick.util.Base64;
import com.maverick.util.IOUtil;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.publickey.SshPublicKeyFile;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.StringTokenizer;
import javax.crypto.Cipher;

public class SshKeyUtils {
    public static String getOpenSSHFormattedKey(SshPublicKey key) throws IOException {
        return SshKeyUtils.getFormattedKey(key, "", 0);
    }

    public static String getOpenSSHFormattedKey(SshPublicKey key, String comment) throws IOException {
        return SshKeyUtils.getFormattedKey(key, comment, 0);
    }

    public static String getFormattedKey(SshPublicKey key, String comment, int format) throws IOException {
        SshPublicKeyFile file = SshPublicKeyFileFactory.create(key, comment, format);
        return new String(file.getFormattedKey(), "UTF-8");
    }

    public static String getFormattedKey(SshPublicKey key, String comment) throws IOException {
        SshPublicKeyFile file = SshPublicKeyFileFactory.create(key, comment, 0);
        return new String(file.getFormattedKey(), "UTF-8");
    }

    public static SshPublicKey getPublicKey(File key) throws IOException {
        return SshKeyUtils.getPublicKey(IOUtil.toUTF8String(new FileInputStream(key)));
    }

    public static SshPublicKey getPublicKey(InputStream key) throws IOException {
        return SshKeyUtils.getPublicKey(IOUtil.toUTF8String(key));
    }

    public static SshPublicKey getPublicKey(String formattedKey) throws IOException {
        SshPublicKeyFile file = SshPublicKeyFileFactory.parse(formattedKey.getBytes("UTF-8"));
        return file.toPublicKey();
    }

    public static String getPublicKeyComment(String formattedKey) throws IOException {
        SshPublicKeyFile file = SshPublicKeyFileFactory.parse(formattedKey.getBytes("UTF-8"));
        return file.getComment();
    }

    public static SshKeyPair getPrivateKey(File key, String passphrase) throws IOException, InvalidPassphraseException {
        return SshKeyUtils.getPrivateKey(IOUtil.toUTF8String(new FileInputStream(key)), passphrase);
    }

    public static SshKeyPair getPrivateKeyOrCertificate(File key, String passphrase) throws IOException, InvalidPassphraseException {
        File certFile = new File(key.getParentFile(), key.getName() + "-cert.pub");
        if (certFile.exists()) {
            return SshKeyUtils.getCertificate(key, passphrase);
        }
        return SshKeyUtils.getPrivateKey(IOUtil.toUTF8String(new FileInputStream(key)), passphrase);
    }

    public static SshKeyPair getPrivateKey(InputStream key, String passphrase) throws IOException, InvalidPassphraseException {
        return SshKeyUtils.getPrivateKey(IOUtil.toUTF8String(key), passphrase);
    }

    public static SshKeyPair getPrivateKey(String formattedKey, String passphrase) throws IOException, InvalidPassphraseException {
        SshPrivateKeyFile file = SshPrivateKeyFileFactory.parse(formattedKey.getBytes("UTF-8"));
        return file.toKeyPair(passphrase);
    }

    public static SshCertificate getCertificate(File privateKey, String passphrase) throws IOException, InvalidPassphraseException {
        return SshKeyUtils.getCertificate(privateKey, passphrase, new File(privateKey.getAbsolutePath() + "-cert.pub"));
    }

    public static SshCertificate getCertificate(File privateKey, String passphrase, File certFile) throws IOException, InvalidPassphraseException {
        if (!certFile.exists()) {
            throw new IOException(String.format("No certificate file %s to match private key file %s", certFile.getName(), privateKey.getName()));
        }
        SshKeyPair pair = SshKeyUtils.getPrivateKey(privateKey, passphrase);
        SshPublicKey publicKey = SshKeyUtils.getPublicKey(certFile);
        if (!(publicKey instanceof OpenSshCertificate)) {
            throw new IOException(String.format("%s is not a certificate file", certFile.getName()));
        }
        return new SshCertificate(pair, (OpenSshCertificate)publicKey);
    }

    public static SshCertificate getCertificate(InputStream privateKey, String passphrase, InputStream certFile) throws IOException, InvalidPassphraseException {
        SshKeyPair pair = SshKeyUtils.getPrivateKey(privateKey, passphrase);
        SshPublicKey publicKey = SshKeyUtils.getPublicKey(certFile);
        if (!(publicKey instanceof OpenSshCertificate)) {
            throw new IOException("Stream input is not a certificate file");
        }
        return new SshCertificate(pair, (OpenSshCertificate)publicKey);
    }

    public static SshCertificate getCertificate(String privateKey, String passphrase, String certFile) throws IOException, InvalidPassphraseException {
        SshKeyPair pair = SshKeyUtils.getPrivateKey(privateKey, passphrase);
        SshPublicKey publicKey = SshKeyUtils.getPublicKey(certFile);
        if (!(publicKey instanceof OpenSshCertificate)) {
            throw new IOException("String input is not a certificate file");
        }
        return new SshCertificate(pair, (OpenSshCertificate)publicKey);
    }

    public static SshKeyPair makeRSAWithSHA256Signature(SshKeyPair pair) {
        SshKeyPair n = new SshKeyPair();
        n.setPrivateKey(pair.getPrivateKey());
        n.setPublicKey(new Ssh2RsaPublicKeySHA256((SshRsaPublicKey)pair.getPublicKey()));
        return n;
    }

    public static SshKeyPair getRSAPrivateKeyWithSHA256Signature(String formattedKey, String passphrase) throws UnsupportedEncodingException, IOException, InvalidPassphraseException {
        SshPrivateKeyFile file = SshPrivateKeyFileFactory.parse(formattedKey.getBytes("UTF-8"));
        return SshKeyUtils.makeRSAWithSHA256Signature(file.toKeyPair(passphrase));
    }

    public static SshKeyPair getRSAPrivateKeyWithSHA256Signature(InputStream key, String passphrase) throws IOException, InvalidPassphraseException {
        return SshKeyUtils.makeRSAWithSHA256Signature(SshKeyUtils.getPrivateKey(key, passphrase));
    }

    public static SshKeyPair getRSAPrivateKeyWithSHA256Signature(File key, String passphrase) throws IOException, InvalidPassphraseException {
        return SshKeyUtils.makeRSAWithSHA256Signature(SshKeyUtils.getPrivateKey(key, passphrase));
    }

    public static SshKeyPair makeRSAWithSHA512Signature(SshKeyPair pair) {
        SshKeyPair n = new SshKeyPair();
        n.setPrivateKey(pair.getPrivateKey());
        n.setPublicKey(new Ssh2RsaPublicKeySHA512((SshRsaPublicKey)pair.getPublicKey()));
        return n;
    }

    public static SshKeyPair getRSAPrivateKeyWithSHA512Signature(String formattedKey, String passphrase) throws UnsupportedEncodingException, IOException, InvalidPassphraseException {
        SshPrivateKeyFile file = SshPrivateKeyFileFactory.parse(formattedKey.getBytes("UTF-8"));
        return SshKeyUtils.makeRSAWithSHA512Signature(file.toKeyPair(passphrase));
    }

    public static SshKeyPair getRSAPrivateKeyWithSHA512Signature(InputStream key, String passphrase) throws IOException, InvalidPassphraseException {
        return SshKeyUtils.makeRSAWithSHA512Signature(SshKeyUtils.getPrivateKey(key, passphrase));
    }

    public static SshKeyPair getRSAPrivateKeyWithSHA512Signature(File key, String passphrase) throws IOException, InvalidPassphraseException {
        return SshKeyUtils.makeRSAWithSHA512Signature(SshKeyUtils.getPrivateKey(key, passphrase));
    }

    public static String getFingerprint(SshPublicKey key) {
        return SshKeyFingerprint.getFingerprint(key);
    }

    public static String getBubbleBabble(SshPublicKey pub) {
        return SshKeyFingerprint.getBubbleBabble(pub);
    }

    public static String encrypt(SshRsaPrivateKey privateKey, String toEncrypt) throws Exception {
        int count;
        StringBuffer ret = new StringBuffer();
        int blockLength = privateKey.getModulus().bitLength() / 16;
        for (int pos = 0; pos < toEncrypt.length(); pos += count) {
            count = Math.min(toEncrypt.length() - pos, blockLength);
            ret.append(SshKeyUtils.doEncrypt(toEncrypt.substring(pos, pos + count), privateKey.getJCEPrivateKey()));
            ret.append('|');
        }
        return ret.toString();
    }

    public static String decrypt(SshRsaPublicKey publicKey, String toDecrypt) throws Exception {
        StringBuffer ret = new StringBuffer();
        StringTokenizer t = new StringTokenizer(toDecrypt, "|");
        while (t.hasMoreTokens()) {
            String data = t.nextToken();
            ret.append(SshKeyUtils.doDecrypt(data, publicKey.getJCEPublicKey()));
        }
        return ret.toString();
    }

    private static String doEncrypt(String toEncrypt, PrivateKey privateKey) throws Exception {
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(1, privateKey);
        return Base64.encodeBytes(c.doFinal(toEncrypt.getBytes("UTF-8")), true);
    }

    private static String doDecrypt(String toDecrypt, PublicKey publicKey) throws Exception {
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(2, publicKey);
        return new String(c.doFinal(Base64.decode(toDecrypt)), "UTF-8");
    }

    public static String encrypt(SshRsaPublicKey publicKey, String toEncrypt) throws Exception {
        int count;
        StringBuffer ret = new StringBuffer();
        int blockLength = publicKey.getModulus().bitLength() / 16;
        for (int pos = 0; pos < toEncrypt.length(); pos += count) {
            count = Math.min(toEncrypt.length() - pos, blockLength);
            ret.append(SshKeyUtils.doEncrypt(toEncrypt.substring(pos, pos + count), publicKey.getJCEPublicKey()));
            ret.append('|');
        }
        return ret.toString();
    }

    public static String decrypt(SshRsaPrivateKey privateKey, String toDecrypt) throws Exception {
        StringBuffer ret = new StringBuffer();
        StringTokenizer t = new StringTokenizer(toDecrypt, "|");
        while (t.hasMoreTokens()) {
            String data = t.nextToken();
            ret.append(SshKeyUtils.doDecrypt(data, privateKey.getJCEPrivateKey()));
        }
        return ret.toString();
    }

    private static String doEncrypt(String toEncrypt, PublicKey publicKey) throws Exception {
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(1, publicKey);
        return Base64.encodeBytes(c.doFinal(toEncrypt.getBytes("UTF-8")), true);
    }

    private static String doDecrypt(String toDecrypt, PrivateKey privateKey) throws Exception {
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(2, privateKey);
        return new String(c.doFinal(Base64.decode(toDecrypt)), "UTF-8");
    }

    public static String encryptOAEP(SshRsaPublicKey publicKey, String toEncrypt) throws Exception {
        int count;
        StringBuffer ret = new StringBuffer();
        int blockLength = publicKey.getModulus().bitLength() / 16;
        for (int pos = 0; pos < toEncrypt.length(); pos += count) {
            count = Math.min(toEncrypt.length() - pos, blockLength);
            ret.append(SshKeyUtils.doOAEPSHA256Encrypt(toEncrypt.substring(pos, pos + count), publicKey.getJCEPublicKey()));
            ret.append('|');
        }
        return ret.toString();
    }

    public static String decryptOAEP(SshRsaPrivateKey privateKey, String toDecrypt) throws Exception {
        StringBuffer ret = new StringBuffer();
        StringTokenizer t = new StringTokenizer(toDecrypt, "|");
        while (t.hasMoreTokens()) {
            String data = t.nextToken();
            ret.append(SshKeyUtils.doOAEPSHA256Decrypt(data, privateKey.getJCEPrivateKey()));
        }
        return ret.toString();
    }

    private static String doOAEPSHA256Encrypt(String toEncrypt, PublicKey publicKey) throws Exception {
        Cipher c = Cipher.getInstance("RSA/None/OAEPWithSHA256AndMGF1Padding");
        c.init(1, publicKey);
        return Base64.encodeBytes(c.doFinal(toEncrypt.getBytes("UTF-8")), true);
    }

    private static String doOAEPSHA256Decrypt(String toDecrypt, PrivateKey privateKey) throws Exception {
        Cipher c = Cipher.getInstance("RSA/None/OAEPWithSHA256AndMGF1Padding");
        c.init(2, privateKey);
        return new String(c.doFinal(Base64.decode(toDecrypt)), "UTF-8");
    }

    public static void savePrivateKey(SshKeyPair pair, String passphrase, String comment, File privateKeyFile) throws IOException {
        SshPrivateKeyFile privateFile = SshPrivateKeyFileFactory.create(pair, passphrase, comment, 0);
        IOUtil.writeBytesToFile(privateFile.getFormattedKey(), privateKeyFile);
        SshKeyUtils.savePublicKey(pair.getPublicKey(), comment, new File(privateKeyFile.getParent(), privateKeyFile.getName() + ".pub"));
    }

    public static void saveCertificate(SshCertificate pair, String passphrase, String comment, File privateKeyFile) throws IOException {
        SshPrivateKeyFile privateFile = SshPrivateKeyFileFactory.create(pair, passphrase, comment, 0);
        IOUtil.writeBytesToFile(privateFile.getFormattedKey(), privateKeyFile);
        SshKeyUtils.savePublicKey(pair.getPublicKey(), comment, new File(privateKeyFile.getParent(), privateKeyFile.getName() + ".pub"));
        SshKeyUtils.savePublicKey(pair.getCertificate(), comment, new File(privateKeyFile.getParent(), privateKeyFile.getName() + "-cert.pub"));
    }

    private static void savePublicKey(SshPublicKey publicKey, String comment, File publicKeyFile) throws IOException {
        SshPublicKeyFile publicFile = SshPublicKeyFileFactory.create(publicKey, comment, 0);
        IOUtil.writeBytesToFile(publicFile.getFormattedKey(), publicKeyFile);
    }
}

