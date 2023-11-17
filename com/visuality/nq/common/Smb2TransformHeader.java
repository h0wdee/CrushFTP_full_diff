/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Smb2Header;

public class Smb2TransformHeader
extends Smb2Header {
    public static final byte[] smb2TrnsfrmHdrProtocolId = new byte[]{-3, 83, 77, 66};
    public static final int SMB2_TRANSFORMHEADER_SIZE = 52;
    private byte[] signature = new byte[16];
    public byte[] nonce = new byte[16];
    int originalMsgSize;
    short encryptionArgorithm;
    private long sid = 0L;

    public byte[] getSignature() {
        return this.signature;
    }

    public byte[] getNonce() {
        return this.nonce;
    }

    public int getOriginalMsgSize() {
        return this.originalMsgSize;
    }

    public long getSid() {
        return this.sid;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public void setOriginalMsgSize(int originalMsgSize) {
        this.originalMsgSize = originalMsgSize;
    }

    public short getEncryptionArgorithm() {
        return this.encryptionArgorithm;
    }

    public void setEncryptionArgorithm(short encryptionArgorithm) {
        this.encryptionArgorithm = encryptionArgorithm;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public static boolean writeHeader(Smb2TransformHeader header, BufferWriter writer) {
        writer.writeBytes(smb2TrnsfrmHdrProtocolId, smb2TrnsfrmHdrProtocolId.length);
        writer.skip(header.signature.length);
        writer.writeBytes(header.nonce, header.nonce.length);
        writer.writeInt4(header.originalMsgSize);
        writer.writeZeros(2);
        writer.writeInt2(header.encryptionArgorithm);
        writer.writeLong(header.sid);
        return true;
    }

    public static void readHeader(Smb2TransformHeader header, BufferReader reader) throws NqException {
        reader.skip(4);
        reader.readBytes(header.signature, header.signature.length);
        reader.readBytes(header.nonce, header.nonce.length);
        header.originalMsgSize = reader.readInt4();
        reader.skip(2);
        header.encryptionArgorithm = reader.readInt2();
        header.sid = reader.readLong();
    }
}

