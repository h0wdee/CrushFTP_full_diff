/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.SshDsaPrivateKey;
import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPrivateCrtKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.jce.ECUtils;
import com.maverick.ssh.components.jce.JCEComponentManager;
import com.maverick.ssh.components.jce.Ssh2DsaPrivateKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPrivateKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPublicKey;
import com.maverick.ssh.components.jce.SshEd25519PrivateKeyJCE;
import com.maverick.ssh.components.jce.SshEd25519PublicKey;
import com.maverick.util.BCryptKDF;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.SimpleASNReader;
import com.maverick.util.SimpleASNWriter;
import com.maverick.util.UnsignedInteger32;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.PEMReader;
import com.sshtools.publickey.PEMWriter;
import com.sshtools.publickey.SshPrivateKeyFile;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;

class OpenSSHPrivateKeyFile
implements SshPrivateKeyFile {
    byte[] formattedkey;
    static final String AUTH_MAGIC = "openssh-key-v1";
    String comment = "";

    OpenSSHPrivateKeyFile(byte[] formattedkey) throws IOException {
        if (!OpenSSHPrivateKeyFile.isFormatted(formattedkey)) {
            throw new IOException("Formatted key data is not a valid OpenSSH key format");
        }
        this.formattedkey = formattedkey;
    }

    OpenSSHPrivateKeyFile(SshKeyPair pair, String passphrase, String comment) throws IOException {
        this.comment = comment;
        this.formattedkey = this.encryptKey(pair, passphrase);
    }

    public OpenSSHPrivateKeyFile() {
    }

    @Override
    public boolean isPassphraseProtected() {
        try {
            StringReader r = new StringReader(new String(this.formattedkey, "US-ASCII"));
            PEMReader pem = new PEMReader(r);
            if (this.isPassphraseProtectedOpenSSHKeyFile()) {
                return true;
            }
            return pem.getHeader().containsKey("DEK-Info");
        }
        catch (IOException e) {
            return true;
        }
    }

    private boolean isPassphraseProtectedOpenSSHKeyFile() throws IOException {
        StringReader r = new StringReader(new String(this.formattedkey, "US-ASCII"));
        PEMReader pem = new PEMReader(r);
        if (pem.getType().equals("OPENSSH PRIVATE KEY")) {
            try (ByteArrayReader bar = new ByteArrayReader(pem.decryptPayload(null));){
                byte[] auth_magic = new byte[14];
                bar.read(auth_magic);
                if (!Arrays.equals(auth_magic, AUTH_MAGIC.getBytes("UTF-8"))) {
                    boolean bl = false;
                    return bl;
                }
                bar.skip(1L);
                String cipherName = bar.readString();
                boolean bl = !cipherName.equals("none");
                return bl;
            }
        }
        return false;
    }

    @Override
    public String getType() {
        return "OpenSSH";
    }

    @Override
    public boolean supportsPassphraseChange() {
        return true;
    }

    @Override
    public SshKeyPair toKeyPair(String passphrase) throws IOException, InvalidPassphraseException {
        StringReader r = new StringReader(new String(this.formattedkey, "US-ASCII"));
        PEMReader pem = new PEMReader(r);
        byte[] payload = pem.decryptPayload(passphrase);
        SimpleASNReader asn = new SimpleASNReader(payload);
        try {
            if ("DSA PRIVATE KEY".equals(pem.getType())) {
                return this.getDSAKeyPair(asn);
            }
            if ("RSA PRIVATE KEY".equals(pem.getType())) {
                return this.getRSAKeyPair(asn);
            }
            if ("EC PRIVATE KEY".equals(pem.getType())) {
                return this.getECKeyPair(asn);
            }
            if ("OPENSSH PRIVATE KEY".equals(pem.getType())) {
                return this.getOpenSSHKeyPair(payload, passphrase);
            }
            throw new IOException("Unsupported type: " + pem.getType());
        }
        catch (SshException | IOException ex) {
            ex.printStackTrace();
            throw new InvalidPassphraseException(ex);
        }
    }

    private void writeOpenSSHKeyPair(ByteArrayWriter writer, SshKeyPair pair, String passphrase) throws IOException, NoSuchAlgorithmException, SshException {
        writer.write(AUTH_MAGIC.getBytes("UTF-8"));
        writer.write(0);
        String cipherName = "none";
        String kdfName = "none";
        byte[] salt = new byte[16];
        JCEComponentManager.getSecureRandom().nextBytes(salt);
        int rounds = 16;
        try (ByteArrayWriter options = new ByteArrayWriter();){
            if (passphrase != null && passphrase.length() > 0) {
                cipherName = "aes256-ctr";
                kdfName = "bcrypt";
                options.writeBinaryString(salt);
                options.writeInt(rounds);
            }
            writer.writeString(cipherName);
            writer.writeString(kdfName);
            writer.writeBinaryString(options.toByteArray());
            writer.writeInt(1);
            writer.writeBinaryString(pair.getPublicKey().getEncoded());
            try (ByteArrayWriter privateKeyData = new ByteArrayWriter();){
                String algorithm;
                int checksum = JCEComponentManager.getSecureRandom().nextInt() & 0xFFFFFFF;
                privateKeyData.writeUINT32(new UnsignedInteger32(checksum));
                privateKeyData.writeUINT32(new UnsignedInteger32(checksum));
                privateKeyData.writeString(pair.getPrivateKey().getAlgorithm());
                switch (algorithm = pair.getPublicKey().getEncodingAlgorithm()) {
                    case "ssh-ed25519": {
                        byte[] a = ((SshEd25519PublicKey)pair.getPublicKey()).getA();
                        privateKeyData.writeBinaryString(a);
                        byte[] sk = ((SshEd25519PrivateKeyJCE)pair.getPrivateKey()).getSeed();
                        privateKeyData.writeInt(64);
                        privateKeyData.write(sk);
                        privateKeyData.write(a);
                        break;
                    }
                    case "ssh-rsa": {
                        SshPublicKey publickey = (SshRsaPublicKey)pair.getPublicKey();
                        SshRsaPrivateCrtKey privatekey = (SshRsaPrivateCrtKey)pair.getPrivateKey();
                        privateKeyData.writeBigInteger(publickey.getModulus());
                        privateKeyData.writeBigInteger(publickey.getPublicExponent());
                        privateKeyData.writeBigInteger(privatekey.getPrivateExponent());
                        privateKeyData.writeBigInteger(privatekey.getCrtCoefficient());
                        privateKeyData.writeBigInteger(privatekey.getPrimeP());
                        privateKeyData.writeBigInteger(privatekey.getPrimeQ());
                        break;
                    }
                    case "ssh-dss": {
                        SshPublicKey publickey = (SshDsaPublicKey)pair.getPublicKey();
                        privateKeyData.writeBigInteger(publickey.getP());
                        privateKeyData.writeBigInteger(publickey.getQ());
                        privateKeyData.writeBigInteger(publickey.getG());
                        privateKeyData.writeBigInteger(publickey.getY());
                        privateKeyData.writeBigInteger(((Ssh2DsaPrivateKey)pair.getPrivateKey()).getX());
                        break;
                    }
                    case "ecdsa-sha2-nistp521": 
                    case "ecdsa-sha2-nistp384": 
                    case "ecdsa-sha2-nistp256": {
                        privateKeyData.writeString(((Ssh2EcdsaSha2NistPublicKey)pair.getPublicKey()).getCurve());
                        privateKeyData.writeBinaryString(((Ssh2EcdsaSha2NistPublicKey)pair.getPublicKey()).getPublicOctet());
                        privateKeyData.writeBinaryString(((ECPrivateKey)pair.getPrivateKey().getJCEPrivateKey()).getS().toByteArray());
                        break;
                    }
                    default: {
                        throw new IOException(String.format("Unsupported public key type %s for OpenSSH private key file format", pair.getPublicKey().getAlgorithm()));
                    }
                }
                if (this.comment == null) {
                    privateKeyData.writeString(String.format("%s@%s", System.getProperty("user.name"), InetAddress.getLocalHost().getHostName()));
                } else {
                    privateKeyData.writeString(this.comment);
                }
                if (!cipherName.equals("none")) {
                    SshCipher cipher = (SshCipher)ComponentManager.getInstance().supportedSsh2CiphersCS().getInstance(cipherName);
                    byte[] iv = new byte[cipher.getBlockSize()];
                    byte[] key = new byte[cipher.getKeyLength()];
                    byte[] keydata = BCryptKDF.bcrypt_pbkdf(passphrase.getBytes("UTF-8"), salt, iv.length + key.length, rounds);
                    System.arraycopy(keydata, 0, key, 0, key.length);
                    System.arraycopy(keydata, key.length, iv, 0, iv.length);
                    cipher.init(0, iv, key);
                    this.pad(privateKeyData, cipher.getBlockSize());
                    byte[] payload = privateKeyData.toByteArray();
                    cipher.transform(payload);
                    writer.writeBinaryString(payload);
                } else {
                    this.pad(privateKeyData, 8);
                    writer.writeBinaryString(privateKeyData.toByteArray());
                }
            }
        }
    }

    private void pad(ByteArrayWriter privateKeyData, int blockSize) {
        int i = 0;
        while (privateKeyData.size() % blockSize != 0) {
            privateKeyData.write(++i);
        }
    }

    /*
     * Exception decompiling
     */
    private SshKeyPair getOpenSSHKeyPair(byte[] payload, String passphrase) throws IOException, InvalidPassphraseException, SshException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 3 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public String oidByteArrayToString(byte[] o) {
        StringBuilder retVal = new StringBuilder();
        int[] oid = new int[o.length];
        for (int x = 0; x < o.length; ++x) {
            oid[x] = o[x] & 0xFF;
        }
        for (int i = 0; i < oid.length; ++i) {
            if (i == 0) {
                int b = oid[0] % 40;
                int a = (oid[0] - b) / 40;
                retVal.append(String.format("%d.%d", a, b));
                continue;
            }
            if (oid[i] < 128) {
                retVal.append(String.format(".%d", oid[i]));
                continue;
            }
            retVal.append(String.format(".%d", (oid[i] - 128) * 128 + oid[i + 1]));
            ++i;
        }
        return retVal.toString();
    }

    SshKeyPair getECKeyPair(SimpleASNReader asn) throws IOException {
        try {
            asn.assertByte(48);
            asn.getLength();
            asn.assertByte(2);
            asn.getData();
            asn.assertByte(4);
            byte[] privateKey = asn.getData();
            asn.assertByte(160);
            asn.getLength();
            asn.assertByte(6);
            byte[] namedCurve = asn.getData();
            asn.assertByte(161);
            asn.getLength();
            asn.assertByte(3);
            byte[] publicKey = asn.getData();
            String curve = this.curveFromOOID(namedCurve);
            ECPublicKey pub = ECUtils.decodeKey(publicKey, curve);
            ECPrivateKey prv = ECUtils.decodePrivateKey(privateKey, pub);
            SshKeyPair pair = new SshKeyPair();
            pair.setPrivateKey(new Ssh2EcdsaSha2NistPrivateKey((PrivateKey)prv, curve));
            pair.setPublicKey(new Ssh2EcdsaSha2NistPublicKey(pub, curve));
            return pair;
        }
        catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private String curveFromOOID(byte[] o) {
        String oid = this.oidByteArrayToString(o);
        if (oid.equals("1.2.840.10045.3.1.7")) {
            return "secp256r1";
        }
        if (oid.equals("1.3.132.0.34")) {
            return "secp384r1";
        }
        if (oid.equals("1.3.132.0.35")) {
            return "secp521r1";
        }
        throw new IllegalArgumentException("Unsupported OID " + oid);
    }

    SshKeyPair getRSAKeyPair(SimpleASNReader asn) throws IOException {
        try {
            asn.assertByte(48);
            asn.getLength();
            asn.assertByte(2);
            asn.getData();
            asn.assertByte(2);
            BigInteger modulus = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger publicExponent = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger privateExponent = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger primeP = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger primeQ = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger primeExponentP = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger primeExponentQ = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger crtCoefficient = new BigInteger(asn.getData());
            SshKeyPair pair = new SshKeyPair();
            pair.setPublicKey(ComponentManager.getInstance().createRsaPublicKey(modulus, publicExponent, 2));
            pair.setPrivateKey(ComponentManager.getInstance().createRsaPrivateCrtKey(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient));
            return pair;
        }
        catch (SshException e) {
            throw new SshIOException(e);
        }
    }

    SshKeyPair getDSAKeyPair(SimpleASNReader asn) throws IOException {
        try {
            asn.assertByte(48);
            asn.getLength();
            asn.assertByte(2);
            asn.getData();
            asn.assertByte(2);
            BigInteger p = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger q = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger g = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger y = new BigInteger(asn.getData());
            asn.assertByte(2);
            BigInteger x = new BigInteger(asn.getData());
            SshKeyPair pair = new SshKeyPair();
            SshDsaPublicKey pub = ComponentManager.getInstance().createDsaPublicKey(p, q, g, y);
            pair.setPublicKey(pub);
            pair.setPrivateKey(ComponentManager.getInstance().createDsaPrivateKey(p, q, g, x, pub.getY()));
            return pair;
        }
        catch (SshException e) {
            throw new SshIOException(e);
        }
    }

    void writeECDSAKeyPair(SimpleASNWriter seq, Ssh2EcdsaSha2NistPrivateKey privatekey, Ssh2EcdsaSha2NistPublicKey publickey) {
        SimpleASNWriter asn = new SimpleASNWriter();
        asn.writeByte(2);
        byte[] version = new byte[1];
        asn.writeData(version);
        asn.writeByte(4);
        asn.writeData(((ECPrivateKey)privatekey.getJCEPrivateKey()).getS().toByteArray());
        asn.writeByte(160);
        SimpleASNWriter oid = new SimpleASNWriter();
        oid.writeByte(6);
        oid.writeData(publickey.getOid());
        byte[] oidBytes = oid.toByteArray();
        asn.writeData(oidBytes);
        asn.writeByte(161);
        SimpleASNWriter pk = new SimpleASNWriter();
        pk.writeByte(3);
        byte[] pub = publickey.getPublicOctet();
        pk.writeLength(pub.length + 1);
        pk.writeByte(0);
        pk.write(pub);
        byte[] pkBytes = pk.toByteArray();
        asn.writeData(pkBytes);
        seq.writeByte(48);
        seq.writeData(asn.toByteArray());
    }

    void writeDSAKeyPair(SimpleASNWriter asn, SshDsaPrivateKey privatekey, SshDsaPublicKey publickey) {
        SimpleASNWriter asn2 = new SimpleASNWriter();
        asn2.writeByte(2);
        byte[] version = new byte[1];
        asn2.writeData(version);
        asn2.writeByte(2);
        asn2.writeData(publickey.getP().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(publickey.getQ().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(publickey.getG().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(publickey.getY().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(privatekey.getX().toByteArray());
        byte[] dsaKeyEncoded = asn2.toByteArray();
        asn.writeByte(48);
        asn.writeData(dsaKeyEncoded);
    }

    void writeRSAKeyPair(SimpleASNWriter asn, SshRsaPrivateCrtKey privatekey) {
        SimpleASNWriter asn2 = new SimpleASNWriter();
        asn2.writeByte(2);
        byte[] version = new byte[1];
        asn2.writeData(version);
        asn2.writeByte(2);
        asn2.writeData(privatekey.getModulus().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(privatekey.getPublicExponent().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(privatekey.getPrivateExponent().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(privatekey.getPrimeP().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(privatekey.getPrimeQ().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(privatekey.getPrimeExponentP().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(privatekey.getPrimeExponentQ().toByteArray());
        asn2.writeByte(2);
        asn2.writeData(privatekey.getCrtCoefficient().toByteArray());
        byte[] rsaKeyEncoded = asn2.toByteArray();
        asn.writeByte(48);
        asn.writeData(rsaKeyEncoded);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public byte[] encryptKey(SshKeyPair pair, String passphrase) throws IOException {
        try (ByteArrayWriter writer = new ByteArrayWriter();){
            StringWriter w = new StringWriter();
            PEMWriter pem = new PEMWriter();
            pem.setType("OPENSSH PRIVATE KEY");
            this.writeOpenSSHKeyPair(writer, pair, passphrase);
            pem.write(w, writer.toByteArray());
            byte[] byArray = w.toString().getBytes("UTF-8");
            return byArray;
        }
        catch (SshException | NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void changePassphrase(String oldpassphrase, String newpassphrase) throws IOException, InvalidPassphraseException {
        SshKeyPair pair = this.toKeyPair(oldpassphrase);
        this.formattedkey = this.encryptKey(pair, newpassphrase);
    }

    @Override
    public byte[] getFormattedKey() {
        return this.formattedkey;
    }

    public static boolean isFormatted(byte[] formattedkey) {
        try {
            StringReader r = new StringReader(new String(formattedkey, "UTF-8"));
            new PEMReader(r);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
}

