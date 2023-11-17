/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.S2K
 *  lw.bouncycastle.crypto.BlockCipher
 *  lw.bouncycastle.crypto.BufferedBlockCipher
 *  lw.bouncycastle.crypto.CipherParameters
 *  lw.bouncycastle.crypto.InvalidCipherTextException
 *  lw.bouncycastle.crypto.engines.AESEngine
 *  lw.bouncycastle.crypto.engines.BlowfishEngine
 *  lw.bouncycastle.crypto.engines.CAST5Engine
 *  lw.bouncycastle.crypto.engines.CamelliaEngine
 *  lw.bouncycastle.crypto.engines.DESEngine
 *  lw.bouncycastle.crypto.engines.DESedeEngine
 *  lw.bouncycastle.crypto.engines.IDEAEngine
 *  lw.bouncycastle.crypto.engines.TwofishEngine
 *  lw.bouncycastle.crypto.modes.CFBBlockCipher
 *  lw.bouncycastle.crypto.params.KeyParameter
 *  lw.bouncycastle.crypto.params.ParametersWithIV
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor
 *  lw.bouncycastle.openpgp.operator.PGPDigestCalculator
 *  lw.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider
 *  lw.bouncycastle.util.Strings
 */
package com.didisoft.pgp.bc;

import java.io.IOException;
import java.io.OutputStream;
import lw.bouncycastle.bcpg.S2K;
import lw.bouncycastle.crypto.BlockCipher;
import lw.bouncycastle.crypto.BufferedBlockCipher;
import lw.bouncycastle.crypto.CipherParameters;
import lw.bouncycastle.crypto.InvalidCipherTextException;
import lw.bouncycastle.crypto.engines.AESEngine;
import lw.bouncycastle.crypto.engines.BlowfishEngine;
import lw.bouncycastle.crypto.engines.CAST5Engine;
import lw.bouncycastle.crypto.engines.CamelliaEngine;
import lw.bouncycastle.crypto.engines.DESEngine;
import lw.bouncycastle.crypto.engines.DESedeEngine;
import lw.bouncycastle.crypto.engines.IDEAEngine;
import lw.bouncycastle.crypto.engines.TwofishEngine;
import lw.bouncycastle.crypto.modes.CFBBlockCipher;
import lw.bouncycastle.crypto.params.KeyParameter;
import lw.bouncycastle.crypto.params.ParametersWithIV;
import lw.bouncycastle.openpgp.PGPException;
import lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import lw.bouncycastle.openpgp.operator.PGPDigestCalculator;
import lw.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider;
import lw.bouncycastle.util.Strings;

public class PBESecretKeyDecryptorASCII
extends PBESecretKeyDecryptor {
    private char[] passPhrase;
    private PGPDigestCalculatorProvider calculatorProvider;

    public PBESecretKeyDecryptorASCII(char[] cArray, PGPDigestCalculatorProvider pGPDigestCalculatorProvider) {
        super(cArray, pGPDigestCalculatorProvider);
        this.passPhrase = cArray;
        this.calculatorProvider = pGPDigestCalculatorProvider;
    }

    public byte[] makeKeyFromPassPhrase(int n, S2K s2K) throws PGPException {
        PGPDigestCalculator pGPDigestCalculator = s2K != null ? this.calculatorProvider.get(s2K.getHashAlgorithm()) : this.calculatorProvider.get(1);
        return PBESecretKeyDecryptorASCII.makeKeyFromPassPhraseAscii(pGPDigestCalculator, n, s2K, this.passPhrase);
    }

    static byte[] makeKeyFromPassPhraseAscii(PGPDigestCalculator pGPDigestCalculator, int n, S2K s2K, char[] cArray) throws PGPException {
        String string = null;
        int n2 = 0;
        switch (n) {
            case 2: {
                n2 = 192;
                string = "DES_EDE";
                break;
            }
            case 1: {
                n2 = 128;
                string = "IDEA";
                break;
            }
            case 3: {
                n2 = 128;
                string = "CAST5";
                break;
            }
            case 4: {
                n2 = 128;
                string = "Blowfish";
                break;
            }
            case 5: {
                n2 = 128;
                string = "SAFER";
                break;
            }
            case 6: {
                n2 = 64;
                string = "DES";
                break;
            }
            case 7: {
                n2 = 128;
                string = "AES";
                break;
            }
            case 8: {
                n2 = 192;
                string = "AES";
                break;
            }
            case 9: {
                n2 = 256;
                string = "AES";
                break;
            }
            case 10: {
                n2 = 256;
                string = "Twofish";
                break;
            }
            case 11: {
                n2 = 128;
                string = "Camellia";
                break;
            }
            case 12: {
                n2 = 192;
                string = "Camellia";
                break;
            }
            case 13: {
                n2 = 256;
                string = "Camellia";
                break;
            }
            default: {
                throw new PGPException("unknown symmetric algorithm: " + n);
            }
        }
        byte[] byArray = Strings.toByteArray((char[])cArray);
        byte[] byArray2 = new byte[(n2 + 7) / 8];
        int n3 = 0;
        int n4 = 0;
        if (s2K != null) {
            if (s2K.getHashAlgorithm() != pGPDigestCalculator.getAlgorithm()) {
                throw new PGPException("s2k/digestCalculator mismatch");
            }
        } else if (pGPDigestCalculator.getAlgorithm() != 1) {
            throw new PGPException("digestCalculator not for MD5");
        }
        OutputStream outputStream = pGPDigestCalculator.getOutputStream();
        try {
            while (n3 < byArray2.length) {
                block36: {
                    block35: {
                        if (s2K == null) break block35;
                        for (int i = 0; i != n4; ++i) {
                            outputStream.write(0);
                        }
                        byte[] byArray3 = s2K.getIV();
                        block16 : switch (s2K.getType()) {
                            case 0: {
                                outputStream.write(byArray);
                                break;
                            }
                            case 1: {
                                outputStream.write(byArray3);
                                outputStream.write(byArray);
                                break;
                            }
                            case 3: {
                                long l = s2K.getIterationCount();
                                outputStream.write(byArray3);
                                outputStream.write(byArray);
                                l -= (long)(byArray3.length + byArray.length);
                                while (l > 0L) {
                                    if (l < (long)byArray3.length) {
                                        outputStream.write(byArray3, 0, (int)l);
                                        break block16;
                                    }
                                    outputStream.write(byArray3);
                                    if ((l -= (long)byArray3.length) < (long)byArray.length) {
                                        outputStream.write(byArray, 0, (int)l);
                                        l = 0L;
                                        continue;
                                    }
                                    outputStream.write(byArray);
                                    l -= (long)byArray.length;
                                }
                                break block36;
                            }
                            default: {
                                throw new PGPException("unknown S2K type: " + s2K.getType());
                            }
                        }
                        break block36;
                    }
                    for (int i = 0; i != n4; ++i) {
                        outputStream.write(0);
                    }
                    outputStream.write(byArray);
                }
                outputStream.close();
                byte[] byArray4 = pGPDigestCalculator.getDigest();
                if (byArray4.length > byArray2.length - n3) {
                    System.arraycopy(byArray4, 0, byArray2, n3, byArray2.length - n3);
                } else {
                    System.arraycopy(byArray4, 0, byArray2, n3, byArray4.length);
                }
                n3 += byArray4.length;
                ++n4;
            }
        }
        catch (IOException iOException) {
            throw new PGPException("exception calculating digest: " + iOException.getMessage(), (Exception)iOException);
        }
        for (int i = 0; i != byArray.length; ++i) {
            byArray[i] = 0;
        }
        return byArray2;
    }

    public byte[] recoverKeyData(int n, byte[] byArray, byte[] byArray2, byte[] byArray3, int n2, int n3) throws PGPException {
        try {
            BufferedBlockCipher bufferedBlockCipher = PBESecretKeyDecryptorASCII.createSymmetricKeyWrapper(false, PBESecretKeyDecryptorASCII.createBlockCipher(n), byArray, byArray2);
            byte[] byArray4 = new byte[n3];
            int n4 = bufferedBlockCipher.processBytes(byArray3, n2, n3, byArray4, 0);
            n4 += bufferedBlockCipher.doFinal(byArray4, n4);
            return byArray4;
        }
        catch (InvalidCipherTextException invalidCipherTextException) {
            throw new PGPException("decryption failed: " + invalidCipherTextException.getMessage(), (Exception)((Object)invalidCipherTextException));
        }
    }

    public static BufferedBlockCipher createSymmetricKeyWrapper(boolean bl, BlockCipher blockCipher, byte[] byArray, byte[] byArray2) {
        BufferedBlockCipher bufferedBlockCipher = new BufferedBlockCipher((BlockCipher)new CFBBlockCipher(blockCipher, blockCipher.getBlockSize() * 8));
        bufferedBlockCipher.init(bl, (CipherParameters)new ParametersWithIV((CipherParameters)new KeyParameter(byArray), byArray2));
        return bufferedBlockCipher;
    }

    static BlockCipher createBlockCipher(int n) throws PGPException {
        AESEngine aESEngine;
        switch (n) {
            case 7: 
            case 8: 
            case 9: {
                aESEngine = new AESEngine();
                break;
            }
            case 11: 
            case 12: 
            case 13: {
                aESEngine = new CamelliaEngine();
                break;
            }
            case 4: {
                aESEngine = new BlowfishEngine();
                break;
            }
            case 3: {
                aESEngine = new CAST5Engine();
                break;
            }
            case 6: {
                aESEngine = new DESEngine();
                break;
            }
            case 1: {
                aESEngine = new IDEAEngine();
                break;
            }
            case 10: {
                aESEngine = new TwofishEngine();
                break;
            }
            case 2: {
                aESEngine = new DESedeEngine();
                break;
            }
            default: {
                throw new PGPException("cannot recognise cipher");
            }
        }
        return aESEngine;
    }
}

