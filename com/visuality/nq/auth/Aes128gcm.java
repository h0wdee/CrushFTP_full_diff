/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.common.Blob;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.ObjectPool;
import java.util.Arrays;

public class Aes128gcm {
    private static final int AES_PRIV_SIZE = 244;
    private static final int AES_PRIV_NR_POS = 60;
    private static final int AES_SESSIONKEY_LENGTH = 16;
    private static final int AES_128_GCM_NONCE_SIZE = 12;
    private static final int AES_BLOCK_SIZE = 16;
    private static ObjectPool<byte[]> arraysPool;
    private static final int[] Te0;
    private static final int[] Te1;
    private static final int[] Te2;
    private static final int[] Te3;
    private static final int[] Te4;
    private static final int[] rcon;
    private static final int[] RTable;

    private static void initialArraysPool() {
        arraysPool = new ObjectPool<byte[]>(10){

            @Override
            protected byte[] createObject() {
                return new byte[16];
            }
        };
    }

    private static int rijndaelKeySetupEnc(int[] rk, byte[] cipherKey, int keyBits) {
        int temp;
        int i;
        rk[0] = (0xFF & cipherKey[0]) << 24 ^ (0xFF & cipherKey[1]) << 16 ^ (0xFF & cipherKey[2]) << 8 ^ 0xFF & cipherKey[3];
        rk[1] = (0xFF & cipherKey[4]) << 24 ^ (0xFF & cipherKey[5]) << 16 ^ (0xFF & cipherKey[6]) << 8 ^ 0xFF & cipherKey[7];
        rk[2] = (0xFF & cipherKey[8]) << 24 ^ (0xFF & cipherKey[9]) << 16 ^ (0xFF & cipherKey[10]) << 8 ^ 0xFF & cipherKey[11];
        rk[3] = (0xFF & cipherKey[12]) << 24 ^ (0xFF & cipherKey[13]) << 16 ^ (0xFF & cipherKey[14]) << 8 ^ 0xFF & cipherKey[15];
        int step = 0;
        if (keyBits == 128) {
            for (int i2 = 0; i2 < 10; ++i2) {
                int temp2 = rk[3 + step];
                rk[4 + step] = rk[0 + step] ^ Te4[temp2 >>> 16 & 0xFF] & 0xFF000000 ^ Te4[temp2 >>> 8 & 0xFF] & 0xFF0000 ^ Te4[temp2 & 0xFF] & 0xFF00 ^ Te4[temp2 >>> 24 & 0xFF] & 0xFF ^ rcon[i2];
                rk[5 + step] = rk[1 + step] ^ rk[4 + step];
                rk[6 + step] = rk[2 + step] ^ rk[5 + step];
                rk[7 + step] = rk[3 + step] ^ rk[6 + step];
                step += 4;
            }
            return 10;
        }
        rk[4] = cipherKey[16] << 24 ^ cipherKey[17] << 16 ^ cipherKey[18] << 8 ^ cipherKey[19];
        rk[5] = cipherKey[20] << 24 ^ cipherKey[21] << 16 ^ cipherKey[22] << 8 ^ cipherKey[23];
        if (keyBits == 192) {
            for (i = 0; i < 8; ++i) {
                temp = rk[5 + step];
                rk[6 + step] = rk[0 + step] ^ Te4[temp >>> 16 & 0xFF] & 0xFF000000 ^ Te4[temp >>> 8 & 0xFF] & 0xFF0000 ^ Te4[temp & 0xFF] & 0xFF00 ^ Te4[temp >>> 24 & 0xFF] & 0xFF ^ rcon[i];
                rk[7 + step] = rk[1 + step] ^ rk[6 + step];
                rk[8 + step] = rk[2 + step] ^ rk[7 + step];
                rk[9 + step] = rk[3 + step] ^ rk[8 + step];
                if (i == 7) {
                    return 12;
                }
                rk[10 + step] = rk[4 + step] ^ rk[9 + step];
                rk[11 + step] = rk[5 + step] ^ rk[10 + step];
                step += 6;
            }
        }
        rk[6 + step] = cipherKey[24] << 24 ^ cipherKey[25] << 16 ^ cipherKey[26] << 8 ^ cipherKey[27];
        rk[7 + step] = cipherKey[28] << 24 ^ cipherKey[29] << 16 ^ cipherKey[30] << 8 ^ cipherKey[31];
        if (keyBits == 256) {
            for (i = 0; i < 7; ++i) {
                temp = rk[7 + step];
                rk[8 + step] = rk[0 + step] ^ Te4[temp >>> 16 & 0xFF] & 0xFF000000 ^ Te4[temp >>> 8 & 0xFF] & 0xFF0000 ^ Te4[temp & 0xFF] & 0xFF00 ^ Te4[temp >>> 24 & 0xFF] & 0xFF ^ rcon[i];
                rk[9 + step] = rk[1 + step] ^ rk[8 + step];
                rk[10 + step] = rk[2 + step] ^ rk[9 + step];
                rk[11 + step] = rk[3 + step] ^ rk[10 + step];
                if (i == 6) {
                    return 14;
                }
                temp = rk[11 + step];
                rk[12 + step] = rk[4 + step] ^ Te4[temp >>> 24 & 0xFF] & 0xFF000000 ^ Te4[temp >>> 16 & 0xFF] & 0xFF0000 ^ Te4[temp >>> 8 & 0xFF] & 0xFF00 ^ Te4[temp & 0xFF] & 0xFF;
                rk[13 + step] = rk[5 + step] ^ rk[12 + step];
                rk[14 + step] = rk[6 + step] ^ rk[13 + step];
                rk[15 + step] = rk[7 + step] ^ rk[14 + step];
                step += 8;
            }
        }
        return -1;
    }

    static void rijndaelEncrypt(int[] rk, int Nr, byte[] pt, byte[] ct, int ctPosition) {
        int t3;
        int t2;
        int t1;
        int t0;
        int s0 = pt[0] << 24 ^ (0xFF & pt[1]) << 16 ^ (0xFF & pt[2]) << 8 ^ 0xFF & pt[3] ^ rk[0];
        int s1 = pt[4] << 24 ^ (0xFF & pt[5]) << 16 ^ (0xFF & pt[6]) << 8 ^ 0xFF & pt[7] ^ rk[1];
        int s2 = pt[8] << 24 ^ (0xFF & pt[9]) << 16 ^ (0xFF & pt[10]) << 8 ^ 0xFF & pt[11] ^ rk[2];
        int s3 = pt[12] << 24 ^ (0xFF & pt[13]) << 16 ^ (0xFF & pt[14]) << 8 ^ 0xFF & pt[15] ^ rk[3];
        int r = Nr >>> 1;
        int step = 0;
        while (true) {
            t0 = Te0[s0 >>> 24 & 0xFF] ^ Te1[s1 >>> 16 & 0xFF] ^ Te2[s2 >>> 8 & 0xFF] ^ Te3[s3 & 0xFF] ^ rk[step + 4];
            t1 = Te0[s1 >>> 24 & 0xFF] ^ Te1[s2 >>> 16 & 0xFF] ^ Te2[s3 >>> 8 & 0xFF] ^ Te3[s0 & 0xFF] ^ rk[step + 4 + 1];
            t2 = Te0[s2 >>> 24 & 0xFF] ^ Te1[s3 >>> 16 & 0xFF] ^ Te2[s0 >>> 8 & 0xFF] ^ Te3[s1 & 0xFF] ^ rk[step + 4 + 2];
            t3 = Te0[s3 >>> 24 & 0xFF] ^ Te1[s0 >>> 16 & 0xFF] ^ Te2[s1 >>> 8 & 0xFF] ^ Te3[s2 & 0xFF] ^ rk[step + 4 + 3];
            step += 8;
            if (--r == 0) break;
            s0 = Te0[t0 >>> 24 & 0xFF] ^ Te1[t1 >>> 16 & 0xFF] ^ Te2[t2 >>> 8 & 0xFF] ^ Te3[t3 & 0xFF] ^ rk[step];
            s1 = Te0[t1 >>> 24 & 0xFF] ^ Te1[t2 >>> 16 & 0xFF] ^ Te2[t3 >>> 8 & 0xFF] ^ Te3[t0 & 0xFF] ^ rk[step + 1];
            s2 = Te0[t2 >>> 24 & 0xFF] ^ Te1[t3 >>> 16 & 0xFF] ^ Te2[t0 >>> 8 & 0xFF] ^ Te3[t1 & 0xFF] ^ rk[step + 2];
            s3 = Te0[t3 >>> 24 & 0xFF] ^ Te1[t0 >>> 16 & 0xFF] ^ Te2[t1 >>> 8 & 0xFF] ^ Te3[t2 & 0xFF] ^ rk[step + 3];
        }
        s0 = Te4[t0 >>> 24 & 0xFF] & 0xFF000000 ^ Te4[t1 >>> 16 & 0xFF] & 0xFF0000 ^ Te4[t2 >>> 8 & 0xFF] & 0xFF00 ^ Te4[t3 & 0xFF] & 0xFF ^ rk[step + 0];
        ct[0 + ctPosition] = (byte)(s0 >>> 24);
        ct[1 + ctPosition] = (byte)(s0 >>> 16);
        ct[2 + ctPosition] = (byte)(s0 >>> 8);
        ct[3 + ctPosition] = (byte)s0;
        s1 = Te4[t1 >>> 24 & 0xFF] & 0xFF000000 ^ Te4[t2 >>> 16 & 0xFF] & 0xFF0000 ^ Te4[t3 >>> 8 & 0xFF] & 0xFF00 ^ Te4[t0 & 0xFF] & 0xFF ^ rk[step + 1];
        ct[4 + ctPosition] = (byte)(s1 >>> 24);
        ct[5 + ctPosition] = (byte)(s1 >>> 16);
        ct[6 + ctPosition] = (byte)(s1 >>> 8);
        ct[7 + ctPosition] = (byte)s1;
        s2 = Te4[t2 >>> 24 & 0xFF] & 0xFF000000 ^ Te4[t3 >>> 16 & 0xFF] & 0xFF0000 ^ Te4[t0 >>> 8 & 0xFF] & 0xFF00 ^ Te4[t1 & 0xFF] & 0xFF ^ rk[step + 2];
        ct[8 + ctPosition] = (byte)(s2 >>> 24);
        ct[9 + ctPosition] = (byte)(s2 >>> 16);
        ct[10 + ctPosition] = (byte)(s2 >>> 8);
        ct[11 + ctPosition] = (byte)s2;
        s3 = Te4[t3 >>> 24 & 0xFF] & 0xFF000000 ^ Te4[t0 >>> 16 & 0xFF] & 0xFF0000 ^ Te4[t1 >>> 8 & 0xFF] & 0xFF00 ^ Te4[t2 & 0xFF] & 0xFF ^ rk[step + 3];
        ct[12 + ctPosition] = (byte)(s3 >>> 24);
        ct[13 + ctPosition] = (byte)(s3 >>> 16);
        ct[14 + ctPosition] = (byte)(s3 >>> 8);
        ct[15 + ctPosition] = (byte)s3;
    }

    private static int[] aesEncryptInit(byte[] key, int len, byte[] keyBuffer) throws NqException {
        int res;
        int[] rk = new int[244];
        if (null != keyBuffer) {
            BufferReader reader = new BufferReader(keyBuffer, 0, false);
            for (int i = 0; i < 61; ++i) {
                rk[i] = reader.readInt4();
            }
        }
        if ((res = Aes128gcm.rijndaelKeySetupEnc(rk, key, len * 8)) < 0) {
            return null;
        }
        rk[60] = res;
        return rk;
    }

    private static void aesEncrypt(int[] ctx, byte[] plain, byte[] encrypted, int encPosition) {
        int[] rk = ctx;
        Aes128gcm.rijndaelEncrypt(ctx, rk[60], plain, encrypted, encPosition);
    }

    private static void inc32(byte[] block) {
        int i = 12;
        int val = (0xFF & block[i++]) << 24 | (0xFF & block[i++]) << 16 | (0xFF & block[i++]) << 8 | 0xFF & block[i++];
        i = 12;
        block[i++] = (byte)(++val >>> 24);
        block[i++] = (byte)(val >>> 16);
        block[i++] = (byte)(val >>> 8);
        block[i++] = (byte)(val & 0xFF);
    }

    private static void xorBlocks(byte[] dst, int dstPos, byte[] src, int srcPos) throws NqException {
        for (int i = 0; i < 16; ++i) {
            dst[i + dstPos] = (byte)(dst[i + dstPos] ^ src[i + srcPos]);
        }
    }

    private static void shiftRblock(byte[] v) {
        for (int i = 12; i >= 0; i -= 4) {
            int val = (0xFF & v[i + 0]) << 24 | (0xFF & v[i + 1]) << 16 | (0xFF & v[i + 2]) << 8 | 0xFF & v[i + 3];
            val >>>= 1;
            if (i > 0 && 0 != (v[i - 1] & 1)) {
                val |= Integer.MIN_VALUE;
            }
            v[i + 0] = (byte)(val >>> 24);
            v[i + 1] = (byte)(val >>> 16);
            v[i + 2] = (byte)(val >>> 8);
            v[i + 3] = (byte)(val & 0xFF);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void multGF128(byte[] multD, byte[] multR, byte[] data, int dataIndex) throws NqException {
        byte[] v = arraysPool.borrowObject();
        try {
            int i;
            for (i = 0; i < 16; ++i) {
                data[i + dataIndex] = 0;
                v[i] = multD[i];
            }
            for (i = 0; i < 16; ++i) {
                for (int j = 0; j < 8; ++j) {
                    if (0 != (multR[i] & 1 << 7 - j)) {
                        Aes128gcm.xorBlocks(data, dataIndex, v, 0);
                    }
                    if (0 != (v[15] & 1)) {
                        Aes128gcm.shiftRblock(v);
                        v[0] = (byte)(v[0] ^ 0xE1);
                        continue;
                    }
                    Aes128gcm.shiftRblock(v);
                }
            }
        }
        finally {
            arraysPool.returnObject(v);
        }
    }

    private static void gcmHashInit(byte[] y) {
        Arrays.fill(y, (byte)0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void gcmHash(byte[] h, byte[] x, int xLen, byte[] y) throws NqException {
        int xPos = 0;
        int m = xLen / 16;
        for (int i = 0; i < m; ++i) {
            Aes128gcm.xorBlocks(y, 0, x, xPos);
            xPos += 16;
            Aes128gcm.multGF128MTable(y, h, y);
        }
        if (xLen > xPos) {
            byte[] temp = arraysPool.borrowObject();
            try {
                int last = xLen - xPos;
                System.arraycopy(x, xPos, temp, 0, last);
                for (int i = last; i < temp.length; ++i) {
                    temp[i] = 0;
                }
                Aes128gcm.xorBlocks(y, 0, temp, 0);
                Aes128gcm.multGF128MTable(y, h, y);
            }
            finally {
                arraysPool.returnObject(temp);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void calcMTable(byte[] hashKey, byte[] mTable) throws NqException {
        int i = 0;
        byte[] MulD = arraysPool.borrowObject();
        try {
            int pMTableIndex = 0;
            Aes128gcm.gcmHashInit(MulD);
            while (true) {
                MulD[0] = (byte)i;
                Aes128gcm.multGF128(MulD, hashKey, mTable, pMTableIndex);
                pMTableIndex += 16;
                if (i == 255) {
                    break;
                }
                ++i;
            }
        }
        finally {
            arraysPool.returnObject(MulD);
        }
    }

    private static void multGF128MTable(byte[] multD, byte[] mTable, byte[] product) throws NqException {
        byte[] buf = new byte[32];
        int bufIndex = 15;
        Aes128gcm.gcmHashInit(buf);
        for (int i = 15; i > 0; --i) {
            Aes128gcm.xorBlocks(buf, bufIndex, mTable, Aes128gcm.byteToUnsignedInt(multD[i]) * 16);
            int val = Aes128gcm.byteToUnsignedInt(buf[bufIndex + 15]);
            int n = --bufIndex;
            buf[n] = (byte)(buf[n] ^ RTable[val * 2]);
            int n2 = bufIndex + 1;
            buf[n2] = (byte)(buf[n2] ^ RTable[val * 2 + 1]);
        }
        Aes128gcm.xorBlocks(buf, bufIndex, mTable, Aes128gcm.byteToUnsignedInt(multD[0]) * 16);
        System.arraycopy(buf, 0, product, 0, 16);
    }

    private static int byteToUnsignedInt(byte b) {
        return b < 0 ? b & 0xFF : b;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void aesGCtr(int[] aes, byte[] iv, byte[] in, int inLen, byte[] out) throws NqException {
        block9: {
            int inPos = 0;
            int outPos = 0;
            if (inLen == 0) {
                return;
            }
            int n = inLen / 16;
            byte[] cBlock = arraysPool.borrowObject();
            try {
                int i;
                Aes128gcm.gcmHashInit(cBlock);
                System.arraycopy(iv, 0, cBlock, 0, iv.length);
                for (i = 0; i < n; ++i) {
                    Aes128gcm.aesEncrypt(aes, cBlock, out, outPos);
                    Aes128gcm.xorBlocks(out, outPos, in, inPos);
                    inPos += 16;
                    outPos += 16;
                    Aes128gcm.inc32(cBlock);
                }
                int last = inLen - inPos;
                if (last <= 0) break block9;
                byte[] tmp = arraysPool.borrowObject();
                try {
                    Aes128gcm.gcmHashInit(tmp);
                    Aes128gcm.aesEncrypt(aes, cBlock, tmp, 0);
                    for (i = 0; i < last; ++i) {
                        out[i + outPos] = (byte)(in[i + inPos] ^ tmp[i]);
                    }
                }
                finally {
                    arraysPool.returnObject(tmp);
                }
            }
            finally {
                arraysPool.returnObject(cBlock);
            }
        }
    }

    private static int[] aesGcmInitHashSubkey(byte[] key, int keyLen, byte[] hash, byte[] keyBuffer) throws NqException {
        int[] aes = Aes128gcm.aesEncryptInit(key, keyLen, keyBuffer);
        if (aes == null) {
            return null;
        }
        Aes128gcm.gcmHashInit(hash);
        Aes128gcm.aesEncrypt(aes, hash, hash, 0);
        return aes;
    }

    private static void aesGcmPreparej0(byte[] iv, byte[] J0) {
        int i;
        for (i = 0; i < 12; ++i) {
            J0[i] = iv[i];
        }
        while (i < 16) {
            J0[i] = 0;
            ++i;
        }
        J0[15] = 1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void aesGcmCtr(int[] aes, byte[] J0, byte[] in, int inLen, byte[] out) throws NqException {
        if (inLen == 0) {
            return;
        }
        byte[] J0inc = arraysPool.borrowObject();
        try {
            Aes128gcm.gcmHashInit(J0inc);
            System.arraycopy(J0, 0, J0inc, 0, J0inc.length);
            Aes128gcm.inc32(J0inc);
            Aes128gcm.aesGCtr(aes, J0inc, in, inLen, out);
        }
        finally {
            arraysPool.returnObject(J0inc);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void aesGcmGHash(byte[] hash, byte[] aad, int aadLen, byte[] encrypted, int encryptedLen, byte[] S) throws NqException {
        byte[] lenBuf = arraysPool.borrowObject();
        try {
            Aes128gcm.gcmHashInit(S);
            Aes128gcm.gcmHash(hash, aad, aadLen, S);
            Aes128gcm.gcmHash(hash, encrypted, encryptedLen, S);
            lenBuf[0] = 0;
            lenBuf[1] = 0;
            lenBuf[2] = 0;
            lenBuf[3] = (byte)((long)aadLen >>> 29);
            lenBuf[4] = (byte)((long)aadLen >>> 21);
            lenBuf[5] = (byte)((long)aadLen >>> 13);
            lenBuf[6] = (byte)((long)aadLen >>> 5);
            lenBuf[7] = (byte)((long)aadLen << 3 & 0xFFL);
            lenBuf[8] = 0;
            lenBuf[9] = 0;
            lenBuf[10] = 0;
            lenBuf[11] = (byte)((long)encryptedLen >>> 29);
            lenBuf[12] = (byte)((long)encryptedLen >>> 21);
            lenBuf[13] = (byte)((long)encryptedLen >>> 13);
            lenBuf[14] = (byte)((long)encryptedLen >>> 5);
            lenBuf[15] = (byte)((long)encryptedLen << 3 & 0xFFL);
            Aes128gcm.gcmHash(hash, lenBuf, lenBuf.length, S);
        }
        finally {
            arraysPool.returnObject(lenBuf);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void aes128GcmEncryptInternal(Blob key, Blob IV, Blob AAD, Blob message, byte[] auth, byte[] keyBuffer, byte[] encMsgBuffer) throws NqException {
        byte[] hash = arraysPool.borrowObject();
        byte[] J0 = arraysPool.borrowObject();
        byte[] S = arraysPool.borrowObject();
        byte[] pMTable = new byte[4096];
        byte[] encrypted = null != encMsgBuffer ? encMsgBuffer : new byte[message.len];
        try {
            int[] aes = Aes128gcm.aesGcmInitHashSubkey(key.data, key.len, hash, keyBuffer);
            if (null == aes) {
                return;
            }
            Aes128gcm.aesGcmPreparej0(IV.data, J0);
            Aes128gcm.aesGcmCtr(aes, J0, message.data, message.len, encrypted);
            Aes128gcm.calcMTable(hash, pMTable);
            Aes128gcm.aesGcmGHash(pMTable, AAD.data, AAD.len, encrypted, message.len, S);
            Aes128gcm.aesGCtr(aes, J0, S, S.length, auth);
            message.data = encrypted;
        }
        finally {
            arraysPool.returnObject(hash);
            arraysPool.returnObject(J0);
            arraysPool.returnObject(S);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void aes128GcmEncrypt(byte[] key, byte[] nonce, byte[] msgBuf, int orgMsgPosition, int orgMsgLen, int addPosition, int addLen, int signaturePostion, byte[] keyBuffer, byte[] msgBuffer) throws NqException {
        Blob keyBlob = new Blob();
        Blob IVBlob = new Blob();
        Blob AADBlob = new Blob(addLen);
        Blob msgBlob = new Blob(orgMsgLen);
        keyBlob.data = key;
        keyBlob.len = 16;
        IVBlob.data = nonce;
        IVBlob.len = 12;
        System.arraycopy(msgBuf, addPosition, AADBlob.data, 0, addLen);
        System.arraycopy(msgBuf, orgMsgPosition, msgBlob.data, 0, orgMsgLen);
        if (null == arraysPool) {
            Aes128gcm.initialArraysPool();
        }
        byte[] auth = arraysPool.borrowObject();
        try {
            Aes128gcm.aes128GcmEncryptInternal(keyBlob, IVBlob, AADBlob, msgBlob, auth, keyBuffer, msgBuffer);
            System.arraycopy(msgBlob.data, 0, msgBuf, orgMsgPosition, msgBlob.len);
            System.arraycopy(auth, 0, msgBuf, signaturePostion, auth.length);
        }
        finally {
            arraysPool.returnObject(auth);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean aes128GcmDecryptInternal(Blob key, Blob IV, Blob AAD, Blob message, byte[] auth, byte[] keyBuffer, byte[] msgBuffer) throws NqException {
        byte[] hash = arraysPool.borrowObject();
        byte[] J0 = arraysPool.borrowObject();
        byte[] S = arraysPool.borrowObject();
        byte[] T = arraysPool.borrowObject();
        byte[] pMTable = new byte[4096];
        try {
            int[] aes = Aes128gcm.aesGcmInitHashSubkey(key.data, key.len, hash, keyBuffer);
            if (null == aes) {
                boolean bl = false;
                return bl;
            }
            byte[] plainText = null != msgBuffer ? msgBuffer : new byte[message.len];
            Aes128gcm.aesGcmPreparej0(IV.data, J0);
            Aes128gcm.aesGcmCtr(aes, J0, message.data, message.len, plainText);
            Aes128gcm.calcMTable(hash, pMTable);
            Aes128gcm.aesGcmGHash(pMTable, AAD.data, AAD.len, message.data, message.len, S);
            Aes128gcm.aesGCtr(aes, J0, S, S.length, T);
            System.arraycopy(plainText, 0, message.data, 0, message.len);
            boolean bl = Arrays.equals(auth, T);
            return bl;
        }
        finally {
            arraysPool.returnObject(hash);
            arraysPool.returnObject(J0);
            arraysPool.returnObject(S);
            arraysPool.returnObject(T);
        }
    }

    public static boolean aes128GcmDecrypt(byte[] key, byte[] nonce, byte[] msgBuf, int msgLen, byte[] addBuf, int addLen, byte[] outMac, byte[] keyBuffer, byte[] msgBuffer) throws NqException {
        Blob keyBlob = new Blob();
        Blob IVBlob = new Blob();
        Blob AADBlob = new Blob();
        Blob msgBlob = new Blob();
        keyBlob.data = key;
        keyBlob.len = 16;
        IVBlob.data = nonce;
        IVBlob.len = 12;
        AADBlob.data = addBuf;
        AADBlob.len = addLen;
        msgBlob.data = msgBuf;
        msgBlob.len = msgLen;
        if (null == arraysPool) {
            Aes128gcm.initialArraysPool();
        }
        return Aes128gcm.aes128GcmDecryptInternal(keyBlob, IVBlob, AADBlob, msgBlob, outMac, keyBuffer, msgBuffer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean aesGcmAuthEncrypt(byte[] key, int keyLen, byte[] iv, int ivLen, byte[] plain, int plainLen, byte[] aad, int aadLen, byte[] encrypted, byte[] tag) throws NqException {
        if (null == arraysPool) {
            Aes128gcm.initialArraysPool();
        }
        byte[] hash = arraysPool.borrowObject();
        byte[] J0 = arraysPool.borrowObject();
        byte[] S = arraysPool.borrowObject();
        byte[] pMTable = new byte[4096];
        try {
            int[] aes = Aes128gcm.aesGcmInitHashSubkey(key, keyLen, hash, null);
            if (null == aes) {
                boolean bl = false;
                return bl;
            }
            Aes128gcm.aesGcmPreparej0(iv, J0);
            Aes128gcm.aesGcmCtr(aes, J0, plain, plainLen, encrypted);
            Aes128gcm.calcMTable(hash, pMTable);
            Aes128gcm.aesGcmGHash(pMTable, aad, aadLen, encrypted, plainLen, S);
            Aes128gcm.aesGCtr(aes, J0, S, S.length, tag);
        }
        finally {
            arraysPool.returnObject(hash);
            arraysPool.returnObject(J0);
            arraysPool.returnObject(S);
        }
        return true;
    }

    static {
        Te0 = new int[]{-966564955, -126059388, -294160487, -159679603, -855539, -697603139, -563122255, -1849309868, 1613770832, 33620227, -832084055, 1445669757, -402719207, -1244145822, 1303096294, -327780710, -1882535355, 528646813, -1983264448, -92439161, -268764651, -1302767125, -1907931191, -68095989, 1101901292, -1277897625, 1604494077, 1169141738, 597466303, 1403299063, -462261610, -1681866661, 1974974402, -503448292, 1033081774, 1277568618, 1815492186, 2118074177, -168298750, -2083730353, 1748251740, 1369810420, -773462732, -101584632, -495881837, -1411852173, 1647391059, 706024767, 134480908, -1782069422, 1176707941, -1648114850, 806885416, 932615841, 168101135, 798661301, 235341577, 605164086, 461406363, -538779075, -840176858, 1311188841, 2142417613, -361400929, 302582043, 495158174, 1479289972, 874125870, 907746093, -596742478, -1269146898, 1537253627, -1538108682, 1983593293, -1210657183, 2108928974, 1378429307, -572267714, 1580150641, 327451799, -1504488459, -1177431704, 0, -1041371860, 1075847264, -469959649, 2041688520, -1235526675, -731223362, -1916023994, 1740553945, 1916352843, -1807070498, -1739830060, -1336387352, -2049978550, -1143943061, -974131414, 1336584933, -302253290, -2042412091, -1706209833, 0x66333355, 293963156, -1975171633, -369493744, 67240454, -25198719, -1605349136, 2017213508, 631218106, 1269344483, -1571728909, 1571005438, -2143272768, 93294474, 1066570413, 563977660, 1882732616, -235539196, 1673313503, 2008463041, -1344611723, 1109467491, 537923632, -436207846, -34344178, -1076702611, -2117218996, 403442708, 638784309, -1007883217, -1101045791, 899127202, -2008791860, 773265209, -1815821225, 1437050866, -58818942, 2050833735, -932944724, -1168286233, 840505643, -428641387, -1067425632, 0x19818198, -1638969391, -1545806721, 0x44222266, 1412049534, 999329963, 0xB888883, -1941551414, -940642775, 1807268051, 672404540, -1478566279, -1134666014, 369822493, -1378100362, -606019525, 1681011286, 1949973070, 336202270, -1840690725, 201721354, 1210328172, -1201906460, -1614626211, -1110191250, 1135389935, -1000185178, 965841320, 831886756, -739974089, -226920053, -706222286, -1949775805, 1849112409, -630362697, 26054028, -1311386268, -1672589614, 1235855840, -663982924, -1403627782, -202050553, -806688219, -899324497, -193299826, 1202630377, 0x10080818, 1874508501, -260540280, 1243948399, 1546530418, 941366308, 1470539505, 1941222599, -1748580783, -873928669, -1579295364, -395021156, 1042226977, -1773450275, 1639824860, 227249030, 260737669, -529502064, 2084453954, 1907733956, -865704278, -1874310952, 100860677, -134810111, 470683154, -1033805405, 1781871967, -1370007559, 1773779408, 394692241, -1715355304, 974986535, 664706745, -639508168, -336005101, 731420851, 0x22111133, -764843589, -1445340816, 126783113, 865375399, 765172662, 1008606754, 361203602, -907417312, -2016489911, -1437248001, 1344809080, -1512054918, 59542671, 1503764984, 0x9898980, 437062935, 1707065306, -672733647, -2076032314, -798463816, -2109652541, 697932208, 1512910199, 504303377, 2075177163, -1470868228, 0x6DBBBBD6, 739644986};
        Te1 = new int[]{-1513725085, -2064089988, -1712425097, -1913226373, 0xDFFF2F2, -1110021269, -1310822545, 1418839493, 1348481072, 50462977, -1446090905, 2102799147, 434634494, 1656084439, -431117397, -1695779210, 1167051466, -1658879358, 1082771913, -2013627011, 368048890, -340633255, -913422521, 0xBFBF0F0, -331240019, 1739838676, -44064094, -364531793, -1088185188, -145513308, -1763413390, 1536934080, -1032472649, 484572669, -1371696237, 1783375398, 1517041206, 1098792767, 49674231, 1334037708, 1550332980, -195975771, 886171109, 150598129, -1813876367, 1940642008, 1398944049, 1059722517, 201851908, 1385547719, 1699095331, 1587397571, 674240536, -1590192490, 252314885, -1255171430, 151914247, 908333586, -1692696448, 1038082786, 651029483, 1766729511, -847269198, -1612024459, 454166793, -1642232957, 1951935532, 775166490, 758520603, -1294176658, -290170278, -77881184, -157003182, 1299594043, 1639438038, -830622797, 2068982057, 0x3EDDE3E3, 1901997871, -1760328572, -173649069, 1757008337, 0, 750906861, 1614815264, 535035132, -931548751, -306816165, -1093375382, 1183697867, -647512386, 1265776953, -560706998, -728216500, -391096232, 1250283471, 1807470800, 717615087, -447763798, 384695291, -981056701, -677753523, 0x55663333, -1810791035, -813021883, 283769337, 100925954, -2114027649, -257929136, 1148730428, -1171939425, -481580888, -207466159, -27417693, -1065336768, -1979347057, -1388342638, -1138647651, 1215313976, 82966005, -547111748, -1049119050, 1974459098, 1665278241, 807407632, 451280895, 251524083, 1841287890, 1283575245, 337120268, 891687699, 801369324, -507617441, -1573546089, -863484860, 959321879, 1469301956, -229267545, -2097381762, 1199193405, -1396153244, -407216803, 724703513, -1780059277, -1598005152, -1743158911, -778154161, 2141445340, 0x66442222, 2119445034, -1422159728, -2096396152, -896776634, 700968686, -747915080, 1009259540, 2041044702, -490971554, 487983883, 1991105499, 1004265696, 1449407026, 1316239930, 504629770, -611169975, 168560134, 1816667172, -457679780, 1570751170, 1857934291, -280777556, -1497079198, -1472622191, -1540254315, 936633572, -1947043463, 852879335, 1133234376, 1500395319, -1210421907, -1946055283, 1689376213, -761508274, -532043351, -1260884884, -89369002, 133428468, 634383082, -1345690267, -1896580486, -381178194, 0x18100808, -714097990, -1997506440, 1867130149, 1918643758, 607656988, -245913946, -948718412, 1368901318, 600565992, 2090982877, -1662487436, 557719327, -577352885, -597574211, -2045932661, -2062579062, -1864339344, 1115438654, -999180875, -1429445018, -661632952, 84280067, 33027830, 303828494, -1547542175, 1600795957, -106014889, -798377543, -1860729210, 1486471617, 658119965, -1188585826, 953803233, 334231800, -1288988520, 0x33221111, -1143838359, 1890179545, -1995993458, -1489791852, -1238525029, 574365214, -1844082809, 550103529, 1233637070, -5614251, 2018519080, 2057691103, -1895592820, -128343647, -2146858615, 387583245, -630865985, 836232934, -964410814, -1194301336, -1014873791, -1339450983, 2002398509, 287182607, -881086288, -56077228, -697451589, 975967766};
        Te2 = new int[]{1671808611, 2089089148, 0x7799EE77, 2072901243, -233963534, 0x6BBDD66B, 1873927791, -984313403, 810573872, 16974337, 1739181671, 729634347, -31856642, -681396777, -1410970197, 1989864566, -901410870, -2103631998, -918517303, 2106063485, -99225606, 1508618841, 1204391495, -267650064, -1377025619, -731401260, -1560453214, -1343601233, -1665195108, -1527295068, 1922491506, -1067738176, -1211992649, -48438787, -1817297517, 644500518, 911895606, 1061256767, -150800905, -867204148, 878471220, -1510714971, -449523227, -251069967, 1905517169, -663508008, 827548209, 356461077, 67897348, -950889017, 593839651, -1017209405, 405286936, -1767819370, 84871685, -1699401830, 118033927, 305538066, -2137318528, -499261470, -349778453, 661212711, -1295155278, 1973414517, 152769033, -2086789757, 745822252, 439235610, 455947803, 1857215598, 1525593178, -1594139744, 1391895634, 994932283, -698239018, -1278313037, 695947817, -482419229, 795958831, -2070473852, 1408607827, -781665839, 0, -315833875, 543178784, -65018884, -1312261711, 1542305371, 1790891114, -884568629, -1093048386, 961245753, 1256100938, 1289001036, 1491644504, -817199665, -798245936, -282409489, -1427812438, -82383365, 1137018435, 1305975373, 0x33556633, -2053893755, 1171229253, -116332039, 33948674, 2139225727, 1357946960, 1011120188, -1615190625, -1461498968, 1374921297, -1543610973, 1086357568, -1886780017, -1834139758, -1648615011, 944271416, -184225291, -1126210628, -1228834890, -629821478, 560153121, 271589392, -15014401, -217121293, -764559406, -850624051, 202643468, 322250259, -332413972, 1608629855, -1750977129, 0x44CC8844, 389623319, -1000893500, -1477290585, 2122513534, 1028094525, 1689045092, 1575467613, 422261273, 1939203699, 1621147744, -2120738431, 1339137615, -595614756, 0x22664422, 712922154, -1867826288, -2004677752, 1187679302, -299251730, -1194103880, 339486740, -562452514, 1591917662, 186455563, -612979237, -532948000, 844522546, 978220090, 169743370, 1239126601, 101321734, 611076132, 1558493276, -1034051646, -747717165, -1393605716, 1655096418, -1851246191, -1784401515, -466103324, 2039214713, -416098841, -935097400, 928607799, 1840765549, -1920204403, -714821163, 1322425422, -1444918871, 1823791212, 1459268694, -200805388, -366620694, 1706019429, 2056189050, -1360443474, 0x8181008, -1160417350, 2022240376, 628050469, 779246638, 472135708, -1494132826, -1261997132, -967731258, -400307224, -579034659, 1956440180, 522272287, 1272813131, -1109630531, -1954148981, -1970991222, 1888542832, 1044544574, -1245417035, 0x66AACC66, 1222152264, 50660867, -167643146, 236067854, 1638122081, 895445557, 1475980887, -1177523783, -2037311610, -1051158079, 489110045, -1632032866, -516367903, -132912136, -1733088360, 0x11332211, 1773916777, -646927911, -1903622258, -1800981612, -1682559589, 505560094, -2020469369, -383727127, -834041906, 0x55FFAA55, 678973480, -545610273, -1936784500, -1577559647, -1988097655, 219617805, -1076206145, -432941082, 1120306242, 1756942440, 1103331905, -1716508263, 762796589, 252780047, -1328841808, 1425844308, -1143575109, 372911126};
        Te3 = new int[]{1667474886, 2088535288, 0x777799EE, 2071694838, -219017729, 0x6B6BBDD6, 1869591006, -976923503, 808472672, 16843522, 1734846926, 724270422, -16901657, -673750347, -1414797747, 1987484396, -892713585, -2105369313, -909557623, 2105378810, -84273681, 1499065266, 1195886990, -252703749, -1381110719, -724277325, -1566376609, -1347425723, -1667449053, -1532692653, 1920112356, -1061135461, -1212693899, -33743647, -1819038147, 640051788, 909531756, 1061110142, -134806795, -859025533, 875846760, -1515850671, -437963567, -235861767, 1903268834, -656903253, 825316194, 353713962, 67374088, -943238507, 589522246, -1010606435, 404236336, -1768513225, 84217610, -1701137105, 117901582, 303183396, -2139055333, -488489505, -336910643, 656894286, -1296904833, 1970642922, 151591698, -2088526307, 741110872, 437923380, 454765878, 1852748508, 1515908788, -1600062629, 1381168804, 993742198, -690593353, -1280061827, 690584402, -471646499, 791638366, -2071685357, 1398011302, -774805319, 0, -303223615, 538992704, -50585629, -1313748871, 1532751286, 1785380564, -875870579, -1094788761, 960056178, 1246420628, 1280103576, 1482221744, -808498555, -791647301, -269538619, -1431640753, -67430675, 1128514950, 1296947098, 0x33335566, -2054843375, 1162203018, -101117719, 33687044, 2139062782, 1347481760, 1010582648, -1616922075, -1465326773, 1364325282, -1549533603, 1077985408, -1886418427, -1835881153, -1650607071, 943212656, -168491791, -1128472733, -1229536905, -623217233, 555836226, 269496352, -58651, -202174723, -757961281, -842183551, 202118168, 320025894, -320065597, 1600119230, -1751670219, 0x4444CC88, 387397934, -993765485, -1482165675, 2122220284, 1027426170, 1684319432, 1566435258, 421079858, 1936954854, 1616945344, -2122213351, 1330631070, -589529181, 0x22226644, 707427924, -1869567173, -2004319477, 1179044492, -286381625, -1195846805, 336870440, -555845209, 1583276732, 185277718, -606374227, -522175525, 842159716, 976899700, 168435220, 1229577106, 101059084, 606366792, 1549591736, -1027449441, -741118275, -1397952701, 1650632388, -1852725191, -1785355215, -454805549, 2038008818, -404278571, -926399605, 926374254, 1835907034, -1920103423, -707435343, 1313788572, -1448484791, 1819063512, 1448540844, -185333773, -353753649, 1701162954, 2054852340, -1364268729, 0x8081810, -1162160785, 2021165296, 623210314, 774795868, 471606328, -1499008681, -1263220877, -960081513, -387439669, -572687199, 1953799400, 522133822, 1263263126, -1111630751, -1953790451, -1970633457, 1886425312, 1044267644, -1246378895, 0x6666AACC, 1212733584, 50529542, -151649801, 235803164, 1633788866, 892690282, 1465383342, -1179004823, -2038001385, -1044293479, 488449850, -1633765081, -505333543, -117959701, -1734823125, 0x11113322, 1768537042, -640061271, -1903261433, -1802197197, -1684294099, 505291324, -2021158379, -370597687, -825341561, 0x5555FFAA, 673740880, -539002203, -1936945405, -1583220647, -1987477495, 218961690, -1077945755, -421121577, 1111672452, 1751693520, 1094828930, -1717981143, 757954394, 252645662, -1330590853, 1414855848, -1145317779, 370555436};
        Te4 = new int[]{0x63636363, 0x7C7C7C7C, 0x77777777, 0x7B7B7B7B, -218959118, 0x6B6B6B6B, 0x6F6F6F6F, -976894523, 0x30303030, 0x1010101, 0x67676767, 0x2B2B2B2B, -16843010, -673720361, -1414812757, 0x76767676, -892679478, -2105376126, -909522487, 0x7D7D7D7D, -84215046, 0x59595959, 0x47474747, -252645136, -1381126739, -724249388, -1566399838, -1347440721, -1667457892, -1532713820, 0x72727272, -1061109568, -1212696649, -33686019, -1819044973, 0x26262626, 0x36363636, 0x3F3F3F3F, -134744073, -858993460, 0x34343434, -1515870811, -437918235, -235802127, 0x71717171, -656877352, 0x31313131, 0x15151515, 0x4040404, -943208505, 0x23232323, -1010580541, 0x18181818, -1768515946, 0x5050505, -1701143910, 0x7070707, 0x12121212, -2139062144, -488447262, -336860181, 0x27272727, -1296911694, 0x75757575, 0x9090909, -2088533117, 0x2C2C2C2C, 0x1A1A1A1A, 0x1B1B1B1B, 0x6E6E6E6E, 0x5A5A5A5A, -1600085856, 0x52525252, 0x3B3B3B3B, -690563370, -1280068685, 0x29292929, -471604253, 0x2F2F2F2F, -2071690108, 0x53535353, -774778415, 0, -303174163, 0x20202020, -50529028, -1313754703, 0x5B5B5B5B, 0x6A6A6A6A, -875836469, -1094795586, 0x39393939, 0x4A4A4A4A, 0x4C4C4C4C, 0x58585858, -808464433, -791621424, -269488145, -1431655766, -67372037, 0x43434343, 0x4D4D4D4D, 0x33333333, -2054847099, 0x45454545, -101058055, 0x2020202, 0x7F7F7F7F, 0x50505050, 0x3C3C3C3C, -1616928865, -1465341784, 0x51515151, -1549556829, 0x40404040, -1886417009, -1835887982, -1650614883, 0x38383838, -168430091, -1128481604, -1229539658, -623191334, 0x21212121, 0x10101010, -1, -202116109, -757935406, -842150451, 0xC0C0C0C, 0x13131313, -320017172, 0x5F5F5F5F, -1751672937, 0x44444444, 0x17171717, -993737532, -1482184793, 0x7E7E7E7E, 0x3D3D3D3D, 0x64646464, 0x5D5D5D5D, 0x19191919, 0x73737373, 0x60606060, -2122219135, 0x4F4F4F4F, -589505316, 0x22222222, 0x2A2A2A2A, -1869574000, -2004318072, 0x46464646, -286331154, -1195853640, 0x14141414, -555819298, 0x5E5E5E5E, 0xB0B0B0B, -606348325, -522133280, 0x32323232, 0x3A3A3A3A, 0xA0A0A0A, 0x49494949, 0x6060606, 0x24242424, 0x5C5C5C5C, -1027423550, -741092397, -1397969748, 0x62626262, -1852730991, -1785358955, -454761244, 0x79797979, -404232217, -926365496, 0x37373737, 0x6D6D6D6D, -1920103027, -707406379, 0x4E4E4E4E, -1448498775, 0x6C6C6C6C, 0x56565656, -185273100, -353703190, 0x65656565, 0x7A7A7A7A, -1364283730, 0x8080808, -1162167622, 0x78787878, 0x25252525, 0x2E2E2E2E, 0x1C1C1C1C, -1499027802, -1263225676, -960051514, -387389208, -572662307, 0x74747474, 0x1F1F1F1F, 0x4B4B4B4B, -1111638595, -1953789045, -1970632054, 0x70707070, 0x3E3E3E3E, -1246382667, 0x66666666, 0x48484848, 0x3030303, -151587082, 0xE0E0E0E, 0x61616161, 0x35353535, 0x57575757, -1179010631, -2038004090, -1044266559, 0x1D1D1D1D, -1633771874, -505290271, -117901064, -1734829928, 0x11111111, 0x69696969, -640034343, -1903260018, -1802201964, -1684300901, 0x1E1E1E1E, -2021161081, -370546199, -825307442, 0x55555555, 0x28282828, -538976289, -1936946036, -1583242847, -1987475063, 0xD0D0D0D, -1077952577, -421075226, 0x42424242, 0x68686868, 0x41414141, -1717986919, 0x2D2D2D2D, 0xF0F0F0F, -1330597712, 0x54545454, -1145324613, 0x16161616};
        rcon = new int[]{0x1000000, 0x2000000, 0x4000000, 0x8000000, 0x10000000, 0x20000000, 0x40000000, Integer.MIN_VALUE, 0x1B000000, 0x36000000};
        RTable = new int[]{0, 0, 1, 194, 3, 132, 2, 70, 7, 8, 6, 202, 4, 140, 5, 78, 14, 16, 15, 210, 13, 148, 12, 86, 9, 24, 8, 218, 10, 156, 11, 94, 28, 32, 29, 226, 31, 164, 30, 102, 27, 40, 26, 234, 24, 172, 25, 110, 18, 48, 19, 242, 17, 180, 16, 118, 21, 56, 20, 250, 22, 188, 23, 126, 56, 64, 57, 130, 59, 196, 58, 6, 63, 72, 62, 138, 60, 204, 61, 14, 54, 80, 55, 146, 53, 212, 52, 22, 49, 88, 48, 154, 50, 220, 51, 30, 36, 96, 37, 162, 39, 228, 38, 38, 35, 104, 34, 170, 32, 236, 33, 46, 42, 112, 43, 178, 41, 244, 40, 54, 45, 120, 44, 186, 46, 252, 47, 62, 112, 128, 113, 66, 115, 4, 114, 198, 119, 136, 118, 74, 116, 12, 117, 206, 126, 144, 127, 82, 125, 20, 124, 214, 121, 152, 120, 90, 122, 28, 123, 222, 108, 160, 109, 98, 111, 36, 110, 230, 107, 168, 106, 106, 104, 44, 105, 238, 98, 176, 99, 114, 97, 52, 96, 246, 101, 184, 100, 122, 102, 60, 103, 254, 72, 192, 73, 2, 75, 68, 74, 134, 79, 200, 78, 10, 76, 76, 77, 142, 70, 208, 71, 18, 69, 84, 68, 150, 65, 216, 64, 26, 66, 92, 67, 158, 84, 224, 85, 34, 87, 100, 86, 166, 83, 232, 82, 42, 80, 108, 81, 174, 90, 240, 91, 50, 89, 116, 88, 182, 93, 248, 92, 58, 94, 124, 95, 190, 225, 0, 224, 194, 226, 132, 227, 70, 230, 8, 231, 202, 229, 140, 228, 78, 239, 16, 238, 210, 236, 148, 237, 86, 232, 24, 233, 218, 235, 156, 234, 94, 253, 32, 252, 226, 254, 164, 255, 102, 250, 40, 251, 234, 249, 172, 248, 110, 243, 48, 242, 242, 240, 180, 241, 118, 244, 56, 245, 250, 247, 188, 246, 126, 217, 64, 216, 130, 218, 196, 219, 6, 222, 72, 223, 138, 221, 204, 220, 14, 215, 80, 214, 146, 212, 212, 213, 22, 208, 88, 209, 154, 211, 220, 210, 30, 197, 96, 196, 162, 198, 228, 199, 38, 194, 104, 195, 170, 193, 236, 192, 46, 203, 112, 202, 178, 200, 244, 201, 54, 204, 120, 205, 186, 207, 252, 206, 62, 145, 128, 144, 66, 146, 4, 147, 198, 150, 136, 151, 74, 149, 12, 148, 206, 159, 144, 158, 82, 156, 20, 157, 214, 152, 152, 153, 90, 155, 28, 154, 222, 141, 160, 140, 98, 142, 36, 143, 230, 138, 168, 139, 106, 137, 44, 136, 238, 131, 176, 130, 114, 128, 52, 129, 246, 132, 184, 133, 122, 135, 60, 134, 254, 169, 192, 168, 2, 170, 68, 171, 134, 174, 200, 175, 10, 173, 76, 172, 142, 167, 208, 166, 18, 164, 84, 165, 150, 160, 216, 161, 26, 163, 92, 162, 158, 181, 224, 180, 34, 182, 100, 183, 166, 178, 232, 179, 42, 177, 108, 176, 174, 187, 240, 186, 50, 184, 116, 185, 182, 188, 248, 189, 58, 191, 124, 190, 190};
    }
}

