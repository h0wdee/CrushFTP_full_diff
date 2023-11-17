/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.ArmoredOutputStream
 *  lw.bouncycastle.bcpg.BCPGKey
 *  lw.bouncycastle.bcpg.ECPublicBCPGKey
 *  lw.bouncycastle.bcpg.PublicKeyPacket
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPPublicKey
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKey
 *  lw.bouncycastle.openpgp.PGPSecretKeyRing
 *  lw.bouncycastle.openpgp.PGPSignature
 *  lw.bouncycastle.util.encoders.Hex
 */
package com.didisoft.pgp;

import com.didisoft.pgp.EcCurve;
import com.didisoft.pgp.KeyAlgorithm;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.bc.BCFactory;
import com.didisoft.pgp.bc.BaseLib;
import com.didisoft.pgp.bc.DirectByteArrayOutputStream;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.bc.ReflectionUtils;
import com.didisoft.pgp.exceptions.NoPrivateKeyFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import lw.bouncycastle.bcpg.ArmoredOutputStream;
import lw.bouncycastle.bcpg.BCPGKey;
import lw.bouncycastle.bcpg.ECPublicBCPGKey;
import lw.bouncycastle.bcpg.PublicKeyPacket;
import lw.bouncycastle.openpgp.PGPPublicKey;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKey;
import lw.bouncycastle.openpgp.PGPSecretKeyRing;
import lw.bouncycastle.openpgp.PGPSignature;
import lw.bouncycastle.util.encoders.Hex;

public class KeyPairInformation
implements Serializable {
    private static final long serialVersionUID = -4750279241178352641L;
    BCFactory bcFactory = new BCFactory(false);
    private PGPPublicKeyRing publicKeyRing;
    private PGPSecretKeyRing privateKeyRing;
    private String publicAsString;
    private String privateAsString;
    private long keyID;
    private String keyIDHex;
    private String keyIDLongHex;
    private String fingerprint;
    private String[] userIDs = new String[0];
    private int keySize = 0;
    private int encryptionKeySize = 0;
    private String algorithm;
    private KeyAlgorithm.Enum algorithmType;
    private Date creationTime;
    private int validDays;
    private long validSeconds;
    private int version;
    private boolean revoked;
    private boolean encryptionKey;
    private boolean signingKey;
    private int[] compressions = new int[0];
    private int[] ciphers = new int[0];
    private int[] hashes = new int[0];
    private long[] sigIds = new long[0];
    List privateSubKeys = new ArrayList();
    List publicSubKeys = new ArrayList();
    protected String asciiVersionHeader = null;

    KeyPairInformation() {
        ArmoredOutputStream armoredOutputStream = new ArmoredOutputStream((OutputStream)new ByteArrayOutputStream());
        this.asciiVersionHeader = (String)ReflectionUtils.getPrivateFieldvalue(armoredOutputStream, "version");
    }

    KeyPairInformation(String string) {
        this.asciiVersionHeader = string;
    }

    KeyPairInformation(PGPPublicKeyRing pGPPublicKeyRing) {
        this.setPublicKeyRing(pGPPublicKeyRing);
    }

    public KeyPairInformation(byte[] byArray) throws IOException {
        try {
            List list = BaseLib.loadKeyStream(new ByteArrayInputStream(byArray));
            for (int i = 0; i < list.size(); ++i) {
                PGPPublicKeyRing pGPPublicKeyRing;
                Object e = list.get(i);
                if (e instanceof PGPPublicKeyRing) {
                    pGPPublicKeyRing = (PGPPublicKeyRing)e;
                    this.setPublicKeyRing(pGPPublicKeyRing);
                    continue;
                }
                if (!(e instanceof PGPSecretKeyRing)) continue;
                pGPPublicKeyRing = (PGPSecretKeyRing)e;
                this.setPrivateKeyRing((PGPSecretKeyRing)pGPPublicKeyRing);
            }
        }
        catch (PGPException pGPException) {
            throw new IOException(pGPException.getMessage(), pGPException.getUnderlyingException());
        }
    }

    public PGPPublicKeyRing getPublicKeyRing() {
        return this.publicKeyRing;
    }

    public void setPublicKeyRing(PGPPublicKeyRing pGPPublicKeyRing) {
        Object object;
        this.publicKeyRing = pGPPublicKeyRing;
        PGPPublicKey pGPPublicKey = this.getMasterKey(pGPPublicKeyRing);
        this.keyID = pGPPublicKey.getKeyID();
        this.keyIDHex = KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID());
        this.keyIDLongHex = KeyPairInformation.keyIdToLongHex(pGPPublicKey.getKeyID());
        this.fingerprint = new String(Hex.encode((byte[])pGPPublicKey.getFingerprint()));
        ArrayList<String> arrayList = new ArrayList<String>();
        Iterator iterator = pGPPublicKey.getRawUserIDs();
        while (iterator.hasNext()) {
            arrayList.add(BaseLib.toUserID((byte[])iterator.next()));
        }
        this.userIDs = arrayList.toArray(new String[arrayList.size()]);
        this.creationTime = pGPPublicKey.getCreationTime();
        this.keySize = pGPPublicKey.getBitStrength();
        this.algorithm = KeyStore.getKeyAlgorithm(pGPPublicKey.getAlgorithm());
        this.algorithmType = KeyStore.getKeyAlgorithmType(pGPPublicKey.getAlgorithm());
        this.validDays = pGPPublicKey.getValidDays();
        this.validSeconds = pGPPublicKey.getValidSeconds();
        this.version = pGPPublicKey.getVersion();
        this.revoked = pGPPublicKey.isRevoked();
        this.encryptionKey = pGPPublicKey.isEncryptionKey();
        this.signingKey = BaseLib.isForVerification(pGPPublicKey);
        iterator = pGPPublicKeyRing.getPublicKeys();
        while (iterator.hasNext()) {
            object = (PGPPublicKey)iterator.next();
            if (object.isEncryptionKey()) {
                this.encryptionKeySize = object.getBitStrength();
            }
            if (object.isMasterKey()) continue;
            SubKey subKey = new SubKey((PGPPublicKey)object);
            this.publicSubKeys.add(subKey);
        }
        if (this.getEncryptionKeySize() == 0 && pGPPublicKeyRing.getPublicKey().isEncryptionKey()) {
            this.encryptionKeySize = pGPPublicKeyRing.getPublicKey().getBitStrength();
        }
        this.initCiphers();
        this.initCompressions();
        this.initHashes();
        this.initSignatures();
        object = new DirectByteArrayOutputStream(4096);
        try {
            this.exportPublicKey((OutputStream)object, true);
            this.publicAsString = new String(((ByteArrayOutputStream)object).toByteArray());
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void initCompressions() {
        Iterator iterator = this.publicKeyRing.getPublicKey().getSignatures();
        while (iterator.hasNext()) {
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getHashedSubPackets() == null || pGPSignature.getHashedSubPackets().getPreferredCompressionAlgorithms() == null) continue;
            int[] nArray = pGPSignature.getHashedSubPackets().getPreferredCompressionAlgorithms();
            this.compressions = nArray;
        }
    }

    private void initHashes() {
        Iterator iterator = this.publicKeyRing.getPublicKey().getSignatures();
        while (iterator.hasNext()) {
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getHashedSubPackets() == null || pGPSignature.getHashedSubPackets().getPreferredHashAlgorithms() == null) continue;
            int[] nArray = pGPSignature.getHashedSubPackets().getPreferredHashAlgorithms();
            this.hashes = nArray;
        }
    }

    private void initCiphers() {
        Iterator iterator = this.publicKeyRing.getPublicKey().getSignatures();
        while (iterator.hasNext()) {
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getHashedSubPackets() == null || pGPSignature.getHashedSubPackets().getPreferredSymmetricAlgorithms() == null) continue;
            int[] nArray = pGPSignature.getHashedSubPackets().getPreferredSymmetricAlgorithms();
            this.ciphers = nArray;
        }
    }

    private void initSignatures() {
        ArrayList<PGPSignature> arrayList = new ArrayList<PGPSignature>();
        Iterator iterator = this.publicKeyRing.getPublicKeys();
        while (iterator.hasNext()) {
            PGPSignature pGPSignature;
            PGPPublicKey pGPPublicKey = (PGPPublicKey)iterator.next();
            Iterator iterator2 = pGPPublicKey.getSignaturesOfType(16);
            while (iterator2.hasNext()) {
                pGPSignature = (PGPSignature)iterator2.next();
                if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID()) continue;
                arrayList.add(pGPSignature);
            }
            iterator2 = pGPPublicKey.getSignaturesOfType(18);
            while (iterator2.hasNext()) {
                pGPSignature = (PGPSignature)iterator2.next();
                if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID()) continue;
                arrayList.add(pGPSignature);
            }
            iterator2 = pGPPublicKey.getSignaturesOfType(19);
            while (iterator2.hasNext()) {
                pGPSignature = (PGPSignature)iterator2.next();
                if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID()) continue;
                arrayList.add(pGPSignature);
            }
        }
        this.sigIds = new long[arrayList.size()];
        for (int i = 0; i < arrayList.size(); ++i) {
            this.sigIds[i] = ((PGPSignature)arrayList.get(i)).getKeyID();
        }
    }

    private PGPPublicKey getMasterKey(PGPPublicKeyRing pGPPublicKeyRing) {
        Iterator iterator = pGPPublicKeyRing.getPublicKeys();
        while (iterator.hasNext()) {
            PGPPublicKey pGPPublicKey = (PGPPublicKey)iterator.next();
            if (!pGPPublicKey.isMasterKey()) continue;
            return pGPPublicKey;
        }
        return pGPPublicKeyRing.getPublicKey();
    }

    public void setPrivateKeyRing(PGPSecretKeyRing pGPSecretKeyRing) {
        Object object;
        this.privateKeyRing = pGPSecretKeyRing;
        if (pGPSecretKeyRing == null) {
            return;
        }
        Iterator iterator = pGPSecretKeyRing.getSecretKeys();
        while (iterator.hasNext()) {
            object = (PGPSecretKey)iterator.next();
            if (object.isMasterKey()) continue;
            SubKey subKey = new SubKey(object.getPublicKey());
            this.privateSubKeys.add(subKey);
        }
        object = new DirectByteArrayOutputStream(4096);
        try {
            this.exportPrivateKey((OutputStream)object, true);
            this.privateAsString = new String(((ByteArrayOutputStream)object).toByteArray());
        }
        catch (IOException iOException) {
        }
        catch (NoPrivateKeyFoundException noPrivateKeyFoundException) {
            // empty catch block
        }
    }

    public SubKey[] getPublicSubKeys() {
        return this.publicSubKeys.toArray(new SubKey[this.publicSubKeys.size()]);
    }

    public SubKey[] getPrivateSubKeys() {
        return this.privateSubKeys.toArray(new SubKey[this.privateSubKeys.size()]);
    }

    public EcCurve.Enum getEcCurve() {
        PublicKeyPacket publicKeyPacket = this.publicKeyRing.getPublicKey().getPublicKeyPacket();
        BCPGKey bCPGKey = publicKeyPacket.getKey();
        if (bCPGKey instanceof ECPublicBCPGKey) {
            ECPublicBCPGKey eCPublicBCPGKey = (ECPublicBCPGKey)bCPGKey;
            return KeyStore.calculateCurve(eCPublicBCPGKey.getCurveOID());
        }
        return EcCurve.Enum.None;
    }

    public boolean isExpired() {
        if (this.validDays <= 0) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.creationTime);
        calendar.add(5, this.validDays);
        return calendar.getTime().before(new Date());
    }

    public boolean isExpiredOnDate(Date date) {
        if (this.validDays <= 0) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.creationTime);
        calendar.add(5, this.validDays);
        return calendar.getTime().before(date);
    }

    public boolean isValidForever() {
        return this.validDays <= 0;
    }

    public Date getExpirationDate() {
        return this.getExpirationTime();
    }

    public boolean isRevoked() {
        return this.revoked;
    }

    public boolean isEncryptionKey() {
        return this.encryptionKey;
    }

    public boolean isSigningKey() {
        return this.signingKey;
    }

    public String getPublicKeyAsString() {
        return this.publicAsString;
    }

    public String getPrivateKeyAsString() {
        return this.privateAsString;
    }

    public PGPPublicKeyRing getRawPublicKeyRing() {
        return this.publicKeyRing;
    }

    public PGPSecretKeyRing getRawPrivateKeyRing() {
        return this.privateKeyRing;
    }

    public boolean hasPrivateKey() {
        return this.privateKeyRing != null;
    }

    public static String keyId2Hex(long l) {
        return KeyPairInformation.keyIdToHex(l);
    }

    public static String keyIdToHex(long l) {
        String string = Long.toHexString(l).toUpperCase();
        if (string.length() < 8) {
            return string;
        }
        return string.substring(string.length() - 8);
    }

    public static String keyIdToLongHex(long l) {
        return Long.toHexString(l).toUpperCase();
    }

    public long getKeyID() {
        return this.keyID;
    }

    public String getKeyIDHex() {
        return this.keyIDHex;
    }

    public String getKeyIDLongHex() {
        return this.keyIDLongHex;
    }

    public String getFingerprint() {
        return this.fingerprint;
    }

    public String getUserID() {
        if (this.userIDs.length > 0) {
            return this.userIDs[0];
        }
        return null;
    }

    public String[] getUserIDs() {
        return this.userIDs;
    }

    public int getKeySize() {
        return this.keySize;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public KeyAlgorithm.Enum getAlgorithmType() {
        return this.algorithmType;
    }

    public Date getCreationTime() {
        return this.creationTime;
    }

    public int getValidDays() {
        return this.validDays;
    }

    public Date getExpirationTime() {
        if (this.validDays > 0) {
            long l = this.creationTime.getTime();
            long l2 = l + this.validSeconds * 1000L;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(l2);
            return calendar.getTime();
        }
        return new Date(Long.MAX_VALUE);
    }

    public int getVersion() {
        return this.version;
    }

    public void exportPublicKey(String string, boolean bl) throws IOException {
        IOUtil.exportPublicKeyRing(this.getRawPublicKeyRing(), string, bl, this.asciiVersionHeader);
    }

    public void exportPublicKey(OutputStream outputStream, boolean bl) throws IOException {
        IOUtil.exportPublicKeyRing(this.getRawPublicKeyRing(), outputStream, bl, this.asciiVersionHeader);
    }

    public void exportPrivateKey(String string, boolean bl) throws NoPrivateKeyFoundException, IOException {
        if (this.getRawPrivateKeyRing() == null) {
            throw new NoPrivateKeyFoundException("No private key was loaded in this KeyPair object");
        }
        IOUtil.exportPrivateKey(this.getRawPrivateKeyRing(), string, bl, this.asciiVersionHeader);
    }

    public void exportPrivateKey(OutputStream outputStream, boolean bl) throws NoPrivateKeyFoundException, IOException {
        if (this.getRawPrivateKeyRing() == null) {
            throw new NoPrivateKeyFoundException("No private key was loaded in this KeyPair object");
        }
        IOUtil.exportPrivateKey(this.getRawPrivateKeyRing(), outputStream, bl, this.asciiVersionHeader);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void exportKeyRing(String string, boolean bl) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(string);
            this.exportKeyRing(fileOutputStream, bl);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileOutputStream);
    }

    public void exportKeyRing(OutputStream outputStream, boolean bl) throws IOException {
        PGPSecretKeyRing pGPSecretKeyRing;
        OutputStream outputStream2 = outputStream;
        if (bl) {
            outputStream2 = new ArmoredOutputStream(outputStream);
        }
        if ((pGPSecretKeyRing = this.getRawPrivateKeyRing()) != null) {
            pGPSecretKeyRing.encode(outputStream2);
            if (bl) {
                IOUtil.closeStream(outputStream2);
            }
        }
        if (bl) {
            outputStream2 = new ArmoredOutputStream(outputStream);
        }
        PGPPublicKeyRing pGPPublicKeyRing = this.getRawPublicKeyRing();
        pGPPublicKeyRing.encode(outputStream2);
        if (bl) {
            IOUtil.closeStream(outputStream2);
        }
    }

    public void exportKeyRing(String string) throws IOException {
        this.exportKeyRing(string, true);
    }

    public boolean checkPassword(String string) throws NoPrivateKeyFoundException {
        if (this.privateKeyRing == null) {
            throw new NoPrivateKeyFoundException("There is no private key in this key pair.");
        }
        if (string == null) {
            string = "";
        }
        try {
            BaseLib.extractPrivateKey(this.privateKeyRing.getSecretKey(), string.toCharArray());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            if (pGPException.getMessage().toLowerCase().startsWith("checksum mismatch at 0 of 2")) {
                return false;
            }
            throw new NoPrivateKeyFoundException(pGPException.getMessage(), (Exception)((Object)pGPException));
        }
        return true;
    }

    public byte getTrust() {
        if (this.hasPrivateKey()) {
            return 120;
        }
        byte[] byArray = this.publicKeyRing.getPublicKey().getTrustData();
        if (byArray != null) {
            return byArray[0];
        }
        return 0;
    }

    public int[] getPreferredCompressions() {
        return this.compressions;
    }

    public int[] getPreferredCiphers() {
        return this.ciphers;
    }

    public int[] getPreferredHashes() {
        return this.hashes;
    }

    public long[] getSignedWithKeyIds() {
        return this.sigIds;
    }

    public int getEncryptionKeySize() {
        return this.encryptionKeySize;
    }

    public class SubKey
    implements Serializable {
        private static final long serialVersionUID = -9122478708446314118L;
        private PGPPublicKey key;
        private boolean encryptionKey;
        private boolean signingKey;
        private boolean revoked;
        private long keyID;
        private String keyIDHex;
        private String fingerprint;
        private String[] userIDs;
        private int keySize;
        private String algorithm;
        private KeyAlgorithm.Enum algorithmType;
        private Date creationTime;
        private int validDays;
        private int version;

        public SubKey(PGPPublicKey pGPPublicKey) {
            this.key = pGPPublicKey;
            this.keyID = pGPPublicKey.getKeyID();
            this.keyIDHex = KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID());
            this.fingerprint = new String(Hex.encode((byte[])pGPPublicKey.getFingerprint()));
            ArrayList<String> arrayList = new ArrayList<String>();
            Iterator iterator = pGPPublicKey.getRawUserIDs();
            while (iterator.hasNext()) {
                arrayList.add(BaseLib.toUserID((byte[])iterator.next()));
            }
            this.userIDs = arrayList.toArray(new String[arrayList.size()]);
            this.creationTime = pGPPublicKey.getCreationTime();
            this.keySize = pGPPublicKey.getBitStrength();
            this.algorithm = KeyStore.getKeyAlgorithm(pGPPublicKey.getAlgorithm());
            this.algorithmType = KeyStore.getKeyAlgorithmType(pGPPublicKey.getAlgorithm());
            this.validDays = pGPPublicKey.getValidDays();
            this.version = pGPPublicKey.getVersion();
            this.revoked = pGPPublicKey.isRevoked();
            this.encryptionKey = pGPPublicKey.isEncryptionKey();
            this.signingKey = BaseLib.isForVerification(pGPPublicKey);
        }

        public boolean isEncryptionKey() {
            return this.encryptionKey;
        }

        public boolean isSigningKey() {
            return this.signingKey;
        }

        public boolean isExpired() {
            if (this.validDays <= 0) {
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.creationTime);
            calendar.add(5, this.validDays);
            return calendar.getTime().before(new Date());
        }

        public EcCurve.Enum getEcCurve() {
            PublicKeyPacket publicKeyPacket = this.key.getPublicKeyPacket();
            BCPGKey bCPGKey = publicKeyPacket.getKey();
            if (bCPGKey instanceof ECPublicBCPGKey) {
                ECPublicBCPGKey eCPublicBCPGKey = (ECPublicBCPGKey)bCPGKey;
                return KeyStore.calculateCurve(eCPublicBCPGKey.getCurveOID());
            }
            return EcCurve.Enum.None;
        }

        public boolean isExpiredOnDate(Date date) {
            if (this.validDays <= 0) {
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.creationTime);
            calendar.add(5, this.validDays);
            return calendar.getTime().before(date);
        }

        public boolean isRevoked() {
            return this.revoked;
        }

        public long getKeyID() {
            return this.keyID;
        }

        public String getKeyIDHex() {
            return this.keyIDHex;
        }

        public String getFingerprint() {
            return this.fingerprint;
        }

        public String[] getUserIDs() {
            return this.userIDs;
        }

        public int getKeySize() {
            return this.keySize;
        }

        public String getAlgorithm() {
            return this.algorithm;
        }

        public Date getCreationTime() {
            return this.creationTime;
        }

        public int getValidDays() {
            return this.validDays;
        }

        public int getVersion() {
            return this.version;
        }
    }
}

