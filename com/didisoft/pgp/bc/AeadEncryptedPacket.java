/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.BCPGInputStream
 *  lw.bouncycastle.bcpg.InputStreamPacket
 *  lw.bouncycastle.crypto.BlockCipher
 *  lw.bouncycastle.crypto.engines.AESEngine
 *  lw.bouncycastle.crypto.modes.AEADBlockCipher
 *  lw.bouncycastle.crypto.modes.EAXBlockCipher
 *  lw.bouncycastle.crypto.modes.GCMBlockCipher
 *  lw.bouncycastle.crypto.modes.OCBBlockCipher
 */
package com.didisoft.pgp.bc;

import java.io.IOException;
import java.io.InputStream;
import lw.bouncycastle.bcpg.BCPGInputStream;
import lw.bouncycastle.bcpg.InputStreamPacket;
import lw.bouncycastle.crypto.BlockCipher;
import lw.bouncycastle.crypto.engines.AESEngine;
import lw.bouncycastle.crypto.modes.AEADBlockCipher;
import lw.bouncycastle.crypto.modes.EAXBlockCipher;
import lw.bouncycastle.crypto.modes.GCMBlockCipher;
import lw.bouncycastle.crypto.modes.OCBBlockCipher;

public class AeadEncryptedPacket
extends InputStreamPacket {
    private byte version;
    private byte algorithm;
    private byte mode;
    private byte chunkSizeBytes;
    private byte[] IV;
    private InputStream Payload;
    public static final byte MODE_EAX = 1;
    public static final byte MODE_OCB = 2;
    public static final byte MODE_GCM = 100;

    public AeadEncryptedPacket(BCPGInputStream bCPGInputStream) throws IOException {
        super(bCPGInputStream);
        this.version = (byte)bCPGInputStream.read();
        if (this.version != 1) {
            throw new IllegalArgumentException("Wrong AEAD packet version: " + this.version);
        }
        this.algorithm = (byte)bCPGInputStream.read();
        this.mode = (byte)bCPGInputStream.read();
        this.chunkSizeBytes = (byte)bCPGInputStream.read();
        this.IV = new byte[this.getIvLenght(this.mode)];
        bCPGInputStream.read(this.getIV(), 0, this.getIV().length);
        this.Payload = bCPGInputStream;
    }

    public int getChunkSize() {
        return (int)Math.pow(2.0, this.chunkSizeBytes + 6);
    }

    private int getIvLenght(byte by) {
        switch (by) {
            case 1: {
                return 16;
            }
            case 2: {
                return 15;
            }
            case 100: {
                return 12;
            }
        }
        throw new IllegalArgumentException("mode");
    }

    public AEADBlockCipher createCipher() {
        switch (this.mode) {
            case 1: {
                return new EAXBlockCipher((BlockCipher)new AESEngine());
            }
            case 2: {
                return new OCBBlockCipher((BlockCipher)new AESEngine(), (BlockCipher)new AESEngine());
            }
            case 100: {
                return new GCMBlockCipher((BlockCipher)new AESEngine());
            }
        }
        throw new IllegalArgumentException("mode");
    }

    public byte[] getNonce(long l) {
        switch (this.mode) {
            case 1: {
                byte[] byArray = new byte[16];
                int n = 8;
                System.arraycopy(this.IV, 0, byArray, 0, byArray.length);
                int n2 = n++;
                byArray[n2] = (byte)(byArray[n2] ^ (byte)(l >> 56));
                int n3 = n++;
                byArray[n3] = (byte)(byArray[n3] ^ (byte)(l >> 48));
                int n4 = n++;
                byArray[n4] = (byte)(byArray[n4] ^ (byte)(l >> 40));
                int n5 = n++;
                byArray[n5] = (byte)(byArray[n5] ^ (byte)(l >> 32));
                int n6 = n++;
                byArray[n6] = (byte)(byArray[n6] ^ (byte)(l >> 24));
                int n7 = n++;
                byArray[n7] = (byte)(byArray[n7] ^ (byte)(l >> 16));
                int n8 = n++;
                byArray[n8] = (byte)(byArray[n8] ^ (byte)(l >> 8));
                int n9 = n++;
                byArray[n9] = (byte)(byArray[n9] ^ (byte)l);
                return byArray;
            }
            case 2: {
                byte[] byArray = new byte[15];
                int n = 7;
                System.arraycopy(this.IV, 0, byArray, 0, byArray.length);
                int n10 = n++;
                byArray[n10] = (byte)(byArray[n10] ^ (byte)(l >> 56));
                int n11 = n++;
                byArray[n11] = (byte)(byArray[n11] ^ (byte)(l >> 48));
                int n12 = n++;
                byArray[n12] = (byte)(byArray[n12] ^ (byte)(l >> 40));
                int n13 = n++;
                byArray[n13] = (byte)(byArray[n13] ^ (byte)(l >> 32));
                int n14 = n++;
                byArray[n14] = (byte)(byArray[n14] ^ (byte)(l >> 24));
                int n15 = n++;
                byArray[n15] = (byte)(byArray[n15] ^ (byte)(l >> 16));
                int n16 = n++;
                byArray[n16] = (byte)(byArray[n16] ^ (byte)(l >> 8));
                int n17 = n++;
                byArray[n17] = (byte)(byArray[n17] ^ (byte)l);
                return byArray;
            }
            case 100: {
                return this.IV;
            }
        }
        throw new IllegalArgumentException();
    }

    public byte getVersion() {
        return this.version;
    }

    public byte getAlgorithm() {
        return this.algorithm;
    }

    public byte getMode() {
        return this.mode;
    }

    public int getChunkSizeBytes() {
        return this.chunkSizeBytes;
    }

    public byte[] getIV() {
        return this.IV;
    }

    public InputStream getDataStream() {
        return this.Payload;
    }

    public int getTagLength() {
        switch (this.mode) {
            case 1: {
                return 16;
            }
            case 2: {
                return 16;
            }
            case 100: {
                return 16;
            }
        }
        throw new IllegalArgumentException();
    }
}

