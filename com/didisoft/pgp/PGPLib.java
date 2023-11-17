/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.ArmoredInputStream
 *  lw.bouncycastle.bcpg.ArmoredOutputStream
 *  lw.bouncycastle.bcpg.BCPGOutputStream
 *  lw.bouncycastle.openpgp.PGPCompressedData
 *  lw.bouncycastle.openpgp.PGPCompressedDataGenerator
 *  lw.bouncycastle.openpgp.PGPDataValidationException
 *  lw.bouncycastle.openpgp.PGPEncryptedDataGenerator
 *  lw.bouncycastle.openpgp.PGPEncryptedDataList
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPLiteralData
 *  lw.bouncycastle.openpgp.PGPLiteralDataGenerator
 *  lw.bouncycastle.openpgp.PGPMarker
 *  lw.bouncycastle.openpgp.PGPObjectFactory
 *  lw.bouncycastle.openpgp.PGPOnePassSignature
 *  lw.bouncycastle.openpgp.PGPOnePassSignatureList
 *  lw.bouncycastle.openpgp.PGPPBEEncryptedData
 *  lw.bouncycastle.openpgp.PGPPrivateKey
 *  lw.bouncycastle.openpgp.PGPPublicKey
 *  lw.bouncycastle.openpgp.PGPPublicKeyEncryptedData
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPPublicKeyRingCollection
 *  lw.bouncycastle.openpgp.PGPSecretKey
 *  lw.bouncycastle.openpgp.PGPSecretKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKeyRingCollection
 *  lw.bouncycastle.openpgp.PGPSignature
 *  lw.bouncycastle.openpgp.PGPSignatureGenerator
 *  lw.bouncycastle.openpgp.PGPSignatureList
 *  lw.bouncycastle.openpgp.PGPSignatureSubpacketGenerator
 *  lw.bouncycastle.openpgp.PGPUtil
 *  lw.bouncycastle.openpgp.PGPV3SignatureGenerator
 *  lw.bouncycastle.openpgp.operator.PGPKeyEncryptionMethodGenerator
 */
package com.didisoft.pgp;

import com.didisoft.pgp.CompressionAlgorithm;
import com.didisoft.pgp.CypherAlgorithm;
import com.didisoft.pgp.HashAlgorithm;
import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.PGPKeyPair;
import com.didisoft.pgp.SignatureCheckResult;
import com.didisoft.pgp.bc.BCFactory;
import com.didisoft.pgp.bc.BaseLib;
import com.didisoft.pgp.bc.DirectByteArrayOutputStream;
import com.didisoft.pgp.bc.DummyStream;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.bc.PGP2xPBEEncryptedData;
import com.didisoft.pgp.bc.PGPObjectFactory2;
import com.didisoft.pgp.bc.PGPPublicKeyExt;
import com.didisoft.pgp.exceptions.DetachedSignatureException;
import com.didisoft.pgp.exceptions.FileIsEncryptedException;
import com.didisoft.pgp.exceptions.FileIsPBEEncryptedException;
import com.didisoft.pgp.exceptions.IntegrityCheckException;
import com.didisoft.pgp.exceptions.KeyIsExpiredException;
import com.didisoft.pgp.exceptions.KeyIsRevokedException;
import com.didisoft.pgp.exceptions.NoPrivateKeyFoundException;
import com.didisoft.pgp.exceptions.NoPublicKeyFoundException;
import com.didisoft.pgp.exceptions.NonPGPDataException;
import com.didisoft.pgp.exceptions.WrongPasswordException;
import com.didisoft.pgp.exceptions.WrongPrivateKeyException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.security.SignatureException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lw.bouncycastle.bcpg.ArmoredInputStream;
import lw.bouncycastle.bcpg.ArmoredOutputStream;
import lw.bouncycastle.bcpg.BCPGOutputStream;
import lw.bouncycastle.openpgp.PGPCompressedData;
import lw.bouncycastle.openpgp.PGPCompressedDataGenerator;
import lw.bouncycastle.openpgp.PGPDataValidationException;
import lw.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import lw.bouncycastle.openpgp.PGPEncryptedDataList;
import lw.bouncycastle.openpgp.PGPLiteralData;
import lw.bouncycastle.openpgp.PGPLiteralDataGenerator;
import lw.bouncycastle.openpgp.PGPMarker;
import lw.bouncycastle.openpgp.PGPObjectFactory;
import lw.bouncycastle.openpgp.PGPOnePassSignature;
import lw.bouncycastle.openpgp.PGPOnePassSignatureList;
import lw.bouncycastle.openpgp.PGPPBEEncryptedData;
import lw.bouncycastle.openpgp.PGPPrivateKey;
import lw.bouncycastle.openpgp.PGPPublicKey;
import lw.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import lw.bouncycastle.openpgp.PGPSecretKey;
import lw.bouncycastle.openpgp.PGPSecretKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import lw.bouncycastle.openpgp.PGPSignature;
import lw.bouncycastle.openpgp.PGPSignatureGenerator;
import lw.bouncycastle.openpgp.PGPSignatureList;
import lw.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import lw.bouncycastle.openpgp.PGPUtil;
import lw.bouncycastle.openpgp.PGPV3SignatureGenerator;
import lw.bouncycastle.openpgp.operator.PGPKeyEncryptionMethodGenerator;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class PGPLib
extends BaseLib {
    private static final String DEFAULT_MESSAGE_FILE_NAME = "message.txt";
    private BCFactory bcFactory = new BCFactory(false);
    private static final int LEGACY_RSA_KEY = 3;
    private List<Integer> hash = new LinkedList<Integer>();
    private List<Integer> cypher = new LinkedList<Integer>();
    private String hashString = "";
    private String cypherString = "";
    private List<Integer> compression = new LinkedList<Integer>();
    private String compressionString = "";
    private boolean useExpiredKeys = false;
    private boolean useRevokedKeys = false;
    private static final Logger log = Logger.getLogger(PGPLib.class.getName());
    private String asciiVersionHeader = null;
    private char contentType = (char)98;
    private Level debugLevel = Level.FINE;
    protected boolean pgp2Compatible = false;
    private boolean extractEmbeddedTar = true;
    private boolean overrideKeyAlgorithmPreferences = false;
    private boolean integrityProtect = true;
    private ArrayList masterKeysList = new ArrayList();

    public PGPLib() {
        this.asciiVersionHeader = version;
        this.cypher.add(9);
        this.compression.add(1);
        this.hash.add(8);
        this.syncCypherString();
        this.syncCompressionString();
    }

    private void syncHashString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.hash.size(); ++i) {
            stringBuilder.append(HashAlgorithm.Enum.fromInt(this.hash.get(i)).name());
            if (i >= this.hash.size() - 1) continue;
            stringBuilder.append(",");
        }
        this.hashString = stringBuilder.toString();
        this.Debug("Preferred hash(es) set to {0}", this.hashString);
    }

    private void syncCypherString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.cypher.size(); ++i) {
            stringBuilder.append(CypherAlgorithm.Enum.fromInt(this.cypher.get(i)).name());
            if (i >= this.cypher.size() - 1) continue;
            stringBuilder.append(",");
        }
        this.cypherString = stringBuilder.toString();
        this.Debug("Preferred cypher(s) set to {0}", this.cypherString);
    }

    private void syncCompressionString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.compression.size(); ++i) {
            stringBuilder.append(HashAlgorithm.Enum.fromInt(this.compression.get(i)).name());
            if (i >= this.compression.size() - 1) continue;
            stringBuilder.append(",");
        }
        this.compressionString = stringBuilder.toString();
        this.Debug("Preferred compression(s) set to {0}", this.compressionString);
    }

    public int getMasterKeysCount() {
        return this.masterKeysList.size();
    }

    public KeyPairInformation getMasterKey(int n) {
        if (n < 0 || n > this.getMasterKeysCount() - 1) {
            throw new IndexOutOfBoundsException();
        }
        return (KeyPairInformation)this.masterKeysList.get(n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public KeyPairInformation addMasterKey(String string) throws IOException, lw.bouncycastle.openpgp.PGPException {
        InputStream inputStream = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string, "publicKeyFileName");
            KeyPairInformation keyPairInformation = this.addMasterKey(inputStream);
            return keyPairInformation;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public KeyPairInformation addMasterKey(InputStream inputStream) throws IOException, lw.bouncycastle.openpgp.PGPException {
        PGPPublicKey pGPPublicKey = this.readPublicKeyForEncryption(inputStream);
        KeyPairInformation keyPairInformation = new KeyPairInformation(new PGPPublicKeyRing(pGPPublicKey.getEncoded(), staticBCFactory.CreateKeyFingerPrintCalculator()));
        this.masterKeysList.add(keyPairInformation);
        return keyPairInformation;
    }

    public KeyPairInformation addMasterKey(KeyPairInformation keyPairInformation) {
        this.masterKeysList.add(keyPairInformation);
        return keyPairInformation;
    }

    public void deleteMasterKey(int n) {
        if (n < 0 || n > this.getMasterKeysCount() - 1) {
            throw new IndexOutOfBoundsException();
        }
        this.masterKeysList.remove(n);
    }

    public char getContentType() {
        return this.contentType;
    }

    public void setContentType(char c) {
        if ('b' != c && 't' != c && 'u' != c) {
            this.Debug("Invalid content type {0}", String.valueOf(c));
            this.Debug("Content type remains {0}", String.valueOf(this.contentType));
            return;
        }
        this.contentType = c;
        this.Debug("Content type set to {0}", String.valueOf(this.contentType));
    }

    public Level getDebugLevel() {
        return this.debugLevel;
    }

    public void setDebuglevel(Level level) {
        this.debugLevel = level;
    }

    public boolean isUseExpiredKeys() {
        return this.useExpiredKeys;
    }

    public void setUseExpiredKeys(boolean bl) {
        this.useExpiredKeys = bl;
    }

    public boolean isUseRevokedKeys() {
        return this.useRevokedKeys;
    }

    public void setUseRevokedKeys(boolean bl) {
        this.useRevokedKeys = bl;
    }

    public void setHash(String string) {
        List<Integer> list = KeyStore.listOfPrefferedHashes(string);
        if (list.contains(-1)) {
            throw new InvalidParameterException("Wrong value for parameter 'hash': " + string + ". Must be one of: SHA1, SHA256, SHA224, SHA384, SHA512, MD5");
        }
        this.hash = list;
        this.syncHashString();
        this.Debug("Preferred hash set to {0}", this.hashString);
    }

    public void setHash(HashAlgorithm.Enum enum_) {
        this.hash.clear();
        this.hash.add(enum_.intValue());
        this.Debug("Preferred hash set to {0}", enum_.name());
    }

    public void setHash(HashAlgorithm.Enum[] enumArray) {
        if (enumArray == null || enumArray.length == 0) {
            throw new InvalidParameterException("Parameter 'hashes' cannot be empty");
        }
        this.hash.clear();
        for (int i = 0; i < enumArray.length; ++i) {
            this.hash.add(enumArray[i].intValue());
        }
        this.syncHashString();
    }

    public String getHash() {
        if (this.hash.size() > 0) {
            return HashAlgorithm.Enum.fromInt(this.hash.get(0)).name();
        }
        return "";
    }

    public HashAlgorithm.Enum getHashEnum() {
        if (this.hash.size() > 0) {
            return HashAlgorithm.Enum.fromInt(this.hash.get(0));
        }
        return null;
    }

    public HashAlgorithm.Enum[] getHashes() {
        LinkedList<HashAlgorithm.Enum> linkedList = new LinkedList<HashAlgorithm.Enum>();
        for (int i = 0; i < this.hash.size(); ++i) {
            linkedList.add(HashAlgorithm.Enum.fromInt(this.hash.get(i)));
        }
        return linkedList.toArray(new HashAlgorithm.Enum[linkedList.size()]);
    }

    public String getCypher() {
        return this.cypherString;
    }

    public CypherAlgorithm.Enum[] getCyphers() {
        LinkedList<CypherAlgorithm.Enum> linkedList = new LinkedList<CypherAlgorithm.Enum>();
        for (int i = 0; i < this.cypher.size(); ++i) {
            linkedList.add(CypherAlgorithm.Enum.fromInt(this.cypher.get(i)));
        }
        return linkedList.toArray(new CypherAlgorithm.Enum[linkedList.size()]);
    }

    public void setCypher(String string) {
        List<Integer> list = KeyStore.listOfPrefferedCyphers(string);
        if (list.contains(-1)) {
            throw new InvalidParameterException("Wrong value for parameter 'cypher': " + string + ". Must be one of: TRIPLE_DES, CAST5, BLOWFISH, AES_128, AES_192, AES_256, TWOFISH, DES, IDEA, SAFER");
        }
        this.cypher = list;
        this.syncCypherString();
    }

    public void setCyphers(CypherAlgorithm.Enum[] enumArray) {
        if (enumArray == null || enumArray.length == 0) {
            throw new InvalidParameterException("Parameter 'cyphers' cannot be empty");
        }
        this.cypher.clear();
        for (int i = 0; i < enumArray.length; ++i) {
            this.cypher.add(enumArray[i].intValue());
        }
        this.syncCypherString();
    }

    public String getAsciiCommentHeader() {
        return "";
    }

    public String getAsciiVersionHeader() {
        return "Version: " + this.asciiVersionHeader;
    }

    public void setAsciiVersionHeader(String string) {
        this.Debug("ASCII version header set to " + string);
        this.asciiVersionHeader = string;
    }

    protected void setAsciiVersionHeader(OutputStream outputStream) {
        if (outputStream instanceof ArmoredOutputStream) {
            ((ArmoredOutputStream)outputStream).setHeader("Version", this.asciiVersionHeader);
        }
    }

    public String getCompression() {
        return this.compressionString;
    }

    public CompressionAlgorithm.Enum[] getCompressions() {
        LinkedList<CompressionAlgorithm.Enum> linkedList = new LinkedList<CompressionAlgorithm.Enum>();
        for (int i = 0; i < this.compression.size(); ++i) {
            linkedList.add(CompressionAlgorithm.Enum.fromInt(this.compression.get(i)));
        }
        return linkedList.toArray(new CompressionAlgorithm.Enum[linkedList.size()]);
    }

    public void setCompression(String string) {
        List<Integer> list = KeyStore.listOfPrefferedCompressions(string);
        if (list.contains(-1)) {
            throw new InvalidParameterException("Wrong value for parameter 'compressions': " + string + ". Must be one of: ZIP, ZLIB, UNCOMPRESSED, BZIP2");
        }
        this.compression = list;
        this.syncCompressionString();
    }

    public void setCompressions(CompressionAlgorithm.Enum[] enumArray) {
        if (enumArray == null || enumArray.length == 0) {
            throw new InvalidParameterException("Parameter 'compressions' cannot be empty");
        }
        this.compression.clear();
        for (int i = 0; i < enumArray.length; ++i) {
            this.compression.add(enumArray[i].intValue());
        }
        this.syncCompressionString();
    }

    public boolean detachedVerifyStream(InputStream inputStream, InputStream inputStream2, InputStream inputStream3) throws PGPException, IOException {
        inputStream2 = PGPLib.cleanGnuPGBackupKeys(inputStream2);
        InputStream inputStream4 = PGPUtil.getDecoderStream((InputStream)inputStream2);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream4);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object instanceof PGPSignatureList) {
            PGPSignatureList pGPSignatureList = (PGPSignatureList)object;
            PGPSignature pGPSignature = null;
            PGPPublicKey pGPPublicKey = null;
            for (int i = 0; i < pGPSignatureList.size(); ++i) {
                pGPSignature = pGPSignatureList.get(i);
                if (pGPSignature == null) {
                    throw new NonPGPDataException("The supplied data is not a valid OpenPGP message: no signature in signature list");
                }
                pGPPublicKey = this.readPublicVerificationKey(inputStream3, pGPSignature.getKeyID());
                if (pGPPublicKey != null) break;
            }
            if (pGPPublicKey == null) {
                return false;
            }
            try {
                int n;
                this.bcFactory.initVerify(pGPSignature, pGPPublicKey);
                byte[] byArray = new byte[0x100000];
                while ((n = inputStream.read(byArray, 0, byArray.length)) > 0) {
                    pGPSignature.update(byArray, 0, n);
                }
                return pGPSignature.verify();
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        throw new PGPException("Unknown message format: " + object);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult detachedVerify(String string, String string2, String string3, String string4) throws IOException, PGPException, FileIsEncryptedException {
        SignatureCheckResult signatureCheckResult;
        if (string == null) {
            throw new IllegalArgumentException("The data parameter cannot be null");
        }
        if (string2 == null) {
            throw new IllegalArgumentException("The signature parameter cannot be null");
        }
        ByteArrayInputStream byteArrayInputStream = null;
        ByteArrayInputStream byteArrayInputStream2 = null;
        InputStream inputStream = null;
        try {
            SignatureCheckResult signatureCheckResult2;
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string4));
            byteArrayInputStream2 = new ByteArrayInputStream(string2.getBytes("UTF-8"));
            inputStream = PGPLib.readFileOrAsciiString(string3, "publicKeyFile");
            signatureCheckResult = signatureCheckResult2 = this.detachedVerify((InputStream)byteArrayInputStream, (InputStream)byteArrayInputStream2, inputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(byteArrayInputStream2);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(byteArrayInputStream2);
        return signatureCheckResult;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult detachedVerify(String string, String string2, KeyStore keyStore, String string3) throws IOException, PGPException, FileIsEncryptedException {
        SignatureCheckResult signatureCheckResult;
        if (string == null) {
            throw new IllegalArgumentException("The data parameter cannot be null");
        }
        if (string2 == null) {
            throw new IllegalArgumentException("The signature parameter cannot be null");
        }
        ByteArrayInputStream byteArrayInputStream = null;
        ByteArrayInputStream byteArrayInputStream2 = null;
        try {
            SignatureCheckResult signatureCheckResult2;
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            byteArrayInputStream2 = new ByteArrayInputStream(string2.getBytes("UTF-8"));
            signatureCheckResult = signatureCheckResult2 = this.detachedVerify((InputStream)byteArrayInputStream, (InputStream)byteArrayInputStream2, keyStore);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(byteArrayInputStream2);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(byteArrayInputStream2);
        return signatureCheckResult;
    }

    public boolean detachedVerifyString(String string, String string2, String string3) throws IOException, PGPException, FileIsEncryptedException {
        return this.detachedVerifyString(string, string2, string3, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean detachedVerifyString(String string, String string2, String string3, String string4) throws IOException, PGPException, FileIsEncryptedException {
        boolean bl;
        if (string == null) {
            throw new IllegalArgumentException("The data parameter cannot be null");
        }
        if (string2 == null) {
            throw new IllegalArgumentException("The signature parameter cannot be null");
        }
        ByteArrayInputStream byteArrayInputStream = null;
        ByteArrayInputStream byteArrayInputStream2 = null;
        InputStream inputStream = null;
        try {
            boolean bl2;
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string4));
            byteArrayInputStream2 = new ByteArrayInputStream(string2.getBytes("UTF-8"));
            inputStream = PGPLib.readFileOrAsciiString(string3, "publicKeyFile");
            bl = bl2 = this.detachedVerifyStream((InputStream)byteArrayInputStream, (InputStream)byteArrayInputStream2, inputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(byteArrayInputStream2);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(byteArrayInputStream2);
        return bl;
    }

    public boolean detachedVerifyString(String string, String string2, KeyStore keyStore) throws IOException, PGPException, FileIsEncryptedException {
        return this.detachedVerifyString(string, string2, keyStore, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean detachedVerifyString(String string, String string2, KeyStore keyStore, String string3) throws IOException, PGPException, FileIsEncryptedException {
        boolean bl;
        if (string == null) {
            throw new IllegalArgumentException("The data parameter cannot be null");
        }
        if (string2 == null) {
            throw new IllegalArgumentException("The signature parameter cannot be null");
        }
        ByteArrayInputStream byteArrayInputStream = null;
        ByteArrayInputStream byteArrayInputStream2 = null;
        try {
            boolean bl2;
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            byteArrayInputStream2 = new ByteArrayInputStream(string2.getBytes("UTF-8"));
            bl = bl2 = this.detachedVerifyStream((InputStream)byteArrayInputStream, (InputStream)byteArrayInputStream2, keyStore);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(byteArrayInputStream2);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(byteArrayInputStream2);
        return bl;
    }

    public boolean detachedVerifyStream(InputStream inputStream, InputStream inputStream2, KeyStore keyStore) throws PGPException, IOException {
        inputStream2 = PGPLib.cleanGnuPGBackupKeys(inputStream2);
        InputStream inputStream3 = PGPUtil.getDecoderStream((InputStream)inputStream2);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object instanceof PGPSignatureList) {
            this.Debug("Detached signature found");
            PGPSignatureList pGPSignatureList = (PGPSignatureList)object;
            PGPSignature pGPSignature = null;
            PGPPublicKey pGPPublicKey = null;
            for (int i = 0; i < pGPSignatureList.size(); ++i) {
                pGPSignature = pGPSignatureList.get(i);
                this.Debug("Detached signature for key ID {0}", KeyPairInformation.keyId2Hex(pGPSignature.getKeyID()));
                pGPPublicKey = PGPLib.readPublicVerificationKey(keyStore, pGPSignature.getKeyID());
                if (pGPPublicKey != null) break;
            }
            if (pGPPublicKey == null) {
                this.Debug("No matching public key found");
                return false;
            }
            try {
                int n;
                this.bcFactory.initVerify(pGPSignature, pGPPublicKey);
                byte[] byArray = new byte[0x100000];
                while ((n = inputStream.read(byArray, 0, byArray.length)) > 0) {
                    pGPSignature.update(byArray, 0, n);
                }
                if (pGPSignature.verify()) {
                    this.Debug("Signature verified");
                    return true;
                }
                this.Debug("Signature cannot be verified. Probably is tampered.");
                return false;
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        throw new PGPException("Unknown message format: " + object);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult detachedVerify(String string, String string2, String string3) throws PGPException, IOException {
        SignatureCheckResult signatureCheckResult;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        InputStream inputStream3 = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string, "message");
            inputStream2 = PGPLib.readFileOrAsciiString(string3, "publicKeyFile");
            inputStream3 = PGPLib.readFileOrAsciiString(string2, "detachedSignature");
            signatureCheckResult = this.detachedVerify(inputStream, inputStream3, inputStream2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            IOUtil.closeStream(inputStream3);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(inputStream3);
        return signatureCheckResult;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult detachedVerify(String string, String string2, KeyStore keyStore) throws PGPException, IOException {
        SignatureCheckResult signatureCheckResult;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string, "message");
            inputStream2 = PGPLib.readFileOrAsciiString(string2, "detachedSignature");
            signatureCheckResult = this.detachedVerify(inputStream, inputStream2, keyStore);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        return signatureCheckResult;
    }

    public SignatureCheckResult detachedVerify(InputStream inputStream, InputStream inputStream2, InputStream inputStream3) throws PGPException, IOException {
        InputStream inputStream4 = PGPUtil.getDecoderStream((InputStream)inputStream2);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream4);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object instanceof PGPSignatureList) {
            PGPSignatureList pGPSignatureList = (PGPSignatureList)object;
            PGPSignature pGPSignature = null;
            PGPPublicKey pGPPublicKey = null;
            for (int i = 0; i < pGPSignatureList.size() && (pGPPublicKey = this.readPublicVerificationKey(inputStream3, (pGPSignature = pGPSignatureList.get(i)).getKeyID())) == null; ++i) {
            }
            if (pGPPublicKey == null) {
                return SignatureCheckResult.PublicKeyNotMatching;
            }
            try {
                int n;
                pGPSignature.init(this.bcFactory.CreatePGPContentVerifierBuilderProvider(), pGPPublicKey);
                byte[] byArray = new byte[0x100000];
                while ((n = inputStream.read(byArray, 0, byArray.length)) > 0) {
                    pGPSignature.update(byArray, 0, n);
                }
                if (pGPSignature.verify()) {
                    return SignatureCheckResult.SignatureVerified;
                }
                return SignatureCheckResult.SignatureBroken;
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        throw new PGPException("Unknown message format: " + object);
    }

    public SignatureCheckResult detachedVerify(InputStream inputStream, InputStream inputStream2, KeyStore keyStore) throws PGPException, IOException {
        InputStream inputStream3 = PGPUtil.getDecoderStream((InputStream)inputStream2);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object instanceof PGPSignatureList) {
            this.Debug("Detached signature found");
            PGPSignatureList pGPSignatureList = (PGPSignatureList)object;
            PGPSignature pGPSignature = null;
            PGPPublicKey pGPPublicKey = null;
            for (int i = 0; i < pGPSignatureList.size(); ++i) {
                pGPSignature = pGPSignatureList.get(i);
                this.Debug("Detached signature for key ID {0}", KeyPairInformation.keyId2Hex(pGPSignature.getKeyID()));
                pGPPublicKey = PGPLib.readPublicVerificationKey(keyStore, pGPSignature.getKeyID());
                if (pGPPublicKey != null) break;
            }
            if (pGPPublicKey == null) {
                this.Debug("No matching public key found");
                return SignatureCheckResult.PublicKeyNotMatching;
            }
            try {
                int n;
                pGPSignature.init(this.bcFactory.CreatePGPContentVerifierBuilderProvider(), pGPPublicKey);
                byte[] byArray = new byte[0x100000];
                while ((n = inputStream.read(byArray, 0, byArray.length)) > 0) {
                    pGPSignature.update(byArray, 0, n);
                }
                if (pGPSignature.verify()) {
                    this.Debug("Signature verified");
                    return SignatureCheckResult.SignatureVerified;
                }
                this.Debug("Signature verified failed. Probably it was tampered.");
                return SignatureCheckResult.SignatureBroken;
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        throw new PGPException("Unknown message format: " + object);
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void detachedSignFile(String string, String string2, String string3, String string4, boolean bl) throws PGPException, IOException {
        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean bl2 = false;
        try {
            fileInputStream = new FileInputStream(string);
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            fileOutputStream = new FileOutputStream(string4);
            this.detachedSignStream(fileInputStream, inputStream, string3, fileOutputStream, bl);
        }
        catch (PGPException pGPException) {
            try {
                bl2 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(fileOutputStream);
                if (!bl2) throw throwable;
                File file = new File(string4);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(fileOutputStream);
        if (!bl2) return;
        File file = new File(string4);
        if (!file.exists()) return;
        file.delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void detachedSignFile(String string, KeyStore keyStore, long l, String string2, String string3, boolean bl) throws PGPException, IOException {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean bl2 = false;
        try {
            fileInputStream = new FileInputStream(string);
            fileOutputStream = new FileOutputStream(string3);
            this.detachedSignStream((InputStream)fileInputStream, keyStore, l, string2, (OutputStream)fileOutputStream, bl);
        }
        catch (PGPException pGPException) {
            try {
                bl2 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(fileOutputStream);
                if (!bl2) throw throwable;
                File file = new File(string3);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(fileOutputStream);
        if (!bl2) return;
        File file = new File(string3);
        if (!file.exists()) return;
        file.delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void detachedSignFile(String string, KeyStore keyStore, String string2, String string3, String string4, boolean bl) throws PGPException, IOException {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean bl2 = false;
        try {
            fileInputStream = new FileInputStream(string);
            fileOutputStream = new FileOutputStream(string4);
            this.detachedSignStream((InputStream)fileInputStream, keyStore, string2, string3, (OutputStream)fileOutputStream, bl);
        }
        catch (PGPException pGPException) {
            try {
                bl2 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(fileOutputStream);
                if (!bl2) throw throwable;
                File file = new File(string4);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(fileOutputStream);
        if (!bl2) return;
        File file = new File(string4);
        if (!file.exists()) return;
        file.delete();
    }

    public void detachedSignStream(InputStream inputStream, InputStream inputStream2, String string, OutputStream outputStream, boolean bl) throws PGPException, IOException {
        PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream2);
        this.internalDetachedSignStream(inputStream, pGPSecretKey, string, outputStream, bl);
    }

    protected void internalDetachedSignStream(InputStream inputStream, PGPSecretKey pGPSecretKey, String string, OutputStream outputStream, boolean bl) throws PGPException, IOException {
        OutputStream outputStream2 = null;
        if (bl) {
            outputStream2 = outputStream;
            outputStream = new ArmoredOutputStream(outputStream2);
            this.setAsciiVersionHeader(outputStream);
        }
        BCPGOutputStream bCPGOutputStream = null;
        PGPV3SignatureGenerator pGPV3SignatureGenerator = null;
        try {
            PGPPrivateKey pGPPrivateKey = PGPLib.extractPrivateKey(pGPSecretKey, string);
            int n = this.selectPreferredHash();
            if (this.pgp2Compatible) {
                n = 1;
            }
            this.Debug("Signing with private key {0}", KeyPairInformation.keyId2Hex(pGPPrivateKey.getKeyID()));
            this.Debug("Signature has is {0}", KeyStore.hashToString(n));
            pGPV3SignatureGenerator = this.bcFactory.CreatePGPV3SignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), n);
            this.bcFactory.initSign(pGPV3SignatureGenerator, 0, pGPPrivateKey);
            bCPGOutputStream = new BCPGOutputStream(outputStream);
            byte[] byArray = new byte[0x100000];
            int n2 = 0;
            while ((n2 = inputStream.read(byArray, 0, byArray.length)) > 0) {
                pGPV3SignatureGenerator.update(byArray, 0, n2);
            }
            IOUtil.closeStream(inputStream);
            if (pGPV3SignatureGenerator != null) {
                pGPV3SignatureGenerator.generate().encode((OutputStream)bCPGOutputStream);
            }
            IOUtil.closeStream((OutputStream)bCPGOutputStream);
            outputStream.flush();
            if (bl) {
                IOUtil.closeStream(outputStream);
            }
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    public void detachedSignStream(InputStream inputStream, KeyStore keyStore, long l, String string, OutputStream outputStream, boolean bl) throws PGPException, IOException {
        PGPSecretKey pGPSecretKey = this.readSecretSigningKey(keyStore, l);
        this.internalDetachedSignStream(inputStream, pGPSecretKey, string, outputStream, bl);
    }

    public void detachedSignStream(InputStream inputStream, KeyStore keyStore, String string, String string2, OutputStream outputStream, boolean bl) throws PGPException, NoPrivateKeyFoundException, IOException {
        long l = keyStore.getKeyIdForUserId(string);
        if (l < 0L) {
            throw new NoPrivateKeyFoundException("No private key pair was found with Id: " + string);
        }
        PGPSecretKey pGPSecretKey = this.readSecretSigningKey(keyStore, keyStore.getKeyIdForUserId(string));
        this.internalDetachedSignStream(inputStream, pGPSecretKey, string2, outputStream, bl);
    }

    public void clearSignStream(InputStream inputStream, InputStream inputStream2, String string, String string2, OutputStream outputStream) throws PGPException, IOException, WrongPasswordException {
        this.Debug("Clear text signing stream");
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream3 = null;
        BufferedOutputStream bufferedOutputStream = null;
        int n = KeyStore.parseHashAlgorithm(string2);
        if (n < 0) {
            throw new InvalidParameterException("Wrong value for parameter 'hashingAlgorithm': " + string2 + ". Must be one of: SHA256, SHA384, SHA512, SHA224, SHA1, MD5, RIPEMD160, MD2");
        }
        try {
            bufferedInputStream = new BufferedInputStream(inputStream);
            inputStream3 = inputStream2;
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream3);
            clearSignedHelper.sign(bufferedInputStream, pGPSecretKey, string, n, bufferedOutputStream);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        catch (IOException iOException) {
            throw iOException;
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void clearSignFile(String string, String string2, String string3, String string4, String string5) throws PGPException, IOException, WrongPasswordException {
        Object object;
        this.Debug("Clear text signing file {0}", string);
        this.Debug("Output file is {0}", string5);
        BufferedInputStream bufferedInputStream = null;
        FileInputStream fileInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bl = false;
        int n = KeyStore.parseHashAlgorithm(string4);
        if (n < 0) {
            throw new InvalidParameterException("Wrong value for parameter 'hashingAlgorithm': " + string4 + ". Must be one of: SHA256, SHA384, SHA512, SHA224, SHA1, MD5, RIPEMD160, MD2");
        }
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            fileInputStream = new FileInputStream(string2);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string5), 0x100000);
            object = new ClearSignedHelper();
            PGPSecretKey pGPSecretKey = this.readSecretSigningKey(fileInputStream);
            ((ClearSignedHelper)object).sign(bufferedInputStream, pGPSecretKey, string3, n, bufferedOutputStream);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (!bl) throw throwable;
                File file = new File(string5);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (!bl) return;
        object = new File(string5);
        if (!((File)object).exists()) return;
        ((File)object).delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void clearSignFileVersion3(String string, String string2, String string3, String string4, String string5) throws PGPException, IOException, WrongPasswordException {
        Object object;
        BufferedInputStream bufferedInputStream = null;
        FileInputStream fileInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bl = false;
        int n = KeyStore.parseHashAlgorithm(string4);
        if (n < 0) {
            throw new InvalidParameterException("Wrong value for parameter 'hashingAlgorithm': " + string4 + ". Must be one of: SHA256, SHA384, SHA512, SHA224, SHA1, MD5, RIPEMD160, MD2");
        }
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            fileInputStream = new FileInputStream(string2);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string5), 0x100000);
            object = new ClearSignedHelper();
            PGPSecretKey pGPSecretKey = this.readSecretSigningKey(fileInputStream);
            ((ClearSignedHelper)object).signV3(bufferedInputStream, pGPSecretKey, string3, n, bufferedOutputStream);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (!bl) throw throwable;
                File file = new File(string5);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (!bl) return;
        object = new File(string5);
        if (!((File)object).exists()) return;
        ((File)object).delete();
    }

    public String clearSignString(String string, String string2, String string3, String string4) throws PGPException, IOException, WrongPasswordException {
        String string5;
        int n = KeyStore.parseHashAlgorithm(string4);
        if (n < 0) {
            throw new InvalidParameterException("Wrong value for parameter 'hashingAlgorithm': " + string4 + ". Must be one of: SHA256, SHA384, SHA512, SHA224, SHA1, MD5, RIPEMD160, MD2");
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string2);
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            PGPSecretKey pGPSecretKey = this.readSecretSigningKey(fileInputStream);
            string5 = clearSignedHelper.sign(string, pGPSecretKey, string3, n);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                throw IOUtil.newPGPException(pGPException);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileInputStream);
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        return string5;
    }

    public String clearSignString(String string, InputStream inputStream, String string2, String string3) throws PGPException, IOException, WrongPasswordException {
        int n = KeyStore.parseHashAlgorithm(string3);
        if (n < 0) {
            throw new InvalidParameterException("Wrong value for parameter 'hashingAlgorithm': " + string3 + ". Must be one of: SHA256, SHA384, SHA512, SHA224, SHA1, MD5, RIPEMD160, MD2");
        }
        InputStream inputStream2 = null;
        try {
            inputStream2 = inputStream;
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream2);
            String string4 = clearSignedHelper.sign(string, pGPSecretKey, string2, n);
            return string4;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream2);
        }
    }

    public String clearSignStringVersion3(String string, String string2, String string3, String string4) throws PGPException, IOException, WrongPasswordException {
        String string5;
        int n = KeyStore.parseHashAlgorithm(string4);
        if (n < 0) {
            throw new InvalidParameterException("Wrong value for parameter 'hashingAlgorithm': " + string4 + ". Must be one of: SHA256, SHA384, SHA512, SHA224, SHA1, MD5, RIPEMD160, MD2");
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string2);
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            PGPSecretKey pGPSecretKey = this.readSecretSigningKey(fileInputStream);
            string5 = clearSignedHelper.signV3(string, pGPSecretKey, string3, n);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                throw IOUtil.newPGPException(pGPException);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileInputStream);
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        return string5;
    }

    public void signFile(KeyStore keyStore, String string, String string2, String string3, String string4) throws PGPException, WrongPasswordException, IOException {
        this.signFile(keyStore, string, keyStore.getKeyIdForKeyIdHex(string2), string3, string4);
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signFile(KeyStore keyStore, String string, long l, String string2, String string3) throws PGPException, WrongPasswordException, IOException {
        File file;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean bl = false;
        try {
            file = new File(string);
            fileInputStream = new FileInputStream(string);
            fileOutputStream = new FileOutputStream(string3);
            PGPSecretKey pGPSecretKey = keyStore.secCollection.getSecretKey(l);
            this.internalSignStream(fileInputStream, file.getName(), pGPSecretKey, string2, fileOutputStream, false);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(fileOutputStream);
                if (!bl) throw throwable;
                File file2 = new File(string3);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(fileOutputStream);
        if (!bl) return;
        file = new File(string3);
        if (!file.exists()) return;
        file.delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signFile(String string, KeyStore keyStore, String string2, String string3, String string4, boolean bl) throws PGPException, WrongPasswordException, IOException {
        Object object;
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean bl2 = false;
        try {
            inputStream = this.createPrivateRingStream(keyStore, string2);
            fileOutputStream = new FileOutputStream(string4);
            object = this.readSecretSigningKey(inputStream);
            File file = new File(string);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            this.internalSignStream(bufferedInputStream, file.getName(), (PGPSecretKey)object, string3, fileOutputStream, bl);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl2 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(fileOutputStream);
                if (!bl2) throw throwable;
                File file = new File(string4);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(fileOutputStream);
        if (!bl2) return;
        object = new File(string4);
        if (!((File)object).exists()) return;
        ((File)object).delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signFile(String string, KeyStore keyStore, long l, String string2, String string3, boolean bl) throws PGPException, WrongPasswordException, IOException {
        Object object;
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean bl2 = false;
        try {
            inputStream = this.createPrivateRingStream(keyStore, l);
            fileOutputStream = new FileOutputStream(string3);
            object = this.readSecretSigningKey(inputStream);
            File file = new File(string);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            this.internalSignStream(bufferedInputStream, file.getName(), (PGPSecretKey)object, string2, fileOutputStream, bl);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl2 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(fileOutputStream);
                if (!bl2) throw throwable;
                File file = new File(string3);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(fileOutputStream);
        if (!bl2) return;
        object = new File(string3);
        if (!((File)object).exists()) return;
        ((File)object).delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signFile(String string, String string2, String string3, String string4, boolean bl) throws IOException, PGPException, WrongPasswordException {
        InputStream inputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bl2 = false;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string4), 0x100000);
            this.signFile(string, inputStream, string3, bufferedOutputStream, bl);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl2 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (!bl2) throw throwable;
                File file = new File(string4);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (!bl2) return;
        File file = new File(string4);
        if (!file.exists()) return;
        file.delete();
    }

    public void signFile(String string, InputStream inputStream, String string2, OutputStream outputStream, boolean bl) throws IOException, PGPException, WrongPasswordException {
        PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream);
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            this.internalSignStream(bufferedInputStream, new File(string).getName(), pGPSecretKey, string2, outputStream, bl);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                throw IOUtil.newPGPException(pGPException);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(outputStream);
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(outputStream);
    }

    public void signStream(InputStream inputStream, String string, InputStream inputStream2, String string2, OutputStream outputStream, boolean bl) throws IOException, PGPException, WrongPasswordException {
        PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream2);
        try {
            this.internalSignStream(inputStream, string, pGPSecretKey, string2, outputStream, bl);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(outputStream);
        }
    }

    public void signStream(InputStream inputStream, String string, KeyStore keyStore, String string2, String string3, OutputStream outputStream, boolean bl) throws IOException, PGPException, WrongPasswordException {
        InputStream inputStream2 = null;
        try {
            inputStream2 = this.createPrivateRingStream(keyStore, string2);
            PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream2);
            this.internalSignStream(inputStream, string, pGPSecretKey, string3, outputStream, bl);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(outputStream);
            IOUtil.closeStream(inputStream2);
        }
    }

    public void signStream(InputStream inputStream, String string, KeyStore keyStore, long l, String string2, OutputStream outputStream, boolean bl) throws IOException, PGPException, WrongPasswordException {
        InputStream inputStream2 = null;
        try {
            inputStream2 = this.createPrivateRingStream(keyStore, l);
            PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream2);
            this.internalSignStream(inputStream, string, pGPSecretKey, string2, outputStream, bl);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(outputStream);
            IOUtil.closeStream(inputStream2);
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signFileVersion3(String string, String string2, String string3, String string4, boolean bl) throws IOException, PGPException, WrongPasswordException {
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bl2 = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string4), 0x100000);
            this.signStreamVersion3(bufferedInputStream, new File(string).getName(), inputStream, string3, bufferedOutputStream, bl);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl2 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (!bl2) throw throwable;
                File file = new File(string4);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (!bl2) return;
        File file = new File(string4);
        if (!file.exists()) return;
        file.delete();
    }

    public void signStreamVersion3(InputStream inputStream, String string, InputStream inputStream2, String string2, OutputStream outputStream, boolean bl) throws IOException, PGPException, WrongPasswordException {
        PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator;
        if (bl) {
            outputStream = new ArmoredOutputStream(outputStream);
            this.setAsciiVersionHeader(outputStream);
        }
        PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream2);
        this.checkKeyIsExpired(pGPSecretKey.getPublicKey());
        this.checkKeyIsRevoked(pGPSecretKey.getPublicKey());
        PGPPrivateKey pGPPrivateKey = PGPLib.extractPrivateKey(pGPSecretKey, string2);
        int n = this.selectPreferredHash();
        PGPSignatureGenerator pGPSignatureGenerator = null;
        try {
            pGPSignatureGenerator = this.bcFactory.CreatePGPSignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), n);
            this.bcFactory.initSign(pGPSignatureGenerator, 0, pGPPrivateKey);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        Iterator iterator = pGPSecretKey.getPublicKey().getRawUserIDs();
        if (iterator.hasNext()) {
            pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
            pGPSignatureSubpacketGenerator.setSignerUserID(false, (byte[])iterator.next());
            pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketGenerator.generate());
        }
        pGPSignatureSubpacketGenerator = new PGPCompressedDataGenerator(2);
        BCPGOutputStream bCPGOutputStream = new BCPGOutputStream(pGPSignatureSubpacketGenerator.open(outputStream));
        DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
        int n2 = 0;
        byte[] byArray = new byte[0x100000];
        while ((n2 = inputStream.read(byArray)) > 0) {
            directByteArrayOutputStream.write(byArray, 0, n2);
            pGPSignatureGenerator.update(byArray, 0, n2);
        }
        try {
            pGPSignatureGenerator.generate().encode((OutputStream)bCPGOutputStream);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        this.Debug("Signing content with file name label {0}. (version 3, old style signature)", string);
        PGPLiteralDataGenerator pGPLiteralDataGenerator = new PGPLiteralDataGenerator();
        OutputStream outputStream2 = pGPLiteralDataGenerator.open((OutputStream)bCPGOutputStream, this.getContentType(), string, (long)directByteArrayOutputStream.size(), new Date());
        outputStream2.write(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size());
        pGPLiteralDataGenerator.close();
        pGPSignatureSubpacketGenerator.close();
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(directByteArrayOutputStream);
        IOUtil.closeStream(outputStream2);
        IOUtil.closeStream((OutputStream)bCPGOutputStream);
        outputStream.flush();
        if (bl) {
            IOUtil.closeStream(outputStream);
        }
    }

    public void signAndEncryptFile(String string, String string2, String string3, String string4, String string5, boolean bl) throws PGPException, WrongPasswordException, IOException {
        this.signAndEncryptFile(string, string2, string3, string4, string5, bl, this.integrityProtect);
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signAndEncryptFile(String string, String string2, String string3, String string4, String string5, boolean bl, boolean bl2) throws PGPException, WrongPasswordException, IOException {
        this.Debug("Signing file {0}", string);
        InputStream inputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        InputStream inputStream2 = null;
        BufferedInputStream bufferedInputStream = null;
        boolean bl3 = false;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string5), 0x100000);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFile");
            this.signAndEncryptStream((InputStream)bufferedInputStream, new File(string).getName(), inputStream, string3, inputStream2, (OutputStream)bufferedOutputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedOutputStream);
                IOUtil.closeStream(inputStream2);
                IOUtil.closeStream(bufferedInputStream);
                if (!bl3) throw throwable;
                File file = new File(string5);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedOutputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(bufferedInputStream);
        if (!bl3) return;
        File file = new File(string5);
        if (!file.exists()) return;
        file.delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signAndEncryptFile(String string, String string2, String string3, String[] stringArray, String string4, boolean bl, boolean bl2) throws PGPException, WrongPasswordException, IOException {
        int n;
        this.Debug("Signing file {0}", string);
        InputStream inputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        InputStream[] inputStreamArray = new InputStream[stringArray.length];
        BufferedInputStream bufferedInputStream = null;
        boolean bl3 = false;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string4), 0x100000);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            for (n = 0; n < stringArray.length; ++n) {
                inputStreamArray[n] = PGPLib.readFileOrAsciiString(stringArray[n], "publicKeyFiles: " + n);
            }
            this.signAndEncryptStream((InputStream)bufferedInputStream, new File(string).getName(), inputStream, string3, inputStreamArray, (OutputStream)bufferedOutputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedOutputStream);
                int n2 = 0;
                while (true) {
                    if (n2 >= inputStreamArray.length) {
                        IOUtil.closeStream(bufferedInputStream);
                        if (!bl3) throw throwable;
                        File file = new File(string4);
                        if (!file.exists()) throw throwable;
                        file.delete();
                        throw throwable;
                    }
                    IOUtil.closeStream(inputStreamArray[n2]);
                    ++n2;
                }
            }
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedOutputStream);
        n = 0;
        while (true) {
            if (n >= inputStreamArray.length) {
                IOUtil.closeStream(bufferedInputStream);
                if (!bl3) return;
                File file = new File(string4);
                if (!file.exists()) return;
                file.delete();
                return;
            }
            IOUtil.closeStream(inputStreamArray[n]);
            ++n;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void signAndEncryptFile(String string, InputStream inputStream, String string2, InputStream inputStream2, OutputStream outputStream, boolean bl, boolean bl2) throws IOException, PGPException, WrongPasswordException {
        BufferedInputStream bufferedInputStream = null;
        File file = new File(string);
        this.Debug("Signing file {0}", string);
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            this.signAndEncryptStream((InputStream)bufferedInputStream, file.getName(), inputStream, string2, inputStream2, outputStream, bl, bl2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(bufferedInputStream);
            IOUtil.closeStream(outputStream);
            throw throwable;
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(outputStream);
    }

    public void signAndEncryptStream(InputStream inputStream, String string, InputStream inputStream2, String string2, InputStream inputStream3, OutputStream outputStream, boolean bl, boolean bl2) throws IOException, PGPException, WrongPasswordException, NoPublicKeyFoundException, NoPrivateKeyFoundException {
        if (this.pgp2Compatible) {
            this.internalSignAndEncryptStreamPgp2x(inputStream, string, inputStream2, string2, new InputStream[]{inputStream3}, outputStream, new Date(), bl);
            return;
        }
        if (!(outputStream instanceof BufferedOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream, 0x100000);
        }
        if (!inputStream3.markSupported()) {
            inputStream3 = new BufferedInputStream(inputStream3);
        }
        if (!inputStream2.markSupported()) {
            inputStream2 = new BufferedInputStream(inputStream2);
        }
        inputStream3.mark(0x100000);
        inputStream2.mark(0x100000);
        PGPPublicKey pGPPublicKey = this.readPublicKeyForEncryption(inputStream3);
        PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream2);
        if (pGPPublicKey.getVersion() == 3) {
            inputStream3.reset();
            inputStream2.reset();
            this.Debug("Swithcing to version 3 signatures");
            this.signAndEncryptStreamVersion3(inputStream, string, inputStream2, string2, inputStream3, outputStream, bl, bl2);
            return;
        }
        OutputStream outputStream2 = null;
        int n = 65536;
        try {
            int n2;
            PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator;
            PGPPublicKey pGPPublicKey2;
            if (bl) {
                outputStream2 = outputStream;
                outputStream = new ArmoredOutputStream(outputStream2);
                this.setAsciiVersionHeader(outputStream);
            }
            int n3 = this.selectPreferredCypher(pGPPublicKey);
            this.Debug("Encrypting with cipher {0}", KeyStore.cypherToString(n3));
            PGPEncryptedDataGenerator pGPEncryptedDataGenerator = new PGPEncryptedDataGenerator(this.bcFactory.CreatePGPDataEncryptorBuilder(n3, bl2, IOUtil.getSecureRandom()));
            pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKey));
            OutputStream outputStream3 = pGPEncryptedDataGenerator.open(outputStream, new byte[n]);
            for (int i = 0; i < this.getMasterKeysCount(); ++i) {
                KeyPairInformation keyPairInformation = (KeyPairInformation)this.masterKeysList.get(i);
                pGPPublicKey2 = this.readPublicKeyForEncryption(keyPairInformation.getPublicKeyRing());
                pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKey2));
                this.Debug("Encrypting with master key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKey2.getKeyID()));
            }
            PGPPrivateKey pGPPrivateKey = PGPLib.extractPrivateKey(pGPSecretKey, string2);
            int n4 = this.selectPreferredHash(pGPPublicKey);
            this.Debug("Signing with hash {0}", KeyStore.hashToString(n4));
            pGPPublicKey2 = this.bcFactory.CreatePGPSignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), n4);
            this.bcFactory.initSign((PGPSignatureGenerator)pGPPublicKey2, 0, pGPPrivateKey);
            Iterator iterator = pGPSecretKey.getPublicKey().getRawUserIDs();
            while (iterator.hasNext()) {
                byte[] byArray = (byte[])iterator.next();
                pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
                this.Debug("Signing with User ID {0}", BaseLib.toUserID(byArray));
                pGPSignatureSubpacketGenerator.setSignerUserID(false, byArray);
                pGPPublicKey2.setHashedSubpackets(pGPSignatureSubpacketGenerator.generate());
            }
            int n5 = this.selectPreferredCompression(pGPPublicKey);
            pGPSignatureSubpacketGenerator = new PGPCompressedDataGenerator(n5);
            OutputStream outputStream4 = null;
            PGPLiteralDataGenerator pGPLiteralDataGenerator = new PGPLiteralDataGenerator();
            OutputStream outputStream5 = null;
            if (n5 == 0) {
                this.Debug("No compression.");
                pGPPublicKey2.generateOnePassVersion(false).encode(outputStream3);
                outputStream5 = pGPLiteralDataGenerator.open(outputStream3, 'b', string, new Date(), new byte[0x100000]);
            } else {
                this.Debug("Compression is {0}", KeyStore.compressionToString(n5));
                outputStream4 = pGPSignatureSubpacketGenerator.open(outputStream3, new byte[n]);
                pGPPublicKey2.generateOnePassVersion(false).encode(outputStream4);
                outputStream5 = pGPLiteralDataGenerator.open(outputStream4, 'b', string, new Date(), new byte[0x100000]);
            }
            this.Debug("Signing stream content with internal file name label: {0}", string);
            byte[] byArray = new byte[n];
            while ((n2 = inputStream.read(byArray, 0, byArray.length)) > 0) {
                outputStream5.write(byArray, 0, n2);
                pGPPublicKey2.update(byArray, 0, n2);
            }
            IOUtil.closeStream(outputStream5);
            pGPLiteralDataGenerator.close();
            if (outputStream4 == null) {
                pGPPublicKey2.generate().encode(outputStream3);
            } else {
                pGPPublicKey2.generate().encode(outputStream4);
            }
            IOUtil.closeStream(outputStream4);
            pGPSignatureSubpacketGenerator.close();
            IOUtil.closeStream(outputStream3);
            pGPEncryptedDataGenerator.close();
            IOUtil.closeStream(inputStream);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            if (bl) {
                IOUtil.closeStream(outputStream);
                outputStream2.flush();
            } else {
                outputStream.flush();
            }
        }
    }

    public void signAndEncryptStream(InputStream inputStream, String string, InputStream inputStream2, String string2, InputStream[] inputStreamArray, OutputStream outputStream, boolean bl, boolean bl2) throws IOException, PGPException, WrongPasswordException, NoPublicKeyFoundException, NoPrivateKeyFoundException {
        if (this.pgp2Compatible) {
            this.internalSignAndEncryptStreamPgp2x(inputStream, string, inputStream2, string2, inputStreamArray, outputStream, new Date(), bl);
            return;
        }
        int n = 65536;
        try {
            int n2;
            PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator;
            int n3;
            if (bl) {
                outputStream = new ArmoredOutputStream(outputStream);
                this.setAsciiVersionHeader(outputStream);
            }
            LinkedList<PGPPublicKey> linkedList = new LinkedList<PGPPublicKey>();
            List<Integer> list = new LinkedList<Integer>();
            List<Integer> list2 = new LinkedList<Integer>();
            List<Integer> list3 = new LinkedList<Integer>();
            for (n3 = 0; n3 < inputStreamArray.length; ++n3) {
                PGPPublicKey pGPPublicKey = this.readPublicKeyForEncryption(inputStreamArray[n3]);
                if (n3 == 0) {
                    list = this.listPreferredCyphers(pGPPublicKey);
                    list2 = this.listPreferredHashes(pGPPublicKey);
                    list3 = this.listPreferredCompressions(pGPPublicKey);
                } else {
                    list = this.intersectList(list, this.listPreferredCyphers(pGPPublicKey));
                    list2 = this.intersectList(list2, this.listPreferredHashes(pGPPublicKey));
                    list3 = this.intersectList(list3, this.listPreferredCompressions(pGPPublicKey));
                }
                this.Debug("Encrypting with public key {0}", KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()));
                linkedList.add(pGPPublicKey);
            }
            n3 = this.selectPreferredCompression(list3);
            int n4 = this.selectPreferredCypher(list);
            int n5 = this.selectPreferredHash(list2);
            this.Debug("Encrypting with cipher {0}", KeyStore.cypherToString(n4));
            PGPEncryptedDataGenerator pGPEncryptedDataGenerator = new PGPEncryptedDataGenerator(this.bcFactory.CreatePGPDataEncryptorBuilder(n4, bl2, IOUtil.getSecureRandom()));
            for (int i = 0; i < linkedList.size(); ++i) {
                pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator((PGPPublicKey)linkedList.get(i)));
            }
            OutputStream outputStream2 = pGPEncryptedDataGenerator.open(outputStream, new byte[n]);
            PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream2);
            PGPPrivateKey pGPPrivateKey = PGPLib.extractPrivateKey(pGPSecretKey, string2);
            this.Debug("Signing with hash {0}", HashAlgorithm.Enum.fromInt(n5).toString());
            PGPSignatureGenerator pGPSignatureGenerator = this.bcFactory.CreatePGPSignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), n5);
            this.bcFactory.initSign(pGPSignatureGenerator, 0, pGPPrivateKey);
            Iterator iterator = pGPSecretKey.getPublicKey().getRawUserIDs();
            while (iterator.hasNext()) {
                pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
                pGPSignatureSubpacketGenerator.setSignerUserID(false, (byte[])iterator.next());
                pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketGenerator.generate());
            }
            pGPSignatureSubpacketGenerator = new PGPCompressedDataGenerator(n3);
            OutputStream outputStream3 = null;
            PGPLiteralDataGenerator pGPLiteralDataGenerator = new PGPLiteralDataGenerator();
            OutputStream outputStream4 = null;
            if (n3 == 0) {
                this.Debug("No Compression.");
                pGPSignatureGenerator.generateOnePassVersion(false).encode(outputStream2);
                outputStream4 = pGPLiteralDataGenerator.open(outputStream2, 'b', string, new Date(), new byte[0x100000]);
            } else {
                this.Debug("Compression is {0}", KeyStore.compressionToString(n3));
                outputStream3 = pGPSignatureSubpacketGenerator.open(outputStream2, new byte[n]);
                pGPSignatureGenerator.generateOnePassVersion(false).encode(outputStream3);
                outputStream4 = pGPLiteralDataGenerator.open(outputStream3, 'b', string, new Date(), new byte[0x100000]);
            }
            this.Debug("Signing stream content with internal file name label: {0}", string);
            byte[] byArray = new byte[n];
            while ((n2 = inputStream.read(byArray, 0, byArray.length)) > 0) {
                outputStream4.write(byArray, 0, n2);
                pGPSignatureGenerator.update(byArray, 0, n2);
            }
            IOUtil.closeStream(outputStream4);
            pGPLiteralDataGenerator.close();
            if (outputStream3 == null) {
                pGPSignatureGenerator.generate().encode(outputStream2);
            } else {
                pGPSignatureGenerator.generate().encode(outputStream3);
            }
            IOUtil.closeStream(outputStream3);
            pGPSignatureSubpacketGenerator.close();
            IOUtil.closeStream(outputStream2);
            pGPEncryptedDataGenerator.close();
            IOUtil.closeStream(inputStream);
            if (bl) {
                outputStream.close();
            }
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signAndEncryptFile(String string, KeyStore keyStore, String string2, String string3, String string4, String string5, boolean bl, boolean bl2) throws IOException, PGPException {
        File file;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bl3 = false;
        this.Debug("Signing and encrypting file {0}", string);
        this.Debug("Output file is {0}", new File(string5).getAbsolutePath());
        try {
            inputStream = this.createPublicRingStream(keyStore, string4);
            inputStream2 = this.createPrivateRingStream(keyStore, string2);
            file = new File(string);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(string5)), 0x100000);
            this.signAndEncryptStream((InputStream)bufferedInputStream, file.getName(), inputStream2, string3, inputStream, (OutputStream)bufferedOutputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                IOUtil.closeStream(inputStream2);
                IOUtil.closeStream(inputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string5);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(inputStream);
        if (!bl3) return;
        file = new File(string5);
        if (!file.exists()) return;
        file.delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signAndEncryptFile(String string, KeyStore keyStore, String string2, String string3, String[] stringArray, String string4, boolean bl, boolean bl2) throws IOException, PGPException {
        InputStream[] inputStreamArray = new InputStream[stringArray.length];
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bl3 = false;
        this.Debug("Signing and encrypting file {0}", string);
        this.Debug("Output file is {0}", new File(string4).getAbsolutePath());
        try {
            for (int i = 0; i < inputStreamArray.length; ++i) {
                inputStreamArray[i] = this.createPublicRingStream(keyStore, stringArray[i]);
            }
            inputStream = this.createPrivateRingStream(keyStore, string2);
            File file = new File(string);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string4), 0x100000);
            this.signAndEncryptStream((InputStream)bufferedInputStream, file.getName(), inputStream, string3, inputStreamArray, (OutputStream)bufferedOutputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                IOUtil.closeStream(inputStream);
                int n = 0;
                while (true) {
                    if (n >= inputStreamArray.length) {
                        if (!bl3) throw throwable;
                        File file = new File(string4);
                        if (!file.exists()) throw throwable;
                        file.delete();
                        throw throwable;
                    }
                    IOUtil.closeStream(inputStreamArray[n]);
                    ++n;
                }
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        IOUtil.closeStream(inputStream);
        int n = 0;
        while (true) {
            if (n >= inputStreamArray.length) {
                if (!bl3) return;
                File file = new File(string4);
                if (!file.exists()) return;
                file.delete();
                return;
            }
            IOUtil.closeStream(inputStreamArray[n]);
            ++n;
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signAndEncryptFile(String string, KeyStore keyStore, long l, String string2, long[] lArray, String string3, boolean bl, boolean bl2) throws IOException, PGPException {
        InputStream[] inputStreamArray = new InputStream[lArray.length];
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bl3 = false;
        this.Debug("Signing and encrypting file {0}", string);
        this.Debug("Output file is {0}", new File(string3).getAbsolutePath());
        try {
            for (int i = 0; i < inputStreamArray.length; ++i) {
                inputStreamArray[i] = this.createPublicRingStream(keyStore, lArray[i]);
            }
            inputStream = this.createPrivateRingStream(keyStore, l);
            File file = new File(string);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string3), 0x100000);
            this.signAndEncryptStream((InputStream)bufferedInputStream, file.getName(), inputStream, string2, inputStreamArray, (OutputStream)bufferedOutputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                IOUtil.closeStream(inputStream);
                int n = 0;
                while (true) {
                    if (n >= inputStreamArray.length) {
                        if (!bl3) throw throwable;
                        File file = new File(string3);
                        if (!file.exists()) throw throwable;
                        file.delete();
                        throw throwable;
                    }
                    IOUtil.closeStream(inputStreamArray[n]);
                    ++n;
                }
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        IOUtil.closeStream(inputStream);
        int n = 0;
        while (true) {
            if (n >= inputStreamArray.length) {
                if (!bl3) return;
                File file = new File(string3);
                if (!file.exists()) return;
                file.delete();
                return;
            }
            IOUtil.closeStream(inputStreamArray[n]);
            ++n;
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signAndEncryptFile(String string, KeyStore keyStore, long l, String string2, long l2, String string3, boolean bl, boolean bl2) throws IOException, PGPException {
        File file;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        this.Debug("Signing and encrypting file {0}", string);
        this.Debug("Output file is {0}", new File(string3).getAbsolutePath());
        boolean bl3 = false;
        try {
            inputStream = this.createPublicRingStream(keyStore, l2);
            inputStream2 = this.createPrivateRingStream(keyStore, l);
            file = new File(string);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string3), 0x100000);
            this.signAndEncryptStream((InputStream)bufferedInputStream, file.getName(), inputStream2, string2, inputStream, (OutputStream)bufferedOutputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                IOUtil.closeStream(inputStream2);
                IOUtil.closeStream(inputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string3);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(inputStream);
        if (!bl3) return;
        file = new File(string3);
        if (!file.exists()) return;
        file.delete();
    }

    public void signAndEncryptStream(InputStream inputStream, String string, KeyStore keyStore, String string2, String string3, String string4, OutputStream outputStream, boolean bl, boolean bl2) throws IOException, PGPException {
        try {
            InputStream inputStream2 = this.createPublicRingStream(keyStore, string4);
            InputStream inputStream3 = this.createPrivateRingStream(keyStore, string2);
            this.signAndEncryptStream(inputStream, string, inputStream3, string3, inputStream2, outputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    public void signAndEncryptStream(InputStream inputStream, String string, KeyStore keyStore, String string2, String string3, String[] stringArray, OutputStream outputStream, boolean bl, boolean bl2) throws IOException, PGPException {
        InputStream[] inputStreamArray = new InputStream[stringArray.length];
        try {
            InputStream inputStream2 = this.createPrivateRingStream(keyStore, string2);
            for (int i = 0; i < stringArray.length; ++i) {
                inputStreamArray[i] = this.createPublicRingStream(keyStore, stringArray[i]);
            }
            this.signAndEncryptStream(inputStream, string, inputStream2, string3, inputStreamArray, outputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    public void signAndEncryptStream(InputStream inputStream, String string, KeyStore keyStore, String string2, String string3, long[] lArray, OutputStream outputStream, boolean bl, boolean bl2) throws IOException, PGPException {
        InputStream[] inputStreamArray = new InputStream[lArray.length];
        try {
            InputStream inputStream2 = this.createPrivateRingStream(keyStore, string2);
            for (int i = 0; i < lArray.length; ++i) {
                inputStreamArray[i] = this.createPublicRingStream(keyStore, lArray[i]);
            }
            this.signAndEncryptStream(inputStream, string, inputStream2, string3, inputStreamArray, outputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signAndEncryptFileVersion3(String string, String string2, String string3, String string4, String string5, boolean bl) throws PGPException, IOException {
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bl2 = false;
        this.Debug("Signing and encrypting file {0}", string);
        this.Debug("Output file is {0}", new File(string5).getAbsolutePath());
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string5), 0x100000);
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFile");
            this.signAndEncryptStreamVersion3((InputStream)bufferedInputStream, new File(string).getName(), inputStream, string3, inputStream2, bufferedOutputStream, bl, this.integrityProtect);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl2 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedOutputStream);
                IOUtil.closeStream(inputStream2);
                if (!bl2) throw throwable;
                File file = new File(string5);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedOutputStream);
        IOUtil.closeStream(inputStream2);
        if (!bl2) return;
        File file = new File(string5);
        if (!file.exists()) return;
        file.delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void signAndEncryptFileVersion3(String string, String string2, String string3, String string4, String string5, boolean bl, boolean bl2) throws PGPException, IOException {
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        BufferedOutputStream bufferedOutputStream = null;
        this.Debug("Signing and encrypting file {0}", string);
        this.Debug("Output file is {0}", new File(string5).getAbsolutePath());
        boolean bl3 = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string5), 0x100000);
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFile");
            this.signAndEncryptStreamVersion3((InputStream)bufferedInputStream, new File(string).getName(), inputStream, string3, inputStream2, bufferedOutputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedOutputStream);
                IOUtil.closeStream(inputStream2);
                if (!bl3) throw throwable;
                File file = new File(string5);
                if (!file.exists()) throw throwable;
                file.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedOutputStream);
        IOUtil.closeStream(inputStream2);
        if (!bl3) return;
        File file = new File(string5);
        if (!file.exists()) return;
        file.delete();
    }

    public void signAndEncryptStreamVersion3(InputStream inputStream, String string, InputStream inputStream2, String string2, InputStream inputStream3, OutputStream outputStream, boolean bl) throws IOException, PGPException, WrongPasswordException {
        this.signAndEncryptStreamVersion3(inputStream, string, inputStream2, string2, inputStream3, outputStream, bl, this.integrityProtect);
    }

    public void signAndEncryptStreamVersion3(InputStream inputStream, String string, KeyStore keyStore, String string2, String string3, String string4, OutputStream outputStream, boolean bl) throws IOException, PGPException, WrongPasswordException {
        InputStream inputStream2 = this.createPublicRingStream(keyStore, string4);
        InputStream inputStream3 = this.createPrivateRingStream(keyStore, string2);
        this.signAndEncryptStreamVersion3(inputStream, string, inputStream3, string3, inputStream2, outputStream, bl);
    }

    public void signAndEncryptStreamVersion3(InputStream inputStream, String string, KeyStore keyStore, long l, String string2, long l2, OutputStream outputStream, boolean bl) throws IOException, PGPException, WrongPasswordException {
        InputStream inputStream2 = this.createPublicRingStream(keyStore, l2);
        InputStream inputStream3 = this.createPrivateRingStream(keyStore, l);
        this.signAndEncryptStreamVersion3(inputStream, string, inputStream3, string2, inputStream2, outputStream, bl);
    }

    public void signAndEncryptStreamVersion3(InputStream inputStream, String string, InputStream inputStream2, String string2, InputStream inputStream3, OutputStream outputStream, boolean bl, boolean bl2) throws IOException, PGPException, WrongPasswordException {
        Object object;
        if (!(outputStream instanceof BufferedOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream, 262144);
        }
        int n = 65536;
        PGPPublicKey pGPPublicKey = this.readPublicKeyForEncryption(inputStream3);
        PGPSecretKey pGPSecretKey = this.readSecretSigningKey(inputStream2);
        if (bl) {
            outputStream = new ArmoredOutputStream(outputStream);
            this.setAsciiVersionHeader(outputStream);
        }
        int n2 = this.selectPreferredCypher(pGPPublicKey);
        this.Debug("Encrypting with cypher {0}", KeyStore.cypherToString(n2));
        PGPEncryptedDataGenerator pGPEncryptedDataGenerator = new PGPEncryptedDataGenerator(this.bcFactory.CreatePGPDataEncryptorBuilder(n2, bl2, IOUtil.getSecureRandom()));
        OutputStream outputStream2 = null;
        try {
            pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKey));
            outputStream2 = pGPEncryptedDataGenerator.open(outputStream, new byte[n]);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        int n3 = this.selectPreferredCompression(pGPPublicKey);
        PGPCompressedDataGenerator pGPCompressedDataGenerator = new PGPCompressedDataGenerator(n3);
        PGPPrivateKey pGPPrivateKey = PGPLib.extractPrivateKey(pGPSecretKey, string2);
        int n4 = this.selectPreferredHash(pGPPublicKey);
        this.Debug("Signing with hash {0}", KeyStore.hashToString(n4));
        PGPSignatureGenerator pGPSignatureGenerator = this.bcFactory.CreatePGPSignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), n4);
        this.bcFactory.initSign(pGPSignatureGenerator, 0, pGPPrivateKey);
        Iterator iterator = pGPSecretKey.getPublicKey().getRawUserIDs();
        if (iterator.hasNext()) {
            object = new PGPSignatureSubpacketGenerator();
            object.setSignerUserID(false, (byte[])iterator.next());
            pGPSignatureGenerator.setHashedSubpackets(object.generate());
        }
        object = null;
        OutputStream outputStream3 = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(262144);
        PGPLiteralDataGenerator pGPLiteralDataGenerator = new PGPLiteralDataGenerator();
        try {
            int n5 = 0;
            byte[] byArray = new byte[262144];
            while ((n5 = inputStream.read(byArray)) > 0) {
                directByteArrayOutputStream.write(byArray, 0, n5);
                pGPSignatureGenerator.update(byArray, 0, n5);
            }
            this.Debug("Signing data with OpenPGP version 3 signature; internal file name {0}", string);
            if (n3 == 0) {
                this.Debug("No Compression.");
                pGPSignatureGenerator.generate().encode(outputStream2);
                outputStream3 = pGPLiteralDataGenerator.open(outputStream2, 'b', string, new Date(), new byte[n]);
            } else {
                this.Debug("Compression is {0}", KeyStore.compressionToString(n3));
                object = pGPCompressedDataGenerator.open(outputStream2, new byte[n]);
                pGPSignatureGenerator.generate().encode((OutputStream)object);
                outputStream3 = pGPLiteralDataGenerator.open((OutputStream)object, 'b', string, new Date(), new byte[n]);
            }
            outputStream3.write(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        IOUtil.closeStream(outputStream3);
        pGPLiteralDataGenerator.close();
        IOUtil.closeStream((OutputStream)object);
        pGPCompressedDataGenerator.close();
        IOUtil.closeStream(outputStream2);
        pGPEncryptedDataGenerator.close();
        IOUtil.closeStream(directByteArrayOutputStream);
        IOUtil.closeStream(inputStream);
        outputStream.flush();
        if (bl) {
            IOUtil.closeStream(outputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signAndEncryptString(String string, KeyStore keyStore, String string2, String string3, String string4, String string5) throws PGPException, IOException {
        String string6;
        ByteArrayInputStream byteArrayInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            inputStream = this.createPrivateRingStream(keyStore, string2);
            inputStream2 = this.createPublicRingStream(keyStore, string4);
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string5));
            char c = this.getContentType();
            this.setContentType('t');
            this.signAndEncryptStream((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, inputStream, string3, inputStream2, (OutputStream)directByteArrayOutputStream, true, this.integrityProtect);
            this.setContentType(c);
            string6 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        return string6;
    }

    public String signAndEncryptString(String string, KeyStore keyStore, String string2, String string3, String string4) throws PGPException, IOException {
        return this.signAndEncryptString(string, keyStore, string2, string3, string4, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signAndEncryptString(String string, KeyStore keyStore, long l, String string2, long l2, String string3) throws PGPException, IOException {
        String string4;
        ByteArrayInputStream byteArrayInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            inputStream = this.createPrivateRingStream(keyStore, l);
            inputStream2 = this.createPublicRingStream(keyStore, l2);
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            char c = this.getContentType();
            this.setContentType('t');
            this.signAndEncryptStream((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, inputStream, string2, inputStream2, (OutputStream)directByteArrayOutputStream, true, this.integrityProtect);
            this.setContentType(c);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        return string4;
    }

    public String signAndEncryptString(String string, KeyStore keyStore, long l, String string2, long l2) throws PGPException, IOException {
        return this.signAndEncryptString(string, keyStore, l, string2, l2, "UTF-8");
    }

    public String signAndEncryptString(String string, String string2, String string3, String string4) throws PGPException, IOException {
        return this.signAndEncryptString(string, string2, string3, string4, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signAndEncryptString(String string, InputStream inputStream, String string2, InputStream inputStream2, String string3) throws PGPException, IOException {
        String string4;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            this.signAndEncryptStream((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, inputStream, string2, inputStream2, (OutputStream)directByteArrayOutputStream, true, false);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        return string4;
    }

    public String signAndEncryptStringVersion3(String string, String string2, String string3, String string4) throws PGPException, IOException {
        return this.signAndEncryptStringVersion3(string, string2, string3, string4, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signAndEncryptString(String string, String string2, String string3, String string4, String string5) throws PGPException, IOException {
        String string6;
        ByteArrayInputStream byteArrayInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFileName");
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string5));
            this.signAndEncryptStream((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, inputStream, string3, inputStream2, (OutputStream)directByteArrayOutputStream, true, this.integrityProtect);
            string6 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        return string6;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signAndEncryptStringVersion3(String string, String string2, String string3, String string4, String string5) throws PGPException, IOException {
        String string6;
        ByteArrayInputStream byteArrayInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFileName");
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(262144);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string5));
            this.signAndEncryptStreamVersion3((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, inputStream, string3, inputStream2, directByteArrayOutputStream, true, false);
            string6 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        return string6;
    }

    public String decryptString(String string, String string2, String string3) throws IOException, PGPException {
        return this.decryptString(string, string2, string3, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String decryptString(String string, String string2, String string3, String string4) throws IOException, PGPException {
        InputStream inputStream = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            String string5 = this.decryptString(string, inputStream, string3, string4);
            return string5;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public String decryptString(String string, InputStream inputStream, String string2) throws IOException, PGPException {
        return this.decryptString(string, inputStream, string2, "UTF-8");
    }

    public String decryptString(String string, KeyStore keyStore, String string2) throws IOException, PGPException {
        return this.decryptString(string, keyStore, string2, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String decryptString(String string, KeyStore keyStore, String string2, String string3) throws IOException, PGPException {
        String string4;
        ByteArrayInputStream byteArrayInputStream = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes("ASCII"));
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            this.decryptStream((InputStream)byteArrayInputStream, keyStore, string2, (OutputStream)directByteArrayOutputStream);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string3);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(directByteArrayOutputStream);
        return string4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String decryptString(String string, InputStream inputStream, String string2, String string3) throws IOException, PGPException {
        String string4;
        InputStream inputStream2 = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            inputStream2 = PGPLib.readFileOrAsciiString(string, "message");
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            this.decryptStream(inputStream2, inputStream, string2, (OutputStream)directByteArrayOutputStream);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string3);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream2);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(directByteArrayOutputStream);
        return string4;
    }

    public String decryptStream(InputStream inputStream, InputStream inputStream2, String string, OutputStream outputStream) throws PGPException, IOException {
        InputStream inputStream3 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object instanceof PGPMarker) {
            this.Debug("Skipping PGP marker.");
            object = pGPObjectFactory2.nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        String string2 = null;
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            string2 = this.parseEncryptedData(pGPEncryptedDataList, false, signatureCheck, null, inputStream2, string, null, outputStream);
        } else if (object instanceof PGPCompressedData) {
            string2 = this.parseCompressedData((PGPCompressedData)object, false, signatureCheck, null, null, outputStream);
        } else if (object instanceof PGPOnePassSignatureList) {
            string2 = this.parseSignedData((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null, outputStream, signatureCheck);
        } else if (object instanceof PGPSignatureList) {
            string2 = this.parseSignedDataVersion3((PGPSignatureList)object, pGPObjectFactory2, null, null, outputStream, signatureCheck);
        } else if (object instanceof PGPLiteralData) {
            string2 = this.parseLiteralData((PGPLiteralData)object, null, outputStream);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object);
        }
        return string2;
    }

    public String[] decryptStreamTo(InputStream inputStream, InputStream inputStream2, String string, String string2) throws PGPException, IOException {
        return this.decryptStreamTo(inputStream, inputStream2, string, string2, null);
    }

    private String[] decryptStreamTo(InputStream inputStream, InputStream inputStream2, String string, String string2, String string3) throws PGPException, IOException {
        this.Debug("Decrypting stream to folder {0}", new File(string2).getAbsolutePath());
        InputStream inputStream3 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object instanceof PGPMarker) {
            this.Debug("Skipping PGP marker.");
            object = pGPObjectFactory2.nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        String[] stringArray = new String[]{};
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            stringArray = this.parseEncryptedDataTo(pGPEncryptedDataList, false, signatureCheck, null, inputStream2, string, null, string2, string3);
        } else if (object instanceof PGPCompressedData) {
            stringArray = this.parseCompressedDataTo((PGPCompressedData)object, false, signatureCheck, null, null, string2, string3);
        } else if (object instanceof PGPOnePassSignatureList) {
            stringArray = this.parseSignedDataTo((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null, string2, string3, signatureCheck);
        } else if (object instanceof PGPSignatureList) {
            stringArray = this.parseSignedDataVersion3To((PGPSignatureList)object, pGPObjectFactory2, null, null, string2, string3, signatureCheck);
        } else if (object instanceof PGPLiteralData) {
            stringArray = this.parseLiteralDataTo((PGPLiteralData)object, null, string2, string3);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object);
        }
        return stringArray;
    }

    public String[] decryptStreamTo(InputStream inputStream, KeyStore keyStore, String string, String string2) throws PGPException, IOException {
        return this.decryptStreamTo(inputStream, keyStore, string, string2, null);
    }

    private String[] decryptStreamTo(InputStream inputStream, KeyStore keyStore, String string, String string2, String string3) throws PGPException, IOException {
        this.Debug("Decrypting stream to folder {0}", new File(string2).getAbsolutePath());
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object instanceof PGPMarker) {
            this.Debug("Skipping PGP marker.");
            object = pGPObjectFactory2.nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        String[] stringArray = new String[]{};
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            stringArray = this.parseEncryptedDataTo(pGPEncryptedDataList, false, signatureCheck, keyStore, null, string, null, string2, string3);
        } else if (object instanceof PGPCompressedData) {
            stringArray = this.parseCompressedDataTo((PGPCompressedData)object, false, signatureCheck, null, null, string2, string3);
        } else if (object instanceof PGPOnePassSignatureList) {
            stringArray = this.parseSignedDataTo((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null, string2, string3, signatureCheck);
        } else if (object instanceof PGPSignatureList) {
            stringArray = this.parseSignedDataVersion3To((PGPSignatureList)object, pGPObjectFactory2, null, null, string2, string3, signatureCheck);
        } else if (object instanceof PGPLiteralData) {
            stringArray = this.parseLiteralDataTo((PGPLiteralData)object, null, string2, string3);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object);
        }
        return stringArray;
    }

    public String decryptStream(InputStream inputStream, KeyStore keyStore, String string, OutputStream outputStream) throws PGPException, IOException {
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object instanceof PGPMarker) {
            this.Debug("Skipping PGP marker.");
            object = pGPObjectFactory2.nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        String string2 = null;
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            string2 = this.parseEncryptedData(pGPEncryptedDataList, false, signatureCheck, keyStore, null, string, null, outputStream);
        } else if (object instanceof PGPCompressedData) {
            string2 = this.parseCompressedData((PGPCompressedData)object, false, signatureCheck, null, null, outputStream);
        } else if (object instanceof PGPLiteralData) {
            string2 = this.parseLiteralData((PGPLiteralData)object, null, outputStream);
        } else if (object instanceof PGPOnePassSignatureList) {
            string2 = this.parseSignedData((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null, outputStream, signatureCheck);
        } else if (object instanceof PGPSignatureList) {
            string2 = this.parseSignedDataVersion3((PGPSignatureList)object, pGPObjectFactory2, null, null, outputStream, signatureCheck);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object);
        }
        return string2;
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public String decryptFile(String string, KeyStore keyStore, String string2, String string3) throws PGPException, IOException {
        File file;
        String string4;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        this.Debug("Decrypting file {0}", string);
        this.Debug("Decrypting to {0}", new File(string3).getAbsolutePath());
        boolean bl = false;
        try {
            fileInputStream = new FileInputStream(string);
            fileOutputStream = new FileOutputStream(string3);
            string4 = this.decryptStream((InputStream)fileInputStream, keyStore, string2, (OutputStream)fileOutputStream);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(fileOutputStream);
                if (bl && (file2 = new File(string3)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(fileOutputStream);
        if (bl && (file = new File(string3)).exists()) {
            file.delete();
        }
        return string4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String decryptFile(String string, String string2, String string3, String string4) throws PGPException, IOException {
        this.Debug("Decrypting file {0}", string);
        InputStream inputStream = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            String string5 = this.decryptFile(string, inputStream, string3, string4);
            return string5;
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
    public String decryptFilePBE(String string, String string2, String string3) throws PGPException, IOException {
        File file;
        String string4;
        this.Debug("Decrypting password encrypted file {0}", string);
        this.Debug("Decrypting to {0}", new File(string3).getAbsolutePath());
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bl = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string3));
            string4 = this.decryptStreamPBE(bufferedInputStream, string2, bufferedOutputStream);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (bl && (file2 = new File(string3)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (bl && (file = new File(string3)).exists()) {
            file.delete();
        }
        return string4;
    }

    public String decryptStringPBE(String string, String string2) throws IOException, PGPException {
        return this.decryptStringPBE(string, string2, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String decryptStringPBE(String string, String string2, String string3) throws IOException, PGPException {
        String string4;
        ByteArrayInputStream byteArrayInputStream = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes("ASCII"));
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            this.decryptStreamPBE(byteArrayInputStream, string2, directByteArrayOutputStream);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string3);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(directByteArrayOutputStream);
        return string4;
    }

    public String decryptStreamPBE(InputStream inputStream, String string, OutputStream outputStream) throws PGPException, IOException {
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object instanceof PGPMarker) {
            object = pGPObjectFactory2.nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        String string2 = null;
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            string2 = this.parseEncryptedDataPBE(pGPEncryptedDataList, false, signatureCheck, string, null, null, outputStream);
        } else if (object instanceof PGPCompressedData) {
            string2 = this.parseCompressedData((PGPCompressedData)object, false, signatureCheck, null, null, outputStream);
        } else if (object instanceof PGPOnePassSignatureList) {
            string2 = this.parseSignedData((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null, outputStream, signatureCheck);
        } else if (object instanceof PGPSignatureList) {
            string2 = this.parseSignedDataVersion3((PGPSignatureList)object, pGPObjectFactory2, null, null, outputStream, signatureCheck);
        } else if (object instanceof PGPLiteralData) {
            string2 = this.parseLiteralData((PGPLiteralData)object, null, outputStream);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object);
        }
        return string2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String[] decryptFileTo(String string, String string2, String string3, String string4) throws PGPException, IOException {
        String[] stringArray;
        this.Debug("Decrypting file {0}", string);
        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            stringArray = this.decryptStreamTo((InputStream)fileInputStream, inputStream, string3, string4, string);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            IOUtil.closeStream(inputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(inputStream);
        return stringArray;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String[] decryptFileTo(String string, KeyStore keyStore, String string2, String string3) throws PGPException, IOException {
        String[] stringArray;
        this.Debug("Decrypting file {0}", string);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            stringArray = this.decryptStreamTo((InputStream)fileInputStream, keyStore, string2, string3, string);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return stringArray;
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public String decryptFile(String string, InputStream inputStream, String string2, String string3) throws PGPException, IOException {
        File file;
        String string4;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        this.Debug("Decrypting file {0}", string);
        this.Debug("Decrypting to {0}", new File(string3).getAbsolutePath());
        boolean bl = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string3), 0x100000);
            string4 = this.decryptStream((InputStream)bufferedInputStream, inputStream, string2, (OutputStream)bufferedOutputStream);
        }
        catch (PGPException pGPException) {
            try {
                bl = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (bl && (file2 = new File(string3)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (bl && (file = new File(string3)).exists()) {
            file.delete();
        }
        return string4;
    }

    public String encryptString(String string, String string2) throws PGPException, IOException {
        return this.encryptString(string, string2, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String encryptString(String string, String string2, String string3) throws PGPException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "publicKeyFileName");
            String string4 = this.encryptString(string, inputStream, string3);
            return string4;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String encryptString(String string, String[] stringArray, String string2) throws PGPException, IOException {
        String string3;
        LinkedList<InputStream> linkedList = new LinkedList<InputStream>();
        InputStream inputStream = null;
        for (int i = 0; i < stringArray.length; ++i) {
            inputStream = PGPLib.readFileOrAsciiString(stringArray[i], "publicKeyFileNames :" + i);
            linkedList.add(inputStream);
        }
        InputStream[] inputStreamArray = linkedList.toArray(new InputStream[linkedList.size()]);
        ByteArrayInputStream byteArrayInputStream = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string2));
            this.encryptStream((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, inputStreamArray, (OutputStream)directByteArrayOutputStream, true, this.integrityProtect);
            string3 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(directByteArrayOutputStream);
            for (int i = 0; i < inputStreamArray.length; ++i) {
                IOUtil.closeStream(inputStreamArray[i]);
            }
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(directByteArrayOutputStream);
        for (int i = 0; i < inputStreamArray.length; ++i) {
            IOUtil.closeStream(inputStreamArray[i]);
        }
        return string3;
    }

    public String encryptString(String string, KeyStore keyStore, String string2) throws PGPException, IOException {
        return this.encryptString(string, keyStore, string2, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String encryptString(String string, KeyStore keyStore, String string2, String string3) throws PGPException, IOException {
        String string4;
        InputStream inputStream = null;
        try {
            inputStream = this.createPublicRingStream(keyStore, string2);
            string4 = this.encryptString(string, inputStream, string3);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        return string4;
    }

    public String encryptString(String string, KeyStore keyStore, long l) throws PGPException, IOException {
        return this.encryptString(string, keyStore, l, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String encryptString(String string, KeyStore keyStore, long l, String string2) throws PGPException, IOException {
        String string3;
        InputStream inputStream = null;
        try {
            inputStream = this.createPublicRingStream(keyStore, l);
            string3 = this.encryptString(string, inputStream, string2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        return string3;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String encryptString(String string, KeyStore keyStore, long[] lArray, String string2) throws PGPException, IOException {
        String string3;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string2));
            this.encryptStream((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, keyStore, lArray, (OutputStream)directByteArrayOutputStream, true, this.integrityProtect);
            string3 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        return string3;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String encryptString(String string, KeyStore keyStore, String[] stringArray, String string2) throws PGPException, IOException {
        String string3;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string2));
            this.encryptStream((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, keyStore, stringArray, (OutputStream)directByteArrayOutputStream, true, this.integrityProtect);
            string3 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        return string3;
    }

    public String encryptString(String string, InputStream inputStream) throws PGPException, IOException {
        return this.encryptString(string, inputStream, "UTF-8");
    }

    public String encryptString(String string, InputStream inputStream, String string2) throws PGPException, IOException {
        return this.encryptString(string, DEFAULT_MESSAGE_FILE_NAME, inputStream, string2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String encryptString(String string, String string2, InputStream inputStream, String string3) throws PGPException, IOException {
        String string4;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            this.encryptStream((InputStream)byteArrayInputStream, string2, inputStream, (OutputStream)directByteArrayOutputStream, true, this.integrityProtect);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        return string4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signString(String string, String string2, String string3) throws PGPException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFile");
            String string4 = this.signString(string, inputStream, string3, "UTF-8");
            return string4;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signString(String string, String string2, String string3, String string4) throws PGPException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFile");
            String string5 = this.signString(string, inputStream, string3, string4);
            return string5;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signString(String string, InputStream inputStream, String string2, String string3) throws PGPException, IOException {
        String string4;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            this.signStream(byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, inputStream, string2, directByteArrayOutputStream, true);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        return string4;
    }

    public String signString(String string, InputStream inputStream, String string2) throws PGPException, IOException {
        return this.signString(string, inputStream, string2, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signString(String string, KeyStore keyStore, String string2, String string3, String string4) throws PGPException, IOException {
        String string5;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string4));
            this.signStream((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, keyStore, string2, string3, (OutputStream)directByteArrayOutputStream, true);
            string5 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        return string5;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String signString(String string, KeyStore keyStore, long l, String string2, String string3) throws PGPException, IOException {
        String string4;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            this.signStream((InputStream)byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, keyStore, l, string2, (OutputStream)directByteArrayOutputStream, true);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        return string4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptStream(InputStream inputStream, String string, InputStream[] inputStreamArray, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        try {
            PGPPublicKey[] pGPPublicKeyArray = new PGPPublicKey[inputStreamArray.length];
            for (int i = 0; i < inputStreamArray.length; ++i) {
                pGPPublicKeyArray[i] = this.readPublicKeyForEncryption(inputStreamArray[i]);
            }
            this.inernalEncryptStream(inputStream, string, pGPPublicKeyArray, outputStream, new Date(), bl, bl2, false);
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public void encryptStream(InputStream inputStream, String string, long l, String string2, OutputStream outputStream, boolean bl) throws PGPException, IOException {
        this.encryptStream(inputStream, string, l, string2, outputStream, bl, this.integrityProtect);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptStream(InputStream inputStream, String string, long l, String string2, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(string2));
            this.encryptStream(inputStream, string, l, fileInputStream, outputStream, bl, bl2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
    }

    public void encryptStream(InputStream inputStream, String string, KeyStore keyStore, String string2, OutputStream outputStream, boolean bl) throws PGPException, IOException {
        this.encryptStream(inputStream, string, keyStore, string2, outputStream, bl, this.integrityProtect);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptStream(InputStream inputStream, String string, KeyStore keyStore, String string2, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        InputStream inputStream2 = this.createPublicRingStream(keyStore, string2);
        try {
            this.encryptStream(inputStream, string, inputStream2, outputStream, bl, bl2);
        }
        finally {
            IOUtil.closeStream(inputStream2);
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptStream(InputStream inputStream, String string, KeyStore keyStore, String[] stringArray, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        int n;
        InputStream[] inputStreamArray = new InputStream[stringArray.length];
        for (n = 0; n < stringArray.length; ++n) {
            inputStreamArray[n] = this.createPublicRingStream(keyStore, stringArray[n]);
        }
        try {
            this.encryptStream(inputStream, string, inputStreamArray, outputStream, bl, bl2);
        }
        finally {
            for (n = 0; n < stringArray.length; ++n) {
                IOUtil.closeStream(inputStreamArray[n]);
            }
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptStream(InputStream inputStream, String string, KeyStore keyStore, long[] lArray, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        InputStream[] inputStreamArray = new InputStream[lArray.length];
        for (int i = 0; i < lArray.length; ++i) {
            PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(lArray[i]);
            inputStreamArray[i] = new ByteArrayInputStream(pGPPublicKeyRing.getEncoded());
        }
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            int n = 0;
            byte[] byArray = new byte[0x100000];
            while ((n = inputStream.read(byArray)) > 0) {
                directByteArrayOutputStream.write(byArray, 0, n);
            }
            this.encryptStream((InputStream)new ByteArrayInputStream(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size()), string, inputStreamArray, outputStream, bl, bl2);
        }
        finally {
            for (int i = 0; i < lArray.length; ++i) {
                IOUtil.closeStream(inputStreamArray[i]);
            }
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptStream(InputStream inputStream, String string, KeyStore keyStore, long l, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        ByteArrayInputStream byteArrayInputStream = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(l);
        try {
            pGPPublicKeyRing.encode((OutputStream)directByteArrayOutputStream);
            byteArrayInputStream = new ByteArrayInputStream(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size());
            DirectByteArrayOutputStream directByteArrayOutputStream2 = new DirectByteArrayOutputStream(0x100000);
            int n = 0;
            byte[] byArray = new byte[0x100000];
            while ((n = inputStream.read(byArray)) > 0) {
                directByteArrayOutputStream2.write(byArray, 0, n);
            }
            this.encryptStream(inputStream, string, (long)directByteArrayOutputStream2.size(), byteArrayInputStream, outputStream, bl, bl2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
    }

    public void encryptStream(InputStream inputStream, String string, long l, InputStream inputStream2, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        PGPPublicKey pGPPublicKey = this.readPublicKeyForEncryption(inputStream2);
        this.internalEncryptStream(inputStream, string, l, new PGPPublicKey[]{pGPPublicKey}, outputStream, bl, bl2, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptStream(InputStream inputStream, String string, InputStream inputStream2, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        try {
            PGPPublicKey pGPPublicKey = this.readPublicKeyForEncryption(inputStream2);
            this.internalEncryptStream2(inputStream, string, new PGPPublicKey[]{pGPPublicKey}, outputStream, new Date(), bl, bl2, false);
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptStream(InputStream inputStream, String string, PGPKeyPair pGPKeyPair, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        try {
            PGPPublicKey pGPPublicKey = this.readPublicKeyForEncryption(pGPKeyPair.getRawPublicKeyRing());
            this.internalEncryptStream2(inputStream, string, new PGPPublicKey[]{pGPPublicKey}, outputStream, new Date(), bl, bl2, false);
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public String encryptStringPBE(String string, String string2) throws PGPException, IOException {
        return this.encryptStringPBE(string, string2, "UTF-8");
    }

    public String encryptStringWithPassword(String string, String string2) throws PGPException, IOException {
        return this.encryptStringPBE(string, string2, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String encryptStringPBE(String string, String string2, String string3) throws PGPException, IOException {
        String string4;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            this.encryptStreamPBE(byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, string2, directByteArrayOutputStream, true, this.integrityProtect);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        return string4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String encryptStringWithPassword(String string, String string2, String string3) throws PGPException, IOException {
        String string4;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            this.encryptStreamPBE(byteArrayInputStream, DEFAULT_MESSAGE_FILE_NAME, string2, directByteArrayOutputStream, true, this.integrityProtect);
            string4 = new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "UTF-8");
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        return string4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptStreamPBE(InputStream inputStream, String string, String string2, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException, IOException {
        try {
            this.inernalEncryptStreamPBE(inputStream, string, string2, outputStream, bl, bl2);
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
    public void encryptFilePBE(String string, String string2, String string3, String string4, boolean bl, boolean bl2) throws PGPException, IOException {
        Object object;
        File file = new File(string);
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        this.Debug("Password encrypting file {0}", string);
        this.Debug("Encrypting to {0}", new File(string4).getAbsolutePath());
        boolean bl3 = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string4), 0x100000);
            inputStream = PGPLib.readFileOrAsciiString(string2, "publicKeyFileName");
            object = this.readPublicKeyForEncryption(inputStream);
            this.inernalEncryptStreamPBE(bufferedInputStream, file.getName(), file.length(), (PGPPublicKey)object, string3, bufferedOutputStream, bl, bl2);
        }
        catch (PGPException pGPException) {
            try {
                bl3 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string4);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (!bl3) return;
        object = new File(string4);
        if (!((File)object).exists()) return;
        ((File)object).delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFilePBE(String string, String string2, String string3, boolean bl, boolean bl2) throws PGPException, IOException {
        File file = new File(string);
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        this.Debug("Password encrypting file {0}", string);
        this.Debug("Encrypting to {0}", new File(string3).getAbsolutePath());
        boolean bl3 = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string3), 0x100000);
            this.inernalEncryptStreamPBE(bufferedInputStream, file.getName(), string2, bufferedOutputStream, bl, bl2);
        }
        catch (PGPException pGPException) {
            try {
                bl3 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string3);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (!bl3) return;
        File file3 = new File(string3);
        if (!file3.exists()) return;
        file3.delete();
    }

    public void encryptFile(String string, String string2, String string3, boolean bl) throws PGPException, IOException {
        this.encryptFile(string, string2, string3, bl, this.integrityProtect);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encryptFile(String string, String string2, String string3, boolean bl, boolean bl2) throws PGPException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string2, "publicKeyFileName");
            this.encryptFile(string, inputStream, string3, bl, bl2);
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFile(String string, String[] stringArray, String string2, boolean bl, boolean bl2) throws PGPException, IOException {
        File file;
        Object object;
        this.Debug("Encrypting file {0}", string);
        this.Debug("Encrypting to {0}", new File(string2).getAbsolutePath());
        LinkedList<PGPPublicKey> linkedList = new LinkedList<PGPPublicKey>();
        InputStream inputStream = null;
        for (int i = 0; i < stringArray.length; ++i) {
            try {
                inputStream = PGPLib.readFileOrAsciiString(stringArray[i], "publicKeysFileNames :" + i);
                object = this.readPublicKeyForEncryption(inputStream);
                linkedList.add((PGPPublicKey)object);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                throw throwable;
            }
            IOUtil.closeStream(inputStream);
        }
        PGPPublicKey[] pGPPublicKeyArray = linkedList.toArray(new PGPPublicKey[linkedList.size()]);
        object = null;
        FileOutputStream fileOutputStream = null;
        boolean bl3 = false;
        try {
            file = new File(string);
            object = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(string2);
            boolean bl4 = false;
            this.internalEncryptStream2((InputStream)object, file.getName(), pGPPublicKeyArray, fileOutputStream, new Date(file.lastModified()), bl, bl2, bl4);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream((InputStream)object);
                IOUtil.closeStream(fileOutputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string2);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream((InputStream)object);
        IOUtil.closeStream(fileOutputStream);
        if (!bl3) return;
        file = new File(string2);
        if (!file.exists()) return;
        file.delete();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFile(String string, KeyStore keyStore, String[] stringArray, String string2, boolean bl, boolean bl2) throws PGPException, IOException {
        File file;
        Object object;
        this.Debug("Encrypting file {0}", string);
        this.Debug("Encrypting to {0}", new File(string2).getAbsolutePath());
        LinkedList<PGPPublicKey> linkedList = new LinkedList<PGPPublicKey>();
        InputStream inputStream = null;
        for (int i = 0; i < stringArray.length; ++i) {
            try {
                inputStream = this.createPublicRingStream(keyStore, stringArray[i]);
                object = this.readPublicKeyForEncryption(inputStream);
                linkedList.add((PGPPublicKey)object);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                throw throwable;
            }
            IOUtil.closeStream(inputStream);
        }
        PGPPublicKey[] pGPPublicKeyArray = linkedList.toArray(new PGPPublicKey[linkedList.size()]);
        object = null;
        FileOutputStream fileOutputStream = null;
        boolean bl3 = false;
        try {
            file = new File(string);
            object = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(string2);
            boolean bl4 = false;
            this.internalEncryptStream2((InputStream)object, file.getName(), pGPPublicKeyArray, fileOutputStream, new Date(file.lastModified()), bl, bl2, bl4);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream((InputStream)object);
                IOUtil.closeStream(fileOutputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string2);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream((InputStream)object);
        IOUtil.closeStream(fileOutputStream);
        if (!bl3) return;
        file = new File(string2);
        if (!file.exists()) return;
        file.delete();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFile(String string, KeyStore keyStore, long[] lArray, String string2, boolean bl, boolean bl2) throws PGPException, IOException {
        File file;
        Object object;
        this.Debug("Encrypting file {0}", string);
        this.Debug("Encrypting to {0}", new File(string2).getAbsolutePath());
        LinkedList<PGPPublicKey> linkedList = new LinkedList<PGPPublicKey>();
        InputStream inputStream = null;
        for (int i = 0; i < lArray.length; ++i) {
            try {
                inputStream = this.createPublicRingStream(keyStore, lArray[i]);
                object = this.readPublicKeyForEncryption(inputStream);
                linkedList.add((PGPPublicKey)object);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                throw throwable;
            }
            IOUtil.closeStream(inputStream);
        }
        PGPPublicKey[] pGPPublicKeyArray = linkedList.toArray(new PGPPublicKey[linkedList.size()]);
        object = null;
        FileOutputStream fileOutputStream = null;
        boolean bl3 = false;
        try {
            file = new File(string);
            object = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(string2);
            boolean bl4 = false;
            this.internalEncryptStream2((InputStream)object, file.getName(), pGPPublicKeyArray, fileOutputStream, new Date(file.lastModified()), bl, bl2, bl4);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream((InputStream)object);
                IOUtil.closeStream(fileOutputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string2);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream((InputStream)object);
        IOUtil.closeStream(fileOutputStream);
        if (!bl3) return;
        file = new File(string2);
        if (!file.exists()) return;
        file.delete();
    }

    public void encryptFiles(String[] stringArray, String string, String string2, boolean bl, boolean bl2) throws PGPException, IOException {
        this.encryptFiles(stringArray, new String[]{string}, string2, bl, bl2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFiles(String[] stringArray, String[] stringArray2, String string, boolean bl, boolean bl2) throws PGPException, IOException {
        Object object;
        this.Debug("Encrypting multiple files");
        this.Debug("Encrypting to {0}", new File(string).getAbsolutePath());
        if (stringArray.length == 0) {
            throw new IllegalArgumentException("Please specify at least one file to be encrypted.");
        }
        LinkedList<PGPPublicKey> linkedList = new LinkedList<PGPPublicKey>();
        InputStream inputStream = null;
        for (int i = 0; i < stringArray2.length; ++i) {
            try {
                inputStream = PGPLib.readFileOrAsciiString(stringArray2[i], "publicKeysFileNames :" + i);
                object = this.readPublicKeyForEncryption(inputStream);
                linkedList.add((PGPPublicKey)object);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                throw throwable;
            }
            IOUtil.closeStream(inputStream);
        }
        PGPPublicKey[] pGPPublicKeyArray = linkedList.toArray(new PGPPublicKey[linkedList.size()]);
        object = null;
        boolean bl3 = false;
        String string2 = stringArray[0];
        if (stringArray.length > 1) {
            object = this.createTarFile(stringArray);
            bl3 = true;
            string2 = ((File)object).getAbsolutePath();
        }
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        File file = null;
        boolean bl4 = false;
        char c = this.contentType;
        try {
            file = new File(string2);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string), 0x100000);
            if (bl3) {
                this.contentType = (char)98;
            }
            this.internalEncryptStream2(bufferedInputStream, file.getName(), pGPPublicKeyArray, bufferedOutputStream, new Date(file.lastModified()), bl, bl2, bl3);
            this.contentType = c;
        }
        catch (PGPException pGPException) {
            try {
                bl4 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl4 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                this.contentType = c;
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (object != null) {
                    ((File)object).delete();
                }
                if (!bl4) throw throwable;
                File file2 = new File(string);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (object != null) {
            ((File)object).delete();
        }
        if (!bl4) return;
        File file3 = new File(string);
        if (!file3.exists()) return;
        file3.delete();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFiles(String[] stringArray, KeyStore keyStore, String[] stringArray2, String string, boolean bl, boolean bl2) throws PGPException, IOException {
        Object object;
        this.Debug("Encrypting multiple files");
        this.Debug("Encrypting to {0}", new File(string).getAbsolutePath());
        if (stringArray.length == 0) {
            throw new IllegalArgumentException("please specify at least one file name to be encrypted.");
        }
        LinkedList<PGPPublicKey> linkedList = new LinkedList<PGPPublicKey>();
        InputStream inputStream = null;
        for (int i = 0; i < stringArray2.length; ++i) {
            try {
                inputStream = this.createPublicRingStream(keyStore, stringArray2[i]);
                object = this.readPublicKeyForEncryption(inputStream);
                linkedList.add((PGPPublicKey)object);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                throw throwable;
            }
            IOUtil.closeStream(inputStream);
        }
        PGPPublicKey[] pGPPublicKeyArray = linkedList.toArray(new PGPPublicKey[linkedList.size()]);
        object = null;
        boolean bl3 = false;
        String string2 = stringArray[0];
        if (stringArray.length > 1) {
            object = this.createTarFile(stringArray);
            bl3 = true;
            string2 = ((File)object).getAbsolutePath();
        }
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        File file = null;
        boolean bl4 = false;
        char c = this.contentType;
        try {
            file = new File(string2);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string), 0x100000);
            if (bl3) {
                this.contentType = (char)98;
            }
            this.internalEncryptStream2(bufferedInputStream, file.getName(), pGPPublicKeyArray, bufferedOutputStream, new Date(file.lastModified()), bl, bl2, bl3);
            this.contentType = c;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl4 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl4 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                this.contentType = c;
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (object != null) {
                    ((File)object).delete();
                }
                if (!bl4) throw throwable;
                File file2 = new File(string);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (object != null) {
            ((File)object).delete();
        }
        if (!bl4) return;
        File file3 = new File(string);
        if (!file3.exists()) return;
        file3.delete();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFiles(String[] stringArray, KeyStore keyStore, long[] lArray, String string, boolean bl, boolean bl2) throws PGPException, IOException {
        Object object;
        this.Debug("Encrypting multiple files");
        this.Debug("Encrypting to {0}", new File(string).getAbsolutePath());
        if (stringArray.length == 0) {
            throw new IllegalArgumentException("please specify at least one file name to be encrypted.");
        }
        LinkedList<PGPPublicKey> linkedList = new LinkedList<PGPPublicKey>();
        InputStream inputStream = null;
        for (int i = 0; i < lArray.length; ++i) {
            try {
                inputStream = this.createPublicRingStream(keyStore, lArray[i]);
                object = this.readPublicKeyForEncryption(inputStream);
                linkedList.add((PGPPublicKey)object);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(inputStream);
                throw throwable;
            }
            IOUtil.closeStream(inputStream);
        }
        PGPPublicKey[] pGPPublicKeyArray = linkedList.toArray(new PGPPublicKey[linkedList.size()]);
        object = null;
        boolean bl3 = false;
        String string2 = stringArray[0];
        if (stringArray.length > 1) {
            object = this.createTarFile(stringArray);
            bl3 = true;
            string2 = ((File)object).getAbsolutePath();
        }
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        File file = null;
        boolean bl4 = false;
        char c = this.contentType;
        try {
            file = new File(string2);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string), 0x100000);
            if (bl3) {
                this.contentType = (char)98;
            }
            this.internalEncryptStream2(bufferedInputStream, file.getName(), pGPPublicKeyArray, bufferedOutputStream, new Date(file.lastModified()), bl, bl2, bl3);
            this.contentType = c;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl4 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl4 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                this.contentType = c;
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (object != null) {
                    ((File)object).delete();
                }
                if (!bl4) throw throwable;
                File file2 = new File(string);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (object != null) {
            ((File)object).delete();
        }
        if (!bl4) return;
        File file3 = new File(string);
        if (!file3.exists()) return;
        file3.delete();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFile(String string, InputStream inputStream, String string2, boolean bl, boolean bl2) throws PGPException, IOException {
        Object object;
        this.Debug("Encrypting file {0}", string);
        this.Debug("Encrypting to {0}", new File(string2).getAbsolutePath());
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        File file = null;
        boolean bl3 = false;
        try {
            file = new File(string);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string2), 0x100000);
            object = this.readPublicKeyForEncryption(inputStream);
            this.internalEncryptStream2(bufferedInputStream, file.getName(), new PGPPublicKey[]{object}, bufferedOutputStream, new Date(file.lastModified()), bl, bl2, false);
        }
        catch (PGPException pGPException) {
            try {
                bl3 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string2);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (!bl3) return;
        object = new File(string2);
        if (!((File)object).exists()) return;
        ((File)object).delete();
    }

    public int encryptFileByUserId(KeyStore keyStore, String string, String string2, String string3) {
        try {
            this.encryptFile(string, keyStore, string2, string3);
        }
        catch (Exception exception) {
            return 1;
        }
        return 0;
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFile(String string, KeyStore keyStore, String string2, String string3, boolean bl, boolean bl2) throws PGPException, IOException {
        File file;
        this.Debug("Encrypting file {0}", string);
        this.Debug("Encrypting to {0}", new File(string3).getAbsolutePath());
        InputStream inputStream = this.createPublicRingStream(keyStore, string2);
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean bl3 = false;
        try {
            file = new File(string);
            fileInputStream = new FileInputStream(string);
            fileOutputStream = new FileOutputStream(string3);
            this.encryptStream((InputStream)fileInputStream, file.getName(), inputStream, (OutputStream)fileOutputStream, bl, bl2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(fileOutputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string3);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(fileOutputStream);
        if (!bl3) return;
        file = new File(string3);
        if (!file.exists()) return;
        file.delete();
    }

    public void encryptFile(String string, KeyStore keyStore, String string2, String string3) throws PGPException, IOException {
        boolean bl = false;
        boolean bl2 = this.integrityProtect;
        this.encryptFile(string, keyStore, string2, string3, bl, bl2);
    }

    public int encryptFileByKeyId(KeyStore keyStore, String string, String string2, String string3) {
        try {
            this.encryptFile(string, keyStore, Long.decode(string2), string3);
        }
        catch (Exception exception) {
            return 0;
        }
        return 1;
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void encryptFile(String string, KeyStore keyStore, long l, String string2, boolean bl, boolean bl2) throws PGPException, IOException {
        File file;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        InputStream inputStream = null;
        this.Debug("Encrypting file {0}", new File(string).getAbsolutePath());
        this.Debug("Encrypting to {0}", new File(string2).getAbsolutePath());
        boolean bl3 = false;
        try {
            file = new File(string);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string2), 0x100000);
            inputStream = this.createPublicRingStream(keyStore, l);
            PGPPublicKey pGPPublicKey = this.readPublicKeyForEncryption(inputStream);
            this.internalEncryptStream2(bufferedInputStream, file.getName(), new PGPPublicKey[]{pGPPublicKey}, bufferedOutputStream, new Date(file.lastModified()), bl, bl2, false);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            try {
                bl3 = true;
                throw IOUtil.newPGPException(pGPException);
                catch (IOException iOException) {
                    bl3 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(bufferedOutputStream);
                IOUtil.closeStream(inputStream);
                if (!bl3) throw throwable;
                File file2 = new File(string2);
                if (!file2.exists()) throw throwable;
                file2.delete();
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(bufferedOutputStream);
        IOUtil.closeStream(inputStream);
        if (!bl3) return;
        file = new File(string2);
        if (!file.exists()) return;
        file.delete();
    }

    public void encryptFile(String string, KeyStore keyStore, long l, String string2) throws PGPException, IOException {
        boolean bl = false;
        boolean bl2 = this.integrityProtect;
        this.encryptFile(string, keyStore, l, string2, bl, bl2);
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean verifyFile(String string, String string2, String string3) throws PGPException, FileIsEncryptedException, IOException {
        File file;
        boolean bl;
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        this.Debug("Signature verification of file {0}", string);
        this.Debug("Extracting to {0}", new File(string3).getAbsolutePath());
        boolean bl2 = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            inputStream = PGPLib.readFileOrAsciiString(string2, "publicKeyFileName");
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string3), 0x100000);
            bl = this.verifyStream((InputStream)bufferedInputStream, inputStream, (OutputStream)bufferedOutputStream);
        }
        catch (PGPException pGPException) {
            try {
                bl2 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (bl2 && (file2 = new File(string3)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (bl2 && (file = new File(string3)).exists()) {
            file.delete();
        }
        return bl;
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean verifyFile(String string, KeyStore keyStore, String string2) throws PGPException, FileIsEncryptedException, IOException {
        File file;
        boolean bl;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;
        this.Debug("Signature verification of file {0}", string);
        this.Debug("Extracting to {0}", new File(string2).getAbsolutePath());
        boolean bl2 = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            fileOutputStream = new FileOutputStream(string2);
            bl = this.verifyStream((InputStream)bufferedInputStream, keyStore, (OutputStream)fileOutputStream);
        }
        catch (PGPException pGPException) {
            try {
                bl2 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(fileOutputStream);
                if (bl2 && (file2 = new File(string2)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(fileOutputStream);
        if (bl2 && (file = new File(string2)).exists()) {
            file.delete();
        }
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean verifyFile(String string, String string2) throws PGPException, FileIsEncryptedException, IOException {
        boolean bl;
        this.Debug("Signature verification of file {0}", string);
        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            inputStream = PGPLib.readFileOrAsciiString(string2, "publicKeyFileName");
            bl = this.verifyStream(fileInputStream, inputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            IOUtil.closeStream(inputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(inputStream);
        return bl;
    }

    public boolean verifyFile(InputStream inputStream, InputStream inputStream2) throws PGPException, IOException {
        return this.verifyStream(inputStream, inputStream2);
    }

    public SignatureCheckResult verifyWithoutExtracting(InputStream inputStream, InputStream inputStream2) throws PGPException, FileIsEncryptedException, IOException {
        Object object;
        if ((inputStream = PGPUtil.getDecoderStream((InputStream)inputStream)) instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream).isClearText()) {
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            return clearSignedHelper.verify2((ArmoredInputStream)object, null, inputStream2, new DummyStream());
        }
        object = new PGPObjectFactory2(inputStream);
        Object object2 = null;
        try {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object2 instanceof PGPMarker) {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        if (object2 instanceof PGPEncryptedDataList) {
            throw new FileIsEncryptedException("This file is encrypted. Use the methods <decryptAndVerifySignature> or <decrypt> to open it.");
        }
        if (object2 instanceof PGPCompressedData) {
            this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, null, inputStream2, new DummyStream());
        } else if (object2 instanceof PGPOnePassSignatureList) {
            this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, null, inputStream2, new DummyStream(), signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, null, inputStream2, new DummyStream(), signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            this.parseLiteralData((PGPLiteralData)object2, null, new DummyStream());
        } else {
            throw new NonPGPDataException("Unknown message format: " + object2);
        }
        return signatureCheck.result;
    }

    public SignatureCheckResult verifyWithoutExtracting(InputStream inputStream, InputStream inputStream2, String string, InputStream inputStream3) throws PGPException, FileIsEncryptedException, IOException {
        Object object;
        if ((inputStream = PGPUtil.getDecoderStream((InputStream)inputStream)) instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream).isClearText()) {
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            return clearSignedHelper.verify2((ArmoredInputStream)object, null, inputStream3, new DummyStream());
        }
        object = new PGPObjectFactory2(inputStream);
        Object object2 = null;
        try {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object2 instanceof PGPMarker) {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        if (object2 instanceof PGPEncryptedDataList) {
            this.parseEncryptedData((PGPEncryptedDataList)object2, true, signatureCheck, null, inputStream2, string, inputStream3, new DummyStream());
        } else if (object2 instanceof PGPCompressedData) {
            this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, null, inputStream3, new DummyStream());
        } else if (object2 instanceof PGPOnePassSignatureList) {
            this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, null, inputStream3, new DummyStream(), signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, null, inputStream3, new DummyStream(), signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            this.parseLiteralData((PGPLiteralData)object2, null, new DummyStream());
        } else {
            throw new NonPGPDataException("Unknown message format: " + object2);
        }
        return signatureCheck.result;
    }

    public SignatureCheckResult verifyWithoutExtracting(InputStream inputStream, KeyStore keyStore, String string) throws PGPException, FileIsEncryptedException, IOException {
        Object object;
        inputStream = PGPUtil.getDecoderStream((InputStream)inputStream);
        DummyStream dummyStream = new DummyStream();
        if (inputStream instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream).isClearText()) {
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            return clearSignedHelper.verify2((ArmoredInputStream)object, keyStore, null, dummyStream);
        }
        object = new PGPObjectFactory2(inputStream);
        Object object2 = null;
        try {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object2 instanceof PGPMarker) {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        if (object2 instanceof PGPEncryptedDataList) {
            this.parseEncryptedData((PGPEncryptedDataList)object2, true, signatureCheck, keyStore, null, string, null, dummyStream);
        } else if (object2 instanceof PGPCompressedData) {
            this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, keyStore, null, dummyStream);
        } else if (object2 instanceof PGPOnePassSignatureList) {
            this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, keyStore, null, dummyStream, signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, keyStore, null, dummyStream, signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            this.parseLiteralData((PGPLiteralData)object2, null, dummyStream);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object2);
        }
        return signatureCheck.result;
    }

    public SignatureCheckResult verifyWithoutExtracting(InputStream inputStream, KeyStore keyStore) throws PGPException, FileIsEncryptedException, IOException {
        Object object;
        inputStream = PGPUtil.getDecoderStream((InputStream)inputStream);
        DummyStream dummyStream = new DummyStream();
        if (inputStream instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream).isClearText()) {
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            return clearSignedHelper.verify2((ArmoredInputStream)object, keyStore, null, dummyStream);
        }
        object = new PGPObjectFactory2(inputStream);
        Object object2 = null;
        try {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object2 instanceof PGPMarker) {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        if (object2 instanceof PGPEncryptedDataList) {
            throw new FileIsEncryptedException("This file is encrypted. Use <decryptAndVerify> or <decrypt> to open it.");
        }
        if (object2 instanceof PGPCompressedData) {
            this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, keyStore, null, dummyStream);
        } else if (object2 instanceof PGPOnePassSignatureList) {
            this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, keyStore, null, dummyStream, signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, keyStore, null, dummyStream, signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            this.parseLiteralData((PGPLiteralData)object2, null, dummyStream);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object2);
        }
        return signatureCheck.result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult verifyWithoutExtracting(String string, String string2, String string3, String string4) throws IOException, PGPException, FileIsEncryptedException {
        SignatureCheckResult signatureCheckResult;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        InputStream inputStream3 = null;
        DummyStream dummyStream = new DummyStream();
        try {
            SignatureCheckResult signatureCheckResult2;
            inputStream = PGPLib.readFileOrAsciiString(string, "message");
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFile");
            inputStream3 = PGPLib.readFileOrAsciiString(string2, "privateKeyFile");
            signatureCheckResult = signatureCheckResult2 = this.verifyWithoutExtracting(inputStream, inputStream3, string3, inputStream2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            IOUtil.closeStream(dummyStream);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(dummyStream);
        return signatureCheckResult;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult verifyWithoutExtracting(String string, String string2) throws IOException, PGPException, FileIsEncryptedException {
        SignatureCheckResult signatureCheckResult;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        DummyStream dummyStream = new DummyStream();
        try {
            SignatureCheckResult signatureCheckResult2;
            inputStream = PGPLib.readFileOrAsciiString(string, "message");
            inputStream2 = PGPLib.readFileOrAsciiString(string2, "publicKeyFile");
            signatureCheckResult = signatureCheckResult2 = this.verifyAndExtract(inputStream, inputStream2, (OutputStream)dummyStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            IOUtil.closeStream(dummyStream);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(dummyStream);
        return signatureCheckResult;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult verifyWithoutExtracting(String string, KeyStore keyStore) throws IOException, PGPException, FileIsEncryptedException {
        InputStream inputStream = null;
        DummyStream dummyStream = new DummyStream();
        try {
            SignatureCheckResult signatureCheckResult;
            inputStream = PGPLib.readFileOrAsciiString(string, "message");
            SignatureCheckResult signatureCheckResult2 = signatureCheckResult = this.verifyAndExtract(inputStream, keyStore, (OutputStream)dummyStream);
            return signatureCheckResult2;
        }
        finally {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(dummyStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult verifyWithoutExtracting(String string, KeyStore keyStore, String string2) throws IOException, PGPException, FileIsEncryptedException {
        InputStream inputStream = null;
        try {
            SignatureCheckResult signatureCheckResult;
            inputStream = PGPLib.readFileOrAsciiString(string, "message");
            SignatureCheckResult signatureCheckResult2 = signatureCheckResult = this.verifyWithoutExtracting(inputStream, keyStore, string2);
            return signatureCheckResult2;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public SignatureCheckResult verifyWithoutExtracting(File file, File file2) throws PGPException, FileIsEncryptedException, IOException {
        return this.verifyWithoutExtracting(file.getAbsolutePath(), file2.getAbsolutePath());
    }

    public SignatureCheckResult verifyWithoutExtracting(File file, File file2, String string, File file3) throws PGPException, FileIsEncryptedException, IOException {
        return this.verifyWithoutExtracting(file.getAbsolutePath(), file2.getAbsolutePath(), string, file3.getAbsolutePath());
    }

    public SignatureCheckResult verifyWithoutExtracting(File file, KeyStore keyStore) throws PGPException, FileIsEncryptedException, IOException {
        return this.verifyWithoutExtracting(file.getAbsolutePath(), keyStore);
    }

    public SignatureCheckResult verifyWithoutExtracting(File file, KeyStore keyStore, String string) throws PGPException, FileIsEncryptedException, IOException {
        return this.verifyWithoutExtracting(file.getAbsolutePath(), keyStore, string);
    }

    public SignatureCheckResult verifyAndExtract(InputStream inputStream, InputStream inputStream2, OutputStream outputStream) throws PGPException, FileIsEncryptedException, IOException {
        Object object;
        if ((inputStream = PGPUtil.getDecoderStream((InputStream)inputStream)) instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream).isClearText()) {
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            return clearSignedHelper.verify2((ArmoredInputStream)object, null, inputStream2, outputStream);
        }
        object = new PGPObjectFactory2(inputStream);
        Object object2 = null;
        try {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object2 instanceof PGPMarker) {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        if (object2 instanceof PGPEncryptedDataList) {
            throw new FileIsEncryptedException("This file is encrypted. Use the methods <decryptAndVerifySignature> or <decrypt> to open it.");
        }
        if (object2 instanceof PGPCompressedData) {
            this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, null, inputStream2, outputStream);
        } else if (object2 instanceof PGPOnePassSignatureList) {
            this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, null, inputStream2, outputStream, signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, null, inputStream2, outputStream, signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            this.parseLiteralData((PGPLiteralData)object2, null, outputStream);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object2);
        }
        return signatureCheck.result;
    }

    public SignatureCheckResult verifyAndExtract(InputStream inputStream, KeyStore keyStore, OutputStream outputStream) throws PGPException, FileIsEncryptedException, IOException {
        Object object;
        if ((inputStream = PGPUtil.getDecoderStream((InputStream)inputStream)) instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream).isClearText()) {
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            return clearSignedHelper.verify2((ArmoredInputStream)object, keyStore, null, outputStream);
        }
        object = new PGPObjectFactory2(inputStream);
        Object object2 = null;
        try {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object2 instanceof PGPMarker) {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        if (object2 instanceof PGPEncryptedDataList) {
            throw new FileIsEncryptedException("This file is encrypted. Use <decryptAndVerify> or <decrypt> to open it.");
        }
        if (object2 instanceof PGPCompressedData) {
            this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, keyStore, null, outputStream);
        } else if (object2 instanceof PGPOnePassSignatureList) {
            this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, keyStore, null, outputStream, signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, keyStore, null, outputStream, signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            this.parseLiteralData((PGPLiteralData)object2, null, outputStream);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object2);
        }
        return signatureCheck.result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult verifyAndExtract(String string, String string2, StringBuffer stringBuffer, String string3) throws IOException, PGPException, FileIsEncryptedException {
        SignatureCheckResult signatureCheckResult;
        if (stringBuffer == null) {
            throw new IllegalArgumentException("The decryptedString parameter cannot be null");
        }
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string, "message");
            inputStream2 = PGPLib.readFileOrAsciiString(string2, "publicKeyFile");
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            SignatureCheckResult signatureCheckResult2 = this.verifyAndExtract(inputStream, inputStream2, (OutputStream)directByteArrayOutputStream);
            stringBuffer.setLength(0);
            stringBuffer.append(new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string3));
            signatureCheckResult = signatureCheckResult2;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(directByteArrayOutputStream);
        return signatureCheckResult;
    }

    public SignatureCheckResult verifyAndExtract(String string, String string2, StringBuffer stringBuffer) throws IOException, PGPException, FileIsEncryptedException {
        return this.verifyAndExtract(string, string2, stringBuffer, "ASCII");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult verifyAndExtract(String string, KeyStore keyStore, StringBuffer stringBuffer, String string2) throws IOException, PGPException, FileIsEncryptedException {
        SignatureCheckResult signatureCheckResult;
        if (stringBuffer == null) {
            throw new IllegalArgumentException("The decryptedString parameter cannot be null");
        }
        InputStream inputStream = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            inputStream = PGPLib.readFileOrAsciiString(string, "message");
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            SignatureCheckResult signatureCheckResult2 = this.verifyAndExtract(inputStream, keyStore, (OutputStream)directByteArrayOutputStream);
            stringBuffer.setLength(0);
            stringBuffer.append(new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string2));
            signatureCheckResult = signatureCheckResult2;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(directByteArrayOutputStream);
        return signatureCheckResult;
    }

    public SignatureCheckResult verifyAndExtract(String string, KeyStore keyStore, StringBuffer stringBuffer) throws IOException, PGPException, FileIsEncryptedException {
        return this.verifyAndExtract(string, keyStore, stringBuffer);
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public SignatureCheckResult verifyAndExtract(String string, String string2, String string3) throws PGPException, FileIsEncryptedException, IOException {
        File file;
        SignatureCheckResult signatureCheckResult;
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        this.Debug("Signature verification of file {0}", string);
        this.Debug("Extracting to {0}", new File(string3).getAbsolutePath());
        boolean bl = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string), 0x100000);
            inputStream = PGPLib.readFileOrAsciiString(string2, "publicKeyFileName");
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string3), 0x100000);
            signatureCheckResult = this.verifyAndExtract((InputStream)bufferedInputStream, inputStream, (OutputStream)bufferedOutputStream);
        }
        catch (PGPException pGPException) {
            try {
                bl = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(bufferedOutputStream);
                if (bl && (file2 = new File(string3)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedOutputStream);
        if (bl && (file = new File(string3)).exists()) {
            file.delete();
        }
        return signatureCheckResult;
    }

    public SignatureCheckResult verifyAndExtract(File file, File file2, File file3) throws PGPException, FileIsEncryptedException, IOException {
        return this.verifyAndExtract(file.getAbsolutePath(), file2.getAbsolutePath(), file3.getAbsolutePath());
    }

    public SignatureCheckResult verifyAndExtract(File file, KeyStore keyStore, File file2) throws PGPException, FileIsEncryptedException, IOException {
        return this.verifyAndExtract(file.getAbsolutePath(), keyStore, file2.getAbsolutePath());
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public SignatureCheckResult verifyAndExtract(String string, KeyStore keyStore, String string2) throws PGPException, FileIsEncryptedException, IOException {
        File file;
        SignatureCheckResult signatureCheckResult;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;
        this.Debug("Signature verification of file {0}", string);
        this.Debug("Extracting to {0}", new File(string2).getAbsolutePath());
        boolean bl = false;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            fileOutputStream = new FileOutputStream(string2);
            signatureCheckResult = this.verifyAndExtract((InputStream)bufferedInputStream, keyStore, (OutputStream)fileOutputStream);
        }
        catch (PGPException pGPException) {
            try {
                bl = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(bufferedInputStream);
                IOUtil.closeStream(fileOutputStream);
                if (bl && (file2 = new File(string2)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(bufferedInputStream);
        IOUtil.closeStream(fileOutputStream);
        if (bl && (file = new File(string2)).exists()) {
            file.delete();
        }
        return signatureCheckResult;
    }

    public boolean verifyStream(InputStream inputStream, InputStream inputStream2) throws PGPException, FileIsEncryptedException, IOException {
        inputStream = PGPUtil.getDecoderStream((InputStream)inputStream);
        return this.verifyStream(inputStream, inputStream2, (OutputStream)new DummyStream());
    }

    public boolean verifyStream(InputStream inputStream, InputStream inputStream2, OutputStream outputStream) throws PGPException, FileIsEncryptedException, IOException {
        Object object;
        if ((inputStream = PGPUtil.getDecoderStream((InputStream)inputStream)) instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream).isClearText()) {
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            return clearSignedHelper.verify2((ArmoredInputStream)object, null, inputStream2, outputStream) == SignatureCheckResult.SignatureVerified;
        }
        object = new PGPObjectFactory2(inputStream);
        Object object2 = null;
        try {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        catch (IOException iOException) {
            throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
        }
        if (object2 instanceof PGPMarker) {
            object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        if (object2 instanceof PGPEncryptedDataList) {
            throw new FileIsEncryptedException("This file is encrypted. Use <decryptAndVerify> or <decrypt> to open it.");
        }
        if (object2 instanceof PGPCompressedData) {
            this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, null, inputStream2, outputStream);
        } else if (object2 instanceof PGPOnePassSignatureList) {
            this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, null, inputStream2, outputStream, signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, null, inputStream2, outputStream, signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            this.parseLiteralData((PGPLiteralData)object2, null, outputStream);
        } else {
            throw new PGPException("Unknown message format: " + object2);
        }
        return signatureCheck.result == SignatureCheckResult.SignatureVerified;
    }

    public boolean verifyStream(InputStream inputStream, KeyStore keyStore, OutputStream outputStream) throws PGPException, FileIsEncryptedException, IOException {
        Object object;
        if ((inputStream = PGPUtil.getDecoderStream((InputStream)inputStream)) instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream).isClearText()) {
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            try {
                return clearSignedHelper.verify((ArmoredInputStream)object, keyStore, null, outputStream);
            }
            catch (SignatureException signatureException) {
                return false;
            }
        }
        object = new PGPObjectFactory2(inputStream);
        Object object2 = object.nextObject();
        if (object2 instanceof PGPMarker) {
            object2 = object.nextObject();
        }
        SignatureCheck signatureCheck = new SignatureCheck();
        if (object2 instanceof PGPEncryptedDataList) {
            throw new FileIsEncryptedException("This file is encrypted. Use <decryptAndVerify> or <decrypt> to open it.");
        }
        if (object2 instanceof PGPCompressedData) {
            this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, keyStore, null, outputStream);
        } else if (object2 instanceof PGPOnePassSignatureList) {
            this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, keyStore, null, outputStream, signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, keyStore, null, outputStream, signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            this.parseLiteralData((PGPLiteralData)object2, null, outputStream);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object2);
        }
        return signatureCheck.result == SignatureCheckResult.SignatureVerified;
    }

    public boolean verifyString(String string, String string2, StringBuffer stringBuffer) throws IOException, PGPException, FileIsEncryptedException {
        return this.verifyString(string, string2, stringBuffer, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean verifyString(String string, String string2, StringBuffer stringBuffer, String string3) throws IOException, PGPException, FileIsEncryptedException {
        boolean bl;
        if (stringBuffer == null) {
            throw new IllegalArgumentException("The decryptedString parameter cannot be null");
        }
        ByteArrayInputStream byteArrayInputStream = null;
        InputStream inputStream = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(string3));
            inputStream = PGPLib.readFileOrAsciiString(string2, "publicKeyFileName");
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            boolean bl2 = this.verifyStream((InputStream)byteArrayInputStream, inputStream, (OutputStream)directByteArrayOutputStream);
            stringBuffer.setLength(0);
            stringBuffer.append(new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string3));
            bl = bl2;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(directByteArrayOutputStream);
        return bl;
    }

    public boolean decryptAndVerifyString(String string, String string2, String string3, String string4, StringBuffer stringBuffer) throws IOException, PGPException, FileIsEncryptedException {
        return this.decryptAndVerifyString(string, string2, string3, string4, stringBuffer, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean decryptAndVerifyString(String string, InputStream inputStream, String string2, InputStream inputStream2, StringBuffer stringBuffer, String string3) throws IOException, PGPException {
        boolean bl;
        if (stringBuffer == null) {
            throw new IllegalArgumentException("The decryptedString parameter cannot be null");
        }
        ByteArrayInputStream byteArrayInputStream = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes("ASCII"));
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            boolean bl2 = this.decryptAndVerifyStream(byteArrayInputStream, inputStream, string2, inputStream2, directByteArrayOutputStream);
            stringBuffer.setLength(0);
            stringBuffer.append(new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string3));
            bl = bl2;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(directByteArrayOutputStream);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean decryptAndVerifyString(String string, String string2, String string3, String string4, StringBuffer stringBuffer, String string5) throws IOException, PGPException {
        boolean bl;
        if (stringBuffer == null) {
            throw new IllegalArgumentException("The decryptedString parameter cannot be null");
        }
        ByteArrayInputStream byteArrayInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes("ASCII"));
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFileName");
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            boolean bl2 = this.decryptAndVerifyStream(byteArrayInputStream, inputStream, string3, inputStream2, directByteArrayOutputStream);
            stringBuffer.setLength(0);
            stringBuffer.append(new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string5));
            bl = bl2;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(directByteArrayOutputStream);
        return bl;
    }

    public SignatureCheckResult decryptAndVerify(String string, String string2, String string3, String string4, StringBuffer stringBuffer) throws IOException, PGPException, FileIsEncryptedException {
        return this.decryptAndVerify(string, string2, string3, string4, stringBuffer, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult decryptAndVerify(String string, InputStream inputStream, String string2, InputStream inputStream2, StringBuffer stringBuffer, String string3) throws IOException, PGPException {
        SignatureCheckResult signatureCheckResult;
        if (stringBuffer == null) {
            throw new IllegalArgumentException("The decryptedString parameter cannot be null");
        }
        ByteArrayInputStream byteArrayInputStream = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes("ASCII"));
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            SignatureCheckResult signatureCheckResult2 = this.decryptAndVerify(byteArrayInputStream, inputStream, string2, inputStream2, directByteArrayOutputStream);
            stringBuffer.setLength(0);
            stringBuffer.append(new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string3));
            signatureCheckResult = signatureCheckResult2;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(directByteArrayOutputStream);
        return signatureCheckResult;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult decryptAndVerify(String string, String string2, String string3, String string4, StringBuffer stringBuffer, String string5) throws IOException, PGPException {
        SignatureCheckResult signatureCheckResult;
        if (stringBuffer == null) {
            throw new IllegalArgumentException("The decryptedString parameter cannot be null");
        }
        ByteArrayInputStream byteArrayInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes("ASCII"));
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFileName");
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            SignatureCheckResult signatureCheckResult2 = this.decryptAndVerify(byteArrayInputStream, inputStream, string3, inputStream2, directByteArrayOutputStream);
            stringBuffer.setLength(0);
            stringBuffer.append(new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string5));
            signatureCheckResult = signatureCheckResult2;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(directByteArrayOutputStream);
        return signatureCheckResult;
    }

    public SignatureCheckResult decryptAndVerifyString(String string, KeyStore keyStore, String string2, StringBuffer stringBuffer) throws PGPException, IOException {
        return this.decryptAndVerifyString(string, keyStore, string2, stringBuffer, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult decryptAndVerifyString(String string, KeyStore keyStore, String string2, StringBuffer stringBuffer, String string3) throws PGPException, IOException {
        SignatureCheckResult signatureCheckResult;
        ByteArrayInputStream byteArrayInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        DirectByteArrayOutputStream directByteArrayOutputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes("ASCII"));
            directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            SignatureCheckResult signatureCheckResult2 = this.decryptAndVerify(byteArrayInputStream, keyStore, string2, directByteArrayOutputStream);
            stringBuffer.setLength(0);
            stringBuffer.append(new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), string3));
            signatureCheckResult = signatureCheckResult2;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            IOUtil.closeStream(directByteArrayOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(directByteArrayOutputStream);
        return signatureCheckResult;
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean decryptAndVerifyFile(String string, KeyStore keyStore, String string2, String string3) throws PGPException, IOException {
        File file;
        boolean bl;
        this.Debug("Decrypting and verifying file {0}", new File(string).getAbsolutePath());
        this.Debug("Extracting to {0}", new File(string3).getAbsolutePath());
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        boolean bl2 = false;
        try {
            fileInputStream = new FileInputStream(string);
            fileOutputStream = new FileOutputStream(string3);
            inputStream = PGPUtil.getDecoderStream((InputStream)fileInputStream);
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
            Object object = pGPObjectFactory2.nextObject();
            if (object instanceof PGPMarker) {
                object = pGPObjectFactory2.nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
                this.parseEncryptedData(pGPEncryptedDataList, true, signatureCheck, keyStore, null, string2, null, fileOutputStream);
            } else if (object instanceof PGPCompressedData) {
                this.parseCompressedData((PGPCompressedData)object, true, signatureCheck, keyStore, null, fileOutputStream);
            } else {
                if (object == null) {
                    throw new NonPGPDataException("The supplied data is not a valid OpenPGP message");
                }
                throw new PGPException("Unknown message format: " + object);
            }
            bl = signatureCheck.result == SignatureCheckResult.SignatureVerified;
        }
        catch (PGPException pGPException) {
            try {
                bl2 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(fileOutputStream);
                if (bl2 && (file2 = new File(string3)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(fileOutputStream);
        if (bl2 && (file = new File(string3)).exists()) {
            file.delete();
        }
        return bl;
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public SignatureCheckResult decryptAndVerify(String string, String string2, String string3, String string4, String string5) throws PGPException, IOException {
        File file;
        SignatureCheckResult signatureCheckResult;
        this.Debug("Decrypting and signature verifying file {0}", string);
        this.Debug("Extracting to {0}", new File(string5).getAbsolutePath());
        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        FileOutputStream fileOutputStream = null;
        boolean bl = false;
        try {
            fileInputStream = new FileInputStream(string);
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFile");
            fileOutputStream = new FileOutputStream(string5);
            signatureCheckResult = this.decryptAndVerify(fileInputStream, inputStream, string3, inputStream2, fileOutputStream);
        }
        catch (PGPException pGPException) {
            try {
                bl = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(inputStream2);
                IOUtil.closeStream(fileOutputStream);
                if (bl && (file2 = new File(string5)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(fileOutputStream);
        if (bl && (file = new File(string5)).exists()) {
            file.delete();
        }
        return signatureCheckResult;
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public SignatureCheckResult decryptAndVerify(String string, KeyStore keyStore, String string2, String string3) throws PGPException, IOException {
        File file;
        SignatureCheckResult signatureCheckResult;
        this.Debug("Decrypting and verifying file {0}", new File(string).getAbsolutePath());
        this.Debug("Extracting to {0}", new File(string3).getAbsolutePath());
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        boolean bl = false;
        try {
            fileInputStream = new FileInputStream(string);
            fileOutputStream = new FileOutputStream(string3);
            signatureCheckResult = this.decryptAndVerify(fileInputStream, keyStore, string2, fileOutputStream);
        }
        catch (PGPException pGPException) {
            try {
                bl = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(fileOutputStream);
                if (bl && (file2 = new File(string3)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(fileOutputStream);
        if (bl && (file = new File(string3)).exists()) {
            file.delete();
        }
        return signatureCheckResult;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean decryptAndVerifyFileTo(String string, KeyStore keyStore, String string2, String string3) throws PGPException, IOException {
        boolean bl;
        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
        this.Debug("Decrypting and verifying file {0}", new File(string).getAbsolutePath());
        this.Debug("Extracting to {0}", new File(string3).getAbsolutePath());
        try {
            fileInputStream = new FileInputStream(string);
            inputStream = PGPUtil.getDecoderStream((InputStream)fileInputStream);
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
            Object object = pGPObjectFactory2.nextObject();
            if (object instanceof PGPMarker) {
                object = pGPObjectFactory2.nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
                this.parseEncryptedDataTo(pGPEncryptedDataList, true, signatureCheck, keyStore, null, string2, null, string3, string);
            } else if (object instanceof PGPCompressedData) {
                this.parseCompressedDataTo((PGPCompressedData)object, true, signatureCheck, keyStore, null, string3, string);
            } else if (object instanceof PGPOnePassSignatureList) {
                this.parseSignedDataTo((PGPOnePassSignatureList)object, pGPObjectFactory2, keyStore, null, string3, string, signatureCheck);
            } else if (object instanceof PGPSignatureList) {
                this.parseSignedDataVersion3To((PGPSignatureList)object, pGPObjectFactory2, keyStore, null, string3, string, signatureCheck);
            } else if (object instanceof PGPLiteralData) {
                this.parseLiteralDataTo((PGPLiteralData)object, null, string3, string);
            } else {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message");
            }
            bl = signatureCheck.result == SignatureCheckResult.SignatureVerified;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            IOUtil.closeStream(inputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(inputStream);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult decryptAndVerifyTo(String string, KeyStore keyStore, String string2, String string3) throws PGPException, IOException {
        SignatureCheckResult signatureCheckResult;
        BufferedInputStream bufferedInputStream = null;
        this.Debug("Decrypting and verifying file {0}", new File(string).getAbsolutePath());
        this.Debug("Extracting to {0}", new File(string3).getAbsolutePath());
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            signatureCheckResult = this.decryptAndVerifyTo(bufferedInputStream, keyStore, string2, string3);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(bufferedInputStream);
            throw throwable;
        }
        IOUtil.closeStream(bufferedInputStream);
        return signatureCheckResult;
    }

    /*
     * Loose catch block
     */
    public boolean decryptAndVerifyStream(InputStream inputStream, KeyStore keyStore, String string, OutputStream outputStream) throws PGPException, IOException {
        InputStream inputStream2 = null;
        try {
            Object object;
            inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
            if (inputStream2 instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream2).isClearText()) {
                this.Debug("Clear text signed data found");
                ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
                try {
                    boolean bl = clearSignedHelper.verify((ArmoredInputStream)object, keyStore, null, outputStream);
                    return bl;
                }
                catch (SignatureException signatureException) {
                    this.Debug("Signature exception: " + signatureException);
                    boolean bl = false;
                    IOUtil.closeStream(inputStream2);
                    return bl;
                }
            }
            object = new PGPObjectFactory2(inputStream2);
            Object object2 = null;
            try {
                object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
            }
            catch (IOException iOException) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            if (object2 instanceof PGPMarker) {
                this.Debug("Skipping PGP marker.");
                object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object2 instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object2;
                this.parseEncryptedData(pGPEncryptedDataList, true, signatureCheck, keyStore, null, string, null, outputStream);
            } else if (object2 instanceof PGPCompressedData) {
                this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, keyStore, null, outputStream);
            } else if (object2 instanceof PGPOnePassSignatureList) {
                this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, keyStore, null, outputStream, signatureCheck);
            } else if (object2 instanceof PGPSignatureList) {
                this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, keyStore, null, outputStream, signatureCheck);
            } else if (object2 instanceof PGPLiteralData) {
                this.parseLiteralData((PGPLiteralData)object2, null, outputStream);
            } else {
                throw new NonPGPDataException("Unknown message format: " + object2);
            }
            boolean bl = signatureCheck.result == SignatureCheckResult.SignatureVerified;
            return bl;
            {
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    throw IOUtil.newPGPException(pGPException);
                }
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            IOUtil.closeStream(inputStream2);
        }
    }

    public SignatureCheckResult decryptAndVerify(InputStream inputStream, KeyStore keyStore, String string, OutputStream outputStream) throws PGPException, IOException {
        InputStream inputStream2 = null;
        try {
            Object object;
            inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
            if (inputStream2 instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream2).isClearText()) {
                this.Debug("Clear text signed data found");
                ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
                SignatureCheckResult signatureCheckResult = clearSignedHelper.verify2((ArmoredInputStream)object, keyStore, null, outputStream);
                return signatureCheckResult;
            }
            object = new PGPObjectFactory2(inputStream2);
            Object object2 = null;
            try {
                object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
            }
            catch (IOException iOException) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            if (object2 instanceof PGPMarker) {
                this.Debug("Skipping PGP marker.");
                object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object2 instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object2;
                this.parseEncryptedData(pGPEncryptedDataList, true, signatureCheck, keyStore, null, string, null, outputStream);
            } else if (object2 instanceof PGPCompressedData) {
                this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, keyStore, null, outputStream);
            } else if (object2 instanceof PGPOnePassSignatureList) {
                this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, keyStore, null, outputStream, signatureCheck);
            } else if (object2 instanceof PGPSignatureList) {
                this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, keyStore, null, outputStream, signatureCheck);
            } else if (object2 instanceof PGPLiteralData) {
                this.parseLiteralData((PGPLiteralData)object2, null, outputStream);
            } else {
                throw new NonPGPDataException("Unknown message format: " + object2);
            }
            SignatureCheckResult signatureCheckResult = signatureCheck.result;
            return signatureCheckResult;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream2);
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean decryptAndVerifyFile(String string, String string2, String string3, String string4, String string5) throws PGPException, IOException {
        File file;
        boolean bl;
        this.Debug("Decrypting and signature verifying file {0}", string);
        this.Debug("Extracting to {0}", new File(string5).getAbsolutePath());
        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        FileOutputStream fileOutputStream = null;
        boolean bl2 = false;
        try {
            fileInputStream = new FileInputStream(string);
            inputStream = PGPLib.readFileOrAsciiString(string2, "privateKeyFileName");
            inputStream2 = PGPLib.readFileOrAsciiString(string4, "publicKeyFile");
            fileOutputStream = new FileOutputStream(string5);
            bl = this.decryptAndVerifyStream(fileInputStream, inputStream, string3, inputStream2, fileOutputStream);
        }
        catch (PGPException pGPException) {
            try {
                bl2 = true;
                throw pGPException;
                catch (IOException iOException) {
                    bl2 = true;
                    throw iOException;
                }
            }
            catch (Throwable throwable) {
                File file2;
                IOUtil.closeStream(fileInputStream);
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(inputStream2);
                IOUtil.closeStream(fileOutputStream);
                if (bl2 && (file2 = new File(string5)).exists()) {
                    file2.delete();
                }
                throw throwable;
            }
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        IOUtil.closeStream(fileOutputStream);
        if (bl2 && (file = new File(string5)).exists()) {
            file.delete();
        }
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean decryptAndVerifyFileTo(String string, String string2, String string3, String string4, String string5) throws PGPException, IOException {
        boolean bl;
        this.Debug("Decrypting and signature verifying file {0}", string);
        this.Debug("Extracting to {0}", new File(string5).getAbsolutePath());
        FileInputStream fileInputStream = null;
        FileInputStream fileInputStream2 = null;
        FileInputStream fileInputStream3 = null;
        try {
            fileInputStream = new FileInputStream(string);
            fileInputStream2 = new FileInputStream(string2);
            fileInputStream3 = new FileInputStream(string4);
            bl = this.decryptAndVerifyStreamTo(fileInputStream, fileInputStream2, string3, fileInputStream3, string5);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            IOUtil.closeStream(fileInputStream2);
            IOUtil.closeStream(fileInputStream3);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(fileInputStream2);
        IOUtil.closeStream(fileInputStream3);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureCheckResult decryptAndVerifyTo(String string, String string2, String string3, String string4, String string5) throws PGPException, IOException {
        SignatureCheckResult signatureCheckResult;
        this.Debug("Decrypting and signature verifying file {0}", string);
        this.Debug("Extracting to {0}", new File(string5).getAbsolutePath());
        FileInputStream fileInputStream = null;
        FileInputStream fileInputStream2 = null;
        FileInputStream fileInputStream3 = null;
        try {
            fileInputStream = new FileInputStream(string);
            fileInputStream2 = new FileInputStream(string2);
            fileInputStream3 = new FileInputStream(string4);
            signatureCheckResult = this.decryptAndVerifyTo(fileInputStream, fileInputStream2, string3, fileInputStream3, string5);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            IOUtil.closeStream(fileInputStream2);
            IOUtil.closeStream(fileInputStream3);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(fileInputStream2);
        IOUtil.closeStream(fileInputStream3);
        return signatureCheckResult;
    }

    public boolean decryptAndVerifyStream(InputStream inputStream, InputStream inputStream2, String string, InputStream inputStream3, OutputStream outputStream) throws PGPException, IOException {
        Object object;
        InputStream inputStream4 = PGPUtil.getDecoderStream((InputStream)inputStream);
        if (inputStream4 instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream4).isClearText()) {
            this.Debug("Clear text signed data found");
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            try {
                return clearSignedHelper.verify((ArmoredInputStream)object, null, inputStream3, outputStream);
            }
            catch (SignatureException signatureException) {
                this.Debug("Signature exception: " + signatureException);
                return false;
            }
        }
        try {
            object = new PGPObjectFactory2(inputStream4);
            Object object2 = null;
            try {
                object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
            }
            catch (IOException iOException) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            if (object2 instanceof PGPMarker) {
                this.Debug("Skipping marker packet.");
                object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object2 instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object2;
                this.parseEncryptedData(pGPEncryptedDataList, true, signatureCheck, null, inputStream2, string, inputStream3, outputStream);
            } else if (object2 instanceof PGPCompressedData) {
                this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, null, inputStream3, outputStream);
            } else if (object2 instanceof PGPOnePassSignatureList) {
                this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, null, inputStream3, outputStream, signatureCheck);
            } else if (object2 instanceof PGPSignatureList) {
                this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, null, inputStream3, outputStream, signatureCheck);
            } else if (object2 instanceof PGPLiteralData) {
                this.parseLiteralData((PGPLiteralData)object2, null, outputStream);
            } else {
                throw new NonPGPDataException("Unknown message format: " + object2);
            }
            boolean bl = signatureCheck.result == SignatureCheckResult.SignatureVerified;
            return bl;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            this.Debug("PGPException " + (Object)((Object)pGPException));
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream4);
        }
    }

    public SignatureCheckResult decryptAndVerify(InputStream inputStream, InputStream inputStream2, String string, InputStream inputStream3, OutputStream outputStream) throws PGPException, IOException {
        Object object;
        InputStream inputStream4 = PGPUtil.getDecoderStream((InputStream)inputStream);
        if (inputStream4 instanceof ArmoredInputStream && (object = (ArmoredInputStream)inputStream4).isClearText()) {
            this.Debug("Clear text signed data found");
            ClearSignedHelper clearSignedHelper = new ClearSignedHelper();
            return clearSignedHelper.verify2((ArmoredInputStream)object, null, inputStream3, outputStream);
        }
        try {
            object = new PGPObjectFactory2(inputStream4);
            Object object2 = null;
            try {
                object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
            }
            catch (IOException iOException) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            if (object2 instanceof PGPMarker) {
                this.Debug("Skipping marker packet.");
                object2 = ((PGPObjectFactory2)((Object)object)).nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object2 instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object2;
                this.parseEncryptedData(pGPEncryptedDataList, true, signatureCheck, null, inputStream2, string, inputStream3, outputStream);
            } else if (object2 instanceof PGPCompressedData) {
                this.parseCompressedData((PGPCompressedData)object2, true, signatureCheck, null, inputStream3, outputStream);
            } else if (object2 instanceof PGPOnePassSignatureList) {
                this.parseSignedData((PGPOnePassSignatureList)object2, (PGPObjectFactory)object, null, inputStream3, outputStream, signatureCheck);
            } else if (object2 instanceof PGPSignatureList) {
                this.parseSignedDataVersion3((PGPSignatureList)object2, (PGPObjectFactory)object, null, inputStream3, outputStream, signatureCheck);
            } else if (object2 instanceof PGPLiteralData) {
                this.parseLiteralData((PGPLiteralData)object2, null, outputStream);
            } else {
                throw new NonPGPDataException("Unknown message format: " + object2);
            }
            SignatureCheckResult signatureCheckResult = signatureCheck.result;
            return signatureCheckResult;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            this.Debug("PGPException " + (Object)((Object)pGPException));
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream4);
        }
    }

    public boolean decryptAndVerifyStreamTo(InputStream inputStream, InputStream inputStream2, String string, InputStream inputStream3, String string2) throws PGPException, IOException {
        this.Debug("Decrypting and signature verifying of stream data to {0}", new File(string2).getAbsolutePath());
        InputStream inputStream4 = null;
        try {
            inputStream4 = PGPUtil.getDecoderStream((InputStream)inputStream);
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream4);
            Object object = null;
            try {
                object = pGPObjectFactory2.nextObject();
            }
            catch (IOException iOException) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            if (object instanceof PGPMarker) {
                this.Debug("Skipping marker packet.");
                object = pGPObjectFactory2.nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
                this.parseEncryptedDataTo(pGPEncryptedDataList, true, signatureCheck, null, inputStream2, string, inputStream3, string2, null);
            } else if (object instanceof PGPCompressedData) {
                this.parseCompressedDataTo((PGPCompressedData)object, true, signatureCheck, null, inputStream3, string2, null);
            } else if (object instanceof PGPOnePassSignatureList) {
                this.parseSignedDataTo((PGPOnePassSignatureList)object, pGPObjectFactory2, null, inputStream3, string2, null, signatureCheck);
            } else if (object instanceof PGPSignatureList) {
                this.parseSignedDataVersion3To((PGPSignatureList)object, pGPObjectFactory2, null, inputStream3, string2, null, signatureCheck);
            } else if (object instanceof PGPLiteralData) {
                this.parseLiteralDataTo((PGPLiteralData)object, null, string2, null);
            } else {
                throw new NonPGPDataException("Unknown message format: " + object);
            }
            boolean bl = signatureCheck.result == SignatureCheckResult.SignatureVerified;
            return bl;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream4);
        }
    }

    public SignatureCheckResult decryptAndVerifyTo(InputStream inputStream, InputStream inputStream2, String string, InputStream inputStream3, String string2) throws PGPException, IOException {
        this.Debug("Decrypting and signature verifying of stream data to {0}", new File(string2).getAbsolutePath());
        InputStream inputStream4 = null;
        try {
            inputStream4 = PGPUtil.getDecoderStream((InputStream)inputStream);
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream4);
            Object object = null;
            try {
                object = pGPObjectFactory2.nextObject();
            }
            catch (IOException iOException) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            if (object instanceof PGPMarker) {
                this.Debug("Skipping marker packet.");
                object = pGPObjectFactory2.nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
                this.parseEncryptedDataTo(pGPEncryptedDataList, true, signatureCheck, null, inputStream2, string, inputStream3, string2, null);
            } else if (object instanceof PGPCompressedData) {
                this.parseCompressedDataTo((PGPCompressedData)object, true, signatureCheck, null, inputStream3, string2, null);
            } else if (object instanceof PGPOnePassSignatureList) {
                this.parseSignedDataTo((PGPOnePassSignatureList)object, pGPObjectFactory2, null, inputStream3, string2, null, signatureCheck);
            } else if (object instanceof PGPSignatureList) {
                this.parseSignedDataVersion3To((PGPSignatureList)object, pGPObjectFactory2, null, inputStream3, string2, null, signatureCheck);
            } else if (object instanceof PGPLiteralData) {
                this.parseLiteralDataTo((PGPLiteralData)object, null, string2, null);
            } else {
                throw new NonPGPDataException("Unknown message format: " + object);
            }
            SignatureCheckResult signatureCheckResult = signatureCheck.result;
            return signatureCheckResult;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream4);
        }
    }

    public boolean decryptAndVerifyStreamTo(InputStream inputStream, KeyStore keyStore, String string, String string2) throws PGPException, IOException {
        this.Debug("Decrypting and signature verifying of stream data to {0}", new File(string2).getAbsolutePath());
        InputStream inputStream2 = null;
        try {
            inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
            Object object = pGPObjectFactory2.nextObject();
            if (object instanceof PGPMarker) {
                object = pGPObjectFactory2.nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
                this.parseEncryptedDataTo(pGPEncryptedDataList, true, signatureCheck, keyStore, null, string, null, string2, null);
            } else if (object instanceof PGPCompressedData) {
                this.parseCompressedDataTo((PGPCompressedData)object, true, signatureCheck, keyStore, null, string2, null);
            } else {
                if (object == null) {
                    throw new NonPGPDataException("The supplied data is not a valid OpenPGP message");
                }
                throw new lw.bouncycastle.openpgp.PGPException("Unknown message format: " + object);
            }
            boolean bl = signatureCheck.result == SignatureCheckResult.SignatureVerified;
            return bl;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream2);
        }
    }

    public SignatureCheckResult decryptAndVerifyTo(InputStream inputStream, KeyStore keyStore, String string, String string2) throws PGPException, IOException {
        this.Debug("Decrypting and signature verifying of stream data to {0}", new File(string2).getAbsolutePath());
        InputStream inputStream2 = null;
        try {
            inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
            Object object = pGPObjectFactory2.nextObject();
            if (object instanceof PGPMarker) {
                object = pGPObjectFactory2.nextObject();
            }
            SignatureCheck signatureCheck = new SignatureCheck();
            if (object instanceof PGPEncryptedDataList) {
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
                this.parseEncryptedDataTo(pGPEncryptedDataList, true, signatureCheck, keyStore, null, string, null, string2, null);
            } else if (object instanceof PGPCompressedData) {
                this.parseCompressedDataTo((PGPCompressedData)object, true, signatureCheck, keyStore, null, string2, null);
            } else {
                if (object == null) {
                    throw new NonPGPDataException("The supplied data is not a valid OpenPGP message");
                }
                throw new lw.bouncycastle.openpgp.PGPException("Unknown message format: " + object);
            }
            SignatureCheckResult signatureCheckResult = signatureCheck.result;
            return signatureCheckResult;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        finally {
            IOUtil.closeStream(inputStream2);
        }
    }

    private void internalEncryptStream(InputStream inputStream, String string, long l, PGPPublicKey[] pGPPublicKeyArray, OutputStream outputStream, boolean bl, boolean bl2, boolean bl3) throws PGPException, IOException {
        OutputStream outputStream2;
        OutputStream outputStream3;
        File file;
        block18: {
            if (this.pgp2Compatible) {
                this.internalEncryptStreamPgp2x(inputStream, string, pGPPublicKeyArray, outputStream, bl);
                return;
            }
            if (!(outputStream instanceof BufferedOutputStream)) {
                outputStream = new BufferedOutputStream(outputStream, 0x100000);
            }
            OutputStream outputStream4 = null;
            if (bl) {
                this.Debug("Output in ASCII armored format.");
                outputStream4 = outputStream;
                outputStream = new ArmoredOutputStream(outputStream4);
                this.setAsciiVersionHeader(outputStream);
            }
            int n = this.compression.get(0);
            int n2 = this.cypher.get(0);
            if (pGPPublicKeyArray.length == 1) {
                n = this.selectPreferredCompression(pGPPublicKeyArray[0]);
                n2 = this.selectPreferredCypher(pGPPublicKeyArray[0]);
                this.Debug("Encrypting with cypher {0}", KeyStore.cypherToString(n2));
                this.Debug("Compression is {0}", KeyStore.compressionToString(n));
            }
            file = null;
            outputStream3 = null;
            outputStream2 = null;
            try {
                int n3;
                PGPEncryptedDataGenerator pGPEncryptedDataGenerator = new PGPEncryptedDataGenerator(this.bcFactory.CreatePGPDataEncryptorBuilder(n2, bl2, IOUtil.getSecureRandom()));
                for (n3 = 0; n3 < pGPPublicKeyArray.length; ++n3) {
                    this.Debug("Encrypting with key {0} ", KeyPairInformation.keyId2Hex(pGPPublicKeyArray[n3].getKeyID()));
                    pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKeyArray[n3]));
                }
                for (n3 = 0; n3 < this.getMasterKeysCount(); ++n3) {
                    KeyPairInformation keyPairInformation = (KeyPairInformation)this.masterKeysList.get(n3);
                    PGPPublicKey pGPPublicKey = this.readPublicKeyForEncryption(keyPairInformation.getPublicKeyRing());
                    pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKey));
                    this.Debug("Encrypting with master key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()));
                }
                if (bl3) {
                    BCPGOutputStream bCPGOutputStream = new BCPGOutputStream(outputStream);
                    this.writeMarker(bCPGOutputStream);
                    bCPGOutputStream.flush();
                }
                outputStream2 = pGPEncryptedDataGenerator.open(outputStream, new byte[0x100000]);
                PGPCompressedDataGenerator pGPCompressedDataGenerator = new PGPCompressedDataGenerator(n);
                if (n == 0) {
                    this.writeFileToLiteralData(outputStream2, this.getContentType(), inputStream, string, l, new Date());
                    break block18;
                }
                outputStream3 = pGPCompressedDataGenerator.open(outputStream2);
                this.writeFileToLiteralData(outputStream3, this.getContentType(), inputStream, string, l, new Date());
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                try {
                    throw IOUtil.newPGPException(pGPException);
                }
                catch (Throwable throwable) {
                    IOUtil.closeStream(outputStream3);
                    IOUtil.closeStream(outputStream2);
                    outputStream.flush();
                    if (bl) {
                        IOUtil.closeStream(outputStream);
                    }
                    if (file != null) {
                        file.delete();
                    }
                    throw throwable;
                }
            }
        }
        IOUtil.closeStream(outputStream3);
        IOUtil.closeStream(outputStream2);
        outputStream.flush();
        if (bl) {
            IOUtil.closeStream(outputStream);
        }
        if (file != null) {
            file.delete();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void internalEncryptStream2(InputStream inputStream, String string, PGPPublicKey[] pGPPublicKeyArray, OutputStream outputStream, Date date, boolean bl, boolean bl2, boolean bl3) throws PGPException, IOException {
        Object object;
        KeyPairInformation keyPairInformation;
        BCPGOutputStream bCPGOutputStream;
        if (this.pgp2Compatible) {
            this.internalEncryptStreamPgp2x(inputStream, string, pGPPublicKeyArray, outputStream, bl);
            return;
        }
        if (!(outputStream instanceof BufferedOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream, 0x100000);
        }
        OutputStream outputStream2 = null;
        if (bl) {
            outputStream2 = outputStream;
            outputStream = new ArmoredOutputStream(outputStream2);
            this.setAsciiVersionHeader(outputStream);
        }
        int n = this.compression.get(0);
        int n2 = this.cypher.get(0);
        if (pGPPublicKeyArray.length == 1) {
            n = this.selectPreferredCompression(pGPPublicKeyArray[0]);
            n2 = this.selectPreferredCypher(pGPPublicKeyArray[0]);
        }
        this.Debug("Encrypting with cypher {0}", KeyStore.cypherToString(n2));
        this.Debug("Compression is {0}", KeyStore.compressionToString(n));
        if (bl3) {
            bCPGOutputStream = new BCPGOutputStream(outputStream);
            this.writeMarker(bCPGOutputStream);
            bCPGOutputStream.flush();
        }
        bCPGOutputStream = new PGPEncryptedDataGenerator(this.bcFactory.CreatePGPDataEncryptorBuilder(n2, bl2, IOUtil.getSecureRandom()));
        OutputStream outputStream3 = null;
        try {
            int n3;
            for (n3 = 0; n3 < pGPPublicKeyArray.length; ++n3) {
                this.Debug("Encrypting with key {0} ", KeyPairInformation.keyId2Hex(pGPPublicKeyArray[n3].getKeyID()));
                bCPGOutputStream.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKeyArray[n3]));
            }
            for (n3 = 0; n3 < this.getMasterKeysCount(); ++n3) {
                keyPairInformation = (KeyPairInformation)this.masterKeysList.get(n3);
                object = this.readPublicKeyForEncryption(keyPairInformation.getPublicKeyRing());
                bCPGOutputStream.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator((PGPPublicKey)object));
                this.Debug("Encrypting with master key Id {0}", KeyPairInformation.keyId2Hex(object.getKeyID()));
            }
            outputStream3 = bCPGOutputStream.open(outputStream, new byte[0x100000]);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        try {
            PGPCompressedDataGenerator pGPCompressedDataGenerator = new PGPCompressedDataGenerator(n);
            keyPairInformation = new PGPLiteralDataGenerator();
            object = null;
            try {
                if (n == 0) {
                    object = keyPairInformation.open(outputStream3, this.getContentType(), string, date, new byte[0x100000]);
                    PGPLib.pipeAll(inputStream, (OutputStream)object);
                } else {
                    object = keyPairInformation.open(pGPCompressedDataGenerator.open(outputStream3), this.getContentType(), string, date, new byte[0x100000]);
                    PGPLib.pipeAll(inputStream, (OutputStream)object);
                }
            }
            finally {
                if (keyPairInformation != null) {
                    keyPairInformation.close();
                }
                IOUtil.closeStream((OutputStream)object);
                IOUtil.closeStream(inputStream);
                pGPCompressedDataGenerator.close();
            }
        }
        finally {
            IOUtil.closeStream(outputStream3);
            outputStream.flush();
            if (bl) {
                IOUtil.closeStream(outputStream);
            }
        }
    }

    private void internalEncryptStreamPgp2x(InputStream inputStream, String string, PGPPublicKey[] pGPPublicKeyArray, OutputStream outputStream, boolean bl) throws PGPException, IOException {
        Object object;
        Object object2;
        this.Debug("Encrypting in PGP 2.x compatibility mode");
        if (string == null || "".equals(string.trim())) {
            this.Debug("No internal file name label was specified. Using {0} instead.", "_CONSOLE");
            string = "_CONSOLE";
        }
        OutputStream outputStream2 = null;
        OutputStream outputStream3 = outputStream;
        if (bl) {
            this.Debug("Output is ASCII armored");
            outputStream2 = outputStream3;
            outputStream3 = new ArmoredOutputStream(outputStream2);
            this.setAsciiVersionHeader(outputStream3);
        }
        int n = 1;
        boolean bl2 = true;
        this.Debug("Cipher used is {0}", KeyStore.cypherToString(n));
        PGPEncryptedDataGenerator pGPEncryptedDataGenerator = this.bcFactory.CreatePGPEncryptedDataGenerator(n, false, IOUtil.getSecureRandom(), bl2);
        for (int i = 0; i < pGPPublicKeyArray.length; ++i) {
            pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKeyArray[i]));
            for (int j = 0; j < this.getMasterKeysCount(); ++j) {
                object2 = (KeyPairInformation)this.masterKeysList.get(j);
                object = this.readPublicKeyForEncryption(((KeyPairInformation)object2).getPublicKeyRing());
                pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator((PGPPublicKey)object));
                this.Debug("Encrypting with master key Id {0}", KeyPairInformation.keyId2Hex(object.getKeyID()));
            }
            this.Debug("Encrypting for key Id {0}", String.valueOf(pGPPublicKeyArray[i].getKeyID()));
        }
        DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
        BaseLib.pipeAll(inputStream, directByteArrayOutputStream);
        DirectByteArrayOutputStream directByteArrayOutputStream2 = new DirectByteArrayOutputStream(0x100000);
        object2 = new PGPLiteralDataGenerator(bl2);
        object = object2.open((OutputStream)directByteArrayOutputStream2, this.contentType, string, (long)directByteArrayOutputStream.size(), new Date());
        directByteArrayOutputStream.writeTo((OutputStream)object);
        object2.close();
        ((OutputStream)object).close();
        OutputStream outputStream4 = null;
        try {
            outputStream4 = pGPEncryptedDataGenerator.open(outputStream3, (long)directByteArrayOutputStream2.size());
            directByteArrayOutputStream2.writeTo(outputStream4);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        IOUtil.closeStream(outputStream4);
        outputStream3.flush();
        if (bl) {
            IOUtil.closeStream(outputStream3);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void inernalEncryptStream(InputStream inputStream, String string, PGPPublicKey[] pGPPublicKeyArray, OutputStream outputStream, Date date, boolean bl, boolean bl2, boolean bl3) throws PGPException, IOException {
        Object object;
        KeyPairInformation keyPairInformation;
        BCPGOutputStream bCPGOutputStream;
        OutputStream outputStream2 = null;
        if (bl) {
            outputStream2 = outputStream;
            outputStream = new ArmoredOutputStream(outputStream2);
            this.setAsciiVersionHeader(outputStream);
        }
        int n = this.compression.get(0);
        int n2 = this.cypher.get(0);
        if (pGPPublicKeyArray.length == 1) {
            n = this.selectPreferredCompression(pGPPublicKeyArray[0]);
            n2 = this.selectPreferredCypher(pGPPublicKeyArray[0]);
            this.Debug("Encrypting with cypher {0}", KeyStore.cypherToString(n2));
            this.Debug("Compression is {0}", KeyStore.compressionToString(n));
        }
        if (bl3) {
            bCPGOutputStream = new BCPGOutputStream(outputStream);
            this.writeMarker(bCPGOutputStream);
            bCPGOutputStream.flush();
        }
        bCPGOutputStream = new PGPEncryptedDataGenerator(this.bcFactory.CreatePGPDataEncryptorBuilder(n2, bl2, IOUtil.getSecureRandom()));
        OutputStream outputStream3 = null;
        try {
            int n3;
            for (n3 = 0; n3 < pGPPublicKeyArray.length; ++n3) {
                this.Debug("Encrypting with key {0} ", KeyPairInformation.keyId2Hex(pGPPublicKeyArray[n3].getKeyID()));
                bCPGOutputStream.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKeyArray[n3]));
            }
            for (n3 = 0; n3 < this.getMasterKeysCount(); ++n3) {
                keyPairInformation = (KeyPairInformation)this.masterKeysList.get(n3);
                object = this.readPublicKeyForEncryption(keyPairInformation.getPublicKeyRing());
                bCPGOutputStream.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator((PGPPublicKey)object));
                this.Debug("Encrypting with master key Id {0}", KeyPairInformation.keyId2Hex(object.getKeyID()));
            }
            outputStream3 = bCPGOutputStream.open(outputStream, new byte[0x100000]);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        try {
            PGPCompressedDataGenerator pGPCompressedDataGenerator = new PGPCompressedDataGenerator(n);
            keyPairInformation = new PGPLiteralDataGenerator();
            object = null;
            try {
                if (n == 0) {
                    object = keyPairInformation.open(outputStream3, 'b', string, date, new byte[0x100000]);
                    PGPLib.pipeAll(inputStream, (OutputStream)object);
                } else {
                    object = keyPairInformation.open(pGPCompressedDataGenerator.open(outputStream3), 'b', string, date, new byte[0x100000]);
                    PGPLib.pipeAll(inputStream, (OutputStream)object);
                }
            }
            finally {
                if (keyPairInformation != null) {
                    keyPairInformation.close();
                }
                IOUtil.closeStream((OutputStream)object);
                IOUtil.closeStream(inputStream);
                pGPCompressedDataGenerator.close();
            }
        }
        finally {
            IOUtil.closeStream(outputStream3);
            if (bl) {
                IOUtil.closeStream(outputStream);
            }
        }
    }

    private void inernalEncryptStreamPBE(InputStream inputStream, String string, long l, PGPPublicKey pGPPublicKey, String string2, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException {
        if (!(outputStream instanceof BufferedOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream, 0x100000);
        }
        try {
            OutputStream outputStream2;
            OutputStream outputStream3;
            block12: {
                OutputStream outputStream4 = null;
                if (bl) {
                    outputStream4 = outputStream;
                    outputStream = new ArmoredOutputStream(outputStream4);
                    this.setAsciiVersionHeader(outputStream);
                }
                Object var11_11 = null;
                outputStream3 = null;
                outputStream2 = null;
                try {
                    int n = this.selectPreferredCompression(pGPPublicKey);
                    PGPCompressedDataGenerator pGPCompressedDataGenerator = new PGPCompressedDataGenerator(n);
                    int n2 = this.selectPreferredCypher(pGPPublicKey);
                    PGPEncryptedDataGenerator pGPEncryptedDataGenerator = this.bcFactory.CreatePGPEncryptedDataGenerator(n2, bl2, IOUtil.getSecureRandom());
                    pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKey));
                    this.Debug("Encrypting with key {0} ", KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()));
                    pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePBEKeyEncryptionMethodGenerator(string2));
                    this.Debug("Encrypting with password");
                    for (int i = 0; i < this.getMasterKeysCount(); ++i) {
                        KeyPairInformation keyPairInformation = (KeyPairInformation)this.masterKeysList.get(i);
                        PGPPublicKey pGPPublicKey2 = this.readPublicKeyForEncryption(keyPairInformation.getPublicKeyRing());
                        pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKey2));
                        this.Debug("Encrypting with master key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKey2.getKeyID()));
                    }
                    outputStream2 = pGPEncryptedDataGenerator.open(outputStream, new byte[0x100000]);
                    if (n == 0) {
                        this.writeFileToLiteralData(outputStream2, this.getContentType(), inputStream, string, l, new Date());
                        break block12;
                    }
                    this.writeFileToLiteralData(pGPCompressedDataGenerator.open(outputStream2), this.getContentType(), inputStream, string, l, new Date());
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    try {
                        throw IOUtil.newPGPException(pGPException);
                    }
                    catch (Throwable throwable) {
                        IOUtil.closeStream(outputStream3);
                        IOUtil.closeStream(outputStream2);
                        outputStream.flush();
                        if (bl) {
                            IOUtil.closeStream(outputStream);
                        }
                        throw throwable;
                    }
                }
            }
            IOUtil.closeStream(outputStream3);
            IOUtil.closeStream(outputStream2);
            outputStream.flush();
            if (bl) {
                IOUtil.closeStream(outputStream);
            }
        }
        catch (IOException iOException) {
            throw new PGPException(iOException.getMessage(), iOException);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void inernalEncryptStreamPBE(InputStream inputStream, String string, String string2, OutputStream outputStream, boolean bl, boolean bl2) throws PGPException {
        try {
            Object object;
            KeyPairInformation keyPairInformation;
            OutputStream outputStream2 = null;
            if (bl) {
                outputStream2 = outputStream;
                outputStream = new ArmoredOutputStream(outputStream2);
                this.setAsciiVersionHeader(outputStream);
            }
            OutputStream outputStream3 = null;
            int n = this.compression.get(0);
            int n2 = this.cypher.get(0);
            PGPEncryptedDataGenerator pGPEncryptedDataGenerator = this.bcFactory.CreatePGPEncryptedDataGenerator(n2, bl2, IOUtil.getSecureRandom());
            try {
                pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePBEKeyEncryptionMethodGenerator(string2));
                this.Debug("Encrypting with password");
                for (int i = 0; i < this.getMasterKeysCount(); ++i) {
                    keyPairInformation = (KeyPairInformation)this.masterKeysList.get(i);
                    object = this.readPublicKeyForEncryption(keyPairInformation.getPublicKeyRing());
                    pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator((PGPPublicKey)object));
                    this.Debug("Encrypting with master key Id {0}", KeyPairInformation.keyId2Hex(object.getKeyID()));
                }
                outputStream3 = pGPEncryptedDataGenerator.open(outputStream, new byte[0x100000]);
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
            try {
                PGPCompressedDataGenerator pGPCompressedDataGenerator = new PGPCompressedDataGenerator(n);
                keyPairInformation = new PGPLiteralDataGenerator();
                object = null;
                if (n == 0) {
                    object = keyPairInformation.open(outputStream3, this.getContentType(), string, new Date(), new byte[0x100000]);
                    PGPLib.pipeAll(inputStream, (OutputStream)object);
                } else {
                    object = keyPairInformation.open(pGPCompressedDataGenerator.open(outputStream3), this.getContentType(), string, new Date(), new byte[0x100000]);
                    PGPLib.pipeAll(inputStream, (OutputStream)object);
                }
                IOUtil.closeStream((OutputStream)object);
                pGPCompressedDataGenerator.close();
            }
            finally {
                IOUtil.closeStream(inputStream);
                IOUtil.closeStream(outputStream3);
                outputStream.flush();
                if (bl) {
                    IOUtil.closeStream(outputStream);
                }
            }
        }
        catch (IOException iOException) {
            throw new PGPException(iOException.getMessage(), iOException);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String parseLiteralDataVersion3(PGPLiteralData pGPLiteralData, PGPSignature pGPSignature, OutputStream outputStream) throws IOException {
        String string = pGPLiteralData.getFileName();
        this.Debug("Found literal data packet");
        this.Debug("Decrypted file original name is {0}", string);
        InputStream inputStream = null;
        try {
            int n;
            inputStream = pGPLiteralData.getInputStream();
            byte[] byArray = new byte[0x100000];
            while ((n = inputStream.read(byArray, 0, byArray.length)) > 0) {
                if (pGPSignature != null) {
                    pGPSignature.update(byArray, 0, n);
                }
                if (outputStream == null) continue;
                outputStream.write(byArray, 0, n);
            }
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String[] parseLiteralDataVersion3To(PGPLiteralData pGPLiteralData, PGPSignature pGPSignature, String string, String string2) throws IOException {
        this.Debug("Found literal data packet");
        String string3 = pGPLiteralData.getFileName();
        if (string3.toUpperCase().endsWith(".TAR") && this.extractEmbeddedTar) {
            this.Debug("Found multiple file archive");
            TarInputStream tarInputStream = new TarInputStream(pGPLiteralData.getInputStream());
            return tarInputStream.extractAll(string);
        }
        FileOutputStream fileOutputStream = null;
        try {
            int n;
            if (string3 == null || "".equals(string3)) {
                if (string2 != null && !"".equals(string2)) {
                    string3 = new File(string2).getName();
                    if (string3.lastIndexOf(".") > 0) {
                        string3 = string3.substring(0, string3.lastIndexOf("."));
                    }
                } else {
                    string3 = "output";
                }
            }
            fileOutputStream = new FileOutputStream(string + File.separator + string3);
            this.Debug("Extracting to {0}", string + File.separator + string3);
            InputStream inputStream = pGPLiteralData.getInputStream();
            byte[] byArray = new byte[0x100000];
            while ((n = inputStream.read(byArray, 0, byArray.length)) > 0) {
                if (pGPSignature != null) {
                    pGPSignature.update(byArray, 0, n);
                }
                fileOutputStream.write(byArray, 0, n);
            }
            IOUtil.closeStream(inputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileOutputStream);
        return new String[]{string3};
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String parseLiteralData(PGPLiteralData pGPLiteralData, PGPOnePassSignature pGPOnePassSignature, OutputStream outputStream) throws IOException {
        String string = pGPLiteralData.getFileName();
        InputStream inputStream = pGPLiteralData.getInputStream();
        this.Debug("Found literal data packet");
        this.Debug("Decrypted file original name is {0}", string);
        byte[] byArray = new byte[0x100000];
        try {
            int n;
            while ((n = inputStream.read(byArray, 0, byArray.length)) > 0) {
                if (pGPOnePassSignature != null) {
                    pGPOnePassSignature.update(byArray, 0, n);
                }
                if (outputStream == null) continue;
                outputStream.write(byArray, 0, n);
            }
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String[] parseLiteralDataTo(PGPLiteralData pGPLiteralData, PGPOnePassSignature pGPOnePassSignature, String string, String string2) throws IOException {
        this.Debug("Found literal data packet");
        String string3 = pGPLiteralData.getFileName();
        if (string3.toUpperCase().endsWith(".TAR") && this.extractEmbeddedTar) {
            this.Debug("Found multiple file archive");
            TarInputStream tarInputStream = new TarInputStream(pGPLiteralData.getInputStream());
            return tarInputStream.extractAll(string);
        }
        InputStream inputStream = pGPLiteralData.getInputStream();
        this.Debug("Decrypted file original name is {0}", pGPLiteralData.getFileName());
        if (string3 == null || "".equals(string3)) {
            if (string2 != null && !"".equals(string2)) {
                string3 = new File(string2).getName();
                if (string3.lastIndexOf(".") > 0) {
                    string3 = string3.substring(0, string3.lastIndexOf("."));
                }
            } else {
                string3 = "output";
            }
        }
        this.Debug("Extracting to {0}", string + File.separator + string3);
        FileOutputStream fileOutputStream = new FileOutputStream(string + File.separator + string3);
        byte[] byArray = new byte[0x100000];
        try {
            int n;
            while ((n = inputStream.read(byArray, 0, byArray.length)) > 0) {
                if (pGPOnePassSignature != null) {
                    pGPOnePassSignature.update(byArray, 0, n);
                }
                fileOutputStream.write(byArray, 0, n);
            }
            IOUtil.closeStream(inputStream);
        }
        finally {
            IOUtil.closeStream(fileOutputStream);
        }
        return new String[]{string3};
    }

    private String parseCompressedData(PGPCompressedData pGPCompressedData, boolean bl, SignatureCheck signatureCheck, KeyStore keyStore, InputStream inputStream, OutputStream outputStream) throws PGPException, IOException {
        this.Debug("Decrypted data compression algorithm is {0}", KeyStore.compressionToString(pGPCompressedData.getAlgorithm()));
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(pGPCompressedData.getDataStream());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        try {
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(bufferedInputStream);
            Object object = pGPObjectFactory2.nextObject();
            if (object instanceof PGPLiteralData) {
                String string = this.parseLiteralData((PGPLiteralData)object, null, outputStream);
                return string;
            }
            if (object instanceof PGPOnePassSignatureList) {
                if (bl) {
                    String string = this.parseSignedData((PGPOnePassSignatureList)object, pGPObjectFactory2, keyStore, inputStream, outputStream, signatureCheck);
                    return string;
                }
                String string = this.parseSignedData((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null, outputStream, signatureCheck);
                return string;
            }
            if (object instanceof PGPSignatureList) {
                if (bl) {
                    String string = this.parseSignedDataVersion3((PGPSignatureList)object, pGPObjectFactory2, keyStore, inputStream, outputStream, signatureCheck);
                    return string;
                }
                String string = this.parseSignedDataVersion3((PGPSignatureList)object, pGPObjectFactory2, null, null, outputStream, signatureCheck);
                return string;
            }
            throw new PGPException("Unknown message format: " + object);
        }
        finally {
            IOUtil.closeStream(bufferedInputStream);
        }
    }

    private String[] parseCompressedDataTo(PGPCompressedData pGPCompressedData, boolean bl, SignatureCheck signatureCheck, KeyStore keyStore, InputStream inputStream, String string, String string2) throws PGPException, IOException {
        this.Debug("Decrypted data compression algorithm is {0}", KeyStore.compressionToString(pGPCompressedData.getAlgorithm()));
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(pGPCompressedData.getDataStream());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        try {
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(bufferedInputStream);
            Object object = pGPObjectFactory2.nextObject();
            if (object instanceof PGPLiteralData) {
                String[] stringArray = this.parseLiteralDataTo((PGPLiteralData)object, null, string, string2);
                return stringArray;
            }
            if (object instanceof PGPOnePassSignatureList) {
                if (bl) {
                    String[] stringArray = this.parseSignedDataTo((PGPOnePassSignatureList)object, pGPObjectFactory2, keyStore, inputStream, string, string2, signatureCheck);
                    return stringArray;
                }
                String[] stringArray = this.parseSignedDataTo((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null, string, string2, signatureCheck);
                return stringArray;
            }
            if (object instanceof PGPSignatureList) {
                if (bl) {
                    String[] stringArray = this.parseSignedDataVersion3To((PGPSignatureList)object, pGPObjectFactory2, keyStore, inputStream, string, string2, signatureCheck);
                    return stringArray;
                }
                String[] stringArray = this.parseSignedDataVersion3To((PGPSignatureList)object, pGPObjectFactory2, null, null, string, string2, signatureCheck);
                return stringArray;
            }
            throw new PGPException("Unknown message format: " + object);
        }
        finally {
            IOUtil.closeStream(bufferedInputStream);
        }
    }

    private String parseSignedData(PGPOnePassSignatureList pGPOnePassSignatureList, PGPObjectFactory pGPObjectFactory, KeyStore keyStore, InputStream inputStream, OutputStream outputStream, SignatureCheck signatureCheck) throws PGPException, IOException {
        this.Debug("Found signature");
        PGPOnePassSignature pGPOnePassSignature = null;
        PGPPublicKey pGPPublicKey = null;
        if (inputStream != null || keyStore != null) {
            for (int i = 0; i != pGPOnePassSignatureList.size(); ++i) {
                pGPOnePassSignature = pGPOnePassSignatureList.get(i);
                pGPPublicKey = inputStream != null ? this.readPublicVerificationKey(inputStream, pGPOnePassSignature.getKeyID()) : PGPLib.readPublicVerificationKey(keyStore, pGPOnePassSignature.getKeyID());
                if (pGPPublicKey != null) {
                    this.Debug("Message signed with Key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()));
                    break;
                }
                this.Debug("Message signed with Unknown Key Id {0}", KeyPairInformation.keyId2Hex(pGPOnePassSignature.getKeyID()));
            }
            if (pGPPublicKey != null) {
                this.bcFactory.initVerify(pGPOnePassSignature, pGPPublicKey);
            }
        }
        Object object = pGPObjectFactory.nextObject();
        String string = "";
        if (!(object instanceof PGPLiteralData)) {
            throw new PGPException("Unknown message format: " + object);
        }
        string = this.parseLiteralData((PGPLiteralData)object, (PGPOnePassSignature)(pGPPublicKey != null ? pGPOnePassSignature : null), outputStream);
        if (inputStream != null || keyStore != null) {
            if (pGPPublicKey == null) {
                this.Debug("The signature of the message does not correspond to the provided public key.");
                signatureCheck.result = SignatureCheckResult.PublicKeyNotMatching;
            } else {
                Object object2 = pGPObjectFactory.nextObject();
                if (object2 != null && pGPOnePassSignature != null) {
                    PGPSignatureList pGPSignatureList = (PGPSignatureList)object2;
                    try {
                        if (!pGPOnePassSignature.verify(pGPSignatureList.get(0))) {
                            this.Debug("The signature of the message did not passed verification.");
                            signatureCheck.result = SignatureCheckResult.SignatureBroken;
                        } else {
                            signatureCheck.result = SignatureCheckResult.SignatureVerified;
                        }
                    }
                    catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                        throw IOUtil.newPGPException(pGPException);
                    }
                }
            }
        }
        return string;
    }

    private String[] parseSignedDataTo(PGPOnePassSignatureList pGPOnePassSignatureList, PGPObjectFactory pGPObjectFactory, KeyStore keyStore, InputStream inputStream, String string, String string2, SignatureCheck signatureCheck) throws PGPException, IOException {
        this.Debug("Found signature");
        PGPOnePassSignature pGPOnePassSignature = null;
        PGPPublicKey pGPPublicKey = null;
        if (inputStream != null || keyStore != null) {
            for (int i = 0; i != pGPOnePassSignatureList.size(); ++i) {
                pGPOnePassSignature = pGPOnePassSignatureList.get(i);
                pGPPublicKey = inputStream != null ? this.readPublicVerificationKey(inputStream, pGPOnePassSignature.getKeyID()) : PGPLib.readPublicVerificationKey(keyStore, pGPOnePassSignature.getKeyID());
                if (pGPPublicKey != null) {
                    this.Debug("The message is signed with Key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()));
                    break;
                }
                this.Debug("The message is signed with Unknown Key Id {0}", KeyPairInformation.keyId2Hex(pGPOnePassSignature.getKeyID()));
            }
            if (pGPPublicKey != null) {
                this.bcFactory.initVerify(pGPOnePassSignature, pGPPublicKey);
            }
        }
        Object object = pGPObjectFactory.nextObject();
        String[] stringArray = new String[]{};
        if (!(object instanceof PGPLiteralData)) {
            throw new PGPException("Unknown message format: " + object);
        }
        stringArray = this.parseLiteralDataTo((PGPLiteralData)object, (PGPOnePassSignature)(pGPPublicKey != null ? pGPOnePassSignature : null), string, string2);
        if (inputStream != null || keyStore != null) {
            if (pGPPublicKey == null) {
                this.Debug("The signature of the message does not correspond to the provided public key.");
                signatureCheck.result = SignatureCheckResult.PublicKeyNotMatching;
            } else {
                Object object2 = pGPObjectFactory.nextObject();
                if (object2 != null && pGPOnePassSignature != null) {
                    PGPSignatureList pGPSignatureList = (PGPSignatureList)object2;
                    try {
                        if (!pGPOnePassSignature.verify(pGPSignatureList.get(0))) {
                            this.Debug("The signature of the message did not passed verification.");
                            signatureCheck.result = SignatureCheckResult.SignatureBroken;
                        } else {
                            this.Debug("The signature of the message passed verification.");
                            signatureCheck.result = SignatureCheckResult.SignatureVerified;
                        }
                    }
                    catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                        throw IOUtil.newPGPException(pGPException);
                    }
                }
            }
        }
        return stringArray;
    }

    private String parseSignedDataVersion3(PGPSignatureList pGPSignatureList, PGPObjectFactory pGPObjectFactory, KeyStore keyStore, InputStream inputStream, OutputStream outputStream, SignatureCheck signatureCheck) throws PGPException, IOException {
        this.Debug("Found signature version 3");
        PGPSignature pGPSignature = null;
        PGPPublicKey pGPPublicKey = null;
        if (inputStream != null || keyStore != null) {
            for (int i = 0; i < pGPSignatureList.size(); ++i) {
                pGPSignature = pGPSignatureList.get(i);
                if (pGPSignature.getSignatureType() != 0 && pGPSignature.getSignatureType() != 1 && pGPSignature.getSignatureType() != 16) continue;
                pGPPublicKey = inputStream != null ? this.readPublicVerificationKey(inputStream, pGPSignature.getKeyID()) : PGPLib.readPublicVerificationKey(keyStore, pGPSignature.getKeyID());
                if (pGPPublicKey != null) {
                    this.Debug("The message is signed with Key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()));
                    break;
                }
                this.Debug("The message is signed with Unknown Key Id {0}", KeyPairInformation.keyId2Hex(pGPSignature.getKeyID()));
            }
            if (pGPPublicKey != null) {
                this.bcFactory.initVerify(pGPSignature, pGPPublicKey);
            }
        }
        Object object = pGPObjectFactory.nextObject();
        String string = "";
        if (!(object instanceof PGPLiteralData)) {
            if (object == null) {
                throw new DetachedSignatureException("This is a detached signature file");
            }
            throw new PGPException("Unknown message format: " + object);
        }
        string = this.parseLiteralDataVersion3((PGPLiteralData)object, (PGPSignature)(pGPPublicKey != null ? pGPSignature : null), outputStream);
        if (inputStream != null || keyStore != null) {
            if (pGPPublicKey == null) {
                this.Debug("The signature of the message does not correspond to the provided public key.");
                signatureCheck.result = SignatureCheckResult.PublicKeyNotMatching;
            } else {
                try {
                    if (!pGPSignature.verify()) {
                        this.Debug("The signature of the message did not passed verification.");
                        signatureCheck.result = SignatureCheckResult.SignatureBroken;
                    } else {
                        signatureCheck.result = SignatureCheckResult.SignatureVerified;
                    }
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    throw IOUtil.newPGPException(pGPException);
                }
            }
        }
        return string;
    }

    private String[] parseSignedDataVersion3To(PGPSignatureList pGPSignatureList, PGPObjectFactory pGPObjectFactory, KeyStore keyStore, InputStream inputStream, String string, String string2, SignatureCheck signatureCheck) throws PGPException, IOException {
        PGPSignature pGPSignature = null;
        PGPPublicKey pGPPublicKey = null;
        if (inputStream != null || keyStore != null) {
            for (int i = 0; i < pGPSignatureList.size(); ++i) {
                pGPSignature = pGPSignatureList.get(i);
                if (pGPSignature.getSignatureType() != 0 && pGPSignature.getSignatureType() != 1 && pGPSignature.getSignatureType() != 16) continue;
                pGPPublicKey = inputStream != null ? this.readPublicVerificationKey(inputStream, pGPSignature.getKeyID()) : PGPLib.readPublicVerificationKey(keyStore, pGPSignature.getKeyID());
                if (pGPPublicKey != null) {
                    this.Debug("Message signed with Key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()));
                    break;
                }
                this.Debug("Message signed with Unknown Key Id {0}", KeyPairInformation.keyId2Hex(pGPSignature.getKeyID()));
            }
            if (pGPPublicKey != null) {
                this.bcFactory.initVerify(pGPSignature, pGPPublicKey);
            }
        }
        Object object = pGPObjectFactory.nextObject();
        String[] stringArray = new String[]{};
        if (!(object instanceof PGPLiteralData)) {
            if (object == null) {
                throw new DetachedSignatureException("This is a detached signature file");
            }
            throw new PGPException("Unknown message format: " + object);
        }
        stringArray = this.parseLiteralDataVersion3To((PGPLiteralData)object, (PGPSignature)(pGPPublicKey != null ? pGPSignature : null), string, string2);
        if (inputStream != null || keyStore != null) {
            if (pGPPublicKey == null) {
                this.Debug("The signature of the message does not correspond to the provided public key.");
                signatureCheck.result = SignatureCheckResult.PublicKeyNotMatching;
            }
            try {
                if (!pGPSignature.verify()) {
                    this.Debug("The signature of the message did not passed verification.");
                    signatureCheck.result = SignatureCheckResult.SignatureBroken;
                } else {
                    this.Debug("The signature of the message passed verification.");
                    signatureCheck.result = SignatureCheckResult.SignatureVerified;
                }
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        return stringArray;
    }

    private String parseEncryptedDataPBE(PGPEncryptedDataList pGPEncryptedDataList, boolean bl, SignatureCheck signatureCheck, String string, KeyStore keyStore, InputStream inputStream, OutputStream outputStream) throws IOException, WrongPasswordException, PGPException {
        Object object;
        Object object2;
        WrongPasswordException wrongPasswordException = null;
        InputStream inputStream2 = null;
        PGPPBEEncryptedData pGPPBEEncryptedData = null;
        if (pGPEncryptedDataList instanceof PGP2xPBEEncryptedData) {
            try {
                this.Debug("Password encrypted data packet found");
                inputStream2 = ((PGP2xPBEEncryptedData)pGPEncryptedDataList).getDataStream(string.toCharArray());
            }
            catch (PGPDataValidationException pGPDataValidationException) {
                throw new WrongPasswordException("The supplied password is incorrect.", pGPDataValidationException.getUnderlyingException());
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        } else {
            object2 = pGPEncryptedDataList.getEncryptedDataObjects();
            while (inputStream2 == null && object2.hasNext()) {
                object = object2.next();
                if (!(object instanceof PGPPBEEncryptedData)) continue;
                this.Debug("Password encrypted data packet found");
                pGPPBEEncryptedData = (PGPPBEEncryptedData)object;
                try {
                    inputStream2 = pGPPBEEncryptedData.getDataStream(this.bcFactory.CreatePBEDataDecryptorFactory(string));
                    break;
                }
                catch (PGPDataValidationException pGPDataValidationException) {
                    wrongPasswordException = new WrongPasswordException("The supplied password is incorrect.", pGPDataValidationException.getUnderlyingException());
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    throw IOUtil.newPGPException(pGPException);
                }
            }
        }
        if (inputStream2 == null && wrongPasswordException != null) {
            throw wrongPasswordException;
        }
        if (inputStream2 == null) {
            throw new FileIsEncryptedException("The file is encrypted with an OpenPGP key.");
        }
        object2 = "";
        object = new PGPObjectFactory2(inputStream2);
        Object object3 = ((PGPObjectFactory2)((Object)object)).nextObject();
        if (object3 instanceof PGPCompressedData) {
            object2 = this.parseCompressedData((PGPCompressedData)object3, bl, signatureCheck, keyStore, inputStream, outputStream);
        } else if (object3 instanceof PGPOnePassSignatureList) {
            object2 = bl ? this.parseSignedData((PGPOnePassSignatureList)object3, (PGPObjectFactory)object, keyStore, inputStream, outputStream, signatureCheck) : this.parseSignedData((PGPOnePassSignatureList)object3, (PGPObjectFactory)object, null, null, outputStream, signatureCheck);
        } else if (object3 instanceof PGPSignatureList) {
            object2 = bl ? this.parseSignedDataVersion3((PGPSignatureList)object3, (PGPObjectFactory)object, keyStore, inputStream, outputStream, signatureCheck) : this.parseSignedDataVersion3((PGPSignatureList)object3, (PGPObjectFactory)object, null, null, outputStream, signatureCheck);
        } else if (object3 instanceof PGPLiteralData) {
            object2 = this.parseLiteralData((PGPLiteralData)object3, null, outputStream);
        } else {
            throw new PGPException("Unknown message format: " + object3);
        }
        if (pGPPBEEncryptedData != null && pGPPBEEncryptedData.isIntegrityProtected()) {
            try {
                if (!pGPPBEEncryptedData.verify()) {
                    this.Debug("Integrity check failed!");
                    throw new IntegrityCheckException("The encrypted data is corrupted!");
                }
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                // empty catch block
            }
        }
        return object2;
    }

    private String parseEncryptedData(PGPEncryptedDataList pGPEncryptedDataList, boolean bl, SignatureCheck signatureCheck, KeyStore keyStore, InputStream inputStream, String string, InputStream inputStream2, OutputStream outputStream) throws IOException, PGPException, WrongPrivateKeyException, WrongPasswordException, FileIsPBEEncryptedException, IntegrityCheckException, DetachedSignatureException {
        Object object;
        PGPSecretKeyRingCollection pGPSecretKeyRingCollection = null;
        pGPSecretKeyRingCollection = inputStream != null ? this.createPGPSecretKeyRingCollection(inputStream) : keyStore.secCollection;
        WrongPasswordException wrongPasswordException = null;
        PGPPrivateKey pGPPrivateKey = null;
        PGPPublicKeyEncryptedData pGPPublicKeyEncryptedData = null;
        String[] stringArray = new String[pGPEncryptedDataList.size()];
        this.fill(stringArray, "Password encrypted");
        for (int i = 0; i < pGPEncryptedDataList.size(); ++i) {
            object = pGPEncryptedDataList.get(i);
            if (!(object instanceof PGPPublicKeyEncryptedData)) continue;
            pGPPublicKeyEncryptedData = (PGPPublicKeyEncryptedData)object;
            stringArray[i] = KeyPairInformation.keyId2Hex(pGPPublicKeyEncryptedData.getKeyID());
            this.Debug("Public key encrypted data packet found");
            this.Debug("Encrypted with key {0}", "0".equals(stringArray[i]) ? "wildcard" : stringArray[i]);
            try {
                pGPPrivateKey = this.getPrivateKey(pGPSecretKeyRingCollection, pGPPublicKeyEncryptedData.getKeyID(), string);
                if (pGPPrivateKey != null && keyStore != null) {
                    keyStore.triggetPrivateKeyNotFound(pGPPublicKeyEncryptedData.getKeyID());
                    pGPPrivateKey = this.getPrivateKey(pGPSecretKeyRingCollection, pGPPublicKeyEncryptedData.getKeyID(), string);
                }
            }
            catch (WrongPasswordException wrongPasswordException2) {
                pGPPrivateKey = null;
                wrongPasswordException = wrongPasswordException2;
            }
            if (pGPPrivateKey != null) break;
        }
        if (pGPPrivateKey == null && wrongPasswordException != null) {
            throw wrongPasswordException;
        }
        if (pGPPublicKeyEncryptedData == null) {
            this.Debug("This file is encrypted with a password.");
            throw new FileIsPBEEncryptedException("This file is encrypted with a password.");
        }
        if (pGPPrivateKey == null) {
            if (inputStream != null) {
                if (pGPSecretKeyRingCollection != null && pGPSecretKeyRingCollection.size() == 0) {
                    throw new WrongPrivateKeyException("Decryption of data encrypted using KEY-ID(s) : " + this.join(stringArray, ",") + " failed, The provided key is not a valid OpenPGP private key.");
                }
                String string2 = "";
                object = pGPSecretKeyRingCollection.getKeyRings();
                if (object.hasNext()) {
                    PGPSecretKeyRing pGPSecretKeyRing = (PGPSecretKeyRing)object.next();
                    string2 = KeyPairInformation.keyId2Hex(pGPSecretKeyRing.getSecretKey().getKeyID());
                }
                throw new WrongPrivateKeyException("Decryption of data encrypted using KEY-ID(s) : " + this.join(stringArray, ",") + " failed, using incorrect private KEY-ID :" + string2);
            }
            throw new WrongPrivateKeyException("Decryption of data encrypted using KEY-ID(s) : " + this.join(stringArray, ",") + " failed, no matching key was found in the KeyStore.");
        }
        InputStream inputStream3 = null;
        try {
            inputStream3 = pGPPublicKeyEncryptedData.getDataStream(this.bcFactory.CreatePublicKeyDataDecryptorFactory(pGPPrivateKey));
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        object = "";
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
        Object object2 = pGPObjectFactory2.nextObject();
        if (object2 instanceof PGPCompressedData) {
            object = this.parseCompressedData((PGPCompressedData)object2, bl, signatureCheck, keyStore, inputStream2, outputStream);
        } else if (object2 instanceof PGPOnePassSignatureList) {
            this.Debug("Signature found!");
            object = bl ? this.parseSignedData((PGPOnePassSignatureList)object2, pGPObjectFactory2, keyStore, inputStream2, outputStream, signatureCheck) : this.parseSignedData((PGPOnePassSignatureList)object2, pGPObjectFactory2, null, null, outputStream, signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            this.Debug("Signature found (version 3, old style)");
            object = bl ? this.parseSignedDataVersion3((PGPSignatureList)object2, pGPObjectFactory2, keyStore, inputStream2, outputStream, signatureCheck) : this.parseSignedDataVersion3((PGPSignatureList)object2, pGPObjectFactory2, null, null, outputStream, signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            object = this.parseLiteralData((PGPLiteralData)object2, null, outputStream);
        } else {
            throw new NonPGPDataException("Unknown message format: " + object2);
        }
        if (pGPPublicKeyEncryptedData.isIntegrityProtected()) {
            try {
                if (!pGPPublicKeyEncryptedData.verify()) {
                    this.Debug("Integrity check failed!");
                    throw new IntegrityCheckException("The encrypted data is corrupted!");
                }
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                // empty catch block
            }
        }
        return object;
    }

    private String[] parseEncryptedDataTo(PGPEncryptedDataList pGPEncryptedDataList, boolean bl, SignatureCheck signatureCheck, KeyStore keyStore, InputStream inputStream, String string, InputStream inputStream2, String string2, String string3) throws IOException, WrongPasswordException, WrongPrivateKeyException, PGPException {
        Object object;
        PGPPrivateKey pGPPrivateKey = null;
        PGPSecretKeyRingCollection pGPSecretKeyRingCollection = null;
        pGPSecretKeyRingCollection = inputStream != null ? this.createPGPSecretKeyRingCollection(inputStream) : keyStore.secCollection;
        WrongPasswordException wrongPasswordException = null;
        PGPPublicKeyEncryptedData pGPPublicKeyEncryptedData = null;
        String[] stringArray = new String[pGPEncryptedDataList.size()];
        this.fill(stringArray, "Password encrypted");
        for (int i = 0; i < pGPEncryptedDataList.size(); ++i) {
            object = pGPEncryptedDataList.get(i);
            if (!(object instanceof PGPPublicKeyEncryptedData)) continue;
            pGPPublicKeyEncryptedData = (PGPPublicKeyEncryptedData)object;
            stringArray[i] = KeyPairInformation.keyId2Hex(pGPPublicKeyEncryptedData.getKeyID());
            this.Debug("Encrypted with key ID {0}", "0".equals(stringArray[i]) ? "wildcard" : stringArray[i]);
            try {
                pGPPrivateKey = this.getPrivateKey(pGPSecretKeyRingCollection, pGPPublicKeyEncryptedData.getKeyID(), string);
                if (pGPPrivateKey != null && keyStore != null) {
                    keyStore.triggetPrivateKeyNotFound(pGPPublicKeyEncryptedData.getKeyID());
                    pGPPrivateKey = this.getPrivateKey(pGPSecretKeyRingCollection, pGPPublicKeyEncryptedData.getKeyID(), string);
                }
            }
            catch (WrongPasswordException wrongPasswordException2) {
                pGPPrivateKey = null;
                wrongPasswordException = wrongPasswordException2;
            }
            if (pGPPrivateKey != null) break;
        }
        if (pGPPrivateKey == null && wrongPasswordException != null) {
            throw wrongPasswordException;
        }
        if (pGPPublicKeyEncryptedData == null) {
            this.Debug("This file is encrypted with a password.");
            throw new FileIsPBEEncryptedException("This file is encrypted with a password.");
        }
        if (pGPPrivateKey == null) {
            if (inputStream != null) {
                if (pGPSecretKeyRingCollection != null && pGPSecretKeyRingCollection.size() == 0) {
                    throw new WrongPrivateKeyException("Decryption of data encrypted using KEY-ID(s) : " + this.join(stringArray, ",") + " failed, The provided key is not a valid OpenPGP private key.");
                }
                String string4 = "";
                object = pGPSecretKeyRingCollection.getKeyRings();
                if (object.hasNext()) {
                    PGPSecretKeyRing pGPSecretKeyRing = (PGPSecretKeyRing)object.next();
                    string4 = KeyPairInformation.keyId2Hex(pGPSecretKeyRing.getSecretKey().getKeyID());
                }
                throw new WrongPrivateKeyException("Decryption of data encrypted using KEY-ID(s) : " + this.join(stringArray, ",") + " failed, using incorrect private KEY-ID :" + string4);
            }
            throw new WrongPrivateKeyException("Decryption of data encrypted using KEY-ID(s) : " + this.join(stringArray, ",") + " failed, no matching key was found in the KeyStore.");
        }
        InputStream inputStream3 = null;
        try {
            inputStream3 = pGPPublicKeyEncryptedData.getDataStream(this.bcFactory.CreatePublicKeyDataDecryptorFactory(pGPPrivateKey));
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        object = new String[]{};
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
        Object object2 = pGPObjectFactory2.nextObject();
        if (object2 instanceof PGPCompressedData) {
            object = this.parseCompressedDataTo((PGPCompressedData)object2, bl, signatureCheck, keyStore, inputStream2, string2, string3);
        } else if (object2 instanceof PGPOnePassSignatureList) {
            object = bl ? this.parseSignedDataTo((PGPOnePassSignatureList)object2, pGPObjectFactory2, keyStore, inputStream2, string2, string3, signatureCheck) : this.parseSignedDataTo((PGPOnePassSignatureList)object2, pGPObjectFactory2, null, null, string2, string3, signatureCheck);
        } else if (object2 instanceof PGPSignatureList) {
            object = bl ? this.parseSignedDataVersion3To((PGPSignatureList)object2, pGPObjectFactory2, keyStore, inputStream2, string2, string3, signatureCheck) : this.parseSignedDataVersion3To((PGPSignatureList)object2, pGPObjectFactory2, null, null, string2, string3, signatureCheck);
        } else if (object2 instanceof PGPLiteralData) {
            object = this.parseLiteralDataTo((PGPLiteralData)object2, null, string2, string3);
        } else {
            throw new PGPException("Unknown message format: " + object2);
        }
        if (pGPPublicKeyEncryptedData.isIntegrityProtected()) {
            try {
                if (!pGPPublicKeyEncryptedData.verify()) {
                    this.Debug("Integrity check failed!");
                    throw new IntegrityCheckException("The encrypted data is corrupted!");
                }
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                // empty catch block
            }
        }
        return object;
    }

    private String getKeyUserId(PGPPublicKey pGPPublicKey) {
        Iterator iterator = pGPPublicKey.getRawUserIDs();
        if (iterator.hasNext()) {
            return BaseLib.toUserID((byte[])iterator.next());
        }
        return "";
    }

    private PGPPublicKey readPublicKeyForEncryption(InputStream inputStream) throws IOException, NoPublicKeyFoundException, PGPException {
        PGPPublicKeyRingCollection pGPPublicKeyRingCollection = this.createPGPPublicKeyRingCollection(inputStream);
        PGPException pGPException = null;
        PGPPublicKey pGPPublicKey = null;
        Iterator iterator = pGPPublicKeyRingCollection.getKeyRings();
        while (pGPPublicKey == null && iterator.hasNext()) {
            PGPPublicKeyRing pGPPublicKeyRing = (PGPPublicKeyRing)iterator.next();
            try {
                pGPPublicKey = this.readPublicKeyForEncryption(pGPPublicKeyRing);
            }
            catch (PGPException pGPException2) {
                pGPException = pGPException2;
            }
            if (pGPPublicKey == null) continue;
            break;
        }
        if (pGPPublicKey == null) {
            if (pGPException != null) {
                throw pGPException;
            }
            throw new NoPublicKeyFoundException("Can't find encryption key in key ring.");
        }
        return pGPPublicKey;
    }

    private PGPPublicKey readPublicKeyForEncryption(PGPPublicKeyRing pGPPublicKeyRing) throws IOException, NoPublicKeyFoundException, PGPException {
        PGPPublicKey pGPPublicKey;
        PGPPublicKey pGPPublicKey2 = null;
        PGPException pGPException = null;
        LinkedList<Integer> linkedList = new LinkedList();
        List<Integer> list = new LinkedList<Integer>();
        List<Integer> list2 = new LinkedList<Integer>();
        Iterator iterator = pGPPublicKeyRing.getPublicKeys();
        while (iterator.hasNext()) {
            pGPPublicKey = (PGPPublicKey)iterator.next();
            if (pGPPublicKey.isMasterKey()) {
                linkedList = this.listPreferredCompressions(pGPPublicKey);
                list = this.listPreferredCyphers(pGPPublicKey);
                list2 = this.preferredHashes(pGPPublicKey);
            }
            if (!pGPPublicKey.isEncryptionKey()) continue;
            if (pGPPublicKey.isRevoked() && !this.useRevokedKeys) {
                this.Debug("The key {0} is revoked", KeyPairInformation.keyIdToHex(pGPPublicKey.getKeyID()));
                pGPException = new KeyIsRevokedException("The key with Key Id:" + KeyPairInformation.keyIdToHex(pGPPublicKey.getKeyID()) + " [" + this.getKeyUserId(pGPPublicKey) + "] is revoked. See PGPLib.setUseRevokedKeys for more information.");
                continue;
            }
            if (this.isKeyExpired(pGPPublicKey) && !this.useExpiredKeys) {
                this.Debug("The key {0} is expired", KeyPairInformation.keyIdToHex(pGPPublicKey.getKeyID()));
                pGPException = new KeyIsExpiredException("The key with Key Id:" + KeyPairInformation.keyIdToHex(pGPPublicKey.getKeyID()) + " [" + this.getKeyUserId(pGPPublicKey) + "] has expired. See PGPLib.setUseExpiredKeys for more information.");
                continue;
            }
            Iterator iterator2 = pGPPublicKey.getSignatures();
            while (iterator2.hasNext()) {
                PGPSignature pGPSignature = (PGPSignature)iterator2.next();
                if (pGPSignature.getKeyID() != pGPPublicKeyRing.getPublicKey().getKeyID() || pGPSignature.getHashedSubPackets() == null || !pGPSignature.getHashedSubPackets().hasSubpacket(27) || (pGPSignature.getHashedSubPackets().getKeyFlags() & 4) != 4 && (pGPSignature.getHashedSubPackets().getKeyFlags() & 8) != 8) continue;
                pGPPublicKey2 = pGPPublicKey;
                break;
            }
            if (pGPPublicKey2 != null && pGPPublicKey.getBitStrength() <= pGPPublicKey2.getBitStrength() && (!pGPPublicKey2.isMasterKey() || pGPPublicKey.getBitStrength() != pGPPublicKey2.getBitStrength())) continue;
            pGPPublicKey2 = pGPPublicKey;
        }
        if (pGPPublicKey2 == null) {
            if (pGPException != null) {
                throw pGPException;
            }
            throw new NoPublicKeyFoundException("Can't find encryption key in key ring.");
        }
        try {
            pGPPublicKey = new PGPPublicKeyExt(pGPPublicKey2.getPublicKeyPacket());
            pGPPublicKey.setPreferredCyphers(list);
            pGPPublicKey.setPreferredCompressions(linkedList);
            pGPPublicKey.setPreferredHashes(list2);
            return pGPPublicKey;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException2) {
            throw IOUtil.newPGPException(pGPException2);
        }
    }

    private boolean isKeyExpired(PGPPublicKey pGPPublicKey) throws KeyIsExpiredException {
        if (pGPPublicKey == null) {
            return false;
        }
        if (this.useExpiredKeys) {
            return false;
        }
        if (pGPPublicKey.getValidDays() <= 0) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(pGPPublicKey.getCreationTime());
        calendar.add(5, pGPPublicKey.getValidDays());
        return calendar.getTime().before(new Date());
    }

    private void checkKeyIsExpired(PGPPublicKey pGPPublicKey) throws KeyIsExpiredException {
        if (this.isKeyExpired(pGPPublicKey)) {
            String string = "";
            Iterator iterator = pGPPublicKey.getRawUserIDs();
            if (iterator.hasNext()) {
                string = BaseLib.toUserID((byte[])iterator.next());
            }
            this.Debug("The key {0} is expired", KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()));
            throw new KeyIsExpiredException("The key with Id:" + KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()) + " [" + string + "] has expired. See PGPLib.setUseExpiredKeys for more information.");
        }
    }

    private boolean isKeyRevoked(PGPPublicKey pGPPublicKey) throws KeyIsRevokedException {
        if (pGPPublicKey == null) {
            return false;
        }
        if (this.useRevokedKeys) {
            return false;
        }
        return pGPPublicKey.hasRevocation();
    }

    private void checkKeyIsRevoked(PGPPublicKey pGPPublicKey) throws KeyIsRevokedException {
        if (this.isKeyRevoked(pGPPublicKey)) {
            String string = "";
            Iterator iterator = pGPPublicKey.getRawUserIDs();
            if (iterator.hasNext()) {
                string = BaseLib.toUserID((byte[])iterator.next());
            }
            this.Debug("The key {0} is revoked", KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID()));
            throw new KeyIsRevokedException("The key with Id:" + pGPPublicKey.getKeyID() + " [" + string + "] is revoked. See PGPLib.setUseRevokedKeys for more information.");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void writeFileToLiteralData(OutputStream outputStream, char c, InputStream inputStream, String string, long l, Date date) throws IOException {
        OutputStream outputStream2;
        block4: {
            PGPLiteralDataGenerator pGPLiteralDataGenerator = null;
            outputStream2 = null;
            try {
                int n;
                pGPLiteralDataGenerator = new PGPLiteralDataGenerator();
                outputStream2 = pGPLiteralDataGenerator.open(outputStream, c, string, l, date);
                byte[] byArray = new byte[0x100000];
                while ((n = inputStream.read(byArray)) > 0) {
                    outputStream2.write(byArray, 0, n);
                }
                if (pGPLiteralDataGenerator == null) break block4;
            }
            catch (Throwable throwable) {
                if (pGPLiteralDataGenerator != null) {
                    pGPLiteralDataGenerator.close();
                }
                IOUtil.closeStream(outputStream2);
                IOUtil.closeStream(inputStream);
                throw throwable;
            }
            pGPLiteralDataGenerator.close();
        }
        IOUtil.closeStream(outputStream2);
        IOUtil.closeStream(inputStream);
    }

    private PGPSecretKey readSecretSigningKey(InputStream inputStream) throws NoPrivateKeyFoundException, IOException, PGPException {
        PGPSecretKeyRingCollection pGPSecretKeyRingCollection = this.createPGPSecretKeyRingCollection(inputStream);
        PGPSecretKey pGPSecretKey = null;
        Iterator iterator = pGPSecretKeyRingCollection.getKeyRings();
        while (pGPSecretKey == null && iterator.hasNext()) {
            PGPSecretKeyRing pGPSecretKeyRing = (PGPSecretKeyRing)iterator.next();
            Iterator iterator2 = pGPSecretKeyRing.getSecretKeys();
            while (pGPSecretKey == null && iterator2.hasNext()) {
                PGPSecretKey pGPSecretKey2 = (PGPSecretKey)iterator2.next();
                if (!pGPSecretKey2.isSigningKey() || pGPSecretKey2.isPrivateKeyEmpty()) continue;
                pGPSecretKey = pGPSecretKey2;
            }
        }
        if (pGPSecretKey == null) {
            throw new NoPrivateKeyFoundException("Can't find signing key in key ring.");
        }
        this.checkKeyIsExpired(pGPSecretKey.getPublicKey());
        this.checkKeyIsRevoked(pGPSecretKey.getPublicKey());
        return pGPSecretKey;
    }

    private PGPSecretKey readSecretSigningKey(KeyStore keyStore, long l) throws NoPrivateKeyFoundException, PGPException {
        KeyPairInformation keyPairInformation = keyStore.getKey(l);
        if (keyPairInformation == null || !keyPairInformation.hasPrivateKey()) {
            throw new NoPrivateKeyFoundException("No private key pair was found with Key Id: " + KeyPairInformation.keyId2Hex(l));
        }
        PGPSecretKeyRing pGPSecretKeyRing = keyPairInformation.getRawPrivateKeyRing();
        Iterator iterator = pGPSecretKeyRing.getSecretKeys();
        PGPSecretKey pGPSecretKey = null;
        PGPSecretKey pGPSecretKey2 = null;
        while (pGPSecretKey == null && iterator.hasNext()) {
            PGPSecretKey pGPSecretKey3 = (PGPSecretKey)iterator.next();
            if (!pGPSecretKey3.isSigningKey() || pGPSecretKey3.isPrivateKeyEmpty()) continue;
            pGPSecretKey2 = pGPSecretKey3;
            try {
                PGPPublicKey pGPPublicKey = keyStore.pubCollection.getPublicKey(pGPSecretKey3.getKeyID());
                if (!PGPLib.hasKeyFlags(pGPPublicKey, 2) || this.isKeyExpired(pGPPublicKey) || this.isKeyRevoked(pGPPublicKey)) continue;
                pGPSecretKey = pGPSecretKey3;
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        if (pGPSecretKey == null && pGPSecretKey2 == null) {
            throw new NoPrivateKeyFoundException("Can't find signing key in key ring.");
        }
        pGPSecretKey = pGPSecretKey2;
        this.checkKeyIsExpired(pGPSecretKey.getPublicKey());
        this.checkKeyIsRevoked(pGPSecretKey.getPublicKey());
        return pGPSecretKey;
    }

    private void internalSignStream(InputStream inputStream, String string, PGPSecretKey pGPSecretKey, String string2, OutputStream outputStream, boolean bl) throws PGPException, WrongPasswordException, IOException {
        Object object;
        if (!(outputStream instanceof BufferedOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream, 0x100000);
        }
        if (pGPSecretKey.getPublicKey() != null && pGPSecretKey.getPublicKey().getVersion() == 3) {
            this.Debug("Switching to version 3 signatures");
            this.signStreamVersion3(inputStream, string, new ByteArrayInputStream(pGPSecretKey.getEncoded()), string2, outputStream, bl);
        }
        OutputStream outputStream2 = null;
        if (bl) {
            this.Debug("Output is ASCII armored");
            outputStream2 = outputStream;
            outputStream = new ArmoredOutputStream(outputStream2);
            this.setAsciiVersionHeader(outputStream);
        }
        PGPPrivateKey pGPPrivateKey = PGPLib.extractPrivateKey(pGPSecretKey, string2);
        int n = this.selectPreferredHash();
        this.Debug("Signature with hash algorithm {0}", KeyStore.hashToString(n));
        PGPSignatureGenerator pGPSignatureGenerator = this.bcFactory.CreatePGPSignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), n);
        this.bcFactory.initSign(pGPSignatureGenerator, 0, pGPPrivateKey);
        Iterator iterator = pGPSecretKey.getPublicKey().getRawUserIDs();
        if (iterator.hasNext()) {
            PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
            object = (byte[])iterator.next();
            this.Debug("Signing for user Id {0}", BaseLib.toUserID(object));
            pGPSignatureSubpacketGenerator.setSignerUserID(false, object);
            pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketGenerator.generate());
        }
        int n2 = this.selectPreferredCompression(pGPSecretKey.getPublicKey());
        object = new PGPCompressedDataGenerator(n2);
        BCPGOutputStream bCPGOutputStream = null;
        if (n2 == 0) {
            this.Debug("No Compression");
            bCPGOutputStream = new BCPGOutputStream(outputStream);
        } else {
            this.Debug("Compression algorithm is {0}", KeyStore.compressionToString(n2));
            bCPGOutputStream = new BCPGOutputStream(object.open(outputStream));
        }
        try {
            pGPSignatureGenerator.generateOnePassVersion(false).encode((OutputStream)bCPGOutputStream);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            IOUtil.closeStream((OutputStream)bCPGOutputStream);
            throw IOUtil.newPGPException(pGPException);
        }
        DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
        int n3 = 0;
        byte[] byArray = new byte[0x100000];
        while ((n3 = inputStream.read(byArray)) > 0) {
            directByteArrayOutputStream.write(byArray, 0, n3);
            pGPSignatureGenerator.update(byArray, 0, n3);
        }
        PGPLiteralDataGenerator pGPLiteralDataGenerator = new PGPLiteralDataGenerator();
        OutputStream outputStream3 = pGPLiteralDataGenerator.open((OutputStream)bCPGOutputStream, this.getContentType(), string, (long)directByteArrayOutputStream.size(), new Date());
        outputStream3.write(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size());
        try {
            pGPSignatureGenerator.generate().encode((OutputStream)bCPGOutputStream);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        pGPLiteralDataGenerator.close();
        object.close();
        IOUtil.closeStream(outputStream3);
        IOUtil.closeStream((OutputStream)bCPGOutputStream);
        IOUtil.closeStream(directByteArrayOutputStream);
        outputStream.flush();
        if (bl) {
            IOUtil.closeStream(outputStream);
        }
    }

    private void internalSignAndEncryptStreamPgp2x(InputStream inputStream, String string, InputStream inputStream2, String string2, InputStream[] inputStreamArray, OutputStream outputStream, Date date, boolean bl) throws PGPException, IOException {
        Object object;
        Object object2;
        int n;
        OutputStream outputStream2 = null;
        if (bl) {
            this.Debug("Output is ASCII armored");
            outputStream2 = outputStream;
            outputStream = new ArmoredOutputStream(outputStream2);
            this.setAsciiVersionHeader(outputStream);
        }
        DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
        BaseLib.pipeAll(inputStream, directByteArrayOutputStream);
        boolean bl2 = true;
        PGPEncryptedDataGenerator pGPEncryptedDataGenerator = this.bcFactory.CreatePGPEncryptedDataGenerator(1, false, IOUtil.getSecureRandom());
        PGPPublicKey[] pGPPublicKeyArray = new PGPPublicKey[inputStreamArray.length];
        for (n = 0; n < inputStreamArray.length; ++n) {
            pGPPublicKeyArray[n] = this.readPublicKeyForEncryption(inputStreamArray[n]);
            pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(pGPPublicKeyArray[n]));
            this.Debug("Ecrypting for key Id {0}", String.valueOf(pGPPublicKeyArray[n].getKeyID()));
        }
        for (n = 0; n < this.getMasterKeysCount(); ++n) {
            object2 = (KeyPairInformation)this.masterKeysList.get(n);
            object = this.readPublicKeyForEncryption(((KeyPairInformation)object2).getPublicKeyRing());
            pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)this.bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator((PGPPublicKey)object));
            this.Debug("Encrypting with master key Id {0}", KeyPairInformation.keyId2Hex(object.getKeyID()));
        }
        PGPLiteralDataGenerator pGPLiteralDataGenerator = new PGPLiteralDataGenerator(bl2);
        object2 = new DirectByteArrayOutputStream(0x100000);
        object = pGPLiteralDataGenerator.open((OutputStream)object2, this.contentType, string, (long)directByteArrayOutputStream.size(), new Date());
        directByteArrayOutputStream.writeTo((OutputStream)object);
        DirectByteArrayOutputStream directByteArrayOutputStream2 = new DirectByteArrayOutputStream(0x100000);
        this.detachedSignStream(new ByteArrayInputStream(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size()), inputStream2, string2, directByteArrayOutputStream2, false);
        byte[] byArray = directByteArrayOutputStream2.toByteArray();
        directByteArrayOutputStream2 = new DirectByteArrayOutputStream(0x100000);
        directByteArrayOutputStream2.write((byte)(byArray[0] | 1));
        directByteArrayOutputStream2.write(0);
        directByteArrayOutputStream2.write(byArray[1]);
        directByteArrayOutputStream2.write(byArray, 2, byArray.length - 2);
        try {
            OutputStream outputStream3 = pGPEncryptedDataGenerator.open(outputStream, (long)(directByteArrayOutputStream2.size() + ((ByteArrayOutputStream)object2).size()));
            directByteArrayOutputStream2.writeTo(outputStream3);
            ((ByteArrayOutputStream)object2).writeTo(outputStream3);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        outputStream.flush();
        if (bl) {
            IOUtil.closeStream(outputStream);
        }
    }

    private InputStream createPublicRingStream(KeyStore keyStore, String string) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(string);
        try {
            return new ByteArrayInputStream(pGPPublicKeyRing.getEncoded());
        }
        catch (IOException iOException) {
            throw new PGPException(iOException.getMessage(), iOException);
        }
    }

    private InputStream createPublicRingStream(KeyStore keyStore, long l) throws IOException, NoPublicKeyFoundException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(l);
        return new ByteArrayInputStream(pGPPublicKeyRing.getEncoded());
    }

    private InputStream createPrivateRingStream(KeyStore keyStore, String string) throws IOException, PGPException {
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(string);
        return new ByteArrayInputStream(pGPSecretKeyRing.getEncoded());
    }

    private InputStream createPrivateRingStream(KeyStore keyStore, long l) throws IOException, PGPException {
        PGPSecretKey pGPSecretKey = null;
        try {
            pGPSecretKey = keyStore.secCollection.getSecretKey(l);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        if (pGPSecretKey != null) {
            return new ByteArrayInputStream(pGPSecretKey.getEncoded());
        }
        throw new NoPrivateKeyFoundException("No private key was found with KeyId : " + l);
    }

    private List<Integer> listPreferredCyphers(PGPPublicKey pGPPublicKey) {
        if (pGPPublicKey instanceof PGPPublicKeyExt) {
            return ((PGPPublicKeyExt)pGPPublicKey).getPreferredCyphers();
        }
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        Iterator iterator = pGPPublicKey.getSignatures();
        PGPSignature pGPSignature = (PGPSignature)iterator.next();
        if (pGPSignature.getHashedSubPackets() != null && pGPSignature.getHashedSubPackets().getPreferredSymmetricAlgorithms() != null) {
            int[] nArray = pGPSignature.getHashedSubPackets().getPreferredSymmetricAlgorithms();
            for (int i = 0; i < nArray.length; ++i) {
                linkedList.add(nArray[i]);
            }
        }
        return linkedList;
    }

    private List<Integer> listPreferredHashes(PGPPublicKey pGPPublicKey) {
        if (pGPPublicKey instanceof PGPPublicKeyExt) {
            return ((PGPPublicKeyExt)pGPPublicKey).getPreferredHashes();
        }
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        Iterator iterator = pGPPublicKey.getSignatures();
        PGPSignature pGPSignature = (PGPSignature)iterator.next();
        if (pGPSignature.getHashedSubPackets() != null && pGPSignature.getHashedSubPackets().getPreferredHashAlgorithms() != null) {
            int[] nArray = pGPSignature.getHashedSubPackets().getPreferredHashAlgorithms();
            for (int i = 0; i < nArray.length; ++i) {
                linkedList.add(nArray[i]);
            }
        }
        return linkedList;
    }

    private int selectPreferredCypher(PGPPublicKey pGPPublicKey) {
        List<Integer> list = this.listPreferredCyphers(pGPPublicKey);
        int n = this.selectPreferredCypher(list);
        if (pGPPublicKey.getAlgorithm() == 18 && n != 8 && n != 9) {
            n = 9;
        }
        return n;
    }

    private int selectPreferredCypher(List<Integer> list) {
        int n = 0;
        if (this.overrideKeyAlgorithmPreferences) {
            for (int i = 0; i < this.cypher.size(); ++i) {
                Integer n2 = this.cypher.get(i);
                if (!list.contains(n2)) continue;
                n = n2;
                break;
            }
            if (n == 0 && this.cypher.size() > 0) {
                return this.cypher.get(0);
            }
        } else {
            for (int i = 0; n == 0 && i < list.size(); ++i) {
                Integer n3 = list.get(i);
                if (!this.cypher.contains(n3)) continue;
                n = n3;
                break;
            }
            if (n == 0 && list.size() > 0) {
                n = list.get(0);
            }
        }
        if (n == 0) {
            if (this.cypher.size() > 0) {
                return this.cypher.get(0);
            }
            n = 9;
        }
        this.Debug("Cypher: {0}", KeyStore.cypherToString(n));
        return n;
    }

    private List<Integer> preferredHashes(PGPPublicKey pGPPublicKey) {
        if (pGPPublicKey instanceof PGPPublicKeyExt) {
            return ((PGPPublicKeyExt)pGPPublicKey).getPreferredHashes();
        }
        Iterator iterator = pGPPublicKey.getSignatures();
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        while (iterator.hasNext()) {
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getHashedSubPackets() == null || pGPSignature.getHashedSubPackets().getPreferredHashAlgorithms() == null) continue;
            int[] nArray = pGPSignature.getHashedSubPackets().getPreferredHashAlgorithms();
            for (int i = 0; i < nArray.length; ++i) {
                linkedList.add(nArray[i]);
            }
        }
        return linkedList;
    }

    private int selectPreferredHash() {
        if (this.hash.size() > 0) {
            return this.hash.get(0);
        }
        return 8;
    }

    private int selectPreferredHash(PGPPublicKey pGPPublicKey) {
        List<Integer> list = this.listPreferredHashes(pGPPublicKey);
        return this.selectPreferredHash(list);
    }

    private int selectPreferredHash(List<Integer> list) {
        int n = -1;
        if (this.overrideKeyAlgorithmPreferences) {
            for (int i = 0; i < this.hash.size(); ++i) {
                Integer n2 = this.hash.get(i);
                if (!list.contains(n2)) continue;
                n = n2;
                break;
            }
            if (n == -1 && this.hash.size() > 0) {
                return this.hash.get(0);
            }
        } else {
            for (int i = 0; n == -1 && i < list.size(); ++i) {
                Integer n3 = list.get(i);
                if (!this.hash.contains(n3)) continue;
                n = n3;
                break;
            }
            if (n == -1 && list.size() > 0) {
                return list.get(0);
            }
        }
        if (n == -1) {
            n = 8;
        }
        this.Debug("Hash: {0}", KeyStore.hashToString(n));
        return n;
    }

    private List<Integer> intersectList(List<Integer> list, List<Integer> list2) {
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        if (list != null && list2 != null) {
            for (int i = 0; i < list.size(); ++i) {
                if (!list2.contains(list.get(i))) continue;
                linkedList.add(list.get(i));
            }
        }
        return linkedList;
    }

    private int selectPreferredCompression(PGPPublicKey pGPPublicKey) {
        List<Integer> list = this.listPreferredCompressions(pGPPublicKey);
        return this.selectPreferredCompression(list);
    }

    private int selectPreferredCompression(List<Integer> list) {
        int n = -1;
        if (this.overrideKeyAlgorithmPreferences) {
            for (int i = 0; i < this.compression.size(); ++i) {
                Integer n2 = this.compression.get(i);
                if (!list.contains(n2)) continue;
                n = n2;
                break;
            }
            if (n == -1 && this.compression.size() > 0) {
                return this.compression.get(0);
            }
        } else {
            for (int i = 0; n == -1 && i < list.size(); ++i) {
                Integer n3 = list.get(i);
                if (!this.compression.contains(n3)) continue;
                n = n3;
                break;
            }
            if (n == -1 && list.size() > 0) {
                return list.get(0);
            }
        }
        if (n == -1) {
            n = 0;
        }
        this.Debug("Compression: {0}", KeyStore.compressionToString(n));
        return n;
    }

    private List<Integer> listPreferredCompressions(PGPPublicKey pGPPublicKey) {
        if (pGPPublicKey instanceof PGPPublicKeyExt) {
            return ((PGPPublicKeyExt)pGPPublicKey).getPreferredCompressions();
        }
        Iterator iterator = pGPPublicKey.getSignatures();
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        while (iterator.hasNext()) {
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getHashedSubPackets() == null || pGPSignature.getHashedSubPackets().getPreferredCompressionAlgorithms() == null) continue;
            int[] nArray = pGPSignature.getHashedSubPackets().getPreferredCompressionAlgorithms();
            for (int i = 0; i < nArray.length; ++i) {
                linkedList.add(nArray[i]);
            }
        }
        return linkedList;
    }

    private void writeMarker(BCPGOutputStream bCPGOutputStream) {
        Class<BCPGOutputStream> clazz = BCPGOutputStream.class;
        byte[] byArray = new byte[]{80, 71, 80};
        Method method = null;
        Method[] methodArray = clazz.getDeclaredMethods();
        for (int i = 0; i < methodArray.length; ++i) {
            Class<?>[] classArray;
            if (!methodArray[i].getName().endsWith("writePacket") || (classArray = methodArray[i].getParameterTypes()).length != 3) continue;
            method = methodArray[i];
            method.setAccessible(true);
            try {
                method.invoke(bCPGOutputStream, new Integer(10), byArray, new Boolean(true));
            }
            catch (InvocationTargetException invocationTargetException) {
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
            return;
        }
        if (method == null) {
            throw new Error("No such method: writeMarkerPacket");
        }
    }

    private File createTarFile(String[] stringArray) throws PGPException {
        File file = null;
        try {
            file = File.createTempFile("tmpTarBCPG", ".tar");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            TarOutputStream tarOutputStream = new TarOutputStream(fileOutputStream);
            for (int i = 0; i < stringArray.length; ++i) {
                tarOutputStream.writeFileEntry(new TarEntry(new File(stringArray[i]), ""));
            }
            tarOutputStream.close();
        }
        catch (IOException iOException) {
            throw new PGPException(iOException.getMessage(), iOException);
        }
        return file;
    }

    protected void Debug(String string) {
        if (log.isLoggable(this.debugLevel)) {
            log.log(this.debugLevel, string);
        }
    }

    protected void Debug(String string, String string2) {
        if (log.isLoggable(this.debugLevel)) {
            log.log(this.debugLevel, MessageFormat.format(string, string2));
        }
    }

    private void fill(String[] stringArray, String string) {
        for (int i = 0; i < stringArray.length; ++i) {
            stringArray[i] = string;
        }
    }

    private String join(String[] stringArray, String string) {
        int n = stringArray.length;
        if (n == 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(stringArray[0]);
        for (int i = 1; i < n; ++i) {
            stringBuffer.append(string).append(stringArray[i]);
        }
        return stringBuffer.toString();
    }

    public boolean isTrialVersion() {
        return false;
    }

    public boolean isPgp2Compatible() {
        return this.pgp2Compatible;
    }

    public void setPgp2Compatible(boolean bl) {
        this.pgp2Compatible = bl;
    }

    public boolean isExtractTarFiles() {
        return this.extractEmbeddedTar;
    }

    public void setExtractTarFiles(boolean bl) {
        this.extractEmbeddedTar = bl;
    }

    public boolean isIntegrityProtectArchives() {
        return this.integrityProtect;
    }

    public void setIntegrityProtectArchives(boolean bl) {
        this.integrityProtect = bl;
    }

    public boolean isOverrideKeyAlgorithmPreferences() {
        return this.overrideKeyAlgorithmPreferences;
    }

    public void setOverrideKeyAlgorithmPreferences(boolean bl) {
        this.overrideKeyAlgorithmPreferences = bl;
    }

    private class SignatureCheck {
        public SignatureCheckResult result = SignatureCheckResult.NoSignatureFound;

        private SignatureCheck() {
        }
    }

    class ClearSignedHelper {
        public boolean verify(ArmoredInputStream armoredInputStream, KeyStore keyStore, InputStream inputStream, OutputStream outputStream) throws PGPException, IOException, SignatureException {
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int n = this.readInputLine(byteArrayOutputStream, (InputStream)armoredInputStream);
            byte[] byArray = this.getLineSeparator();
            byte[] byArray2 = byteArrayOutputStream.toByteArray();
            directByteArrayOutputStream.write(byArray2, 0, this.getLengthWithoutSeparator(byArray2));
            directByteArrayOutputStream.write(byArray);
            if (n != -1 && armoredInputStream.isClearText()) {
                while (n != -1 && armoredInputStream.isClearText()) {
                    n = this.readInputLine(byteArrayOutputStream, n, (InputStream)armoredInputStream);
                    byArray2 = byteArrayOutputStream.toByteArray();
                    directByteArrayOutputStream.write(byArray2, 0, this.getLengthWithoutSeparator(byArray2));
                    directByteArrayOutputStream.write(byArray);
                }
            }
            directByteArrayOutputStream.close();
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2((InputStream)armoredInputStream);
            PGPSignatureList pGPSignatureList = (PGPSignatureList)pGPObjectFactory2.nextObject();
            PGPSignature pGPSignature = null;
            PGPPublicKey pGPPublicKey = null;
            for (int i = 0; i != pGPSignatureList.size(); ++i) {
                pGPSignature = pGPSignatureList.get(i);
                pGPPublicKey = inputStream != null ? PGPLib.this.readPublicVerificationKey(inputStream, pGPSignature.getKeyID()) : PGPLib.readPublicVerificationKey(keyStore, pGPSignature.getKeyID());
                if (pGPPublicKey != null) {
                    PGPLib.this.Debug("Signed with key {0}", KeyPairInformation.keyId2Hex(pGPSignature.getKeyID()));
                    break;
                }
                PGPLib.this.Debug("Signed with unknown key {0}", KeyPairInformation.keyId2Hex(pGPSignature.getKeyID()));
            }
            if (pGPPublicKey == null) {
                throw new NoPublicKeyFoundException("No public key could be found for signature.");
            }
            PGPLib.this.bcFactory.initVerify(pGPSignature, pGPPublicKey);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size());
            n = this.readInputLine(byteArrayOutputStream, byteArrayInputStream);
            this.processLine(pGPSignature, byteArrayOutputStream.toByteArray());
            if (n != -1) {
                do {
                    n = this.readInputLine(byteArrayOutputStream, n, byteArrayInputStream);
                    pGPSignature.update((byte)13);
                    pGPSignature.update((byte)10);
                    this.processLine(pGPSignature, byteArrayOutputStream.toByteArray());
                } while (n != -1);
            }
            boolean bl = false;
            try {
                if (pGPSignature.verify()) {
                    bl = true;
                }
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
            ByteArrayInputStream byteArrayInputStream2 = new ByteArrayInputStream(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size());
            int n2 = 0;
            byte[] byArray3 = new byte[0x100000];
            while ((n2 = byteArrayInputStream2.read(byArray3)) > 0) {
                outputStream.write(byArray3, 0, n2);
            }
            return bl;
        }

        public SignatureCheckResult verify2(ArmoredInputStream armoredInputStream, KeyStore keyStore, InputStream inputStream, OutputStream outputStream) throws PGPException, IOException {
            SignatureCheckResult signatureCheckResult = SignatureCheckResult.NoSignatureFound;
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int n = this.readInputLine(byteArrayOutputStream, (InputStream)armoredInputStream);
            byte[] byArray = this.getLineSeparator();
            byte[] byArray2 = byteArrayOutputStream.toByteArray();
            directByteArrayOutputStream.write(byArray2, 0, this.getLengthWithoutSeparator(byArray2));
            directByteArrayOutputStream.write(byArray);
            if (n != -1 && armoredInputStream.isClearText()) {
                while (n != -1 && armoredInputStream.isClearText()) {
                    n = this.readInputLine(byteArrayOutputStream, n, (InputStream)armoredInputStream);
                    byArray2 = byteArrayOutputStream.toByteArray();
                    directByteArrayOutputStream.write(byArray2, 0, this.getLengthWithoutSeparator(byArray2));
                    directByteArrayOutputStream.write(byArray);
                }
            }
            directByteArrayOutputStream.close();
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2((InputStream)armoredInputStream);
            PGPSignatureList pGPSignatureList = (PGPSignatureList)pGPObjectFactory2.nextObject();
            PGPSignature pGPSignature = null;
            PGPPublicKey pGPPublicKey = null;
            for (int i = 0; i != pGPSignatureList.size(); ++i) {
                pGPSignature = pGPSignatureList.get(i);
                pGPPublicKey = inputStream != null ? PGPLib.this.readPublicVerificationKey(inputStream, pGPSignature.getKeyID()) : PGPLib.readPublicVerificationKey(keyStore, pGPSignature.getKeyID());
                if (pGPPublicKey != null) {
                    PGPLib.this.Debug("Signed with key {0}", KeyPairInformation.keyId2Hex(pGPSignature.getKeyID()));
                    break;
                }
                PGPLib.this.Debug("Signed with unknown key {0}", KeyPairInformation.keyId2Hex(pGPSignature.getKeyID()));
            }
            if (pGPPublicKey == null) {
                signatureCheckResult = SignatureCheckResult.PublicKeyNotMatching;
            } else {
                PGPLib.this.bcFactory.initVerify(pGPSignature, pGPPublicKey);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size());
                n = this.readInputLine(byteArrayOutputStream, byteArrayInputStream);
                this.processLine(pGPSignature, byteArrayOutputStream.toByteArray());
                if (n != -1) {
                    do {
                        n = this.readInputLine(byteArrayOutputStream, n, byteArrayInputStream);
                        pGPSignature.update((byte)13);
                        pGPSignature.update((byte)10);
                        this.processLine(pGPSignature, byteArrayOutputStream.toByteArray());
                    } while (n != -1);
                }
                try {
                    signatureCheckResult = pGPSignature.verify() ? SignatureCheckResult.SignatureVerified : SignatureCheckResult.SignatureBroken;
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    throw IOUtil.newPGPException(pGPException);
                }
            }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size());
            int n2 = 0;
            byte[] byArray3 = new byte[0x100000];
            while ((n2 = byteArrayInputStream.read(byArray3)) > 0) {
                outputStream.write(byArray3, 0, n2);
            }
            return signatureCheckResult;
        }

        public String sign(String string, PGPSecretKey pGPSecretKey, String string2, int n) throws IOException, lw.bouncycastle.openpgp.PGPException {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(string.getBytes("UTF-8"));
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            this.sign(byteArrayInputStream, pGPSecretKey, string2, n, directByteArrayOutputStream);
            return new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size());
        }

        public String signV3(String string, PGPSecretKey pGPSecretKey, String string2, int n) throws IOException, lw.bouncycastle.openpgp.PGPException {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(string.getBytes("UTF-8"));
            DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(0x100000);
            this.signV3(byteArrayInputStream, pGPSecretKey, string2, n, directByteArrayOutputStream);
            return new String(directByteArrayOutputStream.getArray());
        }

        public void sign(InputStream inputStream, PGPSecretKey pGPSecretKey, String string, int n, OutputStream outputStream) throws IOException, lw.bouncycastle.openpgp.PGPException, WrongPasswordException {
            Object object;
            PGPLib.this.Debug("Clear text signing with hash {0}", KeyStore.hashToString(n));
            PGPPrivateKey pGPPrivateKey = BaseLib.extractPrivateKey(pGPSecretKey, string);
            PGPSignatureGenerator pGPSignatureGenerator = PGPLib.this.bcFactory.CreatePGPSignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), n);
            PGPLib.this.bcFactory.initSign(pGPSignatureGenerator, 1, pGPPrivateKey);
            PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
            Iterator iterator = pGPSecretKey.getPublicKey().getRawUserIDs();
            if (iterator.hasNext()) {
                object = (byte[])iterator.next();
                PGPLib.this.Debug("Signing for User Id {0}", BaseLib.toUserID(object));
                pGPSignatureSubpacketGenerator.setSignerUserID(false, object);
                pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketGenerator.generate());
            }
            object = new ArmoredOutputStream(outputStream);
            PGPLib.this.setAsciiVersionHeader((OutputStream)object);
            object.beginClearText(n);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int n2 = this.readInputLine(byteArrayOutputStream, inputStream);
            int n3 = 1;
            this.processLine((OutputStream)object, pGPSignatureGenerator, byteArrayOutputStream.toByteArray());
            if (n2 != -1) {
                do {
                    ++n3;
                    n2 = this.readInputLine(byteArrayOutputStream, n2, inputStream);
                    pGPSignatureGenerator.update((byte)13);
                    pGPSignatureGenerator.update((byte)10);
                    this.processLine((OutputStream)object, pGPSignatureGenerator, byteArrayOutputStream.toByteArray());
                } while (n2 != -1);
            }
            if (n3 >= 1 && !this.isLineEnding(byteArrayOutputStream.toByteArray()[byteArrayOutputStream.toByteArray().length - 1])) {
                object.write(13);
                object.write(10);
            }
            object.endClearText();
            BCPGOutputStream bCPGOutputStream = new BCPGOutputStream((OutputStream)object);
            try {
                pGPSignatureGenerator.generate().encode((OutputStream)bCPGOutputStream);
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
            finally {
                object.close();
            }
        }

        public void signV3(InputStream inputStream, PGPSecretKey pGPSecretKey, String string, int n, OutputStream outputStream) throws IOException, lw.bouncycastle.openpgp.PGPException {
            Object object;
            PGPPrivateKey pGPPrivateKey = BaseLib.extractPrivateKey(pGPSecretKey, string);
            PGPV3SignatureGenerator pGPV3SignatureGenerator = PGPLib.this.bcFactory.CreatePGPV3SignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), n);
            PGPLib.this.bcFactory.initSign(pGPV3SignatureGenerator, 1, pGPPrivateKey);
            PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
            Iterator iterator = pGPSecretKey.getPublicKey().getRawUserIDs();
            if (iterator.hasNext()) {
                object = (byte[])iterator.next();
                PGPLib.this.Debug("Signing for User Id {0}", BaseLib.toUserID(object));
                pGPSignatureSubpacketGenerator.setSignerUserID(false, object);
            }
            object = new ArmoredOutputStream(outputStream);
            PGPLib.this.setAsciiVersionHeader((OutputStream)object);
            object.beginClearText(n);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int n2 = this.readInputLine(byteArrayOutputStream, inputStream);
            this.processLineV3((OutputStream)object, pGPV3SignatureGenerator, byteArrayOutputStream.toByteArray());
            if (n2 != -1) {
                do {
                    n2 = this.readInputLine(byteArrayOutputStream, n2, inputStream);
                    pGPV3SignatureGenerator.update((byte)13);
                    pGPV3SignatureGenerator.update((byte)10);
                    this.processLineV3((OutputStream)object, pGPV3SignatureGenerator, byteArrayOutputStream.toByteArray());
                } while (n2 != -1);
            }
            object.endClearText();
            BCPGOutputStream bCPGOutputStream = new BCPGOutputStream((OutputStream)object);
            try {
                pGPV3SignatureGenerator.generate().encode((OutputStream)bCPGOutputStream);
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
            object.close();
        }

        private byte[] getLineSeparator() {
            String string = System.getProperty("line.separator");
            byte[] byArray = new byte[string.length()];
            for (int i = 0; i != byArray.length; ++i) {
                byArray[i] = (byte)string.charAt(i);
            }
            return byArray;
        }

        private int getLengthWithoutSeparator(byte[] byArray) {
            int n;
            for (n = byArray.length - 1; n >= 0 && this.isLineEnding(byArray[n]); --n) {
            }
            return n + 1;
        }

        private void processLine(OutputStream outputStream, PGPSignatureGenerator pGPSignatureGenerator, byte[] byArray) throws lw.bouncycastle.openpgp.PGPException, IOException {
            int n = this.getLengthWithoutWhiteSpace(byArray);
            if (n > 0) {
                pGPSignatureGenerator.update(byArray, 0, n);
            }
            outputStream.write(byArray, 0, byArray.length);
        }

        private void processLineV3(OutputStream outputStream, PGPV3SignatureGenerator pGPV3SignatureGenerator, byte[] byArray) throws lw.bouncycastle.openpgp.PGPException, IOException {
            int n = this.getLengthWithoutWhiteSpace(byArray);
            if (n > 0) {
                pGPV3SignatureGenerator.update(byArray, 0, n);
            }
            outputStream.write(byArray, 0, byArray.length);
        }

        private void processLine(PGPSignature pGPSignature, byte[] byArray) throws IOException {
            int n = this.getLengthWithoutWhiteSpace(byArray);
            if (n > 0) {
                pGPSignature.update(byArray, 0, n);
            }
        }

        private int getLengthWithoutWhiteSpace(byte[] byArray) {
            int n;
            for (n = byArray.length - 1; n >= 0 && this.isWhiteSpace(byArray[n]); --n) {
            }
            return n + 1;
        }

        private boolean isWhiteSpace(byte by) {
            return by == 13 || by == 10 || by == 9 || by == 32;
        }

        private boolean isLineEnding(byte by) {
            return by == 13 || by == 10;
        }

        private int readInputLine(ByteArrayOutputStream byteArrayOutputStream, InputStream inputStream) throws IOException {
            int n;
            byteArrayOutputStream.reset();
            int n2 = -1;
            while ((n = inputStream.read()) >= 0) {
                byteArrayOutputStream.write(n);
                if (n != 13 && n != 10) continue;
                n2 = this.readPassedEOL(byteArrayOutputStream, n, inputStream);
                break;
            }
            return n2;
        }

        private int readInputLine(ByteArrayOutputStream byteArrayOutputStream, int n, InputStream inputStream) throws IOException {
            byteArrayOutputStream.reset();
            int n2 = n;
            do {
                byteArrayOutputStream.write(n2);
                if (n2 != 13 && n2 != 10) continue;
                n = this.readPassedEOL(byteArrayOutputStream, n2, inputStream);
                break;
            } while ((n2 = inputStream.read()) >= 0);
            if (n2 < 0) {
                n = -1;
            }
            return n;
        }

        private int readPassedEOL(ByteArrayOutputStream byteArrayOutputStream, int n, InputStream inputStream) throws IOException {
            int n2 = inputStream.read();
            if (n == 13 && n2 == 10) {
                byteArrayOutputStream.write(n2);
                n2 = inputStream.read();
            }
            return n2;
        }
    }
}

