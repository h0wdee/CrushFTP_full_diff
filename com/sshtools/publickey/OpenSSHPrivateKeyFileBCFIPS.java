/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.asn1.pkcs.PrivateKeyInfo
 *  org.bouncycastle.openssl.EncryptionException
 *  org.bouncycastle.openssl.PEMEncryptedKeyPair
 *  org.bouncycastle.openssl.PEMKeyPair
 *  org.bouncycastle.openssl.PEMParser
 *  org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
 *  org.bouncycastle.openssl.jcajce.JcaPEMWriter
 *  org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder
 *  org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder
 *  org.bouncycastle.operator.InputDecryptorProvider
 *  org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
 *  org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder
 */
package com.sshtools.publickey;

import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.jce.ECUtils;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.Ssh2DsaPrivateKey;
import com.maverick.ssh.components.jce.Ssh2DsaPublicKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPrivateKey;
import com.maverick.ssh.components.jce.Ssh2EcdsaSha2NistPublicKey;
import com.maverick.ssh.components.jce.Ssh2RsaPrivateCrtKey;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKey;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.PEMReader;
import com.sshtools.publickey.SshPrivateKeyFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.EncryptionException;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;

class OpenSSHPrivateKeyFileBCFIPS
implements SshPrivateKeyFile {
    byte[] formattedkey;

    OpenSSHPrivateKeyFileBCFIPS(byte[] formattedkey) throws IOException {
        if (!OpenSSHPrivateKeyFileBCFIPS.isFormatted(formattedkey)) {
            throw new IOException("Formatted key data is not a valid OpenSSH key format");
        }
        this.formattedkey = formattedkey;
        try {
            this.toKeyPair(null);
        }
        catch (InvalidPassphraseException invalidPassphraseException) {
            // empty catch block
        }
    }

    OpenSSHPrivateKeyFileBCFIPS(SshKeyPair pair, String passphrase) throws IOException {
        this.formattedkey = this.encryptKey(pair, passphrase);
    }

    @Override
    public boolean isPassphraseProtected() {
        try {
            StringReader r = new StringReader(new String(this.formattedkey, "US-ASCII"));
            PEMReader pem = new PEMReader(r);
            return pem.getHeader().containsKey("DEK-Info") || pem.getType().startsWith("ENCRYPTED");
        }
        catch (IOException e) {
            return true;
        }
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
        try (PEMParser pem = new PEMParser((Reader)r);){
            Object obj = pem.readObject();
            if (obj == null) {
                throw new IOException("Invalid key file");
            }
            SshKeyPair pair = new SshKeyPair();
            if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
                if (passphrase == null || passphrase.equals("")) {
                    throw new InvalidPassphraseException();
                }
                PKCS8EncryptedPrivateKeyInfo encPrivKeyInfo = (PKCS8EncryptedPrivateKeyInfo)obj;
                InputDecryptorProvider pkcs8Prov = new JcePKCSPBEInputDecryptorProviderBuilder().setProvider(JCEProvider.getBCProvider().getName()).build(passphrase.toCharArray());
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(JCEProvider.getBCProvider().getName());
                obj = converter.getPrivateKey(encPrivKeyInfo.decryptPrivateKeyInfo(pkcs8Prov));
            }
            if (obj instanceof PEMEncryptedKeyPair) {
                if (passphrase == null || passphrase.equals("")) {
                    throw new InvalidPassphraseException();
                }
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(JCEProvider.getBCProvider().getName());
                obj = converter.getKeyPair(((PEMEncryptedKeyPair)obj).decryptKeyPair(new JcePEMDecryptorProviderBuilder().setProvider(JCEProvider.getBCProvider().getName()).build(passphrase.toCharArray())));
            }
            if (obj instanceof PEMKeyPair) {
                obj = new JcaPEMKeyConverter().setProvider(JCEProvider.getBCProvider().getName()).getKeyPair((PEMKeyPair)obj);
            } else if (obj instanceof PrivateKeyInfo) {
                obj = new JcaPEMKeyConverter().setProvider(JCEProvider.getBCProvider().getName()).getPrivateKey((PrivateKeyInfo)obj);
            }
            if (obj instanceof KeyPair) {
                Object prv;
                KeyPair p = (KeyPair)obj;
                if (p.getPrivate() instanceof ECPrivateKey) {
                    prv = (ECPrivateKey)p.getPrivate();
                    String curve = ECUtils.getNameFromEncodedKey((PrivateKey)prv);
                    pair.setPrivateKey(new Ssh2EcdsaSha2NistPrivateKey((PrivateKey)prv, curve));
                    pair.setPublicKey(new Ssh2EcdsaSha2NistPublicKey((ECPublicKey)p.getPublic(), curve));
                    SshKeyPair sshKeyPair = pair;
                    return sshKeyPair;
                }
                if (p.getPrivate() instanceof RSAPrivateCrtKey) {
                    pair.setPrivateKey(new Ssh2RsaPrivateCrtKey((RSAPrivateCrtKey)p.getPrivate()));
                    pair.setPublicKey(new Ssh2RsaPublicKey((RSAPublicKey)p.getPublic()));
                    prv = pair;
                    return prv;
                }
                if (p.getPrivate() instanceof DSAPrivateKey) {
                    pair.setPrivateKey(new Ssh2DsaPrivateKey((DSAPrivateKey)p.getPrivate(), (DSAPublicKey)p.getPublic()));
                    pair.setPublicKey(new Ssh2DsaPublicKey((DSAPublicKey)p.getPublic()));
                    prv = pair;
                    return prv;
                }
            } else {
                if (obj instanceof DSAPrivateKey) {
                    DSAPrivateKey d = (DSAPrivateKey)obj;
                    try {
                        Ssh2DsaPrivateKey dsa = new Ssh2DsaPrivateKey(d);
                        pair.setPrivateKey(dsa);
                        pair.setPublicKey(dsa.getPublicKey());
                    }
                    catch (Exception e) {
                        throw new IOException("Failed to generate DSA public key from private key: " + e.getMessage());
                    }
                    SshKeyPair e = pair;
                    return e;
                }
                if (obj instanceof RSAPrivateCrtKey) {
                    RSAPrivateCrtKey tmp = (RSAPrivateCrtKey)obj;
                    try {
                        Ssh2RsaPrivateCrtKey rsa = new Ssh2RsaPrivateCrtKey(tmp);
                        pair.setPrivateKey(rsa);
                        pair.setPublicKey(new Ssh2RsaPublicKey(tmp.getModulus(), tmp.getPublicExponent()));
                    }
                    catch (Exception e) {
                        throw new IOException("Failed to generate RSA public key from private key: " + e.getMessage());
                    }
                    SshKeyPair sshKeyPair = pair;
                    return sshKeyPair;
                }
            }
            try {
                throw new IOException("Unsupported type");
            }
            catch (InvalidPassphraseException e) {
                throw e;
            }
            catch (EncryptionException ex) {
                throw new InvalidPassphraseException();
            }
            catch (Throwable e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] encryptKey(SshKeyPair pair, String passphrase) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        JcaPEMWriter pem = new JcaPEMWriter((Writer)new OutputStreamWriter(bout));
        try {
            PublicKey publicKey;
            PrivateKey privateKey;
            if (pair.getPrivateKey() instanceof Ssh2DsaPrivateKey) {
                privateKey = ((Ssh2DsaPrivateKey)pair.getPrivateKey()).getJCEPrivateKey();
                publicKey = ((Ssh2DsaPublicKey)pair.getPublicKey()).getJCEPublicKey();
            } else if (pair.getPrivateKey() instanceof Ssh2RsaPrivateCrtKey) {
                privateKey = ((Ssh2RsaPrivateCrtKey)pair.getPrivateKey()).getJCEPrivateKey();
                publicKey = ((Ssh2RsaPublicKey)pair.getPublicKey()).getJCEPublicKey();
            } else if (pair.getPrivateKey() instanceof Ssh2EcdsaSha2NistPrivateKey) {
                privateKey = ((Ssh2EcdsaSha2NistPrivateKey)pair.getPrivateKey()).getJCEPrivateKey();
                publicKey = ((Ssh2EcdsaSha2NistPublicKey)pair.getPublicKey()).getJCEPublicKey();
            } else {
                throw new IOException(pair.getPrivateKey().getClass().getName() + " is not supported in OpenSSH private key files");
            }
            KeyPair kp = new KeyPair(publicKey, privateKey);
            if (passphrase != null && !"".equals(passphrase)) {
                pem.writeObject((Object)kp, new JcePEMEncryptorBuilder("AES-128-CBC").setProvider(JCEProvider.getBCProvider().getName()).build(passphrase.toCharArray()));
            } else {
                pem.writeObject((Object)kp);
            }
            pem.flush();
            byte[] byArray = bout.toByteArray();
            return byArray;
        }
        finally {
            pem.close();
            bout.close();
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
            PEMReader pem = new PEMReader(r);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
}

