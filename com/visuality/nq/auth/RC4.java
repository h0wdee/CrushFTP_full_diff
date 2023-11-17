/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

public class RC4 {
    public static void Crypt(byte[] data, int dataLen, byte[] key, int keyLen) {
        byte tc;
        int idx;
        byte[] sBox = new byte[258];
        int j = 0;
        int idxI = 0;
        int idxJ = 0;
        for (idx = 0; idx < 256; ++idx) {
            sBox[idx] = (byte)idx;
        }
        for (idx = 0; idx < 256; ++idx) {
            j = (j + RC4.byteToUnsignedInt(sBox[idx]) + RC4.byteToUnsignedInt(key[idx % keyLen])) % 256;
            tc = sBox[idx];
            sBox[idx] = sBox[j];
            sBox[j] = tc;
        }
        sBox[256] = 0;
        sBox[257] = 0;
        for (idx = 0; idx < dataLen; ++idx) {
            idxI = idxI == 255 ? 0 : idxI + 1;
            idxJ = (idxJ + RC4.byteToUnsignedInt(sBox[idxI])) % 256;
            tc = sBox[idxI];
            sBox[idxI] = sBox[idxJ];
            sBox[idxJ] = tc;
            int t = (RC4.byteToUnsignedInt(sBox[idxI]) + RC4.byteToUnsignedInt(sBox[idxJ])) % 256;
            data[idx] = (byte)(data[idx] ^ sBox[t]);
        }
        sBox[256] = (byte)idxI;
        sBox[257] = (byte)idxJ;
    }

    private static int byteToUnsignedInt(byte b) {
        return b < 0 ? b & 0xFF : b;
    }
}

