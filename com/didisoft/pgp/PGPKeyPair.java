/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.ArmoredInputStream
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPKeyRingGenerator
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKeyRing
 *  lw.bouncycastle.openpgp.PGPUtil
 *  lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor
 *  lw.bouncycastle.openpgp.operator.PBESecretKeyEncryptor
 */
package com.didisoft.pgp;

import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.bc.BaseLib;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.bc.PGPObjectFactory2;
import com.didisoft.pgp.exceptions.NoPrivateKeyFoundException;
import com.didisoft.pgp.exceptions.NoPublicKeyFoundException;
import com.didisoft.pgp.exceptions.WrongPasswordException;
import com.didisoft.pgp.exceptions.WrongPrivateKeyException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Logger;
import lw.bouncycastle.bcpg.ArmoredInputStream;
import lw.bouncycastle.openpgp.PGPKeyRingGenerator;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKeyRing;
import lw.bouncycastle.openpgp.PGPUtil;
import lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import lw.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;

public class PGPKeyPair
extends KeyPairInformation
implements Serializable {
    private static final long serialVersionUID = 2538773604623463978L;
    private static final Logger log = Logger.getLogger(BaseLib.class.getName());

    private PGPKeyPair() {
    }

    public PGPKeyPair(String string) throws NoPublicKeyFoundException {
        InputStream inputStream = null;
        try {
            inputStream = IOUtil.readFileOrAsciiString(string, "keyFile");
            this.importKeyFile(inputStream);
        }
        catch (PGPException pGPException) {
            throw new NoPublicKeyFoundException("The specified file does not contain an OpenPGP key.", (Exception)((Object)pGPException));
        }
        catch (IOException iOException) {
            throw new NoPublicKeyFoundException("The specified file does not contain an OpenPGP key.", iOException);
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public PGPKeyPair(String string, String string2) throws NoPublicKeyFoundException, WrongPrivateKeyException {
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            inputStream = IOUtil.readFileOrAsciiString(string, "publicKeyFile");
            inputStream2 = IOUtil.readFileOrAsciiString(string2, "privateKeyFile");
            this.importKeyFile(inputStream);
            this.importKeyFile(inputStream2);
            if (this.getRawPrivateKeyRing().getPublicKey().getKeyID() != this.getRawPublicKeyRing().getPublicKey().getKeyID()) {
                throw new WrongPrivateKeyException("The specified private key does not belong to the public key");
            }
        }
        catch (PGPException pGPException) {
            try {
                throw new NoPublicKeyFoundException("The specified file does not contain an OpenPGP key.", (Exception)((Object)pGPException));
                catch (IOException iOException) {
                    throw new NoPublicKeyFoundException("The specified file does not contain an OpenPGP key.", iOException);
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(inputStream2);
                throw throwable;
            }
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
    }

    public String getAsciiVersionHeader() {
        return "Version: " + this.asciiVersionHeader;
    }

    public void setAsciiVersionHeader(String string) {
        this.asciiVersionHeader = string;
    }

    public static PGPKeyPair generateEccKeyPair(String string, String string2, String string3) throws PGPException {
        int n = 256;
        if ("P256".equalsIgnoreCase(string)) {
            n = 256;
        } else if ("P384".equalsIgnoreCase(string)) {
            n = 384;
        } else if ("P521".equalsIgnoreCase(string)) {
            n = 521;
        } else {
            throw new IllegalArgumentException("The supplied EC Curve parameter is invalid");
        }
        return PGPKeyPair.generateKeyPair(n, string2, "EC", string3, new String[]{"ZIP", "ZLIB", "BZIP2", "UNCOMPRESSED"}, new String[]{"SHA512", "SHA384", "SHA256"}, new String[]{"AES_256", "AES_192", "AES_128"}, 0L);
    }

    public static PGPKeyPair generateEccKeyPair(String string, String string2, String string3, String[] stringArray, String[] stringArray2, String[] stringArray3) throws PGPException {
        int n = 256;
        if ("P256".equalsIgnoreCase(string)) {
            n = 256;
        } else if ("P384".equalsIgnoreCase(string)) {
            n = 384;
        } else if ("P521".equalsIgnoreCase(string)) {
            n = 521;
        } else {
            throw new IllegalArgumentException("The supplied EC Curve parameter is invalid");
        }
        return PGPKeyPair.generateKeyPair(n, string2, "EC", string3, stringArray, stringArray2, stringArray3, 0L);
    }

    public static PGPKeyPair generateEccKeyPair(String string, String string2, String string3, String[] stringArray, String[] stringArray2, String[] stringArray3, long l) throws PGPException {
        int n = 256;
        if ("P256".equalsIgnoreCase(string)) {
            n = 256;
        } else if ("P384".equalsIgnoreCase(string)) {
            n = 384;
        } else if ("P521".equalsIgnoreCase(string)) {
            n = 521;
        } else {
            throw new IllegalArgumentException("The supplied EC Curve parameter is invalid");
        }
        return PGPKeyPair.generateKeyPair(n, string2, "EC", string3, stringArray, stringArray2, stringArray3, l);
    }

    public static PGPKeyPair generateRsaKeyPair(int n, String string, String string2) throws PGPException {
        return PGPKeyPair.generateKeyPair(n, string, "RSA", string2, new String[]{"ZIP", "UNCOMPRESSED", "ZLIB", "BZIP2"}, new String[]{"SHA256", "SHA384", "SHA512", "SHA1", "MD5", "SHA256"}, new String[]{"CAST5", "TRIPLE_DES", "AES_128", "AES_192", "AES_256", "TWOFISH"}, 0L);
    }

    public static PGPKeyPair generateElGamalKeyPair(int n, String string, String string2) throws PGPException {
        return PGPKeyPair.generateKeyPair(n, string, "ELGAMAL", string2, new String[]{"ZIP", "UNCOMPRESSED", "ZLIB", "BZIP2"}, new String[]{"SHA256", "SHA384", "SHA512", "SHA1", "MD5", "SHA256"}, new String[]{"CAST5", "TRIPLE_DES", "AES_128", "AES_192", "AES_256", "TWOFISH"}, 0L);
    }

    public static PGPKeyPair generateKeyPair(int n, String string, String string2, String string3, String[] stringArray, String[] stringArray2, String[] stringArray3, long l) throws PGPException {
        PGPKeyPair pGPKeyPair = new PGPKeyPair();
        String string4 = PGPKeyPair.implode(",", stringArray);
        String string5 = PGPKeyPair.implode(",", stringArray3);
        String string6 = PGPKeyPair.implode(",", stringArray2);
        int n2 = n;
        if ("ELGAMAL".equalsIgnoreCase(string2) || "DSA".equalsIgnoreCase(string2)) {
            n2 = 1024;
        }
        PGPKeyRingGenerator pGPKeyRingGenerator = KeyStore.createKeyPairGenerator(n2, n, string, string2, string3, string4, string6, string5, l, false, false, false);
        PGPSecretKeyRing pGPSecretKeyRing = pGPKeyRingGenerator.generateSecretKeyRing();
        PGPPublicKeyRing pGPPublicKeyRing = pGPKeyRingGenerator.generatePublicKeyRing();
        pGPKeyPair.setPublicKeyRing(pGPPublicKeyRing);
        pGPKeyPair.setPrivateKeyRing(pGPSecretKeyRing);
        return pGPKeyPair;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void importKeyFile(InputStream inputStream) throws IOException, PGPException {
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        try {
            if (inputStream2 instanceof ArmoredInputStream) {
                ArmoredInputStream armoredInputStream = (ArmoredInputStream)inputStream2;
                while (!armoredInputStream.isEndOfStream()) {
                    if (this.parseKeyStream((InputStream)armoredInputStream)) continue;
                    return;
                }
            } else if (!this.parseKeyStream(inputStream2)) {
                return;
            }
        }
        finally {
            inputStream2.close();
        }
    }

    protected boolean parseKeyStream(InputStream inputStream) throws PGPException, IOException {
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
        Object object = pGPObjectFactory2.nextObject();
        while (object != null) {
            PGPPublicKeyRing pGPPublicKeyRing;
            if (object instanceof PGPPublicKeyRing) {
                pGPPublicKeyRing = (PGPPublicKeyRing)object;
                this.setPublicKeyRing(pGPPublicKeyRing);
            } else if (object instanceof PGPSecretKeyRing) {
                pGPPublicKeyRing = (PGPSecretKeyRing)object;
                this.setPrivateKeyRing((PGPSecretKeyRing)pGPPublicKeyRing);
                if (this.getRawPublicKeyRing() == null) {
                    this.setPublicKeyRing(this.bcFactory.CreatePGPPublicKeyRing(pGPPublicKeyRing.getPublicKey().getEncoded()));
                }
            } else {
                throw new PGPException("Unexpected object found in stream: " + object.getClass().getName());
            }
            object = pGPObjectFactory2.nextObject();
        }
        return true;
    }

    public void changePrivateKeyPassword(String string, String string2) throws WrongPasswordException, NoPrivateKeyFoundException, PGPException {
        PGPSecretKeyRing pGPSecretKeyRing = this.getRawPrivateKeyRing();
        if (pGPSecretKeyRing == null) {
            throw new NoPrivateKeyFoundException("This key pair has no private key component.");
        }
        int n = pGPSecretKeyRing.getSecretKey().getKeyEncryptionAlgorithm();
        try {
            pGPSecretKeyRing = PGPSecretKeyRing.copyWithNewPassword((PGPSecretKeyRing)pGPSecretKeyRing, (PBESecretKeyDecryptor)this.bcFactory.CreatePBESecretKeyDecryptor(string), (PBESecretKeyEncryptor)this.bcFactory.CreatePBESecretKeyEncryptor(string2, n));
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            if (pGPException.getMessage().startsWith("checksum mismatch at 0 of 2")) {
                throw new WrongPasswordException(pGPException.getMessage(), pGPException.getUnderlyingException());
            }
            throw IOUtil.newPGPException(pGPException);
        }
        this.setPrivateKeyRing(pGPSecretKeyRing);
    }

    static String implode(String string, String[] stringArray) {
        String string2 = "";
        for (int i = 0; i < stringArray.length; ++i) {
            string2 = string2 + (i == stringArray.length - 1 ? stringArray[i] : stringArray[i] + string);
        }
        return string2;
    }
}

