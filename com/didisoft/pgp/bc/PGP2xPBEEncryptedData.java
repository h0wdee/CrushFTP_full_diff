/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.BCPGInputStream
 *  lw.bouncycastle.bcpg.InputStreamPacket
 *  lw.bouncycastle.crypto.BlockCipher
 *  lw.bouncycastle.crypto.BufferedBlockCipher
 *  lw.bouncycastle.crypto.CipherParameters
 *  lw.bouncycastle.crypto.digests.MD5Digest
 *  lw.bouncycastle.crypto.engines.IDEAEngine
 *  lw.bouncycastle.crypto.io.CipherInputStream
 *  lw.bouncycastle.crypto.modes.CFBBlockCipher
 *  lw.bouncycastle.crypto.params.KeyParameter
 *  lw.bouncycastle.crypto.params.ParametersWithIV
 *  lw.bouncycastle.openpgp.PGPDataValidationException
 *  lw.bouncycastle.openpgp.PGPEncryptedDataList
 *  lw.bouncycastle.openpgp.PGPException
 */
package com.didisoft.pgp.bc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lw.bouncycastle.bcpg.BCPGInputStream;
import lw.bouncycastle.bcpg.InputStreamPacket;
import lw.bouncycastle.crypto.BlockCipher;
import lw.bouncycastle.crypto.BufferedBlockCipher;
import lw.bouncycastle.crypto.CipherParameters;
import lw.bouncycastle.crypto.digests.MD5Digest;
import lw.bouncycastle.crypto.engines.IDEAEngine;
import lw.bouncycastle.crypto.io.CipherInputStream;
import lw.bouncycastle.crypto.modes.CFBBlockCipher;
import lw.bouncycastle.crypto.params.KeyParameter;
import lw.bouncycastle.crypto.params.ParametersWithIV;
import lw.bouncycastle.openpgp.PGPDataValidationException;
import lw.bouncycastle.openpgp.PGPEncryptedDataList;
import lw.bouncycastle.openpgp.PGPException;

public class PGP2xPBEEncryptedData
extends PGPEncryptedDataList {
    private byte[] randomPrefixBytes;
    private int RAND_PREFIX_LENGTH = 8;
    BCPGInputStream bcpgInput;
    private InputStreamPacket encData;
    private InputStream encStream;

    public PGP2xPBEEncryptedData(BCPGInputStream bCPGInputStream) throws IOException {
        super(bCPGInputStream);
        this.bcpgInput = bCPGInputStream;
        byte[] byArray = new byte[this.RAND_PREFIX_LENGTH + 2];
        bCPGInputStream.read(byArray, 0, this.RAND_PREFIX_LENGTH + 2);
        this.randomPrefixBytes = byArray;
    }

    public InputStream getInputStream() {
        return this.bcpgInput;
    }

    public InputStream getDataStream(char[] cArray) throws PGPException {
        try {
            boolean bl;
            SecretKey secretKey = PGP2xPBEEncryptedData.makeKeyFromPassPhrase(1, cArray, "BC");
            boolean bl2 = true;
            CFBBlockCipher cFBBlockCipher = new CFBBlockCipher((BlockCipher)new IDEAEngine(), 64);
            cFBBlockCipher.init(false, (CipherParameters)new KeyParameter(secretKey.getEncoded()));
            byte[] byArray = new byte[8];
            cFBBlockCipher.processBytes(this.randomPrefixBytes, 0, 8, byArray, 0);
            byte[] byArray2 = new byte[2];
            cFBBlockCipher.processBytes(this.randomPrefixBytes, 8, 2, byArray2, 0);
            if (2 < byArray2.length) {
                throw new EOFException("unexpected end of stream.");
            }
            boolean bl3 = byArray[byArray.length - 2] == byArray2[0] && byArray[byArray.length - 1] == byArray2[1];
            boolean bl4 = bl = byArray2[0] == 0 && byArray2[1] == 0;
            if (!bl3 && !bl) {
                throw new PGPDataValidationException("quick check failed.");
            }
            byte[] byArray3 = new byte[cFBBlockCipher.getBlockSize()];
            System.arraycopy(this.randomPrefixBytes, this.randomPrefixBytes.length - cFBBlockCipher.getBlockSize(), byArray3, 0, cFBBlockCipher.getBlockSize());
            BufferedBlockCipher bufferedBlockCipher = new BufferedBlockCipher((BlockCipher)new CFBBlockCipher((BlockCipher)new IDEAEngine(), 64));
            bufferedBlockCipher.init(false, (CipherParameters)new ParametersWithIV((CipherParameters)new KeyParameter(secretKey.getEncoded()), byArray3));
            this.encStream = new BCPGInputStream((InputStream)new CipherInputStream(this.getInputStream(), bufferedBlockCipher));
            return this.encStream;
        }
        catch (PGPException pGPException) {
            throw pGPException;
        }
        catch (Exception exception) {
            throw new PGPException("Exception creating cipher", exception);
        }
    }

    private static SecretKey makeKeyFromPassPhrase(int n, char[] cArray, String string) throws PGPException, NoSuchProviderException {
        String string2 = null;
        int n2 = 0;
        Provider provider = Security.getProvider(string);
        switch (n) {
            case 2: {
                n2 = 192;
                string2 = "DES_EDE";
                break;
            }
            case 1: {
                n2 = 128;
                string2 = "IDEA";
                break;
            }
            case 3: {
                n2 = 128;
                string2 = "CAST5";
                break;
            }
            case 4: {
                n2 = 128;
                string2 = "Blowfish";
                break;
            }
            case 5: {
                n2 = 128;
                string2 = "SAFER";
                break;
            }
            case 6: {
                n2 = 64;
                string2 = "DES";
                break;
            }
            case 7: {
                n2 = 128;
                string2 = "AES";
                break;
            }
            case 8: {
                n2 = 192;
                string2 = "AES";
                break;
            }
            case 9: {
                n2 = 256;
                string2 = "AES";
                break;
            }
            case 10: {
                n2 = 256;
                string2 = "Twofish";
                break;
            }
            default: {
                throw new PGPException("unknown symmetric algorithm: " + n);
            }
        }
        byte[] byArray = new byte[cArray.length];
        for (int i = 0; i != cArray.length; ++i) {
            byArray[i] = (byte)cArray[i];
        }
        byte[] byArray2 = new byte[(n2 + 7) / 8];
        int n3 = 0;
        int n4 = 0;
        while (n3 < byArray2.length) {
            MD5Digest mD5Digest = new MD5Digest();
            for (int i = 0; i != n4; ++i) {
                mD5Digest.update((byte)0);
            }
            mD5Digest.update(byArray, 0, byArray.length);
            byte[] byArray3 = new byte[mD5Digest.getByteLength()];
            mD5Digest.doFinal(byArray3, 0);
            if (byArray3.length > byArray2.length - n3) {
                System.arraycopy(byArray3, 0, byArray2, n3, byArray2.length - n3);
            } else {
                System.arraycopy(byArray3, 0, byArray2, n3, byArray3.length);
            }
            n3 += byArray3.length;
            ++n4;
        }
        for (int i = 0; i != byArray.length; ++i) {
            byArray[i] = 0;
        }
        return new SecretKeySpec(byArray2, string2);
    }
}

