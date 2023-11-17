/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.crypto.generators.Argon2BytesGenerator
 *  org.bouncycastle.crypto.params.Argon2Parameters$Builder
 *  org.bouncycastle.util.Arrays
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshHmac;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh.components.jce.ECUtils;
import com.maverick.ssh.components.jce.HmacSha1;
import com.maverick.ssh.components.jce.HmacSha256;
import com.maverick.ssh.components.jce.JCEComponentManager;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPrivateKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPublicKey;
import com.maverick.ssh.components.jce.SshEd25519PrivateKeyJCE;
import com.maverick.ssh.components.jce.SshEd25519PublicKeyJCE;
import com.maverick.ssh.components.jce.SshEd448PrivateKeyJCE;
import com.maverick.ssh.components.jce.SshEd448PublicKeyJCE;
import com.maverick.util.Base64;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;

class PuTTYPrivateKeyFile
implements SshPrivateKeyFile {
    byte[] formattedKey;

    PuTTYPrivateKeyFile(byte[] formattedKey) throws IOException {
        if (!PuTTYPrivateKeyFile.isFormatted(formattedKey)) {
            throw new IOException("Key is not formatted in the PuTTY key format!");
        }
        this.formattedKey = formattedKey;
    }

    @Override
    public boolean supportsPassphraseChange() {
        return false;
    }

    @Override
    public String getType() {
        return "PuTTY";
    }

    @Override
    public boolean isPassphraseProtected() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.formattedKey)));
        try {
            String line = reader.readLine();
            if (line != null && (line.startsWith("PuTTY-User-Key-File-2:") || line.equals("PuTTY-User-Key-File-1:")) && (line = reader.readLine()) != null && line.startsWith("Encryption:")) {
                String encryption = line.substring(line.indexOf(":") + 1).trim();
                return !encryption.equals("none");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    public static boolean isFormatted(byte[] formattedKey) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(formattedKey)));
        try {
            String line = reader.readLine();
            return line != null && (line.startsWith("PuTTY-User-Key-File-3:") || line.startsWith("PuTTY-User-Key-File-2:") || line.equals("PuTTY-User-Key-File-1:"));
        }
        catch (IOException ex) {
            return false;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public SshKeyPair toKeyPair(String passphrase) throws IOException, InvalidPassphraseException {
        boolean wasEncrpyted;
        block22: {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.formattedKey)));
            wasEncrpyted = false;
            try {
                String line = reader.readLine();
                if (line == null || !line.startsWith("PuTTY-User-Key-File-3:") && !line.startsWith("PuTTY-User-Key-File-2:")) break block22;
                int version = line.startsWith("PuTTY-User-Key-File-3:") ? 3 : 2;
                HashMap<String, String> keyParameters = new HashMap<String, String>();
                String type = Utils.after(line, ':').trim();
                keyParameters.put("Algorithm", type);
                line = reader.readLine();
                if (line == null || !line.startsWith("Encryption:")) break block22;
                String encryption = line.substring(line.indexOf(":") + 1).trim();
                keyParameters.put(Utils.before(line, ':'), encryption);
                line = reader.readLine();
                if (line == null || !line.startsWith("Comment:")) break block22;
                keyParameters.put(Utils.before(line, ':'), Utils.after(line, ':').trim());
                line = reader.readLine();
                if (line == null || !line.startsWith("Public-Lines:")) break block22;
                try {
                    int publiclines = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
                    String publickey = "";
                    for (int i = 0; i < publiclines; ++i) {
                        line = reader.readLine();
                        if (line == null) throw new IOException("Corrupt public key data in PuTTY private key");
                        publickey = publickey + line;
                    }
                    byte[] pub = Base64.decode(publickey);
                    String privatekey = "";
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("Private-Lines:")) {
                            int privatelines = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
                            for (int i = 0; i < privatelines; ++i) {
                                line = reader.readLine();
                                if (line == null) throw new IOException("Corrupt private key data in PuTTY private key");
                                privatekey = privatekey + line;
                            }
                            continue;
                        }
                        keyParameters.put(Utils.before(line, ':'), Utils.after(line, ':').trim());
                    }
                    byte[] prv = Base64.decode(privatekey);
                    if (!encryption.equals("none")) {
                        SshCipher cipher = (SshCipher)JCEComponentManager.getInstance().supportedSsh2CiphersCS().getInstance(encryption);
                        String keyDerivation = (String)keyParameters.get("Key-Derivation");
                        prv = keyDerivation == null ? this.performSHA1Decryption(keyParameters, passphrase, cipher, prv, pub) : this.performDecryption(keyParameters, passphrase, cipher, prv, pub);
                        wasEncrpyted = true;
                    }
                    try (ByteArrayReader bar = new ByteArrayReader(prv);){
                        SshKeyPair sshKeyPair;
                        if (type.equals("ssh-dss")) {
                            sshKeyPair = this.readDsaKey(new ByteArrayReader(pub), bar);
                            return sshKeyPair;
                        }
                        if (type.equals("ssh-rsa")) {
                            sshKeyPair = this.readRsaKey(new ByteArrayReader(pub), bar);
                            return sshKeyPair;
                        }
                        if (type.equals("ssh-ed25519")) {
                            sshKeyPair = this.readEd25519Key(new ByteArrayReader(pub), bar);
                            return sshKeyPair;
                        }
                        if (type.equals("ssh-ed448")) {
                            sshKeyPair = this.readEd448Key(new ByteArrayReader(pub), bar);
                            return sshKeyPair;
                        }
                        if (!type.startsWith("ecdsa")) throw new IOException("Unexpected key type " + type);
                        sshKeyPair = this.readEcdsaKey(new ByteArrayReader(pub), bar);
                        return sshKeyPair;
                    }
                }
                catch (NumberFormatException numberFormatException) {
                }
                catch (OutOfMemoryError outOfMemoryError) {
                }
            }
            catch (Throwable ex) {
                if (wasEncrpyted) break block22;
                throw new IOException("The PuTTY key could not be read! " + ex.getMessage());
            }
        }
        if (!wasEncrpyted) throw new IOException("The PuTTY key could not be read! Invalid format");
        throw new InvalidPassphraseException();
    }

    private byte[] performDecryption(Map<String, String> keyParameters, String passphrase, SshCipher cipher, byte[] prv, byte[] pub) throws IOException {
        int version;
        String keyDerivation;
        switch (keyDerivation = keyParameters.get("Key-Derivation")) {
            case "Argon2d": {
                version = 0;
                break;
            }
            case "Argon2i": {
                version = 1;
                break;
            }
            case "Argon2id": {
                version = 2;
                break;
            }
            default: {
                throw new IOException("Unexpected Key-Derivation value " + keyDerivation);
            }
        }
        int memory = Integer.parseInt(keyParameters.get("Argon2-Memory"));
        int passes = Integer.parseInt(keyParameters.get("Argon2-Passes"));
        int paralledlism = Integer.parseInt(keyParameters.get("Argon2-Parallelism"));
        byte[] salt = Utils.hexToBytes(keyParameters.get("Argon2-Salt"));
        byte[] keydata = this.generate(version, passes, memory, paralledlism, passphrase.getBytes("UTF-8"), salt, 80);
        byte[] key = new byte[32];
        byte[] iv = new byte[16];
        byte[] mac = new byte[32];
        System.arraycopy(keydata, 0, key, 0, key.length);
        System.arraycopy(keydata, key.length, iv, 0, iv.length);
        System.arraycopy(keydata, key.length + iv.length, mac, 0, mac.length);
        cipher.init(1, iv, key);
        cipher.transform(prv);
        this.assertMac(new HmacSha256(), keyParameters, prv, pub, mac);
        return prv;
    }

    private byte[] performSHA1Decryption(Map<String, String> keyParameters, String passphrase, SshCipher cipher, byte[] prv, byte[] pub) throws IOException, SshException {
        byte[] iv = new byte[40];
        byte[] key = new byte[40];
        Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance("SHA-1");
        hash.putInt(0);
        hash.putBytes(passphrase.getBytes());
        byte[] key1 = hash.doFinal();
        hash.putInt(1);
        hash.putBytes(passphrase.getBytes());
        byte[] key2 = hash.doFinal();
        System.arraycopy(key1, 0, key, 0, 20);
        System.arraycopy(key2, 0, key, 20, 20);
        cipher.init(1, iv, key);
        cipher.transform(prv);
        ByteArrayWriter init = new ByteArrayWriter();
        init.write("putty-private-key-file-mac-key".getBytes("UTF-8"));
        if (passphrase != null) {
            init.write(passphrase.getBytes("UTF-8"));
        }
        this.assertMac(new HmacSha1(), keyParameters, prv, pub, Utils.sha1(init.toByteArray()));
        return prv;
    }

    private void assertMac(SshHmac digest, Map<String, String> keyParameters, byte[] prv, byte[] pub, byte[] key) throws IOException {
        ByteArrayWriter w = new ByteArrayWriter();
        w.writeString(keyParameters.get("Algorithm"));
        w.writeString(keyParameters.get("Encryption"));
        w.writeString(keyParameters.get("Comment"));
        w.writeBinaryString(pub);
        w.writeBinaryString(prv);
        try {
            digest.init(key);
            digest.update(w.toByteArray());
            byte[] m = digest.doFinal();
            byte[] m2 = Utils.hexToBytes(keyParameters.get("Private-MAC"));
            if (!Arrays.areEqual((byte[])m, (byte[])m2)) {
                throw new IOException("Invalid mac in PuTTY private key file");
            }
        }
        catch (SshException e) {
            throw new SshIOException(e);
        }
    }

    private SshKeyPair readDsaKey(ByteArrayReader pub, ByteArrayReader bar) throws SshException, IOException {
        pub.readString();
        BigInteger p = pub.readBigInteger();
        BigInteger q = pub.readBigInteger();
        BigInteger g = pub.readBigInteger();
        BigInteger y = pub.readBigInteger();
        BigInteger x = bar.readBigInteger();
        SshKeyPair pair = new SshKeyPair();
        SshDsaPublicKey publ = ComponentManager.getInstance().createDsaPublicKey(p, q, g, y);
        pair.setPublicKey(publ);
        pair.setPrivateKey(ComponentManager.getInstance().createDsaPrivateKey(p, q, g, x, publ.getY()));
        return pair;
    }

    private SshKeyPair readRsaKey(ByteArrayReader pub, ByteArrayReader bar) throws IOException, SshException {
        pub.readString();
        BigInteger publicExponent = pub.readBigInteger();
        BigInteger modulus = pub.readBigInteger();
        BigInteger privateExponent = bar.readBigInteger();
        SshKeyPair pair = new SshKeyPair();
        pair.setPublicKey(ComponentManager.getInstance().createRsaPublicKey(modulus, publicExponent, 2));
        pair.setPrivateKey(ComponentManager.getInstance().createRsaPrivateKey(modulus, privateExponent));
        return pair;
    }

    private SshKeyPair readEcdsaKey(ByteArrayReader pub, ByteArrayReader bar) throws IOException, SshException {
        SshKeyPair pair = new SshKeyPair();
        try {
            SshPublicKey p = SshPublicKeyFileFactory.decodeSSH2PublicKey(pub.array(), pub.getPosition(), pub.available());
            pair.setPublicKey(p);
            byte[] privateKey = bar.readBinaryString();
            ECPrivateKey prv = ECUtils.decodePrivateKey(privateKey, (ECPublicKey)((Ssh2EcdsaSha2NistPublicKey)pair.getPublicKey()).getJCEPublicKey());
            pair.setPrivateKey(new Ssh2EcdsaSha2NistPrivateKey((PrivateKey)prv, ((Ssh2EcdsaSha2NistPublicKey)p).getCurve()));
            return pair;
        }
        catch (InvalidKeySpecException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private SshKeyPair readEd25519Key(ByteArrayReader pub, ByteArrayReader bar) throws IOException, SshException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        SshKeyPair pair = new SshKeyPair();
        String algorithm = pub.readString();
        byte[] publicKey = pub.readBinaryString();
        pair.setPublicKey(new SshEd25519PublicKeyJCE(publicKey));
        byte[] privateKey = bar.readBinaryString();
        pair.setPrivateKey(new SshEd25519PrivateKeyJCE(privateKey, publicKey));
        return pair;
    }

    private SshKeyPair readEd448Key(ByteArrayReader pub, ByteArrayReader bar) throws IOException, SshException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        SshKeyPair pair = new SshKeyPair();
        String algorithm = pub.readString();
        byte[] publicKey = pub.readBinaryString();
        pair.setPublicKey(new SshEd448PublicKeyJCE(publicKey));
        byte[] privateKey = bar.readBinaryString();
        pair.setPrivateKey(new SshEd448PrivateKeyJCE(privateKey));
        return pair;
    }

    @Override
    public void changePassphrase(String oldpassphrase, String newpassprase) throws IOException {
        throw new IOException("Changing passphrase is not supported by the PuTTY key format engine");
    }

    @Override
    public byte[] getFormattedKey() throws IOException {
        return this.formattedKey;
    }

    private byte[] generate(int version, int iterations, int memory, int parallelism, byte[] password, byte[] salt, int outputLength) {
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(version).withVersion(19).withIterations(iterations).withMemoryAsKB(memory).withParallelism(parallelism).withSalt(salt);
        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(builder.build());
        byte[] result = new byte[outputLength];
        gen.generateBytes(password, result, 0, result.length);
        return result;
    }
}

