/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.BCPGKey
 *  lw.bouncycastle.bcpg.PublicKeyPacket
 *  lw.bouncycastle.bcpg.RSAPublicBCPGKey
 *  lw.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPPrivateKey
 *  lw.bouncycastle.openpgp.PGPPublicKey
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKey
 *  lw.bouncycastle.util.encoders.Hex
 */
package com.didisoft.pgp.smartcard;

import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.bc.BCFactory;
import com.didisoft.pgp.bc.BaseLib;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.exceptions.NoPrivateKeyFoundException;
import com.didisoft.pgp.exceptions.NoPublicKeyFoundException;
import com.didisoft.pgp.exceptions.WrongPasswordException;
import com.didisoft.pgp.smartcard.SmartcardException;
import com.didisoft.pgp.smartcard.SmartcardKeyType;
import com.didisoft.pgp.smartcard.SmartcardPrivateKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import lw.bouncycastle.bcpg.BCPGKey;
import lw.bouncycastle.bcpg.PublicKeyPacket;
import lw.bouncycastle.bcpg.RSAPublicBCPGKey;
import lw.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import lw.bouncycastle.openpgp.PGPPrivateKey;
import lw.bouncycastle.openpgp.PGPPublicKey;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKey;
import lw.bouncycastle.util.encoders.Hex;

public class SmartcardKeyStore {
    protected static BCFactory staticBCFactory = new BCFactory(false);
    private Card pcReader;
    private CommandAPDU apduCmd;
    private ResponseAPDU apduResp;
    private String currentReader;
    private String password;
    private byte[] app_id = new byte[0];
    private String version = "";
    private int manufacturer = 0;
    private byte[] serial = new byte[4];
    private String asciiVersionHeader = BaseLib.version;
    private String asciiCommentHeader = null;
    byte[] fp_sig_key = new byte[20];
    byte[] fp_aut_key = new byte[20];
    byte[] fp_dec_key = new byte[20];
    private byte pw1Status = 0;
    private byte pw1MaxLen = 0;
    private byte rcMaxLen = 0;
    private byte pw3MaxLen = 0;
    private byte pw1RetriesCount = 0;
    private byte rcRetriesCount = 0;
    private byte pw3RetriesCount = 0;
    private static final int MAX_PACKET_LENGTH = 254;
    private static final int SW_REFERENCED_DATA_NOT_FOUND = 27272;
    private static final int SW_SECURITY_STATUS_NOT_SATISFIED = 27010;
    private static final int SW_BYTES_REMAINING_00 = 24832;
    private static final int SW_WRONG_LENGTH = 26368;
    private static final int SW_FILE_INVALID = 27011;
    private static final int SW_DATA_INVALID = 27012;
    private static final int SW_CONDITIONS_NOT_SATISFIED = 27013;
    private static final int SW_COMMAND_NOT_ALLOWED = 27014;
    private static final int SW_APPLET_SELECT_FAILED = 27033;
    private static final int SW_WRONG_DATA = 26368;
    private static final int SW_FUNC_NOT_SUPPORTED = 27265;
    private static final int SW_FILE_NOT_FOUND = 27266;
    private static final int SW_RECORD_NOT_FOUND = 27267;
    private static final int SW_INCORRECT_P1P2 = 27270;
    private static final int SW_WRONG_P1P2 = 27392;
    private static final int SW_CORRECT_LENGTH_00 = 27648;
    private static final int SW_INS_NOT_SUPPORTED = 27904;
    private static final int SW_CLA_NOT_SUPPORTED = 28160;
    private static final int SW_UNKNOWN = 28416;
    private static final int SW_FILE_FULL = 27268;
    private static final int SW_LOGICAL_CHANNEL_NOT_SUPPORTED = 26753;
    private static final int SW_SECURE_MESSAGING_NOT_SUPPORTED = 26754;
    private static final int SW_WARNING_STATE_UNCHANGED = 25088;
    private static final int SW_SUCCESS = 36864;

    protected void finalize() throws Throwable {
        try {
            this.pcReader.disconnect(false);
        }
        finally {
            super.finalize();
        }
    }

    public String getCardName() {
        return this.currentReader;
    }

    public int getPin1RetriesCount() {
        return this.pw1RetriesCount;
    }

    public int getResetCounterRetriesCount() {
        return this.rcRetriesCount;
    }

    public int getPin3RetriesCount() {
        return this.pw3RetriesCount;
    }

    public String getApplicationId() {
        return new String(Hex.encode((byte[])this.app_id));
    }

    public String getApplicationVersion() {
        return this.version;
    }

    public String getCardManufacturer() {
        switch (this.manufacturer) {
            case 2: {
                return "Prism";
            }
            case 3: {
                return "OpenFortress";
            }
            case 4: {
                return "Wewid";
            }
            case 5: {
                return "ZeitControl";
            }
            case 6: {
                return "Yubico";
            }
            case 7: {
                return "OpenKMS";
            }
            case 8: {
                return "LogoEmail";
            }
            case 9: {
                return "Fidesmo";
            }
            case 10: {
                return "Dangerous Things";
            }
            case 11: {
                return "Feitian Technologies";
            }
            case 42: {
                return "Magrathea";
            }
            case 66: {
                return "GnuPG e.V.";
            }
            case 4919: {
                return "Warsaw Hackerspace";
            }
            case 9026: {
                return "warpzone";
            }
            case 17236: {
                return "Confidential Technologies";
            }
            case 25519: {
                return "Trustica";
            }
            case 48398: {
                return "Paranoidlabs";
            }
            case 62743: {
                return "FSIJ";
            }
            case 0: 
            case 65535: {
                return "test card";
            }
        }
        return "Unknown";
    }

    public String getSerialNumber() {
        return new String(Hex.encode((byte[])this.serial));
    }

    public static String[] getCards() throws SmartcardException {
        try {
            TerminalFactory terminalFactory = TerminalFactory.getDefault();
            CardTerminals cardTerminals = terminalFactory.terminals();
            List<CardTerminal> list = cardTerminals.list();
            String[] stringArray = new String[list.size()];
            for (int i = 0; i < stringArray.length; ++i) {
                stringArray[i] = list.get(i).getName();
            }
            return stringArray;
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
    }

    public static SmartcardKeyStore openDefaultSmartcard(String string) throws SmartcardException, WrongPasswordException {
        TerminalFactory terminalFactory = TerminalFactory.getDefault();
        CardTerminals cardTerminals = terminalFactory.terminals();
        try {
            if (terminalFactory.terminals().list().size() > 0) {
                List<CardTerminal> list = cardTerminals.list();
                return new SmartcardKeyStore(list.get(0).getName(), string);
            }
            throw new SmartcardException("No smart card is available! Check that a card is inserted in the slot.");
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
    }

    public static SmartcardKeyStore openSmartcard(String string, String string2) throws SmartcardException, WrongPasswordException {
        return new SmartcardKeyStore(string, string2);
    }

    public SmartcardKeyStore(String string, String string2) throws SmartcardException, WrongPasswordException {
        try {
            this.password = string2;
            this.currentReader = this.formatReaderFullName(string);
            TerminalFactory terminalFactory = TerminalFactory.getDefault();
            CardTerminals cardTerminals = terminalFactory.terminals();
            this.pcReader = cardTerminals.getTerminal(string).connect("T=0");
            CardChannel cardChannel = this.pcReader.getBasicChannel();
            this.CardSelectOpenPGPApplet(cardChannel);
            this.CardLoginPIN1(cardChannel);
            this.apduCmd = new CommandAPDU(0, -54, 0, 110, new byte[0], 255);
            this.apduResp = cardChannel.transmit(this.apduCmd);
            if (this.apduResp.getSW() != 36864) {
                throw new SmartcardException("Cannot load keys data.", this.apduResp.getSW());
            }
            this.loadApplicationData(this.apduResp.getData());
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
    }

    public SmartcardPrivateKey getSigningKey() {
        byte[] byArray = this.fp_sig_key;
        if (byArray[0] == 0 && byArray[1] == 0 && byArray[2] == 0 && byArray[3] == 0 && byArray[4] == 0 && byArray[5] == 0 && byArray[6] == 0 && byArray[7] == 0 && byArray[8] == 0 && byArray[9] == 0 && byArray[10] == 0 && byArray[11] == 0 && byArray[12] == 0 && byArray[13] == 0 && byArray[14] == 0 && byArray[15] == 0 && byArray[16] == 0 && byArray[17] == 0 && byArray[18] == 0 && byArray[19] == 0) {
            return null;
        }
        return new SmartcardPrivateKey(this, this.fp_sig_key);
    }

    public SmartcardPrivateKey getAuthenticationKey() {
        byte[] byArray = this.fp_aut_key;
        if (byArray[0] == 0 && byArray[1] == 0 && byArray[2] == 0 && byArray[3] == 0 && byArray[4] == 0 && byArray[5] == 0 && byArray[6] == 0 && byArray[7] == 0 && byArray[8] == 0 && byArray[9] == 0 && byArray[10] == 0 && byArray[11] == 0 && byArray[12] == 0 && byArray[13] == 0 && byArray[14] == 0 && byArray[15] == 0 && byArray[16] == 0 && byArray[17] == 0 && byArray[18] == 0 && byArray[19] == 0) {
            return null;
        }
        return new SmartcardPrivateKey(this, this.fp_aut_key);
    }

    public SmartcardPrivateKey getDecryptionKey() {
        byte[] byArray = this.fp_dec_key;
        if (byArray[0] == 0 && byArray[1] == 0 && byArray[2] == 0 && byArray[3] == 0 && byArray[4] == 0 && byArray[5] == 0 && byArray[6] == 0 && byArray[7] == 0 && byArray[8] == 0 && byArray[9] == 0 && byArray[10] == 0 && byArray[11] == 0 && byArray[12] == 0 && byArray[13] == 0 && byArray[14] == 0 && byArray[15] == 0 && byArray[16] == 0 && byArray[17] == 0 && byArray[18] == 0 && byArray[19] == 0) {
            return null;
        }
        return new SmartcardPrivateKey(this, this.fp_dec_key);
    }

    private void loadApplicationData(byte[] byArray) {
        byte by = 0;
        int n = 0;
        while (byArray[n++] != 79) {
        }
        by = byArray[n++];
        this.app_id = new byte[by];
        System.arraycopy(byArray, n, this.app_id, 0, by);
        n += by;
        this.version = String.format("%1$s.%2$s", this.app_id[6], this.app_id[7]);
        this.manufacturer = this.app_id[8] << 8 | this.app_id[9];
        System.arraycopy(this.app_id, 10, this.serial, 0, 4);
        while (byArray[n++] != 95 || byArray[n++] != 82) {
        }
        while (byArray[n++] != -60 || byArray[n++] != 7) {
        }
        this.pw1Status = byArray[n++];
        this.pw1MaxLen = byArray[n++];
        this.rcMaxLen = byArray[n++];
        this.pw3MaxLen = byArray[n++];
        this.pw1RetriesCount = byArray[n++];
        this.rcRetriesCount = byArray[n++];
        this.pw3RetriesCount = byArray[n++];
        while (byArray[n++] != -59 || byArray[n++] != 60) {
        }
        System.arraycopy(byArray, n, this.fp_sig_key, 0, this.fp_sig_key.length);
        System.arraycopy(byArray, n += this.fp_sig_key.length, this.fp_dec_key, 0, this.fp_dec_key.length);
        System.arraycopy(byArray, n += this.fp_dec_key.length, this.fp_aut_key, 0, this.fp_aut_key.length);
    }

    public KeyPairInformation getKey(SmartcardKeyType smartcardKeyType) throws SmartcardException, PGPException {
        SmartcardPrivateKey smartcardPrivateKey = null;
        switch (smartcardKeyType) {
            case AuthenticationKey: {
                smartcardPrivateKey = this.getAuthenticationKey();
                break;
            }
            case DecryptionKey: {
                smartcardPrivateKey = this.getDecryptionKey();
                break;
            }
            case SignatureKey: {
                smartcardPrivateKey = this.getSigningKey();
                break;
            }
        }
        if (smartcardPrivateKey == null) {
            return null;
        }
        PGPPublicKey pGPPublicKey = this.getPublicKey(smartcardKeyType);
        try {
            return new KeyPairInformation(pGPPublicKey.getEncoded());
        }
        catch (IOException iOException) {
            throw new SmartcardException(iOException.getMessage(), iOException);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] formatRsaKeyImportData(PGPPrivateKey pGPPrivateKey, SmartcardKeyType smartcardKeyType) {
        RSAPrivateCrtKeyParameters rSAPrivateCrtKeyParameters = (RSAPrivateCrtKeyParameters)pGPPrivateKey.getPrivateKeyDataPacket();
        byte[] byArray = new byte[]{};
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.AddDataLengthTag(byteArrayOutputStream, new byte[]{-111}, rSAPrivateCrtKeyParameters.getPublicExponent().toByteArray());
        this.AddDataLengthTag(byteArrayOutputStream, new byte[]{-110}, rSAPrivateCrtKeyParameters.getP().toByteArray());
        this.AddDataLengthTag(byteArrayOutputStream, new byte[]{-109}, rSAPrivateCrtKeyParameters.getQ().toByteArray());
        byte[] byArray2 = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.reset();
        this.AddDataLengthTag(byteArrayOutputStream, new byte[]{127, 72}, byArray2);
        byteArrayOutputStream.write(byArray2, 0, byArray2.length);
        byArray = byteArrayOutputStream.toByteArray();
        byte[] byArray3 = new byte[]{};
        byteArrayOutputStream.reset();
        byte[] byArray4 = new byte[rSAPrivateCrtKeyParameters.getPublicExponent().toByteArray().length + rSAPrivateCrtKeyParameters.getP().toByteArray().length + rSAPrivateCrtKeyParameters.getQ().toByteArray().length];
        int n = 0;
        System.arraycopy(rSAPrivateCrtKeyParameters.getPublicExponent().toByteArray(), 0, byArray4, n, rSAPrivateCrtKeyParameters.getPublicExponent().toByteArray().length);
        System.arraycopy(rSAPrivateCrtKeyParameters.getP().toByteArray(), 0, byArray4, n += rSAPrivateCrtKeyParameters.getPublicExponent().toByteArray().length, rSAPrivateCrtKeyParameters.getP().toByteArray().length);
        System.arraycopy(rSAPrivateCrtKeyParameters.getQ().toByteArray(), 0, byArray4, n += rSAPrivateCrtKeyParameters.getP().toByteArray().length, rSAPrivateCrtKeyParameters.getQ().toByteArray().length);
        n += rSAPrivateCrtKeyParameters.getQ().toByteArray().length;
        this.AddDataLengthTag(byteArrayOutputStream, new byte[]{95, 72}, byArray4);
        byteArrayOutputStream.write(byArray4, 0, byArray4.length);
        byArray3 = byteArrayOutputStream.toByteArray();
        byte[] byArray5 = new byte[]{};
        byteArrayOutputStream.reset();
        byte[] byArray6 = new byte[2 + byArray.length + byArray3.length];
        byArray6[0] = smartcardKeyType.value();
        byArray6[1] = 0;
        System.arraycopy(byArray, 0, byArray6, 2, byArray.length);
        System.arraycopy(byArray3, 0, byArray6, byArray.length + 2, byArray3.length);
        this.AddDataLengthTag(byteArrayOutputStream, new byte[]{77}, byArray6);
        byteArrayOutputStream.write(byArray6, 0, byArray6.length);
        byArray5 = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.close();
        }
        catch (IOException iOException) {
        }
        finally {
            byteArrayOutputStream = null;
        }
        return byArray5;
    }

    public void importPrivateKey(InputStream inputStream, String string, int n, SmartcardKeyType smartcardKeyType, String string2) throws NoPrivateKeyFoundException, WrongPasswordException, PGPException, IOException {
        KeyStore keyStore = new KeyStore();
        KeyPairInformation keyPairInformation = keyStore.importKey(inputStream);
        this.importPrivateKey(keyPairInformation, string, n, smartcardKeyType, string2);
    }

    public void importPrivateKey(String string, String string2, int n, SmartcardKeyType smartcardKeyType, String string3) throws PGPException, IOException {
        KeyStore keyStore = new KeyStore();
        KeyPairInformation keyPairInformation = keyStore.importKey(string);
        this.importPrivateKey(keyPairInformation, string2, n, smartcardKeyType, string3);
    }

    public void importPrivateKey(KeyPairInformation keyPairInformation, String string, int n, SmartcardKeyType smartcardKeyType, String string2) throws SmartcardException, WrongPasswordException, NoPrivateKeyFoundException, PGPException {
        if (!keyPairInformation.hasPrivateKey()) {
            throw new NoPrivateKeyFoundException("There i no private key in the provided key source, only a public key");
        }
        if (keyPairInformation.getRawPrivateKeyRing().getSecretKey().getPublicKey().getAlgorithm() != 2 && keyPairInformation.getRawPrivateKeyRing().getSecretKey().getPublicKey().getAlgorithm() != 3 && keyPairInformation.getRawPrivateKeyRing().getSecretKey().getPublicKey().getAlgorithm() != 1) {
            throw new NoPrivateKeyFoundException("Only RSA keys are supported currently!");
        }
        Iterator iterator = keyPairInformation.getRawPrivateKeyRing().getSecretKeys();
        PGPSecretKey pGPSecretKey = null;
        if (n >= 0 && n <= 15) {
            for (int i = 0; i <= n; ++i) {
                if (!iterator.hasNext()) {
                    throw new NoPrivateKeyFoundException(String.format("There is no private key with index %1$s in the provided key source.", n));
                }
                if (i != n) continue;
                pGPSecretKey = (PGPSecretKey)iterator.next();
            }
        } else {
            while (iterator.hasNext()) {
                PGPSecretKey pGPSecretKey2 = (PGPSecretKey)iterator.next();
                if (pGPSecretKey2.getKeyID() != (long)n) continue;
                pGPSecretKey = pGPSecretKey2;
                break;
            }
        }
        if (pGPSecretKey == null) {
            throw new NoPrivateKeyFoundException(String.format("There is no private key with Key ID %1$s in the provided key source.", KeyPairInformation.keyIdToLongHex(n)));
        }
        PGPPrivateKey pGPPrivateKey = BaseLib.extractPrivateKey(pGPSecretKey, string);
        byte[] byArray = this.formatRsaKeyImportData(pGPPrivateKey, smartcardKeyType);
        this.InitOpenPgpCardReaderModeAdmin(string2);
        try {
            CardChannel cardChannel = this.pcReader.getBasicChannel();
            int n2 = byArray.length / 254;
            for (int i = 0; i < n2; ++i) {
                byte[] byArray2 = new byte[254];
                System.arraycopy(byArray, i * 254, byArray2, 0, byArray2.length);
                this.apduCmd = new CommandAPDU(16, 219, 63, 255, byArray2, 0);
                this.apduResp = cardChannel.transmit(this.apduCmd);
                if (this.apduResp.getSW() == 36864) continue;
                throw new SmartcardException("Import key error. ", this.apduResp.getSW());
            }
            byte[] byArray3 = new byte[byArray.length % 254];
            System.arraycopy(byArray, n2 * 254, byArray3, 0, byArray3.length);
            this.apduCmd = new CommandAPDU(0, 219, 63, 255, byArray3, -1);
            this.apduResp = cardChannel.transmit(this.apduCmd);
            if (this.apduResp.getSW() != 36864) {
                throw new SmartcardException("Import key error.", this.apduResp.getSW());
            }
            int n3 = 0;
            switch (smartcardKeyType) {
                case SignatureKey: {
                    n3 = -57;
                    break;
                }
                case DecryptionKey: {
                    n3 = -56;
                    break;
                }
                case AuthenticationKey: {
                    n3 = -55;
                    break;
                }
            }
            this.apduCmd = new CommandAPDU(0, 218, 0, n3, pGPSecretKey.getPublicKey().getFingerprint(), -1);
            this.apduResp = cardChannel.transmit(this.apduCmd);
            if (this.apduResp.getSW() != 36864) {
                throw new SmartcardException("Import key error.", this.apduResp.getSW());
            }
            int n4 = 0;
            switch (smartcardKeyType) {
                case SignatureKey: {
                    n4 = -50;
                    break;
                }
                case DecryptionKey: {
                    n4 = -49;
                    break;
                }
                case AuthenticationKey: {
                    n4 = -48;
                    break;
                }
            }
            Date date = pGPSecretKey.getPublicKey().getCreationTime();
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.clear();
            calendar.set(date.getYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds());
            long l = calendar.getTimeInMillis() / 1000L;
            byte[] byArray4 = new byte[]{(byte)(l >> 24), (byte)(l >> 16), (byte)(l >> 8), (byte)l};
            this.apduCmd = new CommandAPDU(0, 218, 0, n4, byArray4, -1);
            this.apduResp = cardChannel.transmit(this.apduCmd);
            if (this.apduResp.getSW() != 36864) {
                throw new SmartcardException("Import key error.", this.apduResp.getSW());
            }
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
    }

    public KeyPairInformation generateRsaKeyPair(SmartcardKeyType smartcardKeyType, String string) throws SmartcardException, WrongPasswordException, PGPException {
        Object object;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            object = this.pcReader.getBasicChannel();
            this.CardSelectOpenPGPApplet((CardChannel)object);
            this.CardLoginPIN3((CardChannel)object, string);
            byte by = -1;
            byte[] byArray = new byte[]{smartcardKeyType.value(), by};
            this.apduCmd = new CommandAPDU(0, 71, 128, 0, byArray, by);
            this.apduResp = ((CardChannel)object).transmit(this.apduCmd);
            if (this.apduResp.getSW() != 36864) {
                if (this.apduResp.getData() != null) {
                    byteArrayOutputStream.write(this.apduResp.getData(), 0, this.apduResp.getData().length);
                }
                while (this.apduResp.getSW1() == 97) {
                    byArray = new byte[]{};
                    this.apduCmd = new CommandAPDU(0, 192, 0, 0, byArray, this.apduResp.getSW2());
                    this.apduResp = ((CardChannel)object).transmit(this.apduCmd);
                    byteArrayOutputStream.write(this.apduResp.getData(), 0, this.apduResp.getData().length);
                }
            }
            if (this.apduResp.getSW() != 36864) {
                throw new SmartcardException("Cannot obtain public key.", this.apduResp.getSW());
            }
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
        object = byteArrayOutputStream.toByteArray();
        PGPPublicKey pGPPublicKey = this.constructPublicKeyFromBuffer((byte[])object);
        try {
            return new KeyPairInformation(pGPPublicKey.getEncoded());
        }
        catch (IOException iOException) {
            throw new SmartcardException(iOException.getLocalizedMessage(), iOException);
        }
    }

    public void exportPublicKey(OutputStream outputStream, SmartcardKeyType smartcardKeyType, boolean bl) throws SmartcardException, NoPublicKeyFoundException, PGPException, IOException {
        PGPPublicKey pGPPublicKey = this.getPublicKey(smartcardKeyType);
        if (pGPPublicKey == null) {
            throw new NoPublicKeyFoundException(String.format("No public key of type %1$s found on the smart card", smartcardKeyType.toString()));
        }
        PGPPublicKeyRing pGPPublicKeyRing = new PGPPublicKeyRing(pGPPublicKey.getEncoded(), staticBCFactory.CreateKeyFingerPrintCalculator());
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, outputStream, bl, this.asciiVersionHeader);
    }

    public void exportPublicKey(String string, SmartcardKeyType smartcardKeyType, boolean bl) throws SmartcardException, NoPublicKeyFoundException, PGPException, IOException {
        PGPPublicKey pGPPublicKey = this.getPublicKey(smartcardKeyType);
        if (pGPPublicKey == null) {
            throw new NoPublicKeyFoundException(String.format("No public key of type %1$s found on the smart card", smartcardKeyType.toString()));
        }
        PGPPublicKeyRing pGPPublicKeyRing = new PGPPublicKeyRing(pGPPublicKey.getEncoded(), staticBCFactory.CreateKeyFingerPrintCalculator());
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, string, bl, this.asciiVersionHeader);
    }

    PGPPublicKey getPublicKey(SmartcardKeyType smartcardKeyType) throws SmartcardException, PGPException {
        Object object;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            this.InitOpenPgpCardReader();
            object = this.pcReader.getBasicChannel();
            byte by = -1;
            byte[] byArray = new byte[]{smartcardKeyType.value(), by};
            this.apduCmd = new CommandAPDU(0, 71, 129, 0, byArray, by);
            this.apduResp = ((CardChannel)object).transmit(this.apduCmd);
            if (this.apduResp.getSW() != 36864) {
                if (this.apduResp.getData() != null) {
                    byteArrayOutputStream.write(this.apduResp.getData(), 0, this.apduResp.getData().length);
                }
                while (this.apduResp.getSW1() == 97) {
                    byArray = new byte[]{};
                    this.apduCmd = new CommandAPDU(0, 192, 0, 0, byArray, this.apduResp.getSW2());
                    this.apduResp = ((CardChannel)object).transmit(this.apduCmd);
                    byteArrayOutputStream.write(this.apduResp.getData(), 0, this.apduResp.getData().length);
                }
            }
            if (this.apduResp.getSW() != 36864) {
                throw new SmartcardException("Cannot obtain public key.", this.apduResp.getSW());
            }
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
        object = byteArrayOutputStream.toByteArray();
        return this.constructPublicKeyFromBuffer((byte[])object);
    }

    private PGPPublicKey constructPublicKeyFromBuffer(byte[] byArray) throws SmartcardException, PGPException {
        int n;
        int n2 = 0;
        while (byArray[n2++] != 127 || byArray[n2++] != 73 || byArray[n2++] != -126) {
        }
        int n3 = 0;
        while (byArray[n2++] != 129) {
        }
        if (byArray[n2] == 129) {
            int n4 = ++n2;
            ++n2;
            n3 = byArray[n4];
        } else if (byArray[n2] == 130) {
            int n5 = ++n2;
            int n6 = ++n2;
            ++n2;
            n3 = ((short)byArray[n5] << 8) + (short)byArray[n6];
        } else {
            throw new SmartcardException("Unexpected public key modulus encoding " + new String(Hex.encode((byte[])byArray)));
        }
        byte[] byArray2 = new byte[n3 + 1];
        for (n = 1; n < n3 + 1; ++n) {
            byArray2[n] = byArray[n2++];
        }
        byArray2[0] = 0;
        if (byArray[n2++] != 130) {
            throw new SmartcardException("Unexpected public key exponent encoding " + new String(Hex.encode((byte[])byArray)));
        }
        n = byArray[n2++];
        byte[] byArray3 = new byte[n];
        for (int i = 0; i < n; ++i) {
            byArray3[i] = byArray[n2++];
        }
        BigInteger bigInteger = new BigInteger(byArray2);
        BigInteger bigInteger2 = new BigInteger(byArray3);
        PublicKeyPacket publicKeyPacket = new PublicKeyPacket(1, new Date(), (BCPGKey)new RSAPublicBCPGKey(bigInteger, bigInteger2));
        try {
            return new PGPPublicKey(publicKeyPacket, staticBCFactory.CreateKeyFingerPrintCalculator());
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    private byte[] FixLen(byte[] byArray) {
        int n = -1;
        int n2 = byArray.length;
        n = n2 >= 112 && n2 < 128 ? 128 - n2 : (n2 >= 176 && n2 < 192 ? 192 - n2 : (n2 >= 240 && n2 < 256 ? 256 - n2 : (n2 >= 368 && n2 < 384 ? 384 - n2 : (n2 >= 496 && n2 < 512 ? 512 - n2 : (byArray[0] != 0 && (n2 == 129 || n2 == 193 || n2 == 257 || n2 == 385 || n2 == 513) ? -1 : 0)))));
        if (n > 0) {
            byte[] byArray2 = new byte[++n + n2 + 1];
            for (int i = 0; i < n; ++i) {
                byArray2[i] = 0;
            }
            System.arraycopy(byArray, 0, byArray2, n, n2);
            byArray = byArray2;
        } else {
            byte[] byArray3 = new byte[n2 + 1];
            byArray3[0] = 0;
            System.arraycopy(byArray, 0, byArray3, 1, n2);
            byArray = byArray3;
        }
        return byArray;
    }

    byte[] decipher(byte[] byArray) throws SmartcardException {
        this.InitOpenPgpCardReaderMode82();
        byArray = this.FixLen(byArray);
        try {
            CardChannel cardChannel = this.pcReader.getBasicChannel();
            int n = byArray.length / 254;
            for (int i = 0; i < n; ++i) {
                byte[] byArray2 = new byte[254];
                System.arraycopy(byArray, i * 254, byArray2, 0, byArray2.length);
                this.apduCmd = new CommandAPDU(16, 42, 128, 134, byArray2, 0);
                this.apduResp = cardChannel.transmit(this.apduCmd);
                if (this.apduResp.getSW() == 36864) continue;
                throw new SmartcardException("Decipher operation error. ", this.apduResp.getSW());
            }
            byte[] byArray3 = new byte[byArray.length % 254];
            System.arraycopy(byArray, n * 254, byArray3, 0, byArray3.length);
            this.apduCmd = new CommandAPDU(0, 42, 128, 134, byArray3, -1);
            this.apduResp = cardChannel.transmit(this.apduCmd);
            if (this.apduResp.getSW() != 36864) {
                throw new SmartcardException("Decipher operation error.", this.apduResp.getSW());
            }
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
        return this.apduResp.getData();
    }

    byte[] generateSignature(byte[] byArray) throws SmartcardException {
        this.InitOpenPgpCardReader();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            CardChannel cardChannel = this.pcReader.getBasicChannel();
            this.apduCmd = new CommandAPDU(0, 42, 158, 154, byArray, -1);
            this.apduResp = cardChannel.transmit(this.apduCmd);
            byteArrayOutputStream.write(this.apduResp.getData(), 0, this.apduResp.getData().length);
            while (this.apduResp.getSW1() == 97) {
                byte[] byArray2 = new byte[]{};
                this.apduCmd = new CommandAPDU(0, 192, 0, 0, byArray2, this.apduResp.getSW2());
                this.apduResp = cardChannel.transmit(this.apduCmd);
                byteArrayOutputStream.write(this.apduResp.getData(), 0, this.apduResp.getData().length);
            }
            if (this.apduResp.getSW() != 36864) {
                throw new SmartcardException("Sign operation error", this.apduResp.getSW());
            }
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void CardSelectOpenPGPApplet(CardChannel cardChannel) throws CardException, SmartcardException {
        this.apduCmd = new CommandAPDU(0, -92, 4, 0, new byte[]{-46, 118, 0, 1, 36, 1}, 0);
        this.apduResp = cardChannel.transmit(this.apduCmd);
        if (this.apduResp.getSW() != 36864) {
            throw new SmartcardException("Cannot select OpenPGP applet. ", this.apduResp.getSW());
        }
    }

    private byte[] getPasswordBytes(String string) throws WrongPasswordException {
        if (string == null) {
            return new byte[0];
        }
        try {
            return string.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            throw new WrongPasswordException(unsupportedEncodingException.getMessage());
        }
    }

    private void CardLoginPIN1(CardChannel cardChannel) throws CardException, WrongPasswordException {
        this.apduCmd = new CommandAPDU(0, 32, 0, 129, this.getPasswordBytes(this.password), 0);
        this.apduResp = cardChannel.transmit(this.apduCmd);
        if (this.apduResp.getSW() != 36864) {
            throw new WrongPasswordException(SmartcardException.formatError("Wrong smartcard password (PIN1).", this.apduResp.getSW()));
        }
    }

    private void CardLoginPIN2(CardChannel cardChannel) throws CardException, WrongPasswordException {
        this.apduCmd = new CommandAPDU(0, 32, 0, 130, this.getPasswordBytes(this.password), 0);
        this.apduResp = cardChannel.transmit(this.apduCmd);
        if (this.apduResp.getSW() != 36864) {
            throw new WrongPasswordException(SmartcardException.formatError("Wrong smartcard password (PIN1).", this.apduResp.getSW()));
        }
    }

    private void CardLoginPIN3(CardChannel cardChannel, String string) throws CardException, WrongPasswordException {
        this.apduCmd = new CommandAPDU(0, 32, 0, 131, this.getPasswordBytes(string), 0);
        this.apduResp = cardChannel.transmit(this.apduCmd);
        if (this.apduResp.getSW() != 36864) {
            throw new WrongPasswordException(SmartcardException.formatError("Wrong smartcard password (PIN1).", this.apduResp.getSW()));
        }
    }

    private void InitOpenPgpCardReader() throws SmartcardException {
        try {
            CardChannel cardChannel = this.pcReader.getBasicChannel();
            this.CardSelectOpenPGPApplet(cardChannel);
            this.CardLoginPIN1(cardChannel);
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
        catch (WrongPasswordException wrongPasswordException) {
            throw new SmartcardException(wrongPasswordException.getMessage(), (Exception)((Object)wrongPasswordException));
        }
    }

    private void InitOpenPgpCardReaderMode82() throws SmartcardException {
        try {
            CardChannel cardChannel = this.pcReader.getBasicChannel();
            this.CardSelectOpenPGPApplet(cardChannel);
            this.CardLoginPIN2(cardChannel);
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
        catch (WrongPasswordException wrongPasswordException) {
            throw new SmartcardException(wrongPasswordException.getMessage(), (Exception)((Object)wrongPasswordException));
        }
    }

    private void InitOpenPgpCardReaderModeAdmin(String string) throws SmartcardException, WrongPasswordException {
        try {
            CardChannel cardChannel = this.pcReader.getBasicChannel();
            this.CardSelectOpenPGPApplet(cardChannel);
            this.CardLoginPIN3(cardChannel, string);
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
    }

    private String formatReaderFullName(String string) throws SmartcardException {
        TerminalFactory terminalFactory = TerminalFactory.getDefault();
        CardTerminals cardTerminals = terminalFactory.terminals();
        try {
            String[] stringArray;
            List<CardTerminal> list = cardTerminals.list();
            for (String string2 : stringArray = new String[list.size()]) {
                if (!string2.toLowerCase().contains(string.toLowerCase())) continue;
                return string2;
            }
            throw new SmartcardException(String.format("Smart card %1$s not available! Check is the card inserted in the slot.", string));
        }
        catch (CardException cardException) {
            throw new SmartcardException(cardException.getMessage(), cardException);
        }
    }

    private void AddDataLengthTag(ByteArrayOutputStream byteArrayOutputStream, byte[] byArray, byte[] byArray2) {
        byteArrayOutputStream.write(byArray, 0, byArray.length);
        if (byArray2.length < 128) {
            byteArrayOutputStream.write((byte)byArray2.length);
        } else if (byArray2.length < 256) {
            byteArrayOutputStream.write(-127);
            byteArrayOutputStream.write((byte)byArray2.length);
        } else {
            byteArrayOutputStream.write(-126);
            byteArrayOutputStream.write((byte)(byArray2.length >> 8));
            byteArrayOutputStream.write((byte)(byArray2.length & 0xFF));
        }
    }
}

