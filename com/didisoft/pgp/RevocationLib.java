/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.ArmoredInputStream
 *  lw.bouncycastle.bcpg.ArmoredOutputStream
 *  lw.bouncycastle.bcpg.SignatureSubpacket
 *  lw.bouncycastle.bcpg.sig.IssuerKeyID
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPPublicKey
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPPublicKeyRingCollection
 *  lw.bouncycastle.openpgp.PGPSecretKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKeyRingCollection
 *  lw.bouncycastle.openpgp.PGPSignature
 *  lw.bouncycastle.openpgp.PGPSignatureGenerator
 *  lw.bouncycastle.openpgp.PGPSignatureList
 *  lw.bouncycastle.openpgp.PGPSignatureSubpacketVector
 *  lw.bouncycastle.openpgp.PGPUtil
 *  lw.bouncycastle.util.Arrays
 *  lw.bouncycastle.util.encoders.Hex
 */
package com.didisoft.pgp;

import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.bc.BaseLib;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.bc.PGPObjectFactory2;
import com.didisoft.pgp.bc.PGPSignatureSubpacketGeneratorExtended;
import com.didisoft.pgp.bc.ReflectionUtils;
import com.didisoft.pgp.bc.RevocationKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import lw.bouncycastle.bcpg.ArmoredInputStream;
import lw.bouncycastle.bcpg.ArmoredOutputStream;
import lw.bouncycastle.bcpg.SignatureSubpacket;
import lw.bouncycastle.bcpg.sig.IssuerKeyID;
import lw.bouncycastle.openpgp.PGPPublicKey;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import lw.bouncycastle.openpgp.PGPSecretKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import lw.bouncycastle.openpgp.PGPSignature;
import lw.bouncycastle.openpgp.PGPSignatureGenerator;
import lw.bouncycastle.openpgp.PGPSignatureList;
import lw.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import lw.bouncycastle.openpgp.PGPUtil;
import lw.bouncycastle.util.Arrays;
import lw.bouncycastle.util.encoders.Hex;

public class RevocationLib
extends BaseLib {
    private static final String ASCII_ENCODING = "US-ASCII";
    public static final byte REASON_NO_REASON = 0;
    public static final byte REASON_KEY_SUPERSEDED = 1;
    public static final byte REASON_KEY_COMPROMISED = 2;
    public static final byte REASON_KEY_NO_LONGER_USED = 3;
    public static final byte REASON_USER_NO_LONGER_USED = 32;
    private Logger log = Logger.getLogger(RevocationLib.class.getName());
    private String asciiVersionHeader = null;

    public RevocationLib() {
        ArmoredOutputStream armoredOutputStream = new ArmoredOutputStream((OutputStream)new ByteArrayOutputStream());
        this.asciiVersionHeader = (String)ReflectionUtils.getPrivateFieldvalue(armoredOutputStream, "version");
    }

    public String getAsciiVersionHeader() {
        return "Version: " + this.asciiVersionHeader;
    }

    public void setAsciiVersionHeader(String string) {
        this.asciiVersionHeader = string;
    }

    public String createRevocationCertificateText(String string, String string2, byte by, String string3) throws PGPException, IOException {
        PGPSecretKeyRing pGPSecretKeyRing = this.loadSecretKeyRingFromFile(string);
        return this.createRevocationCertificateHelper(pGPSecretKeyRing, string2, by, string3);
    }

    public String createRevocationCertificateText(KeyStore keyStore, long l, String string, byte by, String string2) throws PGPException, IOException {
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(l);
        return this.createRevocationCertificateHelper(pGPSecretKeyRing, string, by, string2);
    }

    public String createRevocationCertificateText(KeyStore keyStore, String string, String string2, byte by, String string3) throws PGPException, IOException {
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(string);
        return this.createRevocationCertificateHelper(pGPSecretKeyRing, string2, by, string3);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void createRevocationCertificateInFile(String string, String string2, byte by, String string3, String string4) throws PGPException, IOException {
        String string5 = this.createRevocationCertificateText(string, string2, by, string3);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(string4);
            fileOutputStream.write(string5.getBytes(ASCII_ENCODING));
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileOutputStream);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void createRevocationCertificateInFile(KeyStore keyStore, long l, String string, byte by, String string2, String string3) throws PGPException, IOException {
        String string4 = this.createRevocationCertificateText(keyStore, l, string, by, string2);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(string3);
            fileOutputStream.write(string4.getBytes(ASCII_ENCODING));
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileOutputStream);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void createRevocationCertificateInFile(KeyStore keyStore, String string, String string2, byte by, String string3, String string4) throws PGPException, IOException {
        String string5 = this.createRevocationCertificateText(keyStore, string, string2, by, string3);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(string4);
            fileOutputStream.write(string5.getBytes(ASCII_ENCODING));
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileOutputStream);
    }

    public void assignDesignatedRevoker(String string, String string2, String string3, String string4) throws PGPException, IOException {
        PGPPublicKeyRing pGPPublicKeyRing = this.loadPublicKeyRingFromFile(string);
        PGPSecretKeyRing pGPSecretKeyRing = this.loadSecretKeyRingFromFile(string2);
        PGPPublicKeyRing pGPPublicKeyRing2 = this.loadPublicKeyRingFromFile(string4);
        boolean bl = this.isAsciiArmored(string);
        pGPPublicKeyRing = this.internalAssignDesignatedRevoker(pGPPublicKeyRing, pGPSecretKeyRing, string3, pGPPublicKeyRing2);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, string, bl, this.asciiVersionHeader);
    }

    public void assignDesignatedRevoker(KeyStore keyStore, long l, String string, long l2) throws PGPException, IOException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(l);
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(l);
        PGPPublicKeyRing pGPPublicKeyRing2 = keyStore.findPublicKeyRing(l2);
        pGPPublicKeyRing = this.internalAssignDesignatedRevoker(pGPPublicKeyRing, pGPSecretKeyRing, string, pGPPublicKeyRing2);
        keyStore.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public void assignDesignatedRevoker(KeyStore keyStore, String string, String string2, String string3) throws PGPException, IOException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(string);
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(string);
        PGPPublicKeyRing pGPPublicKeyRing2 = keyStore.findPublicKeyRing(string3);
        pGPPublicKeyRing = this.internalAssignDesignatedRevoker(pGPPublicKeyRing, pGPSecretKeyRing, string2, pGPPublicKeyRing2);
        keyStore.replacePublicKeyRing(pGPPublicKeyRing);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void revokeKeyWithRevocationCertificateText(String string, String string2) throws IOException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.loadPublicKeyRingFromFile(string);
        boolean bl = this.isAsciiArmored(string);
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string2.getBytes(ASCII_ENCODING));
            pGPPublicKeyRing = this.revokeKeyWithRevocationCertificateHelper(pGPPublicKeyRing, (InputStream)byteArrayInputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(byteArrayInputStream);
            throw throwable;
        }
        IOUtil.closeStream(byteArrayInputStream);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, string, bl, this.asciiVersionHeader);
    }

    public void revokeKeyWithRevocationCertificateText(KeyStore keyStore, String string) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(string.getBytes(ASCII_ENCODING));
            long l = this.extractRevocationCertificateKeyId(byteArrayInputStream);
            pGPPublicKeyRing = keyStore.findPublicKeyRing(l);
            pGPPublicKeyRing = this.revokeKeyWithRevocationCertificateHelper(pGPPublicKeyRing, (InputStream)byteArrayInputStream);
        }
        catch (IOException iOException) {
            try {
                throw new PGPException(iOException.getMessage(), iOException);
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(byteArrayInputStream);
                throw throwable;
            }
        }
        IOUtil.closeStream(byteArrayInputStream);
        if (pGPPublicKeyRing != null) {
            keyStore.replacePublicKeyRing(pGPPublicKeyRing);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void revokeKeyWithRevocationCertificateFile(String string, String string2) throws IOException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.loadPublicKeyRingFromFile(string);
        boolean bl = this.isAsciiArmored(string);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string2);
            pGPPublicKeyRing = this.revokeKeyWithRevocationCertificateHelper(pGPPublicKeyRing, (InputStream)fileInputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, string, bl, this.asciiVersionHeader);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void revokeKeyWithRevocationCertificateFile(KeyStore keyStore, String string) throws IOException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            pGPPublicKeyRing = this.revokeKeyWithRevocationCertificateHelper(keyStore, (InputStream)fileInputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        if (pGPPublicKeyRing != null) {
            keyStore.replacePublicKeyRing(pGPPublicKeyRing);
        }
    }

    public void revokeKey(KeyStore keyStore, long l, String string, byte by, String string2) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(l);
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(l);
        try {
            pGPPublicKeyRing = this.revokeKeyHelper(pGPPublicKeyRing, pGPPublicKeyRing.getPublicKey(l), pGPSecretKeyRing, string, by, string2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        keyStore.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public void revokeKey(KeyStore keyStore, String string, String string2, byte by, String string3) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(string);
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(string);
        pGPPublicKeyRing = this.revokeKeyHelper(pGPPublicKeyRing, pGPPublicKeyRing.getPublicKey(), pGPSecretKeyRing, string2, by, string3);
        keyStore.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public void revokeKey(String string, String string2, String string3, byte by, String string4) throws IOException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.loadPublicKeyRingFromFile(string);
        PGPSecretKeyRing pGPSecretKeyRing = this.loadSecretKeyRingFromFile(string2);
        boolean bl = this.isAsciiArmored(string);
        pGPPublicKeyRing = this.revokeKeyHelper(pGPPublicKeyRing, pGPPublicKeyRing.getPublicKey(), pGPSecretKeyRing, string3, by, string4);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, string, bl, this.asciiVersionHeader);
    }

    public void revokeUserIdSignature(String string, String string2, String string3, String string4, byte by, String string5) throws IOException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.loadPublicKeyRingFromFile(string);
        PGPSecretKeyRing pGPSecretKeyRing = this.loadSecretKeyRingFromFile(string2);
        boolean bl = this.isAsciiArmored(string);
        pGPPublicKeyRing = this.revokeUserIdHelper(pGPPublicKeyRing, pGPSecretKeyRing, string3, string4, by, string5);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, string, bl, this.asciiVersionHeader);
    }

    public void revokeUserIdSignature(KeyStore keyStore, long l, String string, String string2, byte by, String string3) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(l);
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(l);
        pGPPublicKeyRing = this.revokeUserIdHelper(pGPPublicKeyRing, pGPSecretKeyRing, string2, string, by, string3);
        keyStore.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public void revokeUserIdSignature(KeyStore keyStore, String string, String string2, byte by, String string3) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(string);
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(string);
        pGPPublicKeyRing = this.revokeUserIdHelper(pGPPublicKeyRing, pGPSecretKeyRing, string2, string, by, string3);
        keyStore.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public void revokeKeyWithDesignatedRevoker(String string, String string2, String string3, byte by, String string4) throws PGPException, IOException {
        PGPPublicKeyRing pGPPublicKeyRing = this.loadPublicKeyRingFromFile(string);
        boolean bl = this.isAsciiArmored(string);
        PGPSecretKeyRing pGPSecretKeyRing = this.loadSecretKeyRingFromFile(string2);
        pGPPublicKeyRing = this.revokeWithDisignatedRevokerHelper(pGPPublicKeyRing, pGPSecretKeyRing, string3, by, string4);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, string, bl, this.asciiVersionHeader);
    }

    public void revokeKeyWithDesignatedRevoker(KeyStore keyStore, long l, long l2, String string, byte by, String string2) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(l);
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(l2);
        pGPPublicKeyRing = this.revokeWithDisignatedRevokerHelper(pGPPublicKeyRing, pGPSecretKeyRing, string, by, string2);
        keyStore.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public void revokeKeyWithDesignatedRevoker(KeyStore keyStore, String string, String string2, String string3, byte by, String string4) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = keyStore.findPublicKeyRing(string);
        PGPSecretKeyRing pGPSecretKeyRing = keyStore.findSecretKeyRing(string2);
        pGPPublicKeyRing = this.revokeWithDisignatedRevokerHelper(pGPPublicKeyRing, pGPSecretKeyRing, string3, by, string4);
        keyStore.replacePublicKeyRing(pGPPublicKeyRing);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PGPPublicKeyRing loadPublicKeyRingFromFile(String string) throws PGPException, IOException {
        PGPPublicKeyRing pGPPublicKeyRing = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            PGPPublicKeyRingCollection pGPPublicKeyRingCollection = this.createPGPPublicKeyRingCollection(fileInputStream);
            Iterator iterator = pGPPublicKeyRingCollection.getKeyRings();
            pGPPublicKeyRing = (PGPPublicKeyRing)iterator.next();
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return pGPPublicKeyRing;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean isAsciiArmored(String string) throws IOException {
        boolean bl;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            InputStream inputStream = PGPUtil.getDecoderStream((InputStream)fileInputStream);
            bl = inputStream instanceof ArmoredInputStream;
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PGPSecretKeyRing loadSecretKeyRingFromFile(String string) throws IOException, PGPException {
        PGPSecretKeyRing pGPSecretKeyRing = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            PGPSecretKeyRingCollection pGPSecretKeyRingCollection = this.createPGPSecretKeyRingCollection(fileInputStream);
            Iterator iterator = pGPSecretKeyRingCollection.getKeyRings();
            pGPSecretKeyRing = (PGPSecretKeyRing)iterator.next();
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return pGPSecretKeyRing;
    }

    private PGPPublicKeyRing internalAssignDesignatedRevoker(PGPPublicKeyRing pGPPublicKeyRing, PGPSecretKeyRing pGPSecretKeyRing, String string, PGPPublicKeyRing pGPPublicKeyRing2) throws PGPException {
        PGPSignatureGenerator pGPSignatureGenerator;
        PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended = new PGPSignatureSubpacketGeneratorExtended();
        pGPSignatureSubpacketGeneratorExtended.setSignatureCreationTime(false, new Date());
        pGPSignatureSubpacketGeneratorExtended.setRevocable(false, false);
        pGPSignatureSubpacketGeneratorExtended.setRevocationKey(false, (byte)pGPPublicKeyRing2.getPublicKey().getAlgorithm(), pGPPublicKeyRing2.getPublicKey().getFingerprint());
        PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended2 = new PGPSignatureSubpacketGeneratorExtended();
        pGPSignatureSubpacketGeneratorExtended2.setIssuerKeyID(false, pGPPublicKeyRing.getPublicKey().getKeyID());
        PGPSignature pGPSignature = null;
        try {
            pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPSecretKeyRing.getPublicKey().getAlgorithm(), 2);
            staticBCFactory.initSign(pGPSignatureGenerator, 31, RevocationLib.extractPrivateKey(pGPSecretKeyRing.getSecretKey(), string));
            pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketGeneratorExtended.generate());
            pGPSignatureGenerator.setUnhashedSubpackets(pGPSignatureSubpacketGeneratorExtended2.generate());
            pGPSignature = pGPSignatureGenerator.generateCertification(pGPPublicKeyRing.getPublicKey());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        pGPSignatureGenerator = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKeyRing.getPublicKey(), (PGPSignature)pGPSignature);
        pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKeyRing.getPublicKey());
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPSignatureGenerator);
        return pGPPublicKeyRing;
    }

    private PGPPublicKeyRing revokeKeyHelper(PGPPublicKeyRing pGPPublicKeyRing, PGPPublicKey pGPPublicKey, PGPSecretKeyRing pGPSecretKeyRing, String string, byte by, String string2) throws PGPException {
        PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended = new PGPSignatureSubpacketGeneratorExtended();
        pGPSignatureSubpacketGeneratorExtended.setSignatureCreationTime(false, new Date());
        pGPSignatureSubpacketGeneratorExtended.setRevocationReason(false, by, string2);
        PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended2 = new PGPSignatureSubpacketGeneratorExtended();
        pGPSignatureSubpacketGeneratorExtended2.setIssuerKeyID(false, pGPSecretKeyRing.getSecretKey().getKeyID());
        PGPSignature pGPSignature = null;
        try {
            PGPSignatureGenerator pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPPublicKey.getAlgorithm(), 2);
            if (pGPPublicKey.isMasterKey()) {
                staticBCFactory.initSign(pGPSignatureGenerator, 32, RevocationLib.extractPrivateKey(pGPSecretKeyRing.getSecretKey(), string));
            } else {
                staticBCFactory.initSign(pGPSignatureGenerator, 40, RevocationLib.extractPrivateKey(pGPSecretKeyRing.getSecretKey(), string));
            }
            pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketGeneratorExtended.generate());
            pGPSignatureGenerator.setUnhashedSubpackets(pGPSignatureSubpacketGeneratorExtended2.generate());
            pGPSignature = pGPSignatureGenerator.generateCertification(pGPPublicKey);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        pGPPublicKey = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKey, (PGPSignature)pGPSignature);
        pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
        return pGPPublicKeyRing;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String createRevocationCertificateHelper(PGPSecretKeyRing pGPSecretKeyRing, String string, byte by, String string2) throws PGPException, IOException {
        Object object;
        PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended = new PGPSignatureSubpacketGeneratorExtended();
        pGPSignatureSubpacketGeneratorExtended.setSignatureCreationTime(false, new Date());
        pGPSignatureSubpacketGeneratorExtended.setRevocationReason(false, by, string2);
        PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended2 = new PGPSignatureSubpacketGeneratorExtended();
        pGPSignatureSubpacketGeneratorExtended2.setIssuerKeyID(false, pGPSecretKeyRing.getPublicKey().getKeyID());
        PGPSignature pGPSignature = null;
        try {
            object = staticBCFactory.CreatePGPSignatureGenerator(pGPSecretKeyRing.getPublicKey().getAlgorithm(), 2);
            staticBCFactory.initSign((PGPSignatureGenerator)object, 32, RevocationLib.extractPrivateKey(pGPSecretKeyRing.getSecretKey(), string));
            object.setHashedSubpackets(pGPSignatureSubpacketGeneratorExtended.generate());
            object.setUnhashedSubpackets(pGPSignatureSubpacketGeneratorExtended2.generate());
            pGPSignature = object.generateCertification(pGPSecretKeyRing.getPublicKey());
            this.Debug("Created revocation certificate for key {0}", KeyPairInformation.keyId2Hex(pGPSecretKeyRing.getPublicKey().getKeyID()));
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        object = null;
        ArmoredOutputStream armoredOutputStream = null;
        try {
            object = new ByteArrayOutputStream();
            armoredOutputStream = new ArmoredOutputStream((OutputStream)object);
            pGPSignature.encode((OutputStream)armoredOutputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(armoredOutputStream);
            IOUtil.closeStream((OutputStream)object);
            throw throwable;
        }
        IOUtil.closeStream((OutputStream)armoredOutputStream);
        IOUtil.closeStream((OutputStream)object);
        String string3 = ((ByteArrayOutputStream)object).toString(ASCII_ENCODING);
        string3 = string3.replaceFirst("-----BEGIN PGP SIGNATURE-----", "-----BEGIN PGP PUBLIC KEY BLOCK-----").replaceFirst("-----END PGP SIGNATURE-----", "-----END PGP PUBLIC KEY BLOCK-----");
        return string3;
    }

    private PGPPublicKeyRing revokeKeyWithRevocationCertificateHelper(PGPPublicKeyRing pGPPublicKeyRing, InputStream inputStream) throws IOException, PGPException {
        PGPSignatureList pGPSignatureList;
        Object object;
        inputStream = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
        PGPSignature pGPSignature = null;
        boolean bl = false;
        while ((object = pGPObjectFactory2.nextObject()) != null) {
            if (object instanceof PGPSignatureList) {
                pGPSignatureList = (PGPSignatureList)object;
                for (int i = 0; i < pGPSignatureList.size(); ++i) {
                    IssuerKeyID issuerKeyID;
                    SignatureSubpacket signatureSubpacket;
                    PGPSignatureSubpacketVector pGPSignatureSubpacketVector;
                    pGPSignature = pGPSignatureList.get(i);
                    if (pGPSignature.getSignatureType() == 32) {
                        pGPSignatureSubpacketVector = pGPSignature.getUnhashedSubPackets();
                        signatureSubpacket = pGPSignatureSubpacketVector.getSubpacket(16);
                        if (signatureSubpacket != null && (issuerKeyID = new IssuerKeyID(signatureSubpacket.isCritical(), false, signatureSubpacket.getData())).getKeyID() == pGPPublicKeyRing.getPublicKey().getKeyID()) {
                            bl = true;
                        }
                    } else if (pGPSignature.getSignatureType() == 40 && (signatureSubpacket = (pGPSignatureSubpacketVector = pGPSignature.getUnhashedSubPackets()).getSubpacket(16)) != null && (issuerKeyID = new IssuerKeyID(signatureSubpacket.isCritical(), false, signatureSubpacket.getData())).getKeyID() == pGPPublicKeyRing.getPublicKey().getKeyID()) {
                        bl = true;
                    }
                    if (bl) break;
                }
            }
            if (!bl) continue;
        }
        if (!bl) {
            throw new PGPException("The supplied revocation certificate has no issuer key id subpacket for key with id: " + Long.toHexString(pGPPublicKeyRing.getPublicKey().getKeyID()));
        }
        pGPSignatureList = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKeyRing.getPublicKey(), pGPSignature);
        pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKeyRing.getPublicKey());
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPSignatureList);
        return pGPPublicKeyRing;
    }

    private PGPPublicKeyRing revokeKeyWithRevocationCertificateHelper(KeyStore keyStore, InputStream inputStream) throws IOException, PGPException {
        PGPSignatureList pGPSignatureList;
        Object object;
        inputStream = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
        PGPSignature pGPSignature = null;
        PGPPublicKeyRing pGPPublicKeyRing = null;
        while ((object = pGPObjectFactory2.nextObject()) != null) {
            if (object instanceof PGPSignatureList) {
                pGPSignatureList = (PGPSignatureList)object;
                for (int i = 0; i < pGPSignatureList.size(); ++i) {
                    IssuerKeyID issuerKeyID;
                    SignatureSubpacket signatureSubpacket;
                    PGPSignatureSubpacketVector pGPSignatureSubpacketVector;
                    pGPSignature = pGPSignatureList.get(i);
                    if (pGPSignature.getSignatureType() == 32) {
                        pGPSignatureSubpacketVector = pGPSignature.getUnhashedSubPackets();
                        signatureSubpacket = pGPSignatureSubpacketVector.getSubpacket(16);
                        if (signatureSubpacket != null && keyStore.containsKey((issuerKeyID = new IssuerKeyID(signatureSubpacket.isCritical(), false, signatureSubpacket.getData())).getKeyID())) {
                            pGPPublicKeyRing = keyStore.findPublicKeyRing(issuerKeyID.getKeyID());
                        }
                    } else if (pGPSignature.getSignatureType() == 40 && (signatureSubpacket = (pGPSignatureSubpacketVector = pGPSignature.getUnhashedSubPackets()).getSubpacket(16)) != null && keyStore.containsKey((issuerKeyID = new IssuerKeyID(signatureSubpacket.isCritical(), false, signatureSubpacket.getData())).getKeyID())) {
                        pGPPublicKeyRing = keyStore.findPublicKeyRing(issuerKeyID.getKeyID());
                    }
                    if (pGPPublicKeyRing != null) break;
                }
            }
            if (pGPPublicKeyRing == null) continue;
        }
        if (pGPPublicKeyRing == null) {
            throw new PGPException("No key was found in the KeyStore matching the revocation certificate Issuer ID");
        }
        pGPSignatureList = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKeyRing.getPublicKey(), pGPSignature);
        pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey(pGPPublicKeyRing, (PGPPublicKey)pGPPublicKeyRing.getPublicKey());
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPSignatureList);
        return pGPPublicKeyRing;
    }

    private long extractRevocationCertificateKeyId(InputStream inputStream) throws IOException, PGPException {
        Object object;
        inputStream = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
        PGPSignature pGPSignature = null;
        while ((object = pGPObjectFactory2.nextObject()) != null) {
            if (!(object instanceof PGPSignatureList)) continue;
            PGPSignatureList pGPSignatureList = (PGPSignatureList)object;
            for (int i = 0; i < pGPSignatureList.size(); ++i) {
                SignatureSubpacket signatureSubpacket;
                PGPSignatureSubpacketVector pGPSignatureSubpacketVector;
                pGPSignature = pGPSignatureList.get(i);
                if (pGPSignature.getSignatureType() == 32) {
                    pGPSignatureSubpacketVector = pGPSignature.getUnhashedSubPackets();
                    signatureSubpacket = pGPSignatureSubpacketVector.getSubpacket(16);
                    if (signatureSubpacket == null) continue;
                    IssuerKeyID issuerKeyID = new IssuerKeyID(signatureSubpacket.isCritical(), false, signatureSubpacket.getData());
                    return issuerKeyID.getKeyID();
                }
                if (pGPSignature.getSignatureType() != 40 || (signatureSubpacket = (pGPSignatureSubpacketVector = pGPSignature.getUnhashedSubPackets()).getSubpacket(16)) == null) continue;
                IssuerKeyID issuerKeyID = new IssuerKeyID(signatureSubpacket.isCritical(), false, signatureSubpacket.getData());
                return issuerKeyID.getKeyID();
            }
        }
        return -1L;
    }

    private PGPPublicKeyRing revokeUserIdHelper(PGPPublicKeyRing pGPPublicKeyRing, PGPSecretKeyRing pGPSecretKeyRing, String string, String string2, byte by, String string3) throws PGPException {
        PGPSignatureGenerator pGPSignatureGenerator;
        PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended = new PGPSignatureSubpacketGeneratorExtended();
        pGPSignatureSubpacketGeneratorExtended.setSignatureCreationTime(false, new Date());
        pGPSignatureSubpacketGeneratorExtended.setRevocationReason(false, by, string3);
        PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended2 = new PGPSignatureSubpacketGeneratorExtended();
        pGPSignatureSubpacketGeneratorExtended2.setIssuerKeyID(false, pGPPublicKeyRing.getPublicKey().getKeyID());
        PGPSignature pGPSignature = null;
        try {
            pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPPublicKeyRing.getPublicKey().getAlgorithm(), 2);
            staticBCFactory.initSign(pGPSignatureGenerator, 48, RevocationLib.extractPrivateKey(pGPSecretKeyRing.getSecretKey(), string.toCharArray()));
            pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketGeneratorExtended.generate());
            pGPSignatureGenerator.setUnhashedSubpackets(pGPSignatureSubpacketGeneratorExtended2.generate());
            pGPSignature = pGPSignatureGenerator.generateCertification((String)pGPPublicKeyRing.getPublicKey().getUserIDs().next(), pGPPublicKeyRing.getPublicKey());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        pGPSignatureGenerator = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKeyRing.getPublicKey(), (String)((String)pGPPublicKeyRing.getPublicKey().getUserIDs().next()), (PGPSignature)pGPSignature);
        pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPSignatureGenerator);
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPSignatureGenerator);
        return pGPPublicKeyRing;
    }

    private PGPPublicKeyRing revokeWithDisignatedRevokerHelper(PGPPublicKeyRing pGPPublicKeyRing, PGPSecretKeyRing pGPSecretKeyRing, String string, byte by, String string2) throws PGPException {
        Object object;
        SignatureSubpacket signatureSubpacket;
        Object object2;
        Object object3;
        boolean bl = false;
        Iterator iterator = pGPPublicKeyRing.getPublicKey().getSignaturesOfType(31);
        while (iterator.hasNext()) {
            IssuerKeyID issuerKeyID;
            SignatureSubpacket signatureSubpacket2;
            object3 = (PGPSignature)iterator.next();
            object2 = object3.getHashedSubPackets();
            signatureSubpacket = object2.getSubpacket(12);
            if (signatureSubpacket != null && Arrays.areEqual((byte[])((RevocationKey)((Object)(object = new RevocationKey(signatureSubpacket.isCritical(), signatureSubpacket.getData())))).getFingerprint(), (byte[])pGPSecretKeyRing.getPublicKey().getFingerprint())) {
                bl = true;
            }
            if ((signatureSubpacket2 = (object = object3.getUnhashedSubPackets()).getSubpacket(16)) != null && (issuerKeyID = new IssuerKeyID(signatureSubpacket2.isCritical(), false, signatureSubpacket2.getData())).getKeyID() == pGPPublicKeyRing.getPublicKey().getKeyID()) {
                boolean bl2 = bl = bl;
            }
            if (!bl) continue;
            break;
        }
        if (!bl) {
            throw new PGPException("Target key has no designated revoker signature with fingerprint: " + new String(Hex.encode((byte[])pGPSecretKeyRing.getPublicKey().getFingerprint())));
        }
        object3 = new PGPSignatureSubpacketGeneratorExtended();
        ((PGPSignatureSubpacketGeneratorExtended)((Object)object3)).setSignatureCreationTime(false, new Date());
        ((PGPSignatureSubpacketGeneratorExtended)((Object)object3)).setRevocationReason(false, by, string2);
        object2 = new PGPSignatureSubpacketGeneratorExtended();
        ((PGPSignatureSubpacketGeneratorExtended)((Object)object2)).setIssuerKeyID(false, pGPSecretKeyRing.getPublicKey().getKeyID());
        signatureSubpacket = null;
        try {
            object = staticBCFactory.CreatePGPSignatureGenerator(pGPSecretKeyRing.getPublicKey().getAlgorithm(), 2);
            staticBCFactory.initSign((PGPSignatureGenerator)object, 32, RevocationLib.extractPrivateKey(pGPSecretKeyRing.getSecretKey(), string));
            object.setHashedSubpackets(((PGPSignatureSubpacketGeneratorExtended)((Object)object3)).generate());
            object.setUnhashedSubpackets(((PGPSignatureSubpacketGeneratorExtended)((Object)object2)).generate());
            signatureSubpacket = object.generateCertification(pGPPublicKeyRing.getPublicKey());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        object = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKeyRing.getPublicKey(), (PGPSignature)signatureSubpacket);
        pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKeyRing.getPublicKey());
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)object);
        return pGPPublicKeyRing;
    }

    private void Debug(String string) {
        if (this.log.isLoggable(Level.FINE)) {
            this.log.fine(string);
        }
    }

    private void Debug(String string, String string2) {
        if (this.log.isLoggable(Level.FINE)) {
            this.log.fine(MessageFormat.format(string, string2));
        }
    }
}

