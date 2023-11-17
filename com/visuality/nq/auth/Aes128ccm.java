/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.Aes128cmac;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.BufferWriter;
import java.util.Arrays;

public class Aes128ccm {
    public static final int AES_BLOCK_SIZE = 16;
    private static final int AES_128_CCM_M = 16;
    private static final int AES_128_CCM_L = 4;
    private static final int AES_128_CCM_M_tag = 7;
    private static final int AES_128_CCM_L_tag = 3;
    private static final int AES_128_CCM_NONCE_SIZE = 11;

    private static void AES_XOR_128(byte[] a, byte[] b, byte[] out) {
        for (int i = 0; i < 16; ++i) {
            out[i] = (byte)(a[i] ^ b[i]);
        }
    }

    private static void AES_XOR_128_index(byte[] a, int aIndex, byte[] b, int bIndex, byte[] out, int outIndex) {
        for (int i = 0; i < 16; ++i) {
            out[i + outIndex] = (byte)(a[i + aIndex] ^ b[i + bIndex]);
        }
    }

    public static void aes128CcmEncrypt(byte[] key, byte[] nonce, byte[] msgBuf, int orgMsgPosition, int orgMsgLen, int addPosition, int addLen, int sigPosition) {
        Blob keyBlob = new Blob();
        Blob key1Blob = new Blob();
        Blob prefixBlob = new Blob(addLen);
        Blob msgBlob = new Blob(orgMsgLen);
        keyBlob.data = key;
        keyBlob.len = 16;
        key1Blob.data = nonce;
        key1Blob.len = 11;
        System.arraycopy(msgBuf, addPosition, prefixBlob.data, 0, addLen);
        System.arraycopy(msgBuf, orgMsgPosition, msgBlob.data, 0, orgMsgLen);
        byte[] sigNature = new byte[16];
        Aes128ccm.aes128CcmEncryptionInternal(keyBlob, key1Blob, prefixBlob, msgBlob, sigNature);
        BufferWriter writer = new BufferWriter(msgBuf, 4, false);
        writer.setOffset(sigPosition);
        writer.writeBytes(sigNature, sigNature.length);
        writer.writeBytes(prefixBlob.data, addLen);
        writer.setOffset(orgMsgPosition);
        writer.writeBytes(msgBlob.data, orgMsgLen);
    }

    private static void aes128CcmEncryptionInternal(Blob key, Blob key1, Blob prefix, Blob message, byte[] auth) {
        int lm = message.len % 16 == 0 ? message.len / 16 : message.len / 16 + 1;
        int la = prefix.len % 16 == 0 ? prefix.len / 16 : prefix.len / 16 + 1;
        int written = 0;
        int remaining = 0;
        int i = 0;
        int B_offset = 0;
        byte[] S0 = new byte[16];
        int writer = 0;
        byte[] B = new byte[(lm + la + 2 + 1) * 16];
        byte[] X = new byte[(lm + la + 2) * 16];
        for (i = 0; i < 16; ++i) {
            auth[i] = 0;
        }
        B[0] = 59;
        B[0] = (byte)(prefix.len > 0 ? B[0] + 64 : B[0]);
        for (i = 0; i < key1.len; ++i) {
            B[i + 1] = key1.data[i];
        }
        BufferWriter BWriter = new BufferWriter(B, 12, true);
        BWriter.writeInt4(message.len);
        Aes128cmac.AES_128_Encrypt(B, key.data, X);
        if (prefix.len >= 65280) {
            B[16] = -1;
            B[17] = -2;
            BWriter.setOffset(18);
            BWriter.writeInt4(prefix.len);
            B_offset = 6;
            writer = 22;
        } else if (prefix.len > 0) {
            BWriter.setOffset(16);
            BWriter.writeInt2(prefix.len);
            B_offset = 2;
            writer = 18;
        }
        for (i = 0; i < prefix.len; ++i) {
            B[writer + i] = prefix.data[i];
        }
        writer += prefix.len;
        remaining = (prefix.len + B_offset) % 16;
        if (remaining > 0) {
            for (i = 0; i < 16 - remaining; ++i) {
                B[i + writer] = 0;
            }
            writer += 16 - remaining;
        }
        for (i = 0; i < message.len; ++i) {
            B[i + writer] = message.data[i];
        }
        written = writer += message.len;
        written = written % 16 == 0 ? written / 16 : written / 16 + 1;
        for (i = 1; i < written; ++i) {
            Aes128ccm.AES_XOR_128_index(B, i * 16, X, (i - 1) * 16, B, i * 16);
            byte[] tmp = new byte[16];
            byte[] tmpBuff = new byte[16];
            System.arraycopy(B, i * 16, tmpBuff, 0, tmpBuff.length);
            Aes128cmac.AES_128_Encrypt(tmpBuff, key.data, tmp);
            for (int j = 0; j < 16; ++j) {
                X[i * 16 + j] = tmp[j];
            }
        }
        byte[] A = new byte[16];
        BufferWriter AWriter = new BufferWriter(A, 0, true);
        for (i = 0; i < lm + 1; ++i) {
            int j;
            byte[] S = new byte[16];
            int p = 0;
            if (i == 0) {
                A[0] = 3;
                p = 1;
                for (j = 0; j < 11; ++j) {
                    A[p + j] = key1.data[j];
                }
            }
            p = 12;
            AWriter.setOffset(p);
            AWriter.writeInt4(i);
            Aes128cmac.AES_128_Encrypt(A, key.data, S);
            if (i == 0) {
                for (j = 0; j < 16; ++j) {
                    S0[j] = S[j];
                }
            }
            if (i <= 0) continue;
            if (i == lm && message.len % 16 != 0) {
                for (j = 0; j < message.len % 16; ++j) {
                    message.data[(lm - 1) * 16 + j] = (byte)(message.data[(lm - 1) * 16 + j] ^ S[j]);
                }
                continue;
            }
            Aes128ccm.AES_XOR_128_index(message.data, (i - 1) * 16, S, 0, message.data, (i - 1) * 16);
        }
        Aes128ccm.AES_XOR_128_index(X, (written - 1) * 16, S0, 0, auth, 0);
    }

    public static boolean aes128CcmDecrypt(byte[] key, byte[] nonce, byte[] msgBuf, int msgLen, byte[] addBuf, int addLen, byte[] authValue) {
        Blob keyBlob = new Blob();
        Blob key1Blob = new Blob();
        Blob prefixBlob = new Blob();
        Blob msgBlob = new Blob();
        keyBlob.data = key;
        keyBlob.len = 16;
        key1Blob.data = nonce;
        key1Blob.len = 11;
        prefixBlob.data = addBuf;
        prefixBlob.len = addLen;
        msgBlob.data = msgBuf;
        msgBlob.len = msgLen;
        return Aes128ccm.aes128CcmDecryptionInternal(keyBlob, key1Blob, prefixBlob, msgBlob, authValue);
    }

    private static boolean aes128CcmDecryptionInternal(Blob key, Blob key1, Blob prefix, Blob message, byte[] auth) {
        int lm = message.len % 16 == 0 ? message.len / 16 : message.len / 16 + 1;
        int la = prefix.len % 16 == 0 ? prefix.len / 16 : prefix.len / 16 + 1;
        int i = 0;
        int remaining = 0;
        int B_offset = 0;
        int written = 0;
        byte[] S0 = new byte[16];
        byte[] T = new byte[16];
        byte[] B = new byte[(lm + la + 2 + 1) * 16];
        byte[] X = new byte[(lm + la + 2) * 16];
        byte[] A = new byte[16];
        byte[] S = new byte[16];
        BufferWriter AWriter = new BufferWriter(A, 0, true);
        for (i = 0; i < lm + 1; ++i) {
            int j;
            int p = 0;
            A[0] = 3;
            p = 1;
            for (j = 0; j < 11; ++j) {
                A[j + p] = key1.data[j];
            }
            p = 12;
            AWriter.setOffset(p);
            AWriter.writeInt4(i);
            Aes128cmac.AES_128_Encrypt(A, key.data, S);
            if (i == 0) {
                System.arraycopy(S, 0, S0, 0, 16);
            }
            if (i <= 0) continue;
            if (i == lm && message.len % 16 != 0) {
                for (j = 0; j < message.len % 16; ++j) {
                    message.data[(lm - 1) * 16 + j] = (byte)(message.data[(lm - 1) * 16 + j] ^ S[j]);
                }
                continue;
            }
            Aes128ccm.AES_XOR_128_index(message.data, (i - 1) * 16, S, 0, message.data, (i - 1) * 16);
        }
        Aes128ccm.AES_XOR_128(auth, S0, T);
        B[0] = 59;
        B[0] = (byte)(prefix.len > 0 ? B[0] + 64 : B[0]);
        for (i = 0; i < key1.len; ++i) {
            B[i + 1] = key1.data[i];
        }
        BufferWriter BWriter = new BufferWriter(B, 12, true);
        BWriter.writeInt4(message.len);
        Aes128cmac.AES_128_Encrypt(B, key.data, X);
        int writer = 0;
        if (prefix.len >= 65280) {
            B[16] = -1;
            B[17] = -2;
            BWriter.setOffset(18);
            BWriter.writeInt4(prefix.len);
            B_offset = 6;
            writer = 22;
        } else if (prefix.len > 0) {
            BWriter.setOffset(16);
            BWriter.writeInt2(prefix.len);
            B_offset = 2;
            writer = 18;
        }
        for (i = 0; i < prefix.len; ++i) {
            B[writer + i] = prefix.data[i];
        }
        writer += prefix.len;
        remaining = (prefix.len + B_offset) % 16;
        if (remaining > 0) {
            for (i = 0; i < 16 - remaining; ++i) {
                B[i + writer] = 0;
            }
            writer += 16 - remaining;
        }
        for (i = 0; i < message.len; ++i) {
            B[i + writer] = message.data[i];
        }
        written = writer += message.len;
        written = written % 16 == 0 ? written / 16 : written / 16 + 1;
        for (i = 1; i < written; ++i) {
            Aes128ccm.AES_XOR_128_index(B, i * 16, X, (i - 1) * 16, B, i * 16);
            byte[] tmp = new byte[16];
            byte[] tmpBuff = new byte[16];
            System.arraycopy(B, i * 16, tmpBuff, 0, tmpBuff.length);
            Aes128cmac.AES_128_Encrypt(tmpBuff, key.data, tmp);
            for (int j = 0; j < 16; ++j) {
                X[i * 16 + j] = tmp[j];
            }
        }
        int sOff = (written - 1) * 16;
        int eOff = (written - 1) * 16 + 16;
        byte[] tmpBuff = new byte[eOff - sOff];
        System.arraycopy(X, sOff, tmpBuff, 0, tmpBuff.length);
        return Arrays.equals(T, tmpBuff);
    }
}

