/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.openpgp.PGPCompressedData
 *  lw.bouncycastle.openpgp.PGPEncryptedDataList
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPLiteralData
 *  lw.bouncycastle.openpgp.PGPMarker
 *  lw.bouncycastle.openpgp.PGPObjectFactory
 *  lw.bouncycastle.openpgp.PGPOnePassSignature
 *  lw.bouncycastle.openpgp.PGPOnePassSignatureList
 *  lw.bouncycastle.openpgp.PGPPBEEncryptedData
 *  lw.bouncycastle.openpgp.PGPPrivateKey
 *  lw.bouncycastle.openpgp.PGPPublicKey
 *  lw.bouncycastle.openpgp.PGPPublicKeyEncryptedData
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKey
 *  lw.bouncycastle.openpgp.PGPSecretKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKeyRingCollection
 *  lw.bouncycastle.openpgp.PGPSignature
 *  lw.bouncycastle.openpgp.PGPSignatureList
 *  lw.bouncycastle.openpgp.PGPUtil
 */
package com.didisoft.pgp.inspect;

import com.didisoft.pgp.CypherAlgorithm;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.bc.BaseLib;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.bc.PGP2xPBEEncryptedData;
import com.didisoft.pgp.bc.PGPObjectFactory2;
import com.didisoft.pgp.exceptions.FileIsEncryptedException;
import com.didisoft.pgp.exceptions.NonPGPDataException;
import com.didisoft.pgp.exceptions.WrongPasswordException;
import com.didisoft.pgp.exceptions.WrongPrivateKeyException;
import com.didisoft.pgp.inspect.ContentItem;
import com.didisoft.pgp.inspect.SignatureItem;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Iterator;
import lw.bouncycastle.openpgp.PGPCompressedData;
import lw.bouncycastle.openpgp.PGPEncryptedDataList;
import lw.bouncycastle.openpgp.PGPLiteralData;
import lw.bouncycastle.openpgp.PGPMarker;
import lw.bouncycastle.openpgp.PGPObjectFactory;
import lw.bouncycastle.openpgp.PGPOnePassSignature;
import lw.bouncycastle.openpgp.PGPOnePassSignatureList;
import lw.bouncycastle.openpgp.PGPPBEEncryptedData;
import lw.bouncycastle.openpgp.PGPPrivateKey;
import lw.bouncycastle.openpgp.PGPPublicKey;
import lw.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKey;
import lw.bouncycastle.openpgp.PGPSecretKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import lw.bouncycastle.openpgp.PGPSignature;
import lw.bouncycastle.openpgp.PGPSignatureList;
import lw.bouncycastle.openpgp.PGPUtil;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

public class PGPInspectLib
extends BaseLib {
    public boolean isPGPData(byte[] byArray) {
        int n;
        int n2;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byArray);
        int n3 = byteArrayInputStream.read();
        if (n3 < 0) {
            return false;
        }
        if ((n3 & 0x80) == 0) {
            return false;
        }
        boolean bl = (n3 & 0x40) != 0;
        int n4 = 0;
        int n5 = 0;
        boolean bl2 = false;
        if (bl) {
            n4 = n3 & 0x3F;
            n2 = byteArrayInputStream.read();
            if (n2 < 192) {
                n5 = n2;
            } else if (n2 <= 223) {
                n = byteArrayInputStream.read();
                n5 = (n2 - 192 << 8) + n + 192;
            } else if (n2 == 255) {
                n5 = byteArrayInputStream.read() << 24 | byteArrayInputStream.read() << 16 | byteArrayInputStream.read() << 8 | byteArrayInputStream.read();
            } else {
                bl2 = true;
                n5 = 1 << (n2 & 0x1F);
            }
        } else {
            n2 = n3 & 3;
            n4 = (n3 & 0x3F) >> 2;
            switch (n2) {
                case 0: {
                    n5 = byteArrayInputStream.read();
                    break;
                }
                case 1: {
                    n5 = byteArrayInputStream.read() << 8 | byteArrayInputStream.read();
                    break;
                }
                case 2: {
                    n5 = byteArrayInputStream.read() << 24 | byteArrayInputStream.read() << 16 | byteArrayInputStream.read() << 8 | byteArrayInputStream.read();
                    break;
                }
                case 3: {
                    bl2 = true;
                    break;
                }
                default: {
                    return false;
                }
            }
        }
        n2 = (byte)byteArrayInputStream.read();
        n = bl ? 4 : 3;
        switch (n4) {
            case 0: {
                return false;
            }
            case 1: {
                return n == n2;
            }
            case 2: {
                return false;
            }
            case 3: {
                return n == n2;
            }
            case 4: {
                return n == n2;
            }
            case 5: {
                return false;
            }
            case 6: {
                return false;
            }
            case 7: {
                return false;
            }
            case 8: {
                return n == n2;
            }
            case 9: {
                return false;
            }
            case 10: {
                return n == n2;
            }
            case 11: {
                return n == n2;
            }
            case 12: {
                return false;
            }
            case 13: {
                return false;
            }
            case 17: {
                return false;
            }
            case 14: {
                return false;
            }
            case 18: {
                return false;
            }
            case 19: {
                return false;
            }
            case 60: 
            case 61: 
            case 62: 
            case 63: {
                return false;
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isPublicKeyEncrypted(String string) throws IOException, NonPGPDataException {
        InputStream inputStream = null;
        try {
            inputStream = PGPInspectLib.readFileOrAsciiString(string, "dataFile");
            boolean bl = this.isPublicKeyEncrypted(inputStream);
            return bl;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public boolean isPublicKeyEncrypted(InputStream inputStream) throws IOException, NonPGPDataException {
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            if (object == null) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            throw iOException;
        }
        if (object instanceof PGPMarker) {
            object = pGPObjectFactory2.nextObject();
        }
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            Iterator iterator = pGPEncryptedDataList.getEncryptedDataObjects();
            while (iterator.hasNext()) {
                Object e = iterator.next();
                if (!(e instanceof PGPPublicKeyEncryptedData)) continue;
                return true;
            }
        }
        return false;
    }

    public boolean isPasswordEncrypted(String string) throws IOException, NonPGPDataException {
        return this.isPBEEncrypted(string);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isPBEEncrypted(String string) throws IOException, NonPGPDataException {
        InputStream inputStream = null;
        try {
            inputStream = IOUtil.readFileOrAsciiString(string, "fileName");
            boolean bl = this.isPBEEncrypted(inputStream);
            return bl;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public boolean isPasswodEncrypted(InputStream inputStream) throws IOException, NonPGPDataException {
        return this.isPBEEncrypted(inputStream);
    }

    public boolean isPBEEncrypted(InputStream inputStream) throws IOException, NonPGPDataException {
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            if (object == null) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            throw iOException;
        }
        if (object instanceof PGPMarker) {
            object = pGPObjectFactory2.nextObject();
        }
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            Iterator iterator = pGPEncryptedDataList.getEncryptedDataObjects();
            while (iterator.hasNext()) {
                Object e = iterator.next();
                if (!(e instanceof PGPPBEEncryptedData)) continue;
                return true;
            }
        } else if (object instanceof PGP2xPBEEncryptedData) {
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long[] listEncryptionKeyIds(String string) throws IOException, NonPGPDataException {
        InputStream inputStream = null;
        try {
            inputStream = PGPInspectLib.readFileOrAsciiString(string, "dataFile");
            long[] lArray = this.listEncryptionKeyIds(inputStream);
            return lArray;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public long[] listEncryptionKeyIds(InputStream inputStream) throws IOException, NonPGPDataException {
        PGPPublicKeyEncryptedData pGPPublicKeyEncryptedData;
        Iterator iterator;
        Object object;
        Object object2;
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
        Object object3 = null;
        try {
            object3 = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            if (object3 == null) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            throw iOException;
        }
        if (object3 instanceof PGPMarker) {
            object3 = pGPObjectFactory2.nextObject();
        }
        ArrayList<Long> arrayList = new ArrayList<Long>();
        if (object3 instanceof PGPEncryptedDataList) {
            object2 = (PGPEncryptedDataList)object3;
            object = object2.getEncryptedDataObjects();
            while (object.hasNext()) {
                iterator = object.next();
                if (!(iterator instanceof PGPPublicKeyEncryptedData)) continue;
                pGPPublicKeyEncryptedData = (PGPPublicKeyEncryptedData)iterator;
                arrayList.add(new Long(pGPPublicKeyEncryptedData.getKeyID()));
            }
        } else if (object3 instanceof PGPPublicKeyRing) {
            object2 = null;
            object = (PGPPublicKeyRing)object3;
            iterator = object.getPublicKeys();
            while (object2 == null && iterator.hasNext()) {
                pGPPublicKeyEncryptedData = (PGPPublicKey)iterator.next();
                if (!pGPPublicKeyEncryptedData.isEncryptionKey()) continue;
                arrayList.add(new Long(pGPPublicKeyEncryptedData.getKeyID()));
            }
        } else if (object3 instanceof PGPSecretKeyRing) {
            object2 = (PGPSecretKeyRing)object3;
            object = new ByteArrayOutputStream(10240);
            iterator = object2.getSecretKeys();
            while (iterator.hasNext()) {
                pGPPublicKeyEncryptedData = ((PGPSecretKey)iterator.next()).getPublicKey();
                if (pGPPublicKeyEncryptedData == null) continue;
                ((OutputStream)object).write(pGPPublicKeyEncryptedData.getEncoded());
            }
            iterator = null;
            pGPPublicKeyEncryptedData = staticBCFactory.CreatePGPPublicKeyRing(((ByteArrayOutputStream)object).toByteArray());
            Iterator iterator2 = pGPPublicKeyEncryptedData.getPublicKeys();
            while (iterator == null && iterator2.hasNext()) {
                PGPPublicKey pGPPublicKey = (PGPPublicKey)iterator2.next();
                if (!pGPPublicKey.isEncryptionKey()) continue;
                arrayList.add(new Long(pGPPublicKey.getKeyID()));
            }
        }
        object2 = new long[arrayList.size()];
        for (int i = 0; i < arrayList.size(); ++i) {
            object2[i] = (PGPEncryptedDataList)((Long)arrayList.get(i));
        }
        return object2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureItem[] listDetachedSignatureFile(String string) throws IOException, NonPGPDataException {
        SignatureItem[] signatureItemArray;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            signatureItemArray = this.listDetachedSignatureStream(fileInputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return signatureItemArray;
    }

    public SignatureItem[] listDetachedSignatureStream(InputStream inputStream) throws IOException, NonPGPDataException {
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
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
            SignatureItem[] signatureItemArray = new SignatureItem[pGPSignatureList.size()];
            for (int i = 0; i < pGPSignatureList.size(); ++i) {
                pGPSignature = pGPSignatureList.get(i);
                signatureItemArray[i] = this.createSignatureItem(pGPSignature);
            }
            return signatureItemArray;
        }
        throw new NonPGPDataException("Unknown message format: " + object.getClass().getName());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CypherAlgorithm.Enum getCypherAlgorithmUsed(InputStream inputStream, String string, String string2) throws PGPException, IOException {
        CypherAlgorithm.Enum enum_;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            enum_ = this.getCypherAlgorithmUsed(inputStream, fileInputStream, string2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return enum_;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CypherAlgorithm.Enum getCypherAlgorithmUsed(String string, String string2, String string3) throws PGPException, IOException {
        CypherAlgorithm.Enum enum_;
        InputStream inputStream = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string2);
            inputStream = PGPInspectLib.readFileOrAsciiString(string, "encryptedData");
            enum_ = this.getCypherAlgorithmUsed(inputStream, fileInputStream, string3);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            IOUtil.closeStream(inputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(inputStream);
        return enum_;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CypherAlgorithm.Enum getCypherAlgorithmUsed(String string, KeyStore keyStore, String string2) throws PGPException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = PGPInspectLib.readFileOrAsciiString(string, "encryptedData");
            CypherAlgorithm.Enum enum_ = this.getCypherAlgorithmUsed(inputStream, keyStore, string2);
            return enum_;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public CypherAlgorithm.Enum getCypherAlgorithmUsed(InputStream inputStream, InputStream inputStream2, String string) throws PGPException, IOException {
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
            object = pGPObjectFactory2.nextObject();
        }
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            return this.getSymmetricAlgorithmUsed(pGPEncryptedDataList, null, inputStream2, string);
        }
        return CypherAlgorithm.Enum.NONE;
    }

    public CypherAlgorithm.Enum getCypherAlgorithmUsed(InputStream inputStream, KeyStore keyStore, String string) throws PGPException, IOException {
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
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            return this.getSymmetricAlgorithmUsed(pGPEncryptedDataList, keyStore, null, string);
        }
        return CypherAlgorithm.Enum.NONE;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long[] listSigningKeyIds(String string) throws PGPException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = IOUtil.readFileOrAsciiString(string, "dataFileName");
            long[] lArray = this.listSigningKeyIds(inputStream, null, null);
            return lArray;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public long[] listSigningKeyIds(InputStream inputStream) throws PGPException, IOException {
        return this.listSigningKeyIds(inputStream, null, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long[] listSigningKeyIds(String string, InputStream inputStream, String string2) throws PGPException, IOException {
        InputStream inputStream2 = null;
        try {
            inputStream2 = IOUtil.readFileOrAsciiString(string, "dataFileName");
            long[] lArray = this.listSigningKeyIds(inputStream2, inputStream, string2);
            return lArray;
        }
        finally {
            IOUtil.closeStream(inputStream2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long[] listSigningKeyIds(String string, String string2, String string3) throws PGPException, IOException {
        long[] lArray;
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            inputStream = IOUtil.readFileOrAsciiString(string2, "privateKeyFileName");
            inputStream2 = IOUtil.readFileOrAsciiString(string, "dataFileName");
            lArray = this.listSigningKeyIds(inputStream2, inputStream, string3);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(inputStream2);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(inputStream2);
        return lArray;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isSignedOnly(String string) throws PGPException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = PGPInspectLib.readFileOrAsciiString(string, "dataFile");
            boolean bl = this.isSignedOnly(inputStream);
            return bl;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public boolean isSignedOnly(InputStream inputStream) throws PGPException, IOException {
        InputStream inputStream2 = inputStream;
        InputStream inputStream3 = PGPUtil.getDecoderStream((InputStream)inputStream2);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            if (object == null) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            throw iOException;
        }
        if (object instanceof PGPMarker) {
            object = pGPObjectFactory2.nextObject();
        }
        if (object instanceof PGPOnePassSignatureList) {
            return true;
        }
        if (object instanceof PGPSignatureList) {
            return true;
        }
        if (object instanceof PGPCompressedData) {
            PGPCompressedData pGPCompressedData = (PGPCompressedData)object;
            BufferedInputStream bufferedInputStream = null;
            try {
                bufferedInputStream = new BufferedInputStream(pGPCompressedData.getDataStream());
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
            PGPObjectFactory2 pGPObjectFactory22 = new PGPObjectFactory2(bufferedInputStream);
            Object object2 = pGPObjectFactory22.nextObject();
            if (object2 instanceof PGPOnePassSignatureList) {
                return true;
            }
            if (object2 instanceof PGPSignatureList) {
                return true;
            }
        }
        return false;
    }

    public long[] listSigningKeyIds(InputStream inputStream, InputStream inputStream2, String string) throws PGPException, IOException {
        Object object;
        ArrayList<Long> arrayList;
        block22: {
            InputStream inputStream3 = PGPUtil.getDecoderStream((InputStream)inputStream);
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
            Object object2 = null;
            try {
                object2 = pGPObjectFactory2.nextObject();
            }
            catch (IOException iOException) {
                if (object2 == null) {
                    throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
                }
                throw new PGPException(iOException.getMessage(), iOException);
            }
            if (object2 instanceof PGPMarker) {
                object2 = pGPObjectFactory2.nextObject();
            }
            arrayList = new ArrayList<Long>();
            if (object2 instanceof PGPEncryptedDataList) {
                object = (PGPEncryptedDataList)object2;
                try {
                    if (inputStream2 == null) {
                        throw new FileIsEncryptedException("The data is encrypted.");
                    }
                    return this.listSigningKeysFromEncryptedData((PGPEncryptedDataList)object, null, inputStream2, string, null);
                }
                catch (SignatureException signatureException) {}
            } else {
                PGPPublicKey pGPPublicKey;
                Iterator iterator;
                Object object3;
                if (object2 instanceof PGPCompressedData) {
                    try {
                        return this.listSigningKeysFromCompressedData((PGPCompressedData)object2);
                    }
                    catch (SignatureException signatureException) {
                        break block22;
                    }
                }
                if (object2 instanceof PGPOnePassSignatureList) {
                    return this.listSigningKeysFromOnePassSignatureList((PGPOnePassSignatureList)object2);
                }
                if (object2 instanceof PGPSignatureList) {
                    return this.listSigningKeysFromSignatureList((PGPSignatureList)object2);
                }
                if (object2 instanceof PGPPublicKeyRing) {
                    object = null;
                    object3 = (PGPPublicKeyRing)object2;
                    iterator = object3.getPublicKeys();
                    while (object == null && iterator.hasNext()) {
                        pGPPublicKey = (PGPPublicKey)iterator.next();
                        if (!PGPInspectLib.isForVerification(pGPPublicKey)) continue;
                        arrayList.add(new Long(pGPPublicKey.getKeyID()));
                    }
                } else if (object2 instanceof PGPSecretKeyRing) {
                    object = (PGPSecretKeyRing)object2;
                    object3 = new ByteArrayOutputStream(10240);
                    iterator = object.getSecretKeys();
                    while (iterator.hasNext()) {
                        pGPPublicKey = ((PGPSecretKey)iterator.next()).getPublicKey();
                        if (pGPPublicKey == null) continue;
                        ((OutputStream)object3).write(pGPPublicKey.getEncoded());
                    }
                    iterator = null;
                    pGPPublicKey = staticBCFactory.CreatePGPPublicKeyRing(((ByteArrayOutputStream)object3).toByteArray());
                    Iterator iterator2 = pGPPublicKey.getPublicKeys();
                    while (iterator == null && iterator2.hasNext()) {
                        PGPPublicKey pGPPublicKey2 = (PGPPublicKey)iterator2.next();
                        if (!PGPInspectLib.isForVerification(pGPPublicKey2)) continue;
                        arrayList.add(new Long(pGPPublicKey2.getKeyID()));
                    }
                }
            }
        }
        if (arrayList.size() > 0) {
            object = new long[arrayList.size()];
            for (int i = 0; i < arrayList.size(); ++i) {
                object[i] = (PGPEncryptedDataList)((Long)arrayList.get(i));
            }
            return object;
        }
        return new long[0];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureItem[] listSignatures(String string) throws PGPException, IOException {
        SignatureItem[] signatureItemArray;
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            signatureItemArray = this.listSignatures((InputStream)bufferedInputStream, (KeyStore)null, null);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(bufferedInputStream);
            throw throwable;
        }
        IOUtil.closeStream(bufferedInputStream);
        return signatureItemArray;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureItem[] listSignatures(String string, String string2, String string3) throws PGPException, IOException {
        SignatureItem[] signatureItemArray;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            fileInputStream = new FileInputStream(string2);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            signatureItemArray = this.listSignatures((InputStream)bufferedInputStream, fileInputStream, string3);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            IOUtil.closeStream(bufferedInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(bufferedInputStream);
        return signatureItemArray;
    }

    public SignatureItem[] listSignatures(InputStream inputStream, KeyStore keyStore, String string) throws PGPException, IOException {
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
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            try {
                return this.listSignaturesFromEncryptedData(pGPEncryptedDataList, keyStore, null, string, null);
            }
            catch (SignatureException signatureException) {
            }
        } else if (object instanceof PGPCompressedData) {
            try {
                return this.listSignaturesFromCompressedData((PGPCompressedData)object);
            }
            catch (SignatureException signatureException) {
            }
        } else {
            if (object instanceof PGPOnePassSignatureList) {
                return this.listSignaturesFromOnePassSignatureList(pGPObjectFactory2, (PGPOnePassSignatureList)object);
            }
            if (object instanceof PGPSignatureList) {
                return this.listSignaturesFromSignatureList((PGPSignatureList)object);
            }
        }
        return new SignatureItem[0];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureItem[] listSignatures(String string, KeyStore keyStore, String string2) throws PGPException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = PGPInspectLib.readFileOrAsciiString(string, "encryptedData");
            SignatureItem[] signatureItemArray = this.listSignatures(inputStream, keyStore, string2);
            return signatureItemArray;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public SignatureItem[] listSignatures(InputStream inputStream) throws PGPException, IOException {
        return this.listSignatures(inputStream, (KeyStore)null, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureItem[] listSignatures(InputStream inputStream, String string, String string2) throws PGPException, IOException {
        SignatureItem[] signatureItemArray;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            signatureItemArray = this.listSignatures(inputStream, fileInputStream, string2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return signatureItemArray;
    }

    public SignatureItem[] listSignatures(InputStream inputStream, InputStream inputStream2, String string) throws PGPException, IOException {
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
            object = pGPObjectFactory2.nextObject();
        }
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            try {
                if (inputStream2 == null) {
                    throw new FileIsEncryptedException("The data is encrypted.");
                }
                return this.listSignaturesFromEncryptedData(pGPEncryptedDataList, null, inputStream2, string, null);
            }
            catch (SignatureException signatureException) {}
        } else if (object instanceof PGPCompressedData) {
            try {
                return this.listSignaturesFromCompressedData((PGPCompressedData)object);
            }
            catch (SignatureException signatureException) {
            }
        } else {
            if (object instanceof PGPOnePassSignatureList) {
                return this.listSignaturesFromOnePassSignatureList(pGPObjectFactory2, (PGPOnePassSignatureList)object);
            }
            if (object instanceof PGPSignatureList) {
                return this.listSignaturesFromSignatureList((PGPSignatureList)object);
            }
        }
        return new SignatureItem[0];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ContentItem[] listOpenPGPFile(String string, String string2, String string3) throws PGPException, IOException {
        ContentItem[] contentItemArray;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            fileInputStream = new FileInputStream(string2);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            contentItemArray = this.listOpenPGPStream(bufferedInputStream, fileInputStream, string3);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            IOUtil.closeStream(bufferedInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.closeStream(bufferedInputStream);
        return contentItemArray;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ContentItem[] listOpenPGPFile(String string) throws PGPException, IOException {
        ContentItem[] contentItemArray;
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(string));
            contentItemArray = this.listOpenPGPStream(bufferedInputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(bufferedInputStream);
            throw throwable;
        }
        IOUtil.closeStream(inputStream);
        IOUtil.closeStream(bufferedInputStream);
        return contentItemArray;
    }

    public ContentItem[] listOpenPGPStream(InputStream inputStream, InputStream inputStream2, String string) throws PGPException, IOException {
        Object object;
        PGPObjectFactory2 pGPObjectFactory2;
        block21: {
            InputStream inputStream3 = PGPUtil.getDecoderStream((InputStream)inputStream);
            pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
            object = null;
            try {
                object = pGPObjectFactory2.nextObject();
            }
            catch (IOException iOException) {
                if (object != null) break block21;
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
        }
        if (object instanceof PGPMarker) {
            object = pGPObjectFactory2.nextObject();
        }
        ContentItem[] contentItemArray = new ContentItem[]{};
        if (object instanceof PGPEncryptedDataList) {
            PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
            try {
                contentItemArray = this.analyzeEncryptedData(pGPEncryptedDataList, false, null, inputStream2, string, null);
            }
            catch (SignatureException signatureException) {}
        } else if (object instanceof PGPCompressedData) {
            try {
                contentItemArray = this.analyzeCompressedData((PGPCompressedData)object, false, null, null);
            }
            catch (SignatureException signatureException) {}
        } else if (object instanceof PGPOnePassSignatureList) {
            try {
                contentItemArray = this.analyzeSignedData((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null);
            }
            catch (SignatureException signatureException) {}
        } else if (object instanceof PGPSignatureList) {
            try {
                contentItemArray = this.analyzeSignedDataVersion3((PGPSignatureList)object, pGPObjectFactory2, null, null);
            }
            catch (SignatureException signatureException) {}
        } else if (object instanceof PGPLiteralData) {
            contentItemArray = this.analyzeLiteralData((PGPLiteralData)object);
        } else {
            throw new PGPException("Unknown message format: " + object);
        }
        return contentItemArray;
    }

    public ContentItem[] listOpenPGPStream(InputStream inputStream) throws PGPException, IOException {
        Object object;
        PGPObjectFactory2 pGPObjectFactory2;
        block18: {
            InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
            pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
            object = null;
            try {
                object = pGPObjectFactory2.nextObject();
            }
            catch (IOException iOException) {
                if (object != null) break block18;
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
        }
        if (object instanceof PGPMarker) {
            object = pGPObjectFactory2.nextObject();
        }
        ContentItem[] contentItemArray = new ContentItem[]{};
        if (object instanceof PGPEncryptedDataList) {
            throw new FileIsEncryptedException("The supplied data is encrypted. Use the overloaded version that accepts private decryption key instead.");
        }
        if (object instanceof PGPCompressedData) {
            try {
                contentItemArray = this.analyzeCompressedData((PGPCompressedData)object, false, null, null);
            }
            catch (SignatureException signatureException) {}
        } else if (object instanceof PGPOnePassSignatureList) {
            try {
                contentItemArray = this.analyzeSignedData((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null);
            }
            catch (SignatureException signatureException) {}
        } else if (object instanceof PGPSignatureList) {
            try {
                contentItemArray = this.analyzeSignedDataVersion3((PGPSignatureList)object, pGPObjectFactory2, null, null);
            }
            catch (SignatureException signatureException) {}
        } else if (object instanceof PGPLiteralData) {
            contentItemArray = this.analyzeLiteralData((PGPLiteralData)object);
        } else {
            throw new PGPException("Unknown message format: " + object);
        }
        return contentItemArray;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SignatureItem[] listRevocationCertificate(String string) throws IOException, PGPException {
        SignatureItem[] signatureItemArray;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            signatureItemArray = this.listRevocationCertificate(fileInputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return signatureItemArray;
    }

    public SignatureItem[] listRevocationCertificate(InputStream inputStream) throws IOException, PGPException {
        PGPSignature pGPSignature;
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
        Object object = null;
        try {
            object = pGPObjectFactory2.nextObject();
        }
        catch (IOException iOException) {
            if (object == null) {
                throw new NonPGPDataException("The supplied data is not a valid OpenPGP message", iOException);
            }
            throw new PGPException(iOException.getMessage(), iOException);
        }
        if (object instanceof PGPMarker) {
            object = pGPObjectFactory2.nextObject();
        }
        if (object instanceof PGPSignatureList) {
            PGPSignatureList pGPSignatureList = (PGPSignatureList)object;
            ArrayList<SignatureItem> arrayList = new ArrayList<SignatureItem>();
            for (int i = 0; i != pGPSignatureList.size(); ++i) {
                if (pGPSignatureList.get(i).getSignatureType() != 32) continue;
                arrayList.add(this.createSignatureItem(pGPSignatureList.get(i)));
            }
            return arrayList.toArray(new SignatureItem[arrayList.size()]);
        }
        if (object instanceof PGPSignature && ((pGPSignature = (PGPSignature)object).getSignatureType() == 32 || pGPSignature.getSignatureType() == 40)) {
            return new SignatureItem[]{new SignatureItem(pGPSignature.getKeyID(), pGPSignature.getCreationTime())};
        }
        return new SignatureItem[0];
    }

    private ContentItem[] analyzeEncryptedData(PGPEncryptedDataList pGPEncryptedDataList, boolean bl, KeyStore keyStore, InputStream inputStream, String string, InputStream inputStream2) throws IOException, WrongPasswordException, WrongPrivateKeyException, PGPException, SignatureException {
        Object object;
        PGPPrivateKey pGPPrivateKey = null;
        PGPSecretKeyRingCollection pGPSecretKeyRingCollection = null;
        pGPSecretKeyRingCollection = inputStream != null ? this.createPGPSecretKeyRingCollection(inputStream) : keyStore.getRawSecretKeys();
        PGPPublicKeyEncryptedData pGPPublicKeyEncryptedData = null;
        Object object2 = pGPEncryptedDataList.getEncryptedDataObjects();
        while (pGPPrivateKey == null && object2.hasNext() && (!((object = object2.next()) instanceof PGPPublicKeyEncryptedData) || (pGPPrivateKey = this.getPrivateKey(pGPSecretKeyRingCollection, (pGPPublicKeyEncryptedData = (PGPPublicKeyEncryptedData)object).getKeyID(), string)) == null)) {
        }
        if (pGPPrivateKey == null) {
            throw new WrongPrivateKeyException("secret key for message not found.");
        }
        object2 = null;
        try {
            object2 = pGPPublicKeyEncryptedData.getDataStream(staticBCFactory.CreatePublicKeyDataDecryptorFactory(pGPPrivateKey));
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        object = new PGPObjectFactory2((InputStream)object2);
        Object object3 = object.nextObject();
        if (object3 instanceof PGPCompressedData) {
            return this.analyzeCompressedData((PGPCompressedData)object3, bl, keyStore, inputStream2);
        }
        if (object3 instanceof PGPOnePassSignatureList) {
            if (bl) {
                return this.analyzeSignedData((PGPOnePassSignatureList)object3, (PGPObjectFactory)object, keyStore, inputStream2);
            }
            return this.analyzeSignedData((PGPOnePassSignatureList)object3, (PGPObjectFactory)object, null, null);
        }
        if (object3 instanceof PGPLiteralData) {
            return this.analyzeLiteralData((PGPLiteralData)object3);
        }
        throw new PGPException("Unknown message format: " + object3.getClass().getName());
    }

    private ContentItem[] analyzeCompressedData(PGPCompressedData pGPCompressedData, boolean bl, KeyStore keyStore, InputStream inputStream) throws PGPException, IOException, SignatureException {
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(pGPCompressedData.getDataStream());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(bufferedInputStream);
        Object object = pGPObjectFactory2.nextObject();
        if (object instanceof PGPLiteralData) {
            return this.analyzeLiteralData((PGPLiteralData)object);
        }
        if (object instanceof PGPOnePassSignatureList) {
            if (bl) {
                return this.analyzeSignedData((PGPOnePassSignatureList)object, pGPObjectFactory2, keyStore, inputStream);
            }
            return this.analyzeSignedData((PGPOnePassSignatureList)object, pGPObjectFactory2, null, null);
        }
        if (object instanceof PGPSignatureList) {
            if (bl) {
                return this.analyzeSignedDataVersion3((PGPSignatureList)object, pGPObjectFactory2, keyStore, inputStream);
            }
            return this.analyzeSignedDataVersion3((PGPSignatureList)object, pGPObjectFactory2, null, null);
        }
        throw new PGPException("Unknown message format: " + object.getClass().getName());
    }

    private ContentItem[] analyzeLiteralData(PGPLiteralData pGPLiteralData) throws IOException {
        String string = pGPLiteralData.getFileName();
        if (string.toUpperCase().endsWith(".TAR")) {
            TarInputStream tarInputStream = new TarInputStream(pGPLiteralData.getInputStream());
            ArrayList<ContentItem> arrayList = new ArrayList<ContentItem>();
            TarEntry tarEntry = tarInputStream.getNextEntry();
            while (tarEntry != null) {
                arrayList.add(new ContentItem(tarEntry.getName(), tarEntry.getModTime(), tarEntry.isDirectory()));
                tarEntry = tarInputStream.getNextEntry();
            }
            return arrayList.toArray(new ContentItem[arrayList.size()]);
        }
        return new ContentItem[]{new ContentItem(string, pGPLiteralData.getModificationTime())};
    }

    private ContentItem[] analyzeSignedData(PGPOnePassSignatureList pGPOnePassSignatureList, PGPObjectFactory pGPObjectFactory, KeyStore keyStore, InputStream inputStream) throws PGPException, IOException, SignatureException {
        PGPOnePassSignature pGPOnePassSignature = null;
        PGPPublicKey pGPPublicKey = null;
        if (inputStream != null || keyStore != null) {
            for (int i = 0; i != pGPOnePassSignatureList.size(); ++i) {
                pGPOnePassSignature = pGPOnePassSignatureList.get(i);
                pGPPublicKey = inputStream != null ? this.readPublicVerificationKey(inputStream, pGPOnePassSignature.getKeyID()) : PGPInspectLib.readPublicVerificationKey(keyStore, pGPOnePassSignature.getKeyID());
                if (pGPPublicKey != null) break;
            }
            if (pGPPublicKey == null) {
                throw new PGPException("No public key could be found for signature.");
            }
            try {
                staticBCFactory.initVerify(pGPOnePassSignature, pGPPublicKey);
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        Object object = pGPObjectFactory.nextObject();
        ContentItem[] contentItemArray = new ContentItem[]{};
        if (!(object instanceof PGPLiteralData)) {
            throw new PGPException("Unknown message format: " + object.getClass().getName());
        }
        contentItemArray = this.analyzeLiteralData((PGPLiteralData)object);
        return contentItemArray;
    }

    private ContentItem[] analyzeSignedDataVersion3(PGPSignatureList pGPSignatureList, PGPObjectFactory pGPObjectFactory, KeyStore keyStore, InputStream inputStream) throws PGPException, IOException, SignatureException {
        Object object;
        PGPSignature pGPSignature = null;
        if (inputStream != null || keyStore != null) {
            object = null;
            for (int i = 0; i < pGPSignatureList.size() && ((pGPSignature = pGPSignatureList.get(i)).getSignatureType() != 0 && pGPSignature.getSignatureType() != 1 && pGPSignature.getSignatureType() != 16 || (object = inputStream != null ? this.readPublicVerificationKey(inputStream, pGPSignature.getKeyID()) : PGPInspectLib.readPublicVerificationKey(keyStore, pGPSignature.getKeyID())) == null); ++i) {
            }
            if (object == null) {
                throw new PGPException("No public key could be found for signature.");
            }
            try {
                staticBCFactory.initVerify(pGPSignature, (PGPPublicKey)object);
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        object = pGPObjectFactory.nextObject();
        ContentItem[] contentItemArray = new ContentItem[]{};
        if (!(object instanceof PGPLiteralData)) {
            throw new PGPException("Unknown message format: " + object.getClass().getName());
        }
        contentItemArray = this.analyzeLiteralData((PGPLiteralData)object);
        return contentItemArray;
    }

    private long[] listSigningKeysFromEncryptedData(PGPEncryptedDataList pGPEncryptedDataList, KeyStore keyStore, InputStream inputStream, String string, InputStream inputStream2) throws IOException, WrongPasswordException, WrongPrivateKeyException, PGPException, SignatureException {
        Object object;
        PGPPrivateKey pGPPrivateKey = null;
        PGPSecretKeyRingCollection pGPSecretKeyRingCollection = null;
        pGPSecretKeyRingCollection = inputStream != null ? this.createPGPSecretKeyRingCollection(inputStream) : keyStore.getRawSecretKeys();
        PGPPublicKeyEncryptedData pGPPublicKeyEncryptedData = null;
        Object object2 = pGPEncryptedDataList.getEncryptedDataObjects();
        while (pGPPrivateKey == null && object2.hasNext() && (!((object = object2.next()) instanceof PGPPublicKeyEncryptedData) || (pGPPrivateKey = this.getPrivateKey(pGPSecretKeyRingCollection, (pGPPublicKeyEncryptedData = (PGPPublicKeyEncryptedData)object).getKeyID(), string)) == null)) {
        }
        if (pGPPrivateKey == null) {
            throw new WrongPrivateKeyException("secret key for message not found.");
        }
        object2 = null;
        try {
            object2 = pGPPublicKeyEncryptedData.getDataStream(staticBCFactory.CreatePublicKeyDataDecryptorFactory(pGPPrivateKey));
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        object = new PGPObjectFactory2((InputStream)object2);
        Object object3 = object.nextObject();
        if (object3 instanceof PGPCompressedData) {
            return this.listSigningKeysFromCompressedData((PGPCompressedData)object3);
        }
        if (object3 instanceof PGPOnePassSignatureList) {
            return this.listSigningKeysFromOnePassSignatureList((PGPOnePassSignatureList)object3);
        }
        return new long[0];
    }

    private long[] listSigningKeysFromCompressedData(PGPCompressedData pGPCompressedData) throws PGPException, IOException, SignatureException {
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(pGPCompressedData.getDataStream());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(bufferedInputStream);
        Object object = pGPObjectFactory2.nextObject();
        if (object instanceof PGPOnePassSignatureList) {
            PGPOnePassSignatureList pGPOnePassSignatureList = (PGPOnePassSignatureList)object;
            long[] lArray = new long[pGPOnePassSignatureList.size()];
            for (int i = 0; i != pGPOnePassSignatureList.size(); ++i) {
                lArray[i] = pGPOnePassSignatureList.get(i).getKeyID();
            }
            return lArray;
        }
        if (object instanceof PGPSignatureList) {
            PGPSignatureList pGPSignatureList = (PGPSignatureList)object;
            long[] lArray = new long[pGPSignatureList.size()];
            for (int i = 0; i != pGPSignatureList.size(); ++i) {
                lArray[i] = pGPSignatureList.get(i).getKeyID();
            }
            return lArray;
        }
        return new long[0];
    }

    private long[] listSigningKeysFromOnePassSignatureList(PGPOnePassSignatureList pGPOnePassSignatureList) {
        long[] lArray = new long[pGPOnePassSignatureList.size()];
        for (int i = 0; i != pGPOnePassSignatureList.size(); ++i) {
            lArray[i] = pGPOnePassSignatureList.get(i).getKeyID();
        }
        return lArray;
    }

    private long[] listSigningKeysFromSignatureList(PGPSignatureList pGPSignatureList) {
        long[] lArray = new long[pGPSignatureList.size()];
        for (int i = 0; i != pGPSignatureList.size(); ++i) {
            lArray[i] = pGPSignatureList.get(i).getKeyID();
        }
        return lArray;
    }

    private SignatureItem createSignatureItem(PGPSignature pGPSignature) {
        SignatureItem signatureItem = new SignatureItem(pGPSignature.getKeyID(), pGPSignature.getCreationTime());
        if (pGPSignature.getHashedSubPackets() != null && pGPSignature.getHashedSubPackets().getSignerUserID() != null) {
            signatureItem.setUserId(pGPSignature.getHashedSubPackets().getSignerUserID());
        }
        return signatureItem;
    }

    private SignatureItem[] listSignaturesFromEncryptedData(PGPEncryptedDataList pGPEncryptedDataList, KeyStore keyStore, InputStream inputStream, String string, InputStream inputStream2) throws IOException, WrongPasswordException, WrongPrivateKeyException, PGPException, SignatureException {
        Object object;
        PGPPrivateKey pGPPrivateKey = null;
        PGPSecretKeyRingCollection pGPSecretKeyRingCollection = null;
        pGPSecretKeyRingCollection = inputStream != null ? this.createPGPSecretKeyRingCollection(inputStream) : keyStore.getRawSecretKeys();
        PGPPublicKeyEncryptedData pGPPublicKeyEncryptedData = null;
        Object object2 = pGPEncryptedDataList.getEncryptedDataObjects();
        while (pGPPrivateKey == null && object2.hasNext() && (!((object = object2.next()) instanceof PGPPublicKeyEncryptedData) || (pGPPrivateKey = this.getPrivateKey(pGPSecretKeyRingCollection, (pGPPublicKeyEncryptedData = (PGPPublicKeyEncryptedData)object).getKeyID(), string)) == null)) {
        }
        if (pGPPrivateKey == null) {
            throw new WrongPrivateKeyException("secret key for message not found.");
        }
        object2 = null;
        try {
            object2 = pGPPublicKeyEncryptedData.getDataStream(staticBCFactory.CreatePublicKeyDataDecryptorFactory(pGPPrivateKey));
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        object = new PGPObjectFactory2((InputStream)object2);
        Object object3 = object.nextObject();
        if (object3 instanceof PGPCompressedData) {
            return this.listSignaturesFromCompressedData((PGPCompressedData)object3);
        }
        if (object3 instanceof PGPOnePassSignatureList) {
            return this.listSignaturesFromOnePassSignatureList((PGPObjectFactory)object, (PGPOnePassSignatureList)object3);
        }
        if (object3 instanceof PGPSignatureList) {
            return this.listSignaturesFromSignatureList((PGPSignatureList)object3);
        }
        return new SignatureItem[0];
    }

    private SignatureItem[] listSignaturesFromCompressedData(PGPCompressedData pGPCompressedData) throws PGPException, IOException, SignatureException {
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(pGPCompressedData.getDataStream());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(bufferedInputStream);
        Object object = pGPObjectFactory2.nextObject();
        if (object instanceof PGPOnePassSignatureList) {
            return this.listSignaturesFromOnePassSignatureList(pGPObjectFactory2, (PGPOnePassSignatureList)object);
        }
        if (object instanceof PGPSignatureList) {
            return this.listSignaturesFromSignatureList((PGPSignatureList)object);
        }
        return new SignatureItem[0];
    }

    private SignatureItem[] listSignaturesFromOnePassSignatureList(PGPObjectFactory pGPObjectFactory, PGPOnePassSignatureList pGPOnePassSignatureList) throws IOException {
        Object object = pGPObjectFactory.nextObject();
        Object object2 = pGPObjectFactory.nextObject();
        if (object2 != null) {
            PGPSignatureList pGPSignatureList = (PGPSignatureList)object2;
            SignatureItem[] signatureItemArray = new SignatureItem[pGPSignatureList.size()];
            for (int i = 0; i != pGPSignatureList.size(); ++i) {
                signatureItemArray[i] = this.createSignatureItem(pGPSignatureList.get(i));
            }
            return signatureItemArray;
        }
        return new SignatureItem[0];
    }

    private SignatureItem[] listSignaturesFromSignatureList(PGPSignatureList pGPSignatureList) {
        SignatureItem[] signatureItemArray = new SignatureItem[pGPSignatureList.size()];
        for (int i = 0; i != pGPSignatureList.size(); ++i) {
            signatureItemArray[i] = this.createSignatureItem(pGPSignatureList.get(i));
        }
        return signatureItemArray;
    }

    private CypherAlgorithm.Enum getSymmetricAlgorithmUsed(PGPEncryptedDataList pGPEncryptedDataList, KeyStore keyStore, InputStream inputStream, String string) throws IOException, WrongPasswordException, WrongPrivateKeyException, PGPException {
        PGPPrivateKey pGPPrivateKey = null;
        PGPSecretKeyRingCollection pGPSecretKeyRingCollection = null;
        pGPSecretKeyRingCollection = inputStream != null ? this.createPGPSecretKeyRingCollection(inputStream) : keyStore.getRawSecretKeys();
        PGPPublicKeyEncryptedData pGPPublicKeyEncryptedData = null;
        Iterator iterator = pGPEncryptedDataList.getEncryptedDataObjects();
        while (pGPPrivateKey == null && iterator.hasNext()) {
            Object e = iterator.next();
            if (e instanceof PGPPublicKeyEncryptedData) {
                pGPPublicKeyEncryptedData = (PGPPublicKeyEncryptedData)e;
                pGPPrivateKey = this.getPrivateKey(pGPSecretKeyRingCollection, pGPPublicKeyEncryptedData.getKeyID(), string);
                if (pGPPrivateKey == null) continue;
                int n = 0;
                try {
                    n = pGPPublicKeyEncryptedData.getSymmetricAlgorithm(staticBCFactory.CreatePublicKeyDataDecryptorFactory(pGPPrivateKey));
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    throw IOUtil.newPGPException(pGPException);
                }
                return this.getSymmetricAlgorithm(n);
            }
            return CypherAlgorithm.Enum.NONE;
        }
        if (pGPPrivateKey == null) {
            throw new WrongPrivateKeyException("secret key for message not found.");
        }
        return CypherAlgorithm.Enum.NONE;
    }
}

