/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.ArmoredInputStream
 *  lw.bouncycastle.bcpg.ExperimentalPacket
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPOnePassSignatureList
 *  lw.bouncycastle.openpgp.PGPPrivateKey
 *  lw.bouncycastle.openpgp.PGPPublicKey
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPPublicKeyRingCollection
 *  lw.bouncycastle.openpgp.PGPSecretKey
 *  lw.bouncycastle.openpgp.PGPSecretKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKeyRingCollection
 *  lw.bouncycastle.openpgp.PGPSignature
 *  lw.bouncycastle.openpgp.PGPSignatureSubpacketVector
 *  lw.bouncycastle.openpgp.PGPUtil
 *  lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor
 *  lw.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider
 *  lw.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider
 *  lw.bouncycastle.util.encoders.Base64
 *  lw.bouncycastle.util.encoders.DecoderException
 */
package com.didisoft.pgp.bc;

import com.didisoft.pgp.CompressionAlgorithm;
import com.didisoft.pgp.CypherAlgorithm;
import com.didisoft.pgp.HashAlgorithm;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.bc.BCFactory;
import com.didisoft.pgp.bc.BoolValue;
import com.didisoft.pgp.bc.BufferedInputStreamExtended;
import com.didisoft.pgp.bc.DirectByteArrayOutputStream;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.bc.PBESecretKeyDecryptorASCII;
import com.didisoft.pgp.bc.PGPObjectFactory2;
import com.didisoft.pgp.bc.UnknownKeyPacketsException;
import com.didisoft.pgp.exceptions.NoPrivateKeyFoundException;
import com.didisoft.pgp.exceptions.NoPublicKeyFoundException;
import com.didisoft.pgp.exceptions.WrongPasswordException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import lw.bouncycastle.bcpg.ArmoredInputStream;
import lw.bouncycastle.bcpg.ExperimentalPacket;
import lw.bouncycastle.openpgp.PGPOnePassSignatureList;
import lw.bouncycastle.openpgp.PGPPrivateKey;
import lw.bouncycastle.openpgp.PGPPublicKey;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import lw.bouncycastle.openpgp.PGPSecretKey;
import lw.bouncycastle.openpgp.PGPSecretKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import lw.bouncycastle.openpgp.PGPSignature;
import lw.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import lw.bouncycastle.openpgp.PGPUtil;
import lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import lw.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider;
import lw.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import lw.bouncycastle.util.encoders.Base64;
import lw.bouncycastle.util.encoders.DecoderException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class BaseLib {
    public static String version = "Didisoft OpenPGP Library for Java 3.2";
    protected static BCFactory staticBCFactory = new BCFactory(false);
    protected static final int DEFAULT_BUFFER_SIZE = 0x100000;
    protected static final int QUARTER_DEFAULT_BUFFER_SIZE = 262144;
    protected static final String NOT_A_VALID_OPENPGP_MESSAGE = "The supplied data is not a valid OpenPGP message";
    protected static final String UNKNOWN_MESSAGE_FORMAT = "Unknown message format: ";
    public static final String BOUNCY_CASTLE_PROVIDER = "BC";
    private static final Logger log = Logger.getLogger(BaseLib.class.getName());
    private static final String PGP_START_TAG = "-----BEGIN PGP P";
    private static final String PGP_END_TAG = "-----END PGP P";
    private static final String lineFeed = System.getProperty("line.separator");
    protected static final int UNKNOWN_ALGORITHM = -1;
    public static int READ_AHEAD = 60;
    private static final int[] MASTER_KEY_CERTIFICATION_TYPES = new int[]{19, 18, 17, 16};
    private static Pattern regexHex = Pattern.compile("^(0x)?[A-Fa-f0-9]{6,8}$");

    public static List loadKeyStream(InputStream inputStream) throws FileNotFoundException, IOException, PGPException {
        List list = new LinkedList();
        BoolValue boolValue = new BoolValue();
        InputStream inputStream2 = BaseLib.cleanGnuPGBackupKeys(inputStream);
        if ((inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream2)) instanceof ArmoredInputStream) {
            ArmoredInputStream armoredInputStream = (ArmoredInputStream)inputStream2;
            while (!armoredInputStream.isEndOfStream()) {
                List list2 = BaseLib.loadKeysFromDecodedStream((InputStream)armoredInputStream, boolValue);
                list.addAll(list2);
                if (!boolValue.isValue()) continue;
                break;
            }
        } else {
            list = BaseLib.loadKeysFromDecodedStream(inputStream2, boolValue);
        }
        return list;
    }

    private static List loadKeysFromDecodedStream(InputStream inputStream, BoolValue boolValue) throws PGPException, IOException {
        LinkedList<PGPPublicKeyRing> linkedList = new LinkedList<PGPPublicKeyRing>();
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
        pGPObjectFactory2.setLoadingKey(true);
        try {
            Object object = pGPObjectFactory2.nextObject();
            while (object != null) {
                PGPPublicKeyRing pGPPublicKeyRing;
                if (object instanceof PGPPublicKeyRing) {
                    pGPPublicKeyRing = (PGPPublicKeyRing)object;
                    linkedList.add(pGPPublicKeyRing);
                } else if (object instanceof PGPSecretKeyRing) {
                    pGPPublicKeyRing = (PGPSecretKeyRing)object;
                    linkedList.add(pGPPublicKeyRing);
                } else if (!(object instanceof ExperimentalPacket) && !(object instanceof PGPOnePassSignatureList)) {
                    throw new PGPException("Unexpected object found in stream: " + object.getClass().getName());
                }
                object = pGPObjectFactory2.nextObject();
            }
        }
        catch (UnknownKeyPacketsException unknownKeyPacketsException) {
            boolValue.setValue(true);
        }
        return linkedList;
    }

    protected PGPSecretKeyRingCollection createPGPSecretKeyRingCollection(InputStream inputStream) throws IOException, PGPException {
        inputStream = BaseLib.cleanGnuPGBackupKeys(inputStream);
        if ((inputStream = PGPUtil.getDecoderStream((InputStream)inputStream)) instanceof ArmoredInputStream) {
            ArmoredInputStream armoredInputStream = (ArmoredInputStream)inputStream;
            while (!armoredInputStream.isEndOfStream()) {
                PGPSecretKeyRingCollection pGPSecretKeyRingCollection = BaseLib.createPGPSecretKeyRingCollectionSub((InputStream)armoredInputStream);
                if (pGPSecretKeyRingCollection.size() <= 0) continue;
                return pGPSecretKeyRingCollection;
            }
        } else {
            return BaseLib.createPGPSecretKeyRingCollectionSub(inputStream);
        }
        try {
            return new PGPSecretKeyRingCollection(new ArrayList());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    private static PGPSecretKeyRingCollection createPGPSecretKeyRingCollectionSub(InputStream inputStream) throws IOException, PGPException {
        Object object;
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
        HashMap<Long, PGPSecretKeyRing> hashMap = new HashMap<Long, PGPSecretKeyRing>();
        do {
            try {
                object = pGPObjectFactory2.nextObject();
            }
            catch (IOException iOException) {
                throw new NoPrivateKeyFoundException(NOT_A_VALID_OPENPGP_MESSAGE, iOException);
            }
            if (!(object instanceof PGPSecretKeyRing)) continue;
            PGPSecretKeyRing pGPSecretKeyRing = (PGPSecretKeyRing)object;
            Long l = new Long(pGPSecretKeyRing.getPublicKey().getKeyID());
            hashMap.put(l, pGPSecretKeyRing);
        } while (object != null);
        try {
            return new PGPSecretKeyRingCollection(hashMap.values());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    protected PGPPrivateKey getPrivateKey(PGPSecretKeyRingCollection pGPSecretKeyRingCollection, long l, String string) throws WrongPasswordException, PGPException {
        if (string == null) {
            string = "";
        }
        PGPSecretKey pGPSecretKey = null;
        try {
            pGPSecretKey = pGPSecretKeyRingCollection.getSecretKey(l);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        return BaseLib.extractPrivateKey(pGPSecretKey, string);
    }

    public static int parseHashAlgorithm(String string) {
        if ("SHA256".equalsIgnoreCase(string)) {
            return 8;
        }
        if ("SHA384".equalsIgnoreCase(string)) {
            return 9;
        }
        if ("SHA512".equalsIgnoreCase(string)) {
            return 10;
        }
        if ("SHA224".equalsIgnoreCase(string)) {
            return 11;
        }
        if ("SHA1".equalsIgnoreCase(string)) {
            return 2;
        }
        if ("MD5".equalsIgnoreCase(string)) {
            return 1;
        }
        if ("RIPEMD160".equalsIgnoreCase(string)) {
            return 3;
        }
        if ("MD2".equalsIgnoreCase(string)) {
            return 5;
        }
        return -1;
    }

    public static int getFirstCompressionAlgorithm(String string) {
        if (string.contains(",")) {
            String[] stringArray = string.split(",");
            string = stringArray[0];
        }
        if ("ZLIB".equalsIgnoreCase(string)) {
            return 2;
        }
        if ("ZIP".equalsIgnoreCase(string)) {
            return 1;
        }
        if ("UNCOMPRESSED".equalsIgnoreCase(string)) {
            return 0;
        }
        if ("BZIP2".equalsIgnoreCase(string)) {
            return 3;
        }
        return -1;
    }

    public static int getFirstSymmetricAlgorithm(String string) {
        if (string.contains(",")) {
            String[] stringArray = string.split(",");
            string = stringArray[0];
        }
        if ("TRIPLE_DES".equalsIgnoreCase(string)) {
            return 2;
        }
        if ("CAST5".equalsIgnoreCase(string)) {
            return 3;
        }
        if ("BLOWFISH".equalsIgnoreCase(string)) {
            return 4;
        }
        if ("AES_128".equalsIgnoreCase(string)) {
            return 7;
        }
        if ("AES_192".equalsIgnoreCase(string)) {
            return 8;
        }
        if ("AES_256".equalsIgnoreCase(string)) {
            return 9;
        }
        if ("TWOFISH".equalsIgnoreCase(string)) {
            return 10;
        }
        if ("DES".equalsIgnoreCase(string)) {
            return 6;
        }
        if ("SAFER".equalsIgnoreCase(string)) {
            return 5;
        }
        if ("IDEA".equalsIgnoreCase(string)) {
            return 5;
        }
        return -1;
    }

    public static List<Integer> listOfPrefferedCyphers(String string) {
        String[] stringArray = string.split(",");
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        for (int i = 0; i < stringArray.length; ++i) {
            linkedList.add(CypherAlgorithm.Enum.fromString(stringArray[i].trim()).intValue());
        }
        return linkedList;
    }

    public static List<Integer> listOfPrefferedCompressions(String string) {
        String[] stringArray = string.split(",");
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        for (int i = 0; i < stringArray.length; ++i) {
            linkedList.add(CompressionAlgorithm.Enum.fromString(stringArray[i].trim()).intValue());
        }
        return linkedList;
    }

    public static List<Integer> listOfPrefferedHashes(String string) {
        String[] stringArray = string.split(",");
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        for (int i = 0; i < stringArray.length; ++i) {
            linkedList.add(HashAlgorithm.Enum.fromString(stringArray[i].trim()).intValue());
        }
        return linkedList;
    }

    public static String toUserID(byte[] byArray) {
        try {
            return new String(byArray, "UTF-8");
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            return new String(byArray);
        }
    }

    public static PGPPrivateKey extractPrivateKey(PGPSecretKey pGPSecretKey, String string) throws WrongPasswordException, PGPException {
        return BaseLib.extractPrivateKey(pGPSecretKey, string == null ? new char[]{} : string.toCharArray());
    }

    public static PGPPrivateKey extractPrivateKey(PGPSecretKey pGPSecretKey, char[] cArray) throws WrongPasswordException, PGPException {
        if (pGPSecretKey == null) {
            return null;
        }
        try {
            PBESecretKeyDecryptor pBESecretKeyDecryptor = staticBCFactory.CreatePBESecretKeyDecryptor(cArray);
            return pGPSecretKey.extractPrivateKey(pBESecretKeyDecryptor);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            if (pGPException.getMessage().toLowerCase().contains("checksum mismatch")) {
                try {
                    PBESecretKeyDecryptorASCII pBESecretKeyDecryptorASCII = new PBESecretKeyDecryptorASCII(cArray, (PGPDigestCalculatorProvider)new BcPGPDigestCalculatorProvider());
                    return pGPSecretKey.extractPrivateKey((PBESecretKeyDecryptor)pBESecretKeyDecryptorASCII);
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException2) {
                    if (pGPException2.getMessage().toLowerCase().contains("checksum mismatch")) {
                        throw new WrongPasswordException(pGPException2.getMessage(), pGPException2.getUnderlyingException());
                    }
                    throw new WrongPasswordException(pGPException.getMessage(), pGPException.getUnderlyingException());
                }
            }
            throw IOUtil.newPGPException(pGPException);
        }
    }

    protected static InputStream readFileOrAsciiString(String string, String string2) throws IOException {
        return IOUtil.readFileOrAsciiString(string, string2);
    }

    private static byte[] readPgpAsciiMarkerToEnd(InputStream inputStream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        int n = -1;
        while ((n = inputStream.read()) > -1 && n != 45) {
            if (n > -1) {
                stringBuffer.append((char)n);
                continue;
            }
            return stringBuffer.toString().getBytes("ASCII");
        }
        while ((n = inputStream.read()) > -1 && n == 45) {
            if (n > -1) {
                stringBuffer.append((char)n);
                continue;
            }
            return stringBuffer.toString().getBytes("ASCII");
        }
        stringBuffer.append((char)n);
        return stringBuffer.toString().getBytes("ASCII");
    }

    public static boolean isAsciiArmored(InputStream inputStream) throws IOException {
        int n;
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        inputStream.mark(READ_AHEAD);
        int n2 = inputStream.read();
        if ((n2 & 0x80) != 0) {
            inputStream.reset();
            return false;
        }
        if (!IOUtil.isPossiblyBase64(n2)) {
            inputStream.reset();
            return true;
        }
        byte[] byArray = new byte[READ_AHEAD];
        int n3 = 1;
        byArray[0] = (byte)n2;
        for (n = 1; n != READ_AHEAD && (n2 = inputStream.read()) >= 0; ++n) {
            if (!IOUtil.isPossiblyBase64(n2)) {
                inputStream.reset();
                return true;
            }
            if (n2 == 10 || n2 == 13) continue;
            byArray[n3++] = (byte)n2;
        }
        inputStream.reset();
        if (n < 4) {
            return true;
        }
        byte[] byArray2 = new byte[8];
        System.arraycopy(byArray, 0, byArray2, 0, byArray2.length);
        try {
            byte[] byArray3 = Base64.decode((byte[])byArray2);
            if ((byArray3[0] & 0x80) != 0) {
                return true;
            }
            return true;
        }
        catch (DecoderException decoderException) {
            throw new IOException(decoderException.getMessage());
        }
    }

    public static InputStream cleanGnuPGBackupKeys(InputStream inputStream) throws IOException {
        int n = 4096;
        InputStream inputStream2 = null;
        inputStream2 = inputStream.markSupported() ? inputStream : new BufferedInputStreamExtended(inputStream);
        inputStream2.mark(n);
        if (!BaseLib.isAsciiArmored(inputStream2)) {
            return inputStream2;
        }
        inputStream2.reset();
        DirectByteArrayOutputStream directByteArrayOutputStream = new DirectByteArrayOutputStream(n);
        BaseLib.pipeAll(inputStream2, directByteArrayOutputStream);
        StringBuffer stringBuffer = new StringBuffer(new String(directByteArrayOutputStream.getArray(), 0, directByteArrayOutputStream.size(), "ASCII"));
        int n2 = stringBuffer.indexOf(PGP_START_TAG);
        int n3 = stringBuffer.lastIndexOf(PGP_END_TAG);
        if (n2 > -1) {
            stringBuffer = new StringBuffer(stringBuffer.substring(n2, stringBuffer.indexOf("-----", n3 + 1) + "-----".length()));
        }
        BaseLib.replaceAll(stringBuffer, "\r\r\n", lineFeed);
        if (stringBuffer.indexOf("Comment") != -1 || stringBuffer.indexOf("Version") != -1) {
            BaseLib.replaceAll(stringBuffer, "\r\n\r\n", lineFeed);
        }
        BaseLib.replaceAll(stringBuffer, "\\n", lineFeed);
        BufferedReader bufferedReader = new BufferedReader(new StringReader(stringBuffer.toString()));
        String string = null;
        stringBuffer.setLength(0);
        while ((string = bufferedReader.readLine()) != null) {
            if (string.trim().toLowerCase().startsWith("charset") || string.trim().toLowerCase().startsWith("comment")) continue;
            stringBuffer.append(string).append(lineFeed);
        }
        if (stringBuffer.indexOf("-----BEGIN PGP PUBLIC KEY BLOCK-----") == 0 && stringBuffer.indexOf("Version:") == -1) {
            stringBuffer = stringBuffer.replace(0, "-----BEGIN PGP PUBLIC KEY BLOCK-----".length(), "-----BEGIN PGP PUBLIC KEY BLOCK-----" + lineFeed + "Version:");
        }
        int n4 = 0;
        while (stringBuffer.indexOf("Version", n4) != -1) {
            int n5 = stringBuffer.indexOf(lineFeed, stringBuffer.indexOf("Version", n4) + 1);
            n4 = stringBuffer.indexOf("Version", n4);
            if (!stringBuffer.substring(n5 + lineFeed.length(), n5 + 2 * lineFeed.length()).equals(lineFeed)) {
                stringBuffer.insert(n5 + lineFeed.length(), lineFeed);
            }
            ++n4;
        }
        return new ByteArrayInputStream(stringBuffer.toString().getBytes("ASCII"));
    }

    public static void replaceAll(StringBuffer stringBuffer, String string, String string2) {
        int n = stringBuffer.indexOf(string);
        while (n != -1) {
            stringBuffer.replace(n, n + string.length(), string2);
            n += string2.length();
            n = stringBuffer.indexOf(string, n);
        }
    }

    protected PGPPublicKeyRingCollection createPGPPublicKeyRingCollection(InputStream inputStream) throws IOException, PGPException {
        inputStream = BaseLib.cleanGnuPGBackupKeys(inputStream);
        if ((inputStream = PGPUtil.getDecoderStream((InputStream)inputStream)) instanceof ArmoredInputStream) {
            ArmoredInputStream armoredInputStream = (ArmoredInputStream)inputStream;
            while (!armoredInputStream.isEndOfStream()) {
                BoolValue boolValue = new BoolValue();
                PGPPublicKeyRingCollection pGPPublicKeyRingCollection = BaseLib.createPGPPublicKeyRingCollectionSub((InputStream)armoredInputStream, boolValue);
                if (pGPPublicKeyRingCollection.size() > 0) {
                    return pGPPublicKeyRingCollection;
                }
                if (!boolValue.value) continue;
                break;
            }
        } else {
            return BaseLib.createPGPPublicKeyRingCollectionSub(inputStream, new BoolValue());
        }
        try {
            return new PGPPublicKeyRingCollection(new ArrayList());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    private static PGPPublicKeyRingCollection createPGPPublicKeyRingCollectionSub(InputStream inputStream, BoolValue boolValue) throws IOException, PGPException {
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
        pGPObjectFactory2.setLoadingKey(true);
        HashMap<Long, PGPPublicKeyRing> hashMap = new HashMap<Long, PGPPublicKeyRing>();
        try {
            Object object;
            while ((object = pGPObjectFactory2.nextObject()) != null) {
                if (!(object instanceof PGPPublicKeyRing)) continue;
                PGPPublicKeyRing pGPPublicKeyRing = (PGPPublicKeyRing)object;
                Long l = new Long(pGPPublicKeyRing.getPublicKey().getKeyID());
                hashMap.put(l, pGPPublicKeyRing);
            }
        }
        catch (UnknownKeyPacketsException unknownKeyPacketsException) {
            boolValue.value = true;
        }
        catch (IOException iOException) {
            throw new NoPublicKeyFoundException(iOException.getMessage(), iOException);
        }
        try {
            return new PGPPublicKeyRingCollection(hashMap.values());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw new NoPublicKeyFoundException(pGPException.getMessage(), pGPException.getUnderlyingException());
        }
    }

    protected static PGPPublicKey readPublicVerificationKey(KeyStore keyStore, long l) throws IOException {
        return BaseLib.readPublicVerificationKey(keyStore.getRawPublicKeys(), l);
    }

    protected PGPPublicKey readPublicVerificationKey(InputStream inputStream, long l) throws IOException, PGPException {
        PGPPublicKeyRingCollection pGPPublicKeyRingCollection = this.createPGPPublicKeyRingCollection(inputStream);
        return BaseLib.readPublicVerificationKey(pGPPublicKeyRingCollection, l);
    }

    protected static PGPPublicKey readPublicVerificationKey(PGPPublicKeyRingCollection pGPPublicKeyRingCollection, long l) throws IOException {
        PGPPublicKey pGPPublicKey = null;
        Iterator iterator = pGPPublicKeyRingCollection.getKeyRings();
        while (pGPPublicKey == null && iterator.hasNext()) {
            PGPPublicKeyRing pGPPublicKeyRing = (PGPPublicKeyRing)iterator.next();
            Iterator iterator2 = pGPPublicKeyRing.getPublicKeys();
            while (pGPPublicKey == null && iterator2.hasNext()) {
                PGPPublicKey pGPPublicKey2 = (PGPPublicKey)iterator2.next();
                if (!BaseLib.isForVerification(pGPPublicKey2) || l != pGPPublicKey2.getKeyID()) continue;
                pGPPublicKey = pGPPublicKey2;
                return pGPPublicKey;
            }
        }
        return pGPPublicKey;
    }

    public static boolean isForVerification(PGPPublicKey pGPPublicKey) {
        if (pGPPublicKey.getAlgorithm() == 18 || pGPPublicKey.getAlgorithm() == 16 || pGPPublicKey.getAlgorithm() == 20 || pGPPublicKey.getAlgorithm() == 21 || pGPPublicKey.getAlgorithm() == 2) {
            return false;
        }
        return BaseLib.hasKeyFlags(pGPPublicKey, 2);
    }

    protected static boolean hasKeyFlags(PGPPublicKey pGPPublicKey, int n) {
        if (pGPPublicKey.isMasterKey()) {
            for (int i = 0; i != MASTER_KEY_CERTIFICATION_TYPES.length; ++i) {
                Iterator iterator = pGPPublicKey.getSignaturesOfType(MASTER_KEY_CERTIFICATION_TYPES[i]);
                while (iterator.hasNext()) {
                    PGPSignature pGPSignature = (PGPSignature)iterator.next();
                    if (BaseLib.isMatchingUsage(pGPSignature, n)) continue;
                    return false;
                }
            }
        } else {
            Iterator iterator = pGPPublicKey.getSignaturesOfType(24);
            while (iterator.hasNext()) {
                PGPSignature pGPSignature = (PGPSignature)iterator.next();
                if (BaseLib.isMatchingUsage(pGPSignature, n)) continue;
                return false;
            }
        }
        return true;
    }

    private static boolean isMatchingUsage(PGPSignature pGPSignature, int n) {
        PGPSignatureSubpacketVector pGPSignatureSubpacketVector;
        return !pGPSignature.hasSubpackets() || !(pGPSignatureSubpacketVector = pGPSignature.getHashedSubPackets()).hasSubpacket(27) || (pGPSignatureSubpacketVector.getKeyFlags() & n) != 0;
    }

    protected static void pipeAll(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] byArray = new byte[0x100000];
        int n = 0;
        while ((n = inputStream.read(byArray)) > 0) {
            outputStream.write(byArray, 0, n);
        }
    }

    public static boolean isHexId(String string) {
        return regexHex.matcher(string).matches();
    }

    protected CypherAlgorithm.Enum getSymmetricAlgorithm(int n) {
        if (n == 1) {
            return CypherAlgorithm.Enum.IDEA;
        }
        if (n == 9) {
            return CypherAlgorithm.Enum.AES_256;
        }
        if (n == 8) {
            return CypherAlgorithm.Enum.AES_192;
        }
        if (n == 7) {
            return CypherAlgorithm.Enum.AES_128;
        }
        if (n == 2) {
            return CypherAlgorithm.Enum.TRIPLE_DES;
        }
        if (n == 10) {
            return CypherAlgorithm.Enum.TWOFISH;
        }
        if (n == 4) {
            return CypherAlgorithm.Enum.BLOWFISH;
        }
        if (n == 11) {
            return CypherAlgorithm.Enum.CAMELLIA_128;
        }
        if (n == 12) {
            return CypherAlgorithm.Enum.CAMELLIA_192;
        }
        if (n == 13) {
            return CypherAlgorithm.Enum.CAMELLIA_256;
        }
        if (n == 0) {
            return CypherAlgorithm.Enum.NONE;
        }
        throw new IllegalArgumentException("unknown symmetric encryption algorithm: " + n);
    }
}

